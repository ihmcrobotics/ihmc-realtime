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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class SysFSTools
{
   public static int[] range(String range) throws NumberFormatException
   {
      int size = 0;
      ArrayList<int[]> result = new ArrayList<>();
      
      String[] commaList = range.split(",");
      for(String subRange : commaList)
      {
         String[] rangeList = subRange.split("-");
         
         if(rangeList.length == 1)
         {
            result.add(new int[] { Integer.valueOf(subRange) });
            size++;
         }
         else
         {
            
            int start = Integer.valueOf(rangeList[0]);
            int end = Integer.valueOf(rangeList[1]);
            
            int[] subElement = new int[end - start + 1];
            for (int i = 0; i <= (end - start); i++)
            {
               subElement[i] = i + start;
               size++;
            }
            result.add(subElement);
         }
         
      }
      
      int[] intResult = new int[size];
      int index = 0;
      for(int[] subElement : result)
      {
         for(int ele : subElement)
         {
            intResult[index++] = ele;
         }
      }

      return intResult;
   }
   
   public static String readFirstLine(String path) throws IOException
   {
      return Files.readAllLines(Paths.get(path), Charset.forName("US-ASCII")).get(0);
   }


}
