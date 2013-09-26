package us.ihmc.util;

public interface ThreadFactory
{
   public ThreadInterface createThread(Runnable runnable);
}
