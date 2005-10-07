package org.electrocodeogram.test.client;

import junit.framework.TestCase;

import org.electrocodeogram.client.SendingThreadTest;
import org.electrocodeogram.event.EventPacket;
import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.test.EventGenerator;
import org.electrocodeogram.test.NoTestDataException;
import org.electrocodeogram.test.EventGenerator.SensorDataType;
import org.electrocodeogram.test.client.mocksensor.MockSensor;

import utmj.threaded.RetriedAssert;

/**
 * This class collects all testcases for testing the client side of the ECG framework for
 * correct EventPacket object transportation.
 *
 */
public class ClientsideTests extends TestCase
{

    private static MockSensor _testSensor = new MockSensor();

    private EventGenerator _eventGenerator = null;
    
    int _line = -1;
    
    SendingThreadTest _threadTest;
    
    int _bufferSizeBefore;
    
    WellFormedEventPacket _eventPacket;

    /**
     * This creates the testcases of this collection.
     * @param name The name of the testcase to create
     * @param line Is the line number according to the file "ECG_Test/pseudorandom.strings" that is giving the test data
     */
    public ClientsideTests(String name, int line)
    {
        super(name);
        
        this._line = line;
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
       
        this._eventGenerator = new EventGenerator();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        this._eventGenerator = null;
    }

    /**
     * Testcase CL1 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a single syntactically valid EventPacket from a TestSensor
     * to the ECG SensorShell. The test is successful if the result from the ECG
     * SensorShell is "true", meaning the EventPacket is syntactically valid and accepted. 
     * @throws IllegalEventParameterException If the parameters passed to the event creating method are not legal
     * @throws NoTestDataException If a pseudo-random String is requested by a line number that is not available or if the requested String size is to higher then available
     *
     */
    public void testValidEventIsAccepted() throws IllegalEventParameterException, NoTestDataException
    {
        WellFormedEventPacket eventPacket;

        eventPacket = this._eventGenerator.createValidEventPacket(true, true, this._line, true, true, 10, 10);

        boolean result = this._testSensor.sendEvent(eventPacket);

        assertTrue("" + this._line,result);
       
    }

    
    
    /**
     * Testcase CL2 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a single syntactically valid EventPacket from a TestSensor
     * to the ECG SensorShell. The test is successful if the SendingThreatTest tells
     * that the size of the SendingThread EventPacketBuffer is increased by one element
     * and that this element is the sent EventPacket. 
     * @throws IllegalEventParameterException If the parameters passed to the event creating method are not legal
     * @throws NoTestDataException If a pseudo-random String is requested by a line number that is not available or if the requested String size is to higher then available
     *
     */
    public void testValidEventIsQueued() throws IllegalEventParameterException, NoTestDataException
    {
        ClientsideTests.this._eventPacket = this._eventGenerator.createValidEventPacket(true, true, this._line,  true, true, 10, 10);

        this._testSensor.sendEvent(ClientsideTests.this._eventPacket);

        ClientsideTests.this._threadTest = new SendingThreadTest();

        ClientsideTests.this._bufferSizeBefore = ClientsideTests.this._threadTest.getBufferSize();

        this._testSensor.sendEvent(ClientsideTests.this._eventPacket);
        
        try
		{
			new RetriedAssert(2000, 100) {
			    @Override
			    public void run() throws Exception
			    {
			    	 assertTrue("" + ClientsideTests.this._line,ClientsideTests.this._threadTest.testBufferSize(ClientsideTests.this._bufferSizeBefore + 1));
			    	 
			    	 assertTrue("" + ClientsideTests.this._line,ClientsideTests.this._threadTest.testLastElement(ClientsideTests.this._eventPacket));
			    }
			}.start();
		}
		catch (Exception e)
		{
			assertTrue(e.getMessage(),false);
		}

       

        
    }

    /**
     * Testcase CL3 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a single syntactically invalid EventPacket from a TestSensor
     * to the ECG SensorShell. In this case the timestamp of the EventPacket has the value "null".
     * The test is successful if the result from the ECG SensorShell is "false", meaning the
     * EventPacket is syntactically invalid and not accepted. 
     * @throws NoTestDataException If a pseudo-random String is requested by a line number that is not available or if the requested String size is to higher then available
     *
     */
    public void testInvalidEventIsNotAcceptedTimeStampIsNull() throws NoTestDataException
    {
        EventPacket eventPacket = this._eventGenerator.createEventPacket(false, true, this._line, true, true, 10, 10);

        boolean result = this._testSensor.sendEvent(eventPacket);

        assertFalse("" + this._line,result);
    }

