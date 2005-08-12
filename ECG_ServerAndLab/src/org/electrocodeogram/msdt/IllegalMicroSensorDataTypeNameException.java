package org.electrocodeogram.msdt;


/**
 * This Exception is thrown if the name that is given to create a MicroSensorDataType is of value "null".
 */
public class IllegalMicroSensorDataTypeNameException extends Exception
{

   
    private static final long serialVersionUID = -3266784413111622538L;

    /**
     * This creates the Exception with the given message.
     * @param string Ids the message provided for the Exception.
     */
    public IllegalMicroSensorDataTypeNameException(String string)
    {
       super(string);
    }

}
