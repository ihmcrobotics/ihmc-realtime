package us.ihmc.util;

import us.ihmc.affinity.Processor;
import us.ihmc.realtime.PriorityParameters;

/**
 * Factory to create multiple instances of PeriodicRealtimeThreadScheduler
 * 
 * @author Jesper Smith
 */
public class PeriodicRealtimeThreadSchedulerFactory implements PeriodicThreadSchedulerFactory
{
   private final PriorityParameters priority;
   private final Processor[] processors;

   public PeriodicRealtimeThreadSchedulerFactory(PriorityParameters priority)
   {
      this.priority = priority;
      this.processors = null;
   }

   public PeriodicRealtimeThreadSchedulerFactory(int priority)
   {
      this(new PriorityParameters(priority));
   }

   public PeriodicRealtimeThreadSchedulerFactory(PriorityParameters priority, Processor... processors)
   {
      this.priority = priority;
      this.processors = processors;
   }

   @Override
   public PeriodicThreadScheduler createPeriodicThreadScheduler(String name)
   {
      PeriodicRealtimeThreadScheduler periodicRealtimeThreadScheduler = new PeriodicRealtimeThreadScheduler(name, priority, processors);
      return periodicRealtimeThreadScheduler;
   }

}
