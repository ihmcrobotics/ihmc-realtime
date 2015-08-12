package us.ihmc.util;

import java.util.concurrent.TimeUnit;

public interface PeriodicThreadScheduler
{
   /**
    * Schedule a new periodic thread
    * 
    * @throws RuntimeException if a Runnable is already scheduled to execute
    * 
    * @param runnable 
    * @param period Period given in timeunit
    * @param timeunit 
    */
   public void schedule(Runnable runnable, long period, TimeUnit timeunit);
   
   /**
    * Shutdown the periodic thread. No new runnables can be scheduled. 
    * Running threads are allowed to finish execution the current execution.
    */
   public void shutdown();
}
