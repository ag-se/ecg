/*
 * Class: IllegalEventParameterException
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.event;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * If event data is not wellformed or not valid this exception is thrown.
 */
public class IllegalEventParameterException extends Exception {

    /**
     * The <em>Serialization</em> id.
     */
    private static final long serialVersionUID = 1871343961009715536L;

    /**
     * The logger.
     */
    private static Logger logger = LogHelper
        .createLogger(IllegalEventParameterException.class.getName());

    /**
     * Creates the exception.
     * @param message The message
     */
    public IllegalEventParameterException(final EventPacket event, 
    		final Level logLevel, final String message) {
        super(message);

        logger.log(logLevel, "An IllegalEventParameterException occured.");
        logger.log(logLevel, "  in event " + event.toString());
        logger.log(logLevel, "  because: " + this.getMessage());
    }
}
