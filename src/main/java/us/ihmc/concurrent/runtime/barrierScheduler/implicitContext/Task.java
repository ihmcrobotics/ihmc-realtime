package us.ihmc.concurrent.runtime.barrierScheduler.implicitContext;

public abstract class Task<C> implements Runnable
{
   /**
    * The positive integer divisor of the scheduler frequency at which this task should execute.
    */
   private final long divisor;

   /**
    * The barrier used to await each execution cycle in order to synchronize the task to its divisor
    * of the scheduler tick frequency.
    */
   // participants: scheduler, task
   private final ParkingBarrier barrier = new ParkingBarrier();

   /**
    * Whether or not the {@link #initialize()} method has been called.
    */
   private boolean initialized;

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
    * Called once, immediately before the first {@link #execute()} call. This method is executed on
    * the task's thread.
    */
   protected abstract boolean initialize();

   /**
    * Executes a single iteration of this task.
    * <p>
    * This method is executed on the task's thread.
    */
   protected abstract void execute();

   /**
    * Bubbles internal context data up the the master context.
    * <p>
    * Do not hold a reference to {@code context}. Instead, copy internal data to it as quickly as
    * possible then drop the reference. This method is called on the scheduler's thread while the
    * task is sleeping.
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
    * Returns whether or not this task should be nominally scheduled to execute on this tick. If it
    * is, the scheduler should do its best to execute the <b>on this tick</b>.
    *
    * @param schedulerTick the current scheduler tick
    * @return {@code true} if this task should execute on this tick.
    */
   boolean isPending(long schedulerTick)
   {
      return schedulerTick % divisor == 0;
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
    * Releases the task to continue its execution for another cycle.
    *
    * @see ParkingBarrier#release()
    */
   boolean release()
   {
      return barrier.release();
   }

   @Override
   public final void run()
   {
      while (true)
      {
         // Block until the scheduler releases this task for its next execution.
         barrier.await();

         // Attempt to initialize the task. If the initialization fails then it will be attempted
         // again on the next iteration.
         if (!initialized)
            initialized = initialize();

         // Only execute if the initialization procedure has executed successfully.
         if (initialized)
            execute();
      }
   }
}
