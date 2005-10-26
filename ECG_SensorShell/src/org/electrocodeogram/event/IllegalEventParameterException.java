package org.electrocodeogram.event;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * If the parameters given to an EventPacket are not valid in syntax,
 * this exception is thrown during EventPacket creation.
 */
public class IllegalEventParameterException extends Exception
{
    private static final long serialVersionUID = 1871343961009715536L;

    private static Logger _logger = LogHelper.createLogger(IllegalEventParameterException.class.getName());
       
    public IllegalEventParameterException(String message)
    {
    	super(message);
    	
    	_logger.log(Level.WARNING,"An IllegalEventParameterException occured.");
    	
    	_logger.log(Level.WARNING,this.getMessage());
    }
}
