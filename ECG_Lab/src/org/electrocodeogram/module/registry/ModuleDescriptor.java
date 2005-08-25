package org.electrocodeogram.module.registry;

import org.electrocodeogram.msdt.MicroSensorDataType;


/**
 * This class is a representation for the description of module classes.
 * The description is used by ECG components to gather information about the modules
 * and their class objects.
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
     * This method returns the module class' id.
     * @return The module class' id
     */
    public String getId()
    {
        return this.$id;
    }
    
    public MicroSensorDataType[] getMicroSensorDataTypes()
    {
        return this.$microSensorDataTypes;
    }
}
