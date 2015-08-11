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
package us.ihmc.realtime.concurrent.dataStructures;

import us.ihmc.realtime.util.RealtimeTools;

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
   
   public static class Builder implements us.ihmc.realtime.concurrent.Builder<ChangeListenerLongBuffer>
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
