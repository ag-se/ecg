/*
 * Class: WellformedEventTests
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.test.validation;

import junit.framework.TestCase;

import org.electrocodeogram.event.EventPacket;
import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.test.EventGenerator;
import org.electrocodeogram.test.NoTestDataException;
import org.electrocodeogram.test.EventGenerator.SensorDataType;

/**
 * This class collects all testcases for testing the client side of
 * the ECG framework for correct EventPacket object transportation.
 */
public class WellformedEventTests extends TestCase {

    /**
     * The size of the {@link EventPacket#myArgList} entries.
     */
    private static final int ENTRY_SIZE = 10;

    /**
     * The length of the {@link EventPacket#myArgList}.
     */
    private static final int LIST_LENGTH = 10;

    /**
     * A reference to the object tat is simulating th sensor.
     */
    private static MockSensor sensor = new MockSensor();

    /**
     * A reference to the object that is generating the events.
     */
    private EventGenerator eventGenerator = null;

    /**
     * The number is passed to the {@link #eventGenerator}. There it
     * is used to determine the line in the
     * <em>"pseudorandom.strings"</em> textfile. This line is the
     * payload for the event.
     */
    private int linenumber = -1;

    // int _bufferSizeBefore;

    // WellFormedEventPacket _eventPacket;

    /**
     * Creates a testcase with the given name and payload linenumber.
     * @param name
     *            The name of the testcase to create
     * @param line
     *            Is the line in the <em>"pseudorandom.strings"</em>
     *            textfile, which is used as the payload for the event
     */
    public WellformedEventTests(final String name, final int line) {
        super(name);

        this.linenumber = line;
    }

    /**
     * @see junit.framework.TestCase#setUp() Creates the
     *      {@link #eventGenerator}.
     */
    @Override
    protected final void setUp() throws Exception {
        super.setUp();

        this.eventGenerator = new EventGenerator();
    }

    /**
     * @see junit.framework.TestCase#tearDown() Releases the
     *      {@link #eventGenerator}.
     */
    @Override
    protected final void tearDown() throws Exception {
        super.tearDown();

        this.eventGenerator = null;
    }

    /**
     * Testcase CL1 according to the document TESTPLAN Version 1.0 or
     * higher. Pass a wellformed event to the <em>ECG SensorShell</em>.
     * The expected result is that the <em>SensorShell</em> accepts
     * it.
     * @throws IllegalEventParameterException
     *             If the {@link #eventGenerator} is throwing it.
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line
     *             number that is not available or if the requested
     *             String size is to higher then available
     */
    public final void testValidEventIsAccepted()
        throws IllegalEventParameterException, NoTestDataException {
        WellFormedEventPacket eventPacket;

        eventPacket = this.eventGenerator.createWellformedEventPacket(true,
            true, this.linenumber, true, true, LIST_LENGTH, ENTRY_SIZE);

        boolean result = sensor.sendEvent(eventPacket);

        assertTrue("" + this.linenumber, result);

    }

    /**
     * Testcase CL2 according to the document TESTPLAN Version 1.0 or
     * higher. Pass a malformed event to the <em>ECG SensorShell</em>.
     * The <em>timestamp</em> is <code>null</code>. The expected
     * result is that the <em>SensorShell</em> revokes it.
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line
     *             number that is not available or if the requested
     *             String size is to higher then available
     */
    public final void testInvalidEventIsNotAcceptedForTimeStampIsNull()
        throws NoTestDataException {
        EventPacket eventPacket = this.eventGenerator.createEventPacket(false,
            true, this.linenumber, true, true, LIST_LENGTH, ENTRY_SIZE);

        boolean result = sensor.sendEvent(eventPacket);

        assertFalse("" + this.linenumber, result);
    }

    /**
     * Testcase CL3 according to the document TESTPLAN Version 1.0 or
     * higher. Pass a malformed event to the <em>ECG SensorShell</em>.
     * The <em>commandName</em> is <code>null</code>. The
     * expected result is that the <em>SensorShell</em> revokes it.
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line
     *             number that is not available or if the requested
     *             String size is to higher then available
     */
    public final void testInvalidEventIsNotAcceptedForCommandNameIsNull()
        throws NoTestDataException {
        EventPacket eventPacket = this.eventGenerator.createEventPacket(true,
            false, this.linenumber, true, true, LIST_LENGTH, ENTRY_SIZE);

        boolean result = sensor.sendEvent(eventPacket);

        assertFalse("" + this.linenumber, result);
    }

    /**
     * Testcase CL4 according to the document TESTPLAN Version 1.0 or
     * higher. Pass a malformed event to the <em>ECG SensorShell</em>.
     * The <em>argList</em> is <code>null</code>. The expected
     * result is that the <em>SensorShell</em> revokes it.
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line
     *             number that is not available or if the requested
     *             String size is to higher then available
     */
    public final void testInvalidEventIsNotAcceptedForArgListIsNull()
        throws NoTestDataException {
        EventPacket eventPacket = this.eventGenerator.createEventPacket(true,
            true, this.linenumber, false, true, LIST_LENGTH, ENTRY_SIZE);

        boolean result = sensor.sendEvent(eventPacket);

        assertFalse("" + this.linenumber, result);
    }

