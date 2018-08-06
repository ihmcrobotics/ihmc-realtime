---
title: Do not use locks
---


Due the way our Realtime code works, locks do not work correctly with respect to priorities. So we cannot use locks. Some examples of locks in Java are

```java
synchronized() {}
ReentrantLock
```

Note that some built in Java objects are synchronized internally and should be avoided. When in doubt, install the OpenJDK source and study the object you want to use.
 
Alternatives to locks are:

-Atomic variables (`AtomicBoolean`, `AtomicLong`, `AtomicInteger`, `AtomicReference`, `AtomicDouble`). If you only need to communicate a single variable between threads, use these classes. Remember that if you want to communicate several booleans, you can also encode that in an `AtomicInteger`. Read and set to a default value using `getAndSet()` if necessary. `getAndSet()` is an atomic operation.
- For sporadic data transfer between threads, use a `ConcurrentLinkedQueue` (do NOT use the blocking variant). Note that the `ConcurrentLinkedQueue` creates a small amount of garbage on insertion.
- For high performance data transfer, use either the `ConcurrentCopier` or the `ConcurrentRingBuffer` classes in [https://github.com/ihmcrobotics/ihmc-realtime](https://github.com/ihmcrobotics/ihmc-realtime).
