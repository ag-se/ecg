/**
 * 
 */
package org.electrocodeogram.test.server.modules;


import org.electrocodeogram.module.source.SourceModule;

/**
 *
 */
public class TestSourceModule extends SourceModule
{

    
    

    public TestSourceModule()
    {
        super("org.electrocodeogram.test.server.modules.TestSourceModule", "TestSourceModule");
    }

    /**
     * @see org.electrocodeogram.module.source.SourceModule#startReader(org.electrocodeogram.module.source.SourceModule)
     */
    @Override
    public void startReader(SourceModule sourceModule)
    {
       // Not used
        
    }

    /**
     * @see org.electrocodeogram.module.Module#setProperty(java.lang.String, java.lang.Object)
     */
    @Override
    public void setProperty(String currentPropertyName, Object propertyValue)
    {
        // Not used
        
    }

    /**
     * @see org.electrocodeogram.module.Module#analyseCoreNotification()
     */
    @Override
    public void analyseCoreNotification()
    {
//      Not used
        
    }

    /**
     * @see org.electrocodeogram.module.Module#initialize()
     */
    @Override
    public void initialize()
    {
        // TODO Auto-generated method stub
        
    }

}
