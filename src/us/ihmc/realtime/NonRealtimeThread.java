package us.ihmc.realtime;

import us.ihmc.util.ThreadInterface;

public class NonRealtimeThread extends Thread implements ThreadInterface
{
   public NonRealtimeThread(Runnable runnable)
   {
      super(runnable);
   }
}
