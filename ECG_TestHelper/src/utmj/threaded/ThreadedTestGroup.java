package utmj.threaded;

import junit.framework.*;

/**
 * This class was copied from JunitPerf by J.Link and modified afterwards
 *
 * The <code>ThreadedTestGroup</code> is a <code>ThreadGroup</code>
 * that catches and handles exceptions thrown by threads created 
 * and started by <code>ThreadedTest</code> instances.
 * <p>
 * If a thread managed by a <code>ThreadedTestGroup</code> throws 
 * an uncaught exception, then the exception is added to the current 
 * test's results and all other threads are immediately stopped.
 * Caveat: Waiting threads ignore a stop()!
 *
 * @author Ervin Varga
 * @author <a href="mailto:mike@clarkware.com">Mike Clark</a>
 * @author <a href="http://www.clarkware.com">Clarkware Consulting, Inc.</a>
 * @author <a href="mailto:john.link@gmx.net">Johannes Link</a>
*
 * @see java.lang.ThreadGroup
 */

public class ThreadedTestGroup extends ThreadGroup {

	private Test test;
	private TestResult testResult;
	
/**
 * Constructs a <code>ThreadedTestGroup</code> for the
 * specified test.
 *
 * @param test Current test.
 */
public ThreadedTestGroup(Test test) {
	super("ThreadedTestGroup");
	this.test = test;
}


/**
 * Try to interrupt first then stop the stubborn threads
 */
public void interruptThenStop() {
	this.interrupt();
	if (this.activeCount() > 0) {
	    this.stop(); // For those threads which won't interrupt
	}
}


/**
 * Sets the current test result.
 *
 * @param result Test result.
 */
public void setTestResult(TestResult result) {
	testResult = result;
}


/**
 * Called when a thread in this thread group stops because of
 * an uncaught exception.
 * <p>
 * If the uncaught exception is a <code>ThreadDeath</code>,
 * then it is ignored.  If the uncaught exception is a
 * <code>AssertionFailedError</code>, then a failure
 * is added to the current test result.  Otherwise, an
 * error is added to the current test result.
 *
 * @param t Originating thread.
 * @param e Uncaught exception.
 */
public void uncaughtException(Thread t, Throwable e) {
    if (e instanceof ThreadDeath) {
        return;
    }
    if (e instanceof AssertionFailedError) {
        testResult.addFailure(test, (AssertionFailedError) e);
    } else {
        testResult.addError(test, e);
    }
    this.interruptThenStop();
}
}