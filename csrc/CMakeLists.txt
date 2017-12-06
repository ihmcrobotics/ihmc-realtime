cmake_minimum_required(VERSION 2.6)
project(CommonWalkingControlModules)

include_directories(${JNI_INCLUDE_DIRS})


add_library(RealtimeNative SHARED RealtimeNative.cpp Affinity.cpp Scheduler.cpp)

if(APPLE)
target_link_libraries(RealtimeNative pthread)
endif(APPLE)

if(NOT APPLE)
target_link_libraries(RealtimeNative pthread rt)
endif()

INSTALL(TARGETS RealtimeNative RUNTIME DESTINATION lib LIBRARY DESTINATION lib)
