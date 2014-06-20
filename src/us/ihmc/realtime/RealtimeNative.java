/*
 *   Copyright 2014 Florida Institute for Human and Machine Cognition (IHMC)
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *    
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *    
 *    Written by Jesper Smith with assistance from IHMC team members
 */
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
   static native int join(long threadID);
   
   static native long waitForNextPeriod(long threadID);
   static native long waitUntil(long threadID, long seconds, long nanoseconds);

   static native void setNextPeriodToClock(long threadID); 
   static native void setNextPeriod(long threadID, long seconds, long nanoseconds);
   
   /**
    * Only valid when time < 292 years
    * 
    * @return next trigger time in nanoseconds
    */
   static native long getNextPeriod(long threadID);

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
   {
      // TODO Auto-generated method stub
      
   }


}
