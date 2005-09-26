package org.electrocodeogram.module.registry;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * If the unique String id that is given to get a reference to a
 * availabale module is null or empty or if a available module
 * witht the given unique String id can not be found, this Exception is thrown.
 */
public class ModuleClassException extends Exception
{

	private static final long serialVersionUID = 8400526904223267328L;
	
	private static Logger _logger = LogHelper.createLogger(ModuleClassException.class.getName());

	/**
	 * This creates the Exception with the given message.
	 * @param message Is the message for the Exception
	 */
	public ModuleClassException(String message)
	{
		super(message);
		
		_logger.log(Level.WARNING,"An ModuleClassException has occured.");
		
		_logger.log(Level.WARNING,this.getMessage());
	}

}
