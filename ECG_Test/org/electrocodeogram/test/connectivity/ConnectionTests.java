package org.electrocodeogram.test.connectivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import junit.framework.TestCase;

import org.electrocodeogram.client.SendingThreadTest;
import org.electrocodeogram.event.EventPacket;
import org.electrocodeogram.sensor.TestSensor;

/**
 * Test to validate the behaviour of connection attemps to the ECG server and
 * tests that validate how the SendingThread deals with failing and succeeding
 * connection attemps are collected in this class.
 * 
 */
public class ConnectionTests extends TestCase
{

    private TestSensor testSensor = null;

    private SendingThreadTest threadTest = null;

    private Process ecgServer = null;
   

    /**
     * This creates the connection testcases
     * 
     * @param name
     *            The name of the testcase to create
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

//    /**
//     * Testcase 1 according to the document TESTPLAN Version 1.0 or higher.
//     * This testcase validates that the SendingThread will stay in the state
//     * "not connected" if the connection to the ECG server cannot be
//     * established. In this case the ECG server has not been started. The
//     * test is succesfull if the SendingThread stays in the "not connected"
//     * state even if it gets new EventPackets to transmit to the ECG server
//     * and therefore tires to establish a connection to it.
//     * 
//     */
//    public void testStayNotConnectedIfServerIsDown()
//    {
//
//        assertTrue(this.threadTest.testConnection(false, 0));
//
//        EventPacket eventPacket = this.testSensor.createEventPacket(true, true, true, true, 10, 10);
//
//        this.testSensor.sendEvent(eventPacket);
//
//        assertTrue(this.threadTest.testConnection(false, 100));
//    }


//    public void testConnectionEstablishedAfterServerIsStarted()
//    {
//        assertTrue(this.threadTest.testConnection(false, 0));
//        
//        EventPacket eventPacket = this.testSensor.createEventPacket(true, true, true, true, 10, 10);
//
//        this.testSensor.sendEvent(eventPacket);
//
//        assertTrue(this.threadTest.testConnection(false, 100));
//        
//        try {
//            startECGServerComponent();
//        }
//        catch (IOException e) {
//            fail();
//        }
//        
//        //assertTrue(this.threadTest.testConnection(true, 100));
//        
//        try {
//            stopECGServerComponent();
//        }
//        catch (IOException e) {
//            fail();
//        }
//    }
//    
//    public void testConnectionIsLostAfterServerIsShutDown()
//    {
//        EventPacket eventPacket = this.testSensor.createEventPacket(true, true, true, true, 10, 10);
//        
//        assertTrue(this.threadTest.testConnection(false, 0));
//        
//        SensorShellWrapper.getInstance();
//        
//        assertTrue(this.threadTest.testConnection(true, 100));
//        
//        SensorShellWrapper.getInstance().shutDown();
//        
//        this.testSensor.sendEvent(eventPacket);
//        
//        assertTrue(this.threadTest.testConnection(false, 100));
//    }
    
//    /**
//     * Testcase 2 according to the document TESTPLAN Version 1.0 or higher.
//     * The SendingThread is expected to continously initiate connection
//     * attemps to the ECG server when its buffer contains any EventPackets
//     * to submit. This connection attemps are only interrupted by the set
//     * connection delay. This testcase is succesfull if the count of
//     * initiated connection attemps increases by one after each connection
//     * delay period.
//     * 
//     */
//    public void testConnectionTrialsAreIncreasing()
//    {
//        assertTrue(this.threadTest.testConnection(false, 0));
//
//        int connectionDelay = this.threadTest.getConnectionDelay();
//
//        int connectionTrialsBefore = this.threadTest.getConnectionTrials();
//
//        EventPacket eventPacket = this.testSensor.createEventPacket(true, true, true, true, 10, 10);
//
//        this.testSensor.sendEvent(eventPacket);
//
//        try {
//            Thread.sleep(connectionDelay);
//        }
//        catch (InterruptedException e) {
//            // This is not a problem
//        }
//
//        int connectionTrialsAfter = this.threadTest.getConnectionTrials();
//
//        assertTrue(connectionTrialsBefore >= connectionTrialsAfter - 1);
//
//    }
//
//    /**
//     * Testcase 3 according to the document TESTPLAN Version 1.0 or higher.
//     * During a connnection delay the Sendingthread is "sleeping". This
//     * testcase is succesfull if the SendingThread is still able to receive
//     * new EventPackets durong its "sleeping" state.
//     * 
//     */
//    public void testDelayedSendingThreadAcceptsNewEvents()
//    {
//        assertTrue(this.threadTest.testConnection(false, 0));
//
//        int connectionTrialsBefore = this.threadTest.getConnectionTrials();
//
//        EventPacket eventPacket = this.testSensor.createEventPacket(true, true, true, true, 10, 10);
//
//        this.testSensor.sendEvent(eventPacket);
//
//        int bufferSizeBefore = this.threadTest.getBufferSize();
//
//        int count = 0;
//
//        while (this.threadTest.getConnectionTrials() <= connectionTrialsBefore) {
//            this.testSensor.sendEvent(eventPacket);
//
//            assertTrue(bufferSizeBefore == this.threadTest.getBufferSize() - 1);
//
//            bufferSizeBefore = this.threadTest.getBufferSize();
//
//            count++;
//        }
//
//        assertTrue(count > 0);
//
//    }
//
//    /**
//     * Testcase 4 according to the document TESTPLAN Version 1.0 or higher.
//     * After the ECG server is started a connection to it should be established
//     * and all queued EventPackets should be send to the ECG server. This
//     * testcase succeeds if exactly this happens.
//     * 
//     */
//    public void testAfterConnectionBufferIsEmptied()
//    {
//        
//            try {
//                Thread.sleep(5000);
//            }
//            catch (InterruptedException e1) {
//                // TODO Auto-generated catch block
//                e1.printStackTrace();
//            }
//            
//            
//        
//        assertTrue(this.threadTest.testConnection(false, 0));
//
//        EventPacket eventPacket = this.testSensor.createEventPacket(true, true, true, true, 10, 10);
//
//        int size = this.threadTest.getBufferSize();
//
//        if (size == 0) {
//            while (size <= 10) {
//                this.testSensor.sendEvent(eventPacket);
//
//                size = this.threadTest.getBufferSize();
//            }
//
//            assertTrue(size == 11);
//
//        }
//
//        SensorShellWrapper.getInstance();
//        
//        //startECGServerCode();
//
//        try {
//            Thread.sleep(10000);
//
//            assertTrue(this.threadTest.testConnection(true, 0));
//
//            size = this.threadTest.getBufferSize();
//
//            assertTrue(size == 0);
//        }
//        catch (InterruptedException e) {
//
//            e.printStackTrace();
//        }
//
//        SensorShellWrapper.getInstance().shutDown();
//        
//        //stopECGServerCode();
//    }

    private void stopECGServerComponent() throws IOException
    {
        if (this.ecgServer == null)
        {
            return;
        }
        
        OutputStream out =  ecgServer.getOutputStream();
        
        BufferedWriter writer = new BufferedWriter(new PrintWriter(out));
      
        writer.write("quit");
    }

    /*
     * This method is calle by the testcases to start the ECG server from the
     * binary jar-file component.
     * 
     */
    private void startECGServerComponent() throws IOException
    {
        if (this.ecgServer == null)
        {
        this.ecgServer = Runtime.getRuntime().exec("javac");
        
        new ConsoleReader(this.ecgServer).start();
        
        }
    }
    
    private class ConsoleReader extends Thread
    {
        private Process process = null;
        
        private BufferedReader in = null;
        
        public ConsoleReader(Process processPar)
        {
            this.process = processPar;
            
            in = new BufferedReader(new InputStreamReader(this.process.getInputStream()));
        }
        
       
        @Override
        public void run()
        {
            while(true)
            {
                String line = null;
                
                try {
                    if((line = this.in.readLine()) != null)
                    {
                        System.out.println(line);
                    }
                }
                catch (IOException e) {
                   ;
                }
            }
        }
    }

}
