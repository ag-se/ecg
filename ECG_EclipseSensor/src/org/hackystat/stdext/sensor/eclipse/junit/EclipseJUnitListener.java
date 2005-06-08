package org.hackystat.stdext.sensor.eclipse.junit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jdt.junit.ITestRunListener;
import org.hackystat.stdext.sensor.eclipse.EclipseSensor;


/**
 * Provides a JUnit listener implementing class to get JUnit result information. 
 * A client must add this implementing class to the JUnitPlugin class in such a way that:
 * 
 * <pre>
 * ITestRunListener eclipseListener = new EclipseJUnitListener(EclipseSensor.getInstance());
 * JUnitPlugin.getDefault().addTestRunListener(eclipseListener);
 * </pre>
 * 
 *
 * @author Takuya Yamashita
 * @version $Id: EclipseJUnitListener.java,v 1.3 2004/01/27 02:02:37 takuyay Exp $
 */
public class EclipseJUnitListener implements ITestRunListener {
  /** The EclipseSensor to be used to invoke processUnitTest method */
  private EclipseSensor sensor;
  /** A starting time of each test case. */
  private Date startTime;

  /** A ending time of each test case */
  private Date endTime;

  /**
   * A test case name (method name) of a test class name.
   */
  private String testCaseName;

  /**
   * A test class name.
   */
  private String testClassName;

  /**
   * A elapsed time of the invocation of the test case.
   */
  private long elapsedTime;

  /**
   * A failure stack trace message.
   */
  private String failureMessage;

  /**
   * A error stack trace message.
   */
  private String errorMessage;

  /**
   * Constructor for the EclipseJUnitListener object. Adds this lister instance to the
   * <code>org.eclipse.jdt.internal.junit.ui.JUnitPlugin</code> instance.
   *
   * @param sensor The EclipseSensor instance.
   */
  public EclipseJUnitListener(EclipseSensor sensor) {
    this.sensor = sensor;
    init();
  }

  /**
   * Initializes all the instance fields related to the fields in the JUnitResource instance.
   */
  private void init() {
    this.testCaseName = "";
    this.testCaseName = "";
    this.elapsedTime = 0;
    this.failureMessage = "";
    this.errorMessage = "";
  }

  /**
   * A test failed.
   *
   * @param status the status of the test.
   * @param testName the name of the test that has ended.
   * @param trace the stack trace in the case of a failure.
   *
   */
  public void testFailed(int status, String testName, String trace) {
    if (status == ITestRunListener.STATUS_FAILURE) {
      this.failureMessage = trace;
    }
    else if (status == ITestRunListener.STATUS_ERROR) {
      this.errorMessage = trace;
    }
  }

  /**
   * A single test was reran.
   *
   * @param testClass the name of the test class.
   * @param testName the name of the test.
   * @param status the status of the run
   * @param trace the stack trace in the case of a failure.
   *
   */
  public void testReran(String testClass, String testName, int status, String trace) {
  }

  /**
   * A test started.
   *
   * @param testName the name of the test that has ended
   *
   */
  public void testStarted(String testName) {
    this.startTime = new Date();
    init();
  }

  /**
   * A test ended.
   *
   * @param testName the name of the test that has ended
   *
   */
  public void testEnded(String testName) {
    this.endTime = new Date();

    String testCaseName = getTestCaseNameOf(testName);
    String testClassName = getTestClassNameOf(testName);
    long elapsedTime = this.endTime.getTime() - this.startTime.getTime();

    elapsedTime = (elapsedTime >= 0) ? elapsedTime : 0;

    // Create list and store the JUnit information into it.
    List argList = new ArrayList();
    argList.add("add");
    argList.add(testClassName);
    argList.add(testCaseName);
    argList.add(String.valueOf(elapsedTime));
    if (this.failureMessage.length() > 0) {
      argList.add(this.failureMessage);
    }
    else if (this.errorMessage.length() > 0) {
      argList.add(" ");
      argList.add(this.errorMessage);
    }

    // Process UnitTest
    this.sensor.processUnitTest(argList);
  }

