package org.electrocodeogram.test.server.modules;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.ModuleActivationException;
import org.electrocodeogram.module.ModuleConnectionException;
import org.electrocodeogram.test.module.TestModule;

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
public class ModuleTestHelper
{

    private Module _lastModule;

    private WellFormedEventPacket _testPacket;

    private boolean _result;

    private int _packetCount;

    private int _receivedCount;

    private MockSourceModule _rootModule;
    
    private MockTargetModule _leafModule;

    /**
     * This creates the class as a module. 
     */
    public ModuleTestHelper()
    {
     
    	this._rootModule = new MockSourceModule();
    	
    	this._rootModule.setAllowNonECGmSDTConformEvents(true);
    	
    	this._leafModule = new MockTargetModule(this);
    	
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
     * @throws ModuleActivationException 
     */
    public void makeModuleList(int length) throws ModuleConnectionException, ModuleActivationException
    {

        for (int i = 0; i < length; i++) {

            Module next = new TestModule();
            
            if (i == 0) {

                this._rootModule.connectReceiverModule(next);
            }
            else {

                this._lastModule.connectReceiverModule(next);
            }

            next.activate();
            
            this._lastModule = next;
        }

        this._lastModule.connectReceiverModule(this._leafModule);
        
        this._rootModule.activate();
        
        this._leafModule.activate();
    }

    /**
     * This method creates a binary tree of connected modules with the SensorShellWrapper.sensorSource
     * as the root and this TestModuleTransportModule connected to each leaf.
     * @throws ModuleConnectionException If something goes wrong during conection of the modules
     * @throws ModuleActivationException 
     */
    public void makeModuleBinTree() throws ModuleConnectionException, ModuleActivationException
    {

        Module leftChild = new TestModule();
        
        leftChild.activate();

        Module rightChild = new TestModule();

        rightChild.activate();
        
        this._rootModule.connectReceiverModule(leftChild);

        this._rootModule.connectReceiverModule(rightChild);

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

        leftleftleftChild.connectReceiverModule(this._leafModule);

        leftleftrightChild.connectReceiverModule(this._leafModule);

        leftrightleftChild.connectReceiverModule(this._leafModule);

        leftrightrightChild.connectReceiverModule(this._leafModule);

        rightleftleftChild.connectReceiverModule(this._leafModule);

        rightleftrightChild.connectReceiverModule(this._leafModule);

        rightrightleftChild.connectReceiverModule(this._leafModule);

        rightrightrightChild.connectReceiverModule(this._leafModule);

        this._rootModule.activate();
        
        this._leafModule.activate();
    }

    /**
     * This method passes the given WellFormedEventPacket object to the SensorShellWrapper.sensorSource module
     * that is the head of the collection of modules. After that it waits until this class' object, which is the last
     * connnected module to the collection, receives an event.
     * During event receiving the incoming event is compared to the original event. If the data is indentical
     * then this method returns "true". Otherwise it returns "false".
     * The kind of collection used during event transportation is determined by calls to the make... methods.
     * @param packet Is the WellFormedEventPacket to tranport
     * @param packetCountPar Tells how often the same packet shall be received. Used if this class is connected to multiple
     * other modules.
     */
    public void checkModuleEventTransport(WellFormedEventPacket packet, int packetCountPar)
    {
        this._testPacket = packet;

        this._result = false;

        this._packetCount = packetCountPar;

        this._rootModule.append(packet);
    }

    /**
     * This method returns the value of the result field.
     * @return The value of the result field
     */
    public boolean getResult()
    {
        return this._result;
    }

  
    public void comparePackets(ValidEventPacket eventPacket)
    {
        if (this._testPacket.equals(eventPacket)) {
            this._receivedCount++;

            if (this._receivedCount == this._packetCount) {
                this._result = true;
            }
            else {
                this._result = false;
            }
        }
    }
}
