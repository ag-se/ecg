package org.electrocodeogram.sensor.eclipse.listener;

import org.eclipse.jdt.junit.ITestRunListener;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.sensor.eclipse.ECGEclipseSensor;

/**
 * This is listening for events about tests and testruns.
 */
public class ECGTestListener implements ITestRunListener {

    /**
     * 
     */
    private final ECGEclipseSensor sensor;

    /**
     * @param sensor
     */
    public ECGTestListener(ECGEclipseSensor sensor) {
        this.sensor = sensor;
    }

    /**
     * Stores the number of individual tests in the current
     * testrun.
     */
    private int currentTestCount;

    /**
     * @see org.eclipse.jdt.junit.ITestRunListener#testRunStarted(int)
     */
    public void testRunStarted(final int testCount) {

        ECGEclipseSensor.logger.entering(this.getClass().getName(), "testRunStarted",
            new Object[] {new Integer(testCount)});

        this.currentTestCount = testCount;

        ECGEclipseSensor.logger.log(ECGLevel.PACKET,
            "An testRunStarted event has been recorded.");

        this.sensor.processActivity(
            "msdt.testrun.xsd",
            "<?xml version=\"1.0\"?><microActivity><commonData><version>1</version><creator>" + ECGEclipseSensor.CREATOR + "</creator><username>"
                + this.sensor.username
                + "</username><id>"
                + this.hashCode()
                + "</id></commonData><testrun><activity>started</activity><elapsedtime>0</elapsedtime><testcount>"
                + testCount
                + "</testcount></testrun></microActivity>");

        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "testRunStarted");

    }

    /**
     * @see org.eclipse.jdt.junit.ITestRunListener#testRunEnded(long)
     */
    public void testRunEnded(final long elapsedTime) {
        ECGEclipseSensor.logger.entering(this.getClass().getName(), "testRunEnded",
            new Object[] {new Long(elapsedTime)});

        ECGEclipseSensor.logger.log(ECGLevel.PACKET,
            "An testRunEnded event has been recorded.");

        this.sensor.processActivity(
            "msdt.testrun.xsd",
            "<?xml version=\"1.0\"?><microActivity><commonData><version>1</version><creator>" + ECGEclipseSensor.CREATOR + "</creator><username>"
                + this.sensor.username
                + "</username><id>"
                + this.hashCode()
                + "</id></commonData><testrun><activity>ended</activity><elapsedtime>"
                + elapsedTime + "</elapsedtime><testcount>"
                + this.currentTestCount
                + "</testcount></testrun></microActivity>");

        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "testRunEnded");

    }

    /**
     * @see org.eclipse.jdt.junit.ITestRunListener#testRunStopped(long)
     */
    public void testRunStopped(final long elapsedTime) {
        ECGEclipseSensor.logger.entering(this.getClass().getName(), "testRunStopped",
            new Object[] {new Long(elapsedTime)});

        ECGEclipseSensor.logger.log(ECGLevel.PACKET,
            "An testRunStopped event has been recorded.");

        this.sensor.processActivity(
            "msdt.testrun.xsd",
            "<?xml version=\"1.0\"?><microActivity><commonData><version>1</version><creator>" + ECGEclipseSensor.CREATOR + "</creator><username>"
                + this.sensor.username
                + "</username><projectname>"
                + this.sensor.projectname
                + "</projectname></commonData><testrun><activity>stopped</activity><elapsedtime>"
                + elapsedTime + "</elapsedtime><testcount>"
                + this.currentTestCount
                + "</testcount></testrun></microActivity>");

        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "testRunStopped");

    }

    /**
     * @see org.eclipse.jdt.junit.ITestRunListener#testRunTerminated()
     */
    public void testRunTerminated() {
        ECGEclipseSensor.logger.entering(this.getClass().getName(), "testRunTerminated");

        ECGEclipseSensor.logger.log(ECGLevel.PACKET,
            "An testRunTerminated event has been recorded.");

        this.sensor.processActivity(
            "msdt.testrun.xsd",
            "<?xml version=\"1.0\"?><microActivity><commonData><version>1</version><creator>" + ECGEclipseSensor.CREATOR + "</creator><username>"
                + this.sensor.username
                + "</username><id>"
                + this.hashCode()
                + "</id></commonData><testrun><activity>terminated</activity><elapsedtime>0</elapsedtime><testcount>"
                + this.currentTestCount
                + "</testcount></testrun></microActivity>");

        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "testRunTerminated");

    }

    /**
     * @see org.eclipse.jdt.junit.ITestRunListener#testStarted(java.lang.String,
     *      java.lang.String)
     */
    public void testStarted(final String testId, final String testName) {
        ECGEclipseSensor.logger.entering(this.getClass().getName(), "testStarted",
            new Object[] {testId, testName});

        ECGEclipseSensor.logger.log(ECGLevel.PACKET,
            "An testStarted event has been recorded.");

        this.sensor.processActivity(
            "msdt.test.xsd",
            "<?xml version=\"1.0\"?><microActivity><commonData><version>1</version><creator>" + ECGEclipseSensor.CREATOR + "</creator><username>"
                + this.sensor.username
                + "</username><id>"
                + testId
                + "</id></commonData><test><activity>started</activity><name>"
                + testName
                + "</name><id>"
                + testId
                + "</id><status>ok</status></test></microActivity>");

        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "testStarted");

    }

    /**
     * @see org.eclipse.jdt.junit.ITestRunListener#testEnded(java.lang.String,
     *      java.lang.String)
     */
    public void testEnded(final String testId, final String testName) {
        ECGEclipseSensor.logger.entering(this.getClass().getName(), "testEnded",
            new Object[] {testId, testName});

        ECGEclipseSensor.logger
            .log(ECGLevel.PACKET, "An testEnded event has been recorded.");

        this.sensor.processActivity(
            "msdt.test.xsd",
            "<?xml version=\"1.0\"?><microActivity><commonData><version>1</version><creator>" + ECGEclipseSensor.CREATOR + "</creator><username>"
                + this.sensor.username
                + "</username><id>"
                + testId
                + "</id></commonData><test><activity>ended</activity><name>"
                + testName
                + "</name><id>"
                + testId
                + "</id><status>ok</status></test></microActivity>");

        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "testEnded");

    }

    /**
     * @see org.eclipse.jdt.junit.ITestRunListener#testFailed(int,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    public void testFailed(final int status, final String testId,
        final String testName, final String trace) {
        ECGEclipseSensor.logger.entering(this.getClass().getName(), "testFailed",
            new Object[] {testId, testName, new Integer(status), trace});

        String statusString;

        switch (status) {
            case ITestRunListener.STATUS_OK:

                statusString = "ok";

                break;

            case ITestRunListener.STATUS_ERROR:

                statusString = "error";

                break;

            case ITestRunListener.STATUS_FAILURE:

                statusString = "failure";

                break;

            default:

                statusString = "";

                break;
        }

        ECGEclipseSensor.logger.log(ECGLevel.PACKET,
            "An testFailed event has been recorded.");

        this.sensor.processActivity(
            "msdt.test.xsd",
            "<?xml version=\"1.0\"?><microActivity><commonData><version>1</version><creator>" + ECGEclipseSensor.CREATOR + "</creator><username>"
                + this.sensor.username
                + "</username><id>"
                + testId
                + "</id></commonData><test><activity>failed</activity><name>"
                + testName + "</name><id>" + testId
                + "</id><status>" + statusString
                + "</status></test></microActivity>");

        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "testFailed");

    }

    /**
     * @see org.eclipse.jdt.junit.ITestRunListener#testReran(java.lang.String,
     *      java.lang.String, java.lang.String, int,
     *      java.lang.String)
     */
    public void testReran(final String testId, 
    final String testClass, final String testName, final int status,
        final String trace) {
        ECGEclipseSensor.logger.entering(this.getClass().getName(), "testReran",
            new Object[] {testId, testName, new Integer(status), trace});

        String statusString;

        switch (status) {
            case ITestRunListener.STATUS_OK:

                statusString = "ok";

                break;

            case ITestRunListener.STATUS_ERROR:

                statusString = "error";

                break;

            case ITestRunListener.STATUS_FAILURE:

                statusString = "failure";

                break;

            default:

                statusString = "";

                break;
        }

        ECGEclipseSensor.logger
            .log(ECGLevel.PACKET, "An testReran event has been recorded.");

        this.sensor.processActivity(
            "msdt.test.xsd",
            "<?xml version=\"1.0\"?><microActivity><commonData><version>1</version><creator>" + ECGEclipseSensor.CREATOR + "</creator><username>"
                + this.sensor.username
                + "</username><id>"
                + testId
                + "</id></commonData><test><activity>reran</activity><name>"
                + testName + "</name><id>" + testId
                + "</id><status>" + statusString
                + "</status></test></microActivity>");

        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "testReran");

    }

}