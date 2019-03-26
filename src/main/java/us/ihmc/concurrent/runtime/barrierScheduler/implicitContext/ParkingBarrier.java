package us.ihmc.concurrent.runtime.barrierScheduler.implicitContext;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public class ParkingBarrier
{
   private final AtomicReference<Thread> waitingThread = new AtomicReference<>(null);

   /**
    * Blocks until the {@link #release()} method is called.
    * <p>
    * This method must not be called from another thread while the barrier is currently awaiting.
    * That is, only a single thread can await on this barrier at once.
    *
    * @throws IllegalStateException if another thread is already waiting on this barrier
    */
   public void await()
   {
      // Make sure another thread isn't already awaiting this barrier.
      if (!waitingThread.compareAndSet(null, Thread.currentThread()))
         throw new IllegalStateException("only one thread can await() on a barrier at a time");

      // Wait for a release() call to nullify the waiting thread reference.
      while (waitingThread.get() != null)
         LockSupport.park(this);
   }

   /**
    * Unblocks the corresponding {@link #await()} call if the thread is waiting. Otherwise, does
    * nothing.
    * <p>
    * It is safe to call this method from multiple threads. In all cases, the barrier will only
    * be released once for each time it is awaited.
    *
    * @return true if the barrier was successfully released, false if it was not
    */
   public boolean release()
   {
      Thread waitingThreadRef = waitingThread.getAndSet(null);

      // Don't try to release a barrier that isn't awaiting. Otherwise it's next await will
      // immediately return (and we can't do it, because we don't know which thread to unpark).
      if (waitingThreadRef == null)
         return false;

      // Unpark guarantees the current park will exit or the next park will return immediately,
      // so we don't have to worry about a race condition where the waiting thread has been set
      // but the waiting thread has not yet been parked.
      LockSupport.unpark(waitingThreadRef);
      return true;
   }

   /**
    * Returns whether or not the barrier is currently waiting.
    *
    * @return true if the barrier is waiting, false otherwise
    */
   public boolean isSleeping()
   {
      return waitingThread.get() != null;
   }
}
