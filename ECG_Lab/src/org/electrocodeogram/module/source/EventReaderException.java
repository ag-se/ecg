/*
 * Class: EventReaderException
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.source;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * When an error occures during event reading in a
 * <em>SourceModule</em> this <code>Exception</code> is thrown.
 */
public class EventReaderException extends Exception {

    /**
     * Is the <em>Serialization</em> id.
     */
    private static final long serialVersionUID = -64199202002297093L;

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(EventReaderException.class.getName());

    /**
     * Creates the <code>Exception</code>.
     * @param message
     *            Is the message
     */
    public EventReaderException(final String message) {
        super(message);

        logger.entering(this.getClass().getName(), "EventReaderException",
            new Object[] {message});

        logger.log(Level.SEVERE,
            "A SourceModuleException has occured while reading an event.");

        logger.log(Level.SEVERE, this.getMessage());

        logger.exiting(this.getClass().getName(), "EventReaderException");
    }

}
