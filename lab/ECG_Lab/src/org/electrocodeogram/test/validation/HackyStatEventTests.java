/*
 * Classname: HackyStatEventTests
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.test.validation;

import junit.framework.TestCase;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.event.ValidEventPacket.VALIDATION_LEVEL;
import org.electrocodeogram.test.EventGenerator;
import org.electrocodeogram.test.NoTestDataException;
import org.electrocodeogram.test.EventGenerator.SensorDataType;

/**
 * Collects the testcases that are testing the use of
 * <em>HackyStat</em> events for the ECG. The
 * {@link org.electrocodeogram.event.ValidEventPacket.VALIDATION_LEVEL}
 * is set to
 * {@link org.electrocodeogram.event.ValidEventPacket.VALIDATION_LEVEL#HACKYSTAT}.
 * Every event that is not a valid <em>HackyStat</em> event must not
 * be allowed and every event that is a valid <em>HackyStat</em>
 * event has to be allowed by the ECG. Every testcase is sendind an
 * event of a different <em>SensorDataType</em>. To set the payload
 * for each event, a number is given to this class' constructor. The
 * number is passed to
 * {@link org.electrocodeogram.test.EventGenerator} which determines
 * the line in the <em>"pseudorandom.strings"</em> textfile with it.
 * The string in line with the given number is used as the payload for
 * the event. That way by calling a testcase one hundret times with a
 * different number, the payload can be varied for the test.
 */
public class HackyStatEventTests extends TestCase {

    /**
     * A reference to the object that is genertaing events for testing
     * purpoeses.
     */
    private EventGenerator eventGenerator;

    /**
     * The number is passed to the {@link #eventGenerator}. There it
     * is used to determine the line in the
     * <em>"pseudorandom.strings"</em> textfile. This line is the
     * payload for the event.
     */
    private int linenumber = -1;

    /**
     * Creates a testcase with the given name and payload linenumber.
     * @param name
     *            The name of the testcase to create
     * @param linePar
     *            Is the line in the <em>"pseudorandom.strings"</em>
     *            textfile, which is used as the payload for the event
     */
    public HackyStatEventTests(final String name, final int linePar) {
        super(name);

        this.linenumber = linePar;
    }

    /**
     * @see junit.framework.TestCase#setUp() Sets the
     *      Creates the {@link EventGenerator}.
     */
    @Override
    protected final void setUp() throws Exception {
        super.setUp();

        this.eventGenerator = new EventGenerator();

    }

    /**
     * @see junit.framework.TestCase#tearDown()
     * Releases the {@link #eventGenerator}.
     */
    @Override
    protected final void tearDown() throws Exception {
        super.tearDown();

        this.eventGenerator = null;
    }

    /**
     * Testcase SE6 according to the document TESTPLAN Version 1.0 or
     * higher. This is trying to validate an event which is not a valid
     * <em>HackyStat</em> event.
     * The expected result is a thrown {@link IllegalEventParameterException}.
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line
     *             number that is not available or if the requested
     *             String size is to higher then available
     */
    public final void testUnknownCommandNameIsNotAccepted()
        throws NoTestDataException {
        WellFormedEventPacket eventPacket = null;

        try {
            eventPacket = this.eventGenerator.createWellformedEventPacket(true,
                true, this.linenumber, true, true, 10, 10);

            validate(eventPacket);

            assertTrue(false);

        } catch (IllegalEventParameterException e) {
            assertTrue(true);
        }

    }

    /**
     * Testcase SE7 according to the document TESTPLAN Version 1.0 or
     * higher. This testcase is trying to validate a <em>HackyStat "Activity"</em>
     * event.
     * The expected result is that the test is not throwing an exception.
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line
     *             number that is not available or if the requested
     *             String size is higher as available
     * @throws IllegalEventParameterException If the event is not recognized as a valid <em>HackyStat</em> event by the ECG
     */
    public final void testHackyStatActivityEventsAccepted()
        throws NoTestDataException, IllegalEventParameterException {
        WellFormedEventPacket eventPacket = this.eventGenerator
            .createHackyStatEventPacket(SensorDataType.ACTIVITY,
                this.linenumber);

        validate(eventPacket);

        assertTrue(true);

    }

