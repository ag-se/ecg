package org.electrocodeogram.moduleapi.msdt.registry;

import org.electrocodeogram.module.Module;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.msdt.registry.MicroSensorDataTypeRegistrationException;

/**
 * This interface declares methods that are used by ECG modules to access
 * the MicroSensorDataType registry.
 */
public interface IModuleMsdtRegistry
{
	/**
	 * This method returns an Array of all currently registered
	 * MicroSensorDataType XMLSchemas.
	 * 
	 * @return An Array of all currently registered MicroSensorDataType
	 *         XMLSchemas
	 */
	public abstract MicroSensorDataType[] getMicroSensorDataTypes();
	
	/**
	 * The ECG Lab contains a set of predefined MicroSensorDataTypes that are provided by all SourceModule
	 * implementations. This method returns an Array of these MicroSensorDataTypes.
	 * @return The predefined MicroSensorDataTypes
	 */
	public abstract MicroSensorDataType[] getPredefinedMicroSensorDataTypes();

	/**
	 * This method is used by modules to register their MicroSensorDataTypes (MSDTs)
	 * with this MsdtRegistry. If the MSDT has allready been registered it is not
	 * registered again but the providingModule parameter is passed to the MSDT's list
	 * of modules that are providing this MSDT.
	 * The actual MSDT is returned so that the providingModule can add it to its
	 * list of provided MSDT.
	 * 
	 * @param msdt
	 *            Is the MSDT the providingModule wants to register
	 * @param module Is the Module object that provides the MSDT
	 * @return A reference to the registered MSDT
	 * @throws MicroSensorDataTypeRegistrationException
	 *             Is thrown by this method, if the provided MSDT
	 *             is "null" or the module is null.
	 */
	public abstract MicroSensorDataType requestMsdtRegistration(MicroSensorDataType msdt, Module module) throws MicroSensorDataTypeRegistrationException;

}
