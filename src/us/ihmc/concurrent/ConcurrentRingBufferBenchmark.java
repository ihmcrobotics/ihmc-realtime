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
package us.ihmc.concurrent;


public class ConcurrentRingBufferBenchmark
{
   public static void main(String[] args)
   {
      final long iterations = Long.valueOf(args[0]);
      final long writesPerIteration = 1L;
      
      final ConcurrentRingBuffer<MutableLong> concurrentRingBuffer = new ConcurrentRingBuffer<MutableLong>(new MutableLongBuilder(), 1024);
            
      // Producer
      new Thread(new Runnable()
      {
         public void run()
         {
            for(long value = 0; value < iterations; value++)
            {
               for(int y = 0; y < writesPerIteration; y++)
               {
                  MutableLong nextValue;
                  while((nextValue = concurrentRingBuffer.next()) == null);  // Spinlock
                  nextValue.value = value;
               }
               concurrentRingBuffer.commit();
            }
            
         }
      }).start();
      
      
     
      boolean running = true; 
      long iteration = 0;
      long start = System.nanoTime();
      while(running)
      {
         if(concurrentRingBuffer.poll())
         {
            MutableLong value;
            while((value = concurrentRingBuffer.read()) != null)
            {
               if(value.value != iteration)
               {
                  throw new RuntimeException("Values not equal");
               }
               
               ++iteration;
               if(iteration >= (iterations-1) * writesPerIteration)
               {
                  running = false;
                  break;
               }
            }
            concurrentRingBuffer.flush();
         }
      }
      long executionTime = System.nanoTime() - start;
      double executionTimeS = executionTime / 1000000000.0;
      double average = iterations / executionTimeS;
      
      System.out.println(iterations + " iterations, total execution time: " + executionTime + "ns; " + executionTimeS + "s.");
      System.out.println("Average: " + average + " iterations/s");
   }
   
   private static class MutableLong
   {
      public long value;

   }

   private static class MutableLongBuilder implements Builder<MutableLong>
   {
      
      public MutableLong newInstance()
      {
         return new MutableLong();
      }
   }


}
