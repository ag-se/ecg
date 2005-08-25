package org.electrocodeogram.system;

import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.registry.ISystemModuleRegistry;
import org.electrocodeogram.msdt.registry.ISystemMsdtRegistry;
import org.electrocodeogram.ui.IGui;

/**
 * The ISystemRoot interfaces declares methods that are used by ECG system
 * components to access other ECG system components and to register ECG modules
 * with the system for getting notifications about system statechanges. An
 * instance of ISystemRoot is always accesible by a call to the static method
 * SystemRoot.getSystemInstance();
 */
public interface ISystemRoot
{
	/**
	 * This method registers a module instance with the IModuleSystemRoot. all
	 * registered modules ill be notified of system statechanges.
	 * 
	 * @param module
	 *            Is the module instance to register
	 */
	public abstract void addModule(Module module);

	/**
	 * This method deregisters a module instance with the IModuleSystemRoot. all
	 * registered modules ill be notified of system statechanges.
	 * 
	 * @param module
	 *            Is the module instance to deregister
	 */
	public abstract void deleteModule(Module module);

	/**
	 * This method returns a reference to the MicroSensorDataType registry
	 * object.
	 * 
	 * @return A reference to the MicroSensorDataType registry object
	 */
	public abstract ISystemMsdtRegistry getSystemMsdtRegistry();

	/**
	 * This method returns a reference to the ModuleRegistry.
	 * 
	 * @return A reference to the ModuleRegistry
	 */
	public abstract ISystemModuleRegistry getSystemModuleRegistry();

	/**
	 * This method returns a reference to the gui main frame object.
	 * 
	 * @return A reference to the gui main frame object
	 */
	public abstract IGui getGui();

	/**
	 * This method is used to quit the ECG Lab application.
	 */
	public abstract void quit();

	/**
	 * When a ECG system component has changed its state in a way that is
	 * interesting to other ECG system components or to ECG modules, it calls
	 * the fireStateChange method of the ISystemRoot. That will lead to a
	 * notification to all registered Observers.
	 * 
	 */
	public abstract void fireStateChange();
}