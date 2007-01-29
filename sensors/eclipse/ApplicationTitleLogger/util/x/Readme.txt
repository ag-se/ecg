                                 Readme
			List of NT Processes-JNI Example

This contains an example demonstrating the use of JNI for enumerate Windows NT 
running processes. Although, the list is not complete(missing Processor,Thread, 
Virtual Memory information), I thought that is an util example.

Description

The right way to find all informations about Windows NT Processes is to check Performance
Data that is grouped by performance object to which it is related. Performance object 
contain performance counters which are used to measure system, aplication and devices(Procesor
counter, Memory counter ...). You can accesed Performance Data through the registry key
HKEY_PERFORMANCE_DATA, using registry functions.
This example not use Performance Data, it use  Process Status API(PSAPI) function.

To make this in C or C++ it is no problem but you can make this in Java with litle work
using Sun JDK native methos interface.   


Contents

Main.java          Entry point to this example
nmakefile          Makefiles for compiling and running this
		   example
MemoryInfo.java    A simple class that contains Memory Counters.
NTProcess.java	   Contains the native methods declaration.
Process.c          Implementation of native methods



Run this Examples:

promp>nmake -f nmakefile JDK=C:\jdk1.2		 		 


Requirements

I tested this example on NT4.0 SP3 machine with Sun JDK 1.2.( I thing that SUN JDK 1.1 
will be enough, but I don't tested).
Also, you need the utilities "nmake" and "cl" from the Microsoft Developer Studio suite 
of products; I tested this example with "nmake" and "cl" from Microsoft Developer Studio 
Version 5.0. 

Documentation

Sun JDK Documentation
Microsoft Developer Network
 




