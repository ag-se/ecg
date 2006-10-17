/*
 * Class: EventSender
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;

/**
 * The class <code>EventSender</code> provides asynchronous transfer of
 * events to the ECG Lab. The <code>EventSender</code> has an event
 * buffer the {@link EventSender.EventPacketQueue}, which is filled
 * continously by the ECG sensor that uses this
 * <em>ECG SensorShell</em> component. The <code>EventPacketQueue</code>
 * is implemented as a Monitor making the <code>EventSender</code> wait
 * when the buffer size is 0 and waking it up
 * when new events are added. So the <code>EventSender</code> only works
 * when it has work to do. If new events are added and a connection to
 * the ECG Lab is established they are sent to the ECG Lab as
 * serialised objects over a socket. If the connection is not established a
 * new connection approach is initiated after a delay.
 */
public class EventSender extends Thread {

    /**
     * When working with <em>Serialization</em> the
     * <em>ObjectStreams</em> needs to be reset periodicallly to
     * avoid <em>OutOfMemoryErrors</em>. This constant tells how
     * often this occurs.
     */
    private static final int STREAM_RESET_COUNT = 100;

    /**
     * The maximum number of events in the buffer, before an
     * {@link EventPacketQueueOverflowException} is thrown.
     */
    public static final int MAX_QUEUE_SIZE = 1000000;

    /**
     * The lowest legal TCP-PORT.
     */
    private static final int MIN_PORT = 1024;

    /**
     * The highest legal TCP-PORT.
     */
    private static final int MAX_PORT = 65565;

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper.createLogger(EventSender.class
        .getName());

    /**
     * The FIFO event buffer to store events temporarily. It is
     * implemented as an <em>ArrayList</em> Monitor.
     */
    private EventPacketQueue queue;

    /**
     * This tells how often the <em>EventSender</em> did try to
     * connect since its creation.
     */
    private int connectionTrials = 0;

    /**
     * This is the <em>Stream</em> on wich events are sent to the
     * ECG Lab.
     */
    private ObjectOutputStream toEcgLab;

    /**
     * This is the reference on the <em>Socket</em> to the ECG Lab.
     */
    private Socket socketToEcgLab;

    /**
     * The hostname/IP-Address of the ECG Lab.
     */
    private InetAddress host;

    /**
     * The TCP_Port of the ECG Lab.
     */
    private int port = -1;

    /**
     * If a connection attempt fails this tells, when the next
     * connection attempt shall be made.
     */
    private static final int CONNECTION_DELAY = 5000;

    /**
     * The <em>EventSender</em> runs as lon as this is
     * <code>true</code>.
     */
    private boolean run = false;

    /**
     * This creates the <em>EventSender</em>.
     * @param hostname
     *            The <em>InetAddress</em> object giving the
     *            IP-Address/Hostname of the ECG Lab
     * @param tcpport
     *            The TCP-Port of the ECG Lab
     * @throws IllegalHostOrPortException
     *             When the parameter values are illegal e.g. Host is
     *             "null" or port is "-1".
     */
    public EventSender(final InetAddress hostname, final int tcpport)
        throws IllegalHostOrPortException {

        logger.entering(this.getClass().getName(), "EventSender", new Object[] {
            hostname, new Integer(tcpport)});

        if (hostname == null || tcpport < MIN_PORT || tcpport > MAX_PORT) {
            logger.log(Level.SEVERE,
                "Hostname/IP-Address or TCP-Port values are illegal.");

            logger.exiting(this.getClass().getName(), "SendingThread");

            throw new IllegalHostOrPortException();
        }

        this.queue = new EventPacketQueue();

        this.host = hostname;

        this.port = tcpport;

        this.setDaemon(true);

        this.setPriority(Thread.MIN_PRIORITY);

        this.start();

        logger.exiting(this.getClass().getName(), "SendingThread");
    }

