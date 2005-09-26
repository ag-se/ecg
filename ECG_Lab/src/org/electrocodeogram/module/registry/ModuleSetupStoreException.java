
package org.electrocodeogram.module.registry;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * This Exception is thrown by the ModuleRegistry, if an error occurs during
 * module-setup storage.
 */
public class ModuleSetupStoreException extends Exception
{

	private static final long serialVersionUID = -5743166486387441291L;
	
	 private static Logger _logger = LogHelper.createLogger(ModuleSetupStoreException.class.getName());

	/**
	 * This creates the Exception with the given message.
	 * @param message Is the message for the Exception
	 */
	public ModuleSetupStoreException(String message)
	{
		super(message);
		
		_logger.log(Level.WARNING, "An ModuleSetupStoreException occured");

		_logger.log(Level.WARNING, this.getMessage());
	}
	
}
