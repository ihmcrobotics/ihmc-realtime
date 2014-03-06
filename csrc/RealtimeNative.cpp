#include <time.h>
#include <sys/time.h>
#include <sched.h>
#include <sys/mman.h>
#include <iostream>
#include <pthread.h>

#if __linux__
	#include <sys/capability.h>
#endif

#if __MACH__
#include <mach/clock.h>
#include <mach/clock_types.h>
#include <mach/mach.h>
#include <mach/thread_policy.h>
#include <mach/mach_init.h>

clock_serv_t cclock;
mach_timespec_t mts;
bool clockInitialized = false;
#endif

#include "RealtimeNative.h"
#include "Utils.hpp"
#include "Thread.h"

const int SCHED_POLICY = SCHED_RR;

#define NSEC_PER_SEC 1000000000

JavaVM* globalVirtualMachine;


int magical_gettime(struct timespec *tp)
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
#if defined(__i386__)
static __inline__ unsigned long long rdtsc(void) {
    unsigned long long int x;
    __asm__ volatile (".byte 0x0f, 0x31" : "=A" (x));
    return x;

}

#elif defined(__x86_64__)
static __inline__ unsigned long long rdtsc(void) {
    unsigned hi, lo;
    __asm__ __volatile__ ("rdtsc" : "=a"(lo), "=d"(hi));
    return ( (unsigned long long)lo)|( ((unsigned long long)hi)<<32 );
}
#endif
#endif

void* run(void* threadPtr)
{
	Thread* thread = (Thread*) threadPtr;
	JNIEnv* env = getEnv(globalVirtualMachine);

	if(env == 0)
	{
		// Cannot throw a runtime exception because we don't have an env
		std::cerr << "Cannot load env" << std::endl;
		return (void*)-1;
	}

	if(thread->periodic && thread->setTriggerToClock)
	{
	        JNIassert(env, magical_gettime(&thread->nextTrigger) == 0);
		thread->setTriggerToClock = false;
	}

	env->CallVoidMethod(thread->javaThread, thread->methodID);

	env->DeleteGlobalRef(thread->javaThread);
	delete thread;

	releaseEnv(globalVirtualMachine);

	thread->retVal = 0;
	pthread_exit(&thread->retVal);
	return NULL;
}


/**
 * Call mlockall to avoid paging memory
 */
JNIEXPORT void JNICALL Java_us_ihmc_realtime_RealtimeNative_mlockall(JNIEnv* env, jclass klass)
{
#if __linux__
	struct __user_cap_header_struct cap_header_data;
	cap_header_data.pid = getpid();
	cap_header_data.version = _LINUX_CAPABILITY_VERSION;

	struct __user_cap_data_struct cap_data;
	capget(&cap_header_data, &cap_data);
	if((cap_data.effective & CAP_IPC_LOCK) != CAP_IPC_LOCK)
	{
//		throwRuntimeException(env, "Cannot lock memory, expect page faults. Run as root");
		std::cerr << "Cannot lock memory, expect page faults. Run as root" << std::endl;
	}
	else
	{
		JNIassert(env, mlockall(MCL_CURRENT|MCL_FUTURE) == 0);
	}
#else
	std::cerr << "Not locking memory on non-linux OS" << std::endl;
#endif
}

/**
 * Register VM to call back to target runnable
 */
JNIEXPORT void JNICALL Java_us_ihmc_realtime_RealtimeNative_registerVM
  (JNIEnv* env, jclass klass)
{
	JNIassert(env, env->GetJavaVM(&globalVirtualMachine) == 0);
}


/**
 * Create a new thread, but do not start
 *
 * @param target Target classes, needs to define a run() method
 * @param priority System priority for thread
 * @param periodic True if thread is periodic
 * @param startOnClock True if the thread has to start at startSeconds + startNanoseconds
 * @param startSeconds Start time relative to MONOTONIC clock, seconds part
 * @param startNanoseconds Start time relative to MONOTONIC clock, nanoseconds part
 * @param periodSeconds Period, seconds part
 * @param periodNanoseconds Period, nanoseconds part
 *
 * @return 64bit pointer to Thread object
 */
