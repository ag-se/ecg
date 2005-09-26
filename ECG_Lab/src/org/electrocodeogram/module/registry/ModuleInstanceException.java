package org.electrocodeogram.module.registry;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * If the unique int id that is given to get a reference to a
 * running module is invalid (id < 0) or if a running module
 * witht the given unique int id can not be found, this Exception is thrown.
 */
public class ModuleInstanceException extends Exception
{

	private static Logger _logger = LogHelper.createLogger(ModuleInstanceException.class.getName());

	private static final long serialVersionUID = 2250598659169570982L;

	/**
	 * This creates the Exception with the given message.
	 * @param message Is the message for the Exception
	 */
	public ModuleInstanceException(String message)
	{
		super(message);

		_logger.log(Level.WARNING, "An ModuleInstanceException occured");

		_logger.log(Level.WARNING, this.getMessage());
	}

}
