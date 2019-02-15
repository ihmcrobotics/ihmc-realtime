package us.ihmc.concurrent.runtime.barrierScheduler.implicitContext.tasks;

import java.io.Serializable;

public interface InPlaceCopyable<T extends InPlaceCopyable<T>> extends Serializable
{
   void copyFrom(T src);
}
