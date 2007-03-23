// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   HackyEventReader.java

package org.electrocodeogram.module.source.implementation;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.source.*;


public class HackyEventReader extends EventReader
{
    
    private static Logger logger = LogHelper.createLogger(HackyEventReader.class.getName());
    private EventQueue eventBuffer;
    
    /**
     * This is a buffer for the events that are submittted by the user.
     * As the {@link ManualReader#read()} method is continously reading events,
     * the <em>EventReader</em> has to <em>wait</em> until the user
     * has submitted a new event.<br>
     * This causes the {@link ManualReader#read()} method to block.
     * 
     */
    private static class EventQueue {

        /**
         * Is the logger.
         */
        private Logger eventQueueLogger = LogHelper
            .createLogger(EventQueue.class.getName());

        /**
         * Events are stored in this list.
         */
        private ArrayList < WellFormedEventPacket > queue;

        /**
         * Creates the buffer.
         *
         */
        public EventQueue() {

            this.eventQueueLogger.entering(this.getClass().getName(),
                "EventQueue");

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

            synchronized (this) {
                this.queue.add(packet);

                this.eventQueueLogger.log(Level.FINER,
                    "Added a packet... Size is " + this.queue.size());

                notifyAll();
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

            synchronized (this) {

                if (this.queue.size() > 0) {

                    this.eventQueueLogger.log(Level.FINER,
                        "Remove a packet... Size is " + this.queue.size());

                    WellFormedEventPacket toReturn = this.queue.remove(0);

                    this.eventQueueLogger.exiting(this.getClass().getName(),
                        "remove", toReturn);

                    return toReturn;

                }

                try {

                    this.eventQueueLogger.log(Level.FINER,
                        "Wating to remove a packet... Size is "
                                        + this.queue.size());

                    wait();

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



    public HackyEventReader(SourceModule sourceModule)
    {
        super(sourceModule);
        logger.entering(getClass().getName(), "ManualReader", new Object[] {
            sourceModule
        });
        eventBuffer = new EventQueue();
        // org.electrocodeogram.modulepackage.ModuleProperty runtimeProperties[] = sourceModule.getRuntimeProperties();
        logger.exiting(getClass().getName(), "ManualReader");
    }

    public WellFormedEventPacket read()
        throws EventReaderException
    {
        return eventBuffer.remove();
    }

    /**
     * @param event
     */
    public void add(WellFormedEventPacket event) {
        this.eventBuffer.add(event);
        
    }

    

}
