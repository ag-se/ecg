/*
 * Class: ModuleSetupStoreException
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
 * storing a <em>ModuleSetup</em>.
 */
public class ModuleSetupStoreException extends Exception {

    /**
     * This is the <em>Serialization</em> id.
     */
    private static final long serialVersionUID = -5743166486387441291L;

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(ModuleSetupStoreException.class.getName());

    /**
     * Creates the <code>Exception</code>.
     * @param message Is the message
     * @param filename Is the filename from which the <em>ModuleSetup</em> has been loaded
     */
    public ModuleSetupStoreException(final String message, final String filename) {
        super(message);

        logger.entering(this.getClass().getName(), "ModuleSetupStoreException",
            new Object[] {message, filename});
        
        logger.log(Level.SEVERE, "An ModuleSetupStoreException occured while storing the ModuleSetup in the file \"" + filename + "\".");

        logger.log(Level.SEVERE, this.getMessage());
        
        logger.exiting(this.getClass().getName(), "ModuleSetupStoreException");
    }

}
