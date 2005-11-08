package org.electrocodeogram.test.eventtransport;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.ModuleActivationException;
import org.electrocodeogram.module.ModuleConnectionException;
import org.electrocodeogram.test.module.TestModule;

/**
 * Is helper class for the
 * {@link org.electrocodeogram.test.eventtransport.ModuleEventTransportTests}.
 * It provides methods to create graphs of connected modules and to
 * send,receive and compare events on the modules.
 */
public class ModuleTestHelper {

    /**
     * A reference to the event that is beeing transported.
     */
    private WellFormedEventPacket testPacket;

    /**
     * This is either <code>true</code>, if the same event has been
     * received, or <code>false</code>, if not.
     */
    private boolean result;

    /**
     * Tells how often the same event has to be received by the
     * {@link #leafModule}. Is 1 for the list and 8 for the binary
     * tree for example.
     */
    private int howOftenToReceive;

    /**
     * Is the number of times the same event has been received. As you
     * expect, this has to be the same as {@link #howOftenToReceive}
     * for a test to be successfull.
     */
    private int receivedCount;

    /**
     * A reference to the module which is sending the event initially.
     */
    private MockSourceModule rootModule;

    /**
     * A reference to the module which is receiving the events.
     */
    private MockTargetModule leafModule;

    /**
     * Creates the class as a module.
     */
    public ModuleTestHelper() {

        this.rootModule = new MockSourceModule();

        this.leafModule = new MockTargetModule(this);

    }

    // /**
    // * This is cleaning up by deleting all connected modules before
    // * a new module graph is created.
    // * @param node
    // */
    // private void removeConnectedModules(final Module node) {
    // Module[] connectedModules = node.getReceivingModules();
    //
    // for (Module module : connectedModules) {
    // removeConnectedModules(module);
    //
    // module.remove();
    // }
    // }

    /**
     * Creates a list of connected {@link TestModule}.
     * @param length
     *            The number of modules to connect
     * @throws ModuleConnectionException
     *             If an exception occures while connecting two
     *             modules.
     * @throws ModuleActivationException
     *             If a module can not be activated
     */
    public final void makeModuleList(final int length)
        throws ModuleConnectionException, ModuleActivationException {

        Module lastModule = null;

        for (int i = 0; i < length; i++) {

            Module next = new TestModule();

            if (i == 0) {

                this.rootModule.connectReceiverModule(next);
            } else {

                lastModule.connectReceiverModule(next);
            }

            next.activate();

            lastModule = next;
        }

        lastModule.connectReceiverModule(this.leafModule);

        this.rootModule.activate();

        this.leafModule.activate();
    }

    /**
     * Creates a binary tree of connected {@link TestModule}.
     * @throws ModuleConnectionException
     *             If an exception occures while connecting two
     *             modules.
     * @throws ModuleActivationException
     *             If a module can not be activated
     */
    public final void makeModuleBinTree() throws ModuleConnectionException,
        ModuleActivationException {

        Module leftChild = new TestModule();

        leftChild.activate();

        Module rightChild = new TestModule();

        rightChild.activate();

        this.rootModule.connectReceiverModule(leftChild);

        this.rootModule.connectReceiverModule(rightChild);

        Module leftleftChild = new TestModule();

        leftleftChild.activate();

        Module leftrightChild = new TestModule();

        leftrightChild.activate();

        Module rightleftChild = new TestModule();

        rightleftChild.activate();

        Module rightrightChild = new TestModule();

        rightrightChild.activate();

        leftChild.connectReceiverModule(leftleftChild);

        leftChild.connectReceiverModule(leftrightChild);

        rightChild.connectReceiverModule(rightleftChild);

        rightChild.connectReceiverModule(rightrightChild);

        Module leftleftleftChild = new TestModule();

        leftleftleftChild.activate();

        Module leftleftrightChild = new TestModule();

        leftleftrightChild.activate();

        Module leftrightleftChild = new TestModule();

        leftrightleftChild.activate();

        Module leftrightrightChild = new TestModule();

        leftrightrightChild.activate();

        Module rightleftleftChild = new TestModule();

        rightleftleftChild.activate();

        Module rightleftrightChild = new TestModule();

        rightleftrightChild.activate();

        Module rightrightleftChild = new TestModule();

        rightrightleftChild.activate();

        Module rightrightrightChild = new TestModule();

        rightrightrightChild.activate();

        leftleftChild.connectReceiverModule(leftleftleftChild);

        leftleftChild.connectReceiverModule(leftleftrightChild);

        leftrightChild.connectReceiverModule(leftrightleftChild);

        leftrightChild.connectReceiverModule(leftrightrightChild);

        rightleftChild.connectReceiverModule(rightleftleftChild);

        rightleftChild.connectReceiverModule(rightleftrightChild);

        rightrightChild.connectReceiverModule(rightrightleftChild);

        rightrightChild.connectReceiverModule(rightrightrightChild);

        leftleftleftChild.connectReceiverModule(this.leafModule);

        leftleftrightChild.connectReceiverModule(this.leafModule);

        leftrightleftChild.connectReceiverModule(this.leafModule);

        leftrightrightChild.connectReceiverModule(this.leafModule);

        rightleftleftChild.connectReceiverModule(this.leafModule);

        rightleftrightChild.connectReceiverModule(this.leafModule);

        rightrightleftChild.connectReceiverModule(this.leafModule);

        rightrightrightChild.connectReceiverModule(this.leafModule);

        this.rootModule.activate();

        this.leafModule.activate();
    }

    /**
     * This method passes the given event to the {@link #rootModule}
     * of the module graph.
     * @param packet
     *            Is the event to tranport
     * @param packetCountPar
     *            Tells how often the this event has to be received by
     *            the {@link #leafModule}
     */
    public final void checkModuleEventTransport(
        final WellFormedEventPacket packet, final int packetCountPar) {
        this.testPacket = packet;

        this.result = false;

        this.howOftenToReceive = packetCountPar;

        this.rootModule.appendDirectly(packet);
    }

    /**
     * This method returns the result.
     * @return <code>true</code> if the given event has been
     *         received {@link #howOftenToReceive} times and
     *         <code>false</code> if not
     */
    public final boolean getResult() {
        return this.result;
    }

    /**
     * Compares an event that has been received by the
     * {@link #leafModule} to the event that was initially sent by the
     * {@link #rootModule}. If this method is called
     * {@link #howOftenToReceive} times and and if the events are
     * equal every time, the {@link #result} becomes <code>true</code>.
     * @param eventPacket
     *            Is the event that has been received by the
     *            {@link #leafModule}
     */
    public final void comparePackets(final ValidEventPacket eventPacket) {
        if (this.testPacket.isEqual(eventPacket)) {
            this.receivedCount++;

            if (this.receivedCount == this.howOftenToReceive) {
                this.result = true;
            } else {
                this.result = false;
            }
        }
    }
}
