package org.electrocodeogram.module;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * When a module is requestet to set a property to a value that is illegal
 * for the property, the module will throw this Exception and give a cause
 * as a message to the user.
 */
public class ModulePropertyException extends Exception {

    private static Logger logger = LogHelper
        .createLogger(ModulePropertyException.class.getName());

    private static final long serialVersionUID = -7100538090443553026L;

    /**
     * This creates the Exception with the given message.
     * @param message Is the message for the user
     */
    public ModulePropertyException(String message) {
        super(message);

        logger.log(Level.SEVERE, "A ModulePropertyException has occured.");

        logger.log(Level.SEVERE, message);
    }

}
