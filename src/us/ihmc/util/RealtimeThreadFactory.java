/*
 *   Copyright 2014 Florida Institute for Human and Machine Cognition (IHMC)
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *    
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *    
 *    Written by Jesper Smith with assistance from IHMC team members
 */
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
      RealtimeThread realtimeThread = new RealtimeThread(priorityParameters, periodicParameters, runnable, name);
      
      if(processors != null && processors.length > 0)
      {
         realtimeThread.setAffinity(processors);
      }
      return realtimeThread;
   }
   
   
}
