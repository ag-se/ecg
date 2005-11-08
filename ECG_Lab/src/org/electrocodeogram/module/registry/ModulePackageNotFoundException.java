/*
 * Class: ModulePackageNotFoundException
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.registry;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * If the unique <code>String</code> id that is given to get a
 * <em>ModulePackage</em> is null or empty or if a
 * <em>ModulePackage</em> with this id can not be found, this
 * <code>Exception</code> is thrown.
 */
public class ModulePackageNotFoundException extends Exception {

    /**
     * The <em>Serialization</em> id.
     */
    private static final long serialVersionUID = 8400526904223267328L;

    /**
     * < The logger.
     */
    private static Logger logger = LogHelper
        .createLogger(ModulePackageNotFoundException.class.getName());

    /**
     * This creates the <code>Exception</code> with the given
     * message.
     * @param message
     *            Is the message for the Exception
     * @param id
     *            Is the unique <code>String</code> id that has been
     *            requested
     */
    public ModulePackageNotFoundException(final String message, final String id) {
        super(message);

        logger.entering(this.getClass().getName(),
            "ModulePackageNotFoundException", new Object[] {message, id});

        logger
            .log(
                Level.SEVERE,
                "A ModulePackageNotFoundException has occured because the requested ModulePackage id + \""
                                + id + "\" + is invalid or unknown.");

        logger.log(Level.SEVERE, this.getMessage());

        logger.exiting(this.getClass().getName(),
            "ModulePackageNotFoundException");
    }

}
