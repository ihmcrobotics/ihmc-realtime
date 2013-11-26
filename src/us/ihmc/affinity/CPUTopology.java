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
         return SysFSTools.range(SysFSTools.readFirstLine("/sys/devices/system/cpu/online"));
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
         return Integer.valueOf(SysFSTools.readFirstLine("/sys/devices/system/cpu/cpu" + cpu + "/topology/physical_package_id"));
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
