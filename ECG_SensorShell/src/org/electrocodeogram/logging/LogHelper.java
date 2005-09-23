/**
 * 
 */
package org.electrocodeogram.logging;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 */
public class LogHelper
{
	
	private static Level DEFAULT_LEVEL = Level.WARNING;
		
	public static Logger createLogger(Object object)
	{
		Logger logger = Logger.getLogger("");
		
		logger.setUseParentHandlers(true);
		
		return logger;
	}
	
	public static void setLogLevel(Level logLevel)
	{
		if(logLevel == null)
		{
			logLevel = DEFAULT_LEVEL; 
		}
		
		Logger logger = Logger.getLogger("");
		
		logger.setLevel(logLevel);
		
		Handler[] handlers = Logger.getLogger("").getHandlers();
		
		for(Handler handler : handlers)
		{
			handler.setLevel(logLevel);
			
		}
		
	}

	public static void setLogFile(String filename) throws SecurityException, IOException
	{
		if(filename == null)
		{
			return;
		}
		    // Create a file handler that write log record to a file called my.log
	        FileHandler handler = new FileHandler(filename,true);
	        
	        handler.setFormatter(new Formatter() {

				@Override
				public String format(LogRecord record)
				{
					return new Date(record.getMillis()).toString() + " : " + record.getLoggerName() + " : " + record.getMessage() + "\r\n"; 
				}});
	    
	        // Add to the desired logger
	        Logger logger = Logger.getLogger("");
	        
	        if(logger != null)
	        {
	        	logger.addHandler(handler);
	        }
	        
	    

	}

	public static Level getLogLevel(String logLevel)
	{
		
		if(logLevel == null)
		{
			return DEFAULT_LEVEL;
		}
		else if(logLevel.equalsIgnoreCase("INFO"))
		{
			return Level.INFO;
		}
		else if(logLevel.equals("WARNING"))
		{
			return Level.WARNING;
		}
		else if(logLevel.equals("ERROR"))
		{
			return Level.SEVERE;
		}
		else if(logLevel.equals("DEBUG"))
		{
			return Level.FINEST;
		}
		else
		{
			return DEFAULT_LEVEL;
		}
	}
}
