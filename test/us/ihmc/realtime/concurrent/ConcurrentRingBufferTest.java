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
 *    Written by Alex Lesman with assistance from IHMC team members
 */
package us.ihmc.realtime.concurrent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

import us.ihmc.realtime.concurrent.Builder;
import us.ihmc.realtime.concurrent.ConcurrentRingBuffer;

public class ConcurrentRingBufferTest
{
   
   private int m_z = 9821271;
   private int m_w = 18917240;
   int get_random()
   {
       m_z = 36969 * (m_z & 65535) + (m_z >> 16);
       m_w = 18000 * (m_w & 65535) + (m_w >> 16);
       return (m_z << 16) + m_w;  /* 32-bit result */
   }
   
   @Test(timeout=300000)
   public void test() throws IOException
   {
      final long iterations = 100000000L;
      final long writesPerIteration = 1L;
      final long seed = 89126450L;
      
      final ConcurrentRingBuffer<MutableLong> concurrentRingBuffer = new ConcurrentRingBuffer<MutableLong>(new MutableLongBuilder(), 1024);
            
      // Producer
      new Thread(new Runnable()
      {
         public void run()
         {
            Random random = new Random(seed);
            for(long value = 0; value < iterations; value++)
            {
               for(int y = 0; y < writesPerIteration; y++)
               {
                  MutableLong nextValue;
                  while((nextValue = concurrentRingBuffer.next()) == null);  // Spinlock
                  nextValue.value = get_random();
               }
               concurrentRingBuffer.commit();
            }
            
         }
      }).start();
      
      
     
      boolean running = true; 
      long iteration = 0;
      Random random = new Random(seed);
      
      
      int n = 5000;
      long[][] dat = new long[2][(int) ((iterations * writesPerIteration)/n)];
      
      long start = System.nanoTime();
      while(running)
      {
         if(concurrentRingBuffer.poll())
         {
            MutableLong value;
            while((value = concurrentRingBuffer.read()) != null)
            {
//               assertTrue(random.nextLong() == value.value);
               if(iteration % n == 0)
               {
                  int i = (int) (iteration / n);
                  dat[0][i] = System.nanoTime() - start;
                  dat[1][i] = value.value;
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

      String data = "x=" + Arrays.toString(dat[0]) + ";\ny=" + Arrays.toString(dat[1]) + ";\nxi=x/1e9;\nyi=y./xi; plot(xi,yi)";
      Files.write(Paths.get("data.m"), data.getBytes());
   }

   private class MutableLong
   {
      public long value;

   }

   public class MutableLongBuilder implements Builder<MutableLong>
   {
      
      public MutableLong newInstance()
      {
         return new MutableLong();
      }
   }
}
