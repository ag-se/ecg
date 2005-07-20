package utmj.threaded;

import junit.framework.*;

public class MultiThreadedTestTest extends TestCase {

public MultiThreadedTestTest(String selector) {
	super(selector);
}

/** 
 * This method executes some succeeding asserts in a subthread
 */
public void assertInOtherThread() {
	Thread thread1 = new Thread(new Runnable() {
		public void run() {
			assertEquals(1, 1);
			assertNull(null);
		}
	});
	thread1.start();
	try {
		thread1.join();
	} catch (InterruptedException ignored) {}
}


/** This method throws a Runtim Exception in subthread
 */
public void errorInOtherThread() {
	Thread thread = this.spawnErrorThread();
	try {
		Thread.sleep(10000); // should be stopped by error in other thread
		thread.join();
	} catch (InterruptedException ignored) {}
}


/** This method fails in subthread
 */
public void failingInOtherThread() {
	Thread thread1 = new Thread(new Runnable() {
		public void run() {
			fail("Test must fail");
		}
	});
	thread1.start();
	try {
		Thread.sleep(10000); // should be stopped by failure in other thread
		thread1.join();
	} catch (InterruptedException ignored) {}
}


private Thread spawnErrorThread() {
	Thread thread = new Thread(new Runnable() {
		public void run() {
			throw new RuntimeException("Should be thrown");
		}
	});
	thread.start();
	return thread;
}


private Thread spawnWaitingThread() throws Exception {
	Thread thread = new Thread(new Runnable() {
		public void run() {
			try {
				synchronized(this) {
					this.wait();
				}
			} catch (InterruptedException ignored) {
			}
		}
	});
	thread.start();
	return thread;
}


public void testErrorInOtherThread() {
	TestResult result = new TestResult();
	TestCase tc = new MultiThreadedTestTest("errorInOtherThread");
	MultiThreadedTest test = new MultiThreadedTest(tc);
	test.run(result);
	assertEquals(0, result.failureCount());
	assertEquals(1, result.errorCount());
}


public void testErrorWithWaitingThread() {
	TestCase tc = new TestCase("succeeds") {
		public void runTest() throws Exception {
			spawnWaitingThread();
			errorInOtherThread();
		}
	};
	MultiThreadedTest test = new MultiThreadedTest(tc);
	TestResult result = new TestResult();
	test.run(result);
	//assertTrue(!test.hasFinished());
	assertEquals(0, result.failureCount());
	assertEquals(1, result.errorCount());
}


public void testFailureInOtherThread() {
	TestResult result = new TestResult();
	TestCase tc = new MultiThreadedTestTest("failingInOtherThread");
	MultiThreadedTest test = new MultiThreadedTest(tc);
	test.run(result);
	assertEquals(1, result.failureCount());
	assertEquals(0, result.errorCount());
}


public void testSuceedingAssertsInOtherThread() {
	TestResult result = new TestResult();
	TestCase tc = new MultiThreadedTestTest("assertInOtherThread");
	MultiThreadedTest test = new MultiThreadedTest(tc);
	test.run(result);
	assertEquals(0, result.failureCount());
	assertEquals(0, result.errorCount());
}


public void testTimeout() {
	TestCase tc = new TestCase("succeeds") {
		public void runTest() throws Exception {
			Thread.sleep(10000);
		}
	};
	MultiThreadedTest test = new MultiThreadedTest(tc, 100);
	TestResult result = new TestResult();
	test.run(result);
	assertEquals(1, result.failureCount());
	assertEquals(1, result.errorCount()); // Interrupted Exception
}


public void testTimeoutOnWaitingThread() {
	TestCase tc = new TestCase("succeeds") {
		public void runTest() throws Exception {
			synchronized(this) {
				this.wait();
			}
		}
	};
	MultiThreadedTest test = new MultiThreadedTest(tc, 100);
	TestResult result = new TestResult();
	test.run(result);
	assertEquals(1, result.failureCount());
	assertEquals(1, result.errorCount()); // Interrupted Exception
}
}