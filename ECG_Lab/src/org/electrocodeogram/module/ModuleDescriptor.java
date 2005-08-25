package org.electrocodeogram.module;

import org.electrocodeogram.msdt.MicroSensorDataType;


/**
 * This class is a representation for the description of module classes.
 * The description is used by ECG components to gather information about an
 * available module class.
 */
public class ModuleDescriptor
{

    private String $id;

    private String $name;

    private String $provider_name;
    
    private String $version;
    
    private Class $clazz;

    private ModuleProperty[] $moduleProperties;
    
    private MicroSensorDataType[] $microSensorDataTypes;

    private String $description;

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
    public ModuleDescriptor(String id, String name, String provider_name, String version, Class clazz, String moduleDescription, ModuleProperty[] moduleProperties, MicroSensorDataType[] microSensorDataTypes)
    {
        this.$id = id;
        
        this.$moduleProperties = moduleProperties;

        this.$name = name;
        
        this.$provider_name = provider_name;
        
        this.$version = version;

        this.$clazz = clazz;

        this.$description = moduleDescription;
        
        this.$microSensorDataTypes = microSensorDataTypes;

    }

    /**
     * This returns the module's description.
     * @return the module's description
     */
    public String getDescription()
    {
        return this.$description;
    }

    /**
     * This method returns the module class object.
     * @return The module class object
     */
    public Class getClazz()
    {
        return this.$clazz;
    }

    /**
     * This method returns the module name.
     * @return The module name
     */
    public String getName()
    {
        return this.$name;
    }

    /**
     * This method returns module properties as an Array of ModuleProperty objects.
     * @return The module properties as an Array of ModuleProperty objects
     */
    public ModuleProperty[] getProperties()
    {
        return this.$moduleProperties;
    }

    /**
     * This method returns the unique String id of this module
     * @return The unique String id of this module
     */
    public String getId()
    {
        return this.$id;
    }
    
    /**
     * This method returns the MicroSensorDataTypes that are provided by this module as an Array
     * @return The MicroSensorDataTypes that are provided by this module
     */
    public MicroSensorDataType[] getMicroSensorDataTypes()
    {
        return this.$microSensorDataTypes;
    }

	
    /**
     * This method returns the module provider name.
     * @return The module provider name
     */
    public String getProvider_name()
	{
		return this.$provider_name;
	}
    
    /**
     * This method returns the module version.
     * @return The module version
     */

	public String getVersion()
	{
		return this.$version;
	}
}
