/*
 * Thread.h
 *
 *  Created on: Aug 28, 2013
 *      Author: jesper
 */

#ifndef THREAD_H_
#define THREAD_H_

#include <time.h>
#include <jni.h>

class Thread
{
private:

	jobject javaThread;

	int priority;

	bool periodic;

	struct timespec nextTrigger;
	struct timespec period;


public:
	Thread(jobject javaThread, int priority, bool periodic, struct timespec start, struct timespec period);

};

#endif /* THREAD_H_ */