    /**
     * This method is used to pass a new event to the
     * <em>EventSender</em> for transmission to the ECG Lab.
     * @param packet
     *            This is the event to transmit
     * @return <code>true</code> if adding the event was successful
     *         and <code>false</code> otherwise
     * @throws EventPacketQueueOverflowException
     *             If the event buffer is full
     */
    public final boolean addEventPacket(final WellFormedEventPacket packet)
        throws EventPacketQueueOverflowException {
        logger.entering(this.getClass().getName(), "addEventPacket",
            new Object[] {packet});

        if (packet == null) {
            logger.log(Level.WARNING, "The parameter \"packet\" is null.");

            logger.exiting(this.getClass().getName(), "addEventPacket",
                new Boolean(false));

            return false;
        }

        boolean result = false;

        result = this.queue.addToTail(packet);

        logger.exiting(this.getClass().getName(), "addEventPacket",
            new Boolean(result));

        return result;

    }

    /**
     * This method tries to establish a <em>Socket</em> connection
     * to the ECG Lab.
     */
    private void connect() {
        logger.entering(this.getClass().getName(), "connect");

        this.connectionTrials++;

        try {

            // open a new socket
            this.socketToEcgLab = new Socket(this.host, this.port);

            logger.log(Level.INFO,
                "A network socket connection to to the ECG Lab at "
                                + this.host.toString() + ":" + this.port
                                + " has been established");

            // create a stream upon the socket
            this.toEcgLab = new ObjectOutputStream(this.socketToEcgLab
                .getOutputStream());

            logger.log(Level.FINE, "The streams to the ECG Lab are open.");

            logger.log(Level.INFO, "The connection is open to the ECG Lab at "
                                   + this.host.toString() + ":" + this.port
                                   + ".");
        } catch (IOException e) {

            logger.log(Level.WARNING, "Unable to connect to the ECG Lab at "
                                      + this.host.toString() + ":" + this.port
                                      + " \nNext attempt in "
                                      + CONNECTION_DELAY / 1000 + " seconds.");

            try {
                // delay for another attempt
                Thread.sleep(CONNECTION_DELAY);

                connect();
            } catch (InterruptedException e1) {

                logger
                    .log(Level.WARNING,
                        "The EventSender was interrupted while delaying for another connection atempt.");

                // this is not a problem
            }

        }

        logger.exiting(this.getClass().getName(), "connect");

    }
    
    /**
     * Disable this EventSender
     */
    public void disconnect() {
        try {
            this.toEcgLab.close();
            this.socketToEcgLab.close();
        } catch (IOException e) {
            logger
            .log(Level.WARNING,
                "The EventSender could not be disconnected dur to: \n" + e.getMessage());
        }
    }

    /**
     * This is doing the actual transmission to the ECG Lab. If any
     * new events are in the buffer this is notified and if a
     * connection is established, sending events from the buffer is
     * done. Sent events are then removed from the buffer. If the
     * connection is lost reconnection is initiated. After the last
     * event has left the buffer this <code>Thread</code> blocks
     * until new events are added.
     */
    @Override
    public final void run() {
        logger.entering(this.getClass().getName(), "run");

        this.run = true;

        WellFormedEventPacket packet;

        // first attempt to connect to server
        connect();

        int count = 0;

        // is the SendingThread running?
        while (this.run) {

            /*
             * Any EventPackets to transmit? This Thread blocks here
             * if the buffer size is 0. If the size is greater than 0
             * the Thread sends one EventPacket after another and
             * removes it from their buffer until the buffer size is 0
             * again and the Thread blocks again.
             */
            while (this.queue.getSize() > 0) {

                // Are we still connected?
                if (this.socketToEcgLab == null
                    || !(this.socketToEcgLab.isConnected())) {
                    logger.log(Level.INFO,
                        "Connection is not established, reconnecting.");

                    // reconnect
                    connect();
                }
                try {
                    // remove the oldest packet
                    packet = this.queue.removeFromHead();

                    // send packet serialized over socket
                    this.toEcgLab.writeObject(packet);

                    this.toEcgLab.flush();

                    logger.log(Level.FINE,
                        "An event has benn written to the network socket.");

                    count++;

                    if (count >= STREAM_RESET_COUNT) {
                        this.toEcgLab.reset();

                        count = 0;

                    }

                } catch (EventPacketQueueUnderflowException e) {

                    logger.log(Level.WARNING, e.getMessage());

                    // this is checked in the loop head condition and should not occur
                } catch (IOException e) {

                    logger.log(Level.WARNING,
                        "The connection to the ECG Lab is lost.");

                    this.socketToEcgLab = null;
                }
            }
        }

        logger.exiting(this.getClass().getName(), "run");
    }