    /**
     * Testcase CL4 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a single syntactically invalid EventPacket from a TestSensor
     * to the ECG SensorShell. In this case the commandName of the EventPacket has the value "null".
     * The test is successful if the result from the ECG SensorShell is "false", meaning the
     * EventPacket is syntactically invalid and not accepted. 
     * @throws NoTestDataException If a pseudo-random String is requested by a line number that is not available or if the requested String size is to higher then available
     *
     */
    public void testInvalidEventIsNotAcceptedCommandNameIsNull() throws NoTestDataException
    {
        EventPacket eventPacket = this._eventGenerator.createEventPacket(true, false, this._line, true, true, 10, 10);

        boolean result = this._testSensor.sendEvent(eventPacket);

        assertFalse("" + this._line,result);
    }

    /**
     * Testcase CL5 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a single syntactically invalid EventPacket from a TestSensor
     * to the ECG SensorShell. In this case the argList of the EventPacket has the value "null".
     * The test is successful if the result from the ECG SensorShell is "false", meaning the
     * EventPacket is syntactically invalid and not accepted. 
     * @throws NoTestDataException If a pseudo-random String is requested by a line number that is not available or if the requested String size is to higher then available
     *
     */
    public void testInvalidEventIsNotAcceptedArgListIsNull() throws NoTestDataException
    {
        EventPacket eventPacket = this._eventGenerator.createEventPacket(true, true, this._line, false, true, 10, 10);

        boolean result = this._testSensor.sendEvent(eventPacket);

        assertFalse("" + this._line,result);
    }

    /**
     * Testcase CL6 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a single syntactically invalid EventPacket from a TestSensor
     * to the ECG SensorShell. In this case the argList is empty.
     * The test is successful if the result from the ECG SensorShell is "false", meaning the
     * EventPacket is syntactically invalid and not accepted. 
     * @throws NoTestDataException If a pseudo-random String is requested by a line number that is not available or if the requested String size is to higher then available
     *
     */
    public void testInvalidEventIsNotAcceptedArgListIsEmpty() throws NoTestDataException
    {
        EventPacket eventPacket = this._eventGenerator.createEventPacket(true, true, this._line, true, true, 0, 10);

        boolean result = this._testSensor.sendEvent(eventPacket);

        assertFalse("" + this._line,result);
    }

    /**
     * Testcase CL7 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a single syntactically invalid EventPacket from a TestSensor
     * to the ECG SensorShell. In this case the argList is not of type List<String>.
     * The test is successful if the result from the ECG SensorShell is "false", meaning the
     * EventPacket is syntactically invalid and not accepted. 
     * @throws NoTestDataException If a pseudo-random String is requested by a line number that is not available or if the requested String size is to higher then available
     *
     */
    public void testInvalidEventIsNotAcceptedArgListIsNotOfTypeString() throws NoTestDataException
    {
        EventPacket eventPacket = this._eventGenerator.createEventPacket(true, true, this._line, true, false, 10, 10);

        boolean result = this._testSensor.sendEvent(eventPacket);

        assertFalse("" + this._line,result);
    }

    /**
     * Testcase CL8 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase validates the correct behaviour of creating an ValidEventPacket.
     * The test succeeds if the creation brings up an Exception, if invalid
     * parameters are passed to the ValidEventPacket constructor.
     * @throws NoTestDataException If a pseudo-random String is requested by a line number that is not available or if the requested String size is to higher then available
     */
    public void testIllegalEventParametersCauseException() throws NoTestDataException
    {
        try {
            this._eventGenerator.createValidEventPacket(false, true, this._line, true, true, 10, 10);

            fail("IllegalEventParameterException should be thrown");
        }
        catch (IllegalEventParameterException e) {
            assertTrue("" + this._line,true);

            try {
                this._eventGenerator.createValidEventPacket(true, false, this._line, true, true, 10, 10);

                fail("IllegalEventParameterException should be thrown");
            }
            catch (IllegalEventParameterException e1) {

                assertTrue("" + this._line,true);

                try {
                    this._eventGenerator.createValidEventPacket(true, true, this._line, false, true, 10, 10);

                    fail("IllegalEventParameterException should be thrown");
                }
                catch (IllegalEventParameterException e2) {
                    assertTrue("" + this._line,true);

                    try {
                        this._eventGenerator.createValidEventPacket(true, true, this._line, true, true, 0, 10);

                        fail("IllegalEventParameterException should be thrown");
                    }
                    catch (IllegalEventParameterException e3) {
                        assertTrue("" + this._line,true);

                        try {
                            this._eventGenerator.createValidEventPacket(true, true, this._line, true, false, 10, 10);

                            fail("IllegalEventParameterException should be thrown");
                        }
                        catch (IllegalEventParameterException e4) {
                            assertTrue("" + this._line,true);
                        }
                    }
                }
            }
        }
    }

