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

import java.io.IOException;
import java.util.ArrayList;

public class CPUTopology
{
   private final ArrayList<Package> packages = new ArrayList<>();

   public CPUTopology()
   {
      int[] online = getOnline();

      for (int cpu : online)
      {
         int cpuPackage = getCPUPackage(cpu);
         if (cpuPackage != -1)
         {
            while (packages.size() <= cpuPackage)
            {
               packages.add(new Package(packages.size()));
            }

            packages.get(cpuPackage).addCore(cpu);
         }
      }
   }

   private int[] getOnline()
   {
      try
      {
         String range = SysFSTools.readFirstLine("/sys/devices/system/cpu/online");
         return SysFSTools.range(range);
      }
      catch (NumberFormatException | IOException e)
      {
         return new int[0];
      }
   }

   private int getCPUPackage(int cpu)
   {
      try
      {
         String id = SysFSTools.readFirstLine("/sys/devices/system/cpu/cpu" + cpu + "/topology/physical_package_id");
         return Integer.parseInt(id);
      }
      catch (NumberFormatException | IOException e)
      {
         return -1;
      }
   }
   
   public Package getPackage(int packageID)
   {
      return packages.get(packageID);
   }
   
   public int getNumberOfPackages()
   {
      return packages.size();
   }

   public int getNumberOfCores()
   {
      int cores = 0;
      for (Package pack : packages)
      {
         cores += pack.getNumberOfCores();
      }

      return cores;
   }

   public int getNumberOfProcessors()
   {
      int processors = 0;
      for (Package pack : packages)
      {
         processors += pack.getNumberOfProcessors();
      }

      return processors;
   }

   public boolean isHyperThreadingEnabled()
   {
      return getNumberOfCores() != getNumberOfProcessors();
   }

   @Override
   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append("Cores: ");
      builder.append(getNumberOfCores());
      builder.append('\n');
      builder.append("Processors: ");
      builder.append(getNumberOfProcessors());
      builder.append('\n');

      if (isHyperThreadingEnabled())
      {
         builder.append("Hyper-Threading enabled\n");
      }
      else
      {
         builder.append("Hyper-Threading disabled\n");
      }
      
      builder.append("CPU Topology:\n");
      for (Package pack : packages)
      {
         builder.append(pack);
      }

      return builder.toString();
   }

   public static void main(String[] args)
   {
      System.out.println(new CPUTopology());
   }

}
