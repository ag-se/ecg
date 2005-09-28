package org.electrocodeogram.module;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 *  If the connection of to modules fails, this Exception is thrown.
 */
public class ModuleConnectionException extends Exception
{
    private static final long serialVersionUID = 8296313792147390311L;
  
    private static Logger _logger = LogHelper.createLogger(ModuleConnectionException.class.getName());

    
    /**
     * This creates a new ModuleConnectionException with the given error message.
     * @param msg Gives the error message for the Exception
     */
    public ModuleConnectionException(String msg)
    {
        super(msg);
        
        _logger.log(Level.WARNING, "An ModuleConnectionException occured");

		_logger.log(Level.WARNING, this.getMessage());
    }
   
}
