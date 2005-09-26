package org.electrocodeogram.msdt.registry;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.moduleapi.msdt.registry.IModuleMsdtRegistry;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.msdt.MicroSensorDataTypeException;
import org.electrocodeogram.system.SystemRoot;
import org.xml.sax.SAXException;

/**
 * The MicroSensorDataType registry is a database for MicroSensorDataTypes.
 * Every ECG module is able to bring in its own MicroSensorDataTypes which are
 * the types of events that the actual module is sending. During hte module
 * registration process the module's MicroSensorDataType definitions are
 * registered with this MsdtRegistry. When a module is removed from workspace
 * and dregestering takes place, the module's MicroSensorDataTypes are also
 * deregestered. A core set of MicroSensorDataTypes are provided by the core
 * modules which are built into the ECG.
 */
public class MsdtRegistry implements ISystemMsdtRegistry, IModuleMsdtRegistry
{

	private HashMap<String, MicroSensorDataType> registeredMsdt;

	private HashMap<String, MicroSensorDataType> predefinedMsdt;

	private static Logger _logger = LogHelper.createLogger(MsdtRegistry.class.getName());

	/**
	 * This creates the MsdtRegistry.
	 */
	public MsdtRegistry()
	{

		_logger.entering(this.getClass().getName(), "MsdtRegistry");

		this.registeredMsdt = new HashMap<String, MicroSensorDataType>();

		this.predefinedMsdt = new HashMap<String, MicroSensorDataType>();

		try
		{
			loadPredefinedSourceMsdt();
		}
		catch (FileNotFoundException e)
		{

			_logger.log(Level.SEVERE, "The predifined MSDTs could not be found.");

			_logger.log(Level.FINEST, e.getMessage());

		}
		_logger.exiting(this.getClass().getName(), "MsdtRegistry");

	}

	/**
	 * This method parses the XML schema files in the "msdt" subdirectory and
	 * strores each XML schema as a MicroSensorDataType in the MsdtRegitry.
	 */
	private void loadPredefinedSourceMsdt() throws FileNotFoundException
	{

		_logger.entering(this.getClass().getName(), "loadPredefinedSourceMsdt");

		String msdtSubDirString = "msdt";

		File msdtDir = new File(msdtSubDirString);

		if (!msdtDir.exists() || !msdtDir.isDirectory())
		{
			_logger.log(Level.WARNING, "The MicroSensorDataType \"msdt\" subdirectory can not be found.");

			throw new FileNotFoundException(
					"The MicroSensorDataType \"msdt\" subdirectory can not be found.");
		}

		String[] defs = msdtDir.list();

		if (defs != null)
		{
			for (int i = 0; i < defs.length; i++)
			{
				File defFile = new File(
						msdtDir.getAbsolutePath() + File.separator + defs[i]);

				SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

				Schema schema = null;

				try
				{

					schema = schemaFactory.newSchema(defFile);

					MicroSensorDataType microSensorDataType = new MicroSensorDataType(
							defFile.getName(), schema);

					_logger.log(Level.INFO, "Loaded additional MicroSensorDatyType " + defFile.getName());

					this.predefinedMsdt.put(microSensorDataType.getName(), microSensorDataType);

				}
				catch (SAXException e)
				{

					_logger.log(Level.WARNING, "Error while reading the XML schema file " + defFile.getName());

					_logger.log(Level.FINEST, e.getMessage());

				}
				catch (MicroSensorDataTypeException e)
				{
					_logger.log(Level.WARNING, "Error while reading the XML schema file " + defFile.getName());
				}

			}
		}
		else
		{
			_logger.log(Level.SEVERE, "No MSDTs are found!");
		}

		_logger.exiting(this.getClass().getName(), "loadPredefinedSourceMsdt");

	}

	/**
	 * @see org.electrocodeogram.msdt.registry.ISystemMsdtRegistry#getMicroSensorDataTypes()
	 */
	public MicroSensorDataType[] getMicroSensorDataTypes()
	{
		_logger.entering(this.getClass().getName(), "getMicroSensorDataTypes");

		_logger.exiting(this.getClass().getName(), "getMicroSensorDataTypes");

		return this.registeredMsdt.values().toArray(new MicroSensorDataType[0]);

	}

