package us.ihmc.util;

import us.ihmc.realtime.PriorityParameters;
import us.ihmc.util.PeriodicRealtimeThreadScheduler;
import us.ihmc.util.PeriodicThreadScheduler;

/**
 * Factory to create multiple instances of PeriodicRealtimeThreadScheduler
 * 
 * @author Jesper Smith
 *
 */
public class PeriodicRealtimeThreadSchedulerFactory implements PeriodicThreadSchedulerFactory
{
   private final PriorityParameters priority;

   public PeriodicRealtimeThreadSchedulerFactory(PriorityParameters priority)
   {
      this.priority = priority;
   }

   public PeriodicRealtimeThreadSchedulerFactory(int priority)
   {
      this.priority = new PriorityParameters(priority);
   }

   @Override
   public PeriodicThreadScheduler createPeriodicThreadScheduler(String name)
   {
      return new PeriodicRealtimeThreadScheduler(name, priority);
   }

}
