/*
 * Classname: IModuleSystem
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.system;

import javax.swing.JFrame;

import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.ModuleDescriptor;
import org.electrocodeogram.module.registry.ModuleClassException;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.msdt.registry.MicroSensorDataTypeRegistrationException;

/**
 * The IModuleSystem interface declares methods that are used by ECG
 * modules. An instance of IModuleSystem is always accesible by a call
 * to the static method
 * {@link org.electrocodeogram.system.ModuleSystem#getInstance()}
 */
public interface IModuleSystem {

    /**
     * This method registers a running module with the ModuleRegistry.
     * If the module instance is already registered with the
     * ModuleRegistry nothing happens and the running module is not
     * registered. This method is automatically called whenever a new
     * object of class Module is created.
     * @param module
     *            Is the module instance to register
     */
    void registerModule(Module module);

    /**
     * This method deregistera a running module from the
     * ModuleRegistry. It is called whenever a module is removed.
     * @param module
     *            Is the module instance to de-register
     */
    void deregisterModule(Module module);

    /**
     * The method returns the ModuleDescriptor object of an available
     * module. The ModuleDescriptor contains the information that was
     * provided with the module in its "module.properties.xml" file.
     * @param id
     *            Is the unique String id of the available module
     * @return The ModuleDescriptor object of an available module
     * @throws ModuleClassException
     *             If the given String id is empty or if an availabale
     *             module with the given String id can not be found
     */

    ModuleDescriptor getModuleDescriptor(String id) throws ModuleClassException;

    /**
     * This method is used by modules that need to reference the ECG
     * GUIs main frame i.e. for own dialog components.
     * @return The ECG GUIs main frame.
     */
    JFrame getRootFrame();

    /**
     * This method is used to shut down the ECG Lab.
     *
     */
    void quit();

    /**
     * This method returns an Array of all currently registered
     * MicroSensorDataType XMLSchemas.
     * @return An Array of all currently registered
     *         MicroSensorDataType XMLSchemas
     */
    MicroSensorDataType[] getMicroSensorDataTypes();

    /**
     * The ECG Lab contains a set of predefined MicroSensorDataTypes
     * that are provided by all SourceModule implementations. This
     * method returns an Array of these MicroSensorDataTypes.
     * @return The predefined MicroSensorDataTypes
     */
    MicroSensorDataType[] getPredefinedMicroSensorDataTypes();

    /**
     * This method is used by modules to register their
     * MicroSensorDataTypes (MSDTs) with this MsdtRegistry. If the
     * MSDT has allready been registered it is not registered again
     * but the providingModule parameter is passed to the MSDT's list
     * of modules that are providing this MSDT. The actual MSDT is
     * returned so that the providingModule can add it to its list of
     * provided MSDT.
     * @param msdt
     *            Is the MSDT the providingModule wants to register
     * @param module
     *            Is the Module object that provides the MSDT
     * @return A reference to the registered MSDT
     * @throws MicroSensorDataTypeRegistrationException
     *             Is thrown by this method, if the provided MSDT is
     *             "null" or the module is null.
     */
    MicroSensorDataType requestMsdtRegistration(MicroSensorDataType msdt,
        Module module) throws MicroSensorDataTypeRegistrationException;

}
