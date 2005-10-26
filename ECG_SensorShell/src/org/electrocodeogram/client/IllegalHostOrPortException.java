/*
 * Class: IllegalHostOrPortException
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * If the "host" or "port" values are malformed when being passed to the
 * <em>EventSender</em> for connection to the ECG Lab, this <code>Exception</code> is thrown.
 */
public class IllegalHostOrPortException extends Exception {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(IllegalHostOrPortException.class.getName());

    /**
     * This is the <em>Serialization</em> id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates the <em>Exception</em>.
     */
    public IllegalHostOrPortException() {
        logger
            .entering(this.getClass().getName(), "IllegalHostOrPortException");

        logger.log(Level.SEVERE, "An IllegalHostOrPortException has occured.");

        logger.exiting(this.getClass().getName(), "IllegalHostOrPortException");
    }
}
