package org.electrocodeogram.module.registry;

import org.electrocodeogram.module.Module;

/**
 * This interfce declares methods that are used to access information about
 * available modules and running modules and to create new running modules from
 * available modules. This interface is used by other ECG system components and
 * can be refernced by a call to
 * SystemRoot.getSystemInstance().getModuleRegistry()
 */
public interface ISystemModuleRegistry
{

	/**
	 * This method returns the unique String IDs of all currently known module
	 * class files that are available in the module directory.
	 * 
	 * @return The IDs of all currently known module class files
	 */
	public abstract String[] getAvailableModuleIds();

	/**
	 * This method returns the running module with the given int id.
	 * 
	 * @param id
	 *            Is the unique int id of the running module to return
	 * @return The desired running module instance
	 * @throws ModuleInstanceException
	 *             If the given int id is illegal (id < 0) or if a running
	 *             module with the given id can not be found
	 */
	public abstract Module getRunningModule(int id) throws ModuleInstanceException;

	/**
	 * This method takes the unique String id of a available module and returns
	 * a new running module instance of it. It also gives the running module the
	 * provided name.
	 * 
	 * @param id
	 *            Is the unique String id of the available module class to
	 *            create the running module from
	 * @param name
	 *            Is the name that should be given to the new running module
	 * @throws ModuleInstantiationException
	 *             If an Exception is thrown during the instanciation of the
	 *             module
	 * @throws ModuleClassException
	 *             If the given String id is empty or if an availabale module
	 *             with the given String id can not be found
	 */
	public abstract void createRunningModule(String id, String name) throws ModuleInstantiationException, ModuleClassException;

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