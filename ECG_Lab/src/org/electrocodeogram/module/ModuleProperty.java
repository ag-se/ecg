
package org.electrocodeogram.module;

/**
 * A ModuleProperty is a property of a module that is changeable by the user during
 * runtime. Module properties are declared in the module's "module.properties.xml"
 * file, that is provided by the module's developer with each module.
 * This file is parsed in the ECG's initialization phase and each declared module
 * property becomes a ModuleProperty object registered along with the other module
 * informations in the ModuleRegistry.
 */
public class ModuleProperty
{

    private String $name = null;
    
    private String $value = null;
    
    private Class $type = null;

    /**
     * This creates the ModuleProperty.
     * @param name Is the name of the property
     * @param value Is a default value to use before the user sets antoher value
     * @param type Is the type of the property. The type must always be a java class like "java.lang.Integer"
     */
    public ModuleProperty(String name, String value, Class type)
    {
        this.$name = name;
        
        this.$value = value;
        
        this.$type = type;
    }

    /**
     * This returns the property's name.
     * @return The property's name
     */
    public String getName()
    {
        return this.$name;
    }

    /**
     * This returns the property's default value.
     * @return The property's default value
     */
    public String getValue()
    {
        return this.$value;
    }
    
    /**
     * This returns the property's type.
     * @return The property's type
     */
    public Class getType()
    {
        return this.$type;
    }
    
}
