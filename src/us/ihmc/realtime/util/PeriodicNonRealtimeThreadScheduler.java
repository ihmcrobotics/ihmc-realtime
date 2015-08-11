package us.ihmc.realtime.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PeriodicNonRealtimeThreadScheduler implements PeriodicThreadScheduler
{
   private final ScheduledExecutorService executorService;
   
   public PeriodicNonRealtimeThreadScheduler(String name)
   {
      this.executorService = Executors.newSingleThreadScheduledExecutor(getNamedThreadFactory(name));
   }

   @Override
   public void schedule(Runnable runnable, long period, TimeUnit timeunit)
   {
      executorService.scheduleAtFixedRate(runnable, 0, period, timeunit);
   }

   @Override
   public void shutdown()
   {
      executorService.shutdown();
   }
   
   /**
    * Duplicated from IHMCUtilties.ThreadTools.getNamedThreadFactory. Copied to keep IHMCRealtime stand-alone.
    * 
    * @param name prefix for thread names
    * @return Thread factory that generates threads prefixed with name
    */
   private ThreadFactory getNamedThreadFactory(final String name)
   {
      return new ThreadFactory()
      {
         private final AtomicInteger threadNumber = new AtomicInteger(1);

         public Thread newThread(Runnable r)
         {
            Thread t = new Thread(r, name + "-thread-" + threadNumber.getAndIncrement());

            if (t.isDaemon())
               t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
               t.setPriority(Thread.NORM_PRIORITY);

            return t;
         }
      };
   }

}
