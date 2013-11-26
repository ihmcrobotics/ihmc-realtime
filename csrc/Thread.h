#include <pthread.h>
#include <jni.h>
#include <time.h>

#ifndef THREAD_H_
#define THREAD_H_


struct Thread
{
	pthread_t thread;
	jobject javaThread;
	jmethodID methodID;

	int priority;

	bool periodic;
	bool setTriggerToClock;

	struct timespec nextTrigger;
	struct timespec period;

	int retVal;
};

#endif /* THREAD_H_ */
