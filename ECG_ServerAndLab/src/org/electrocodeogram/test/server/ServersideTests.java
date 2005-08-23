package org.electrocodeogram.test.server;

import junit.framework.TestCase;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.TypedValidEventPacket;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.msdt.EventValidator;
import org.electrocodeogram.test.EventGenerator;
import org.electrocodeogram.test.NoTestDataException;
import org.electrocodeogram.test.EventGenerator.SensorDataType;
import org.electrocodeogram.test.server.mockClient.TestClient;

/**
 * This class collects testcases running over the ECG serverside.
 * 
 */
public class ServersideTests extends TestCase
{

    private EventValidator validator = null;

    private TestClient testClient = null;

    private EventGenerator eventGenerator = null;

    private int line = -1;

    /**
     * This creates a testcase with the given name.
     * 
     * @param name
     *            The name of the testcase to create
     * @param linePar
     *            Is the linenumber according to the file
     *            "ECG_Test/pseudorandom.strings" that is giving the testdata
     */
    public ServersideTests(String name, int linePar)
    {
        super(name);

        this.line = linePar;
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.validator = new EventValidator(null);

        this.validator.setAllowNonECGmSDTConformEvents(false);

        this.validator.setAllowNonECGmSDTConformEvents(true);

        this.testClient = new TestClient();

        this.eventGenerator = new EventGenerator();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();

        this.validator = null;

        this.testClient = null;

        this.eventGenerator = null;
    }

    // /**
    // * Testcase SE1 according to the document TESTPLAN Version 1.0 or higher.
    // * This testcase passes a single syntactically invalid EventPacket from a
    // TestClient
    // * to the ECG SensorShellWrapper. In this case the timestamp of the
    // EventPacket has the value "null".
    // * The test is successfull if the result from the ECG SensorShell is
    // "false", meaning the
    // * EventPacket is syntactically invalid and not accepted.
    // * @throws NoTestDataException If a pseudorandom String is requested by a
    // line number that is not available or if the requested String size is to
    // higher then available
    // *
    // */
    // public void testInvalidEventIsNotAcceptedTimeStampIsNull() throws
    // NoTestDataException
    // {
    // EventPacket eventPacket = this.eventGenerator.createEventPacket(false,
    // true, this.line, true, true, 10, 10);
    //
    // boolean result = this.testClient.passEventData(this.shell, eventPacket);
    //
    // assertFalse(result);
    // }
    //
    // /**
    // * Testcase SE2 according to the document TESTPLAN Version 1.0 or higher.
    // * This testcase passes a single syntactically invalid EventPacket from a
    // TestClient
    // * to the ECG SensorShellWrapper. In this case the commandName of the
    // EventPacket has the value "null".
    // * The test is successfull if the result from the ECG SensorShellWrapper
    // is "false", meaning the
    // * EventPacket is syntactically invalid and not accepted.
    // * @throws NoTestDataException If a pseudorandom String is requested by a
    // line number that is not available or if the requested String size is to
    // higher then available
    // *
    // */
    // public void testInvalidEventIsNotAcceptedCommandNameIsNull() throws
    // NoTestDataException
    // {
    // EventPacket eventPacket = this.eventGenerator.createEventPacket(true,
    // false, this.line, true, true, 10, 10);
    //
    // boolean result = this.testClient.passEventData(this.shell, eventPacket);
    //
    // assertFalse(result);
    // }
    //
    // /**
    // * Testcase SE3 according to the document TESTPLAN Version 1.0 or higher.
    // * This testcase passes a single syntactically invalid EventPacket from a
    // TestClient
    // * to the ECG SensorShellWrapper. In this case the argList of the
    // EventPacket has the value "null".
    // * The test is successfull if the result from the ECG SensorShellWrapper
    // is "false", meaning the
    // * EventPacket is syntactically invalid and not accepted.
    // * @throws NoTestDataException If a pseudorandom String is requested by a
    // line number that is not available or if the requested String size is to
    // higher then available
    // *
    // */
    // public void testInvalidEventIsNotAcceptedArgListIsNull() throws
    // NoTestDataException
    // {
    // EventPacket eventPacket = this.eventGenerator.createEventPacket(true,
    // true, this.line, false, true, 10, 10);
    //
    // boolean result = this.testClient.passEventData(this.shell, eventPacket);
    //
    // assertFalse(result);
    // }
    //
    // /**
    // * Testcase SE4 according to the document TESTPLAN Version 1.0 or higher.
    // * This testcase passes a single syntactically invalid EventPacket from a
    // TestClient
    // * to the ECG SensorShellWrapper. In this case the argList is empty.
    // * The test is successfull if the result from the ECG SensorShellWrapper
    // is "false", meaning the
    // * EventPacket is syntactically invalid and not accepted.
    // * @throws NoTestDataException If a pseudorandom String is requested by a
    // line number that is not available or if the requested String size is to
    // higher then available
    // *
    // */
    // public void testInvalidEventIsNotAcceptedArgListIsEmpty() throws
    // NoTestDataException
    // {
    // EventPacket eventPacket = this.eventGenerator.createEventPacket(true,
    // true, this.line, true, true, 0, 10);
    //
    // boolean result = this.testClient.passEventData(this.shell, eventPacket);
    //
    // assertFalse(result);
    // }
    //
    // /**
    // * Testcase SE5 according to the document TESTPLAN Version 1.0 or higher.
    // * This testcase passes a single syntactically invalid EventPacket from a
    // TestClient
    // * to the ECG SensorShellWrapper. In this case the argList is not of type
    // List<String>.
    // * The test is successfull if the result from the ECG SensorShellWrapper
    // is "false", meaning the
    // * EventPacket is syntactically invalid and not accepted.
    // * @throws NoTestDataException If a pseudorandom String is requested by a
    // line number that is not available or if the requested String size is to
    // higher then available
    // *
    // */
    // public void testInvalidEventIsNotAcceptedArgListIsNotOfTypeString()
    // throws NoTestDataException
    // {
    // EventPacket eventPacket = this.eventGenerator.createEventPacket(true,
    // true, this.line, true, false, 10, 10);
    //
    // boolean result = this.testClient.passEventData(this.shell, eventPacket);
    //
    // assertFalse(result);
    // }

