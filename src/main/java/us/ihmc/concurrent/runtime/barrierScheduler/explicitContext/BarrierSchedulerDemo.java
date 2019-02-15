package us.ihmc.concurrent.runtime.barrierScheduler.explicitContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A demo that schedules two threads -- a publisher and subscriber. The publisher increments and
 * prints a value which the subscriber reads and also prints.
 */
public class BarrierSchedulerDemo
{
   /**
    * The context object contains the data to be shared between the threads.
    * <p>
    * Note that no thread directly writes or reads to the context object during its execution.
    * Instead, transactions with the context object are scheduled when the task thread is sleeping.
    */
   public static class Context
   {
      public long counter = 0;
   }

   public static class Publisher extends Task<Context, Context>
   {
      // Create a local copy of context data.
      private long counter = 0;

      public Publisher(long divisor)
      {
         super(divisor);
      }

      @Override
      protected boolean initialize()
      {
         // No initialization needed. Return true to indicate that the task has successfully been
         // initialized and is ready to begin executing.
         return true;
      }

      @Override
      protected void execute()
      {
         // Use only the locally copied context data in the execute() loop.
         System.out.println("[publisher] counter: " + counter);
         counter++;
      }

      @Override
      protected void updateMasterContext(Context context)
      {
         // Copy produced data up to the master context.
         context.counter = counter;
      }

      @Override
      protected void updateLocalContext(Context context)
      {
         // This thread does not consume any data back from the master context.
      }
   }

   public static class Subscriber extends Task<Context, Context>
   {
      // Create a local copy of context data.
      private long counter = 0;

      public Subscriber(long divisor)
      {
         super(divisor);
      }

      @Override
      protected boolean initialize()
      {
         // As above, no initialization needed.
         return true;
      }

      @Override
      protected void execute()
      {
         // Use only the locally copied context data in the execute() loop.
         System.out.println("[subscriber] counter: " + counter);
      }

      @Override
      protected void updateMasterContext(Context context)
      {
         // This thread does not publish any data back to the master context.
      }

      @Override
      protected void updateLocalContext(Context context)
      {
         // Copy data down from the master context for use in the execute() loop.
         counter = context.counter;
      }
   }

   public static void main(String[] args) throws InterruptedException
   {
      final int SCHEDULER_FREQ = 100; // 100Hz
      final int PUBLISHER_DIVISOR = 1; // (100Hz / 1) = 100Hz
      final int SUBSCRIBER_DIVISOR = 10; // (100Hz / 10) = 10Hz

      Publisher publisher = new Publisher(PUBLISHER_DIVISOR);
      Subscriber subscriber = new Subscriber(SUBSCRIBER_DIVISOR);

      // Create and start the threads for each task. Neither thread will initialize() or execute()
      // until the barrierScheduler schedules their first cycle.
      Thread publisherThread = new Thread(publisher, "publisher");
      Thread subscriberThread = new Thread(subscriber, "subscriber");

      publisherThread.start();
      subscriberThread.start();

      // Create the barrierScheduler.
      Collection<Task<Context, Context>> tasks = Arrays.asList(publisher, subscriber);
      Context context = new Context();
      BarrierScheduler<Context> barrierScheduler = new BarrierScheduler<>(tasks, context,
                                                                          BarrierScheduler.TaskOverrunBehavior.SKIP_TICK);

      // Schedule the barrierScheduler to run at some fixed rate. The publisher and subscriber threads
      // will run at their respective divisors of this scheduled rate.
      ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
      executor.scheduleAtFixedRate(barrierScheduler, 0, 1000 / SCHEDULER_FREQ, TimeUnit.MILLISECONDS);
      executor.awaitTermination(0, TimeUnit.SECONDS);
   }
}
