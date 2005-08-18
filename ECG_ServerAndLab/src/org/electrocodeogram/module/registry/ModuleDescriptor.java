package org.electrocodeogram.module.registry;


/**
 * This class is a representation for the description of module classes.
 * The description is used by ECG components to gather information about the modules
 * and their class objects.
 */
public class ModuleDescriptor
{

    private int $id = -1;

    private String $name = null;

    private Class $clazz = null;

    private ModuleProperty[] $moduleProperties = null;

    private String $description;

    /**
     * This creates a new ModuleDescriptor with the given values.
     * @param id This is the unique id of the module class as defined in the ModuleRegistry
     * @param name This is the name of the module
     * @param clazz This is the module class object
     * @param moduleDescription Is a String describing the module 
     * @param moduleProperties Is an Array of ModuleProperty objects, each defining a property of this module
     
     */
    public ModuleDescriptor(int id, String name, Class clazz, String moduleDescription, ModuleProperty[] moduleProperties)
    {
        this.$id = id;

        this.$moduleProperties = moduleProperties;

        this.$name = name;

        this.$clazz = clazz;

        this.$description = moduleDescription;

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
    public int getId()
    {
        return this.$id;
    }
}
