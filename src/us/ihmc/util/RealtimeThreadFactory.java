package us.ihmc.util;

import us.ihmc.realtime.PeriodicParameters;
import us.ihmc.realtime.PriorityParameters;
import us.ihmc.realtime.RealtimeThread;

public class RealtimeThreadFactory implements ThreadFactory
{
   
   private final PriorityParameters priorityParameters;
   private final PeriodicParameters periodicParameters;
   
   public RealtimeThreadFactory(PriorityParameters priorityParameters, PeriodicParameters periodicParameters)
   {
      this.priorityParameters = priorityParameters;
      this.periodicParameters = periodicParameters;
   }

   public ThreadInterface createThread(Runnable runnable)
   {
      return new RealtimeThread(priorityParameters, periodicParameters, runnable);
   }
   
   
}
