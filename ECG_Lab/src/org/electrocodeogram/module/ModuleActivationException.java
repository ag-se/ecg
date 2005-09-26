/**
 * 
 */
package org.electrocodeogram.module;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 *
 */
public class ModuleActivationException extends Exception
{

	private static Logger _logger = LogHelper.createLogger(ModuleActivationException.class.getName());
	
	
	public ModuleActivationException(String message)
	{
		super(message);
		
		_logger.log(Level.WARNING,"An ModuleActivationException has occured.");
		
		_logger.log(Level.WARNING,this.getMessage());
	}
	
}
