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

#include <time.h>
#include <jni.h>
#include <iostream>
#include <pthread.h>
#include <sys/types.h>
#include <string>
#include <sstream>
#include <cerrno>
#include <cstring>

#include <unistd.h>
#include <sys/syscall.h>

#include "RealTimeHeaders.h"
#include "Thread.h"

#ifndef NSEC_PER_SEC
#define NSEC_PER_SEC 1000000000
#endif

/**
 * Check if cond == true, otherwise throw a Java RuntimeException
 *
 * @param env Java environment
 * @Param
 */
#define JNIassert(env, cond) \
	do { \
		if(!(cond)) { \
			throwRuntimeException((env), __FILE__, __PRETTY_FUNCTION__, __LINE__, errno); \
		} \
	} while(0)

inline void throwRuntimeException(JNIEnv* env, std::string file, std::string function, int line, int err);

/**
 * Attach the current thread to the VM if necessary and return the Java environment
 *
 * @param vm Java VM
 */
inline JNIEnv* getEnv(JavaVM* vm)
{
	// Get the java environment
	JNIEnv* env;
	int getEnvStat = vm->GetEnv((void **) &env, JNI_VERSION_1_6);
	if (getEnvStat == JNI_EDETACHED)
	{

		struct sched_param priority;
		int policy;

		JNIassert(env, pthread_getschedparam(pthread_self(), &policy, &priority) == 0);

        #if __MACH__
        std::cout << "Attaching native thread " << (syscall(SYS_thread_selfid)) << " with priority " << priority.SCHED_PRIORITY << " to JVM" << std::endl;
        #else
		std::cout << "Attaching native thread " << ((long int)syscall(SYS_gettid)) << " with priority " << priority.SCHED_PRIORITY << " to JVM" << std::endl;
		#endif

		if (vm->AttachCurrentThread((void **) &env, NULL)
				!= 0)
		{
			std::cerr << "Failed to attach" << std::endl;
			return 0;
		}

	}
	else if (getEnvStat == JNI_EVERSION)
	{
		std::cerr << "GetEnv: Version not supported" << std::endl;
		return 0;
	}
	else if (getEnvStat == JNI_OK)
	{
		//
	}

	return env;
}

inline void releaseEnv(JavaVM* vm)
{
	vm->DetachCurrentThread();
}

/**
 * Helper function to throw a Java runtime exception
 *
 * @param env Java environment
 * @param msg Message to throw
 */
inline void throwRuntimeException(JNIEnv* env, std::string msg)
{
	jclass exClass = env->FindClass("java/lang/RuntimeException");
	env->ThrowNew(exClass, msg.c_str());
}

/**
 * Helper function to throw a formatted runtime exception
 *
 * @param env Java environment
 * @param file Caller file name
 * @param function Caller function name
 * @param line Caller line #
 */
inline void throwRuntimeException(JNIEnv* env, std::string file, std::string function, int line, int err)
{
	std::stringstream s;
	s << "Exception in " << file << ", " << function << " at line " << line << ": " << strerror(err) << " ";
	throwRuntimeException(env, s.str());
	JavaVM* vm;
	env->GetJavaVM(&vm);
	vm->DetachCurrentThread();
}

/**
 * Return a system-indepedent monotic clock result
 */
inline int system_monotonic_gettime(struct timespec *tp)
{
#ifdef __MACH__
    if(!clockInitialized)
    {
        host_get_clock_service(mach_host_self(), SYSTEM_CLOCK, &cclock);
        clockInitialized = true;
    }
    kern_return_t ret = clock_get_time(cclock, &mts);
    if(ret != KERN_SUCCESS)
    {
        return -1;
    }
    else
    {
        tp->tv_sec = mts.tv_sec;
        tp->tv_nsec = mts.tv_nsec;
        return 0;
    }
#else
    return clock_gettime(CLOCK_MONOTONIC, tp);
#endif
}

