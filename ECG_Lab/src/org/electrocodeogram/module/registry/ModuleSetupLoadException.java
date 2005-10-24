/*
 * Class: ModuleSetupLoadException
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.registry;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * This <code>Exception</code> is thrown if an error occurs while
 * loading a <em>ModuleSetup</em>.
 */
public class ModuleSetupLoadException extends Exception {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(ModuleSetupLoadException.class.getName());

    /**
     * This is the <em>Serialization</em> id.
     */
    private static final long serialVersionUID = 8611067658025512073L;

    /**
     * Creates the <code>Exception</code>.
     * @param string Is the message
     * @param filename Is the filename from which the <em>ModuleSetup</em> has been loaded
     */
    public ModuleSetupLoadException(final String string, final String filename) {
        super(string);

        logger.entering(this.getClass().getName(), "ModuleSetupLoadException",
            new Object[] {string, filename});

        logger
            .log(
                Level.SEVERE,
                "An ModuleSetupLoadException occured while loading the ModuleSetup from file \""
                                + filename + "\".");

        logger.log(Level.SEVERE, this.getMessage());

        logger.exiting(this.getClass().getName(), "ModuleSetupLoadException");
    }

}
