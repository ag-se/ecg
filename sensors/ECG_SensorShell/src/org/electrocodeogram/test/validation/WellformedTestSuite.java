/*
 * Class: WellformedTestSuite
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.test.validation;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This suite runs the {@link org.electrocodeogram.test.validation.WellformedEventTests}
 * multiple times and varries the payload for each event.
 */
public final class WellformedTestSuite {

    /**
     * The number of times to call each test.
     */
    private static final int TEST_COUNT = 100;

    /**
     * The name of this test suite.
     */
    private static final String TEST_SUIT_NAME = "WellformedEventTests";

    /**
     * This is hidden for a utility class.
     *
     */
    private WellformedTestSuite() {
        // not implemented
    }

    /**
     * This method returns the current testsuite.
     * @return The testsuite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(TEST_SUIT_NAME);

        int testACount = TEST_COUNT;

        for (int i = 0; i < testACount; i++) {
            suite.addTest(new WellformedEventTests("testValidEventIsAccepted",
                i));
        }

        int testCCount = TEST_COUNT;

        for (int i = 0; i < testCCount; i++) {
            suite.addTest(new WellformedEventTests(
                "testInvalidEventIsNotAcceptedForTimeStampIsNull", i));
        }

        int testDCount = TEST_COUNT;

        for (int i = 0; i < testDCount; i++) {
            suite.addTest(new WellformedEventTests(
                "testInvalidEventIsNotAcceptedForCommandNameIsNull", i));
        }

        int testECount = TEST_COUNT;

        for (int i = 0; i < testECount; i++) {
            suite.addTest(new WellformedEventTests(
                "testInvalidEventIsNotAcceptedForArgListIsNull", i));
        }

        int testFCount = TEST_COUNT;

        for (int i = 0; i < testFCount; i++) {
            suite.addTest(new WellformedEventTests(
                "testInvalidEventIsNotAcceptedForArgListIsEmpty", i));
        }

        int testGCount = TEST_COUNT;

        for (int i = 0; i < testGCount; i++) {
            suite.addTest(new WellformedEventTests(
                "testInvalidEventIsNotAcceptedForArgListIsNoStringList", i));
        }

        int testICount = TEST_COUNT;

        for (int i = 0; i < testICount; i++) {
            suite.addTest(new WellformedEventTests(
                "testHackyStatActivityEventIsAccepted", i));
        }

        int testJCount = TEST_COUNT;

        for (int i = 0; i < testJCount; i++) {
            suite.addTest(new WellformedEventTests(
                "testHackyStatBuildEventsAccepted", i));
        }

        int testKCount = TEST_COUNT;

        for (int i = 0; i < testKCount; i++) {
            suite.addTest(new WellformedEventTests(
                "testHackyStatBuffTransEventsAccepted", i));
        }

        int testLCount = TEST_COUNT;

        for (int i = 0; i < testLCount; i++) {
            suite.addTest(new WellformedEventTests(
                "testHackyStatCommitEventsAccepted", i));
        }

        int testMCount = TEST_COUNT;

        for (int i = 0; i < testMCount; i++) {
            suite.addTest(new WellformedEventTests(
                "testHackyStatFileMetricEventsAccepted", i));
        }

        int testNCount = TEST_COUNT;

        for (int i = 0; i < testNCount; i++) {
            suite.addTest(new WellformedEventTests(
                "testHackyStatUnitTestEventsAccepted", i));
        }

        return suite;
    }

}
