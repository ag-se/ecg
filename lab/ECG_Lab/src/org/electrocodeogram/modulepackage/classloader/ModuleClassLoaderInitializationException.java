/*
 * Class: ModuleClassLoaderInitializationException
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.modulepackage.classloader;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * This exception is thrown during the initialization
 * of the {@link ModuleClassLoaderInitializationException} if any
 * error occurs.
 */
public class ModuleClassLoaderInitializationException extends Exception {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(ModuleClassLoaderInitializationException.class.getName());

    /**
     * This is the <em>Serialization</em> id.
     */
    private static final long serialVersionUID = 3786132315837665255L;

    /**
     * This creates the exception with the given
     * message.
     * @param message
     *            Is the message for the Exception
     */
    public ModuleClassLoaderInitializationException(final String message) {
        super(message);

        logger.entering(this.getClass().getName(),
            "ModuleClassLoaderInitializationException", new Object[] {message});

        logger.log(Level.WARNING,
            "A ModuleClassLoaderInitializationException has occured.");

        logger.log(Level.WARNING, this.getMessage());

        logger.entering(this.getClass().getName(),
            "ModuleClassLoaderInitializationException");
    }

}
