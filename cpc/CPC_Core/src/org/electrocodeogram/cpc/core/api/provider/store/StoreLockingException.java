package org.electrocodeogram.cpc.core.api.provider.store;


/**
 * Thrown by the {@link IStoreProvider} if a caller violates the locking protocol.
 * 
 * @author vw
 */
@SuppressWarnings("serial")
public class StoreLockingException extends Exception
{
	/**
	 * Creates a new exception of this type.
	 */
	public StoreLockingException(String message)
	{
		super(message);
	}
}
