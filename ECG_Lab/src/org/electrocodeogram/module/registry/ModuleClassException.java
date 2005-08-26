package org.electrocodeogram.module.registry;

/**
 * If the unique String id that is given to get a reference to a
 * availabale module is null or empty or if a available module
 * witht the given unique String id can not be found, this Exception is thrown.
 */
public class ModuleClassException extends Exception
{

	private static final long serialVersionUID = 8400526904223267328L;

	/**
	 * This creates the Exception with the given message.
	 * @param message Is the message for the Exception
	 */
	public ModuleClassException(String message)
	{
		super(message);
	}

}
