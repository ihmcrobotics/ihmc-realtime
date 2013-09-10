package us.ihmc.util;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

public class RealtimeTools
{
   
   private static final Unsafe unsafe;
   static
   {      
      try {
         Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
         field.setAccessible(true);
         unsafe = (sun.misc.Unsafe) field.get(null);
      } catch (Exception e) {
         throw new AssertionError(e);
      }

   }
   
   public static final Unsafe getUnsafe()
   {
      return unsafe;
   }
   
   public static final int nextPowerOfTwo(int v)
   {
      // Algorithm from http://graphics.stanford.edu/~seander/bithacks.html#RoundUpPowerOf2
      v--;
      v |= v >> 1;
      v |= v >> 2;
      v |= v >> 4;
      v |= v >> 8;
      v |= v >> 16;
      return ++v;
   }
   
   public static final int nextDivisibleByEight(int v)
   {
      return (v/8 + 1) * 8;
   }

   
}
