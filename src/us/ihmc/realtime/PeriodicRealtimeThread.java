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
package us.ihmc.realtime;

/**
 * Periodic realtime thread
 * 
 * @author Jesper Smith
 *
 */
public class PeriodicRealtimeThread extends RealtimeThread
{
   private volatile boolean running = true;
   
   /**
    * Construct a new PeriodicRealtimeThread with given priority and periodic parameters.
    * 
    * The runnable gets called given the period in periodicParameters
    * 
    * @param priorityParameters
    * @param periodicParameters
    * @param runnable
    */
   public PeriodicRealtimeThread(PriorityParameters priorityParameters, PeriodicParameters periodicParameters, Runnable runnable)
   {
      this(priorityParameters, periodicParameters, runnable, null);
   }
   
   
   /**
    * Construct a new PeriodicRealtimeThread with given priority and periodic parameters.
    * 
    * The runnable gets called given the period in periodicParameters
    * 
    * @param priorityParameters
    * @param periodicParameters
    * @param runnable
    * @param name Human readable name for this thread
    */
   public PeriodicRealtimeThread(PriorityParameters priorityParameters, PeriodicParameters periodicParameters, Runnable runnable, String name)
   {
      super(priorityParameters, periodicParameters, runnable, name);
   }
   
   
   @Override
   public final void run()
   {      
      while(running)
      {
         super.waitForNextPeriod();
         
         if(running && runnable != null)
         {
            runnable.run();
         }
      }
   }
   
   public void shutdown()
   {
      running = false;
   }

}
