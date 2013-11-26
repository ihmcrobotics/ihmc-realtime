package us.ihmc.affinity;

import java.util.ArrayList;

import us.ihmc.realtime.RealtimeThread;

public class Affinity
{
   public static native void setAffinity(int... cpuID);

   private static native void setAffinity(long threadID, int... cpuID);

   private static void setAffinity(RealtimeThread thread, int... cpuID)
   {
      long threadId = thread.getThreadID();
      setAffinity(threadId, cpuID);
   }

   private static int[] getCPUIDs(Processor... processors)
   {
      int[] result = new int[processors.length];
      for (int i = 0; i < processors.length; i++)
      {
         result[i] = processors[i].getId();
      }
      return result;
   }

   private static int[] getCPUIDs(Core... cores)
   {
      ArrayList<Processor> processors = new ArrayList<>();
      for (Core core : cores)
      {
         processors.addAll(core.getProcessors());
      }

      Processor[] processorArray = new Processor[processors.size()];
      return getCPUIDs(processors.toArray(processorArray));
   }

   public static void setAffinity(RealtimeThread thread, Processor... processors)
   {
//      setAffinity(thread, getCPUIDs(processors));
   }

   public static void setAffinity(RealtimeThread thread, Core... cores)
   {
//      setAffinity(thread, getCPUIDs(cores));
   }

   public static void setAffinity(Processor... processors)
   {
//      setAffinity(getCPUIDs(processors));
   }

   public static void setAffinity(Core... cores)
   {
//      setAffinity(getCPUIDs(cores));
   }
}
