package org.electrocodeogram.module;

/**
 *  If the connection of to modules fails, this Exception is thrown.
 */
public class ModuleConnectionException extends Exception
{
    private static final long serialVersionUID = 8296313792147390311L;
  
    /**
     * This creates a new ModuleConnectionException with the given error message.
     * @param msg Gives the error message for the Exception
     */
    public ModuleConnectionException(String msg)
    {
        super(msg);
    }
   
}
