package us.ihmc.util;

import java.util.concurrent.TimeUnit;

import us.ihmc.realtime.MonotonicTime;
import us.ihmc.realtime.PeriodicParameters;
import us.ihmc.realtime.PeriodicRealtimeThread;
import us.ihmc.realtime.PriorityParameters;

public class PeriodicRealtimeThreadScheduler implements PeriodicThreadScheduler
{
   private final PriorityParameters priorityParameters;
   private PeriodicRealtimeThread thread;

   public PeriodicRealtimeThreadScheduler(int priority)
   {
      this(new PriorityParameters(priority));
   }
   
   public PeriodicRealtimeThreadScheduler(PriorityParameters priorityParameters)
   {
      this.priorityParameters = priorityParameters;
   }

   @Override
   public void schedule(Runnable runnable, long period, TimeUnit timeunit)
   {
      if(thread != null)
      {
         throw new RuntimeException("Thread has already been scheduled");
      }
      MonotonicTime time = new MonotonicTime(0, TimeUnit.NANOSECONDS.convert(period, timeunit));
      PeriodicParameters periodicParameters = new PeriodicParameters(time);
      thread = new PeriodicRealtimeThread(priorityParameters, periodicParameters, runnable);
      thread.start();
   }

   @Override
   public void shutdown()
   {
      thread.shutdown();
   }

}
