# Configuring Linux and RT_PREEMPT for usage with IHMC Realtime

This documents describes how to setup Linux to have minimimal latency when used in a realtime context. While this document is focussed on IHMC Realtime, it should still be very useful for other application.

We assume

## Requirements

- A modern, x86-64 based system. (Intel or AMD)
- Basic knowledge of Linux

## Installing Linux

Install Ubuntu Server 16.04.3 LTS 64 bit on your system. 

Ubuntu 16.04.3 can be downloaded from [Ubuntu](https://www.ubuntu.com/download/server/thank-you?country=US&version=16.04.3&architecture=amd64)

Tips:
- Boot the installation drive in UEFI mode to install in UEFI mode
- Disable swap (!)
- Install OpenSSH server
- Don't install anything you don't absolutly need

## Enable root access

Running a realtime thread works best when you have root access. To enable the root account on the computer, login to your new installation and run

    sudo passwd root


To enable root login over SSH, edit "/etc/ssh/sshd_config". Change "PermitRootLogin prohibit-password" to "PermitRootLogin yes". Save and reboot.

Note 1: At IHMC, we usually delete the user account and permit only root logins. This reduces the amount of times people accidently try to run a realtime process as user.
Note 2: To make SSH logins quicker, add  "UseDNS no" at the end of "/etc/ssh/sshd_config".
Note 3: It is technically possible to run realtime threads as user, however that is outside the scope of this document.

## Installing RT_PREEMPT

To reduce latency it is important to install a Linux kernel patched with RT_PREEMPT. We at IHMC provide an Ubuntu Personal Package Archive with a precompiled kernel. This avoids having to manually compile a kernel.

The current kernel we provide is 4.9.30 with the -rt21 RT_PREEMPT patch. To install use the following commands:

    sudo add-apt-repository ppa:ihmcrobotics/ppa
    sudo apt-get update
    sudo apt install linux-image-4.9.30-rt21-ihmc1-generic linux-image-extra-4.9.30-rt21-ihmc1-generic linux-headers-4.9.30-rt21-ihmc1 linux-headers-4.9.30-rt21-ihmc1-generic

Note: All drivers, including keyboard, are in the linux-image-extra package. So make sure to install that.


## Configure kernel options in GRUB for minimal latency

To set the kernel options, edit /etc/default/grub and set GRUB_CMDLINE_LINUX_DEFAULT to 

    GRUB_CMDLINE_LINUX_DEFAULT="isolcpus=[CPUS TO ISOLATE] acpi_irq_nobalance maxcpus=[NUMBER OF CPUS] intel_pstate=disable"

The options have the following effects
- isolcpus: Isolate these CPU's (cores) from the scheduler. No threads will be scheduled on this CPU, but you can manually set the affinity of realtime threads to this CPU. Replace [CPUS TO ISOLATE] with a comma seperated list of CPU's to isolate. Do  not isolate CPU 0. Optional, can be removed if not neccessary.
- acpi_irq_nobalance: Force all IRQ's to be handled by CPU 0. This reduces scheduling jitter.
- maxcpus: Trick to disable HyperThreading. Replaced [NUMBER OF CPUS] with the number of physical cores you have. Optional, can be removed if you don't have hyperthreading.
- intel_pstate=disable: Disable Intel TurboBoost. Drastically reduces latency at the cost of higher power usage. Can be removed if you do not have an Intel processsor.


After editting /etc/default/grub run "update-grub" and reboot the computer.

## Disable irq_balance

Setting acpi_irq_nobalance in the grub configration is not enough. We also need to disable irqbalance in /etc/default/irqbalance. Edit the file to match

    #Configuration for the irqbalance daemon
     
    #Should irqbalance be enabled?
    ENABLED="0"
     
    #Balance the IRQs only once?
    ONESHOT="0"

After editting /etc/default/irqbalance reboot the computer.

## Setting thread affinity of realtime threads

By setting the thread affinty of realtime threads you avoid latency due to the scheduler moving the threads between different cores. 
Ideally, you use isolcpus in the grub configuration to isolate cores to set the thread affinity to. 

Using IHMC Realtime you can set the thread affinity using the following code

    RealtimeThread.setAffinity(new Processor([CPU number]));
    
    
## Increasing kernel irq priority

When doing realtime communication with an external device, be it a network card or other port latency can be reduced by increasing the priority of the kernel's interrupt thread.

Using IHMC Realtime you can increase the priority of an arbritrary proccess using the Scheduler interface

    List<LinuxProcess> proccesses = LinuxProcess.getProcessesByPattern("[REGEXP FOR PROCESS NAME]");
    for(LinuxProcess process : processes)
    {
    Scheduler.setScheduler(ethernetIRQThread, SchedulerAlgorithm.SCHED_FIFO, 90);
    }


If you do not use IHMC Realtime, you could use the "chrt" command. See "man chrt".


- Jesper Smith
