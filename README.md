# Ricart-Agrawala-Algorithm
Ricart–Agrawala Algorithm for Mutual Exclusion in Distributed System

* Keep both the StartProcess.java & icon.png in same location.
* Compile the StartProcess.java - javac StartProcess.java
* The main method requires process name & port number to be passed as parameters.
* The program has to be run for each process separately. E.g. java StartProcess A 1000, java StartProcess B 2000 and so on till java StartProcess F 6000
* Critical Section can be requested by any process by clicking on the "Request for Critical Section" button on right.
* Each process window will show the log of all messages sent or received. It will also log when entering, executing, exiting the CS.
* The log window background will turn orange when the process is executing the CS.
* At any instance at max only one process log window will turn orange. This indicates achievement of mutex in Distributed System.
* The demo can be viewed at Ricart-Agrawala - Distributed Mutual Exclusion.mp4