#ifdef __MACH__
/**
 *  Sets the additional params on the pthread using the Mach
 *  conventions.  This is all sort of heuristic based, and
 *  probably need some tuning.
 */
inline void set_mach_thread_params(JNIEnv* env, Thread* thread)
{
    int result;
    thread_port_t port = pthread_mach_thread_np(thread->thread);
    
    thread_extended_policy_data_t extendedPolicy;
    extendedPolicy.timeshare = 0;
    result = thread_policy_set(port, THREAD_EXTENDED_POLICY, (thread_policy_t)&extendedPolicy, THREAD_EXTENDED_POLICY_COUNT);
    JNIassert(env, result == KERN_SUCCESS);
    
    thread_precedence_policy_data_t precedencePolicy;
    precedencePolicy.importance = INT32_MAX; // - (sched_get_priority_max(SCHED_POLICY) - thread->priority);
    result = thread_policy_set(port, THREAD_PRECEDENCE_POLICY, (thread_policy_t) &precedencePolicy, THREAD_PRECEDENCE_POLICY_COUNT);
    JNIassert(env, result == KERN_SUCCESS);
    
    if(thread->periodic)
    {
        mach_timebase_info_data_t timebaseInfo;
        result = mach_timebase_info(&timebaseInfo);
        JNIassert(env, result == KERN_SUCCESS);
        
        thread_time_constraint_policy_data_t timeConstraint;
        double millisToMachAbsoluteTime = ((double) timebaseInfo.denom / (double) timebaseInfo.numer) * 1e6;
        uint32_t periodInMillis = (thread->period.tv_nsec * 1e-6) + (thread->period.tv_sec * 1e3);
        
        timeConstraint.period = periodInMillis * millisToMachAbsoluteTime;
        timeConstraint.computation = (periodInMillis * 0.90) * millisToMachAbsoluteTime;
        timeConstraint.constraint = (periodInMillis * 0.95) * millisToMachAbsoluteTime;
        timeConstraint.preemptible = 0;
        
        result = thread_policy_set(port, THREAD_TIME_CONSTRAINT_POLICY, (thread_policy_t) &timeConstraint, THREAD_TIME_CONSTRAINT_POLICY_COUNT);
        JNIassert(env, result == KERN_SUCCESS);
    }
}
#endif

/**
 * Normalize ts to nsec < 1000000000
 *
 * @param ts
 */
static inline void tsnorm(struct timespec *ts)
{
   while (ts->tv_nsec >= NSEC_PER_SEC) {
      ts->tv_nsec -= NSEC_PER_SEC;
      ts->tv_sec++;
   }
}

/**
 * Add ts2 to ts1 and set ts1 to the result
 *
 * @param ts1
 * @param ts2
 */
static inline void tsadd(struct timespec *ts1, struct timespec *ts2)
{
	ts1->tv_sec += ts2->tv_sec;
	ts1->tv_nsec += ts2->tv_nsec;
	tsnorm(ts1);
}

/**
 * Add ts2 to ts1 and set ts1 to the result
 *
 * @param ts1
 * @param ts2
 * @param offset
 */
static inline void tsadd(struct timespec *ts1, struct timespec *ts2, long long offset)
{
	ts1->tv_sec += ts2->tv_sec;
	ts1->tv_nsec += ts2->tv_nsec + offset;
	tsnorm(ts1);
}


/**
 * Check if ts1 < ts2
 *
 * @param ts1
 * @param ts2
 */
static inline bool tsLessThan(struct timespec *ts1, struct timespec *ts2)
{
	if(ts1->tv_sec == ts2->tv_sec)
	{
		return ts1->tv_nsec < ts2->tv_nsec;
	}
	else
	{
		return ts1->tv_sec < ts2->tv_sec;
	}
}

static inline long tsdelta(struct timespec* start, struct timespec* end)
{
   long delta = (end->tv_sec - start->tv_sec) * 1000000000L;
   delta += end->tv_nsec - start->tv_nsec;
   return delta;
}
