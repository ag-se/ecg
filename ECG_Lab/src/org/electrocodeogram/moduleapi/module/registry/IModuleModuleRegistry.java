package org.electrocodeogram.moduleapi.module.registry;

import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.ModuleDescriptor;
import org.electrocodeogram.module.registry.ModuleClassException;
import org.electrocodeogram.module.registry.ModuleInstanceException;

/**
 * This interfce declares methods that are used to access information about
 * available modules and running modules and to create new running modules from
 * available modules. This interface is used by other ECG modules and can be
 * refernced by a call to SystemRoot.getModuleInstance().getModuleRegistry()
 */
public interface IModuleModuleRegistry
{

	/**
	 * This method registers a running module with the ModuleRegistry. If the
	 * module instance is already registered with the ModuleRegistry nothing
	 * happens and the running module is not registered. This method is
	 * automatically called whenever a new object of class Module is created.
	 * 
	 * @param module
	 *            Is the module instance to register
	 */
	public abstract void registerRunningModule(Module module);

	/**
	 * This method deregistera a running module from the ModuleRegistry. It is
	 * called whenever a module is removed.
	 * 
	 * @param id
	 *            Is the unique int id of the running module to deregister
	 * @throws ModuleInstanceException
	 *             If the given int id is illegal (id < 0) or if a running
	 *             module with the given id can not be found
	 * 
	 */
	public abstract void deregisterRunningModule(int id) throws ModuleInstanceException;

	/**
	 * The method returns the ModuleDescriptor object of an available module.
	 * The ModuleDescriptor contains the information that was provided with the
	 * module in its "module.properties.xml" file.
	 * 
	 * @param id
	 *            Is the unique String id of the available module
	 * @return The ModuleDescriptor object of an available module
	 * @throws ModuleClassException
	 *             If the given String id is empty or if an availabale module
	 *             with the given String id can not be found
	 */
	public abstract ModuleDescriptor getModuleDescriptor(String id) throws ModuleClassException;

}
