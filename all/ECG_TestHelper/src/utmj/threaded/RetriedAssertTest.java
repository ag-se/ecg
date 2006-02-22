package utmj.threaded;
import junit.framework.*;

public class RetriedAssertTest extends TestCase {

/** Dumb trick to get around the "final" restriction. */
private static class Counter {
	int value = 0;
}

public RetriedAssertTest(String name) {
	super(name);
}


/** Tests RetriedAssert failing immediately on an error. */
public void testError() throws Exception {
	final Counter c = new Counter();
	try {
		new RetriedAssert(5000, 250) {
			public void run() throws Exception {
				c.value++;
				throw new IllegalArgumentException(); // pick one
			}
		}
		.start();
	} catch (IllegalArgumentException e) {
		// good, it failed
		assertEquals("Number of tries", 1, c.value);
		return;
	}
	fail("Inner exception not propagated");
}


/** Tests RetriedAssert failing after all tries. */
public void testFailure() throws Exception {
	final Counter c = new Counter();
	try {
		new RetriedAssert(5000, 250) {
			public void run() throws Exception {
				c.value++;
				fail();
			}
		}
		.start();
	} catch (AssertionFailedError e) {
		// good, it failed
		assertTrue("Should have tried at least 10 times", c.value > 10);
		return;
	}
	fail("Inner assertion failure not propagated");
}


/** Tests RetriedAssert succeeding on the first try. */
public void testSucceedOneTry() throws Exception {
	final Counter c = new Counter();
	new RetriedAssert(5000, 250) {
		public void run() throws Exception {
			c.value++;
			assertTrue(true);
		}
	}
	.start();
	assertEquals("Number of tries", 1, c.value);
}


/** Tests RetriedAssert succeeding after several tries. */
public void testSucceedSeveralTries() throws Exception {
	final long start = System.currentTimeMillis();
	new RetriedAssert(5000, 250) {
		public void run() throws Exception {
			assertTrue(System.currentTimeMillis() - start > 2000);
		}
	}
	.start();
}
}