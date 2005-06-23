package org.electrocodeogram.test;

import junit.framework.TestCase;

import org.electrocodeogram.SendingThreadTest;
import org.electrocodeogram.TestEventPacket;
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
     * This testcase passes a single syntactically valid EventPacket from a TestSensor
     * to the ECG SensorShell. The test is successfull if the result from the ECG
     * SensorShell is "true", meaning the EventPacket is syntactically valid and accepted. 
     *
     */
    public void testA()
    {
        TestEventPacket eventPacket = this.testSensor.createEventPacket(true,true,true,true,10,10);
        
        boolean result = this.testSensor.sendEvent(eventPacket);
        
        assertTrue(result);
    }
    
    /**
     * This testcase passes a single syntactically valid EventPacket from a TestSensor
     * to the ECG SensorShell. The test is successfull if the SendingThreatTest tells
     * that the size of the SendingThreat EventPacketBuffer is increased by one element
     * and that this element is the sended EventPacket. 
     *
     */
    public void testB()
    {
        TestEventPacket eventPacket = this.testSensor.createEventPacket(true,true,true,true,10,10);
        
        this.testSensor.sendEvent(eventPacket);
        
        SendingThreadTest threadTest = new SendingThreadTest();
        
        int bufferSizeBefore = threadTest.getBufferSize();
        
        this.testSensor.sendEvent(eventPacket);
        
        assertTrue(threadTest.testBufferSize(bufferSizeBefore+1));
        
        assertTrue(threadTest.testLastElement(eventPacket));
    }
    
    /**
     * This testcase passes a single syntactically invalid EventPacket from a TestSensor
     * to the ECG SensorShell. In this case the timestamp of the EventPacket has the value "null".
     * The test is successfull if the result from the ECG SensorShell is "false", meaning the
     * EventPacket is syntactically invalid and not accepted. 
     *
     */
    public void testC()
    {
        TestEventPacket eventPacket = this.testSensor.createEventPacket(false,true,true,true,10,10);
        
        boolean result = this.testSensor.sendEvent(eventPacket);
        
        assertFalse(result);
    }
    
    /**
     * This testcase passes a single syntactically invalid EventPacket from a TestSensor
     * to the ECG SensorShell. In this case the commandName of the EventPacket has the value "null".
     * The test is successfull if the result from the ECG SensorShell is "false", meaning the
     * EventPacket is syntactically invalid and not accepted. 
     *
     */
    public void testD()
    {
        TestEventPacket eventPacket = this.testSensor.createEventPacket(true,false,true,true,10,10);
        
        boolean result = this.testSensor.sendEvent(eventPacket);
        
        assertFalse(result);
    }
    
    
    /**
     * This testcase passes a single syntactically invalid EventPacket from a TestSensor
     * to the ECG SensorShell. In this case the argList of the EventPacket has the value "null".
     * The test is successfull if the result from the ECG SensorShell is "false", meaning the
     * EventPacket is syntactically invalid and not accepted. 
     *
     */
    public void testE()
    {
        TestEventPacket eventPacket = this.testSensor.createEventPacket(true,true,false,true,10,10);
        
        boolean result = this.testSensor.sendEvent(eventPacket);
        
        assertFalse(result);
    }
    
    /**
     * This testcase passes a single syntactically invalid EventPacket from a TestSensor
     * to the ECG SensorShell. In this case the argList is empty.
     * The test is successfull if the result from the ECG SensorShell is "false", meaning the
     * EventPacket is syntactically invalid and not accepted. 
     *
     */
    public void testF()
    {
        TestEventPacket eventPacket = this.testSensor.createEventPacket(true,true,true,true,0,10);
        
        boolean result = this.testSensor.sendEvent(eventPacket);
        
        assertFalse(result);
    }
    
    /**
     * This testcase passes a single syntactically invalid EventPacket from a TestSensor
     * to the ECG SensorShell. In this case the argList is not of type List<String>.
     * The test is successfull if the result from the ECG SensorShell is "false", meaning the
     * EventPacket is syntactically invalid and not accepted. 
     *
     */
    public void testG()
    {
        TestEventPacket eventPacket = this.testSensor.createEventPacket(true,true,true,false,10,10);
        
        boolean result = this.testSensor.sendEvent(eventPacket);
        
        assertFalse(result);
    }
    
}
