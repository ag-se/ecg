package org.electrocodeogram.msdt;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.validation.Schema;

import org.electrocodeogram.module.Module;
import org.electrocodeogram.msdt.registry.MicroSensorDataTypeRegistrationException;
import org.electrocodeogram.system.Core;

/**
 * A MicroSensorDataType is a type for an actual MicroActivity event. Each
 * MicroActivity is belonging to exactly one MicroSensorDataType. Each
 * MicroSensorDataType contains a XML schema object and each MicroActivity
 * accroding to this type is written in an XML document string that is according
 * to the type's XML schema. In addition a MicroSensorDataType has a name which
 * is set to be the name of the XML schema file during MicroSensorDataType
 * creation. Additionally a unique integer id is given to each
 * MicroSensorDataType during creation.
 */
public class MicroSensorDataType
{
	private Logger _logger;

	private Schema _schema;

	private String _name;

	private static int _count;

	private int _id;
	
	private File _defFile;

	private ArrayList<Module> _providingModules;

	/**
	 * This creates a MicroSensorDataType and assigns a unique integer id ti it.
	 * 
	 * @param name
	 *            Is the name for the new type. It is provided by the module
	 *            object and is the filename of the XMl schema file.
	 * @param schema
	 *            Is the XML schema that actually defines the type
	 * @throws MicroSensorDataTypeException
	 *             If the given name is empty or of if the schema is null
	 */
	public MicroSensorDataType(String name, Schema schema, File defFile) throws MicroSensorDataTypeException
	{
		this._logger = Logger.getLogger("MicroSensorDataType");

		this._id = ++_count;
		
		this._defFile = defFile;

		if (name == null || name.equals(""))
		{
			throw new MicroSensorDataTypeException(
					"The given name is \"null\" or empty.");
		}

		this._name = name;

		if (schema == null)
		{
			throw new MicroSensorDataTypeException(
					"The given XML schema is \"null\".");
		}

		this._schema = schema;

		this._providingModules = new ArrayList<Module>();

	}

	/**
	 * This method returns the name of the MicroSensorDataType.
	 * 
	 * @return The name of the MicroSensorDataType
	 */
	public String getName()
	{
		return this._name;
	}

	public File getDefFile()
	{
		return this._defFile;
	}
	
	/**
	 * This method returns the XML Schema of the MicroSensorDataType.
	 * 
	 * @return The XML Schema of the MicroSensorDataType
	 */
	public Schema getSchema()
	{
		return this._schema;
	}

	/**
	 * This method returns the unique integer id of the MicroSensorDataType.
	 * 
	 * @return The unique integer id of the MicroSensorDataType
	 */
	public int getId()
	{
		return this._id;
	}

	/**
	 * This method is used during the load of MicroSensorDataTypes (MSTDs).
	 * Every module that defines this MSDT registers with the MSDT using this
	 * method. An MSDT is unloaded when there are no more modules registered
	 * with it. If the module has allready registered with the MSDT, this method
	 * simply returns.
	 * 
	 * @param module
	 *            Is the module that defines this MSDT
	 * @throws MicroSensorDataTypeRegistrationException
	 *             If the module parameter is "null"
	 */
	public void addProvidingModule(Module module) throws MicroSensorDataTypeRegistrationException
	{
		if (module == null)
		{
			throw new MicroSensorDataTypeRegistrationException(
					"The given module is of value \"null\"");
		}

		if (this._providingModules.contains(module))
		{
			return;
		}

		this._providingModules.add(module);

		this._logger.log(Level.INFO, "Registered module " + module.getName() + " for the MSDT " + this.getName() + ".");
	}

	/**
	 * This method is used during the deregistration of modules. If a module is
	 * deregistered (unloaded) that has defined this MicroSensorDataType (MSDT),
	 * then this method is called by the module to tell this MSDT that it no
	 * longer needs it. If no module is registered with the MSDT the MSDT can be
	 * unloaded too. If the given module was never registered with the MSDt,
	 * this method simply returns.
	 * 
	 * @param module
	 *            Is the module to deregister with the MSDT
	 * @throws MicroSensorDataTypeRegistrationException
	 *             If the given module parameter is "null"
	 */
	public void removeProvidingModule(Module module) throws MicroSensorDataTypeRegistrationException
	{
		if (module == null)
		{
			throw new MicroSensorDataTypeRegistrationException(
					"The given module is of value \"null\"");
		}

		if (!this._providingModules.contains(module))
		{
			return;
		}

		this._providingModules.remove(module);

		if (this._providingModules.size() == 0)
		{
			try
			{
                org.electrocodeogram.system.System.getInstance().getMsdtRegistry().deregisterMsdt(this);
			}
			catch (MicroSensorDataTypeRegistrationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
