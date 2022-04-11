package us.ihmc.concurrent.runtime.barrierScheduler.implicitContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A realtime-safe thread scheduler that triggers its tasks at integer divisors of its own loop
 * rate.
 * <p>
 * The scheduler maintains a master context object that is populated by update functions implemented
 * in each task. This master context is first bubbled up from the tasks, then the updates are
 * bubbled back down.
 *
 * @param <C> the context type
 */
public class BarrierScheduler<C> implements Runnable
{
   /**
    * If {@link #overrunBehavior} is {@link TaskOverrunBehavior#BUSY_WAIT}, this is the time resolution
    * at which to busy wait. This should be much faster than the fastest loop period in the scheduler.
    */
   private static final int BUSY_SLEEP_RESOLUTION_NS = 10000; // 10 us

   /**
    * The collection of {@link Task}s which this scheduler is responsible for scheduling.
    */
   private final List<? extends Task<C>> tasks;

   /**
    * A boolean for every task that is used as a temporary flag indicating the task is pending and
    * sleeping.
    */
   private final boolean[] releaseTasks;

   /**
    * The master context. This is the context to which all tasks bubble up their outputs. Immediately
    * after a scheduler tick this context object holds the latest context data from all threads that
    * were sleeping on the tick, and possible stale data from those that have not yet fallen asleep.
    */
   private final C masterContext;

   /**
    * Describes the different possible behaviors for when a task overruns into its next execution
    * period.
    */
   public enum TaskOverrunBehavior
   {
      /**
       * An overrunning task will be busy-waited on until it completes. This will block the scheduler.
       * This is useful for simulation where a slow task implementation should block the simulation thread
       * until it completes, ensuring a consistent simulation-clock loop rate.
       * <p>
       * NOTE: This should not be used in a realtime context as it will cause other non-overrun loops to
       * miss their deadlines.
       */
      BUSY_WAIT,

      /**
       * An overrunning task will be allowed to continue while the scheduler schedules the rest of the
       * tasks. Its next iterations are skipped until it finishes.
       */
      SKIP_TICK,
   }

   /**
    * The task overrun behavior.
    *
    * @see TaskOverrunBehavior
    */
   private final TaskOverrunBehavior overrunBehavior;

   /**
    * How to handle exception thrown by a task.
    */
   private TaskExceptionHandler<C> taskExceptionHandler = TaskExceptionHandler.newTaskExceptionThrower();

   /**
    * The scheduler tick counter. This value is incremented each time the scheduler executes.
    */
   private long tick;

   /**
    * Constructs a new scheduler for a fixed task collection.
    * <p>
    * After creation the scheduler must be scheduled at a fixed rate on a thread and the tasks must
    * individually be started on their own threads (not scheduled, just run once).
    *
    * @param tasks         the fixed collection of tasks to be scheduler
    * @param masterContext the master context object
    */
   public BarrierScheduler(Collection<? extends Task<C>> tasks, C masterContext, TaskOverrunBehavior overrunBehavior)
   {
      this.tasks = new ArrayList<>(tasks); // defensive copy
      this.releaseTasks = new boolean[tasks.size()];
      this.masterContext = masterContext;
      this.overrunBehavior = overrunBehavior;
   }

   /**
    * Sets a new behavior for the scheduler for when a task throws an exception.
    * 
    * @param taskExceptionHandler the new handler for managing exceptions. The default implementation,
    *                             wraps the task's exception in a {@link BarrierSchedulerException} and
    *                             throws it.
    */
   public void setTaskExceptionHandler(TaskExceptionHandler<C> taskExceptionHandler)
   {
      Objects.requireNonNull(taskExceptionHandler);
      this.taskExceptionHandler = taskExceptionHandler;
   }

