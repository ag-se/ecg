package org.electrocodeogram.msdt;

import javax.xml.validation.Schema;

public class MicroSensorDataType
{

    private Schema $schema = null;
    
    private String $name = null;
    
    private static int count = 0;
    
    private int id = 0;
    
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
        
        this.$schema = $schema;
    }

    public String getName()
    {
        return this.$name;
    }

    public Schema getSchema()
    {
        return this.$schema;
    }

    public int getId()
    {
        return this.id;
    }
    
    
    
}
