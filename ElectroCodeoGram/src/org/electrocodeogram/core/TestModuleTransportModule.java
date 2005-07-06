package org.electrocodeogram.core;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.ModuleConnectionException;
import org.electrocodeogram.module.ModuleRegistry;
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

    private Module root = null;

    private Module last = null;

    private ValidEventPacket testPacket = null;

    private boolean received = false;

    private boolean same = false;

    /**
     * This creates the class as a module. 
     *
     */
    public TestModuleTransportModule()
    {
        super(ModuleType.SOURCE_MODULE);

        this.root = SensorShellWrapper.getInstance().sensorSource;

    }

    /**
     * This method creates a list of connected TestModules with the SensorShellWrapper.sensorSource
     * as head and the class itself as the last element in the list.
     * @param length Is the length of the list
     * @throws ModuleConnectionException If something goes wrong during conection of the modules
     */
    public void makeModuleList(int length) throws ModuleConnectionException
    {

        ModuleRegistry mr = ModuleRegistry.getInstance();
        
        for (int i = 0; i < length; i++) {
            Module next = new TestModule();

            if (i == 0) {
                //this.root.connectChildModule(next);
                mr.connectModule(this.root.getId(),next.getId());
            }
            else {
                //this.last.connectChildModule(next);
                mr.connectModule(this.last.getId(),next.getId());
            }

            this.last = next;
        }

        //this.last.connectChildModule(this);
        mr.connectModule(this.last.getId(),this.getId());
    }

//    public void makeModuleBinTree(int depth) throws ModuleConnectionException
//    {
//        
//        Module node = root;
//        
//        
//    }
//    
//    private void buildBinTree(Module node) throws ModuleConnectionException
//    {
//        
//        Module[] childreen = node.getChildModules();
//        
//        for(Module child : childreen)
//        {
//            connectChildreen(child);
//        }
//    }
//    
//    private void connectChildreen(Module node) throws ModuleConnectionException
//    {
//        TestModule leftChild = new TestModule();
//        
//        TestModule rightChild = new TestModule();
//        
//        node.connectChildModule(leftChild);
//        
//        node.connectChildModule(rightChild);
//    }
    
    /**
     * This method passes the given ValidEventPacket object to the SensorShellWrapper.sensorSource module
     * that is the head of the collection of modules. After that it waits until this class' object, which is the last
     * connnected module to the collection, receives an event.
     * During event receiving the incoming event is compared to the original event. If the data is indentical
     * then this method returns "true". Otherwise it returns "false".
     * The kind of collection used during event transportation is determined by calls to the make... methods.
     * @param packet Is the ValidEventPacket to tranport
     * @return "true" if an indentical ValidEventPacket is received and "false" otherwise.
     */
    public boolean checkModuleEventTransport(ValidEventPacket packet)
    {
        this.testPacket = packet;

        SensorShellWrapper.getInstance().doCommand(packet.getTimeStamp(), packet.getHsCommandName(), packet.getArglist());

        while (!this.received) {
            // wait
        }

        if (this.same) {
            this.same = false;

            return true;
        }

        return false;

    }

    /**
     * @see org.electrocodeogram.module.Module#receiveEventPacket(org.electrocodeogram.event.ValidEventPacket)
     * This method compares the received event data to the original event data and sets flags to indicate
     * the result.
     */
    @Override
    public void receiveEventPacket(ValidEventPacket eventPacket)
    {
        if (eventPacket.getTimeStamp().equals(this.testPacket.getTimeStamp()) && eventPacket.getHsCommandName().equals(this.testPacket.getHsCommandName())) {
            if (eventPacket.getArglist().size() == this.testPacket.getArglist().size()) {
                int size = this.testPacket.getArglist().size();

                for (int i = 0; i < size; i++) {
                    String testString = (String) this.testPacket.getArglist().get(i);

                    String receivedString = (String) eventPacket.getArglist().get(i);

                    if (!testString.equals(receivedString)) {
                        this.received = true;
                        this.same = false;
                        return;
                    }

                    this.received = true;
                    this.same = true;
                }
            }
        }

    }

    /**
     * @see org.electrocodeogram.module.Module#setProperty(java.lang.String, java.lang.Object)
     */
    @Override
    public void setProperty(@SuppressWarnings("unused") String currentPropertyName, @SuppressWarnings("unused") Object propertyValue)
    {
        // not needed
    }
}
