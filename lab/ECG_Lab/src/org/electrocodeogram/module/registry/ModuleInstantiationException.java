/*
 * Class: ModuleInstantiationException
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.registry;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * Whenever an error occurs during module instantiation, this
 * exception is thrown.
 */
public class ModuleInstantiationException extends Exception {

    /**
     * This is the <em>Serialization</em> id.
     */
    private static final long serialVersionUID = 4209402497277710920L;

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(ModuleInstantiationException.class.getName());

    /**
     * This creates the exception with the given
     * message.
     * @param message
     *            Is the message
     * @param id
     *            Is the unique string id of the
     *            <em>ModulePackage</em> from which the module
     *            instance should have been created
     */
    public ModuleInstantiationException(final String message, final String id) {
        super(message);

        logger.entering(this.getClass().getName(),
            "ModuleInstantiationException", new Object[] {message, id});

        logger.log(Level.WARNING, "An ModuleInstantiationException occured");

        logger.log(Level.WARNING, this.getMessage());

        logger.exiting(this.getClass().getName(),
            "ModuleInstantiationException");
    }

}
