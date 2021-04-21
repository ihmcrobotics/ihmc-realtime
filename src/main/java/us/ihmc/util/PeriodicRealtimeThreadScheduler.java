package us.ihmc.util;

import java.util.concurrent.TimeUnit;

import us.ihmc.affinity.Processor;
import us.ihmc.realtime.MonotonicTime;
import us.ihmc.realtime.PeriodicParameters;
import us.ihmc.realtime.PeriodicRealtimeThread;
import us.ihmc.realtime.PriorityParameters;

/**
 * Realtime implementation of the periodic thread scheduler.
 * 
 * @author Jesper Smith
 */
public class PeriodicRealtimeThreadScheduler implements PeriodicThreadScheduler
{
   private final String name;
   private final PriorityParameters priorityParameters;
   private final Processor[] processors;
   private PeriodicRealtimeThread thread;

   public PeriodicRealtimeThreadScheduler(String name, int priority)
   {
      this(name, new PriorityParameters(priority));
   }

   public PeriodicRealtimeThreadScheduler(int priority)
   {
      this(null, new PriorityParameters(priority));
   }

   public PeriodicRealtimeThreadScheduler(PriorityParameters priorityParameters)
   {
      this(null, priorityParameters);
   }

   public PeriodicRealtimeThreadScheduler(String name, PriorityParameters priorityParameters)
   {
      this.name = name;
      this.priorityParameters = priorityParameters;
      this.processors = null;
   }

   public PeriodicRealtimeThreadScheduler(String name, PriorityParameters priorityParameters, Processor... processors)
   {
      this.name = name;
      this.priorityParameters = priorityParameters;
      this.processors = processors;
   }

   @Override
   public void schedule(Runnable runnable, long period, TimeUnit timeunit)
   {
      if (thread != null)
      {
         throw new RuntimeException("Thread has already been scheduled");
      }
      MonotonicTime time = new MonotonicTime(0, TimeUnit.NANOSECONDS.convert(period, timeunit));
      PeriodicParameters periodicParameters = new PeriodicParameters(time);
      thread = new PeriodicRealtimeThread(priorityParameters, periodicParameters, runnable, name);
      if (processors != null)
      {
         thread.setAffinity(processors);
      }
      thread.start();
   }

   @Override
   public void shutdown()
   {
      thread.shutdown();
   }

   @Override
   public void awaitTermination(long timeout, TimeUnit timeUnit) throws InterruptedException
   {
      thread.join();
   }

}
