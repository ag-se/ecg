package org.electrocodeogram.msdt;

import javax.xml.validation.Schema;

/**
 * A MicroSensorDataType is a type for an actual MicroActivity event.
 * Each MicroActivity is belonging to exactly one MicroSensorDataType.
 * Each MicroSensorDataType contains a XML schema object and each
 * MicroActivity accroding to this type is written in an XML document
 * string that is according to the type's XML schema.
 * In addition a MicroSensorDataType has a name which is set to be the name
 * of the XML schema file during MicroSensorDataType creation.
 * Additionally a unique integer id is given to each MicroSensorDataType during creation. 
 */
public class MicroSensorDataType
{

    private Schema $schema = null;
    
    private String $name = null;
    
    private static int count = 0;
    
    private int id = 0;
   
    /**
     * This creates a MicroSensorDataType and assigns a unique integer id ti it.
     * @param name Is the name for the new type. It is provided by the module object and is the filename of the XMl schema file.
     * @param schema Is the XML schema that actually defines the type
     * @throws IllegalMicroSensorDataTypeNameException If the name value is "null"
     * @throws IllegalMicroSensorDataTypeSchemaException If the schema value is "null"
     */
    public MicroSensorDataType(String name, Schema schema) throws IllegalMicroSensorDataTypeNameException, IllegalMicroSensorDataTypeSchemaException
    {
        this.id = ++count;
        
        if(name == null || name.equals(""))
        {
            throw new IllegalMicroSensorDataTypeNameException("The given name is \"null\" or empty.");
        }
        
        this.$name = name;
        
        if(schema == null)
        {
            throw new IllegalMicroSensorDataTypeSchemaException("The given XML schema is \"null\".");
        }
        
        this.$schema = schema;
    }

    /**
     * This method returns the name of the MicroSensorDataType.
     * @return The name of the MicroSensorDataType
     */
    public String getName()
    {
        return this.$name;
    }

    /**
     * This method returns the XML Schema of the MicroSensorDataType.
     * @return The XML Schema of the MicroSensorDataType
     */
    public Schema getSchema()
    {
        return this.$schema;
    }

    /**
     * This method returns the unique integer id of the MicroSensorDataType.
     * @return The unique integer id of the MicroSensorDataType
     */
    public int getId()
    {
        return this.id;
    }
    
    
    
}
