package org.electrocodeogram.msdt;

/**
 * If an attempt is made to register a MicroSensorDataType ith value "null" with the
 * MsdtRegistry or if the given MicroSensorDataType is allready registered this Exception is thrown.
 */
public class MSDTIsNullException extends Exception
{

    private static final long serialVersionUID = -3793556996477505178L;

    /**
     * This creates the Exception with the given message.
     * @param string Is the message provided for the Exception
     */
    public MSDTIsNullException(String string)
    {
        super(string);
    }

}
