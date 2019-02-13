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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import us.ihmc.affinity.Affinity;
import us.ihmc.affinity.Processor;
import us.ihmc.process.SchedulerAlgorithm;
import us.ihmc.util.ThreadInterface;

public class RealtimeThread implements Runnable, ThreadInterface
{
   
   enum ThreadStatus 
   {
      NEW,
      STARTED
   }
   
   private static final ThreadLocal<RealtimeThread> realtimeThreads = new ThreadLocal<RealtimeThread>();
   private static final AtomicInteger threadNumber = new AtomicInteger(1);
   
   private volatile ThreadStatus threadStatus = ThreadStatus.NEW;
   private final long threadID;
   private final String name;
   
   protected final Runnable runnable;
   
   private Processor[] affinity = null;
   
   private final ReentrantLock joinLock = new ReentrantLock();
   private boolean hasJoined = false;
   private int returnValue;
   
   public RealtimeThread(PriorityParameters priorityParameters)
   {
      this(priorityParameters, null, null, null);
   }
   
   public RealtimeThread(PriorityParameters priorityParameters, String name)
   {
      this(priorityParameters, null, null, name);
   }
   
   public RealtimeThread(PriorityParameters priorityParameters, Runnable runnable)
   {
      this(priorityParameters, null, runnable, null);
   }

   public RealtimeThread(PriorityParameters priorityParameters, Runnable runnable, String name)
   {
      this(priorityParameters, null, runnable, name);
   }
   
   public RealtimeThread(PriorityParameters priorityParameters, PeriodicParameters periodicParameters)
   {
      this(priorityParameters, periodicParameters, null, null);
   }

   public RealtimeThread(PriorityParameters priorityParameters, PeriodicParameters periodicParameters, String name)
   {
      this(priorityParameters, periodicParameters, null, name);
   }
   
   public RealtimeThread(PriorityParameters priorityParameters, PeriodicParameters periodicParameters, Runnable runnable)
   {
      this(priorityParameters, periodicParameters, runnable, null);
   }
   
   public RealtimeThread(PriorityParameters priorityParameters, PeriodicParameters periodicParameters, Runnable runnable, String name)
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
      if(name != null)
      {
         this.name = name + "-realtime-thread-" + threadNumber.getAndIncrement();
      }
      else
      {
         this.name = "realtime-thread-" + threadNumber.getAndIncrement();
      }
      
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

   /**
    * Instead of calling start() on the Java object, this method is
    * intended to be called from the JNI by a native POSIX thread.
    *
    * Don't forget to change the native library if modified.
    * IHMCRealtime/csrc/RealtimeNative.cpp:164
    */
   void runFromNative()
   {
      realtimeThreads.set(this);
      Thread.currentThread().setName(name);
      run();
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
    * @param offset Offset time to synchronize execution to external sources
    * @return time waiting
    */
   public long waitForNextPeriod(long offset)
   {
      return RealtimeNative.waitForNextPeriod(threadID, offset);
   }
   /**
    * @return time waiting
    */
   public long waitForNextPeriod()
   {
      return RealtimeNative.waitForNextPeriod(threadID, 0);
   }
   
   public void setNextPeriodToClock()
   {
      RealtimeNative.setNextPeriodToClock(threadID);
   }
   
   public long waitUntil(MonotonicTime time) 
   {
      return RealtimeNative.waitUntil(threadID, time.seconds(), time.nanoseconds());
   }
   
   /**
    * Get a monotonically increasing time value in nanoseconds
    * 
    * @return monotonically increasing time in nanoseconds since arbitrary epoch
    */
   public static long getCurrentMonotonicClockTime()
   {
      return RealtimeNative.getCurrentTimeNative();
   }
   
   public static SchedulerAlgorithm getCurrentThreadScheduler()
   {
      int sched = RealtimeNative.getCurrentThreadScheduler();
      return SchedulerAlgorithm.fromCOrdinal(sched);
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

   public void join()
   {
      joinWithReturn();
   }

   public int joinWithReturn()
   {
      // pthread_join can only be called once. Using a mutex here to allow multiple threads calling join. 
      // The first caller will wait for the native join, the rest will block on the mutex.
      
      joinLock.lock();
      {
         if(!hasJoined)
         {
            returnValue = RealtimeNative.join(threadID);
            hasJoined = true;
         }
      }
      joinLock.unlock();
      
      return returnValue;
   }

   public long getThreadID()
   {
      return threadID;
   }
   
   /**
    * Uses CLOCK_REALTIME to get the current time
    * @return gets the wall time in nanoseconds since the unix epoch
    */
   public long getCurrentRealtimeClock()
   {
      return RealtimeNative.getCurrentRealtimeClockTimeNative();
   }
   
   @Override
   public void finalize()
   {
      RealtimeNative.destroy(threadID);
   }
}