    /**
     * Testcase SE8 according to the document TESTPLAN Version 1.0 or
     * higher. This testcase is trying to validate a <em>HackyStat "Build"</em>
     * event.
     * The expected result is that the test is not throwing an exception.
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line
     *             number that is not available or if the requested
     *             String size is higher as available
     * @throws IllegalEventParameterException If the event is not recognized as a valid <em>HackyStat</em> event by the ECG
     */
    public final void testHackyStatBuildEventsAccepted()
        throws NoTestDataException, IllegalEventParameterException {
        WellFormedEventPacket eventPacket = this.eventGenerator
            .createHackyStatEventPacket(SensorDataType.BUILD, this.linenumber);

        validate(eventPacket);
        assertTrue(true);
    }

    /**
     * Testcase SE9 according to the document TESTPLAN Version 1.0 or
     * higher. This testcase is trying to validate a <em>HackyStat "BuffTrans"</em>
     * event.
     * The expected result is that the test is not throwing an exception.
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line
     *             number that is not available or if the requested
     *             String size is higher as available
     * @throws IllegalEventParameterException If the event is not recognized as a valid <em>HackyStat</em> event by the ECG
     */
    public final void testHackyStatBuffTransEventsAccepted()
        throws NoTestDataException, IllegalEventParameterException {
        WellFormedEventPacket eventPacket = this.eventGenerator
            .createHackyStatEventPacket(SensorDataType.BUFFTRANS,
                this.linenumber);

        validate(eventPacket);

        assertTrue(true);

    }

    /**
     * Testcase SE10 according to the document TESTPLAN Version 1.0 or
     * higher. This testcase is trying to validate a <em>HackyStat "Commit"</em>
     * event.
     * The expected result is that the test is not throwing an exception.
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line
     *             number that is not available or if the requested
     *             String size is higher as available
     * @throws IllegalEventParameterException If the event is not recognized as a valid <em>HackyStat</em> event by the ECG
     */
    public final void testHackyStatCommitEventsAccepted()
        throws NoTestDataException, IllegalEventParameterException {
        WellFormedEventPacket eventPacket = this.eventGenerator
            .createHackyStatEventPacket(SensorDataType.COMMIT, this.linenumber);

        validate(eventPacket);

        assertTrue(true);
    }

    /**
     * Testcase SE11 according to the document TESTPLAN Version 1.0 or
     * higher. This testcase is trying to validate a <em>HackyStat "FileMetric"</em>
     * event.
     * The expected result is that the test is not throwing an exception.
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line
     *             number that is not available or if the requested
     *             String size is higher as available
     * @throws IllegalEventParameterException If the event is not recognized as a valid <em>HackyStat</em> event by the ECG
     */
    public final void testHackyStatFileMetricEventsAccepted()
        throws NoTestDataException, IllegalEventParameterException {
        WellFormedEventPacket eventPacket = this.eventGenerator
            .createHackyStatEventPacket(SensorDataType.FILEMETRIC,
                this.linenumber);

        validate(eventPacket);

        assertTrue(true);
    }

    /**
     * Testcase SE12 according to the document TESTPLAN Version 1.0 or
     * higher. This testcase is trying to validate a <em>HackyStat "UnitTest"</em>
     * event.
     * The expected result is that the test is not throwing an exception.
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line
     *             number that is not available or if the requested
     *             String size is higher as available
     * @throws IllegalEventParameterException If the event is not recognized as a valid <em>HackyStat</em> event by the ECG
     */
    public final void testHackyStatUnitTestEventsAccepted()
        throws NoTestDataException, IllegalEventParameterException {
        WellFormedEventPacket eventPacket = this.eventGenerator
            .createHackyStatEventPacket(SensorDataType.UNITTEST,
                this.linenumber);

        validate(eventPacket);

        assertTrue(true);

    }

    /**
     * This method is trying to create a {@link ValidEventPacket} from the data of a given {@link WellFormedEventPacket}.
     * @param event The event containing the testdata
     * @throws IllegalEventParameterException If the event is not valid according to {@link org.electrocodeogram.event.ValidEventPacket.VALIDATION_LEVEL#HACKYSTAT}
     */
    private void validate(final WellFormedEventPacket event)
        throws IllegalEventParameterException {

        ValidEventPacket.setValidityLevel(VALIDATION_LEVEL.HACKYSTAT);

        new ValidEventPacket(event.getTimeStamp(), event
            .getSensorDataType(), event.getArgList());
    }
}
