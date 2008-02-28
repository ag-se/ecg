package org.electrocodeogram.cpc.optimiser.task;


/**
 * Thrown by {@link IOptimiserTask}s.
 * 
 * @author vw
 * 
 * @see IOptimiserTask
 */
@SuppressWarnings("serial")
public class OptimiserTaskException extends Exception
{
	public OptimiserTaskException(String message)
	{
		super(message);
	}

	public OptimiserTaskException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
