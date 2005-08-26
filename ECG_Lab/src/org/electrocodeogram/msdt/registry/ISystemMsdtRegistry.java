package org.electrocodeogram.msdt.registry;

import java.io.File;

import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.msdt.MicroSensorDataTypeException;

/**
 * This interface declares methods that are used by ECG system components to
 * access the MicroSensorDataType registry.
 */
public interface ISystemMsdtRegistry
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
	 * This method is used by modules to deregister their MicroSensorDataTypes.
	 * 
	 * @param msdt
	 *            Is the actual MicroSensorDataType to deregister
	 * @throws MicroSensorDataTypeRegistrationException
	 *             Is thrown by this method, if the provided MSDT is "null" or
	 *             the module is null.
	 */
	public abstract void deregisterMsdt(MicroSensorDataType msdt) throws MicroSensorDataTypeRegistrationException;

	/**
	 * This method is used to parse a given File and create a
	 * MicroSensorDataType from it. The file must be a XMl schema file.
	 * 
	 * @param msdtFile
	 *            Is the File that defines a MicroSensorDataType
	 * @return The MicroSensorDataType defined by the file
	 * @throws MicroSensorDataTypeException
	 *             If the file does not contain a valid XML schema or if any
	 *             other error occured during parsing
	 */
	public abstract MicroSensorDataType parseMicroSensorDataType(File msdtFile) throws MicroSensorDataTypeException;

}