ElectroCodeoGram 1.1.3
(c) FU Berlin, Frank Schlesinger, 2006

CONTENTS
========
ecg/ - An ECGLab distribution. 
lib/ - ECG libs to develop modules and Java-based sensors
doc/ - JavaDoc for Jars in /lib
plugins/ - Eclipse-Plugin. NOTE: This does not contain the Lab for INLINE mode. Copy ecg/ to the plugin directory.
user.home/ - Includes files to be copied in the home folder of each user who uses the Eclipse sensor. It is configured for INLINE mode. Change ECG_SERVER_TYPE=REMOTE and the ECG_SERVER_ADDRESS for true Client/Server functionality

REQUIREMENTS
============
Requires Java 1.5 JVM as the default Java version for ECGLab
Requires Java 1.4 for the Eclipse Sensore
Eclipse Sensor runs on Eclipse 3.1 or 3.2

CHANGE LOG
==========
Changes for 1.1.3:
* Badly handled CDATA section in some events resolved (rev903)
* CDATA parsing in FileSystemSourceModule fixed (rev922)
* Allows to terminate ECGLab after last event was read from file (rev910, rev923)
* Hidden folders are now ignored during search for MSDT and Module folders (rev919)
* Some minor bug fixes (rev902, rev911, rev918)
* Build procedure for EclipseSensor (rev920) and main Build (rev924) slightly changed
* MSDTs: Introduced new MSDT 'system' which reports internal ECGLab termination event (rev909), Changed 'user' MSDT schema definition to allow for CDATA sections (rev921)
* Internals: Modules may emit more than one event after recieving event (rev899, rev901, rev902), helper functions (rev908), new package structure in EclipseSensor (rev916) and ECGLab (rev917)
* First version of CodeLocationTrackerIntermediateModule (rev904, rev906, rev915, rev925)
* Cleanup of CodechangeDifferIntermediateModule (rev907, rev914)
* Latest version 1.0.4 of FocusTracker (rev912, rev913)
