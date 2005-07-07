package utmj.threaded;

import junit.framework.*;

public class ThreadedTestTest extends junit.framework.TestCase {
public ThreadedTestTest(String name) {
	super(name);
}

public void testErrorTest() {
	ThreadedTest test = new ThreadedTest(new TestCase("succeeds") {
		public void runTest() throws Exception {
			throw new RuntimeException("jau!");
		}
	});
	TestResult result = new TestResult();
	test.run(result);
	assertTrue(test.hasFinished());
	assertEquals(0, result.failureCount());
	assertEquals(1, result.errorCount());
}

public void testFailureTest() {
	ThreadedTest test = new ThreadedTest(new TestCase("succeeds") {
		public void runTest() {
			assertEquals("ja","nein");
		}
	});
	TestResult result = new TestResult();
	test.run(result);
	assertTrue(test.hasFinished());
	assertEquals(1, result.failureCount());
	assertEquals(0, result.errorCount());
}

public void testSucceedingTest() {
	ThreadedTest test = new ThreadedTest(new TestCase("succeeds") {
		public void runTest() {
			assertEquals("ja","ja");
		}
	});
	TestResult result = new TestResult();
	test.run(result);
	assertTrue(test.hasFinished());
	assertEquals(0, result.failureCount());
	assertEquals(0, result.errorCount());
}
}