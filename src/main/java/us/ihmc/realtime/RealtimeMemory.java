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
 *    Written by Igor Kalkov with assistance from IHMC team members
 */
package us.ihmc.realtime;

public class RealtimeMemory
{
   /**
    * Locks all pages mapped into the address space of the calling process using mlockall().
    */
   public static void lock()
   {
      RealtimeNative.mlockall();
   }

   /**
    * Unlocks all pages mapped into the address space of the calling process using munlockall().
    */
   public static void unlock()
   {
      throw new RuntimeException("Unlocking memory is not implemented yet!");
   }
}
