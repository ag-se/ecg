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
	
	private boolean _active;
	
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
	public ModuleConfiguration(Integer[] to, int id, String name, boolean active,ModuleProperty[] properties,String fromClassId)
	{
		
		this._moduleId = id;
		
		this._moduleName = name;
		
		this._fromClassId = fromClassId;
		
		this._active = active;
		
		if(properties != null)
		{
		
			int size = properties.length;

			this._moduleProperties = new ModuleProperty[size];
			
			for(int i=0;i<size;i++)
			{
				this._moduleProperties[i] = properties[i];
			}
			
			
		}
		
		
		if(to != null)
		{
		
			int size = to.length;

			this._connectedTo = new Integer[size];
			
			for(int i=0;i<size;i++)
			{
				this._connectedTo[i] = to[i];
			}
			
			
		}
		
		
		
	}

	public Integer[] getConnectedTo()
	{
		if(this._connectedTo == null)
		{
			return null;
		}
		
		int size = this._connectedTo.length;
		
		Integer[] toReturn = new Integer[size];
		
		for(int i=0;i<size;i++)
		{
			toReturn[i] = this._connectedTo[i];
		}
		
		return toReturn;
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
		if(this._moduleProperties == null)
		{
			return null;
		}
		
		int size = this._moduleProperties.length;
		
		ModuleProperty[] toReturn = new ModuleProperty[size];
		
		for(int i=0;i<size;i++)
		{
			toReturn[i] = this._moduleProperties[i];
		}
		
		return toReturn;
	}
	
	public boolean isActive()
	{
		return this._active;
	}
}
