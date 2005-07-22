package utmj.threaded;

import junit.framework.*;
import junit.extensions.*;

/**
 * This class was motivated by JunitPerf and partly copied from there.
 *
 * The <code>ThreadedTest</code> is a test decorator that
 * runs a test in a separate thread.
 *
 * @author <a href="mailto:mike@clarkware.com">Mike Clark</a>
 * @author <a href="mailto:john.link@gmx.net">Johannes Link</a>
 *
 * @see junit.framework.Test
 */

public class ThreadedTest extends TestDecorator {
	private ThreadGroup group;
	private volatile boolean finished = false;

	class TestRunner implements Runnable {
		private TestResult result;
		public TestRunner(TestResult result) {
			this.result = result;
		}
		public void run() {
			basicRun(result);
			finished = true;
		}
	}

/**
 * Constructs a <code>ThreadedTest</code> to decorate the
 * specified test using the same thread group as the
 * current thread.
 *
 * @param test Test to decorate.
 */
public ThreadedTest(Test test) {
	this(test, null);
}


/**
 * Constructs a <code>ThreadedTest</code> to decorate the
 * specified test using the specified thread group.
 *
 * @param test Test to decorate.
 * @param group Thread group.
 */
public ThreadedTest(Test test, ThreadGroup group) {
	super(test);
	this.group = group;
}


protected Thread createTestThread(TestResult result) {
	return new Thread(group, new TestRunner(result));
}


protected ThreadGroup getGroup() {
	return group;
}


/**
 * return true if the test's main thread has finished
 */
protected boolean hasFinished() {
	return finished;
}


/**
 * Runs this test.
 *
 * @param result Test result.
 */
public void run(TestResult result) {
	Thread t = this.createTestThread(result);
	t.start();
	try {
		t.join();
	} catch (InterruptedException ignored) {
	}
}


/**
 * Returns the test description.
 *
 * @return Description.
 */
public String toString() {
	return "ThreadedTest: " + super.toString();
}
}