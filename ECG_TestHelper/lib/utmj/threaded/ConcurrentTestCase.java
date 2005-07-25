package utmj.threaded;

import java.util.*;
import junit.framework.*;

/** 
 * Copyright (c) 2001, <a href="mailto:john.link@gmx.net">Johannes Link</a>
 *
 * This class can be used to test the concurrent behaviour of a unit under test.
 */
public class ConcurrentTestCase extends TestCase {
	private TestResult currentResult;
	private ThreadedTestGroup threadGroup;
	private Hashtable threads = new Hashtable();
	private boolean deadlockDetected = false;
	private Vector checkpoints = new Vector();

	class ConcurrentTestThread extends Thread {
		private volatile boolean hasStarted = false;
		private volatile boolean hasFinished = false;
		ConcurrentTestThread( ThreadGroup group, Runnable runnable, String name) {
			super(group, runnable, name);
		}
		public void run() {
			hasStarted = true;
			super.run();
			finishThread(this);
		}
	}

public ConcurrentTestCase(String name) {
	super(name);
}


protected void addThread(String name, final Runnable runnable) {
	if (threads.get(name) != null) {
		fail("Thread with name '" + name + "' already exists");
	}
	ConcurrentTestThread newThread = new ConcurrentTestThread(threadGroup, runnable, name);
	threads.put(name, newThread);
}


public synchronized void checkpoint(String checkpointName) {
	checkpoints.addElement(checkpointName);
	this.notifyAll();
}


public boolean checkpointReached(String checkpointName) {
	return checkpoints.contains(checkpointName);
}


public boolean deadlockDetected() {
	return deadlockDetected;
}


private synchronized void finishThread(ConcurrentTestThread thread) {
	thread.hasFinished = true;
	this.notifyAll();
}


private ConcurrentTestThread getThread(String threadName) {
	return (ConcurrentTestThread) threads.get(threadName);
}


/**
 * Returns true if the thread finished normally, i.e. was not inerrupted or stopped
 */
public boolean hasThreadFinished(String threadName) {
	ConcurrentTestThread thread = this.getThread(threadName);
	if (thread == null) {
		fail("Unknown Thread: " + threadName);
	}
	return thread.hasFinished;
}


public boolean hasThreadStarted(String threadName) {
	ConcurrentTestThread thread = this.getThread(threadName);
	if (thread == null) {
		fail("Unknown Thread: " + threadName);
	}
	return thread.hasStarted;
}


private void interruptAllAliveThreads() {
    threadGroup.interruptThenStop();
}


/**
 * Wait till all threads have finished. Wait maximally millisecondsToWait.
 * Should only be called after startThreads().
 */
protected void joinAllThreads(long millisecondsToWait) {
	Enumeration enumi = threads.elements();
	long remainingMilliseconds = millisecondsToWait;
	while (enumi.hasMoreElements()) {
		long before = System.currentTimeMillis();
		ConcurrentTestThread each = (ConcurrentTestThread) enumi.nextElement();
		try {
			each.join(remainingMilliseconds);
		} catch (InterruptedException ignored) {
		}
		long spent = System.currentTimeMillis() - before;
		if (millisecondsToWait != 0) {
			remainingMilliseconds = remainingMilliseconds - spent;
			if (remainingMilliseconds <= 0) {
				deadlockDetected = true;
				break;
			}
		}
	}
}


public void joinThread(String threadName) throws InterruptedException {
	this.joinThread(threadName, 0);
}


public void joinThread(String threadName, long millisecondsToTimeout) throws InterruptedException {
	ConcurrentTestThread thread = this.getThread(threadName);
	if (thread == null) {
		fail("Unknown Thread: " + threadName);
	}
	thread.join(millisecondsToTimeout);
}


/**
 * Stores the current result to be accessible during the test
 */
public void run(TestResult result) {
	currentResult = result;
	super.run(result);
}


protected void setUp() {
	threadGroup = new ThreadedTestGroup(this);
}


/**
 * Sleep and ignore interruption
 */
public void sleep(long milliseconds) {
	try {
		Thread.sleep(milliseconds);
	} catch (InterruptedException ignored) {}
}


/**
 * Run all threads and wait for them to finish without timeout
 */
protected void startAndJoinAllThreads() {
	this.startAndJoinThreads(0);
}


/**
 * Run all threads and wait for them to finish. 
 * Assume deadlock after millisecondsToDeadlock and time out then.
 */
protected void startAndJoinThreads(long millisecondsToDeadlock) {
	this.startThreads();
	this.joinAllThreads(millisecondsToDeadlock);
}


/**
 * Start all threads.
 */
protected void startThreads() {
	threadGroup.setTestResult(currentResult);
	Enumeration enumi = threads.elements();
	while (enumi.hasMoreElements()) {
		ConcurrentTestThread each = (ConcurrentTestThread) enumi.nextElement();
		each.start();
		each.hasStarted = true;
	}
	Thread.yield();
}


protected void tearDown() {
	this.interruptAllAliveThreads();
	threads = new Hashtable();
	checkpoints = new Vector();
	deadlockDetected = false;
	threadGroup = null;
	currentResult = null;
}


/**
 * Wait till a checkpoint has been reached by another thread
 */
public synchronized void waitForCheckpoint(String checkpointName) {
	while (! this.checkpointReached(checkpointName)) {
		try {
			this.wait();
		} catch (InterruptedException ignored) {}
	}
}


/**
 * Wait till a thread has regularly finished. This may never happen.
 * see also joinThread(String threadName);
 */
public synchronized void waitUntilFinished(String threadName) {
	while (! this.hasThreadFinished(threadName)) {
		try {
			this.wait();
		} catch (InterruptedException ignored) {}
	}
}
}