	/**
	 * @throws MicroSensorDataTypeRegistrationException
	 * @see org.electrocodeogram.moduleapi.msdt.registry.IModuleMsdtRegistry#requestMsdtRegistration(org.electrocodeogram.msdt.MicroSensorDataType,
	 *      org.electrocodeogram.module.Module)
	 */
	public MicroSensorDataType requestMsdtRegistration(MicroSensorDataType msdt, Module module) throws MicroSensorDataTypeRegistrationException
	{
		_logger.entering(this.getClass().getName(), "requestMsdtRegistration");

		if (msdt == null)
		{
			throw new MicroSensorDataTypeRegistrationException(
					"The given MicroSensorDataType is \"null\".");
		}

		if (module == null)
		{
			throw new MicroSensorDataTypeRegistrationException(
					"The given Module is of value \"null\"");
		}

		if (this.registeredMsdt.containsKey(msdt.getName()))
		{
			MicroSensorDataType knownMsdt = this.registeredMsdt.get(msdt.getName());

			knownMsdt.addProvidingModule(module);

			_logger.log(Level.INFO, "Registered additonal Module with a known MicroSensorDatyType " + knownMsdt.getName());

			SystemRoot.getSystemInstance().fireStateChange();

			return knownMsdt;

		}

		msdt.addProvidingModule(module);

		this.registeredMsdt.put(msdt.getName(), msdt);

		_logger.log(Level.INFO, "Registered a new MicroSensorDatyType " + msdt.getName());

		SystemRoot.getSystemInstance().fireStateChange();

		_logger.exiting(this.getClass().getName(), "requestMsdtRegistration");

		return msdt;

	}

	/**
	 * @see org.electrocodeogram.msdt.registry.ISystemMsdtRegistry#deregisterMsdt(org.electrocodeogram.msdt.MicroSensorDataType)
	 */
	public void deregisterMsdt(MicroSensorDataType msdt) throws MicroSensorDataTypeRegistrationException
	{
		_logger.entering(this.getClass().getName(), "deregisterMsdt");

		if (msdt == null)
		{
			throw new MicroSensorDataTypeRegistrationException(
					"The given MicroSensorDataType is \"null\".");
		}

		if (!this.registeredMsdt.containsKey(msdt.getName()))
		{
			throw new MicroSensorDataTypeRegistrationException(
					"A MicroSensorDataType with the name " + msdt.getName() + " is not registered.");
		}

		this.registeredMsdt.remove(msdt.getName());

		_logger.log(Level.INFO, "Deregistered MicroSensorDatyType " + msdt.getName());

		SystemRoot.getSystemInstance().fireStateChange();

		_logger.exiting(this.getClass().getName(), "deregisterMsdt");
	}

	/**
	 * @see org.electrocodeogram.moduleapi.msdt.registry.IModuleMsdtRegistry#getPredefinedMicroSensorDataTypes()
	 */
	public MicroSensorDataType[] getPredefinedMicroSensorDataTypes()
	{
		_logger.entering(this.getClass().getName(), "getPredefinedMicroSensorDataTypes");

		_logger.exiting(this.getClass().getName(), "getPredefinedMicroSensorDataTypes");

		return this.predefinedMsdt.values().toArray(new MicroSensorDataType[0]);
	}

	/**
	 * @see org.electrocodeogram.msdt.registry.ISystemMsdtRegistry#parseMicroSensorDataType(java.io.File)
	 */
	public MicroSensorDataType parseMicroSensorDataType(File defFile) throws MicroSensorDataTypeException
	{
		_logger.entering(this.getClass().getName(), "parseMicroSensorDataType");

		if (!defFile.exists())
		{
			throw new MicroSensorDataTypeException(
					"Error while loading MSDT:\nThe schema file " + defFile.getAbsolutePath() + " does not exist.");
		}

		if (!defFile.isFile())
		{
			throw new MicroSensorDataTypeException(
					"Error while loading MSDT:\nThe schema file " + defFile.getAbsolutePath() + " is not a plain file.");
		}

		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		Schema schema = null;

		try
		{

			schema = schemaFactory.newSchema(defFile);

			MicroSensorDataType microSensorDataType = new MicroSensorDataType(
					defFile.getName(), schema);

			_logger.log(Level.INFO, "Loaded additional MicroSensorDatyType " + defFile.getName());

			_logger.exiting(this.getClass().getName(), "parseMicroSensorDataType");

			return microSensorDataType;

		}
		catch (SAXException e)
		{

			throw new MicroSensorDataTypeException(
					"Error while reading the XML schema file " + defFile.getName() + "\n" + e.getMessage());
		}

	}
}
