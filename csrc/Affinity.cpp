#include "Affinity.h"
#include <sched.h>
#include <pthread.h>
#include <sched.h>
#include "Utils.hpp"
#include "Thread.h"

#ifdef __MACH__
#include <mach/thread_policy.h>
#include <mach/thread_act.h>
#endif
/*
 * Class:     us_ihmc_affinity_Affinity
 * Method:    setAffinity
 * Signature: ([I)V
 */
JNIEXPORT void JNICALL Java_us_ihmc_affinity_Affinity_setAffinity___3I
  (JNIEnv *env, jclass, jintArray jcpus)
{
	jsize arrayLength = env->GetArrayLength(jcpus);
	jint *cpus = env->GetIntArrayElements(jcpus, 0);
#ifndef __MACH__
	cpu_set_t set;
	CPU_ZERO(&set);

	for(int i = 0; i < arrayLength; i++)
	{
		CPU_SET(cpus[i], &set);
	}

	if(sched_setaffinity(0, sizeof(set), &set) == -1)
	{
		throwRuntimeException(env, "sched_setaffinity: Cannot set processor affinity. Make sure that the CPUS exist");
	}
#else
    thread_port_t port = pthread_mach_thread_np(pthread_self());
    struct thread_affinity_policy policy;

    policy.affinity_tag = arrayLength;

    int ret = thread_policy_set(port, THREAD_AFFINITY_POLICY, (thread_policy_t) &policy, THREAD_AFFINITY_POLICY_COUNT);

    if(ret != 0)
    {
    throwRuntimeException(env, "thread_policy_set: Error setting processor affinity.");
    }
#endif
}

/*
 * Class:     us_ihmc_affinity_Affinity
 * Method:    setAffinity
 * Signature: (J[I)V
 */
JNIEXPORT void JNICALL Java_us_ihmc_affinity_Affinity_setAffinity__J_3I
  (JNIEnv *env, jclass, jlong threadID, jintArray jcpus)
{
    Thread* thread = (Thread*) threadID;
    jsize arrayLength = env->GetArrayLength(jcpus);
    jint *cpus = env->GetIntArrayElements(jcpus, 0);

#ifndef __MACH__
	cpu_set_t set;
	CPU_ZERO(&set);

	for(int i = 0; i < arrayLength; i++)
	{
		CPU_SET(cpus[i], &set);
	}

	if(pthread_setaffinity_np(thread->thread, sizeof(set), &set) == -1)
	{
		throwRuntimeException(env, "pthread_setaffinity_np: Cannot set processor affinity. Make sure that the CPUS exist");
	}
#else
    thread_port_t port = pthread_mach_thread_np(thread->thread);
    struct thread_affinity_policy policy;

    policy.affinity_tag = arrayLength;

    int ret = thread_policy_set(port, THREAD_AFFINITY_POLICY, (thread_policy_t) &policy, THREAD_AFFINITY_POLICY_COUNT);

    if(ret != 0)
    {
    throwRuntimeException(env, "thread_policy_set: Error setting processor affinity.");
    }
#endif
}
