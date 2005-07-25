package org.electrocodeogram.test.client;

import java.io.IOException;

import junit.framework.TestCase;

import org.electrocodeogram.client.SendingThreadTest;
import org.electrocodeogram.event.EventPacket;
import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.test.EventGenerator;
import org.electrocodeogram.test.NoTestDataException;
import org.electrocodeogram.test.EventGenerator.SensorDataType;
import org.electrocodeogram.test.client.mocksensor.MockSensor;

/**
 * This class collects all testcases for testing the client side of the ECG framework for
 * correct EventPacket object transportation.
 *
 */
public class ClientsideTests extends TestCase
{

    private MockSensor testSensor = null;

    private EventGenerator eventGenerator = null;
    
    private int $line = -1;

    /**
     * This creates the testcases of this collection.
     * @param name The name of the testcase to create
     * @param line Is the line number according to the file "ECG_Test/pseudorandom.strings" that is giving the test data
     */
    public ClientsideTests(String name, int line)
    {
        super(name);
        
        this.$line = line;
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.testSensor = new MockSensor();

        this.eventGenerator = new EventGenerator();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        this.testSensor = null;

        this.eventGenerator = null;
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
        ValidEventPacket eventPacket;

        eventPacket = this.eventGenerator.createValidEventPacket(true, true, this.$line, true, true, 10, 10);

        boolean result = this.testSensor.sendEvent(eventPacket);

        assertTrue("" + this.$line,result);
       
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
        ValidEventPacket eventPacket;

        eventPacket = this.eventGenerator.createValidEventPacket(true, true, this.$line,  true, true, 10, 10);

        this.testSensor.sendEvent(eventPacket);

        SendingThreadTest threadTest = new SendingThreadTest();

        int bufferSizeBefore = threadTest.getBufferSize();

        this.testSensor.sendEvent(eventPacket);

        assertTrue("" + this.$line,threadTest.testBufferSize(bufferSizeBefore + 1));

        assertTrue("" + this.$line,threadTest.testLastElement(eventPacket));
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
        EventPacket eventPacket = this.eventGenerator.createEventPacket(false, true, this.$line, true, true, 10, 10);

        boolean result = this.testSensor.sendEvent(eventPacket);

        assertFalse("" + this.$line,result);
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
        EventPacket eventPacket = this.eventGenerator.createEventPacket(true, false, this.$line, true, true, 10, 10);

        boolean result = this.testSensor.sendEvent(eventPacket);

        assertFalse("" + this.$line,result);
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
        EventPacket eventPacket = this.eventGenerator.createEventPacket(true, true, this.$line, false, true, 10, 10);

        boolean result = this.testSensor.sendEvent(eventPacket);

        assertFalse("" + this.$line,result);
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
        EventPacket eventPacket = this.eventGenerator.createEventPacket(true, true, this.$line, true, true, 0, 10);

        boolean result = this.testSensor.sendEvent(eventPacket);

        assertFalse("" + this.$line,result);
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
        EventPacket eventPacket = this.eventGenerator.createEventPacket(true, true, this.$line, true, false, 10, 10);

        boolean result = this.testSensor.sendEvent(eventPacket);

        assertFalse("" + this.$line,result);
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
            this.eventGenerator.createValidEventPacket(false, true, this.$line, true, true, 10, 10);

            fail("IllegalEventParameterException should be thrown");
        }
        catch (IllegalEventParameterException e) {
            assertTrue("" + this.$line,true);

            try {
                this.eventGenerator.createValidEventPacket(true, false, this.$line, true, true, 10, 10);

                fail("IllegalEventParameterException should be thrown");
            }
            catch (IllegalEventParameterException e1) {

                assertTrue("" + this.$line,true);

                try {
                    this.eventGenerator.createValidEventPacket(true, true, this.$line, false, true, 10, 10);

                    fail("IllegalEventParameterException should be thrown");
                }
                catch (IllegalEventParameterException e2) {
                    assertTrue("" + this.$line,true);

                    try {
                        this.eventGenerator.createValidEventPacket(true, true, this.$line, true, true, 0, 10);

                        fail("IllegalEventParameterException should be thrown");
                    }
                    catch (IllegalEventParameterException e3) {
                        assertTrue("" + this.$line,true);

                        try {
                            this.eventGenerator.createValidEventPacket(true, true, this.$line, true, false, 10, 10);

                            fail("IllegalEventParameterException should be thrown");
                        }
                        catch (IllegalEventParameterException e4) {
                            assertTrue("" + this.$line,true);
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
        ValidEventPacket eventPacket = this.eventGenerator.createHackyStatEventPacket(SensorDataType.ACTIVITY,this.$line);

        boolean result = this.testSensor.sendEvent(eventPacket);

        assertTrue("" + this.$line,result);

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
        ValidEventPacket eventPacket = this.eventGenerator.createHackyStatEventPacket(SensorDataType.BUILD,this.$line);

        boolean result = this.testSensor.sendEvent(eventPacket);

        assertTrue("" + this.$line,result);

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
        ValidEventPacket eventPacket = this.eventGenerator.createHackyStatEventPacket(SensorDataType.BUFFTRANS,this.$line);

        boolean result = this.testSensor.sendEvent(eventPacket);

        assertTrue("" + this.$line,result);

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
        ValidEventPacket eventPacket = this.eventGenerator.createHackyStatEventPacket(SensorDataType.COMMIT,this.$line);

        boolean result = this.testSensor.sendEvent(eventPacket);

        assertTrue("" + this.$line,result);

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
        ValidEventPacket eventPacket = this.eventGenerator.createHackyStatEventPacket(SensorDataType.FILEMETRIC,this.$line);

        boolean result = this.testSensor.sendEvent(eventPacket);

        assertTrue("" + this.$line,result);

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
        ValidEventPacket eventPacket = this.eventGenerator.createHackyStatEventPacket(SensorDataType.UNITTEST,this.$line);

        boolean result = this.testSensor.sendEvent(eventPacket);

        assertTrue("" + this.$line,result);

    }

}
