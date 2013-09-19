package us.ihmc.concurrent.dataStructures;

import us.ihmc.util.RealtimeTools;

public class PaddedDoubleArray
{
   private final double[] data;
   
   private final long doubleOffset;
   private final long byteOffset;
   
   private final long expectedByteArrayLength;
   
   public PaddedDoubleArray(int capacity)
   {
      data = new double[RealtimeTools.nextDivisibleByEight(capacity)];
      
      doubleOffset = RealtimeTools.getUnsafe().arrayBaseOffset(double[].class);
      byteOffset = RealtimeTools.getUnsafe().arrayBaseOffset(byte[].class);
      
      
      expectedByteArrayLength = data.length * 8;
      
   }
   
   public void set(int index, double value)
   {
      data[index] = value;
   }
   
   public void set(byte[] array)
   {
      if(array.length > expectedByteArrayLength)
      {
         throw new IndexOutOfBoundsException();
      }
      
      RealtimeTools.getUnsafe().copyMemory(array, byteOffset, data, doubleOffset, array.length);
   }
   
   public void set(double[] array)
   {
      System.arraycopy(array, 0, data, 0, array.length);
   }
   
   public double get(int index)
   {
      return data[index];
   }
      
   public void get(byte[] array)
   {
      if(array.length != expectedByteArrayLength)
      {
         throw new IndexOutOfBoundsException();
      }
      
      RealtimeTools.getUnsafe().copyMemory(data, doubleOffset, array, byteOffset, expectedByteArrayLength);
   }
   
   public static int getExpectedByteArrayLength(int capacity)
   {
      return RealtimeTools.nextDivisibleByEight(capacity) * 8;
   }
   
   
   public static class Builder implements us.ihmc.concurrent.Builder<PaddedDoubleArray>
   {
      private final double[] initialValue;
      
      public Builder(double[] initialValue)
      {
         this.initialValue = initialValue;
      }

      public PaddedDoubleArray newInstance()
      {
         PaddedDoubleArray paddedDoubleArray = new PaddedDoubleArray(initialValue.length);
         paddedDoubleArray.set(initialValue);
         
         return paddedDoubleArray;
      }
      
   }

   
}
