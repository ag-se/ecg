package org.electrocodeogram.module;

import org.electrocodeogram.module.Module.ModuleType;
import org.electrocodeogram.msdt.MicroSensorDataType;


/**
 * This class is a representation for the description of module classes.
 * The description is used by ECG components to gather information about an
 * available module class.
 */
public class ModuleDescriptor
{

    private String _id;

    private String _name;

    private String _provider_name;
    
    private String _version;
    
    private Class _clazz;

    private ModuleProperty[] _moduleProperties;
    
    private MicroSensorDataType[] _microSensorDataTypes;

    private String _description;
    
    private ModuleType _moduleType; 

    /**
     * This creates a new ModuleDescriptor with the given values.
     * @param id This is the unique id of the module class as defined in the ModuleRegistry
     * @param name This is the name of the module
     * @param provider_name This is the name of the module provider
     * @param version Is the version of the module
     * @param clazz This is the module class object
     * @param moduleDescription Is a String describing the module 
     * @param moduleProperties Is an Array of ModuleProperty objects, each defining a property of this module
     * @param microSensorDataTypes 
     
     */
    public ModuleDescriptor(String id, String name, String provider_name, String version, Class clazz, String moduleDescription, ModuleType moduleType, ModuleProperty[] moduleProperties, MicroSensorDataType[] microSensorDataTypes)
    {
        this._id = id;
        
        this._name = name;
        
        this._provider_name = provider_name;
        
        this._version = version;

        this._clazz = clazz;

        this._moduleType = moduleType;
        
        this._description = moduleDescription;
        
        if(moduleProperties != null)
        {
        	int size = moduleProperties.length;
        	
        	this._moduleProperties = new ModuleProperty[size];
        	
        	for(int i=0;i<size;i++)
        	{
        		this._moduleProperties[i] = moduleProperties[i];
        	}
        }
                
        if(microSensorDataTypes != null)
        {
        	int size = microSensorDataTypes.length;
        	
        	this._microSensorDataTypes = new MicroSensorDataType[size];
        	
        	for(int i=0;i<size;i++)
        	{
        		this._microSensorDataTypes[i] = microSensorDataTypes[i];
        	}
        }

    }

    /**
     * This returns the module's description.
     * @return the module's description
     */
    public String getDescription()
    {
        return this._description;
    }

    /**
     * This method returns the module class object.
     * @return The module class object
     */
    public Class getClazz()
    {
        return this._clazz;
    }

    /**
     * This method returns the module name.
     * @return The module name
     */
    public String getName()
    {
        return this._name;
    }

    /**
     * This method returns module properties as an Array of ModuleProperty objects.
     * @return The module properties as an Array of ModuleProperty objects
     */
    public ModuleProperty[] getProperties()
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

    /**
     * This method returns the unique String id of this module
     * @return The unique String id of this module
     */
    public String getId()
    {
        return this._id;
    }
    
    /**
     * This method returns the MicroSensorDataTypes that are provided by this module as an Array
     * @return The MicroSensorDataTypes that are provided by this module
     */
    public MicroSensorDataType[] getMicroSensorDataTypes()
    {
    	if(this._microSensorDataTypes == null)
    	{
    		return null;
    	}
    	
    	int size = this._microSensorDataTypes.length;
    	
    	MicroSensorDataType[] toReturn = new MicroSensorDataType[size];
    	
    	for(int i=0;i<size;i++)
    	{
    		toReturn[i] = this._microSensorDataTypes[i];
    	}
    	
        return toReturn;
    }

	
    /**
     * This method returns the module provider name.
     * @return The module provider name
     */
    public String getProvider_name()
	{
		return this._provider_name;
	}
    
    /**
     * This method returns the module version.
     * @return The module version
     */

	public String getVersion()
	{
		return this._version;
	}

	public ModuleType get_moduleType()
	{
		return this._moduleType;
	}
}
