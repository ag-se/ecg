package org.electrocodeogram.msdt;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.xml.ClassLoadingException;

/**
 *
 */
public class MicroSensorDataTypeException extends Exception
{
	private static Logger _logger = LogHelper.createLogger(MicroSensorDataType.class.getName());

    public MicroSensorDataTypeException(String message)
    {
        super(message);
        
        _logger.log(Level.WARNING, "An MicroSensorDataTypeException occured");

		_logger.log(Level.WARNING, this.getMessage());
    }
    
}