    /**
     * This class is a queue with FIFO characteristics for buffering
     * incoming events. It is also a <em>Monitor</em> for the
     * <em>eventSender</em> causing it to wait if the buffer is
     * empty and notifying it if new events are added.
     */
    @SuppressWarnings("serial")
    private static class EventPacketQueue extends
        ArrayList<WellFormedEventPacket> {

        /**
         * This is the logger.
         */
        private Logger eventPacketQueuelogger = LogHelper
            .createLogger(EventPacketQueue.class.getName());

        /**
         * This method add a single event to the tail of the queue and
         * notifies the <em>EventSender</em>.
         * @param packet
         *            The event to queue
         * @return <code>true</code> if queueing succeeded and
         *         <code>false</code> otherwise
         * @throws EventPacketQueueOverflowException
         *             If the number of events in the buffer is
         *             getting greater than
         *             {@link EventSender#MAX_QUEUE_SIZE}.
         */
        public final boolean addToTail(final WellFormedEventPacket packet)
            throws EventPacketQueueOverflowException {
            this.eventPacketQueuelogger.entering(this.getClass().getName(),
                "addToTail", new Object[] {packet});

            synchronized (this) {

                if (this.size() > MAX_QUEUE_SIZE) {
                    this.eventPacketQueuelogger.exiting(this.getClass()
                        .getName(), "addToTail");

                    throw new EventPacketQueueOverflowException();
                }

                boolean result = this.add(packet);

                this.notifyAll();

                this.eventPacketQueuelogger.exiting(this.getClass().getName(),
                    "addToTail", new Boolean(result));

                return result;
            }
        }

        /**
         * This method returns and removes the head-most event in the
         * queue.
         * @return The head event
         * @throws EventPacketQueueUnderflowException
         *             If the queue is empty already
         */
        public WellFormedEventPacket removeFromHead()
            throws EventPacketQueueUnderflowException {

            this.eventPacketQueuelogger.entering(this.getClass().getName(),
                "removeFromHead");

            synchronized (this) {

                int sizeBefore = this.size();

                if (sizeBefore <= 0) {

                    this.eventPacketQueuelogger.exiting(this.getClass()
                        .getName(), "removeFromHead");

                    throw new EventPacketQueueUnderflowException();
                }

                WellFormedEventPacket packet = this.get(0);

                this.remove(0);

                assert (this.size() == sizeBefore - 1);

                this.notifyAll();

                this.eventPacketQueuelogger.exiting(this.getClass().getName(),
                    "removeFromHead", packet);

                return packet;

            }

        }

        /**
         * This method returns the number of evenst in the queue and
         * causes the <em>EventSender</em> to wait if the size is 0.
         * @return The number of events
         */
        public synchronized int getSize() {
            this.eventPacketQueuelogger.entering(this.getClass().getName(),
                "getSize");

            int size = this.size();

            if (size == 0) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    this.eventPacketQueuelogger
                        .log(Level.WARNING,
                            "EventSender was interrupted while waiting for buffer to fill.");
                }
            }

            this.eventPacketQueuelogger.exiting(this.getClass().getName(),
                "getSize", new Integer(this.size()));

            return this.size();
        }

    }

}
