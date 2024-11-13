#!/bin/bash

REPO_ROOT=$(pwd)

rm -rf cppbuild # Optional clean
mkdir cppbuild

#### Building NativeCommonOps ####
cd cppbuild
if [ "$MAC_CROSS_COMPILE_ARM" == "1" ]; then
  cmake -DCMAKE_OSX_ARCHITECTURES="arm64" \
        ..
elif [ "$LINUX_CROSS_COMPILE_ARM" == "1" ]; then
  cmake -DCMAKE_C_COMPILER=aarch64-linux-gnu-gcc \
        -DCMAKE_CXX_COMPILER=aarch64-linux-gnu-g++ \
        -DCMAKE_FIND_ROOT_PATH=/usr/aarch64-linux-gnu \
        -DCMAKE_PROGRAM_PATH=/usr/aarch64-linux-gnu/bin \
        ..
else
  cmake ..
fi
cmake --build . --config Release --target install
cd $REPO_ROOT

#### Copy shared libs to resources ####
cd cppbuild
# Linux
mkdir -p ../src/main/resources/ihmc-realtime/native/linux-arm64
mkdir -p ../src/main/resources/ihmc-realtime/native/linux-x86_64
if [ -f "install/lib/libRealtimeNative.so" ]; then
  if [ "$LINUX_CROSS_COMPILE_ARM" == "1" ]; then
    cp install/lib/libRealtimeNative.so ../src/main/resources/ihmc-realtime/native/linux-arm64
  else
    cp install/lib/libRealtimeNative.so ../src/main/resources/ihmc-realtime/native/linux-x86_64
  fi
fi
# macOS
mkdir -p ../src/main/resources/ihmc-realtime/native/macos-arm64
mkdir -p ../src/main/resources/ihmc-realtime/native/macos-x86_64
if [ -f "install/lib/libRealtimeNative.dylib" ]; then
  if [ "$MAC_CROSS_COMPILE_ARM" == "1" ]; then
    cp install/lib/libRealtimeNative.dylib ../src/main/resources/ihmc-realtime/native/macos-arm64
  else
    cp install/lib/libRealtimeNative.dylib ../src/main/resources/ihmc-realtime/native/macos-x86_64
  fi
fi
cd $REPO_ROOT