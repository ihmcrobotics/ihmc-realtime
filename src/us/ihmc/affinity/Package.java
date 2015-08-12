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
