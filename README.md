# IHMC Realtime

![buildstatus](https://github.com/ihmcrobotics/ihmc-realtime/actions/workflows/gradle-test.yml/badge.svg?branch=develop) develop

![buildstatus](https://github.com/ihmcrobotics/ihmc-realtime/actions/workflows/gradle-test.yml/badge.svg?branch=main) main

![buildstatus](https://github.com/ihmcrobotics/ihmc-realtime/actions/workflows/gradle-test.yml/badge.svg?branch=0.1.6) Current release

IHMCRealtime is a simple support library that provides four feature sets:

1. A JNI-backed threading library for attaching real-time POSIX threads to a running JVM process, allowing for
deterministic computation of tasks
2. A JNI-backed CPU Affinity library
3. Pure Java data structures for lockless inter-thread communication
4. Simple utility to retrieve Linux processes from /proc and set their scheduler and priority
5. Setting CPU DMA Latency

Pre-compiled shared objects for the native portion are provided for Ubuntu 20.04+ (x86_64, arm64).

### Documentation
Javadoc of the code can be found at [http://ihmcrobotics.bitbucket.org/ihmcrealtime/](http://ihmcrobotics.bitbucket.org/ihmcrealtime/). In the coming weeks, we will be working on improving the documentation.

### What is "real-time"?
Frequently today, "real-time" is meant to refer to UI design that is extremely responsive in regards to changes in
the underlying data model, especially in systems with latency such as web UI's bound to data models living on a server.  In this situation, however, we are using real-time in the [well-defined](http://en.wikipedia.org/wiki/Real-time_computing) manner, meaning computation that has a time deadline
and as such cannot afford to be interrupted by many things that would normally be allowed to take precedence over a thread of execution, such as the operating system or GUI.  As such, this library is meant to be used on platforms that feature fully preemptible kernels.

### Caveats
Real-time computation in a multi-threaded environment is not a small problem.  This library aims to provide the bare essentials in terms of real-time computation, and so pushes some of the challenge on to the developer instead of solving it in code.  Specifically, IHMC Realtime *does not account for*:

1. Garbage Collection
2. [Priority inversion](http://en.wikipedia.org/wiki/Priority_inversion)

We work around these two restrictions by writing low-to-zero object allocation code in our real-time processes, and by using lockless concurrent data structures.  Code paths that will run inside of a thread spawned using the IHMCRealtime library cannot use `synchronize`, `wait()/notify()`, or other such lock-based thread safety techniques.  Because of this, we have made available the pure Java data structures that we use to avoid having to rely on these constructs.

## Pure Java Component

### Binary
IHMC Realtime can be downloaded from Maven Central using Maven or gradle. Add the following to your gradle build script


```gradle
dependencies {
       compile group: 'us.ihmc', name: 'ihmc-realtime', version: '1.5.0'
}
```

## Native Component

### System Requirements
The Java portion of IHMCRealtime was developed and tested under Java 7.

IHMCRealtime is designed to be run on a Linux kernel backed OS with the RT_PREEMPT patch applied. Instructions for
setting up such a system can be found [here](https://rt.wiki.kernel.org/index.php/RT_PREEMPT_HOWTO).

### Re-building the JNI portion

#### Dependencies

On Ubuntu, you will need a minimum of the `cmake`, `build-essential`, and `libcap-dev` installed from your package manager.  You will also need a JDK installation for the JNI headers.  IHMCRealtime was developed against OpenJDK 17
from the Ubuntu package repositories.

    $ sudo apt-get update && sudo apt-get install cmake build-essential libcap-dev openjdk-17-jdk

To compile, run `cppbuild.bash` from the repository root directory. For building the libraries for a release, use the `build-natives` GitHub workflow.
