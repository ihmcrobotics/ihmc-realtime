#!/bin/sh
javah -classpath ./bin -o csrc/RealtimeNative.h us.ihmc.realtime.RealtimeNative
javah -classpath ./bin -o csrc/Affinity.h us.ihmc.affinity.Affinity
javah -classpath ./bin -o csrc/Scheduler.h us.ihmc.process.Scheduler
