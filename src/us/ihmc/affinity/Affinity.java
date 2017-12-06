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
package us.ihmc.affinity;

import java.util.ArrayList;

import us.ihmc.realtime.RealtimeNative;
import us.ihmc.realtime.RealtimeThread;

public class Affinity
{
   static
   {
      RealtimeNative.init();
   }
   
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
      setAffinity(thread, getCPUIDs(processors));
   }

   public static void setAffinity(RealtimeThread thread, Core... cores)
   {
      setAffinity(thread, getCPUIDs(cores));
   }

   public static void setAffinity(Processor... processors)
   {
      setAffinity(getCPUIDs(processors));
   }

   public static void setAffinity(Core... cores)
   {
      setAffinity(getCPUIDs(cores));
   }
}
