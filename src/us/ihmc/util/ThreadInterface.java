package us.ihmc.util;

import us.ihmc.realtime.MonotonicTime;

public interface ThreadInterface
{
   public void start();
   public void run();
   
   public void getNextTriggerTime(MonotonicTime nextTriggerTime);
}
