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
package us.ihmc.realtime.concurrent;

import us.ihmc.realtime.util.PaddedAtomicLong;
import us.ihmc.realtime.util.RealtimeTools;

/**
 * Concurrent lock-free and garbage-free implementation of a cyclic buffer
 * 
 * Based on http://lmax-exchange.github.io/disruptor/
 * 
 * For more information see
 *    http://mechanical-sympathy.blogspot.com/2011/07/memory-barriersfences.html
 *    
 * 
 * @author Jesper Smith
 *
 * @param <T> Class in buffer
 */
public class ConcurrentRingBuffer<T>
{

   /*
    * Producer variables
    */
   private long writePosition = -1;
   private long cachedMaxWritePosition = -1;
   private final PaddedAtomicLong commitPosition = new PaddedAtomicLong(-1);

   /*
    * 
    * Some padding to hopefully avoid false sharing, see
    * 
    * http://mechanical-sympathy.blogspot.com/2011/07/false-sharing.html
    * http://mechanical-sympathy.blogspot.com/2011/08/false-sharing-java-7.html
    */

   public volatile long p1, p2, p3, p4, p5, p6, p7 = 8L;

   /*
    * Consumer variables
    */
   private long readLimit = -1;
   private long readPosition = -1;
   private final PaddedAtomicLong consumerPosition = new PaddedAtomicLong(0);

   // More padding
   public volatile long p8, p9, p10, p11, p12, p13, p14 = 15L;

   private final int capacity;
   private final int capacityMask;
   private T[] buffer;

   /**
    * 
    * @param classBuilder Helper class to create empty version of T
    * @param capacity Capacity of the ring buffer, automatically rounded up to the next power of two
    */
   @SuppressWarnings("unchecked")
   public ConcurrentRingBuffer(Builder<? extends T> classBuilder, int capacity)
   {
      if (capacity < 0)
      {
         throw new RuntimeException("Capacity < 0");
      }

      this.capacity = RealtimeTools.nextPowerOfTwo(capacity);
      this.capacityMask = this.capacity - 1;

      buffer = (T[]) new Object[this.capacity];

      for (int i = 0; i < this.capacity; i++)
      {
         buffer[i] = classBuilder.newInstance();
      }
   }

   private T getObject(long position)
   {
      return buffer[(int) (position & capacityMask)];
   }

   /**
    * Get a new object for writing, return null if there is no space in the buffer
    *  
    * @return Object for writing
    */
   public T next()
   {
      ++writePosition;
      if(writePosition >= cachedMaxWritePosition)
      {
         cachedMaxWritePosition = consumerPosition.get() + capacity;
         if(writePosition >= cachedMaxWritePosition)
         {
            --writePosition;
            return null;
         }
      }
      return getObject(writePosition);
   }

   /**
    * Make all objects set using @see{next} since last commit visible to the consumer 
    * 
    */
   public void commit()
   {
      // Set the commitPosition to writePosition. Writing to commitPosition inserts a memory write barrier
      
      commitPosition.set(writePosition);
   }

   /**
    * Get latest data from producer. Run before a sequence of @see{read}
    * 
    * @return true if there is new data available
    */
   public boolean poll()
   {
      // Reading from commitPosition inserts a memory read barrier, making sure that this thread's caches are coherent
      readLimit = commitPosition.get();
      
      if(readPosition < readLimit)
      {
         return true;
      }
      else
      {
         return false;
      }
   }

   /**
    * Reads next data object. Run @see{poll} to update the read limit.
    * 
    * Usage:
    *    if(ConcurrentCyclicBuffer.poll()) // Cache new data
    *    {
    *       T data;
    *       while((data = ConcurrentCyclicBuffer.read()) != null)
    *       {  
    *        // Process data
    *       }
    *       ConcurrentCyclicBuffer.flush()
    *    }
    * 
    * 
    * @return new data, null if no new data
    */
   public T read()
   {
      if(readPosition >= readLimit)
      {
         return null;
      }
      ++readPosition;
      
      return getObject(readPosition);
   }
   
   /**
    * Get next data object. Run @see{poll} to update the read limit. 
    * Does not advance the read position.
    * 
    * @return new data, null if no new data
    */
   public T peek()
   {
      if(readPosition >= readLimit)
      {
         return null;
      }
      
      return getObject(readPosition + 1);
   }
   
   /**
    * Flushes read objects, making them available for writing. Call after @see{read}
    */
   public void flush()
   {
      consumerPosition.set(readPosition);
   }
   
   
   /**
    * @return Capacity of the cyclic buffer
    */
   
   public long getCapacity()
   {
      return this.capacity;
   }

   /**
    * Public function to avoid removal of padding
    * 
    * @return sum of p
    * 
    */
   public long avoidPaddingRemoval()
   {
      return p1 + p2 + p3 + p4 + p5 + p6 + p7 + p8 + p9 + p10 + p11 + p12 + p13 + p14;
   }
}
