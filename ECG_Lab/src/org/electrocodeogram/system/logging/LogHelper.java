/**
 * 
 */
package org.electrocodeogram.system.logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class LogHelper
{
	
	private static Level _logLevel = Level.WARNING;
		
	public static Logger createLogger(Object object)
	{
		Logger logger = Logger.getLogger(object.getClass().getName());
		
		logger.setUseParentHandlers(true);
		
		logger.setLevel(_logLevel);
		
		return logger;
	}
	
	public static void setLogLevel(Level logLevel)
	{
		if(logLevel != null)
		{
			_logLevel = logLevel;
		}
		
		Handler[] handlers = Logger.getLogger("").getHandlers();
		
		for(Handler handler : handlers)
		{
			handler.setLevel(_logLevel);
			
		}
	}

	
}
