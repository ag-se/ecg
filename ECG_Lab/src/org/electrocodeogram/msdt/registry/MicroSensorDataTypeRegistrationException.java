package org.electrocodeogram.msdt.registry;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * If an attempt is made to register a MicroSensorDataType with either the MSDT's value or the
 * module's value is "null" with the
 * MsdtRegistry or if the given MicroSensorDataType is allready registered this Exception is thrown.
 */
public class MicroSensorDataTypeRegistrationException extends Exception
{

	private static final long serialVersionUID = -3793556996477505178L;

	private static Logger _logger = LogHelper.createLogger(MicroSensorDataTypeRegistrationException.class.getName());
	
	/**
	 * This creates the Exception with the given message.
	 * @param string Is the message provided for the Exception
	 */
	public MicroSensorDataTypeRegistrationException(String string)
	{
		super(string);
		
		_logger.log(Level.WARNING,"An MicroSensorDataTypeRegistrationException has occured.");
		
		_logger.log(Level.WARNING,this.getMessage());
	}

}
