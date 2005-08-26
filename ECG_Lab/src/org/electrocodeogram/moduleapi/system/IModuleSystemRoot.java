package org.electrocodeogram.moduleapi.system;

import org.electrocodeogram.moduleapi.module.registry.IModuleModuleRegistry;
import org.electrocodeogram.moduleapi.msdt.registry.IModuleMsdtRegistry;
import org.electrocodeogram.msdt.registry.ISystemMsdtRegistry;

import javax.swing.JFrame;

/**
 * The IModuleSystemRoot interfaces declares methods that are used by ECG
 * modules to access ECG system components. An instance of IModuleSystemRoot is
 * always accesible by a call to the static method
 * SystemRoot.getModuleInstance();
 */
public interface IModuleSystemRoot
{
	/**
	 * This method returns a reference to the ModuleRegistry.
	 * 
	 * @return A reference to the ModuleRegistry
	 */
	public abstract IModuleModuleRegistry getModuleModuleRegistry();
	
	/**
	 * This method is used by modules that need to reference the ECG GUIs main frame i.e.
	 * for own dialog components.
	 * @return The ECG GUIs main frame.
	 */
	public abstract JFrame getRootFrame();
	
	/**
	 * This method returns a reference to the MicroSensorDataType registry
	 * object.
	 * 
	 * @return A reference to the MicroSensorDataType registry object
	 */
	public abstract IModuleMsdtRegistry getModuleMsdtRegistry();
}
