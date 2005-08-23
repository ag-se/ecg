/**
 * 
 */
package org.electrocodeogram.core;

import java.util.Observer;

import org.electrocodeogram.module.registry.ModuleRegistry;
import org.electrocodeogram.msdt.MsdtRegistry;
import org.electrocodeogram.ui.IGui;

/**
 *
 */
public interface ICore
{

    public abstract void addObserver(Observer o);
    
    public abstract void deleteObserver(Observer o); 
    
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

    public abstract void fireStateChange();
}