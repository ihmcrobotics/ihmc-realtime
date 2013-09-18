package us.ihmc.realtime;

public class RealtimeNative
{
   static
   {
      System.loadLibrary("RealtimeNative");
      mlockall();
      registerVM();
   }

   private static native void mlockall();
   private static native void registerVM();
   
   static native long createThread(Runnable target, int priority, boolean periodic, boolean startOnClock, long startSeconds, long startNanos, long periodSeconds, long periodNanos);
   static native int startThread(long threadID);
   static native void waitForNextPeriod(long threadID);
   static native void setNextPeriodToClock(long threadID); 

   static native int getMaximumPriorityNative();
   static native int getMinimumPriorityNative();

   /**
    * Only valid when time < 292 years
    * 
    * @return monotonic time in nanoseconds
    */
   static native long getCurrentTimeNative();
   
   static native int getCurrentThreadPriority();
   static native int getCurrentThreadScheduler();


}
