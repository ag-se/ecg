
Project: CPC – an Eclipse framework for automated clone life cycle tracking and update anomaly detection
Website: http://cpc.anetwork.de
Author: Valentin Weckerle

-------------------------------------------

Contents:

1) General comments

2) How to build CPC

3) Things to take note of 

4) TODO items

-------------------------------------------


// Under construction, this file will be extended in the future.
// For now please contact me directly, if you have any questions.
// Email - username: weckerle, domain: anetwork.de

1) General comments
Reading the thesis is a recommended first step towards understanding CPC.
Additional documentation can be found inside the source code as JavaDoc comments. 
...

2) How to build CPC
Checkout all projects and use the normal build mechanism of the Eclipse Plug-in Development plug-in to
build the "CPC_Update_Site" Project.
Take note that for some so far unknown reason building may fail if you try to compile CPC from within
an Eclipse installation which has CPC installed. To still benefit from clone tracking during CPC development
the following approach is recommended:
	- use two separate Eclipse installations (i.e. named "development" and "build")
	- install CPC only in the "development" Eclipse and use the "build" Eclipse to build CPC
	- synchronisation between the two can be done via SVN (note: this should be a one way transfer as the
	  build Eclipse should not be used to modify the CPC source)
	- modifications to the CPC_Update_Site project which occur while building CPC should not be checked in
...

3) Things to take note of
The priority of event listeners is not simply a performance or timing question. There are cases
where the priority and the listener execution order resulting from it have crucial importance
for the correct workings of CPC.
It is recommended to always take a look at the implementation of all other registered listeners for
the same event type before adding a new listener or modifying the implementation or priority of an
existing listener.
One example are the CPC_Track and CPC_Reconciler components. When a file is opened the reconciler
needs to be executed before the tracking module starts working on the data. Thus the priority of
the reconciler's listener must always be higher than those of the tracking module's listeners.
...

4) TODO items
A search for TODO and FIXME in the CPC source code should uncover all areas in which further
work is likely to be needed or beneficial.

Some key areas:
- remote synchronisation still needs a lot of work to become functional/meaningful
- the custom ORM mapper of the CPC_Store_Local_SQL module does not yet support incremental schema updates
- better clone data visualisations are needed
- better classification, similarity and update anomaly detection (notification) strategies are needed

...
