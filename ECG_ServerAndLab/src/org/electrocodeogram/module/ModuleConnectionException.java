/*
 * Created on 11.03.2005
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
public class ModuleConnectionException extends Exception
{
    private String message;
    
    public ModuleConnectionException(String msg)
    {
        super(msg);
        this.message = msg;
    }

    /**
     * 
     * @uml.property name="message"
     */
    public String getMessage() {
        return message;
    }

}
