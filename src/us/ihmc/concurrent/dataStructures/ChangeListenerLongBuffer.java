package us.ihmc.concurrent.dataStructures;

import us.ihmc.util.RealtimeTools;

public class ChangeListenerLongBuffer
{
   private final boolean[] valueChanged;
   private final long[] values;
      
   public ChangeListenerLongBuffer(int capacity)
   {
      capacity = RealtimeTools.nextDivisibleBySixteen(capacity);
      
      valueChanged = new boolean[capacity];
      values = new long[capacity];
   }
   
   public void update(int index, long value)
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
   
   public long getValue(int index)
   {
      return values[index];
   }
   
   public static class Builder implements us.ihmc.concurrent.Builder<ChangeListenerLongBuffer>
   {
      private final int capacity;
      
      public Builder(int capacity)
      {
         this.capacity = capacity;
      }

    
      public ChangeListenerLongBuffer newInstance()
      {
         return new ChangeListenerLongBuffer(capacity);
      }
      
   }
}
