package us.ihmc.realtime;

public class PeriodicRealtimeThread extends RealtimeThread
{

   public PeriodicRealtimeThread(PriorityParameters priorityParameters, PeriodicParameters periodicParameters, Runnable runnable)
   {
      super(priorityParameters, periodicParameters, runnable);
   }
   
   
   @Override
   public final void run()
   {
      super.waitForNextPeriod();
      
      if(runnable != null)
      {
         runnable.run();
      }
   }

}
