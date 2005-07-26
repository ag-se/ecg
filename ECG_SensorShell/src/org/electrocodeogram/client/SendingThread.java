package org.electrocodeogram.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.electrocodeogram.event.ValidEventPacket;

/**
 * The class SendingThread provides asynchronous transfer of event data to the
 * ECG Server & Lab.
 * 
 * The SendingThread has an EventPacket buffer, which is filled continously by
 * the ECG sensor that uses this ECG SensorShell component. The EventBuffer is
 * implemented as a Monitor making the SendingThread wait if the buffersize is 0
 * and notifies the SendingThread when new EventPackets are added. So the
 * SendingThread only works when it has work to do.
 * 
 * If new EventPackets are added and a connection to the ECG server is
 * established they are sended to the ECG server via serialisation over sockets.
 * 
 * If the connection is not established a new connection approach is initiated
 * after a delay.
 */
public class SendingThread extends Thread
{

    /**
     * The FIFO EventPacket buffer to store event data temporarily. It is
     * implemented as an ArrayList<EventPacket> Monitor.
     */
    protected EventPacketQueue queue = null;

    /**
     * This tells how often did the SendingThread try to connect since its
     * creation.
     */
    protected int connectionTrials = 0;

    private ObjectOutputStream oos = null;

    /**
     * This is the reference to the communication socket.
     */
    protected Socket socketToServer = null;

    private InetAddress $host = null;

    private Logger logger = null;

    private int $port = -1;

    /**
     * If a connection attempt fails this tells, when the next connection
     * attempt shall occur.
     */
    protected int connectionDelay = 5000;

    private boolean runningFlag = false;

    /**
     * This creates the instance of the SendingThread.
     * 
     * @param host
     *            The InetAddress object giving the IP-Address/Hostname of the
     *            ECG server
     * @param port
     *            The TCP port of the ECG server
     * @throws IllegalHostOrPortException,
     *             when the parameter values are illegal e.g. Host is "null" or
     *             port is "-1". This exception is not thrown every time the
     *             parameter values are illegal, but only when the parameter
     *             values are used for the SendingThread because the
     *             SendingThread's "host" and "port" values have not been set
     *             yet.
     */

    public SendingThread(InetAddress host, int port) throws IllegalHostOrPortException
    {

        // assert parameter value are legal
        if (host == null || port < 0 || port > 65565) {
            throw new IllegalHostOrPortException();
        }

        this.queue = new EventPacketQueue();

        this.logger = Logger.getLogger("SendingThread");

        this.$host = host;

        this.$port = port;

        this.setDaemon(true);

        this.setPriority(Thread.MIN_PRIORITY);

        this.start();
    }

    /**
     * This method is used to pass a new EventPacket to the SendingThread for
     * transmission to the ECG server. The EventPacket is passed to the
     * EventPacket buffer.
     * 
     * @param packet
     *            This is the EventPacket to transmit.
     * @return "true" if adding the EventPacket was successful and "false"
     *         otherwise
     */
    public boolean addEventPacket(ValidEventPacket packet)
    {
        if (packet == null) {
            return false;
        }

        boolean result = this.queue.addToTail(packet);

        return result;

    }

    /*
     * This private method tries to establish a Socket connection to the ECG
     * server. If it fails it throws an IOException.
     */
    private void connectToServer()
    {
        this.connectionTrials++;

        try {

            // open a new socket
            this.socketToServer = new Socket(this.$host, this.$port);

            // create a stream upon the socket
            this.oos = new ObjectOutputStream(
                    this.socketToServer.getOutputStream());

            this.logger.log(Level.INFO, "Connected to the ECG Server at " + this.$host.toString() + ":" + this.$port);
        }
        catch (IOException e) {

            this.logger.log(Level.WARNING, "Unable to connect to the ECG Server at " + this.$host.toString() + ":" + this.$port + " \nNext attempt in " + this.connectionDelay / 1000 + " seconds.");

            try {

                // delay for another attempt
                Thread.sleep(this.connectionDelay);

                connectToServer();
            }
            catch (InterruptedException e1) {
                // no need of special treatment here
            }

        }

    }

