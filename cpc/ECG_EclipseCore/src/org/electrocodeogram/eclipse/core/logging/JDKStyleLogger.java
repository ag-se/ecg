package org.electrocodeogram.eclipse.core.logging;


import org.apache.commons.logging.Log;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class JDKStyleLogger implements Log
{
	private Logger logger;

	public JDKStyleLogger(Logger logger)
	{
		this.logger = logger;
	}

	public void log(java.util.logging.Level level, String message)
	{
		logger.log(convertLevel(level), message);
	}

	public void entering(String className, String methodName)
	{
		if (logger.isTraceEnabled())
			logger.trace("entering #" + methodName);
	}

	public void entering(String className, String methodName, Object[] parameters)
	{
		if (logger.isTraceEnabled())
			logger.trace("entering #" + methodName + " - " + arrayToString(parameters));
	}

	public void exiting(String className, String methodName)
	{
		if (logger.isTraceEnabled())
			logger.trace("exiting #" + methodName);
	}

	public void exiting(String className, String methodName, Object returnValue)
	{
		if (logger.isTraceEnabled())
			logger.trace("exiting #" + methodName + " - " + returnValue);
	}

	private Level convertLevel(java.util.logging.Level level)
	{
		//TODO: maybe we want special handling of ECGLevel.PACKET here?

		if (level.equals(java.util.logging.Level.SEVERE))
			return Level.FATAL;
		else if (level.equals(java.util.logging.Level.WARNING))
			return Level.WARN;
		else if (level.equals(java.util.logging.Level.INFO) || level.equals(java.util.logging.Level.CONFIG))
			return Level.INFO;
		else if (level.equals(java.util.logging.Level.FINE))
			return Level.DEBUG;
		else
			return Level.TRACE;
	}

	private String arrayToString(Object[] parameters)
	{
		//we could use a stringbuffer here, but it is not clear whether that
		//would improve matters, as this is a method used in trace logging only
		//and the number of elements in the array are usually very small.
		String result = "";
		if (parameters == null)
			return null;

		for (Object o : parameters)
		{
			result += o + ", ";
		}

		return result;
	}

	@Override
	public void debug(Object message)
	{
		logger.debug(message);
	}

	@Override
	public void debug(Object message, Throwable t)
	{
		logger.debug(message, t);
	}

	@Override
	public void error(Object message)
	{
		logger.error(message);
	}

	@Override
	public void error(Object message, Throwable t)
	{
		logger.error(message, t);
	}

	@Override
	public void fatal(Object message)
	{
		logger.fatal(message);
	}

	@Override
	public void fatal(Object message, Throwable t)
	{
		logger.fatal(message, t);
	}

	@Override
	public void info(Object message)
	{
		logger.info(message);
	}

	@Override
	public void info(Object message, Throwable t)
	{
		logger.info(message, t);
	}

	@Override
	public boolean isDebugEnabled()
	{
		return logger.isDebugEnabled();
	}

	@Override
	public boolean isErrorEnabled()
	{
		return logger.isEnabledFor(Level.ERROR);
	}

	@Override
	public boolean isFatalEnabled()
	{
		return logger.isEnabledFor(Level.FATAL);
	}

	@Override
	public boolean isInfoEnabled()
	{
		return logger.isInfoEnabled();
	}

	@Override
	public boolean isTraceEnabled()
	{
		return logger.isTraceEnabled();
	}

	@Override
	public boolean isWarnEnabled()
	{
		return logger.isEnabledFor(Level.WARN);
	}

	@Override
	public void trace(Object message)
	{
		logger.trace(message);
	}

	@Override
	public void trace(Object message, Throwable t)
	{
		logger.trace(message, t);
	}

	@Override
	public void warn(Object message)
	{
		logger.warn(message);
	}

	@Override
	public void warn(Object message, Throwable t)
	{
		logger.warn(message, t);
	}
}
