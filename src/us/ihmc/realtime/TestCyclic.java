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

import us.ihmc.affinity.CPUTopology;
import us.ihmc.affinity.Processor;

public class TestCyclic
{
   public static void main(String[] args) throws InterruptedException
   {
      final int periodInNS = 1000000;
      final int iterations = 60000;
      final double duration = (double) periodInNS * (double) iterations / 1e9;

      final boolean endless = (args.length > 0) && (args[0].equals("--endless"));
      if (endless)
      {
         System.out.println("You can cancel this test by pressing Ctrl+C");
      }
      else
      {
         System.out.println("Iteration count: " + iterations);
         System.out.println("Estimated duration: " + duration + " seconds");
      }

      PriorityParameters priorityParameters = new PriorityParameters(99);
      PeriodicParameters periodicParameters = new PeriodicParameters(new MonotonicTime(0, periodInNS));

      RealtimeThread periodicRealtimeThread = new RealtimeThread(priorityParameters, periodicParameters)
      {
         private void perform(int run)
         {
            long previousTime = 0;
            long avgJitter = 0;
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

               if(jitter > maxJitter) { maxJitter = jitter; }
               avgJitter += jitter;

               previousTime = newTime;
            }

            final double usAvgJitter = (double) avgJitter / (double) iterations / 1e3;
            final double usMaxJitter = (double) maxJitter / 1e3;

            System.out.format("[%d] Jitter: avg = %.4f us, max = %.4f us%n", run, usAvgJitter, usMaxJitter);
         }

         @Override
         public void run()
         {
            int run = 1;

            do { perform(run++); }
            while (endless);
         }
      };

      System.out.println("Pinning the periodic thread to processor 1");
      Processor testProcessor = new CPUTopology().getPackage(0).getCore(1).getDefaultProcessor();
      periodicRealtimeThread.setAffinity(testProcessor);

      periodicRealtimeThread.start();
      periodicRealtimeThread.join();
   }
}
