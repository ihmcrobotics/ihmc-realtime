#include "Affinity.h"
#include <sched.h>
#include <pthread.h>
#include <sched.h>
#include "Utils.hpp"
#include "Thread.h"
/*
 * Class:     us_ihmc_affinity_Affinity
 * Method:    setAffinity
 * Signature: ([I)V
 */
JNIEXPORT void JNICALL Java_us_ihmc_affinity_Affinity_setAffinity___3I
  (JNIEnv *env, jclass, jintArray jcpus)
{
	cpu_set_t set;
	CPU_ZERO(&set);

	jsize arrayLength = env->GetArrayLength(jcpus);
	jint *cpus = env->GetIntArrayElements(jcpus, 0);
	for(int i = 0; i < arrayLength; i++)
	{
		CPU_SET(cpus[i], &set);
	}

	if(sched_setaffinity(0, sizeof(set), &set) == -1)
	{
		throwRuntimeException(env, "sched_setaffinity: Cannot set processor affinity. Make sure that the CPUS exist");
	}
}

/*
 * Class:     us_ihmc_affinity_Affinity
 * Method:    setAffinity
 * Signature: (J[I)V
 */
JNIEXPORT void JNICALL Java_us_ihmc_affinity_Affinity_setAffinity__J_3I
  (JNIEnv *env, jclass, jlong threadID, jintArray jcpus)
{
	cpu_set_t set;
	CPU_ZERO(&set);

	jsize arrayLength = env->GetArrayLength(jcpus);
	jint *cpus = env->GetIntArrayElements(jcpus, 0);
	for(int i = 0; i < arrayLength; i++)
	{
		CPU_SET(cpus[i], &set);
	}

	Thread* thread = (Thread*) threadID;
	if(pthread_setaffinity_np(thread->thread, sizeof(set), &set) == -1)
	{
		throwRuntimeException(env, "pthread_setaffinity_np: Cannot set processor affinity. Make sure that the CPUS exist");
	}
}
