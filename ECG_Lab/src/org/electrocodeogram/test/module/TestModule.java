package org.electrocodeogram.test.module;

import org.electrocodeogram.event.TypedValidEventPacket;
import org.electrocodeogram.module.Module;

/**
 * This is a simple test module. every event that is received is immediatly send.
 *
 */
public class TestModule extends Module
{

    /**
     * This creates the TestModule as a Module.INTERMEDIATE_MODULE
     */
    public TestModule()
    {
        super(ModuleType.INTERMEDIATE_MODULE,"org.electrocodeogram.module.TestModule","TestModule");
    }

    
    /**
     * @see org.electrocodeogram.module.Module#receiveEventPacket(org.electrocodeogram.event.TypedValidEventPacket)
     */
    @Override
    public void receiveEventPacket(TypedValidEventPacket eventPacket)
    {
        sendEventPacket(eventPacket);
        
    }

    /**
     * @see org.electrocodeogram.module.Module#setProperty(java.lang.String, java.lang.Object)
     */
    @Override
    public void setProperty(@SuppressWarnings("unused") String currentPropertyName, @SuppressWarnings({"unused","unused"}) String propertyValue)
    {
        // not needed
        
    }

    /**
     * @see org.electrocodeogram.module.Module#analyseCoreNotification()
     */
    @Override
    public void analyseCoreNotification()
    {
//      not needed
        
    }

    /* (non-Javadoc)
     * @see org.electrocodeogram.module.Module#initialize()
     */
    @Override
    public void initialize()
    {
        // TODO Auto-generated method stub
        
    }


}
