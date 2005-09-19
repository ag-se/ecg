/**
 * 
 */
package org.electrocodeogram.module.setup;

import org.electrocodeogram.module.ModuleProperty;

/**
 *
 */
public class ModuleConfiguration
{

	private String _moduleName;
	
	private int _moduleId;
	
	private ModuleProperty[] _moduleProperties;
	
	private Integer[] _connectedTo;
	
	private String _fromClassId;

	
	
	public String getFromClassId()
	{
		return this._fromClassId;
	}

	/**
	 * @param to
	 * @param id
	 * @param name
	 * @param properties
	 */
	public ModuleConfiguration(Integer[] to, int id, String name, ModuleProperty[] properties,String fromClassId)
	{
		this._connectedTo = to;
		this._moduleId = id;
		this._moduleName = name;
		this._moduleProperties = properties;
		this._fromClassId = fromClassId;
	}

	public Integer[] getConnectedTo()
	{
		return this._connectedTo;
	}

	public int getModuleId()
	{
		return this._moduleId;
	}

	public String getModuleName()
	{
		return this._moduleName;
	}

	public ModuleProperty[] getModuleProperties()
	{
		return this._moduleProperties;
	}
	
}
