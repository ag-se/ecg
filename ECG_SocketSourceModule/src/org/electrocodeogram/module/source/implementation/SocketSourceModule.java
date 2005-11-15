/*
 * Class: SocketSourceModule
 * Version: 1.0
 * Date: 19.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.source.implementation;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.ModuleActivationException;
import org.electrocodeogram.module.UIModule;
import org.electrocodeogram.module.event.MessageEvent;
import org.electrocodeogram.module.source.EventReader;
import org.electrocodeogram.module.source.ServerModule;
import org.electrocodeogram.module.source.SourceModule;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.electrocodeogram.modulepackage.ModulePropertyException;

/**
 * This module reads events from sensors over a socket connection. It
 * is implemented as common <em>SocketServer</em>. Each new
 * incoming communication request starts a new
 * {@link SocketServerThread}, which than receives the events as
 * serialized objects from the the client sensor.
 */
public class SocketSourceModule extends SourceModule implements UIModule,
    ServerModule {

    /**
     * This is the highest legal TCP-Port.
     */
    protected static final int MAX_PORT = 65536;

    /**
     * This is the lowest legal TCP-Port.
     */
    protected static final int MIN_PORT = 1024;

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(SocketSourceModule.class.getName());

    /**
     * The TCP-Port the server is listeneing on.
     */
    private int port;

    /**
     * A reference to the <em>SocketServer</em>.
     */
    private SocketServer socketServer;

    /**
     * Creates the module. The constructor is not to be called by
     * users. Instead the <em>ModuleRegistry</em> subsystem of the
     * ECG Labmanages thr creation of modules and calls this
     * construcotr, when neccessary.
     * @param packageId
     *            Is the unique <code>String</code> id of the
     *            <em>ModulePackage</em> oh this module
     * @param name
     *            Is the name that is assigned to this module
     */
    public SocketSourceModule(final String packageId, final String name) {
        super(packageId, name);

        logger.entering(this.getClass().getName(), "SocketSourceModule",
            new Object[] {packageId, name});

        logger.exiting(this.getClass().getName(), "SocketSourceModule");

    }

    /**
     * @see org.electrocodeogram.module.Module#update()
     * This method is not implemented for this module.
     */
    @Override
    public final void update() {
    // not implemented
    }

    /**
     * @see org.electrocodeogram.module.Module#initialize()
     * This method is not implemented for this module.
     */
    @Override
    public final void initialize() {
    // not implemented
    }

    /**
     * @see org.electrocodeogram.module.Module#propertyChanged(org.electrocodeogram.modulepackage.ModuleProperty)
     */
    @Override
    public final void propertyChanged(final ModuleProperty moduleProperty)
        throws ModulePropertyException {

        if (moduleProperty.getName().equals("port")) {
            logger.log(Level.INFO, "Request to set the property: "
                                   + moduleProperty.getName());

            try {
                int portValue = Integer.parseInt(moduleProperty.getValue());

                if (portValue > MIN_PORT && portValue < MAX_PORT) {
                    this.port = portValue;

                    logger
                        .log(Level.INFO, "Property: "
                                         + moduleProperty.getName() + " set.");

                    if (this.isActive()) {
                        this.deactivate();

                        this.activate();

                    }

                } else {
                    logger.log(Level.WARNING,
                        "The value for the port property must be a number greater than "
                                        + MIN_PORT + " and less then "
                                        + MAX_PORT + ".");

                    throw new ModulePropertyException(
                        "The value for the port property must be a number greater than "
                                        + MIN_PORT + " and less then "
                                        + MAX_PORT + ".", this.getName(), this
                            .getId(), moduleProperty.getName(), moduleProperty
                            .getValue());
                }

            } catch (NumberFormatException e) {
                logger.log(Level.WARNING,
                    "The value for the port property must be a number greater than "
                                    + MIN_PORT + " and less then " + MAX_PORT
                                    + ".");

                throw new ModulePropertyException(
                    "The value for the port property must be a number greater than "
                                    + MIN_PORT + " and less then " + MAX_PORT
                                    + ".", this.getName(), this.getId(),
                    moduleProperty.getName(), moduleProperty.getValue());
            } catch (ModuleActivationException e) {
                throw new org.electrocodeogram.modulepackage.ModulePropertyException(
                    e.getMessage(), this.getName(), this.getId(),
                    moduleProperty.getName(), moduleProperty.getValue());
            }

        }
    }

    /**
     * @see org.electrocodeogram.module.source.SourceModule#getEventReader()
     */
    @Override
    public final EventReader[] getEventReader() {

        logger.entering(this.getClass().getName(), "getEventReader");

        logger.exiting(this.getClass().getName(), "getEventReader",
            this.socketServer.getEventReader());

        return this.socketServer.getEventReader();
    }

    /**
     * @see org.electrocodeogram.module.source.SourceModule#preStart()
     */
    @Override
    public final void preStart() {

        logger.entering(this.getClass().getName(), "preStart");

        if (this.socketServer == null) {
            logger.log(Level.FINE,
                "No SocketServer started yet. Going to start one...");

            this.socketServer = new SocketServer(this, this.port);

            this.socketServer.start();

            logger.log(Level.FINE, "A SocketServer has been started.");
        } else {
            logger.log(Level.FINE, "A SocketServer is already started.");
        }

        logger.exiting(this.getClass().getName(), "preStart");
    }

    /**
     * @see org.electrocodeogram.module.source.SourceModule#postStop()
     */
    @Override
    public final void postStop() {

        logger.entering(this.getClass().getName(), "postStop");

        if (this.socketServer != null) {
            logger.log(Level.FINE,
                "A SocketServer is present. Going to shut it down...");

            this.socketServer.shutDown();

            this.socketServer = null;

            logger.log(Level.FINE, "The SocketServer has been shut down.");
        } else {
            logger.log(Level.FINE, "The SocketServer is already shut down.");
        }

        logger.exiting(this.getClass().getName(), "postStop");
    }

    /**
     * @see org.electrocodeogram.module.UIModule#getPanelName()
     */
    public final String getPanelName() {

        logger.entering(this.getClass().getName(), "getPanelName");

        logger.exiting(this.getClass().getName(), "getPanelName", "Connected Sensors");

        return "Connected Sensors";
    }

    /**
     * @see org.electrocodeogram.module.UIModule#getPanel()
     */
    public final JPanel getPanel() {

        logger.entering(this.getClass().getName(), "getPanel");
        
        JPanel panel = new JPanel();

        String message = "";

        if (this.socketServer == null) {

            MessageEvent event = new MessageEvent(
                "The module has not been started yet.",
                MessageEvent.MessageType.ERROR, getName(), getId());

            getGuiNotifiator().fireMessageNotification(event);

            logger.exiting(this.getClass().getName(), "getPanel", null);
            
            return null;

        }

        int count = this.socketServer.getSensorCount();

        message += "Connected to " + count + " ECG clients.\n";

        for (int i = 0; i < count; i++) {
            message += "Client " + i + ": "
                       + this.socketServer.getSensorNames()[i] + " at "
                       + this.socketServer.getSensorAddresses()[i].toString()
                       + "\n";
        }

        panel.add(new JLabel(message));

        logger.exiting(this.getClass().getName(), "getPanel", panel);
        
        return panel;
    }
}
