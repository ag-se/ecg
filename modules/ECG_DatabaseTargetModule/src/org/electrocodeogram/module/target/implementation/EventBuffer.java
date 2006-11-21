package org.electrocodeogram.module.target.implementation;

import java.util.LinkedList;
import org.electrocodeogram.event.ValidEventPacket;

public class EventBuffer {
    /**
     * The LinkesList which holds the events to buffer
     */
    LinkedList<ValidEventPacket> buffer;

    /**
     * An Integer which limits the max number of events which can be buffered
     */
    int maxElements;

    /**
     * The constructor
     * 
     * @param size
     *            the max number of events which can be buffered at the same
     *            time
     * 
     */
    public EventBuffer(int size) {
        this.buffer = new LinkedList<ValidEventPacket>();
        maxElements = size;
    }

    /**
     * This Method allows to put an Event at the end of the buffer queue
     * 
     * @param eventPacket
     *            the Event which has to be buffered
     */
    public synchronized void put(final ValidEventPacket eventPacket) {
        // if the buffer is filled with the max number of events, wait until
        // some events are removed from the buffer queue
        while (maxElements == buffer.size()) {
            try {
                wait();
            } // try
            catch (InterruptedException ie) {
                ie.printStackTrace();
            } // catch
        } // while
        // when the queue is not full put the event in the buffer queue
        buffer.add(eventPacket);
        notifyAll();
    } // put()

    /**
     * This Method removes the first Event from the buffer queue
     * 
     * @return the removed Event from the buffer queue
     */
    public synchronized ValidEventPacket get() {
        while (buffer.isEmpty()) {
            try {
                wait();
            } // try
            catch (InterruptedException ie) {
                System.out.println("An InterruptedException caught\n"
                        + ie.getMessage());
                ie.printStackTrace();
            } // catch
        } // while
        notifyAll();
        return (buffer.removeFirst());
    } // get()

    /**
     * Get the number of events in the buffer
     * 
     * @return the number of events in the buffer queue
     */
    public synchronized int getSize() {
        return (buffer.size());
    } // getSize()
}