  /**
   * A test run ended.
   *
   * @param elapsedTime the elapsed time of the test run.
   *
   * @see org.eclipse.jdt.junit.ITestRunListener#testRunEnded(long)
   */
  public void testRunEnded(long elapsedTime) {
  }

  /**
   * A test run has started.
   *
   * @param testCount the number of tests that will be run.
   *
   * @see org.eclipse.jdt.junit.ITestRunListener#testRunStarted(int)
   */
  public void testRunStarted(int testCount) {
  }

  /**
   * A test run was stopped before it ended.
   *
   * @param elapsedTime the elapsed time of the test run.
   *
   * @see org.eclipse.jdt.junit.ITestRunListener#testRunStopped(long)
   */
  public void testRunStopped(long elapsedTime) {
  }

  /**
   * The test runner VM has terminated.
   *
   * @see org.eclipse.jdt.junit.ITestRunListener#testRunTerminated()
   */
  public void testRunTerminated() {
  }

  /**
   * Gets the fully qualified package class name from the testName string. For example, returns
   * "org.hackystat.stdext.sensor.eclipse.junit.EclipseJUnitListener".
   *
   * @param testName The test name string to be passed.
   *
   * @return The fully qualified package class name.
   */
  private String getTestClassNameOf(String testName) {
    int startIndex = testName.indexOf('(');
    int endIndex = testName.indexOf(')');

    return testName.substring(startIndex + 1, endIndex);
  }

  /**
   * Gets the test case name (method name) from the testName string. For example, returns
   * "testMethod".
   *
   * @param testName The test name string to be passed.
   *
   * @return The test case name (method name).
   */
  private String getTestCaseNameOf(String testName) {
    int endIndex = testName.indexOf('(');

    return testName.substring(0, endIndex);
  }

  /**
   * An individual test has started. Supports the method for the 2.1 stream from RC2.
   *
   * @param testId a unique Id identifying the test
   * @param testName the name of the test that started
   *
   * @see org.eclipse.jdt.junit.ITestRunListener#testStarted(java.lang.String, java.lang.String)
   * @since 2.1 RC2
   **/
  public void testStarted(String testId, String testName) {
    testStarted(testName);
  }

  /**
   * An individual test has ended. Supports the method for the 2.1 stream from RC2.
   *
   * @param testId a unique Id identifying the test
   * @param testName the name of the test that ended
   *
   * @see org.eclipse.jdt.junit.ITestRunListener#testEnded(java.lang.String, java.lang.String)
   * @since 2.1 RC2
   **/
  public void testEnded(String testId, String testName) {
    testEnded(testName);
  }

  /**
   * An individual test has failed with a stack trace.
   * Supports the method for the 2.1 stream from RC2.
   *
   * @param testId a unique Id identifying the test
   * @param testName the name of the test that failed
   * @param status the outcome of the test; one of
   * {@link #STATUS_ERROR STATUS_ERROR} or
   * {@link #STATUS_FAILURE STATUS_FAILURE}
   * @param trace the stack trace
   *
   * @see org.eclipse.jdt.junit.ITestRunListener#testFailed(int, java.lang.String,
   * java.lang.String, java.lang.String)
   * @since 2.1 RC2
   */
  public void testFailed(int status, String testId, String testName, String trace) {
    testFailed(status, testName, trace);
  }

  /**
   * An individual test has been rerun.
   * Supports the method for the 2.1 stream from RC2.
   *
   * @param testId a unique Id identifying the test
   * @param testClass the name of the test class that was rerun
   * @param testName the name of the test that was rerun
   * @param status the outcome of the test that was rerun; one of
   * {@link #STATUS_OK STATUS_OK}, {@link #STATUS_ERROR STATUS_ERROR},
   * or {@link #STATUS_FAILURE STATUS_FAILURE}
   * @param trace the stack trace in the case of abnormal termination,
   * or the empty string if none
   *
   * @see org.eclipse.jdt.junit.ITestRunListener#testReran(java.lang.String, java.lang.String,
   *  java.lang.String, int, java.lang.String)
   * @since 2.1 RC2
   */
  public void testReran(String testId, String testClass,
                        String testName, int status, String trace) {
    testReran(testClass, testName, status, trace);
  }

}
