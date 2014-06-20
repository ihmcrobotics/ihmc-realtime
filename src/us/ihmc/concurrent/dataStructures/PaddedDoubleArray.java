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
