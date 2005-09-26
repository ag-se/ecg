/**
 * 
 */
package org.electrocodeogram.module.registry;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 *
 */
public class ModuleSetupLoadException extends Exception
{

	private static Logger _logger = LogHelper.createLogger(ModuleSetupLoadException.class.getName());
	
	private static final long serialVersionUID = 8611067658025512073L;

	
	/**
	 * @param string
	 */
	public ModuleSetupLoadException(String string)
	{
		super(string);
		
		_logger.log(Level.WARNING, "An ModuleSetupLoadException occured");

		_logger.log(Level.WARNING, this.getMessage());
	}

	
	
}
