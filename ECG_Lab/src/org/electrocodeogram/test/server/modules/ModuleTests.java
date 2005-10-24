package org.electrocodeogram.test.server.modules;

import junit.framework.TestCase;

import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.test.EventGenerator;

import utmj.threaded.RetriedAssert;

/**
 * This class collects testcases that are testing the transportation of events between
 * connected modules. 
 *
 */
public class ModuleTests extends TestCase {

    ModuleTestHelper _moduleTestHelper = null;

    private EventGenerator _eventGenerator = null;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        this._moduleTestHelper = new ModuleTestHelper();

        this._eventGenerator = new EventGenerator();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {

        super.tearDown();

        this._moduleTestHelper = null;

        this._eventGenerator = null;
    }

    /**
     * This is testcase MO1 according to the document TESTPLAN version 1.0 or higher.
     * For this test 100 modules are connected as a list. The testcase is successful
     * if an event, that is passed to the first module, is received by the last module.  
     * @throws Exception If the module connection fails or if the RetriedAssert Thread causes an Exception
     *
     */
    public void testEventTransportInHundredNodeModuleList() throws Exception {

        WellFormedEventPacket eventPacket = this._eventGenerator
            .createECGEventPacket(org.electrocodeogram.test.EventGenerator.MicroSensorDataType.CODECHANGE);

        this._moduleTestHelper.makeModuleList(100);

        this._moduleTestHelper.checkModuleEventTransport(eventPacket, 1);

        new RetriedAssert(50000, 100) {

            @Override
            public void run() throws Exception {
                assertTrue(ModuleTests.this._moduleTestHelper.getResult());
            }
        }.start();

    }

    /**
     * This is testcase MO1 according to the document TESTPLAN version 1.0 or higher.
     * For this test 100 modules are connected as a list. The testcase is successfull
     * if an event, that is passed to the first module, is received by the last module.  
     * @throws Exception If the module connection fails or if the RetriedAssert Thread causes an Exception
     *
     */
    public void testEventTransportInFifteenNodeModuleTree() throws Exception {

        WellFormedEventPacket eventPacket = this._eventGenerator
            .createECGEventPacket(org.electrocodeogram.test.EventGenerator.MicroSensorDataType.CODECHANGE);

        this._moduleTestHelper.makeModuleBinTree();

        this._moduleTestHelper.checkModuleEventTransport(eventPacket, 8);

        new RetriedAssert(5000, 100) {

            @Override
            public void run() throws Exception {
                assertTrue(ModuleTests.this._moduleTestHelper.getResult());
            }
        }.start();

    }
}
