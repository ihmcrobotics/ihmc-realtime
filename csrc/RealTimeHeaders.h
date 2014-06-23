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
 *    Written by Doug Stephen with assistance from IHMC team members
 */

#ifndef IHMC_RealTimeHeaders_h
#define IHMC_RealTimeHeaders_h

#if __linux__
#include <sys/capability.h>
#endif

#if __MACH__
#include <mach/clock.h>
#include <mach/clock_types.h>
#include <mach/mach.h>
#include <mach/thread_policy.h>
#include <mach/mach_init.h>
#include <mach/mach_time.h>

extern clock_serv_t cclock;
extern mach_timespec_t mts;
extern bool clockInitialized;

#define SCHED_PRIORITY sched_priority
#else
#define SCHED_PRIORITY __sched_priority
#endif

#endif
