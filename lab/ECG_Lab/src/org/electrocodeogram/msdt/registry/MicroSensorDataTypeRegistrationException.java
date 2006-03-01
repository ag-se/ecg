/*
 * Class: MicroSensorDataTypeRegistrationException
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.msdt.registry;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * If an attempt is made to register a {@link org.electrocodeogram.msdt.MicroSensorDataType} with either
 * the MSDT's value or the module's value beeing <code>null</code> or if the
 * <em>MicroSensorDataType</em> is already
 * registered this exception is thrown.
 */
public class MicroSensorDataTypeRegistrationException extends Exception {

    /**
     * This is the <em>Serialization</em> id.
     */
    private static final long serialVersionUID = -3793556996477505178L;

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(MicroSensorDataTypeRegistrationException.class.getName());

    /**
     * This creates the exception.
     * @param message
     *            Is the message
     */
    public MicroSensorDataTypeRegistrationException(final String message) {
        super(message);

        logger.entering(this.getClass().getName(),
            "MicroSensorDataTypeRegistrationException", new Object[] {message});

        logger.log(Level.SEVERE,
            "An MicroSensorDataTypeRegistrationException has occured.");

        logger.log(Level.SEVERE, this.getMessage());

        logger.exiting(this.getClass().getName(),
            "MicroSensorDataTypeRegistrationException");
    }

}