    /**
     * The run-method is doing the actual transmission to the ECG server. If any
     * new EventPackets are in the buffer this Thread is notified and if a
     * connection is established, sending EventPackets in the buffer is done.
     * 
     * Sent EventPackets are then removed from the buffer. If the connection is
     * lost reconnection is initiated. After the last EventPacket has left the
     * buffer this Thread blocks until new EventPackets are added.
     */
    @Override
    public void run()
    {
        this.runningFlag = true;

        // first attempt to connect to server
        connectToServer();

        // is the SendingThread running?
        while (this.runningFlag) {

            ValidEventPacket packet = null;

            /*
             * Any EventPackets to transmit? This Thread blocks here
             * if the buffer size is 0. If the size is greater than
             * 0 the Thread sends one EventPacket after another and
             * removes it from ther buffer until the buffer size is
             * 0 again and the Thread blocks again.
             */
            while (this.queue.getSize() > 0) {

                // Are we still connected?
                if (this.socketToServer == null || !(this.socketToServer.isConnected())) {
                    // reconnect
                    connectToServer();
                }

                try {
                    // remove the oldest packet
                    packet = this.queue.removeFromHead();

                    this.oos.flush();

                    // send packet serialized over socket
                    this.oos.writeObject(packet);

                    this.oos.flush();
                }
                catch (EventPacketQueueUnderflowException e) {

                    // As the head-condition in the while proofs for
                    // the element count, this should never occur

                    e.printStackTrace();

                }
                catch (IOException e) {

                    this.logger.log(Level.WARNING, "Lost connection to the ECG Server.");
                }
            }
        }

    }

    /**
     * This method tells whether the SendingThread is running or not.
     * 
     * @return "true" if the SendingThread is running and "false" otherwise
     */
    public boolean isRunning()
    {
        return this.runningFlag;
    }

    /**
     * This class represents a queue with FIFO characteristic for buffering
     * incoming EventPackets. It is also a Monitor for the SendingThread causing
     * it to wait if the buffer is empty and notifying it if new EventPackets
     * are added.
     */
    private static class EventPacketQueue extends ArrayList<ValidEventPacket>
    {
        private static final long serialVersionUID = -7457045862890074109L;

        /**
         * This method add a single EventPacket to the tail of the queue and
         * notifies the SendingThread.
         * 
         * @param packet
         *            The EventPacket to queue
         * @return "true if cuing succeeded and "false" otherwise
         */
        public synchronized boolean addToTail(ValidEventPacket packet)
        {
            boolean result = this.add(packet);

            this.notifyAll();

            return result;
        }

        /**
         * This method returns and removes the head-most EventPacket of the
         * queue.
         * 
         * @return The head EventPacket
         * @throws EventPacketQueueUnderflowException
         *             If the queue is empty already
         */
        public synchronized ValidEventPacket removeFromHead() throws EventPacketQueueUnderflowException
        {
            int sizeBefore;

            if ((sizeBefore = this.size()) > 0) {

                ValidEventPacket packet = this.get(0);

                this.remove(0);

                assert (this.size() == sizeBefore - 1);

                return packet;

            }

            throw new EventPacketQueueUnderflowException();

        }

        /**
         * This method returns the number of EventPackets in the queue and
         * causes the SendingThread to wait if the size is 0.
         * 
         * @return The number of EventPackets
         */
        public synchronized int getSize()
        {
            int size = this.size();

            if (size == 0) {
                try {

                    this.wait();
                }
                catch (InterruptedException e) {

                    e.printStackTrace();
                }
            }

            return this.size();
        }

    }

    protected boolean ping()
    {
        try {
            this.socketToServer.sendUrgentData(0);

            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * This Exception shall be thrown, when the EventPacketQueue is empty but an
     * EventPacket is to be removed.
     * 
     */
    private static class EventPacketQueueUnderflowException extends Exception
    {
        private static final long serialVersionUID = 870916601241806158L;

    }
}
