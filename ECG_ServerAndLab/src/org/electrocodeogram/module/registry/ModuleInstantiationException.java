package org.electrocodeogram.module.registry;

/**
 * Whenever an error occurs during module instantiation, this Exception is thrown.
 *
 */public class ModuleInstantiationException extends Exception
{

    /**
     * This creates the Exception with the given message.
     * @param message
     */
    public ModuleInstantiationException(String message)
    {
        super(message);
    }

    private static final long serialVersionUID = 4209402497277710920L;


}
