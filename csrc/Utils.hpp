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

const int NSEC_PER_SEC = 1000000000;


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

		std::cout << "Attaching native thread " << ((long int)syscall(SYS_gettid)) << " with priority " << priority.__sched_priority << " to JVM" << std::endl;
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
