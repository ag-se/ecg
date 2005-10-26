/*
 * Class: EventPacketQueueOverflowException
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */
package org.electrocodeogram.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * When more events are added to the buffer as allowed by
 * {@link EventSender#MAX_QUEUE_SIZE}, this <em>Exceptiony</em> is
 * thrown.
 */
public class EventPacketQueueOverflowException extends Exception {

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
     * Creates the <em>Exception</em>.
     */
    public EventPacketQueueOverflowException() {
        logger.entering(this.getClass().getName(),
            "EventPacketQueueOverflowException");

        logger.log(Level.SEVERE,
            "An EventPacketQueueOverflowException has occured.");

        logger.exiting(this.getClass().getName(),
            "EventPacketQueueOverflowException");
    }

}
