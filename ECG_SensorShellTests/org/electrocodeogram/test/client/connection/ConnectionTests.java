package org.electrocodeogram.test.client.connection;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import junit.framework.TestCase;

import org.electrocodeogram.client.SendingThreadTest;
import org.electrocodeogram.event.EventPacket;
import org.electrocodeogram.test.EventGenerator;
import org.electrocodeogram.test.NoTestDataException;
import org.electrocodeogram.test.client.mocksensor.MockSensor;

import utmj.threaded.RetriedAssert;

/**
 * Test to validate the behaviour of connection attemps to the ECG server and
 * tests that validate how the SendingThread deals with failing and succeeding
 * connection attemps are collected in this class.
 * 
 */
public class ConnectionTests extends TestCase
{

    private MockSensor testSensor = null;

    private SendingThreadTest threadTest = null;

    private Process ecgServer = null;

    private EventGenerator eventGenerator = null;

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

        this.testSensor = new MockSensor();

        this.threadTest = new SendingThreadTest();

        this.eventGenerator = new EventGenerator();

    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();

        this.testSensor = null;

        this.threadTest = null;

        this.eventGenerator = null;

        stopECGServer();

    }

    

    /**
     * Testcase CO1 according to the document TESTPLAN Version 1.0 or higher. The
     * SendingThread is expected to continously initiate connection attemps to
     * the ECG server when its buffer contains any EventPackets to submit. This
     * connection attemps are only interrupted by the set connection delay. This
     * testcase is succesfsull if the count of initiated connection attemps
     * increases by one after each connection delay period.
     * @throws InterruptedException If this Thread is interruptedduring its sleeping period
     * @throws NoTestDataException If a pseudorandom String is requested by a line number that is not available or if the requested String size is to higher then available
     * 
     */
    public void testConnectionTrialsAreIncreasing() throws InterruptedException, NoTestDataException
    {
        assertTrue(this.threadTest.testConnection(false, 0));

        int connectionDelay = this.threadTest.getConnectionDelay();

        int connectionTrialsBefore = this.threadTest.getConnectionTrials();

        EventPacket eventPacket = this.eventGenerator.createEventPacket(true, true, 0, true, true, 10, 10);

        this.testSensor.sendEvent(eventPacket);

        Thread.sleep(connectionDelay);

        int connectionTrialsAfter = this.threadTest.getConnectionTrials();

        assertTrue(connectionTrialsBefore >= connectionTrialsAfter - 1);

    }

    /**
     * Testcase CO2 according to the document TESTPLAN Version 1.0 or higher.
     * During a connnection delay the Sendingthread is "sleeping". This testcase
     * is succesfull if the SendingThread is still able to receive new
     * EventPackets durong its "sleeping" state.
     * @throws NoTestDataException If a pseudorandom String is requested by a line number that is not available or if the requested String size is to higher then available
     * 
     */
    public void testDelayedSendingThreadAcceptsNewEvents() throws NoTestDataException
    {
        assertTrue(this.threadTest.testConnection(false, 0));

        int connectionTrialsBefore = this.threadTest.getConnectionTrials();

        EventPacket eventPacket = this.eventGenerator.createEventPacket(true, true, 0, true, true, 10, 10);

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
     * Testcase CO3 according to the document TESTPLAN Version 1.0 or higher.
     * After the ECG server is started a connection to it should be established
     * and all queued EventPackets should be send to the ECG server. This
     * testcase succeeds if exactly this happens.
     * @throws Exception If the RetriedAssert Thread causes an Exception
     * 
     */
    public void testAfterConnectionBufferIsEmptied() throws Exception
    {

        assertTrue(this.threadTest.testConnection(false, 0));

        EventPacket eventPacket = this.eventGenerator.createEventPacket(true, true, 0, true, true, 10, 10);

        int size = this.threadTest.getBufferSize();

        if (size == 0) {
            while (size <= 10) {
                this.testSensor.sendEvent(eventPacket);

                size = this.threadTest.getBufferSize();
            }

            assertTrue(size == 11);

        }

        startECGServer("..\\ECG_ServerAndLab\\application");

        new RetriedAssert(10000, 1000) {
            @Override
            public void run() throws Exception
            {
                assertEquals(threadTest.getBufferSize(), 0);
            }
        }.start();

        stopECGServer();
    }

    /*
     * This method starts the ECG Server & Lab component by invoking the jar
     * file ElectroCodeoGram.jar, which is located inside the "ECGServer"
     * subdirectory in the ECG_Test basedirectory.
     * 
     * After the ECG Server & lab component has been started, output of them is
     * written to the console.
     */
    private void startECGServer(String path) throws IOException
    {

        if (this.ecgServer != null) {
            return;
        }

        Runtime rt = Runtime.getRuntime();

        Process process = rt.exec("java -jar " + path + File.separator + "ECGLab.jar " + path + File.separator + "modules");

        ConsoleReader errorGobbler = new ConsoleReader(
                process.getErrorStream(), "ERROR");

        ConsoleReader outputGobbler = new ConsoleReader(
                process.getInputStream(), "OUTPUT");

        errorGobbler.start();

        outputGobbler.start();

        this.ecgServer = process;

    }

    /*
     * This methos sends a single 'quit' String to the standard inpuzt of the
     * running ECG Server & Lab process and causes it to quit.
     */
    private void stopECGServer()
    {
        if (this.ecgServer == null) {
            return;
        }

        PrintWriter toProcess = new PrintWriter(
                this.ecgServer.getOutputStream());

        toProcess.println("quit");

        toProcess.flush();
    }

    /**
     * The ConsoleReader Thread continously reads from an InputStream and prints
     * out all input to the console. It is used for getting the ECG Server & Lab
     * process' output.
     */
    private class ConsoleReader extends Thread
    {
        private InputStream fromProcess = null;

        // either stdError or stdOut
        private String type = null;

        /**
         * The construcot creates the ConsoeReader and associates it with an
         * actual InputString to read from. It also sets the outputprefix to the
         * String 'type' + ">".
         * 
         * @param inputStreamPar
         *            is the InputStream to read from
         * @param typePar
         *            is the type of the according OutputStream. Either stdError
         *            or stdOut
         */
        public ConsoleReader(InputStream inputStreamPar, String typePar)
        {
            this.fromProcess = inputStreamPar;
            this.type = typePar;
        }

        /**
         * The actual reading and printing out of the InputStream's is done
         * within this Thread's run method.
         */
        @Override
        public void run()
        {
            try {

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        this.fromProcess));

                String line = null;

                while ((line = br.readLine()) != null)

                    System.out.println(this.type + ">" + line);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This Thread continously reads input from stdIn and writes it out to the
     * given OutputStream. It is used for sending commands to the ECG Server &
     * Lab process.
     * 
     */
    private class ConsoleWriter extends Thread
    {
        OutputStream toProcess;

        /**
         * The constructor creates a ConsoleWriter and associates it with an
         * actual OutputStrean.
         * 
         * @param outputStreamPar
         *            is the OutputStream to write to
         */
        public ConsoleWriter(OutputStream outputStreamPar)
        {
            this.toProcess = outputStreamPar;
        }

        /**
         * The reading from stdIn and writing to the OutputStream is done here.
         */
        @Override
        public void run()
        {
            try {
                PrintWriter pw = new PrintWriter(this.toProcess);

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        System.in));

                String line = null;

                while (true) {
                    line = br.readLine();

                    pw.println(line);

                    pw.flush();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
