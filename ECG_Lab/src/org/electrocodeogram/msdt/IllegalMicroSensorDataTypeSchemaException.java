package org.electrocodeogram.msdt;

/**
 * This Exception is thrown if the XML schema that is given to create a MicroSensorDataType is of value "null".
 */
public class IllegalMicroSensorDataTypeSchemaException extends Exception
{

    private static final long serialVersionUID = -139460826990865732L;

    /**
     * This creates the Exception with the given message.
     * @param string Ids the message provided for the Exception.
     */
    public IllegalMicroSensorDataTypeSchemaException(String string)
    {
        super(string);
    }

}
