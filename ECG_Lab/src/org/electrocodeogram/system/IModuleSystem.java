/*
 * Classname: IModuleSystem
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.system;

import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.registry.ModulePackageNotFoundException;
import org.electrocodeogram.modulepackage.ModuleDescriptor;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.msdt.registry.MicroSensorDataTypeRegistrationException;

/**
 * The <code>IModuleSystem</code> interface declares methods that are used by ECG
 * modules. An instance of <code>IModuleSystem</code> is always accesible by a call
 * to the static method
 * {@link org.electrocodeogram.system.ModuleSystem#getInstance()}
 */
public interface IModuleSystem {

    /**
     * This method registers a running module with the {@link org.electrocodeogram.module.registry.ModuleRegistry}.
     * If the module instance is already registered with the
     * <code>ModuleRegistry</code> nothing happens and the running module is not
     * registered. This method is automatically called whenever a new
     * module is created.
     * @param module
     *            Is the module instance to register
     */
    void registerModule(Module module);

    /**
     * This method deregistera a running module from the
     * {@link org.electrocodeogram.module.registry.ModuleRegistry}. It is called whenever a module is removed.
     * @param module
     *            Is the module instance to de-register
     */
    void deregisterModule(Module module);

    /**
     * The method returns the {@link ModuleDescriptor} object of an available
     * module. The <code>ModuleDescriptor</code> contains the information that is
     * provided with the module in its "module.properties.xml" file.
     * @param id
     *            Is the unique string id of the available module
     * @return The <code>ModuleDescriptor</code>-object of an available module
     * @throws ModulePackageNotFoundException
     *             If the given String id is empty or if an availabale
     *             module with the given String id can not be found
     */

    ModuleDescriptor getModuleDescriptor(String id) throws ModulePackageNotFoundException;


    /**
     * This method is used to shut down the ECG Lab.
     *
     */
    void quit();

    /**
     * This method returns an array of all currently registered
     * MicroSensorDataType XMLSchemas.
     * @return An Array of all currently registered
     *         MicroSensorDataType XMLSchemas
     */
    MicroSensorDataType[] getMicroSensorDataTypes();

    /**
     * The ECG Lab contains a set of predefined <em>MicroSensorDataTypes</em>
     * that are provided by all {@link org.electrocodeogram.module.source.SourceModule}-implementations. This
     * method returns an array of these <em>MicroSensorDataTypes</em>.
     * @return The predefined <em>MicroSensorDataTypes</em>
     */
    MicroSensorDataType[] getPredefinedMicroSensorDataTypes();

    /**
     * This method is used by modules to register their
     * <em>MicroSensorDataTypes</em> (MSDTs) with this {@link org.electrocodeogram.msdt.registry.MsdtRegistry}. If the
     * MSDT has allready been registered it is not registered again
     * but the providingModule parameter is passed to the MSDT's list
     * of modules that are providing this MSDT. The actual MSDT is
     * returned so that the providingModule can add it to its list of
     * provided MSDT.
     * @param msdt
     *            Is the MSDT the providingModule wants to register
     * @param module
     *            Is the module object that provides the MSDT
     * @return A reference to the registered MSDT
     * @throws MicroSensorDataTypeRegistrationException
     *             Is thrown by this method, if the provided MSDT is
     *             <code>null</code> or the module is <code>null</code>.
     */
    MicroSensorDataType requestMsdtRegistration(MicroSensorDataType msdt,
        Module module) throws MicroSensorDataTypeRegistrationException;

}
