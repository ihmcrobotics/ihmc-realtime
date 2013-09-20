package us.ihmc.concurrent;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

public class ConcurrentCopierTest
{
   @Test
   public void singleThreadTest()
   {
      final long iterations = 10L;
      final long seed = 890327L;
      
      Random random = new Random(seed);
      final ConcurrentCopier<MutableTestObject> copier = new ConcurrentCopier<>(new MutableTestObjectBuilder());
      
      for(long i = 0; i < iterations; i++)
      {
         for(long y = 1; y < 8; y++)
         {
            MutableTestObject object = copier.getCopyForWriting();
            object.update(i * y, random.nextLong());
            copier.commit();
         }
         
         for(long y = 0; y < 4; y++)
         {
            MutableTestObject readObject = copier.getCopyForReading();
            readObject.test();
         }
      }
   }
   
   @Test
   public void test()
   {
      final long iterations = 1000000L;
      final long seed = 89126450L;
      
      final ConcurrentCopier<MutableTestObject> copier = new ConcurrentCopier<>(new MutableTestObjectBuilder());
            
      // Producer
      new Thread(new Runnable()
      {
         public void run()
         {
            Random random = new Random(seed);
            for(long value = 0; value < iterations; value++)
            {
               MutableTestObject next = copier.getCopyForWriting();
               next.update(value, random.nextLong());
               copier.commit();
            }
            
         }
      }).start();
      
      
     
      // Consumer
      while(true)
      {
         MutableTestObject mutableTestObject;
         while((mutableTestObject = copier.getCopyForReading()) == null);
         mutableTestObject.test();
         
         if(mutableTestObject.iteration >= iterations - 1)
         {
            break;
         }
      }
   }

   private class MutableTestObject
   {
      public long iteration;
      public long seed;
      public double[] values = new double[100];
      
      public void update(long iteration, long seed)
      {
         this.iteration = iteration; 
         this.seed = seed;
         Random random = new Random(seed);
         for(int i = 0; i < values.length; i++)
         {
            values[i] = random.nextDouble();
         }
      }
      
      public void test()
      {
         Random random = new Random(seed);
         for(int i = 0; i < values.length; i++)
         {
            assertEquals(values[i], random.nextDouble(), 1e-12);
         }
      }

   }

   public class MutableTestObjectBuilder implements Builder<MutableTestObject>
   {
      
      public MutableTestObject newInstance()
      {
         return new MutableTestObject();
      }
   }
}
