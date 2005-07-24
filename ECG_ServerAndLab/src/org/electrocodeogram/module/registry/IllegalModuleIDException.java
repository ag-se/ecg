package org.electrocodeogram.module.registry;

/**
 * 
 * @author 7oas7er
 *
 */public class IllegalModuleIDException extends Exception
{

   
    private static final long serialVersionUID = 2586499435072988832L;

    /**
     * 
     */
    public IllegalModuleIDException()
    {
        super();
    }

    /**
     * @param message
     */
    public IllegalModuleIDException(String message)
    {
        super(message);
    }

    /**
     * @param cause
     */
    public IllegalModuleIDException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public IllegalModuleIDException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
