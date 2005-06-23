package org.electrocodeogram.test;
import org.electrocodeogram.EventPacket;
import org.electrocodeogram.SendingThreadTest;
import org.electrocodeogram.sensor.TestSensor;

import junit.framework.TestCase;

/**
 * Test to validate the behaviour of connection attemps to the ECG server and tests
 * that validate how the SendingThread deals with failing and succeeding connection attemps
 * are collected in this class.
 *
 */
public class ConnectionTests extends TestCase
{

    private TestSensor testSensor = null;
    
    private SendingThreadTest threadTest = null;
    
    public ConnectionTests(String name)
    {
        super(name);
    }
    
    protected void setUp() throws Exception
    {
        super.setUp();
        
        testSensor = new TestSensor();
        
        threadTest = new SendingThreadTest();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        testSensor = null;
        
        threadTest = null;
    }
    
    /**
     * This testcase validates that the SendingThread will stay in the state "not connected" if the connection
     * to the ECG server cannot be established. In this case the ECG server has not been started.
     * The test is succesfull if the SendingThread stays in the "not connected" state even if it gets 
     * new EventPackets to transmit to the ECG server and therefore tires to establish a connection to it.
     *
     */
    public void testA()
    {
        
        assertTrue(threadTest.testConnection(false,0));
        
        EventPacket eventPacket = testSensor.createEventPacket(true,true,true,true,10,10);
        
        testSensor.sendEvent(eventPacket);
        
        assertTrue(threadTest.testConnection(false,100));
    }
    
    /**
     * The SendingThread is expected to continously initiate connection attemps to the ECG server
     * when its buffer contains any EventPackets to submit. This connection attemps are only interrupted
     * by the set connection delay.
     * This testcase is succesfull if the count of initiated connection attemps increases by one after
     * each connection delay period.
     *
     */
    public void testB()
    {
        assertTrue(threadTest.testConnection(false,0));
        
        int connectionDelay = threadTest.getConnectionDelay();
        
        int connectionTrialsBefore = threadTest.getConnectionTrials();
        
        EventPacket eventPacket = testSensor.createEventPacket(true,true,true,true,10,10);
        
        testSensor.sendEvent(eventPacket);
        
        try {
            Thread.sleep(connectionDelay);
        }
        catch (InterruptedException e) {
            
        }
            
        int connectionTrialsAfter = threadTest.getConnectionTrials();
            
        assertTrue(connectionTrialsBefore >= connectionTrialsAfter-1);
     
    }
    
    /**
     * During a connnection delay the Sendingthread is "sleeping". This testcase is succesfull if the
     * SendingThread is still able to receive new EventPackets durong its "sleeping" state.
     *
     */
    public void testC()
    {
        assertTrue(threadTest.testConnection(false,0));
        
        int connectionTrialsBefore = threadTest.getConnectionTrials();
        
        EventPacket eventPacket = testSensor.createEventPacket(true,true,true,true,10,10);
        
        testSensor.sendEvent(eventPacket);

        int bufferSizeBefore = threadTest.getBufferSize();
        
        int count = 0;
        
        while(threadTest.getConnectionTrials()<=connectionTrialsBefore)
        {
            testSensor.sendEvent(eventPacket);
            
            assertTrue(bufferSizeBefore == threadTest.getBufferSize()-1);
            
            bufferSizeBefore = threadTest.getBufferSize();
            
            count++;
        }
     
        assertTrue(count>0);
    }
}
