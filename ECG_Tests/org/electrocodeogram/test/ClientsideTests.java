package org.electrocodeogram.test;

import junit.framework.TestCase;

import org.electrocodeogram.client.SendingThreadTest;
import org.electrocodeogram.event.EventPacket;
import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.sensor.TestSensor;

/**
 * This class collects all testcases for testing the client side of the ECG framework for
 * correct EventObject transportation.
 *
 */
public class ClientsideTests extends TestCase
{

    private TestSensor testSensor = null;

    /**
     * This creates the testcases of this collection.
     * @param name The name of the testcase to create
     */
    public ClientsideTests(String name)
    {
        super(name);
    }

    @Override
    protected void setUp()
    {
        this.testSensor = new TestSensor();
    }

    @Override
    protected void tearDown()
    {
        this.testSensor = null;
    }

    /**
     * Testcase 1 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a single syntactically valid EventPacket from a TestSensor
     * to the ECG SensorShell. The test is successfull if the result from the ECG
     * SensorShell is "true", meaning the EventPacket is syntactically valid and accepted. 
     *
     */
    public void testValidEventIsAccepted()
    {
        ValidEventPacket eventPacket;

        try {
            eventPacket = this.testSensor.createValidEventPacket(true, true, true, true, 10, 10);

            boolean result = this.testSensor.sendEvent(eventPacket);

            assertTrue(result);
        }
        catch (IllegalEventParameterException e) {

            e.printStackTrace();

            fail();
        }

    }

    /**
     * Testcase 2 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a single syntactically valid EventPacket from a TestSensor
     * to the ECG SensorShell. The test is successfull if the SendingThreatTest tells
     * that the size of the SendingThreat EventPacketBuffer is increased by one element
     * and that this element is the sended EventPacket. 
     *
     */
    public void testValidEventIsQueued()
    {
        ValidEventPacket eventPacket;
        try {
            eventPacket = this.testSensor.createValidEventPacket(true, true, true, true, 10, 10);

            this.testSensor.sendEvent(eventPacket);

            SendingThreadTest threadTest = new SendingThreadTest();

            int bufferSizeBefore = threadTest.getBufferSize();

            this.testSensor.sendEvent(eventPacket);

            assertTrue(threadTest.testBufferSize(bufferSizeBefore + 1));

            assertTrue(threadTest.testLastElement(eventPacket));
        }
        catch (IllegalEventParameterException e) {

            e.printStackTrace();

            fail();
        }

    }

    /**
     * Testcase 3 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a single syntactically invalid EventPacket from a TestSensor
     * to the ECG SensorShell. In this case the timestamp of the EventPacket has the value "null".
     * The test is successfull if the result from the ECG SensorShell is "false", meaning the
     * EventPacket is syntactically invalid and not accepted. 
     *
     */
    public void testInvalidEventIsNotAcceptedTimeStampIsNull()
    {
        EventPacket eventPacket = this.testSensor.createEventPacket(false, true, true, true, 10, 10);

        boolean result = this.testSensor.sendEvent(eventPacket);

        assertFalse(result);
    }

    /**
     * Testcase 4 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a single syntactically invalid EventPacket from a TestSensor
     * to the ECG SensorShell. In this case the commandName of the EventPacket has the value "null".
     * The test is successfull if the result from the ECG SensorShell is "false", meaning the
     * EventPacket is syntactically invalid and not accepted. 
     *
     */
    public void testInvalidEventIsNotAcceptedCommandNameIsNull()
    {
        EventPacket eventPacket = this.testSensor.createEventPacket(true, false, true, true, 10, 10);

        boolean result = this.testSensor.sendEvent(eventPacket);

        assertFalse(result);
    }

    /**
     * Testcase 5 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a single syntactically invalid EventPacket from a TestSensor
     * to the ECG SensorShell. In this case the argList of the EventPacket has the value "null".
     * The test is successfull if the result from the ECG SensorShell is "false", meaning the
     * EventPacket is syntactically invalid and not accepted. 
     *
     */
    public void testInvalidEventIsNotAcceptedArgListIsNull()
    {
        EventPacket eventPacket = this.testSensor.createEventPacket(true, true, false, true, 10, 10);

        boolean result = this.testSensor.sendEvent(eventPacket);

        assertFalse(result);
    }

    /**
     * Testcase 6 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a single syntactically invalid EventPacket from a TestSensor
     * to the ECG SensorShell. In this case the argList is empty.
     * The test is successfull if the result from the ECG SensorShell is "false", meaning the
     * EventPacket is syntactically invalid and not accepted. 
     *
     */
    public void testInvalidEventIsNotAcceptedArgListIsEmpty()
    {
        EventPacket eventPacket = this.testSensor.createEventPacket(true, true, true, true, 0, 10);

        boolean result = this.testSensor.sendEvent(eventPacket);

        assertFalse(result);
    }

    /**
     * Testcase 7 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a single syntactically invalid EventPacket from a TestSensor
     * to the ECG SensorShell. In this case the argList is not of type List<String>.
     * The test is successfull if the result from the ECG SensorShell is "false", meaning the
     * EventPacket is syntactically invalid and not accepted. 
     *
     */
    public void testInvalidEventIsNotAcceptedArgListIsNotOfTypeString()
    {
        EventPacket eventPacket = this.testSensor.createEventPacket(true, true, true, false, 10, 10);

        boolean result = this.testSensor.sendEvent(eventPacket);

        assertFalse(result);
    }

    /**
     * Testcase 8 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase validates the correct behaviour of creating an ValidEventPacket.
     * The test succeeds if the creation brings up an Exception, if invalid
     * parameters are passed to the ValidEventPacket constructor.
     */
    public void testIllegalEventParametersCauseException()
    {
        try {
            this.testSensor.createValidEventPacket(false, true, true, true, 10, 10);

            fail("IllegalEventParameterException should be thrown");
        }
        catch (IllegalEventParameterException e) {
            assertTrue(true);

            try {
                this.testSensor.createValidEventPacket(true, false, true, true, 10, 10);

                fail("IllegalEventParameterException should be thrown");
            }
            catch (IllegalEventParameterException e1) {

                assertTrue(true);

                try {
                    this.testSensor.createValidEventPacket(true, true, false, true, 10, 10);

                    fail("IllegalEventParameterException should be thrown");
                }
                catch (IllegalEventParameterException e2) {
                    assertTrue(true);

                    try {
                        this.testSensor.createValidEventPacket(true, true, true, true, 0, 10);

                        fail("IllegalEventParameterException should be thrown");
                    }
                    catch (IllegalEventParameterException e3) {
                        assertTrue(true);

                        try {
                            this.testSensor.createValidEventPacket(true, true, true, false, 10, 10);

                            fail("IllegalEventParameterException should be thrown");
                        }
                        catch (IllegalEventParameterException e4) {
                            assertTrue(true);
                        }
                    }
                }
            }
        }
    }

}
