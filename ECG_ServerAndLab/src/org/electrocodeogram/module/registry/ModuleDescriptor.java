
package org.electrocodeogram.module.registry;

import java.util.Properties;
/**
 * This class is a representation for the description of module classes.
 * The description is used by ECG components to gather information aboaut the modules
 * and their class objects.
 */
public class ModuleDescriptor
{

    private int $id = -1;
    
    private String $name = null;
    
    private Class $clazz = null;
    
    private Properties $properties = null;
    
    // TODO : make the prop file XML
    
   /**
    * This creates a new ModuleDescriptor with the given values.
 * @param id This is the unique id of the module class as defined in the ModuleRegistry
 * @param name This is the name of the module
 * @param clazz This is the module class object
 * @param properties  This is the module's properties object built from the module's "module.properties" file
 
    */
    public ModuleDescriptor(int id, String name, Class clazz, Properties properties)
    {
        this.$id = id;
        
        this.$properties = properties;
        
        this.$name = name;
        
        this.$clazz = clazz;
        
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
     * This method returns the module's property object.
     * @return The module's property object
     */
    public Properties getProperties()
    {
        return this.$properties;
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
