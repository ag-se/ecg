package utmj.threaded;
import junit.framework.*;

/** 
 * This class allows you to assert a condition that may not be true right
 * at the moment, but should become true within a specified time frame.
 * To use it, simply replace calls like:
 * <pre>
 *     assert(someCondition);</pre>
 * With:
 * <pre>
 *     new RetriedAssert(5000, 250) { // timeout, interval
 *         public void run() throws Exception {
 *             assert(someCondition);
 *         }
 *     }.start();</pre>
 *
 * The <code>start()</code> and <code>run()</code> methods were named after 
 * those in <code>java.lang.Thread</code>, whose function they mimic.
 * <p>
 * This class was written by <a href="http://www.modeln.com">Model N, Inc.</a>
 * You may use it and modify it any way you wish--but please leave this 
 * message intact.
 *
 * @author Kevin Bourrillion 
 * &lt;<a href="mailto:kevinb@modeln.com">kevinb@modeln.com</a>&gt;
 */
public abstract class RetriedAssert {

    private int _timeOutMs;
    private int _intervalMs;

/**
 * Constructor.
 *
 * @param timeOutMs
 * The minimum total length of time, in milliseconds, to try the 
 * assert before giving up and registering the assertion failure 
 * "for real".  This is not treated as a precise upper limit.
 *
 * @param intervalMs
 * The length of time, in milliseconds, to sleep between tries.
 */
protected RetriedAssert(int timeOutMs, int intervalMs) {
	_timeOutMs = timeOutMs;
	_intervalMs = intervalMs;
}

/**
 * Starts the retries.  If your implementation of run() has an 
 * assertion failure, it sleeps and tries again, until the timeout has
 * elapsed, at which time it lets the assertion failure propagate.
 * However, if run() throws another type of Exception, this will be
 * propagated out to the caller immediately.
 */
public final void start() throws Exception {
	long stopAt = System.currentTimeMillis() + _timeOutMs;

	// Main loop, waiting _intervalMs between tries
	while (System.currentTimeMillis() < stopAt) {
		try {
			run();
			return;
		} catch (AssertionFailedError afe) {
			// Ignore it, we'll try again
		}
		try {
			Thread.sleep(_intervalMs);
		} catch (InterruptedException ie) {
		}
	}

	// All tries have failed so far.  Try one last time, 
	// now letting any failure pass out to the caller.
	run();
}

/**
 * Users of this class implement this method to perform the asserts
 * they want.
 */
public abstract void run() throws Exception;
}