JNIEXPORT jlong JNICALL Java_us_ihmc_realtime_RealtimeNative_createThread(JNIEnv* env, jclass klass,
		jobject target, jint priority, jboolean periodic, jboolean startOnClock, jlong startSeconds,
		jlong startNanoseconds, jlong periodSeconds, jlong periodNanoseconds)
{

	// Create thread object
	Thread* thread = new Thread();
	thread->priority = priority;
	thread->periodic = periodic;
	if(periodic)
	{
		if(startOnClock)
		{
			thread->setTriggerToClock = false;
			thread->nextTrigger.tv_sec = startSeconds;
			thread->nextTrigger.tv_nsec = startNanoseconds;

			// Normalize nextTrigger
			tsnorm(&thread->nextTrigger);
		}
		else
		{
			thread->setTriggerToClock = true;
		}

		thread->period.tv_sec = periodSeconds;
		thread->period.tv_nsec = periodNanoseconds;

		// Normalize period
		tsnorm(&thread->period);
	}


	// Get reference to thread function
	thread->javaThread = env->NewGlobalRef(target);
	jclass targetClass = env->GetObjectClass(target);

	JNIassert(env, targetClass != NULL);
	thread->methodID = env->GetMethodID(targetClass, "runThread", "()V");

	JNIassert(env, thread->methodID != NULL);

	return (long long)thread;
}

/**
 * Start thread
 *
 * @param threadPtr 64bit pointer to Thread
 */
JNIEXPORT jint JNICALL Java_us_ihmc_realtime_RealtimeNative_startThread(JNIEnv* env, jclass klass,
		jlong threadPtr)
{
	Thread* thread = (Thread*) threadPtr;

	pthread_attr_t attr;
	sched_param param;
	param.SCHED_PRIORITY = thread->priority;

	JNIassert(env, pthread_attr_init(&attr) == 0);
	JNIassert(env, pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE) == 0);
	JNIassert(env, pthread_attr_setinheritsched(&attr, PTHREAD_EXPLICIT_SCHED) == 0);
	JNIassert(env, pthread_attr_setschedpolicy(&attr, SCHED_POLICY) == 0);
	JNIassert(env, pthread_attr_setschedparam(&attr, &param) == 0);

	int err = pthread_create(&thread->thread, &attr, run, thread);

	#ifdef __MACH__
	    struct timespec timespec;
       	timespec.tv_nsec = 0;
       	timespec.tv_sec = 1;

   	    long long start = rdtsc();
   	    nanosleep(&timespec, NULL);
   	    long long HZ = rdtsc() - start;

        thread_port_t port = pthread_mach_thread_np(thread->thread);
   	    struct thread_time_constraint_policy ttcpolicy;

        long period = (thread->period.tv_nsec * 1e9) + (thread->period.tv_sec);

        if(period > 0)
        {
            ttcpolicy.period = HZ / (period * 1.0e-9);
        }
        else
        {
            ttcpolicy.period = 0;
        }

        ttcpolicy.computation = ttcpolicy.period;
        ttcpolicy.constraint = ttcpolicy.period;
        ttcpolicy.preemptible = 0;

        thread_policy_set(port, THREAD_TIME_CONSTRAINT_POLICY, (thread_policy_t)&ttcpolicy, THREAD_TIME_CONSTRAINT_POLICY_COUNT);
  	#endif

	pthread_attr_destroy(&attr);

	return err;
}

/**
 * Wait till thread finishes
 */
JNIEXPORT jint JNICALL Java_us_ihmc_realtime_RealtimeNative_join
  (JNIEnv *env, jclass klass, jlong threadPtr)
{
	Thread* thread = (Thread*) threadPtr;
	int *retVal;
	pthread_join(thread->thread, (void**) &retVal);

	return *retVal;
}

long waitForAbsoluteTime(timespec* ts)
{
	timespec currentTime;
	magical_gettime(&currentTime);

	long delta = tsdelta(&currentTime, ts);

	if(delta < 0)
	{
		return delta;
	}

	#ifdef __MACH__
	while(nanosleep(ts, NULL) == EINTR);
	#else
	while(clock_nanosleep(CLOCK_MONOTONIC, TIMER_ABSTIME, ts, NULL) == EINTR);
        #endif
	return delta;
}


/**
 * Wait for next period in thread
 *
 * @param threadPtr 64bit pointer to Thread
 */
