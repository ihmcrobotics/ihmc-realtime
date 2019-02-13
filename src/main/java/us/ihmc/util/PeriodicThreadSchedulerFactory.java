package us.ihmc.util;

import us.ihmc.util.PeriodicThreadScheduler;

/**
 * Interface to create periodic thread schedulers
 * 
 * @author Jesper Smith
 *
 */
public interface PeriodicThreadSchedulerFactory
{
   /**
    * 
    * @param name Name for the scheduler thread (optionally used)
    * @return
    */
   PeriodicThreadScheduler createPeriodicThreadScheduler(String name);
}
