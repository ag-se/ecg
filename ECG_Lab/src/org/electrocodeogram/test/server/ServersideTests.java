package org.electrocodeogram.test.server;

import junit.framework.TestCase;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.TypedValidEventPacket;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.msdt.validation.EventValidator;
import org.electrocodeogram.test.EventGenerator;
import org.electrocodeogram.test.NoTestDataException;
import org.electrocodeogram.test.EventGenerator.SensorDataType;
import org.electrocodeogram.test.server.mockClient.MockClient;

/**
 * This class collects testcases running over the ECG serverside.
 * 
 */
public class ServersideTests extends TestCase
{

    private EventValidator _eventValidator = null;

    private MockClient _mockClient = null;

    private EventGenerator _eventGenerator = null;

    private int _line = -1;

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

        this._line = linePar;
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this._eventValidator = new EventValidator(null);

        this._eventValidator.setAllowNonECGmSDTConformEvents(false);

        this._eventValidator.setAllowNonECGmSDTConformEvents(true);

        this._mockClient = new MockClient();

        this._eventGenerator = new EventGenerator();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();

        this._eventValidator = null;

        this._mockClient = null;

        this._eventGenerator = null;
    }

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

        eventPacket = this._eventGenerator.createValidEventPacket(true, true, this._line, true, true, 10, 10);

        TypedValidEventPacket result = this._mockClient.passEventData(this._eventValidator, eventPacket);

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
        ValidEventPacket eventPacket = this._eventGenerator.createHackyStatEventPacket(SensorDataType.ACTIVITY, this._line);

        TypedValidEventPacket result = this._mockClient.passEventData(this._eventValidator, eventPacket);

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
        ValidEventPacket eventPacket = this._eventGenerator.createHackyStatEventPacket(SensorDataType.BUILD, this._line);

        TypedValidEventPacket result = this._mockClient.passEventData(this._eventValidator, eventPacket);

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
        ValidEventPacket eventPacket = this._eventGenerator.createHackyStatEventPacket(SensorDataType.BUFFTRANS, this._line);

        TypedValidEventPacket result = this._mockClient.passEventData(this._eventValidator, eventPacket);

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
        ValidEventPacket eventPacket = this._eventGenerator.createHackyStatEventPacket(SensorDataType.COMMIT, this._line);

        TypedValidEventPacket result = this._mockClient.passEventData(this._eventValidator, eventPacket);

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
        ValidEventPacket eventPacket = this._eventGenerator.createHackyStatEventPacket(SensorDataType.FILEMETRIC, this._line);

        TypedValidEventPacket result = this._mockClient.passEventData(this._eventValidator, eventPacket);

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
        ValidEventPacket eventPacket = this._eventGenerator.createHackyStatEventPacket(SensorDataType.UNITTEST, this._line);

        TypedValidEventPacket result = this._mockClient.passEventData(this._eventValidator, eventPacket);

        assertNotNull(result);

    }
}