JNIEXPORT jlong JNICALL Java_us_ihmc_realtime_RealtimeNative_waitForNextPeriod(JNIEnv* env, jclass klass,
		jlong threadPtr)
{
	Thread* thread = (Thread*) threadPtr;
	if(!thread->periodic)
	{
		throwRuntimeException(env, "Thread is not periodic");
	}

	tsadd(&thread->nextTrigger, &thread->period);

	return waitForAbsoluteTime(&thread->nextTrigger);
}

/**
 * Wait for a specific time
 *
 * @param threadPtr 64bit pointer to Thread
 * @param seconds Monotonic time, seconds part
 * @param nanoseconds Monotonic time, nanoseconds part
 */
JNIEXPORT jlong JNICALL Java_us_ihmc_realtime_RealtimeNative_waitUntil (JNIEnv* env , jclass klass, jlong threadPtr, jlong seconds, jlong nanoseconds)
{
	Thread* thread = (Thread*) threadPtr;
	timespec ts;
	ts.tv_sec = seconds;
	ts.tv_nsec = nanoseconds;
	tsnorm(&ts);

	return waitForAbsoluteTime(&ts);
}

/**
 * Set the next period to the current clock time
 *
 * @param threadPtr 64bit pointer to Thread
 */
JNIEXPORT void JNICALL Java_us_ihmc_realtime_RealtimeNative_setNextPeriodToClock
  (JNIEnv* env, jclass klass, jlong threadPtr)
{
	Thread* thread = (Thread*) threadPtr;
	if(!thread->periodic)
	{
		throwRuntimeException(env, "Thread is not periodic");
	}

	JNIassert(env, magical_gettime(&thread->nextTrigger) == 0);
	thread->setTriggerToClock = false;
}

JNIEXPORT void JNICALL Java_us_ihmc_realtime_RealtimeNative_setNextPeriod
  (JNIEnv* env, jclass klass, jlong threadPtr, jlong seconds, jlong nanoseconds)
{
	Thread* thread = (Thread*) threadPtr;
	if(!thread->periodic)
	{
		throwRuntimeException(env, "Thread is not periodic");
	}

	thread->nextTrigger.tv_sec = seconds;
	thread->nextTrigger.tv_nsec = nanoseconds;
	tsnorm(&thread->nextTrigger);
	thread->setTriggerToClock = false;
}

JNIEXPORT jlong JNICALL Java_us_ihmc_realtime_RealtimeNative_getNextPeriod
  (JNIEnv* env, jclass klass, jlong threadPtr)
{
	Thread* thread = (Thread*) threadPtr;
	if(!thread->periodic)
	{
		throwRuntimeException(env, "Thread is not periodic");
	}


	return (((long long) thread->nextTrigger.tv_sec) * NSEC_PER_SEC) + thread->nextTrigger.tv_nsec;
}

/**
 * Get maximum priority for a thread
 */
JNIEXPORT jint JNICALL Java_us_ihmc_realtime_RealtimeNative_getMaximumPriorityNative(JNIEnv* env,
		jclass klass)
{
	return sched_get_priority_max(SCHED_POLICY);
}

/**
 * Get minimum priority for a thread
 */
JNIEXPORT jint JNICALL Java_us_ihmc_realtime_RealtimeNative_getMinimumPriorityNative(JNIEnv* env,
		jclass klass)
{
	return sched_get_priority_min(SCHED_POLICY);
}

/**
 * Return current time from MONOTONIC clock
 */
JNIEXPORT jlong JNICALL Java_us_ihmc_realtime_RealtimeNative_getCurrentTimeNative(JNIEnv* env, jclass klass)
{
	struct timespec t;
	JNIassert(env, magical_gettime(&t) == 0);

	return (((long long) t.tv_sec) * NSEC_PER_SEC) + t.tv_nsec;
}

JNIEXPORT jint JNICALL Java_us_ihmc_realtime_RealtimeNative_getCurrentThreadPriority
  (JNIEnv *env, jclass klass)
{
	struct sched_param priority;
	int policy;

	JNIassert(env, pthread_getschedparam(pthread_self(), &policy, &priority) == 0);

	return priority.SCHED_PRIORITY;

}

JNIEXPORT jint JNICALL Java_us_ihmc_realtime_RealtimeNative_getCurrentThreadScheduler
  (JNIEnv *env, jclass klass)
{
	struct sched_param priority;
	int policy;

	JNIassert(env, pthread_getschedparam(pthread_self(), &policy, &priority) == 0);

	return policy;
}
