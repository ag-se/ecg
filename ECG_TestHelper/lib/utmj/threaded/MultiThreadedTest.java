package utmj.threaded;

import junit.framework.*;

/** 
 * Copyright (c) 2001, <a href="mailto:john.link@gmx.net">Johannes Link</a>
 *
 * A special threaded test which uses a ThreadedTestGroup as thread group.
 * This ensures that exceptions and failures in spawned threads are registered in the
 * test result object.
 * If the test finishes (regularly or unexpectedly) all running threads of this test 
 * are interrupted.
 *
 * Caveat: When you decorate a test suite, the first failure or exception in a spawned thread
 * will terminate the whole suite. Thus, some test cases might not be run.
 */
public class MultiThreadedTest extends ThreadedTest {
	private long timeoutMilliseconds;

public MultiThreadedTest(Test test) {
	this(test, 0);
}


public MultiThreadedTest(Test test, long timeoutMilliseconds) {
	super(test, new ThreadedTestGroup(test));
	this.timeoutMilliseconds = timeoutMilliseconds;
}


private void checkTimeout(TestResult result, long millisecondsRun) {
	if (timeoutMilliseconds > 0) {
		if (!this.hasFinished() && millisecondsRun >= timeoutMilliseconds) {
			result.addFailure(this.getTest(), new AssertionFailedError("Timeout of " + timeoutMilliseconds + " exceeded."));
		}
	}
}


private ThreadedTestGroup getThreadedTestGroup() {
	return (ThreadedTestGroup) this.getGroup();
}


public void run(TestResult result) {
	this.getThreadedTestGroup().setTestResult(result);
	Thread thread = this.createTestThread(result);
	long before = System.currentTimeMillis();
	thread.start();
	try {
		thread.join(timeoutMilliseconds);
		long millisecondsRun = System.currentTimeMillis() - before;
		this.checkTimeout(result, millisecondsRun);
	} catch (InterruptedException ignore) {
	}
	this.getThreadedTestGroup().interrupt();
}


public String toString() {
	return "Multi" + super.toString();
}
}