package org.electrocodeogram.module.registry;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * Whenever an error occurs during module instantiation, this Exception is thrown.
 *
 */
public class ModuleInstantiationException extends Exception
{

	 private static final long serialVersionUID = 4209402497277710920L;
	 
	 private static Logger _logger = LogHelper.createLogger(ModuleInstantiationException.class.getName());
    /**
     * This creates the Exception with the given message.
     * @param message
     */
    public ModuleInstantiationException(String message)
    {
        super(message);
        
        _logger.log(Level.WARNING, "An ModuleInstantiationException occured");

		_logger.log(Level.WARNING, this.getMessage());
    }

  


}
