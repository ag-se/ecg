/**
 * 
 */
package org.electrocodeogram.module.source;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 *
 */
public class SourceModuleException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -64199202002297093L;
	
	private static Logger _logger = LogHelper.createLogger(SourceModuleException.class.getName());
	
	public SourceModuleException(String message)
	{
		super(message);
		
		_logger.log(Level.WARNING,"An SourceModuleException has occured.");
		
		_logger.log(Level.WARNING,this.getMessage());
	}
	
}
