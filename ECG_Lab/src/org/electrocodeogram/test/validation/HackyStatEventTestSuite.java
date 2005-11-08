package org.electrocodeogram.test.validation;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * A suite of testcases from {@link org.electrocodeogram.test.validation.HackyStatEventTests}.
 * Each test is called {@link #DEFAULT_TEST_COUNT} times.
 * In every single call another linenumber parameter is passed to the tests, so
 * that the {@link org.electrocodeogram.test.EventGenerator} is using different
 * lines from <em>pseudorandom.strings</em> as payload.
 */
public final class HackyStatEventTestSuite {

    /**
     * This is the name for this testsuite.
     */
    private static final String SUITE_NAME = "HackyStatEventTests";

    /**
     * This is the default testcount. It is equal to the number
     * of lines in <em>pseudorandom.strings</em>.
     */
    private static final int DEFAULT_TEST_COUNT = 100;

    /**
     * The constructor is hidden in this utility class.
     *
     */
    private HackyStatEventTestSuite() {
    // empty constructor
    }

    /**
     * This method returns the suite of tests from {@link HackyStatEventTests}.
     * @return The testsuite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(SUITE_NAME);

        int testHCount = DEFAULT_TEST_COUNT;

        for (int i = 0; i < testHCount; i++) {
            suite.addTest(new HackyStatEventTests(
                "testUnknownCommandNameIsNotAccepted", i));
        }

        int testICount = DEFAULT_TEST_COUNT;

        for (int i = 0; i < testICount; i++) {
            suite.addTest(new HackyStatEventTests(
                "testHackyStatActivityEventsAccepted", i));
        }

        int testJCount = DEFAULT_TEST_COUNT;

        for (int i = 0; i < testJCount; i++) {
            suite.addTest(new HackyStatEventTests(
                "testHackyStatBuildEventsAccepted", i));
        }

        int testKCount = DEFAULT_TEST_COUNT;

        for (int i = 0; i < testKCount; i++) {
            suite.addTest(new HackyStatEventTests(
                "testHackyStatBuffTransEventsAccepted", i));
        }

        int testLCount = DEFAULT_TEST_COUNT;

        for (int i = 0; i < testLCount; i++) {
            suite.addTest(new HackyStatEventTests(
                "testHackyStatCommitEventsAccepted", i));
        }

        int testMCount = DEFAULT_TEST_COUNT;

        for (int i = 0; i < testMCount; i++) {
            suite.addTest(new HackyStatEventTests(
                "testHackyStatFileMetricEventsAccepted", i));
        }

        int testNCount = DEFAULT_TEST_COUNT;

        for (int i = 0; i < testNCount; i++) {
            suite.addTest(new HackyStatEventTests(
                "testHackyStatUnitTestEventsAccepted", i));
        }

        return suite;
    }

}
