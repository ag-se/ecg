
package org.electrocodeogram.module.registry;

/**
 * This Exception can be thrown during the initialization of the module classes.
 * It tells that something went wrong while the ModuleRegistry tried to load
 * module classes from themodule directory. 
 */
public class ModuleClassLoaderInitializationException extends Exception
{

 
    private static final long serialVersionUID = 3786132315837665255L;

    /**
     * This creates the Exception with the given message.
     * @param message Is the message for the Exception
     */
    public ModuleClassLoaderInitializationException(String message)
    {
        super(message);
    }

 
}
