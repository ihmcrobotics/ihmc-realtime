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

public class TestCyclic
{
   public static void main(String[] args) throws InterruptedException
   {
      final int periodInNS = 1000000;
      final int iterations = 10000;

      PriorityParameters priorityParameters = new PriorityParameters(99);
      PeriodicParameters periodicParameters = new PeriodicParameters(new MonotonicTime(0, periodInNS));
      
      RealtimeThread periodicRealtimeThread = new RealtimeThread(priorityParameters, periodicParameters, null)
      {
         @Override 
         public void run()
         {
            long previousTime = 0;
            
            long averageJitter = 0;
            long maxJitter = 0;
            for(int i = -1; i < iterations; i++)
            {
               super.waitForNextPeriod();
               if(i < 0)
               {
                  previousTime = System.nanoTime();
                  continue;
               }
               long newTime = System.nanoTime();
               
               long jitter = Math.abs(newTime - previousTime - periodInNS);
               averageJitter += jitter;
               
               if(jitter > maxJitter) 
               {
                  maxJitter = jitter;
               }
               previousTime = newTime;
            }
            
            System.out.println("Average jitter: " + (((double) averageJitter)/((double)iterations))/1e6 + "ms");
            System.out.println("Max jitter: " + ((double)maxJitter)/(1e6) + "ms");
         }
      };

      RealtimeMemory.lock();
      periodicRealtimeThread.start();

      Thread.sleep(1000);
   }
}
