package us.ihmc.concurrent.runtime.barrierScheduler.implicitContext.tasks;

import java.io.Serializable;

/**
 * Interface used by {@link UniBinding} for copying information between tasks.
 * @param <T> the type implementing the interface
 */
public interface InPlaceCopyable<T extends InPlaceCopyable<T>> extends Serializable
{
   /**
    * Called by the update methods in the {@link UniBinding} when receiving data from another task.
    *
    * @param src The data being received
    */
   void copyFrom(T src);
}
