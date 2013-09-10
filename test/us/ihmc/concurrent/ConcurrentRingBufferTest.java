package us.ihmc.concurrent;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

public class ConcurrentRingBufferTest
{
   @Test
   public void test()
   {
      final long iterations = 100000000L;
      final long writesPerIteration = 1L;
      final long seed = 89126450L;
      
      final ConcurrentRingBuffer<MutableLong> concurrentRingBuffer = new ConcurrentRingBuffer<>(new MutableLongBuilder(), 1024);
            
      // Producer
      new Thread(new Runnable()
      {
         @Override
         public void run()
         {
            Random random = new Random(seed);
            for(long value = 0; value < iterations; value++)
            {
               for(int y = 0; y < writesPerIteration; y++)
               {
                  MutableLong nextValue;
                  while((nextValue = concurrentRingBuffer.next()) == null);  // Spinlock
                  nextValue.value = random.nextLong();
               }
               concurrentRingBuffer.commit();
            }
            
         }
      }).start();
      
      
     
      boolean running = true; 
      long iteration = 0;
      Random random = new Random(seed);
      while(running)
      {
         if(concurrentRingBuffer.poll())
         {
            MutableLong value;
            while((value = concurrentRingBuffer.read()) != null)
            {
               assertTrue(random.nextLong() == value.value);
               
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
   }

   private class MutableLong
   {
      public long value;

   }

   public class MutableLongBuilder implements Builder<MutableLong>
   {
      @Override
      public MutableLong newInstance()
      {
         return new MutableLong();
      }
   }
}
