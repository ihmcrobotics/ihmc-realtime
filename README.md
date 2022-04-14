# IHMC Realtime
[ ![ihmc-realtime](https://maven-badges.herokuapp.com/maven-central/us.ihmc/ihmc-realtime/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/us.ihmc/ihmc-realtime)
[ ![buildstatus](https://bamboo.ihmc.us/plugins/servlet/wittified/build-status/LIBS-IHMCREALTIME)](https://bamboo.ihmc.us/plugins/servlet/wittified/build-status/LIBS-IHMCREALTIME)

IHMCRealtime is a simple support library that provides four feature sets:

1. A JNI-backed threading library for attaching real-time POSIX threads to a running JVM process, allowing for
deterministic computation of tasks
2. A JNI-backed CPU Affinity library
3. Pure Java data structures for lockless inter-thread communication
4. Simple utility to retrieve Linux processes from /proc and set their scheduler and priority
5. Setting CPU DMA Latency

Pre-compiled shared objects for the native portion are provided for Ubuntu 14.04+ and Mac OS X 10.8+.  The libraries
can be easily rebuilt using the instructions included below.

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
IHMC Realtime can be downloaded from BinTray using Maven or gradle. Add the following to your gradle build script


```gradle
repositories {
   mavenCentral()
}

dependencies {
       compile group: 'us.ihmc', name: 'ihmc-realtime', version: '1.5.0'
}
```

### Building and dependency management
IHMCRealtime uses the gradle build system. To build, simply run

```
gradle jar
```

## Native Component

### System Requirements
The Java portion of IHMCRealtime was developed and tested under Java 7.

IHMCRealtime is designed to be run on a Linux kernel backed OS with the RT_PREEMPT patch applied. Instructions for
setting up such a system can be found [here](https://rt.wiki.kernel.org/index.php/RT_PREEMPT_HOWTO).  IHMCRealtime has
been built and tested on Ubuntu 12.04, 12.10, 13.04, 13.10, and 14.04.

IHMCRealtime provides limited support for OS X via the Mach threading API; the JNI library will build and link under
OS X, but the deadlines will not be enforced nearly as hard as they are on Linux-based installations with pure pthreads.

### Re-building the JNI portion

#### Dependencies

On Ubuntu, you will need a minimum of the `cmake`, `build-essential`, and `libcap-dev` installed from your package manager.  You will also need a JDK installation for the JNI headers.  IHMCRealtime was developed against OpenJDK 8
from the Ubuntu package repositories.

    $ sudo apt-get update && sudo apt-get install cmake build-essential libcap-dev openjdk-8-jdk

#### Invoking CMake

You can build the libraries with the following commands:

    $ cd IHMCRealtime
    $ mkdir build
    $ cd build
    $ cmake ..
    $ make install

The .so or .dylib files will be placed in the `lib` folder.

## License
     Copyright 2014 Florida Institute for Human and Machine Cognition (IHMC)

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.

     Written by Jesper Smith, Doug Stephen, and Alex Lesman with assistance from IHMC team members