    /**
     * Testcase SE6 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a single syntactically valid EventPacket from a
     * TestClient to the ECG SensorShellWrapper. But the EventPacket's
     * commandName value is not any of the HackyStat SensorDataTypes. The test
     * is successfull if the result from the ECG SensorShellWrapper is "false".
     * 
     * @throws IllegalEventParameterException
     *             If the parameters passed to the event creating method are
     *             illegal
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line number that
     *             is not available or if the requested String size is to higher
     *             then available
     * 
     */
    public void testUnknownCommandNameIsNotAccepted() throws IllegalEventParameterException, NoTestDataException
    {
        ValidEventPacket eventPacket = null;

        eventPacket = this.eventGenerator.createValidEventPacket(true, true, this.line, true, true, 10, 10);

        TypedValidEventPacket result = this.testClient.passEventData(this.validator, eventPacket);

        assertNull(result);
    }

    /**
     * Testcase SE7 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a HackyStat "Activity" SensorDataType event to the
     * ECG SensorShellWrapper.
     * 
     * The test is successfull if the result from the ECG SensorShellWrapper is
     * "true".
     * 
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line number that
     *             is not available or if the requested String size is to higher
     *             then available
     * 
     */
    public void testHackyStatActivityEventsAccepted() throws NoTestDataException
    {
        ValidEventPacket eventPacket = this.eventGenerator.createHackyStatEventPacket(SensorDataType.ACTIVITY, this.line);

        TypedValidEventPacket result = this.testClient.passEventData(this.validator, eventPacket);

        assertNotNull(result);

    }

    /**
     * Testcase SE8 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a HackyStat "Build" SensorDataType event to the ECG
     * SensorShellWrapper.
     * 
     * The test is successfull if the result from the ECG SensorShellWrapper is
     * "true".
     * 
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line number that
     *             is not available or if the requested String size is to higher
     *             then available
     * 
     */
    public void testHackyStatBuildEventsAccepted() throws NoTestDataException
    {
        ValidEventPacket eventPacket = this.eventGenerator.createHackyStatEventPacket(SensorDataType.BUILD, this.line);

        TypedValidEventPacket result = this.testClient.passEventData(this.validator, eventPacket);

        assertNotNull(result);
    }

    /**
     * Testcase SE9 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a HackyStat "BuffTrans" SensorDataType event to the
     * ECG SensorShellWrapper.
     * 
     * The test is successfull if the result from the ECG SensorShellWrapper is
     * "true".
     * 
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line number that
     *             is not available or if the requested String size is to higher
     *             then available
     * 
     */
    public void testHackyStatBuffTransEventsAccepted() throws NoTestDataException
    {
        ValidEventPacket eventPacket = this.eventGenerator.createHackyStatEventPacket(SensorDataType.BUFFTRANS, this.line);

        TypedValidEventPacket result = this.testClient.passEventData(this.validator, eventPacket);

        assertNotNull(result);

    }

    /**
     * Testcase SE10 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a HackyStat "Commit" SensorDataType event to the ECG
     * SensorShellWrapper.
     * 
     * The test is successfull if the result from the ECG SensorShellWrapper is
     * "true".
     * 
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line number that
     *             is not available or if the requested String size is to higher
     *             then available
     * 
     */
    public void testHackyStatCommitEventsAccepted() throws NoTestDataException
    {
        ValidEventPacket eventPacket = this.eventGenerator.createHackyStatEventPacket(SensorDataType.COMMIT, this.line);

        TypedValidEventPacket result = this.testClient.passEventData(this.validator, eventPacket);

        assertNotNull(result);

    }

    /**
     * Testcase SE11 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a HackyStat "Commit" SensorDataType event to the ECG
     * SensorShellWrapper.
     * 
     * The test is successfull if the result from the ECG SensorShellWrapper is
     * "true".
     * 
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line number that
     *             is not available or if the requested String size is to higher
     *             then available
     * 
     */
    public void testHackyStatFileMetricEventsAccepted() throws NoTestDataException
    {
        ValidEventPacket eventPacket = this.eventGenerator.createHackyStatEventPacket(SensorDataType.FILEMETRIC, this.line);

        TypedValidEventPacket result = this.testClient.passEventData(this.validator, eventPacket);

        assertNotNull(result);

    }

    /**
     * Testcase SE12 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a HackyStat "Commit" SensorDataType event to the ECG
     * SensorShellWrapper.
     * 
     * The test is successfull if the result from the ECG SensorShellWrapper is
     * "true".
     * 
     * @throws NoTestDataException
     *             If a pseudorandom String is requested by a line number that
     *             is not available or if the requested String size is to higher
     *             then available
     * 
     */
    public void testHackyStatUnitTestEventsAccepted() throws NoTestDataException
    {
        ValidEventPacket eventPacket = this.eventGenerator.createHackyStatEventPacket(SensorDataType.UNITTEST, this.line);

        TypedValidEventPacket result = this.testClient.passEventData(this.validator, eventPacket);

        assertNotNull(result);

    }
}
