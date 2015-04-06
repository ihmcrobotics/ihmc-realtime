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

public class MonotonicTime
{
   public static final long NANOSECONDS_PER_SECOND = 1000000000;
   
   private long seconds;
   private long nanoseconds;
   
   public static MonotonicTime getCurrentTime()
   {
      MonotonicTime monotonicTime = new MonotonicTime();
      monotonicTime.setToCurrentTime();
      return monotonicTime;
   }
   
   public MonotonicTime(long seconds, long nanoseconds)
   {
      this.seconds = seconds;
      this.nanoseconds = nanoseconds;
      
      normalize();
   }
   
   public MonotonicTime()
   {
      this.seconds = 0;
      this.nanoseconds = 0;
   }
   
   
   private void normalize()
   {      
      seconds += nanoseconds / NANOSECONDS_PER_SECOND;
      nanoseconds = nanoseconds % NANOSECONDS_PER_SECOND;
   }
   
   public void add(MonotonicTime time)
   {
      seconds += time.seconds;
      nanoseconds += time.nanoseconds;
      
      normalize();
   }
   
   public void sub(MonotonicTime time)
   {
      seconds -= time.seconds;
      nanoseconds -= time.nanoseconds;
      
      normalize();
   }
   
   public void set(long seconds, long nanoseconds)
   {
      this.seconds = seconds;
      this.nanoseconds = nanoseconds;
      
      normalize();
   }
   
   public void set(MonotonicTime monotonicTime)
   {
      this.seconds = monotonicTime.seconds;
      this.nanoseconds = monotonicTime.nanoseconds;
   }
   
   public void setToCurrentTime()
   {
      this.seconds = 0;
      this.nanoseconds = RealtimeNative.getCurrentTimeNative();
      
      normalize();
   }

   long seconds()
   {
      return seconds;
   }
   
   long nanoseconds()
   {
      return nanoseconds;
   }
   
   @Override
   public String toString()
   {
      return seconds + "s " + nanoseconds + "ns";
   }
}
