package org.electrocodeogram.test.server.modules;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.ModuleConnectionException;
import org.electrocodeogram.module.TestModule;

/**
 * This class is helper class for the ModuleTests. It provides methods to create
 * colllections of connected modules and methods to send and receive
 * events on the module collections. These methods are used in the testcases
 * in the ModuleTests class.
 *
 * Additionaly this class is also a module by itself. So this class is able to
 * connect itself to the end of each module collection and wait for incoming events
 * to proof.  
 */
public class TestModuleTransportModule extends Module
{

    private Module last = null;

    private ValidEventPacket testPacket = null;

    private boolean result = false;

    private int packetCount = -1;

    private int receivedCount = 0;

    private TestSourceModule root = null;

    /**
     * This creates the class as a module. 
     *
     */
    public TestModuleTransportModule()
    {
        super(ModuleType.SOURCE_MODULE);

        this.root = new TestSourceModule();

        removeConnectedModules(this.root);
    }

    private void removeConnectedModules(Module node)
    {
        Module[] connectedModules = node.getReceivingModules();

        for (Module module : connectedModules) {
            removeConnectedModules(module);

            module.remove();
        }
    }

    /**
     * This method creates a list of connected TestModules with the SensorShellWrapper.sensorSource
     * as head and the class itself as the last element in the list.
     * @param length Is the length of the list
     * @throws ModuleConnectionException If something goes wrong during conection of the modules
     */
    public void makeModuleList(int length) throws ModuleConnectionException
    {

        for (int i = 0; i < length; i++) {

            Module next = new TestModule();

            if (i == 0) {

                this.root.connectReceiverModule(next);
            }
            else {

                this.last.connectReceiverModule(next);
            }

            this.last = next;
        }

        this.last.connectReceiverModule(this);
    }

    /**
     * This method creates a binary tree of connected modules with the SensorShellWrapper.sensorSource
     * as the root and this TestModuleTransportModule connected to each leaf.
     * @throws ModuleConnectionException If something goes wrong during conection of the modules
     */
    public void makeModuleBinTree() throws ModuleConnectionException
    {

        Module leftChild = new TestModule();

        Module rightChild = new TestModule();

        this.root.connectReceiverModule(leftChild);

        this.root.connectReceiverModule(rightChild);

        Module leftleftChild = new TestModule();

        Module leftrightChild = new TestModule();

        Module rightleftChild = new TestModule();

        Module rightrightChild = new TestModule();

        leftChild.connectReceiverModule(leftleftChild);

        leftChild.connectReceiverModule(leftrightChild);

        rightChild.connectReceiverModule(rightleftChild);

        rightChild.connectReceiverModule(rightrightChild);

        Module leftleftleftChild = new TestModule();

        Module leftleftrightChild = new TestModule();

        Module leftrightleftChild = new TestModule();

        Module leftrightrightChild = new TestModule();

        Module rightleftleftChild = new TestModule();

        Module rightleftrightChild = new TestModule();

        Module rightrightleftChild = new TestModule();

        Module rightrightrightChild = new TestModule();

        leftleftChild.connectReceiverModule(leftleftleftChild);

        leftleftChild.connectReceiverModule(leftleftrightChild);

        leftrightChild.connectReceiverModule(leftrightleftChild);

        leftrightChild.connectReceiverModule(leftrightrightChild);

        rightleftChild.connectReceiverModule(rightleftleftChild);

        rightleftChild.connectReceiverModule(rightleftrightChild);

        rightrightChild.connectReceiverModule(rightrightleftChild);

        rightrightChild.connectReceiverModule(rightrightrightChild);

        leftleftleftChild.connectReceiverModule(this);

        leftleftrightChild.connectReceiverModule(this);

        leftrightleftChild.connectReceiverModule(this);

        leftrightrightChild.connectReceiverModule(this);

        rightleftleftChild.connectReceiverModule(this);

        rightleftrightChild.connectReceiverModule(this);

        rightrightleftChild.connectReceiverModule(this);

        rightrightrightChild.connectReceiverModule(this);

    }

    /**
     * This method passes the given ValidEventPacket object to the SensorShellWrapper.sensorSource module
     * that is the head of the collection of modules. After that it waits until this class' object, which is the last
     * connnected module to the collection, receives an event.
     * During event receiving the incoming event is compared to the original event. If the data is indentical
     * then this method returns "true". Otherwise it returns "false".
     * The kind of collection used during event transportation is determined by calls to the make... methods.
     * @param packet Is the ValidEventPacket to tranport
     * @param packetCountPar Tells how often the same packet shall be received. Used if this class is connected to multiple
     * other modules.
     */
    public void checkModuleEventTransport(ValidEventPacket packet, int packetCountPar)
    {
        this.testPacket = packet;

        this.result = false;

        this.packetCount = packetCountPar;

        this.root.append(packet);
    }

    /**
     * This method returns the value of the result field.
     * @return The value of the result field
     */
    public boolean getResult()
    {
        return this.result;
    }

    /**
     * @see org.electrocodeogram.module.Module#receiveEventPacket(org.electrocodeogram.event.ValidEventPacket)
     * This method compares the received event data to the original event data and sets flags to indicate
     * the result.
     */
    @Override
    public void receiveEventPacket(ValidEventPacket eventPacket)
    {
        if (this.testPacket.equals(eventPacket)) {
            this.receivedCount++;

            if (this.receivedCount == this.packetCount) {
                this.result = true;
            }
            else {
                this.result = false;
            }
        }
    }

    /**
     * @see org.electrocodeogram.module.Module#setProperty(java.lang.String, java.lang.Object)
     */
    @Override
    public void setProperty(@SuppressWarnings("unused")
    String currentPropertyName, @SuppressWarnings("unused")
    Object propertyValue)
    {
        // not needed
    }
}
