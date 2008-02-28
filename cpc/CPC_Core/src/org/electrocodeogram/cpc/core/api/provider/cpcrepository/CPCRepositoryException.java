package org.electrocodeogram.cpc.core.api.provider.cpcrepository;


/**
 * Thrown by the {@link ICPCRepositoryProvider} if any error occurs during processing
 * of store or retrieve operations.
 * 
 * @author vw
 * 
 * @see ICPCRepositoryProvider
 */
public class CPCRepositoryException extends Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception of this type.
	 */
	public CPCRepositoryException(String message)
	{
		super(message);
	}

	/**
	 * Creates a new exception of this type.
	 */
	public CPCRepositoryException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
