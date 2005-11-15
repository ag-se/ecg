/*
 * Classname: ModuleEventTransportTests
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.test.eventtransport;

import junit.framework.TestCase;

import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.test.EventGenerator;

import utmj.threaded.RetriedAssert;

/**
 * Collects testcases that are testing the event transport between
 * connected modules.
 */
public class ModuleEventTransportTests extends TestCase {

    /**
     * Is the delay for a {@link RetriedAssert}.
     */
    private static final int ASSERT_DELAY = 100;

    /**
     * Is the whole time to make the {@link RetriedAssert}.
     */
    private static final int TIME_TO_WAIT = 2000;

    /**
     * Is the length of the module list to test the event transport
     * on.
     */
    private static final int MODULE_LIST_LENGTH = 100;

    /**
     * A reference to the object, which is creating the
     * <em>ModuleSetup</em>.
     */
    private ModuleTestHelper moduleTestHelper;

    /**
     * A reference to the object, which is generating the test events.
     */
    private EventGenerator eventGenerator = null;

    /**
     * @see junit.framework.TestCase#setUp() Creates the
     *      {@link #moduleTestHelper} and the {@link #eventGenerator}.
     */
    @Override
    protected final void setUp() throws Exception {
        super.setUp();

        this.moduleTestHelper = new ModuleTestHelper();

        this.eventGenerator = new EventGenerator();
    }

    /**
     * @see junit.framework.TestCase#tearDown() Releases the
     *      {@link #moduleTestHelper} and the {@link #eventGenerator}.
     */
    @Override
    protected final void tearDown() throws Exception {

        super.tearDown();

        this.moduleTestHelper = null;

        this.eventGenerator = null;
    }

    /**
     * This is testcase MO1 according to the document TESTPLAN version
     * 1.0 or higher. For this test 100 modules are connected to a
     * list of modules. The expected result is that an event, that is
     * passed to the first module in the list, is received by the last
     * module in the list.
     * @throws Exception
     *             If the module connection fails or if the
     *             {@link RetriedAssert} causes an exception
     */
    public final void testEventTransportInListOfModules()
        throws Exception {

        WellFormedEventPacket eventPacket = this.eventGenerator
            .createECGEventPacket(org.electrocodeogram.test.EventGenerator.MicroSensorDataType.CODECHANGE);

        this.moduleTestHelper.makeModuleList(MODULE_LIST_LENGTH);

        int howOftenToReceive = 1;

        this.moduleTestHelper.checkModuleEventTransport(eventPacket,
            howOftenToReceive);

        RetriedAssert ra = new RetriedAssert(TIME_TO_WAIT, ASSERT_DELAY) {

            @SuppressWarnings("synthetic-access")
            @Override
            public void run() throws Exception {
                assertTrue(ModuleEventTransportTests.this.moduleTestHelper.getResult());
            }
        };

        ra.start();

    }

    /**
     * This is testcase MO2 according to the document TESTPLAN version
     * 1.0 or higher. For this test 15 modules are connected to a
     * binary tree of modules. The expected result is that an event,
     * that is passed to the root module in the tree, is received by
     * every leaf module in the tree.
     * @throws Exception
     *             If the module connection fails or if the
     *             {@link RetriedAssert} causes an exception
     */
    public final void testEventTransportInBinaryTreeOfModules()
        throws Exception {

        WellFormedEventPacket eventPacket = this.eventGenerator
            .createECGEventPacket(org.electrocodeogram.test.EventGenerator.MicroSensorDataType.CODECHANGE);

        this.moduleTestHelper.makeModuleBinTree();

        int howOftenToReceive = 8;

        this.moduleTestHelper.checkModuleEventTransport(eventPacket,
            howOftenToReceive);

        RetriedAssert ra = new RetriedAssert(TIME_TO_WAIT, ASSERT_DELAY) {

            @SuppressWarnings("synthetic-access")
            @Override
            public void run() throws Exception {
                assertTrue(ModuleEventTransportTests.this.moduleTestHelper.getResult());
            }
        };

        ra.start();

    }
}
