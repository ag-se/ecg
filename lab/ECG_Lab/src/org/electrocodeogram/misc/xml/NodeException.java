/*
 * Class: NodeException
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */
package org.electrocodeogram.misc.xml;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * If a module class is loaded while parsing <em>ModuleSetup</em> or
 * <em>ModuleProperties</em> files and an error occurs, this
 * <code>Exception</code> is thrown.
 */
public class NodeException extends Exception {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper.createLogger(NodeException.class
        .getName());

    /**
     * This is the <em>Serialization</em> id.
     */
    private static final long serialVersionUID = 2292155480118662068L;

    /**
     * This creates the <code>Exception</code>.
     * @param message
     *            Is the message
     * @param nodeName
     *            Is the name of the XML node at which this
     *            <code>Exception</code> has occured
     * @param documentName Is the nmae of the XML document
     */
    public NodeException(final String message, final String nodeName,
        final String documentName) {

        super(message);

        logger.entering(this.getClass().getName(), "NodeException");

        logger
            .log(
                Level.FINE,
                "An NodeException occured at the node "
                                + nodeName
                                + " in document "
                                + documentName
                                + ". As this is expected to occur it is not a problem.");

        logger.log(Level.FINE, this.getMessage());

        logger.exiting(this.getClass().getName(), "NodeException");
    }
}
