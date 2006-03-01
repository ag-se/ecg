/*
 * Class: ModuleInstanceException
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.registry;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * If the unique int id provided to get a module instance is invalid (id &lt;
 * 0) or if a module instance with the given unique int id can not be
 * found, this exception is thrown.
 */
public class ModuleInstanceNotFoundException extends Exception {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(ModuleInstanceNotFoundException.class.getName());

    /**
     * This is the <em>Serialization</em> id.
     */
    private static final long serialVersionUID = 2250598659169570982L;

    /**
     * This creates the exception with the given
     * message.
     * @param message
     *            Is the message for the exception
     * @param id Is the unique int id of the module instance that has been requested
     */
    public ModuleInstanceNotFoundException(final String message, final int id) {
        super(message);

        logger.entering(this.getClass().getName(),
            "ModuleInstanceNotFoundException", new Object[] {new Integer(id)});

        logger.log(Level.SEVERE,
            "An ModuleInstanceNotFoundException because the module id " + id
                            + " is invalid or could not be found.");

        logger.log(Level.SEVERE, this.getMessage());

        logger.entering(this.getClass().getName(),
            "ModuleInstanceNotFoundException");
    }

}
