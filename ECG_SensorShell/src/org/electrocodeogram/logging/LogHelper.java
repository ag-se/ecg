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
import java.util.logging.SimpleFormatter;

/**
 *
 */
public class LogHelper
{

	private static String LOG_DIR = "ecg_log";

	private static Level DEFAULT_LEVEL = Level.WARNING;

	private static int MAX_FILES = 50;
	
	private static final int FILE_SIZE = 10*1024*1024;
	
	static
	{
		Logger rootLogger = Logger.getLogger("");
		
		Handler[] handlers = rootLogger.getHandlers();
		
		for (Handler handler : handlers)
		{
			handler.setFormatter(new ECGFormatter());
		}
		
	}
	
	public static Logger createLogger(String name)
	{
		Logger logger = Logger.getLogger(name);

		logger.setUseParentHandlers(true);
		
		return logger;
	}

	public static void setLogLevel(Level logLevel)
	{
		if (logLevel == null)
		{
			logLevel = DEFAULT_LEVEL;
		}

		Logger logger = Logger.getLogger("");

		logger.setLevel(logLevel);

		Handler[] handlers = Logger.getLogger("").getHandlers();

		for (Handler handler : handlers)
		{
			handler.setLevel(logLevel);

		}

	}

	public static void setLogFile(String filename) throws SecurityException, IOException
	{
		if (filename == null)
		{
			return;
		}

		String homeDir = System.getProperty("user.home");

		if (homeDir == null || homeDir.equals(""))
		{
			homeDir = ".";
		}

		filename = homeDir + File.separator + LOG_DIR + File.separator + filename;

		// Create a file handler that write log record to a file called my.log
		FileHandler handler = new FileHandler(filename, FILE_SIZE, MAX_FILES, true);
		
		handler.setFormatter(new ECGFormatter());
		
		// Add to the desired logger
		Logger logger = Logger.getLogger("");

		if (logger != null)
		{
			logger.addHandler(handler);
		}

	}

	public static Level getLogLevel(String logLevel)
	{

		if (logLevel == null)
		{
			return DEFAULT_LEVEL;
		}
		else if (logLevel.equalsIgnoreCase("INFO"))
		{
			return Level.INFO;
		}
		else if (logLevel.equals("WARNING"))
		{
			return Level.WARNING;
		}
		else if (logLevel.equals("ERROR"))
		{
			return Level.SEVERE;
		}
		else if (logLevel.equals("DEBUG"))
		{
			return Level.FINEST;
		}
		else
		{
			return DEFAULT_LEVEL;
		}
	}

	private static class ECGFormatter extends Formatter
	{

		/**
		 * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
		 */
		@Override
		public String format(LogRecord record)
		{
			return "[" + record.getLevel()  + "]  " +  new Date(record.getMillis()).toString() + " : " + record.getSourceClassName() + "#" + record.getSourceMethodName() + " : " + record.getMessage() + "\r\n";
		}
		
	}
}
