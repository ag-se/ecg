package org.electrocodeogram.cpc.core.api.provider.xml;


/**
 * Thrown by an {@link IMappingProvider} if any serious error occurs during cpc data mapping.
 * 
 * @author vw
 */
@SuppressWarnings("serial")
public class MappingException extends Exception
{
	/**
	 * 
	 * @param message
	 */
	public MappingException(String message)
	{
		super(message);
	}

	/**
	 * 
	 * @param message
	 * @param cause
	 */
	public MappingException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
