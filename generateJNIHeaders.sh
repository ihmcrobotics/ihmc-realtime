#!/bin/sh
javah -classpath ./bin -o csrc/RealtimeNative.h us.ihmc.realtime.RealtimeNative
javah -classpath ./bin -o csrc/Affinity.h us.ihmc.affinity.Affinity
