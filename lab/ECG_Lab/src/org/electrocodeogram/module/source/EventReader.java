/*
 * Class: EventReader
 * Version: 1.0
 * Date: 19.10.2005
 * By: Frank@Schlesinger.com
 */
package org.electrocodeogram.module.source;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.logging.LogHelper.ECGLevel;

/**
 * A {@link org.electrocodeogram.module.source.SourceModule} is
 * reading events into the ECG Lab by using one or multiple
 * <code>EventReader</code>. Each <code>EventReader</code> is a <code>Thread</code>,
 * so that reading is done asynchroneously. The actual implementation
 * of an <code>EventReader</code> has to provide the {@link #read()}
 * method, which reads in one single event from the source and
 * returns it.
 */
public abstract class EventReader extends Thread {

    /**
     * This is this class' logger.
     */
    private static Logger logger = LogHelper.createLogger(EventReader.class
        .getName());

    /**
     * The state of the <code>EventReader</code>.
     */
    private boolean run;

    /**
     * Is the module to pass events to.
     */
    private SourceModule mySourceModule;

    /**
     * Creates the <code>EventReader</code>.
     * @param sourceModule
     *            Is the module to pass events to
     */
    public EventReader(final SourceModule sourceModule) {

        logger.entering(this.getClass().getName(), "EventReader");

        this.mySourceModule = sourceModule;

        logger.exiting(this.getClass().getName(), "EventReader");
    }

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public final void run() {

        logger.entering(this.getClass().getName(), "run");

        while (this.run) {
            WellFormedEventPacket packet;

            try {
                packet = read();

                if (packet == null) {
                    logger.log(Level.FINE,
                        "Null has been read by the the EventReader of "
                                        + this.mySourceModule.getName());

                } else {
                    logger.log(Level.FINE,
                        "A new event has been read by the EventReader of "
                                        + this.mySourceModule.getName());
                    logger.log(ECGLevel.PACKET, packet.toString());

                    this.mySourceModule.append(packet);

                    logger.log(Level.FINE,
                        "The event has been passed to the SourceModule "
                                        + this.mySourceModule.getName());
                }

            } catch (EventReaderException e) {

                logger.log(Level.WARNING,
                    "An Exception occured while reading an event in the EventReader of "
                                    + this.mySourceModule.getName());

                stopReader();
            }

        }

        logger.exiting(this.getClass().getName(), "run");

    }

    /**
     * This method is to be implemented by all actual
     * <code>EventReader</code> implementations. It shall read one
     * single event from the source and return it.
     * @return The event that has been read
     * @throws EventReaderException
     *             If any exception occurs during event
     *             reading
     */
    public abstract WellFormedEventPacket read() throws EventReaderException;

    /**
     * Starts the <code>EventReader</code>.
     */
    public final void startReader() {

        logger.entering(this.getClass().getName(), "startReader");

        this.run = true;

        if (!this.isAlive()) {
            logger.log(Level.FINE, "EventReader is not active.");

            this.start();

            logger.log(Level.FINE, "The EventReader for "
                                   + this.mySourceModule.getName()
                                   + " has been started.");
        } else {
            logger.log(Level.FINE, "EventReader is already active.");
        }

        logger.exiting(this.getClass().getName(), "startReader");
    }

    /**
     * Stops the <code>EventReader</code>.
     */
    public final void stopReader() {

        logger.entering(this.getClass().getName(), "stopReader");

        this.run = false;

        logger.log(Level.FINE, "The EventReader for "
                               + this.mySourceModule.getName()
                               + " has been stopped.");

        logger.exiting(this.getClass().getName(), "stopReader");
    }

    /**
     * This method returns the <code>SourceModule</code> to which this <code>EventReader</code>
     * is appending read events.
     * @return The <code>SourceModule</code> of this <code>EventReader</code>
     */
    public final SourceModule getSourceModule() {
        logger.entering(this.getClass().getName(), "getSourceModule");

        logger.exiting(this.getClass().getName(), "getSourceModule",
            this.mySourceModule);

        return this.mySourceModule;
    }
}
