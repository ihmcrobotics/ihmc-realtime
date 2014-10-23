cmake_minimum_required(VERSION 2.6)
project(IHMCRealtime)

IF(CMAKE_INSTALL_PREFIX_INITIALIZED_TO_DEFAULT)
  SET(CMAKE_INSTALL_PREFIX
    ${CMAKE_SOURCE_DIR}/resources/us/ihmc/realtime CACHE PATH "IHMCRealtime install prefix" FORCE
    )
ENDIF(CMAKE_INSTALL_PREFIX_INITIALIZED_TO_DEFAULT)

set(CMAKE_BUILD_TYPE RelWithDebInfo)


find_package(JNI REQUIRED)

add_subdirectory(csrc)
