package us.ihmc.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class to copy data from one producer thread to one consumer thread guaranteeing atomicity.
 * This class is lock-free, non-blocking and garbage-free
 *   
 *   Only one producer and one consumer are supported
 *   
 * 
 * @author Jesper Smith
 * 
 * @param <T> object
 *
 */
public class ConcurrentCopier<T>
{
   private static final int NEXT_OBJECT_TO_READ_MASK = 0b1100;
   private static final int CURRENTLY_BEING_READ_MASK = 0b0011;
   private static final int INITIAL_STATE = 0b1100;
   
   
   public final T[] buffer;
   
   private int currentlyBeingWritten = -1;
   
   /*
    * State bitmask integer
    * 
    * NEXT_OBJECT_TO_READ_MASK : nextObjectToRead
    * CURRENTLY_BEING_READ_MASK : currentlyBeingRead
    */
   private final AtomicInteger state = new AtomicInteger(); 
   
   
   @SuppressWarnings("unchecked")
   public ConcurrentCopier(Builder<? extends T> classBuilder)
   {
      buffer = (T[]) new Object[3];

      for (int i = 0; i < 3; i++)
      {
         buffer[i] = classBuilder.newInstance();
      }
      
      state.set(INITIAL_STATE);
   }
   
   public T getCopyForReading()
   {      
      while(true)
      {
         int currentState = state.get();
         if (currentState == INITIAL_STATE) 
         {
            return null;
         }
         
         int nextObjectToRead = (currentState & NEXT_OBJECT_TO_READ_MASK) >> 2;
         int newState = (currentState & NEXT_OBJECT_TO_READ_MASK) | (nextObjectToRead);
         if(state.compareAndSet(currentState, newState))
         {
            return buffer[nextObjectToRead];
         }
      }
   }
   
   private int getNextWriteIndex(int currentState)
   {
      switch(currentState)
      {
      case 0b0000:
         return 0b01;
      case 0b0001:
         return 0b10;
      case 0b0010:
         return 0b01;
      case 0b0100:
         return 0b10;
      case 0b0101:
         return 0b00;
      case 0b0110:
         return 0b00;
      case 0b1000:
         return 0b01;
      case 0b1001:
         return 0b00;
      case 0b1010:
         return 0b00;
      case INITIAL_STATE:
         return 0b01;
      default:
         throw new RuntimeException("Invalid Copier State: " + currentState);
      }
      
   }
   
   public T getCopyForWriting()
   {
      currentlyBeingWritten = getNextWriteIndex(state.get());
      return buffer[currentlyBeingWritten];
   }
   
   
   /**
    * Commit write such that getCopyForWriting returns the newest copy
    */
   public void commit()
   {
      // Updating nextObjectToRead(state & NEXT_OBJECT_TO_READ_MASK) to currentlyBeingWritten 
      while(true)
      {
         int currentState = state.get();
         int newState = (currentState & CURRENTLY_BEING_READ_MASK) | (currentlyBeingWritten << 2);
         if(state.compareAndSet(currentState, newState))
         {
            break;
         }
      }
   }
}
