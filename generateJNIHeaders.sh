#!/bin/sh
javah -classpath ./classes -o csrc/RealtimeNative.h us.ihmc.realtime.RealtimeNative
javah -classpath ./classes -o csrc/Affinity.h us.ihmc.affinity.Affinity
