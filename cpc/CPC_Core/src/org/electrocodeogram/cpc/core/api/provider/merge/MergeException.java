package org.electrocodeogram.cpc.core.api.provider.merge;


/**
 * Thrown by an {@link IMergeProvider} in case of critical errors during merge processing.
 * 
 * @author vw
 */
@SuppressWarnings("serial")
public class MergeException extends Exception
{
	/**
	 * Creates a new exception of this type. 
	 */
	public MergeException(String message)
	{
		super(message);
	}

	/**
	 * Creates a new exception of this type. 
	 */
	public MergeException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
