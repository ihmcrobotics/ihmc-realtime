cmake_minimum_required(VERSION 2.6)
project(CommonWalkingControlModules)

include_directories(${JNI_INCLUDE_DIRS})


add_library(RealtimeNative SHARED RealtimeNative.cpp Affinity.cpp)
target_link_libraries(RealtimeNative pthread rt)

INSTALL(TARGETS RealtimeNative RUNTIME DESTINATION lib LIBRARY DESTINATION lib)
