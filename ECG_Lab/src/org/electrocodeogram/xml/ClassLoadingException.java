/**
 * 
 */
package org.electrocodeogram.xml;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

public class ClassLoadingException extends Exception
{
	private static Logger _logger = LogHelper.createLogger(ClassLoadingException.class.getName());
	
	private static final long serialVersionUID = 2292155480118662068L;
	
	public ClassLoadingException()
	{
		_logger.log(Level.WARNING, "An ClassLoadingException occured");

		_logger.log(Level.WARNING, this.getMessage());
	}
}