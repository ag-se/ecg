/*
 * Class: MicroSensorDataTypeException
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.msdt;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * This is thrown when an error occurs while operating with a
 * {@link MicroSensorDataType}.
 */
public class MicroSensorDataTypeException extends Exception {

    /**
     * This is the <em>Serialization</em> id.
     */
    private static final long serialVersionUID = 2007972379869837079L;

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(MicroSensorDataType.class.getName());

    /**
     * This creates the exception.
     * @param message
     *            Is the message
     */
    public MicroSensorDataTypeException(final String message) {
        super(message);

        logger.entering(this.getClass().getName(),
            "MicroSensorDataTypeException", new Object[] {message});

        logger
            .log(Level.SEVERE, "An MicroSensorDataTypeException has occured.");

        logger.log(Level.SEVERE, this.getMessage());

        logger.exiting(this.getClass().getName(),
            "MicroSensorDataTypeException");
    }

}
