package us.ihmc.concurrent.runtime.barrierScheduler.implicitContext;

import java.util.concurrent.atomic.AtomicReference;

import us.ihmc.concurrent.runtime.barrierScheduler.implicitContext.BarrierScheduler.TaskOverrunBehavior;

public abstract class Task<C> implements Runnable
{
   /**
    * The positive integer divisor of the scheduler frequency at which this task should execute.
    */
   private final long divisor;

   /**
    * The barrier used to await each execution cycle in order to synchronize the task to its divisor of
    * the scheduler tick frequency.
    */
   // participants: scheduler, task
   private final ParkingBarrier barrier = new ParkingBarrier();

   /**
    * Whether or not the {@link #initialize()} method has been called.
    */
   private boolean initialized;

   /**
    * Signals to the task to break its loop and run its {@link #cleanup()} method.
    */
   private boolean shutdownRequested = false;

   private boolean hasShutdown = false;

   /** Delay expressed in number of scheduler ticks for this task. */
   private long delay = 0;

   private final AtomicReference<Exception> thrownException = new AtomicReference<>(null);

   /**
    * Creates a new task, which can later be scheduled.
    *
    * @param divisor the divisor of the scheduler frequency
    */
   public Task(long divisor)
   {
      if (divisor <= 0)
         throw new IllegalArgumentException("divisor must be > 0");

      this.divisor = divisor;
   }

   /**
    * Initializes the internal state of the task.
    * <p>
    * Called once, immediately before the first {@link #execute()} call. This method is executed on the
    * task's thread.
    */
   protected abstract boolean initialize();

   /**
    * Executes a single iteration of this task.
    * <p>
    * This method is executed on the task's thread.
    */
   protected abstract void execute();

   /**
    * Perform any cleanup before shutting down. Called the next tick after a shutdown request from the
    * barrier scheduler.
    */
   protected abstract void cleanup();

   /**
    * Bubbles internal context data up the the master context.
    * <p>
    * Do not hold a reference to {@code context}. Instead, copy internal data to it as quickly as
    * possible then drop the reference. This method is called on the scheduler's thread while the task
    * is sleeping.
    *
    * @param context the master context
    */
   protected abstract void updateMasterContext(C context);

   /**
    * Bubbles master context data down to this task's internal state.
    * <p>
    * Do not hold a reference to {@code context}. Instead, copy data from it as quickly as possible
    * then drop the reference. This method is called on the scheduler's thread while the task is
    * sleeping.
    *
    * @param context the master context
    */
   protected abstract void updateLocalContext(C context);

   /**
    * Returns whether or not this task should be nominally scheduled to execute on this tick. If it is,
    * the scheduler should do its best to execute the <b>on this tick</b>.
    *
    * @param schedulerTick the current scheduler tick
    * @return {@code true} if this task should execute on this tick.
    */
   boolean isPending(long schedulerTick)
   {
      return (schedulerTick - delay) % divisor == 0;
   }

   /**
    * Returns whether or not this task has finished its work and is sleeping, waiting for the next
    * execution.
    *
    * @return {@code true} if the task is asleep.
    */
   boolean isSleeping()
   {
      return barrier.isSleeping();
   }

   /**
    * Increment the delay, expressed in number of scheduler ticks, used for scheduling the next tick
    * execution for this task.
    * <p>
    * The delay is only used when the barrier scheduler is using
    * {@link TaskOverrunBehavior#SKIP_SCHEDULER_TICK}.
    * </p>
    */
   void incrementDelay()
   {
      delay++;
   }

   /**
    * Releases the task to continue its execution for another cycle.
    *
    * @see ParkingBarrier#release()
    */
   boolean release()
   {
      return barrier.release();
   }

   /**
    * Package private method to begin shutdown process. This will wake up a sleeping task, which will
    * then skip its next execution and go straight to its {@link #cleanup()}
    */
   void requestShutdown()
   {
      shutdownRequested = true;
      if (isSleeping())
      {
         release();
      }
   }

   /**
    * Method to see if a task has shut down after its {@link #cleanup()}.
    * 
    * @return whether or not the task has finished its cleanup and shut down.
    */
   public boolean hasShutdown()
   {
      return hasShutdown;
   }

   /**
    * Whether this task has thrown an exception and is waiting for resolution.
    * 
    * @return {@code true} if an exception was thrown and needs to be resolved for resuming.
    */
   boolean hasThrownException()
   {
      return thrownException.get() != null;
   }

   /**
    * Gets the exception thrown by this task, or {@code null} if no exception was thrown.
    * 
    * @return the last exception thrown.
    */
   Exception getThrownException()
   {
      return thrownException.get();
   }

   /**
    * Clears the last exception thrown and resume normal operation.
    * <p>
    * If no exception was thrown by this task, this method does nothing.
    * </p>
    */
   void clearExceptionAndResume()
   {
      thrownException.set(null);
   }

   @Override
   public final void run()
   {
      while (!shutdownRequested)
      {
         // Block until the scheduler releases this task for its next execution.
         barrier.await();

         if (shutdownRequested)
         {
            continue;
         }

         if (hasThrownException())
            continue; // We wait until the exception has been resolved.

         try
         {
            // Attempt to initialize the task. If the initialization fails then it will be attempted
            // again on the next iteration.
            if (!initialized)
               initialized = initialize();

            // Only execute if the initialization procedure has executed successfully.
            if (initialized)
               execute();
         }
         catch (Exception e)
         {
            // Catch the exception that is to be resolved by the scheduler.
            thrownException.set(e);
         }
      }

      cleanup();

      hasShutdown = true;
   }
}
