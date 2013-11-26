package us.ihmc.util;

import us.ihmc.affinity.Processor;
import us.ihmc.realtime.PeriodicParameters;
import us.ihmc.realtime.PriorityParameters;
import us.ihmc.realtime.RealtimeThread;

public class RealtimeThreadFactory implements ThreadFactory
{
   
   private final PriorityParameters priorityParameters;
   private final PeriodicParameters periodicParameters;
   
   private final Processor[] processors;
      
   public RealtimeThreadFactory(PriorityParameters priorityParameters, PeriodicParameters periodicParameters, Processor... processors)
   {
      this.priorityParameters = priorityParameters;
      this.periodicParameters = periodicParameters;
      this.processors = processors;
   }

   public ThreadInterface createThread(Runnable runnable, String name)
   {
      // native thread library doesn't use names
      RealtimeThread realtimeThread = new RealtimeThread(priorityParameters, periodicParameters, runnable);
      
      if(processors != null && processors.length > 0)
      {
         realtimeThread.setAffinity(processors);
      }
      return realtimeThread;
   }
   
   
}
