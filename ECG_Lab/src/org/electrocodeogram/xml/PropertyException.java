package org.electrocodeogram.xml;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

public class PropertyException extends Exception
{
	private static Logger _logger = LogHelper.createLogger(PropertyException.class.getName());
	
	private static final long serialVersionUID = 4841697530729167223L;
	
	public PropertyException()
	{
		_logger.log(Level.WARNING, "An PropertyException occured");

		_logger.log(Level.WARNING, this.getMessage());
	}
}