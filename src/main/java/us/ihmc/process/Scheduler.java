package us.ihmc.process;

import java.io.IOException;
import java.util.List;

import us.ihmc.realtime.RealtimeNative;

/**
 * Utility to set and get the scheduler and priority of a LinuxProcess
 * 
 * @author jesper
 *
 */
public class Scheduler
{
   static
   {
      RealtimeNative.init();
   }

   static native int setScheduler(int pid, int sched, int priority);

   static native int getScheduler(int pid);

   static native int getPriority(int pid);

   /**
    * Set the desired scheduler
    * 
    * @param process A LinuxProcess, if null the scheduler of the current process is set
    * @param scheduler Desired scheduler
    * @param priority Desired priority
    * @throws IOException If the desired schedular and priority cannot be set.
    */
   public static void setScheduler(LinuxProcess process, SchedulerAlgorithm scheduler, int priority) throws IOException
   {
      int pid;
      if (process == null)
      {
         pid = 0;
      }
      else
      {
         pid = process.getPid();
      }

      int ret = setScheduler(pid, scheduler.getCOrdinal(), priority);
      if (ret < 0)
      {
         throw new IOException("Cannot set scheduler of pid " + pid + ". Error no: " + ret);
      }

   }

   /**
    * Get the scheduler for a LinuxProcess.
    * 
    * @param process A LinuxProcess, if null the scheduler of the current process is returned
    * @return Scheduler of the Linux Process
    * @throws IOException If the scheduler of the LinuxProcess cannot be retrieved
    */
   public static SchedulerAlgorithm getScheduler(LinuxProcess process) throws IOException
   {
      int pid;
      if (process == null)
      {
         pid = 0;
      }
      else
      {
         pid = process.getPid();
      }

      int sched = getScheduler(pid);
      if (sched < 0)
      {
         throw new IOException("Cannot get scheduler of pid " + pid + ". Error no: " + sched);
      }

      return SchedulerAlgorithm.fromCOrdinal(sched);
   }

   /**
    * Get the priority of a Linux process
    * 
    * @param process  A LinuxProcess, if null the priority of the current process is returned
    * @return The priority of the LinuxProcess
    * @throws IOException If the priority of the LinuxProcess cannot be retrieved
    */
   public static int getPriority(LinuxProcess process) throws IOException
   {
      int pid;
      if (process == null)
      {
         pid = 0;
      }
      else
      {
         pid = process.getPid();
      }

      int priority = getPriority(pid);
      if (priority < 0)
      {
         throw new IOException("Cannot get priority of pid " + pid + ". Error no: " + priority);
      }

      return priority;
   }
}
