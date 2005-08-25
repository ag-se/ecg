package org.electrocodeogram.module.registry;

/**
 * If the unique int id that is given to get a reference to a
 * running module is invalid (id < 0) or if a running module
 * witht the given unique int id can not be found, this Exception is thrown.
 */
public class ModuleInstanceException extends Exception
{

	private static final long serialVersionUID = 2250598659169570982L;

	/**
	 * This creates the Exception with the given message.
	 * @param message Is the message for the Exception
	 */
	public ModuleInstanceException(String message)
	{
		super(message);
	}

}
