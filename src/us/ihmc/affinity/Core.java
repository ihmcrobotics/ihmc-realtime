package us.ihmc.affinity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Core
{
   private final int id;
   private final ArrayList<Processor> processors = new ArrayList<>();
   
   public Core(int cpuID)
   {
      this.id = cpuID;
      try
      {
         String siblingStrings = SysFSTools.readFirstLine("/sys/devices/system/cpu/cpu" + cpuID + "/topology/thread_siblings_list");
         
         for(int id : SysFSTools.range(siblingStrings)) 
         {
            processors.add(new Processor(id));
         }
      }
      catch (NumberFormatException | IOException e)
      {
         processors.add(new Processor(cpuID));
      }
      
   }
   
   public boolean isThreadSibling(int sibling)
   {
      for(Processor processor : processors)
      {
         if(sibling == processor.getId())
         {
            return true;
         }
      }
      return false;
   }
   
   public int getNumberOfProcessors()
   {
      return processors.size();
   }
   
   public List<Processor> getProcessors()   
   {
      return Collections.unmodifiableList(processors);
   }
   
   public Processor getProcessor(int processor)
   {
      return processors.get(processor);
   }
   
   public Processor getDefaultProcessor()
   {
      return processors.get(0);
   }
   
   @Override
   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append("\t\tPhysical core: ");
      builder.append(id);
      builder.append('\n');
      for(Processor processor : processors)
      {
         builder.append(processor.toString());
      }
      return builder.toString();
   }


   
}
