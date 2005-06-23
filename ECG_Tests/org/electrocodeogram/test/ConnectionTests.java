package org.electrocodeogram.test;
import junit.framework.TestCase;

import org.electrocodeogram.SendingThreadTest;
import org.electrocodeogram.TestEventPacket;
import org.electrocodeogram.core.SensorServer;
import org.electrocodeogram.sensor.TestSensor;

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
    
    /**
     * This creates the connection testcases
     * @param name The name of the testcase to create
     */
    public ConnectionTests(String name)
    {
        super(name);
    }
    
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.testSensor = new TestSensor();
        
        this.threadTest = new SendingThreadTest();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        this.testSensor = null;
        
        this.threadTest = null;
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
        
        assertTrue(this.threadTest.testConnection(false,0));
        
        TestEventPacket eventPacket = this.testSensor.createEventPacket(true,true,true,true,10,10);
        
        this.testSensor.sendEvent(eventPacket);
        
        assertTrue(this.threadTest.testConnection(false,100));
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
        assertTrue(this.threadTest.testConnection(false,0));
        
        int connectionDelay = this.threadTest.getConnectionDelay();
        
        int connectionTrialsBefore = this.threadTest.getConnectionTrials();
        
        TestEventPacket eventPacket = this.testSensor.createEventPacket(true,true,true,true,10,10);
        
        this.testSensor.sendEvent(eventPacket);
        
        try {
            Thread.sleep(connectionDelay);
        }
        catch (InterruptedException e) {
             // This is not a problem
        }
            
        int connectionTrialsAfter = this.threadTest.getConnectionTrials();
            
        assertTrue(connectionTrialsBefore >= connectionTrialsAfter-1);
     
    }
    
    /**
     * During a connnection delay the Sendingthread is "sleeping". This testcase is succesfull if the
     * SendingThread is still able to receive new EventPackets durong its "sleeping" state.
     *
     */
    public void testC()
    {
        assertTrue(this.threadTest.testConnection(false,0));
        
        int connectionTrialsBefore = this.threadTest.getConnectionTrials();
        
        TestEventPacket eventPacket = this.testSensor.createEventPacket(true,true,true,true,10,10);
        
        this.testSensor.sendEvent(eventPacket);

        int bufferSizeBefore = this.threadTest.getBufferSize();
        
        int count = 0;
        
        while(this.threadTest.getConnectionTrials()<=connectionTrialsBefore)
        {
            this.testSensor.sendEvent(eventPacket);
            
            assertTrue(bufferSizeBefore == this.threadTest.getBufferSize()-1);
            
            bufferSizeBefore = this.threadTest.getBufferSize();
            
            count++;
        }
     
        assertTrue(count>0);
    }
    
    /**
     * After the ECG server is started a connection to it should be established and
     * all queued EventPackets should be send to the ECG server. This testcase
     * succeeds if exactly this happens.
     *
     */
    public void testD()
    {
        assertTrue(this.threadTest.testConnection(false,0));
        
        TestEventPacket eventPacket = this.testSensor.createEventPacket(true,true,true,true,10,10);
        
        while(this.threadTest.getBufferSize() <= 10)
        {
            this.testSensor.sendEvent(eventPacket);
        }
        
        assertTrue(this.threadTest.getBufferSize() > 10);
        
        startECGServer();
        
        try {
            Thread.sleep(10000);

            assertTrue(this.threadTest.testConnection(true,0));
            
            assertTrue(this.threadTest.getBufferSize() == 0);
        }
        catch (InterruptedException e) {
         
            
            e.printStackTrace();
        }
        
        
        
    }
    
    private void startECGServer()
    {
        new ECGServerStarter().start();
    }
    
    // this Thread is used to run the ECG server asynchrneously
    private class ECGServerStarter extends Thread
    {
    
        /**
         * @see java.lang.Thread#run()
         * This starts the ECG server.
         */
        @Override
        public void run()
        {
            SensorServer.main(null);
        }
    }
}

    
