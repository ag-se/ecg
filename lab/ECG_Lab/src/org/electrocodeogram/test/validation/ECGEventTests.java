/*
 * Classname: ECGEventTests
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.test.validation;

import java.io.IOException;

import junit.framework.TestCase;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.event.ValidEventPacket.VALIDATION_LEVEL;
import org.electrocodeogram.test.EventGenerator;
import org.electrocodeogram.test.EventGenerator.MicroSensorDataType;
import org.electrocodeogram.test.module.TestModule;

/**
 * Collects the testcases that are testing the use of
 * <em>MicroActivityEvent</em> for the ECG. The
 * {@link org.electrocodeogram.event.ValidEventPacket.VALIDATION_LEVEL}
 * is set to
 * {@link org.electrocodeogram.event.ValidEventPacket.VALIDATION_LEVEL#ECG}.
 * Every event that is not a valid <em>MicroActivityEvent</em> must
 * not be allowed and every event that is a valid
 * <em>MicroActivityEvent</em> has to be allowed by the ECG. Every
 * <em>MicroSensorDataType</em> is tested with multiple testcases.
 * At least one for every element value, which is expected to pass and
 * one value, which is expected to fail.
 */
public class ECGEventTests extends TestCase {

    /**
     * A reference to the object that is genertaing events for testing
     * purpoeses.
     */
    private EventGenerator eventGenerator;

    /**
     * This {@link org.electrocodeogram.module.source.SourceModule}
     * must be created to register the default
     * <em>MicroSensorDataType</em> for the event validation.
     */
    private TestModule testModule;

    /**
     * @throws IOException
     * @see junit.framework.TestCase#setUp() Creates the
     *      {@link #testModule} and the {@link #eventGenerator}.
     */
    @Override
    protected final void setUp() throws IOException {

        if (this.testModule == null) {
            this.testModule = new TestModule();

            this.testModule.registerMSDTs();
        }

        this.eventGenerator = new EventGenerator();

    }

    /**
     * @see junit.framework.TestCase#tearDown() Releses the
     *      {@link #eventGenerator}.
     */
    @Override
    protected final void tearDown() {

        this.eventGenerator = null;

    }

    /**
     * Testcase SE13 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>resource (added) MicroActivityEvent</em>. The expected
     * result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidResourceAddedMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.RESOURCEADDED);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE14 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>resource (removed) MicroActivityEvent</em>. The expected
     * result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidResourceRemovedMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.RESOURCEREMOVED);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE15 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>resource (changed) MicroActivityEvent</em>. The expected
     * result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidResourceChangedMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.RESOURCECHANGED);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE16 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate an invalid
     * <em>resource MicroActivityEvent</em>. It is invalid because
     * the &lt;activity&gt; element has an unexpected value. The
     * expected result is that the test is throwing an
     * {@link IllegalEventParameterException}.
     */
    public final void testInvalidResourceMicroActivityIsNotAcceptedForUnknownActivity() {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.RESOURCEACTIVITYUNKNOWN);

