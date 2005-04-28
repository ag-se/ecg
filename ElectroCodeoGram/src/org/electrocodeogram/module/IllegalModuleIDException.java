/*
 * Created on 13.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.module;

/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class IllegalModuleIDException extends RuntimeException
{

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
