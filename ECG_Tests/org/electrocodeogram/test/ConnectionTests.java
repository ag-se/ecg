package org.electrocodeogram.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.swing.text.StyleConstants.CharacterConstants;

import junit.framework.TestCase;

import org.electrocodeogram.client.SendingThreadTest;
import org.electrocodeogram.core.SensorServer;
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

    private ECGServerManager manager = null;

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

        this.manager = new ECGServerManager();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();

        this.testSensor = null;

        this.threadTest = null;

        this.manager = null;
    }

    /**
     * Testcase 1 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase validates that the SendingThread will stay in the state
     * "not connected" if the connection to the ECG server cannot be
     * established. In this case the ECG server has not been started. The
     * test is succesfull if the SendingThread stays in the "not connected"
     * state even if it gets new EventPackets to transmit to the ECG server
     * and therefore tires to establish a connection to it.
     * 
     */
    public void testStayNotConnectedIfServerIsDown()
    {

        assertTrue(this.threadTest.testConnection(false, 0));

        EventPacket eventPacket = this.testSensor.createEventPacket(true, true, true, true, 10, 10);

        this.testSensor.sendEvent(eventPacket);

        assertTrue(this.threadTest.testConnection(false, 100));
    }

    /**
     * Testcase 2 according to the document TESTPLAN Version 1.0 or higher.
     * The SendingThread is expected to continously initiate connection
     * attemps to the ECG server when its buffer contains any EventPackets
     * to submit. This connection attemps are only interrupted by the set
     * connection delay. This testcase is succesfull if the count of
     * initiated connection attemps increases by one after each connection
     * delay period.
     * 
     */
    public void testConnectionTrialsAreIncreasing()
    {
        assertTrue(this.threadTest.testConnection(false, 0));

        int connectionDelay = this.threadTest.getConnectionDelay();

        int connectionTrialsBefore = this.threadTest.getConnectionTrials();

        EventPacket eventPacket = this.testSensor.createEventPacket(true, true, true, true, 10, 10);

        this.testSensor.sendEvent(eventPacket);

        try {
            Thread.sleep(connectionDelay);
        }
        catch (InterruptedException e) {
            // This is not a problem
        }

        int connectionTrialsAfter = this.threadTest.getConnectionTrials();

        assertTrue(connectionTrialsBefore >= connectionTrialsAfter - 1);

    }

    /**
     * Testcase 3 according to the document TESTPLAN Version 1.0 or higher.
     * During a connnection delay the Sendingthread is "sleeping". This
     * testcase is succesfull if the SendingThread is still able to receive
     * new EventPackets durong its "sleeping" state.
     * 
     */
    public void testDelayedSendingThreadAcceptsNewEvents()
    {
        assertTrue(this.threadTest.testConnection(false, 0));

        int connectionTrialsBefore = this.threadTest.getConnectionTrials();

        EventPacket eventPacket = this.testSensor.createEventPacket(true, true, true, true, 10, 10);

        this.testSensor.sendEvent(eventPacket);

        int bufferSizeBefore = this.threadTest.getBufferSize();

        int count = 0;

        while (this.threadTest.getConnectionTrials() <= connectionTrialsBefore) {
            this.testSensor.sendEvent(eventPacket);

            assertTrue(bufferSizeBefore == this.threadTest.getBufferSize() - 1);

            bufferSizeBefore = this.threadTest.getBufferSize();

            count++;
        }

        assertTrue(count > 0);

    }

    /**
     * Testcase 4 according to the document TESTPLAN Version 1.0 or higher.
     * After the ECG server is started a connection to it should be established
     * and all queued EventPackets should be send to the ECG server. This
     * testcase succeeds if exactly this happens.
     * 
     */
    public void testAfterConnectionBufferIsEmptied()
    {
        assertTrue(this.threadTest.testConnection(false, 0));

        EventPacket eventPacket = this.testSensor.createEventPacket(true, true, true, true, 10, 10);

        int size = this.threadTest.getBufferSize();

        if (size == 0) {
            while (size <= 10) {
                this.testSensor.sendEvent(eventPacket);

                size = this.threadTest.getBufferSize();
            }

            assertTrue(size == 11);

        }

        startECGServerCode();

        try {
            Thread.sleep(10000);

            assertTrue(this.threadTest.testConnection(true, 0));

            size = this.threadTest.getBufferSize();

            assertTrue(size == 0);
        }
        catch (InterruptedException e) {

            e.printStackTrace();
        }

    }

    /*
     * This method is calle by the testcases to start the ECG server from the
     * binary jar-file component.
     * 
     */
    private void startECGServerComponent()
    {

        if (this.manager == null)
            return;

        try {
            this.manager.startECGServerComponent();
        }
        catch (IOException e) {

            e.printStackTrace();
        }

        this.manager.start();
    }

    /*
     * This method is called by the testcases to start the ECG server from the
     * main-method of the SensorServer class.
     * 
     */
    private void startECGServerCode()
    {
        if (this.manager == null)
            return;

        this.manager.startECGServerCode();

        this.manager.start();
    }

    /*
     * This method is called by the testcases to start the ECG server from the
     * main-method of the SensorServer class.
     * 
     */
    private void stopECGServerCode()
    {
        if (this.manager == null)
            return;

        this.manager = null;
        
        

    }
    
    // this Thread is used to run the ECG server asynchrneously
    private class ECGServerManager extends Thread
    {

        private Process ecgServer = null;

        private BufferedReader console = null;

        private boolean stop = false;

        private boolean component;

        /**
         * This is called to stop the Thread.
         * 
         */
        public void stopMe()
        {
            this.stop = true;
        }

        /**
         * This method starts the ElectroCodeoGram.jar file containing the ECG
         * server and ECG Lab by calling a batch file.
         * 
         * @throws IOException
         *             If the batch file can not be found
         */
        public void startECGServerComponent() throws IOException
        {
            if (this.ecgServer == null) {

                this.component = true;

                ProcessBuilder pb = new ProcessBuilder("startECGServer.bat", "");

                pb.redirectErrorStream();

                this.ecgServer = pb.start();

                InputStream s = this.ecgServer.getInputStream();

                InputStreamReader r = new InputStreamReader(s);

                this.console = new BufferedReader(r);

            }
        }

        /**
         * Thi method starts the ECG server by directly calling it main-method.
         * 
         */
        public void startECGServerCode()
        {
            this.component = false;
        }

        /**
         * @see java.lang.Thread#run() This starts the ECG server.
         */
        @Override
        public void run()
        {
            while (!this.stop) {
                if (this.component) {
                    if (this.console != null) {
                        String line;

                        try {
                            while ((line = this.console.readLine()) != null) {

                                System.out.println(line);
                            }
                        }
                        catch (IOException e) {

                            e.printStackTrace();
                        }
                    }
                }
                else {
                    SensorServer.main(null);

                    this.stopMe();
                }
            }

        }
    }
}