        try {
            validate(packet);

            assertTrue(false);
        } catch (IllegalEventParameterException e) {
            assertTrue(true);
        }

    }

    /**
     * Testcase SE17 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>codechange MicroActivityEvent</em>. The expected result
     * is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidCodechangeMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.CODECHANGE);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE18 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>editor (closed) MicroActivityEvent</em>. The expected
     * result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidEditorClosedMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.EDITORCLOSED);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE19 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>editor (activated) MicroActivityEvent</em>. The expected
     * result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidEditorActivatedMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.EDITORACTIVATED);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE20 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>editor (deactivated) MicroActivityEvent</em>. The
     * expected result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidEditorDeactivatedMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.EDITORDEACTIVATED);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE21 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>editor (opened) MicroActivityEvent</em>. The expected
     * result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidEditorOpenedMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.EDITOROPENED);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE22 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate an invalid
     * <em>editor MicroActivityEvent</em>. It is invalid because
     * the &lt;activity&gt; element has an unexpected value. The
     * expected result is that the test is throwing an
     * {@link IllegalEventParameterException}.
     */
    public final void testInvalidEditorMicroActivityIsNotAcceptedForUnknownActivity() {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.EDITORACTIVITYUNKNOWN);

        try {
            validate(packet);

            assertTrue(false);
        } catch (IllegalEventParameterException e) {
            assertTrue(true);
        }

    }

    /**
     * Testcase SE23 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>part (closed) MicroActivityEvent</em>. The expected
     * result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidPartClosedMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.PARTCLOSED);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE24 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>part (activated) MicroActivityEvent</em>. The expected
     * result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidPartActivatedMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.PARTACTIVATED);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE25 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>part (deactivated) MicroActivityEvent</em>. The expected
     * result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidPartDeactivatedMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.PARTDEACTIVATED);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE26 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>part (deactivated) MicroActivityEvent</em>. The expected
     * result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidPartOpenedMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.PARTOPENED);

        validate(packet);

        assertTrue(true);
    }

    /**
     * Testcase SE27 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate an invalid
     * <em>part MicroActivityEvent</em>. It is invalid because the
     * &lt;activity&gt; element has an unexpected value. The expected
     * result is that the test is throwing an
     * {@link IllegalEventParameterException}.
     */
    public final void testInvalidPartMicroActivityIsNotAcceptedForUnknownActivity() {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.PARTACTIVITYUNKNOWN);

        try {
            validate(packet);

            assertTrue(false);
        } catch (IllegalEventParameterException e) {
            assertTrue(true);
        }

    }

    /**
     * Testcase SE28 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>rundebug (run) MicroActivityEvent</em>. The expected
     * result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidRunDebugMicroActivtyIsAcceptedWithoutDebug()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.RUNDEBUGNODEBUG);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE29 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>rundebug (debug) MicroActivityEvent</em>. The expected
     * result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidRunDebugMicroActivtyIsAcceptedWithDebug()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.RUNDEBUGWITHDEBUG);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE30 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate an invalid
     * <em>rundebug MicroActivityEvent</em>. It is invalid because
     * the attribute "debug" is neither "true" nor "false". The
     * expected result is that the test is throwing an
     * {@link IllegalEventParameterException}.
     */
    public final void testInvalidRunDebugMicroActivityIsNotAcceptedWithIllegalDebug() {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.RUNDEBUGWITHILLEGALDEBUG);

        try {
            validate(packet);

            assertTrue(false);
        } catch (IllegalEventParameterException e) {
            assertTrue(true);
        }

    }

    /**
     * Testcase SE31 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>window (closed) MicroActivityEvent</em>. The expected
     * result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidWindowClosedMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.WINDOWCLOSED);

        validate(packet);

        assertTrue(true);
    }

    /**
     * Testcase SE32 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>window (closed) MicroActivityEvent</em>. The expected
     * result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidWindowActivatedMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.WINDOWACTIVATED);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE33 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>window (deactivated) MicroActivityEvent</em>. The
     * expected result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidWindowDeactivatedMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.WINDOWDEACTIVATED);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE34 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>window (opened) MicroActivityEvent</em>. The expected
     * result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidWindowOpenedMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.WINDOWOPENED);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE35 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate an invalid
     * <em>window MicroActivityEvent</em>. It is invalid because
     * the &lt;activity&gt; element has an unexpected value. The
     * expected result is that the test is throwing an
     * {@link IllegalEventParameterException}.
     */
    public final void testInvalidWindowMicroActivtyIsNotAcceptedForUnknownActivity() {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.WINDOWACTIVITYUNKNOWN);

        try {
            validate(packet);

            assertTrue(false);
        } catch (IllegalEventParameterException e) {
            assertTrue(true);
        }

    }

    /**
     * Testcase SE36 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>testrun (started) MicroActivityEvent</em>. The expected
     * result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidTestrunStartedMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.TESTRUNSTARTED);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE37 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>testrun (ended) MicroActivityEvent</em>. The expected
     * result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidTestrunEndedMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.TESTRUNENDED);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE38 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>testrun (stopped) MicroActivityEvent</em>. The expected
     * result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidTestrunStoppedMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.TESTRUNSTOPPED);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE39 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>testrun (terminated) MicroActivityEvent</em>. The
     * expected result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidTestrunTerminatedMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.TESTRUNTERMINATED);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE40 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>test (started) MicroActivityEvent</em>. The
     * expected result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidTestStartedMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.TESTSTARTED);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE41 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>test (ended) MicroActivityEvent</em>. The
     * expected result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidTestEndedMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.TESTENDED);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE42 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>test (failed) MicroActivityEvent</em>. The
     * expected result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidTestFailedMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.TESTFAILED);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE43 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate a
     * <em>test (reran) MicroActivityEvent</em>. The
     * expected result is that the test is not throwing an exception.
     * @throws IllegalEventParameterException
     *             If the event is not recognized as a valid
     *             <em>HackyStat</em> event by the ECG
     */
    public final void testValidTestReranMicroActivtyIsAccepted()
        throws IllegalEventParameterException {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.TESTRERAN);

        validate(packet);

        assertTrue(true);

    }

    /**
     * Testcase SE44 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate an invalid
     * <em>testrun MicroActivityEvent</em>. It is invalid because
     * the &lt;activity&gt; element has an unexpected value. The
     * expected result is that the test is throwing an
     * {@link IllegalEventParameterException}.
     */
    public final void testInvalidTestrunMicroActivityIsNotAcceptedForUnknownActivity() {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.TESTRUNACTIVITYUNKNOWN);

        try {
            validate(packet);

            assertTrue(false);
        } catch (IllegalEventParameterException e) {
            assertTrue(true);
        }

    }

    /**
     * Testcase SE45 according to the document TESTPLAN Version 2.0 or
     * higher. This testcase is trying to validate an invalid
     * <em>test MicroActivityEvent</em>. It is invalid because
     * the &lt;activity&gt; element has an unexpected value. The
     * expected result is that the test is throwing an
     * {@link IllegalEventParameterException}.
     */
    public final void testInvalidTestMicroActivityIsNotAcceptedForUnknownActivity() {
        WellFormedEventPacket packet = this.eventGenerator
            .createECGEventPacket(MicroSensorDataType.TESTACTIVITYUNKNOWN);

        try {
            validate(packet);

            assertTrue(false);
        } catch (IllegalEventParameterException e) {
            assertTrue(true);
        }

    }

    /**
     * This method is trying to create a {@link ValidEventPacket} from
     * the data of a given {@link WellFormedEventPacket}.
     * @param event
     *            The event containing the testdata
     * @throws IllegalEventParameterException
     *             If the event is not valid according to
     *             {@link org.electrocodeogram.event.ValidEventPacket.VALIDATION_LEVEL#ECG}
     */
    private void validate(final WellFormedEventPacket event)
        throws IllegalEventParameterException {

        ValidEventPacket.setValidityLevel(VALIDATION_LEVEL.ECG);

        new ValidEventPacket(event.getSourceId(), event.getTimeStamp(), event
            .getSensorDataType(), event.getArgList());
    }

}
