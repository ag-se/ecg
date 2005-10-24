/*
 * Class: ModuleActivationException
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * When an error occures during the activation of a module, this
 * <em>Exception</em> is thrown.
 */
public class ModuleActivationException extends Exception {

    /**
     * The <em>Serialization</em> id.
     */
    private static final long serialVersionUID = -4365985882032349845L;

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(ModuleActivationException.class.getName());

    /**
     * Creates the <em>Exception</em> and logs the message.
     * @param message
     *            Is the message
     * @param moduleName
     *            Is the name of the module that should have been
     *            activated
     * @param moduleId
     *            Is the unique int id of the module that should have
     *            been activated
     */
    public ModuleActivationException(final String message,
        final String moduleName, final int moduleId) {
        super(message);

        logger.entering(this.getClass().getName(), "ModuleActivationException",
            new Object[] {message, moduleName, new Integer(moduleId)});

        logger.log(Level.SEVERE,
            "A ModuleActivationException has occured while activating the module \""
                            + moduleName + "\" with id \"" + moduleId + "\".");

        logger.log(Level.SEVERE, this.getMessage());

        logger.exiting(this.getClass().getName(), "ModuleActivationException");
    }

}
