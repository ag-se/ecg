/**
 * 
 */
package org.electrocodeogram.core;

import org.electrocodeogram.module.registry.ModuleRegistry;
import org.electrocodeogram.msdt.MsdtRegistry;
import org.electrocodeogram.ui.IGui;

/**
 *
 */
public interface ICore
{

    /**
     * This method returns a reference to the MicroSensorDataType-Manager object. 
     * @return A reference to the MicroSensorDataType-Manager object
     */
    public abstract MsdtRegistry getMsdtRegistry();

    /**
     * This method returns a reference to the ModuleRegistry object. 
     * @return A reference to the ModuleRegistry object
     */
    public abstract ModuleRegistry getModuleRegistry();

    /**
     * This method returns a reference to the gui main frame object. 
     * @return A reference to the gui main frame object
     */
    public abstract IGui getGui();

    /**
     * 
     */
    public abstract void quit();

    
}