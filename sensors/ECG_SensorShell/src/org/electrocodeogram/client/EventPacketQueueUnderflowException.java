/*
 * Class: EventPacketQueueUnderflowException
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */
package org.electrocodeogram.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * when the event buffer contains no events but an event shall be
 * removed, this exception is thrown.
 */
public class EventPacketQueueUnderflowException extends Exception {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(EventPacketQueueOverflowException.class.getName());

    /**
     * This is the <em>Serialization</em> id.
     */
    private static final long serialVersionUID = 870916601241806158L;

    /**
     * Creates the exception.
     */
    public EventPacketQueueUnderflowException() {
        logger.entering(this.getClass().getName(),
            "EventPacketQueueUnderflowException");

        logger.log(Level.SEVERE,
            "An EventPacketQueueUnderflowException has occured.");

        logger.exiting(this.getClass().getName(),
            "EventPacketQueueUnderflowException");
    }

}
