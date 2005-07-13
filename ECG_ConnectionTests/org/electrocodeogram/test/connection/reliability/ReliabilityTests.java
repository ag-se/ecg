package org.electrocodeogram.test.connection.reliability;

import java.io.IOException;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.test.EventGenerator;
import org.electrocodeogram.test.NoTestDataException;
import org.electrocodeogram.test.client.mocksensor.MockSensor;
import org.electrocodeogram.test.connection.mockserver.MockSensorshellWrapper;

import junit.framework.TestCase;

import utmj.threaded.RetriedAssert;

/**
 * This class collects testcases that are concerning the reliability of the connection
 * between the ECG SensorShell (client) and the ECG Server&Lab.
 * 
 */
public class ReliabilityTests extends TestCase
{

    private static MockSensorshellWrapper mockSensorShellWrapper = null;

    private static MockSensor mockSensor = null;

    private static EventGenerator eventGenerator = null;

    private int counter = 0;

    private ValidEventPacket testPacket = null;

    private int line = -1;

    /**
     * This creates a testcase with the given name.
     * @param name Is the name of the testcase to create
     * @param linePar Is the linenumber according to the file "pseudorandom.strings", which provides the testdata
     * @throws IOException If such an Exception is thrown during the use of the file "pseudorandom.strings"
     */
    public ReliabilityTests(String name, int linePar) throws IOException
    {
        super(name);

        this.line = linePar;

        if (eventGenerator == null) {
            eventGenerator = new EventGenerator();
        }

        if (mockSensor == null) {
            mockSensor = new MockSensor();
        }

        if (mockSensorShellWrapper == null) {
            mockSensorShellWrapper = new MockSensorshellWrapper();
        }
    }

    /**
     * This method returns a the value of a field that is used to store the
     * number of received EventPackets by the server.
     * @return The number of received EventPackets by the server
     */
    public int getCounter()
    {
        return this.counter;
    }

    /**
     * This method returns the reference to the MockServer object.
     * @return The reference to the MockServer object
     */
    public MockSensorshellWrapper getMockSensorShellWrapper()
    {
        return mockSensorShellWrapper;

    }

    /**
     * This method returns the EventPacket that was used for the last test.
     * @return The EventPacket that was used for the last test
     */
    public ValidEventPacket getTestPacket()
    {
        return this.testPacket;
    }

    /**
     * This testcase is successfull if an EventPacket that is sended to the server
     * brings the server to receive one EventPacket.
     * @throws IllegalEventParameterException If the parameters passed to the event creating method are not legal
     * @throws NoTestDataException If a pseudorandom String is requested by a line number that is not available or if the requested String size is to higher then available
     */
    public void testIfNoEventsAreLost() throws IllegalEventParameterException, NoTestDataException
    {
        ValidEventPacket eventPacket = eventGenerator.createValidEventPacket(true, true, this.line, true, true, 10, 10);

        this.counter = mockSensorShellWrapper.getReceivingCount();

        mockSensor.sendEvent(eventPacket);

        new RetriedAssert(5000, 100) {

            @Override
            public void run() throws Exception
            {
                assertEquals(getMockSensorShellWrapper().getReceivingCount(), getCounter() + 1);

            }
        };
    }

    /**
     * This testcase is successsfull if an EventPacket is received by the server without any modifications to
     * its contained event data.
     * @throws IllegalEventParameterException If the parameters passed to the event creating method are not legal
     * @throws NoTestDataException If a pseudorandom String is requested by a line number that is not available or if the requested String size is to higher then available
     */
    public void testIfEventPacketsAreNotAltered() throws IllegalEventParameterException, NoTestDataException
    {
        ValidEventPacket eventPacket = eventGenerator.createValidEventPacket(true, true, this.line, true, true, 10, 10);

        mockSensorShellWrapper.getReceivedEventPacket();

        assertNull(mockSensorShellWrapper.getReceivedEventPacket());

        mockSensor.sendEvent(eventPacket);

        new RetriedAssert(5000, 100) {

            @Override
            public void run() throws Exception
            {
                assertEquals(getMockSensorShellWrapper().getReceivedEventPacket().getTimeStamp(), getTestPacket().getTimeStamp());

                assertEquals(getMockSensorShellWrapper().getReceivedEventPacket().getHsCommandName(), getTestPacket().getHsCommandName());

                assertEquals(getMockSensorShellWrapper().getReceivedEventPacket().getArglist().size(), getTestPacket().getArglist().size());

                int size = getTestPacket().getArglist().size();

                for (int i = 0; i < size; i++) {
                    assertEquals(getMockSensorShellWrapper().getReceivedEventPacket().getArglist().get(i), getTestPacket().getArglist().get(i));
                }

            }
        };
    }
}
