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
 * <em>EventReader</em>. Each <em>EventReader</em> is a Thread,
 * so that reading is done asynchroneously. The actual implementation
 * of an <em>EventReader</em> has to provide the {@link #read()}
 * method, which shall read in one single event from the source and
 * return it.
 */
public abstract class EventReader extends Thread {

    /**
     * This is this class' logger.
     */
    private static Logger logger = LogHelper.createLogger(EventReader.class
        .getName());

    /**
     * The state of the <em>EventReader</em>.
     */
    private boolean run;

    /**
     * Is the module to pass events to.
     */
    private SourceModule mySourceModule;

    /**
     * Creates the <em>EventReader</em>.
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

                logger.log(Level.FINE,
                    "A new event has been read by the EventReader of "
                                    + this.mySourceModule.getName());

                logger.log(ECGLevel.PACKET, packet.toString());

                this.mySourceModule.append(packet);

                logger.log(Level.FINE,
                    "The event has been passed to the SourceModule "
                                    + this.mySourceModule.getName());

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
     * <em>EventReader</em> implementations. It shall read one
     * single event from the source and return it.
     * @return The single event that has been read
     * @throws EventReaderException
     *             If any <em>Exception</em> occurs during event
     *             reading
     */
    public abstract WellFormedEventPacket read() throws EventReaderException;

    /**
     * Starts the <em>EventReader</em>.
     */
    public final void startReader() {

        logger.entering(this.getClass().getName(), "startReader");

        this.run = true;

        this.start();

        logger.log(Level.FINE, "The EventReader for "
                               + this.mySourceModule.getName()
                               + " has been started.");

        logger.exiting(this.getClass().getName(), "startReader");
    }

    /**
     * Stops the <em>EventReader</em>.
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
     * This method returns the <em>SourceModule</em> to which this <em>EventReader</em>
     * is appending read events.
     * @return The <em>SourceModule</em> of this <em>EventReader</em>
     */
    public SourceModule getSourceModule() {
        logger.entering(this.getClass().getName(), "getSourceModule");

        logger.exiting(this.getClass().getName(), "getSourceModule",
            this.mySourceModule);

        return this.mySourceModule;
    }
}
