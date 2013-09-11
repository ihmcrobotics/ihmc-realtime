package us.ihmc.concurrent.dataStructures;

import us.ihmc.util.RealtimeTools;

public class IndexedDoubleBuffer
{
   private final int[] indices;
   private final double[] values;
   
   private int position = -1;
   private int elements = 0;
   
   public IndexedDoubleBuffer(int capacity)
   {
      capacity = RealtimeTools.nextDivisibleBySixteen(capacity);
      
      indices = new int[capacity];
      values = new double[capacity];
   }
   
   public void put(int index, double value)
   {
      ++position;
      indices[position] = index;
      values[position] = value;
      ++elements;
   }
   
   public boolean next()
   {
      position++;
      return position < elements;
   }
   
   public int getIndex()
   {
      return indices[position];
   }
   
   public double getValue()
   {
      return values[position];
   }
   
   public void rewind()
   {
      position = -1;
   }

   public void clear()
   {
      position = -1;
      elements = 0;
   }
   
   public static class Builder implements us.ihmc.concurrent.Builder<IndexedDoubleBuffer>
   {
      private final int capacity;
      
      public Builder(int capacity)
      {
         this.capacity = capacity;
      }

      @Override
      public IndexedDoubleBuffer newInstance()
      {
         return new IndexedDoubleBuffer(capacity);
      }
      
   }
}
