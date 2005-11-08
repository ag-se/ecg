/*
 * Class: SourceModule
 * Version: 1.0
 * Date: 19.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.source;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.modulepackage.ModuleType;

/**
 * This is the superclass for all
 * {@link org.electrocodeogram.module.Module.ModuleType#SOURCE_MODULE}
 * implementations.
 */
public abstract class SourceModule extends Module {

    /**
     * This is this class' logger.
     */
    private static Logger logger = LogHelper.createLogger(SourceModule.class
        .getName());

    /**
     * Creates the <em>SourceModule</em>.
     * @param id
     *            Is the id of the <em>ModulePackage</em>
     * @param name
     *            Is the name to assign to this module
     */
    public SourceModule(final String id, final String name) {
        super(ModuleType.SOURCE_MODULE, id, name);

        logger.entering(this.getClass().getName(), "SourceModule",
            new Object[] {id, name});

        logger.exiting(this.getClass().getName(), "SourceModule");

        initialize();
    }

    /**
     * This method is called during module activation and starts the
     * {@link EventReader} of this <em>SourceModule</em>.
     * @throws SourceModuleException
     *             In <code>Exception</code> is thrown while the
     *             module is activated
     */
    public final void startReader() throws SourceModuleException {

        logger.entering(this.getClass().getName(), "startReader");

        int count = 0;

        preStart();

        if (this instanceof ServerModule) {

            logger
                .log(
                    Level.FINE,
                    "This module is a ServerModule. Activation of its EventReader is left to the module.");

        } else {

            logger
                .log(Level.FINE,
                    "This module is not a ServerModule. Going to activate its EventReader.");

            EventReader[] eventReader = getEventReader();

            if (eventReader == null || eventReader.length == 0) {

                logger.log(Level.FINE,
                    "There are no EventReader in SourceModule "
                                    + this.getName());

                logger.exiting(this.getClass().getName(), "startReader");

                throw new SourceModuleException("There are no EventReader",
                    this.getName());
            }

            for (EventReader reader : eventReader) {

                if (reader != null) {
                    reader.startReader();

                    count++;
                }

            }

            logger.log(Level.INFO,
                count + " EventReader has/have been started for SourceModule "
                                + this.getName());

            if (count == 0) {
                logger.log(Level.FINE,
                    "There are no EventReader in SourceModule "
                                    + this.getName());

                logger.exiting(this.getClass().getName(), "startReader");

                throw new SourceModuleException("There are no EventReader",
                    this.getName());
            }
        }
        logger.exiting(this.getClass().getName(), "startReader");
    }

    /**
     * This is called when the module is deactivated. It stops all
     * running {@link EventReader}.
     */
    public final void stopReader() {

        logger.entering(this.getClass().getName(), "stopReader");

        EventReader[] eventReader = getEventReader();

        if (eventReader == null || eventReader.length == 0) {

            logger.log(Level.FINE, "There are no EventReader in SourceModule "
                                   + this.getName());

            logger.exiting(this.getClass().getName(), "stopReader");

            postStop();

            return;
        }

        for (EventReader reader : eventReader) {
            if (reader != null) {
                reader.stopReader();
            }
        }

        logger.log(Level.INFO,
            eventReader.length
                            + "EventReader have been stopped for SourceModule "
                            + this.getName());

        postStop();

        logger.exiting(this.getClass().getName(), "stopReader");
    }

    /**
     * This method is called by the {@link EventReader} implementation
     * to pass read or received
     * {@link org.electrocodeogram.event.ValidEventPacket} events to
     * this <em>SourceModule</em>.
     * @param eventPacket
     *            Is the read event
     */
    protected final void append(final WellFormedEventPacket eventPacket) {
        logger.entering(this.getClass().getName(), "append",
            new Object[] {eventPacket});

        if (eventPacket == null) {
            logger.log(Level.WARNING,
                "Parameter \"eventPacket\" is null. Ignoring event.");

            logger.exiting(this.getClass().getName(), "append");

            return;
        }

        ValidEventPacket validEventPacket = null;

        try {
            validEventPacket = new ValidEventPacket(this.getId(), eventPacket
                .getTimeStamp(), eventPacket.getSensorDataType(), eventPacket
                .getArgList());

            logger.log(Level.INFO,
                "An event has been appended to the SourceModule: "
                                + this.getName());

            logger.log(ECGLevel.PACKET, validEventPacket.toString());
        } catch (IllegalEventParameterException e) {
            logger.log(Level.WARNING,
                "An Exception occured while appending an event to the SourceModule: "
                                + this.getName());
        }

        if (validEventPacket != null) {
            sendEventPacket(validEventPacket);
        }

        logger.exiting(this.getClass().getName(), "append");
    }

    /**
     * This method is not implemented for a <em>SourceModule</em>.
     * @param eventPacket
     *            not used
     */
    @Override
    public final void receiveEventPacket(@SuppressWarnings("unused")
    final ValidEventPacket eventPacket) {
        logger.entering(this.getClass().getName(), "receiveEventPacket",
            new Object[] {eventPacket});

        logger.exiting(this.getClass().getName(), "receiveEventPacket");

        return;
    }

    /**
     * @see org.electrocodeogram.module.Module#initialize()
     */
    @Override
    public abstract void initialize();

    /**
     * This is to be implemented by all actual <em>SourceMosules</em>.
     * It returns the module's {@link EventReader}.
     * @return The module's {@link EventReader}
     */
    public abstract EventReader[] getEventReader();

    /**
     * This is to be implemented by all actual <em>SourceMosules</em>.
     * It is called during module activation before the
     * {@link EventReader} are started.
     */
    public abstract void preStart() throws SourceModuleException;

    /**
     * This is to be implemented by all actual <em>SourceMosules</em>.
     * It is called during module deactivation after the
     * {@link EventReader} are stopped.
     */
    public abstract void postStop();
}
