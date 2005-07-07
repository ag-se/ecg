package utmj.threaded;

import junit.framework.*;

public class ExceptionAssertTest extends TestCase {
public ExceptionAssertTest(String name) {
	super(name);
}

/**
 * Fail if an unexpected exception arrives before the right exception
 */
public void testExpectedExceptionAfterWrongException() {
	ExceptionAssert asserti =
		new ExceptionAssert(IllegalArgumentException.class, 1000) {
		public void run() {
			Thread subThread1 = new Thread() {
				public void run() {
					throw new RuntimeException();
				}
			};
			Thread subThread2 = new Thread() {
				public void run() {
					try {
						Thread.sleep(200);
					} catch (InterruptedException ignore) {
					}
					throw new IllegalArgumentException();
				}
			};
			subThread2.start();
			subThread1.start();
		}
	};
	try {
		asserti.start();
	} catch (AssertionFailedError expected) {
		return;
	}
	fail("AssertionFailedError expected");
}


/**
 * After timeout the start() fails.
 */
public void testExpectedExceptionComesTooLate() {
	ExceptionAssert asserti = new ExceptionAssert(IllegalArgumentException.class, 1000) {
		public void run() {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException ignore) {}
			throw new IllegalArgumentException();
		}
	};
	try {
		asserti.start();
	} catch (AssertionFailedError expected) {
		return;
	}
	fail("AssertionFailedError expected");
}
	
/**
 * An exception thrown in run 
 * should succeed if it has the expected type.
 */
public void testExpectedExceptionInMainThread() {
	ExceptionAssert asserti = new ExceptionAssert(IllegalArgumentException.class, 2000) {
		public void run() {
			throw new IllegalArgumentException("test");
		}
	};
	asserti.start();
	assertEquals("test", asserti.getCaughtException().getMessage());
}
	
/**
 * An exception thrown in run or in a thread spawned by run
 * should succeed if it has the expected type.
 */
public void testExpectedExceptionInSubThread() {
	ExceptionAssert asserti = new ExceptionAssert(IllegalArgumentException.class, 1000) {
		public void run() {
			Thread subThread = new Thread() {
				public void run() {
					throw new IllegalArgumentException();
				}
			};
			subThread.start();
		}
	};
	asserti.start();
}


/**
 * If there's no exception thrown, start() must fail
 */
public void testNoExceptionInMainThread() {
	ExceptionAssert asserti = new ExceptionAssert(IllegalArgumentException.class, 1000) {
		public void run() {
				// do nothing
		}
	};
	try {
		asserti.start();
	} catch (AssertionFailedError expected) {
		return;
	}
	fail("AssertionFailedError expected");
}


/**
 * If there's the wrong exception thrown, start() must fail
 */
public void testWrongExceptionInMainThread() {
	ExceptionAssert asserti =
		new ExceptionAssert(IllegalArgumentException.class, 1000) {
		public void run() {
			throw new RuntimeException("unexpected");
		}
	};
	try {
		asserti.start();
	} catch (AssertionFailedError expected) {
		return;
	}
	fail("AssertionFailedError expected");
}
}