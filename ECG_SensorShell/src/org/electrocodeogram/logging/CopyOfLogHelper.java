package org.electrocodeogram.logging;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * The LogHelper builds the centralized logging mechanism for the ECG.
 */
public class CopyOfLogHelper
{

	private static Level DEFAULT_LEVEL = Level.WARNING;
	
	private static final String LOG_DIR = "ecg_log";
	
	private static Level _logLevel;
	
	private static String _fileName;
	
	public static Logger createLogger(String name)
	{
		Logger logger = Logger.getLogger(name);
		
		if(_fileName != null)
		{
			FileHandler handler = null;
			try
			{
				handler = new FileHandler(_fileName, true);
			}
			catch (SecurityException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			handler.setFormatter(new Formatter()
			{

				@Override
				public String format(LogRecord record)
				{
					return record.getLevel().toString() + " : " + new Date(
							record.getMillis()).toString() + " : " + record.getSourceClassName() + "#" + record.getSourceMethodName() + " : " + record.getMessage() + "\r\n";
				}
			});
			
			logger.addHandler(handler);
		}
		
		
		if(_logLevel != null)
		{
			logger.setLevel(_logLevel);
		}
		else
		{
			
		}
		
		
		logger.setUseParentHandlers(true);
		
		return logger;
	}

	public static void setLogLevel(Level logLevel)
	{
		if (logLevel == null)
		{
			_logLevel = DEFAULT_LEVEL;
		}

		updateLogLevel();

	}

	private static void updateLogLevel()
	{
		LogManager logManager = LogManager.getLogManager();
		
		Enumeration<String> loggers = logManager.getLoggerNames();
		
		while(loggers.hasMoreElements())
		{
			Logger logger = Logger.getLogger(loggers.nextElement());
			
			logger.setLevel(_logLevel);
			
			Handler[] handlers = logger.getHandlers();

			for (Handler handler : handlers)
			{
				handler.setLevel(_logLevel);

			}
		}
	}
	
	
	private static void addLogFile() throws SecurityException, IOException
	{
//		 Create a file handler that write log record to a file called my.log
		FileHandler handler = new FileHandler(_fileName, true);

		handler.setFormatter(new Formatter()
		{

			@Override
			public String format(LogRecord record)
			{
				return record.getLevel().toString() + " : " + new Date(
						record.getMillis()).toString() + " : " + record.getSourceClassName() + "#" + record.getSourceMethodName() + " : " + record.getMessage() + "\r\n";
			}
		});

		LogManager logManager = LogManager.getLogManager();
		
		Enumeration<String> loggers = logManager.getLoggerNames();

		while(loggers.hasMoreElements())
		{
			Logger logger = Logger.getLogger(loggers.nextElement());
			
			logger.addHandler(handler);
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

		_fileName = homeDir + File.separator + LOG_DIR + File.separator + filename;

		addLogFile();
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
}
