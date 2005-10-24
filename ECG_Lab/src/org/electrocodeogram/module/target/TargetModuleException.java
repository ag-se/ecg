/*
 * Class: TargetModuleException
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */
package org.electrocodeogram.module.target;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * This is thrown if an error occurs while activatin a
 * <em>TargetModule</em>.
 */
public class TargetModuleException extends Exception {

    /**
     * This is the <em>Serialization</em> id.
     */
    private static final long serialVersionUID = -5662660466138968398L;

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(TargetModuleException.class.getName());

    /**
     * This creates the <code>Exception</code>.
     * @param message
     *            Is the message
     * @param moduleName
     *            Is the name of <em>TargetModule</em> that has
     *            caused the <code>Exception</code>
     */
    public TargetModuleException(final String message, final String moduleName) {
        super(message);

        logger.entering(this.getClass().getName(), "ModuleConnectionException",
            new Object[] {message, moduleName});

        logger.log(Level.SEVERE,
            "A TargetModuleException has occured in module \"" + moduleName
                            + "\".");

        logger.log(Level.SEVERE, this.getMessage());

        logger.exiting(this.getClass().getName(), "TargetModuleException");
    }

}
