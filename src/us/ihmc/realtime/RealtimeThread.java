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

import us.ihmc.affinity.Affinity;
import us.ihmc.affinity.Processor;
import us.ihmc.util.ThreadInterface;

public class RealtimeThread implements Runnable, ThreadInterface
{
   enum SchedulerAlgorithm
   {
      SCHED_OTHER,
      SCHED_FIFO,
      SCHED_RR,
      SCHED_BATCH,
      SCHED_IDLE
   }
   
   enum ThreadStatus 
   {
      NEW,
      STARTED
   }
   
   private static ThreadLocal<RealtimeThread> realtimeThreads = new ThreadLocal<RealtimeThread>();
   
   private volatile ThreadStatus threadStatus = ThreadStatus.NEW;
   private final long threadID;
   
   protected final Runnable runnable;
   
   private Processor[] affinity = null;
   
   public RealtimeThread(PriorityParameters priorityParameters)
   {
      this(priorityParameters, null, null);
   }
   
   public RealtimeThread(PriorityParameters priorityParameters, Runnable runnable)
   {
      this(priorityParameters, null, runnable);
   }
   
   public RealtimeThread(PriorityParameters priorityParameters, PeriodicParameters periodicParameters, Runnable runnable)
   {
      boolean periodic = false;
      boolean startOnClock = false;
      long startSeconds = 0;
      long startNanos = 0;
      long periodSeconds = 0;
      long periodNanos = 0;

      if (periodicParameters != null)
      {
         periodic = true;
         if(periodicParameters.getStartTime() != null)
         {
            startOnClock = true;
            startSeconds = periodicParameters.getStartTime().seconds();
            startNanos = periodicParameters.getStartTime().nanoseconds();
         }
         periodSeconds = periodicParameters.getPeriod().seconds();
         periodNanos = periodicParameters.getPeriod().nanoseconds();
      }

      threadID = RealtimeNative.createThread(this, priorityParameters.getPriority(), periodic, startOnClock, startSeconds, startNanos, periodSeconds, periodNanos);
      this.runnable = runnable;
      
   }
   
   public final synchronized void setAffinity(Processor... processors)
   {
      this.affinity = processors;

      if(threadStatus == ThreadStatus.STARTED)
      {
         Affinity.setAffinity(this, processors);
      }
   }

   @Override
   public final synchronized void start()
   {
      if(threadStatus != ThreadStatus.NEW)
      {
         throw new IllegalThreadStateException("Thread already started");
      }
      
      if(RealtimeNative.startThread(threadID) != 0)
      {
         throw new RuntimeException("Cannot start realtime thread, do you have permission");
      }
      
      if(affinity != null)
      {
         Affinity.setAffinity(this, affinity);
      }
      
      threadStatus = ThreadStatus.STARTED;
   }

   @Override
   public void run()
   {
      if(runnable != null)
      {
         runnable.run();
      }
   }
   
   public ThreadStatus getStatus()
   {
      return threadStatus;
   }
   
   /**
    * @return time waiting
    */
   public long waitForNextPeriod()
   {
      return RealtimeNative.waitForNextPeriod(threadID);
   }
   
   public void setNextPeriodToClock()
   {
      RealtimeNative.setNextPeriodToClock(threadID);
   }
   
   public long waitUntil(MonotonicTime time) 
   {
      return RealtimeNative.waitUntil(threadID, time.seconds(), time.nanoseconds());
   }
   
   public static long getCurrentMonotonicClockTime()
   {
      return RealtimeNative.getCurrentTimeNative();
   }
   
   public static SchedulerAlgorithm getCurrentThreadScheduler()
   {
      int sched = RealtimeNative.getCurrentThreadScheduler();
      
      switch(sched)
      {
      case 0:
         return SchedulerAlgorithm.SCHED_OTHER;
      case 1:
         return SchedulerAlgorithm.SCHED_FIFO;
      case 2:
         return SchedulerAlgorithm.SCHED_RR;
      case 3:
         return SchedulerAlgorithm.SCHED_BATCH;
      case 5:
         return SchedulerAlgorithm.SCHED_IDLE;
      default:
         throw new RuntimeException("Unknown scheduler: " + sched);
      }
   }
   
   public static int getCurrentThreadPriority()
   {
      return RealtimeNative.getCurrentThreadPriority();
   }
   
   public static RealtimeThread getCurrentRealtimeThread()
   {
      RealtimeThread currentThread = realtimeThreads.get();
      if(currentThread == null)
      {
         throw new RuntimeException("Current thread is not a realtime thread");
      }
      
      return currentThread;
   }

   @Override
   public void getNextTriggerTime(MonotonicTime timeToPack)
   {
      long timestamp = RealtimeNative.getNextPeriod(threadID);
      timeToPack.set(0, timestamp);
   }

   public void setNextPeriod(MonotonicTime nextControllerTrigger)
   {
      RealtimeNative.setNextPeriod(threadID, nextControllerTrigger.seconds(), nextControllerTrigger.nanoseconds());
   }

   public int join()
   {
      return RealtimeNative.join(threadID);
   }

   public long getThreadID()
   {
      return threadID;
   }
}