    /**
     * Testcase CL9 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a HackyStat "Activity" SensorDataType event to the ECG SensorShellWrapper.
     * 
     * The test is successful if the result from the ECG SensorShellWrapper is "true".
     * @throws NoTestDataException If a pseudo-random String is requested by a line number that is not available or if the requested String size is to higher then available
     *
     */
    public void testHackyStatActivityEventsAccepted() throws NoTestDataException
    {
        WellFormedEventPacket eventPacket = this._eventGenerator.createHackyStatEventPacket(SensorDataType.ACTIVITY,this._line);

        boolean result = this._testSensor.sendEvent(eventPacket);

        assertTrue("" + this._line,result);

    }

    /**
     * Testcase CL10 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a HackyStat "Build" SensorDataType event to the ECG SensorShellWrapper.
     * 
     * The test is successful if the result from the ECG SensorShellWrapper is "true".
     * @throws NoTestDataException If a pseudo-random String is requested by a line number that is not available or if the requested String size is to higher then available
     *
     */
    public void testHackyStatBuildEventsAccepted() throws NoTestDataException
    {
        WellFormedEventPacket eventPacket = this._eventGenerator.createHackyStatEventPacket(SensorDataType.BUILD,this._line);

        boolean result = this._testSensor.sendEvent(eventPacket);

        assertTrue("" + this._line,result);

    }

    /**
     * Testcase CL11 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a HackyStat "BuffTrans" SensorDataType event to the ECG SensorShellWrapper.
     * 
     * The test is successful if the result from the ECG SensorShellWrapper is "true".
     * @throws NoTestDataException If a pseudo-random String is requested by a line number that is not available or if the requested String size is to higher then available
     *
     */
    public void testHackyStatBuffTransEventsAccepted() throws NoTestDataException
    {
        WellFormedEventPacket eventPacket = this._eventGenerator.createHackyStatEventPacket(SensorDataType.BUFFTRANS,this._line);

        boolean result = this._testSensor.sendEvent(eventPacket);

        assertTrue("" + this._line,result);

    }

    /**
     * Testcase CL12 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a HackyStat "Commit" SensorDataType event to the ECG SensorShellWrapper.
     * 
     * The test is successful if the result from the ECG SensorShellWrapper is "true".
     * @throws NoTestDataException If a pseudo-random String is requested by a line number that is not available or if the requested String size is to higher then available
     *
     */
    public void testHackyStatCommitEventsAccepted() throws NoTestDataException
    {
        WellFormedEventPacket eventPacket = this._eventGenerator.createHackyStatEventPacket(SensorDataType.COMMIT,this._line);

        boolean result = this._testSensor.sendEvent(eventPacket);

        assertTrue("" + this._line,result);

    }

    /**
     * Testcase CL13 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a HackyStat "Commit" SensorDataType event to the ECG SensorShellWrapper.
     * 
     * The test is successful if the result from the ECG SensorShellWrapper is "true".
     * @throws NoTestDataException If a pseudo-random String is requested by a line number that is not available or if the requested String size is to higher then available
     *
     */
    public void testHackyStatFileMetricEventsAccepted() throws NoTestDataException
    {
        WellFormedEventPacket eventPacket = this._eventGenerator.createHackyStatEventPacket(SensorDataType.FILEMETRIC,this._line);

        boolean result = this._testSensor.sendEvent(eventPacket);

        assertTrue("" + this._line,result);

    }

    /**
     * Testcase CL14 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a HackyStat "Commit" SensorDataType event to the ECG SensorShellWrapper.
     * 
     * The test is successful if the result from the ECG SensorShellWrapper is "true".
     * @throws NoTestDataException If a pseudo-random String is requested by a line number that is not available or if the requested String size is to higher then available
     *
     */
    public void testHackyStatUnitTestEventsAccepted() throws NoTestDataException
    {
        WellFormedEventPacket eventPacket = this._eventGenerator.createHackyStatEventPacket(SensorDataType.UNITTEST,this._line);

        boolean result = this._testSensor.sendEvent(eventPacket);

        assertTrue("" + this._line,result);

    }

}