    /**
     * Testcase CL5 according to the document TESTPLAN Version 1.0 or
     * higher. Pass a malformed event to the <em>ECG SensorShell</em>.
     * The <em>argList</em> is empty. The expected result is that
     * the <em>SensorShell</em> revokes it.
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line
     *             number that is not available or if the requested
     *             String size is to higher then available
     */
    public final void testInvalidEventIsNotAcceptedForArgListIsEmpty()
        throws NoTestDataException {
        EventPacket eventPacket = this.eventGenerator.createEventPacket(true,
            true, this.linenumber, true, true, 0, ENTRY_SIZE);

        boolean result = sensor.sendEvent(eventPacket);

        assertFalse("" + this.linenumber, result);
    }

    /**
     * Testcase CL6 according to the document TESTPLAN Version 1.0 or
     * higher. Pass a malformed event to the <em>ECG SensorShell</em>.
     * The <em>argList</em> is empty. The expected result is that
     * the <em>SensorShell</em> revokes it.
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line
     *             number that is not available or if the requested
     *             String size is to higher then available
     */
    public final void testInvalidEventIsNotAcceptedForArgListIsNoStringList()
        throws NoTestDataException {
        EventPacket eventPacket = this.eventGenerator.createEventPacket(true,
            true, this.linenumber, true, false, LIST_LENGTH, ENTRY_SIZE);

        boolean result = sensor.sendEvent(eventPacket);

        assertFalse("" + this.linenumber, result);
    }

    /**
     * Testcase CL7 according to the document TESTPLAN Version 1.0 or
     * higher. Pass a valid <em>HackyStat Activity</em> event to the
     * <em>ECG SensorShell</em>. The expected result is that the
     * <em>SensorShell</em> accepts it.
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line
     *             number that is not available or if the requested
     *             String size is to higher then available
     */
    public final void testHackyStatActivityEventIsAccepted()
        throws NoTestDataException {
        WellFormedEventPacket eventPacket = this.eventGenerator
            .createHackyStatEventPacket(SensorDataType.ACTIVITY,
                this.linenumber);

        boolean result = sensor.sendEvent(eventPacket);

        assertTrue("" + this.linenumber, result);

    }

    /**
     * Testcase CL8 according to the document TESTPLAN Version 1.0 or
     * higher. Pass a valid <em>HackyStat Build</em> event to the
     * <em>ECG SensorShell</em>. The expected result is that the
     * <em>SensorShell</em> accepts it.
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line
     *             number that is not available or if the requested
     *             String size is to higher then available
     */
    public final void testHackyStatBuildEventsAccepted() throws NoTestDataException {
        WellFormedEventPacket eventPacket = this.eventGenerator
            .createHackyStatEventPacket(SensorDataType.BUILD, this.linenumber);

        boolean result = sensor.sendEvent(eventPacket);

        assertTrue("" + this.linenumber, result);

    }

    /**
     * Testcase CL9 according to the document TESTPLAN Version 1.0 or
     * higher. Pass a valid <em>HackyStat BuffTrans</em> event to the
     * <em>ECG SensorShell</em>. The expected result is that the
     * <em>SensorShell</em> accepts it.
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line
     *             number that is not available or if the requested
     *             String size is to higher then available
     */
    public final void testHackyStatBuffTransEventsAccepted()
        throws NoTestDataException {
        WellFormedEventPacket eventPacket = this.eventGenerator
            .createHackyStatEventPacket(SensorDataType.BUFFTRANS,
                this.linenumber);

        boolean result = sensor.sendEvent(eventPacket);

        assertTrue("" + this.linenumber, result);

    }

    /**
     * Testcase CL10 according to the document TESTPLAN Version 1.0 or
     * higher. Pass a valid <em>HackyStat Commit</em> event to the
     * <em>ECG SensorShell</em>. The expected result is that the
     * <em>SensorShell</em> accepts it.
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line
     *             number that is not available or if the requested
     *             String size is to higher then available
     */
    public final void testHackyStatCommitEventsAccepted() throws NoTestDataException {
        WellFormedEventPacket eventPacket = this.eventGenerator
            .createHackyStatEventPacket(SensorDataType.COMMIT, this.linenumber);

        boolean result = sensor.sendEvent(eventPacket);

        assertTrue("" + this.linenumber, result);

    }

    /**
     * Testcase CL11 according to the document TESTPLAN Version 1.0 or
     * higher. Pass a valid <em>HackyStat FileMetric</em> event to the
     * <em>ECG SensorShell</em>. The expected result is that the
     * <em>SensorShell</em> accepts it.
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line
     *             number that is not available or if the requested
     *             String size is to higher then available
     */
    public final void testHackyStatFileMetricEventsAccepted()
        throws NoTestDataException {
        WellFormedEventPacket eventPacket = this.eventGenerator
            .createHackyStatEventPacket(SensorDataType.FILEMETRIC,
                this.linenumber);

        boolean result = sensor.sendEvent(eventPacket);

        assertTrue("" + this.linenumber, result);

    }

    /**
     * Testcase CL12 according to the document TESTPLAN Version 1.0 or
     * higher. Pass a valid <em>HackyStat UnitTest</em> event to the
     * <em>ECG SensorShell</em>. The expected result is that the
     * <em>SensorShell</em> accepts it.
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line
     *             number that is not available or if the requested
     *             String size is to higher then available
     */
    public final void testHackyStatUnitTestEventsAccepted()
        throws NoTestDataException {
        WellFormedEventPacket eventPacket = this.eventGenerator
            .createHackyStatEventPacket(SensorDataType.UNITTEST,
                this.linenumber);

        boolean result = sensor.sendEvent(eventPacket);

        assertTrue("" + this.linenumber, result);

    }

}
