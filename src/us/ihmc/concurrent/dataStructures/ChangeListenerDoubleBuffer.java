package us.ihmc.concurrent.dataStructures;

import us.ihmc.util.RealtimeTools;

public class ChangeListenerDoubleBuffer
{
   private final boolean[] valueChanged;
   private final double[] values;
      
   public ChangeListenerDoubleBuffer(int capacity)
   {
      capacity = RealtimeTools.nextDivisibleBySixteen(capacity);
      
      valueChanged = new boolean[capacity];
      values = new double[capacity];
   }
   
   public void update(int index, double value)
   {
      valueChanged[index] = true;
      values[index] = value;
   }
   
   /**
    * Checks if value changed and resets the latch
    * 
    * @param int Index
    * @return true if changed
    */
   public boolean hasChangedAndReset(int index)
   {
      if(valueChanged[index])
      {
         valueChanged[index] = false;
         return true;
      }
      else
      {
         return false;
      }
   }
   
   public double getValue(int index)
   {
      return values[index];
   }
   
   public static class Builder implements us.ihmc.concurrent.Builder<ChangeListenerDoubleBuffer>
   {
      private final int capacity;
      
      public Builder(int capacity)
      {
         this.capacity = capacity;
      }

      @Override
      public ChangeListenerDoubleBuffer newInstance()
      {
         return new ChangeListenerDoubleBuffer(capacity);
      }
      
   }
}
