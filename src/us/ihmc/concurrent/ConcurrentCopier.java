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
   private static final int NEXT_OBJECT_TO_READ_MASK = 0xC;
   private static final int CURRENTLY_BEING_READ_MASK = 0x3;
   private static final int INITIAL_STATE = 0xC;
   
   
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
      case 0x0:
         return 0x1;
      case 0x1:
         return 0x2;
      case 0x2:
         return 0x1;
      case 0x4:
         return 0x2;
      case 0x5:
         return 0x0;
      case 0x6:
         return 0x0;
      case 0x8:
         return 0x1;
      case 0x9:
         return 0x0;
      case 0xA:
         return 0x0;
      case INITIAL_STATE:
         return 0x1;
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
