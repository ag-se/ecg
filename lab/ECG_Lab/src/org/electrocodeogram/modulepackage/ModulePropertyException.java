/*
 * Class: ModulePropertyException
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.modulepackage;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * When a module is requestet to set a property to a value that is
 * causing a problem in the module this exception is
 * thrown. After that the property is reset to its original value.
 */
public class ModulePropertyException extends Exception {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(ModulePropertyException.class.getName());

    /**
     * This is the <em>Serialization</em> id.
     */
    private static final long serialVersionUID = -7100538090443553026L;

    /**
     * This creates the exception with the given message.
     * @param message
     *            Is the message
     * @param moduleName
     *            Is the name of the module that caused the
     *            exception
     * @param moduleId
     *            Is the unique int of the module that caused the
     *            exception
     * @param propertyName
     *            Is the name of the property
     * @param propertyValue
     *            Is the value of the property
     */
    public ModulePropertyException(final String message,
        final String moduleName, final int moduleId, final String propertyName,
        final String propertyValue) {
        super(message);

        logger.entering(this.getClass().getName(), "ModulePropertyException",
            new Object[] {moduleName, new Integer(moduleId), propertyName,
                propertyValue});

        logger.log(Level.SEVERE, "A ModulePropertyException has occured.");

        logger.log(Level.SEVERE, message);

        logger.exiting(this.getClass().getName(), "ModulePropertyException");
    }

}
