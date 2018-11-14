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
         System.out.println("Estimated duration: " + duration + " seconds");
      }

      PriorityParameters priorityParameters = new PriorityParameters(99);
      PeriodicParameters periodicParameters = new PeriodicParameters(new MonotonicTime(0, periodInNS));

      final double[] jitterValues = new double[iterations];

      RealtimeThread periodicRealtimeThread = new RealtimeThread(priorityParameters, periodicParameters)
      {
         private void perform(int run)
         {
            super.waitForNextPeriod();
            long previousTime = System.nanoTime();

            for (int i = 0; i < iterations; i++)
            {
               super.waitForNextPeriod();
               long newTime = System.nanoTime();
               jitterValues[i] = Math.abs(newTime - previousTime - periodInNS) * 1.0e-3;
               previousTime = newTime;
            }

            double avg = 0.0;
            double max = 0.0;
            double std = 0.0;

            for (double value : jitterValues)
            {
               avg += value / iterations;
               max = Math.max(max, value);
            }
            for (double value : jitterValues)
            {
               std += Math.pow(value - avg, 2);
            }
            std = Math.sqrt(std / iterations);

            System.out.format("[%d] Jitter: avg = %.2f us, max = %.2f us, std = %.2f us%n", run, avg, max, std);
         }

         @Override
         public void run()
         {
            int run = 1;
            do
            {
               perform(run++);
            }
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
