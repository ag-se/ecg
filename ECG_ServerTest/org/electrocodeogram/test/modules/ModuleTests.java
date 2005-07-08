package org.electrocodeogram.test.modules;

import java.io.IOException;

import junit.framework.TestCase;

import org.electrocodeogram.core.TestModuleTransportModule;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.module.ModuleConnectionException;
import org.electrocodeogram.test.EventGenerator;
import org.electrocodeogram.test.EventGenerator.SensorDataType;

import utmj.threaded.RetriedAssert;

/**
 * This class collects testcases that are testing the transportation of events between
 * connected modules. 
 *
 */
public class ModuleTests extends TestCase
{
    private TestModuleTransportModule testModuleTransport = null;

    private EventGenerator eventGenerator = null;

    @Override
    protected void setUp() throws IOException
    {
        this.testModuleTransport = new TestModuleTransportModule();

        this.eventGenerator = new EventGenerator();
    }

    @Override
    protected void tearDown()
    {
        this.testModuleTransport = null;

        this.eventGenerator = null;
    }

    /**
     * This is testcase MO1 according to the document TESTPLAN version 1.0 or higher.
     * For this test 100 modules are connected as a list. The testcase is successfull
     * if an event, that is passed to the first module, is received by the last module.  
     * @throws Exception If the module connection fails or if the RetriedAssert Thread causes an Exception
     *
     */
    public void testEventTransportInModuleList() throws Exception
    {

        ValidEventPacket eventPacket = this.eventGenerator.createHackyStatEventPacket(SensorDataType.ACTIVITY,0);

        this.testModuleTransport.makeModuleList(100);

        this.testModuleTransport.checkModuleEventTransport(eventPacket);

        new RetriedAssert(5000, 100) {
            @Override
            public void run() throws Exception
            {
                assertTrue(testModuleTransport.getResult());
            }
        }.start();

    }
}
