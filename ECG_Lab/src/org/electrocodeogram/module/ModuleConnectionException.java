/*
 * Class: ModuleConnectionException
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * If the connection of modules fails, this <em>Exception</em> is
 * thrown.
 */
public class ModuleConnectionException extends Exception {

    /**
     * Is the <em>Serialization</em> id.
     */
    private static final long serialVersionUID = 8296313792147390311L;

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(ModuleConnectionException.class.getName());

    /**
     * This creates the <em>Exception</em> with the given error
     * message and logs it.
     * @param msg
     *            Gives the error message for the Exception
     * @param fromModule
     *            Is the name of the module to connect to
     * @param fromModuleId
     *            Is the unique int id of the module to connect to
     * @param toModuleName
     *            Is the name of the module to be connnected
     * @param toModuleId
     *            Is the unique int id of the module to be connected
     */
    public ModuleConnectionException(final String msg, final String fromModule,
        final int fromModuleId, final String toModuleName, final int toModuleId) {

        super(msg);

        logger.entering(this.getClass().getName(), "ModuleConnectionException",
            new Object[] {msg, fromModule, new Integer(fromModuleId), toModuleName,
                new Integer(toModuleId)});

        logger.log(Level.SEVERE,
            "An ModuleConnectionException occured while connecting module \""
                            + fromModule + "\" having id \"" + fromModuleId
                            + "\" with module \"" + toModuleName
                            + "\" having id \"" + toModuleId + "\".");

        logger.log(Level.SEVERE, this.getMessage());

        logger.exiting(this.getClass().getName(), "ModuleConnectionException");
    }

}
