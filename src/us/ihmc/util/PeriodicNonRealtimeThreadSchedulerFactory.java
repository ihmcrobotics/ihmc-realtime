package us.ihmc.util;

import us.ihmc.util.PeriodicNonRealtimeThreadScheduler;
import us.ihmc.util.PeriodicThreadScheduler;

/**
 * Factory to create multiple instances of PeriodicNonRealtimeThreadScheduler
 * 
 * @author Jesper Smith
 *
 */
public class PeriodicNonRealtimeThreadSchedulerFactory implements PeriodicThreadSchedulerFactory
{

   @Override
   public PeriodicThreadScheduler createPeriodicThreadScheduler(String name)
   {
      return new PeriodicNonRealtimeThreadScheduler(name);
   }

}
