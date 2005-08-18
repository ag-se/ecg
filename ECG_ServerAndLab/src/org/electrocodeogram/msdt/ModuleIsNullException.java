package org.electrocodeogram.msdt;

/**
 * If a Module object is passed to a method as a parameter and
 * the value of the Module object is \"null\", this Exception
 * can be thrown by the method.
 */
public class ModuleIsNullException extends Exception
{
   
    /**
     * This creates the Exception with the given message.
     * @param message Is the message for the Eception
     */
    public ModuleIsNullException(String message)
    {
        super(message);
    }

    private static final long serialVersionUID = 3524235565432788027L;

}
