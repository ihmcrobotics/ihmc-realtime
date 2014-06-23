/*
 *   Copyright 2014 Florida Institute for Human and Machine Cognition (IHMC)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *    Written by Jesper Smith with assistance from IHMC team members
 */

#include <iostream>
#include <string>

using namespace std;


void cpuID(unsigned i, unsigned regs[4]) {
#ifdef _WIN32
  __cpuid((int *)regs, (int)i);

#else
  asm volatile
    ("cpuid" : "=a" (regs[0]), "=b" (regs[1]), "=c" (regs[2]), "=d" (regs[3])
     : "a" (i), "c" (0));
  // ECX is set to zero for CPUID function 4
#endif
}


int main(int argc, char *argv[]) {
  unsigned regs[4];

  // Get vendor
  char vendor[12];
  cpuID(0, regs);
  ((unsigned *)vendor)[0] = regs[1]; // EBX
  ((unsigned *)vendor)[1] = regs[3]; // EDX
  ((unsigned *)vendor)[2] = regs[2]; // ECX
  string cpuVendor = string(vendor, 12);

  // Get CPU features
  cpuID(1, regs);
  unsigned cpuFeatures = regs[3]; // EDX

  // Logical core count per CPU
  cpuID(1, regs);
  unsigned logical = (regs[1] >> 16) & 0xff; // EBX[23:16]
  cout << " logical cpus: " << logical << endl;
  unsigned cores = logical;

  if (cpuVendor == "GenuineIntel") {
    // Get DCP cache info
    cpuID(4, regs);
    cores = ((regs[0] >> 26) & 0x3f) + 1; // EAX[31:26] + 1

  } else if (cpuVendor == "AuthenticAMD") {
    // Get NC: Number of CPU cores - 1
    cpuID(0x80000008, regs);
    cores = ((unsigned)(regs[2] & 0xff)) + 1; // ECX[7:0] + 1
  }

  cout << "    cpu cores: " << cores << endl;

  // Detect hyper-threads  
  bool hyperThreads = cpuFeatures & (1 << 28) && cores < logical;

  cout << "hyper-threads: " << (hyperThreads ? "true" : "false") << endl;

  return 0;
}

