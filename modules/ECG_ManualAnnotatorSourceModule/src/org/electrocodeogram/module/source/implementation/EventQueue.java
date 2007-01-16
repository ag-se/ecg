package org.electrocodeogram.module.source.implementation;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.source.EventReaderException;

/**
 * This is a buffer for the events that are submittted by the user.
 * As the {@link ManualReader#read()} method is continously reading events,
 * the <em>EventReader</em> has to <em>wait</em> until the user
 * has submitted a new event.<br>
 * This causes the {@link ManualReader#read()} method to block.
 * 
 */
class EventQueue {

    /**
     * Is the logger.
     */
    private Logger eventQueueLogger = LogHelper
        .createLogger(EventQueue.class.getName());

    /**
     * Events are stored in this list.
     */
    private ArrayList < WellFormedEventPacket > queue;

    private ManualReader reader;

    /**
     * Creates the buffer.
     *
     */
    public EventQueue(ManualReader reader) {

        this.eventQueueLogger.entering(this.getClass().getName(),
            "EventQueue");

        this.reader = reader;
        this.queue = new ArrayList < WellFormedEventPacket >();

        this.eventQueueLogger.exiting(this.getClass().getName(),
            "EventQueue");
    }

    /**
     * This adds a event after the user has pushed the event's button.
     * @param packet Is the event
     */
    public void add(WellFormedEventPacket packet) {

        this.eventQueueLogger.entering(this.getClass().getName(), "add",
            new Object[] {packet});

        synchronized (this.reader) {
            this.queue.add(packet);

            this.eventQueueLogger.log(Level.FINE,
                "Added a packet... Size is " + this.queue.size());

            this.reader.notify();
        }

        this.eventQueueLogger.exiting(this.getClass().getName(), "add");
    }

    /**
     * Retruns and removes the first event from the buffer.
     * @return The first event from the buffer
     * @throws EventReaderException If this method is interrupted while waiting for the buffer to fill.
     */
    public WellFormedEventPacket remove() throws EventReaderException {

        this.eventQueueLogger.entering(this.getClass().getName(), "remove");

        synchronized (this.reader) {

            if (this.queue.size() > 0) {

                this.eventQueueLogger.log(Level.INFO,
                    "Remove a packet... Size is " + this.queue.size());

                WellFormedEventPacket toReturn = this.queue.remove(0);

                this.eventQueueLogger.exiting(this.getClass().getName(),
                    "remove", toReturn);

                return toReturn;

            }

            try {

                this.eventQueueLogger.log(Level.INFO,
                    "Wating to remove a packet... Size is "
                                    + this.queue.size());

                this.reader.wait();

                WellFormedEventPacket toReturn = this.queue.remove(0);

                this.eventQueueLogger.exiting(this.getClass().getName(),
                    "remove", toReturn);

                return toReturn;

            } catch (InterruptedException e) {

                this.eventQueueLogger.exiting(this.getClass().getName(),
                    "remove");

                throw new EventReaderException(
                    "The EventReader has been interrupted while waiting for events to be submittet by the user.");

            }

        }

    }

}