   @Override
   public void run()
   {
      // If busy wait is enabled, sleep until all pending-execution tasks are sleeping.
      if (overrunBehavior == TaskOverrunBehavior.BUSY_WAIT)
      {
         while (!allTasksOnSchedule())
         {
            try
            {
               // TODO: Implement a poll() method on the task's barrier so we can block until it
               // completes rather than busy-waiting
               Thread.sleep(0, BUSY_SLEEP_RESOLUTION_NS);
            }
            catch (InterruptedException e)
            {
               // Keep sleeping
            }
         }
      }

      // Record whether a task is ready to be released. Do this once to avoid inconsistency in case
      // the task becomes ready in between the following for loops.
      for (int i = 0; i < tasks.size(); i++)
      {
         Task<C> task = tasks.get(i);
         releaseTasks[i] = task.isPending(tick) && task.isSleeping();
      }

      // Copy data from pending and sleeping tasks to the master context object.
      for (int i = 0; i < tasks.size(); i++)
      {
         // Wait until a task's NEXT execution cycle to copy the output data from it's PREVIOUS
         // cycle. A possible optimization is to copy the data immediately when the thread goes
         // to sleep, even if it has ticks remaining until it will wake up again. But, this
         // introduces variable latency, so is not used here.
         if (releaseTasks[i])
            tasks.get(i).updateMasterContext(masterContext);
      }

      // Copy data down from the master context object to threads that are pending and sleeping. No
      // use in preemptively copying to a task that is sleeping but isn't pending since it won't do
      // anything with the data until its next execution cycle.
      for (int i = 0; i < tasks.size(); i++)
      {
         if (releaseTasks[i])
            tasks.get(i).updateLocalContext(masterContext);
      }

      // Release pending and sleeping tasks to kick them off again.
      for (int i = 0; i < tasks.size(); i++)
      {
         if (releaseTasks[i])
         {
            // We have already verified that the job is waiting on the barrier. This call to
            // release() will return immediately and free the job thread to execute another
            // iteration.
            if (!tasks.get(i).release())
            {
               // Something has gone unsynchronized... just give up.
               throw new BarrierSchedulerException("tried to release an unwaiting task");
            }
         }
      }

      for (int i = 0; i < tasks.size(); i++)
      {
         Task<C> task = tasks.get(i);
         if (task.hasThrownException())
         {
            boolean restart = taskExceptionHandler.handleException(task, task.getThrownException());

            if (restart)
               task.clearExceptionAndResume();
            else
               shutdown();
         }
      }

      tick++;
   }

   private boolean allTasksOnSchedule()
   {
      for (int i = 0; i < tasks.size(); i++)
      {
         Task<C> task = tasks.get(i);

         // If the task is scheduled to run this tick but isn't yet sleeping then it is behind
         // schedule.
         if (task.isPending(tick) && !task.isSleeping())
            return false;
      }

      return true;
   }

   /**
    * Waits until every task is sleeping.
    * <p>
    * This should be called only for pausing the scheduler and its tasks.
    * </p>
    */
   public void waitUntilTasksDone() throws InterruptedException
   {
      long initialTick = tick;

      while (true)
      {
         if (tick > initialTick)
            throw new IllegalStateException("Cannot invoke waitUntilTasksDone while the scheduler is still running");

         boolean allDone = true;

         for (int i = 0; i < tasks.size(); i++)
         {
            if (!tasks.get(i).isSleeping())
            {
               allDone = false;
               break;
            }
         }

         if (allDone)
            break;

         Thread.sleep(1);
      }

      // Update master context and each task's local context before giving back the hand.
      for (int i = 0; i < tasks.size(); i++)
      {
         tasks.get(i).updateMasterContext(masterContext);
      }

      for (int i = 0; i < tasks.size(); i++)
      {
         tasks.get(i).updateLocalContext(masterContext);
      }
   }

   /**
    * Requests shutdown on all tasks. This is a blocking call. After calling this do not call the
    * {@link #run()} method anymore.
    */
   public void shutdown()
   {
      for (int i = 0; i < tasks.size(); i++)
      {
         Task<C> task = tasks.get(i);

         while (!task.hasShutdown())
         {
            task.requestShutdown();
         }
      }
   }

   /**
    * This interface can be used to provide the desired scheduler's behavior when one of the tasks is
    * throwing an exception.
    *
    * @param <C> the type of the context used by the task.
    */
   public static interface TaskExceptionHandler<C>
   {
      /**
       * Called when the given {@code task} has just thrown the given {@code exception}.
       * <p>
       * Implement this method to specify the desired behavior of the scheduler.
       * </p>
       * 
       * @param task      the task that has just thrown an exception.
       * @param exception the exception that has been thrown.
       * @return {@code true} if the task can recover from the exception and thus should be resumed,
       *         {@code false} if the exception is not recoverable and the scheduler should shutdown.
       */
      boolean handleException(Task<C> task, Exception exception);

      static <C> TaskExceptionHandler<C> newTaskExceptionThrower()
      {
         return (task, exception) ->
         {
            throw new BarrierSchedulerException("Unhandled exception:", exception);
         };
      }
   }
}
