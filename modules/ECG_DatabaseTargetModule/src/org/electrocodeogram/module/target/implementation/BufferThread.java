package org.electrocodeogram.module.target.implementation;

import org.electrocodeogram.event.ValidEventPacket;

/**
 * @author jule
 * @version 1.0 The BufferThread class is a Thread which is running when some
 *          Events could not directly be inserted in the database in cause of a
 *          SQL Exception which occured. The Thread checks if therte are any
 *          Events in the eventBuffer and while there are some events the tread
 *          runs. During its running time the thread tries to establish a
 *          database connection and then to insert the events from the buffer.
 *          If there are no more events in the buffer the thread waits to be
 *          notified by the DBCommunicator which is not able to insert some
 *          events in the database.
 */
public class BufferThread extends Thread {
    /**
     * The DBCommunicator to communicate with the database.
     */
    private DBCommunicator dbCommunicator;

    /**
     * The constructor.
     * 
     * @param dbCommunicator
     *            the
     */
    public BufferThread(final DBCommunicator dbCommunicator) {
        this.dbCommunicator = dbCommunicator;
    }

    /**
     * This is the run Method which has to be implemented by all classes
     * implementing the Runnable Interface.
     */
    @Override
    public void run() {
        while (true) {
            // get the eventBuffer from the DBCommunicator
            EventBuffer eventBuffer = dbCommunicator.getEventBuffer();
            ValidEventPacket currentPacket = eventBuffer.get();
            // insert this event
            if (dbCommunicator.insertEvent(currentPacket)) {
                continue;
            }
            // if the event could not be inserted in the database put it
            // back in to the eventBuffer
            else {
                dbCommunicator.getEventBuffer().put(currentPacket);
            }
        } // END WHILE(true)
    }
}
