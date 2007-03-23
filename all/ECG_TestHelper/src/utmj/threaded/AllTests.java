package utmj.threaded;

import junit.framework.*;

public class AllTests {
public static void main(String[] args) {
	junit.textui.TestRunner.run(suite());
}
public static Test suite() {
	TestSuite suite = new TestSuite("utmj.threaded");
	suite.addTestSuite(ThreadedTestTest.class);
	suite.addTestSuite(MultiThreadedTestTest.class);
	suite.addTestSuite(ConcurrentTestCaseTest.class);
	suite.addTestSuite(RetriedAssertTest.class);
	suite.addTestSuite(ExceptionAssertTest.class);
	return suite;
}
}