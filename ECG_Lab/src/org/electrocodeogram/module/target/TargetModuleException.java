/**
 * 
 */
package org.electrocodeogram.module.target;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 *
 */
public class TargetModuleException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5662660466138968398L;
	
	private static Logger _logger = LogHelper.createLogger(TargetModuleException.class.getName());
	

	public TargetModuleException(String message)
	{
		super(message);
		
		_logger.log(Level.WARNING,"An TargetModuleException has occured.");
		
		_logger.log(Level.WARNING,this.getMessage());
	}
	
}
