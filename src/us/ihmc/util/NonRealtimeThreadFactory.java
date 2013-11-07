package us.ihmc.util;

import us.ihmc.realtime.NonRealtimeThread;

public class NonRealtimeThreadFactory implements ThreadFactory
{
   public ThreadInterface createThread(Runnable runnable, String name)
   {
      return new NonRealtimeThread(runnable, name);
   }
}
