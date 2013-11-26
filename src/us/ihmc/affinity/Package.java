package us.ihmc.affinity;

import java.util.ArrayList;

public class Package
{
   private final int id;
   private final ArrayList<Core> cores = new ArrayList<>();
   
   public Package(int id)
   {
      this.id = id;
   }
         
   public void addCore(int cpu)
   {
      for(Core core : cores)
      {
         if(core.isThreadSibling(cpu))
            return;
      }
      
      cores.add(new Core(cpu));
   }  

   public Core getCore(int core)
   {
      return cores.get(core);
   }
   
   @Override
   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append("\tPackage id: ");
      builder.append(id);
      builder.append('\n');
      for(Core core : cores)
      {
         builder.append(core.toString());
      }
      
      return builder.toString();
   }

   public int getNumberOfCores()
   {
      return cores.size();
   }
   
   public int getNumberOfProcessors()
   {
      int processors = 0;
      for(Core core : cores)
      {
         processors += core.getNumberOfProcessors();
      }
      
      return processors;
   }

}
