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
 *    Written by Jesper Smith and Doug Stephen with assistance from IHMC team members
 */
 
#include <sched.h>
#include "Scheduler.h"
#include <errno.h>

JNIEXPORT jint JNICALL Java_us_ihmc_process_Scheduler_setScheduler
  (JNIEnv *, jclass, jint pid, jint sched, jint priority)
  {
  	sched_param param;
  	param.sched_priority = priority;
  	
  	int ret = sched_setscheduler(pid, sched, &param);
  	if(ret < 0)
  	{
  		return -errno;
  	}
  	else
  	{
  		return ret;
  	}
  }
 
 JNIEXPORT jint JNICALL Java_us_ihmc_process_Scheduler_getScheduler
  (JNIEnv *, jclass, jint pid)
  {
  	int ret = sched_getscheduler(pid);
  	if(ret < 0)
  	{
  		return -errno;
  	}
  	else
  	{
  		return ret;
  	}
  }
 
 JNIEXPORT jint JNICALL Java_us_ihmc_process_Scheduler_getPriority
  (JNIEnv *, jclass, jint pid)
  {
  	sched_param param;
  	int ret = sched_getparam(pid, &param);
  	if(ret < 0)
  	{
  		return -errno;
  	}
  	else
  	{
  		return param.sched_priority;
  	}
  	
  }