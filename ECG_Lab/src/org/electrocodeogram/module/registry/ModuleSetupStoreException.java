
package org.electrocodeogram.module.registry;

/**
 * This Exception is thrown by the ModuleRegistry, if an error occurs during
 * module-setup storage.
 */
public class ModuleSetupStoreException extends Exception
{

	private static final long serialVersionUID = -5743166486387441291L;

	/**
	 * This creates the Exception with the given message.
	 * @param message Is the message for the Exception
	 */
	public ModuleSetupStoreException(String message)
	{
		super(message);
	}
	
}
