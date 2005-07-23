package org.electrocodeogram.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import org.electrocodeogram.event.ValidEventPacket;

/**
 * The singleton class SendingThread provides asynchronous transfer of
 * event data to the ECG Server & Lab.
 * 
 * The SendingThread has an EventPacket buffer, which is filled continously by
 * the running ECG sensors. The buffer is checked periodically for new EventPackets. 
 * 
 * If new EventPackets are found and a connection to the ECG server
 * is established they are sended to the ECG server via serialisation
 * over sockets.
 * 
 * If the connection is not established a new connection approach
 * is initiated.
 */
public class SendingThread extends Thread
{

    /**
     * the singleton instance
     */
    private static SendingThread theInstance = null;

    /**
     * The FIFO EventPacket buffer to store event data temporarily.
     * It is implemented as an ArrayList<EventPacket>
     */
    protected EventPacketQueue queue = null;

    /**
     * This tells how often did the SendingThread try to connect since its creation.
     */
    protected int connectionTrials = 0;

    private ObjectOutputStream oos = null;

    /**
     * This is the reference to the communication socket.
     */
    protected Socket socketToServer = null;

    private InetAddress host = null;

    private int port = -1;

    /**
     * If a connection attempt fails this tells, when the next connection attempt shall occur. 
     */
    protected int connectionDelay = 5000;

    private boolean runningFlag = false;

    private SendingThread(InetAddress hostPar, int portPar)
    {

        this();

        // assert parameter value are legal
        assert (hostPar != null && portPar > 0 && portPar < 65565);

        this.host = hostPar;

        this.port = portPar;
    }

    //  This is a private constructor only used internally
    private SendingThread()
    {
        this.queue = new EventPacketQueue();
    }

    /**
     * This method returns the singleton instance of the sending thread.
     * If the instance of the SendingThread does not yet have values for
     * "host" and "port", the values that are passed as parameters to this
     * method are given to to SendingThread.
     * 
     * @param host The InetAddress object giving the IP-Address/Hostname of the ECG server
     * @param port The TCP port of the ECG server
     * @return This method returns the instance of the SendingThread
     * @throws IllegalHostOrPortException, when the parameter values are illegal e.g. Host is "null" or port is "-1". This exception is not thrown every time the parameter values are illegal, but only when the parameter values are used for the SendingThread because the SendingThread's "host" and "port" values have not been set yet.
     */
    public static SendingThread getInstance(InetAddress host, int port) throws IllegalHostOrPortException
    {
        if (theInstance == null) {
            theInstance = new SendingThread(host, port);

            theInstance.start();
        }
        else if (theInstance.host == null || theInstance.port == -1) {
            if (host == null || port < 0 || port > 65565) {
                throw new IllegalHostOrPortException();
            }
            theInstance.host = host;

            theInstance.port = port;

            theInstance.start();
        }

        return theInstance;
    }

    /**
     * This method returns the singleton instance of the SendingThread.
     * The returned instance might not be running yet, as the SendingThread
     * only starts after getting legal "host" and "port" values through
     * a call to getInstance(InetAddress host, int port).
     * 
     * @return The singleton instance of the SendingThread
     */
    public static SendingThread getInstance()
    {
        if (theInstance == null) {
            theInstance = new SendingThread();

        }
        return theInstance;
    }

    /**
     * This method is used to pass a new EventPacket to the
     * SendingThread for transmission to the ECG server.
     * 
     * @param packet This is the EventPacket to transmit.
     * @return "true" if adding the EventPacket was successful and "false" otherwise
     */
    public boolean addEventPacket(ValidEventPacket packet)
    {
        if (packet == null) {
            return false;
        }

        return this.queue.add(packet);

    }

    /*
     * This private method tries to establish a Socket connection
     * to the ECG server. If it fails it throws an IOException.
     */
    private void connectToServer()
    {
        this.connectionTrials++;

        try {

            // open a new socket
            this.socketToServer = new Socket(this.host, this.port);

            // create a stream upon the socket
            this.oos = new ObjectOutputStream(
                    this.socketToServer.getOutputStream());

        }
        catch (IOException e) {

            System.err.println("Unable to connect to the ECG Server at " + this.host.toString() + ":" + this.port + " \nNext attempt in " + this.connectionDelay / 1000 + " seconds.");

            try {

                // delay for another attempt 
                Thread.sleep(this.connectionDelay);
            }
            catch (InterruptedException e1) {
                // no need of special treatment here
            }

        }

    }

    /**
     * The run()-method is doing the actual transmission to the ECG server.
     * If any new EventPackets are in the buffer and if a connection is
     * established, sending new EventPackets begins.
     * 
     * Sent EventPackets are then removed from the buffer. If the connection
     * is lost reconnection is initiated.
     */
    @Override
    public void run()
    {
        this.runningFlag = true;

        // first attempt to connect to server
        connectToServer();

        // is the SendingThread running?
        while (this.runningFlag) {
            // any EventPackets to transmit?

            if (this.queue.getSize() > 0) {
                // is the connection up?
                if (this.socketToServer != null && this.socketToServer.isConnected()) {
                    try {
                        // assert new EventPackets
                        assert (this.queue.getSize() > 0);

                        // for every new EventPacket
                        // throws an ConcurrentModificationException after the first sent EventPacket!
                        //for (EventPacket packet : backupBuffer)

                        ValidEventPacket packet = null;

                        while (this.queue.getSize() > 0) {

                            try {
                                packet = this.queue.removeFromHead();
                            }
                            catch (EventPacketQueueUnderflowException e) {

                                // As the head-condition in the while proofs for the element count, this should never occur

                                e.printStackTrace();

                            }

                            this.oos.flush();

                            // send serialized over socket
                            this.oos.writeObject(packet);

                            this.oos.flush();
                        }

                        // assert buffer is empty

                        assert (this.queue.getSize() == 0);
                    }
                    catch (Exception e) {

                        e.printStackTrace();
                    }
                }
                else {
                    // try to connect again
                    connectToServer();
                }
            }
        }
    }

    /**
     * This method tells whether the SendingThread is running or not.
     * @return "true" if the SendingThread is running and "false" otherwise
     */
    public boolean isRunning()
    {
        return this.runningFlag;
    }

    /**
     * This class represents a queue with FIFO characteristic for buffering incoming EventPackets.
     */
    private class EventPacketQueue extends ArrayList<ValidEventPacket>
    {
        private static final long serialVersionUID = -7457045862890074109L;

        /**
         * This method add a single EventPacket to the tail of the queue.
         * @param packetPar The EventPacket to queue
         * @return "true if cuing succeeded and "false" otherwise
         */
        public boolean addToTail(ValidEventPacket packetPar)
        {
            return this.add(packetPar);
        }

        /**
         * This method returns and removes the head-most EventPacket of the queue. 
         * @return The head EventPacket
         * @throws EventPacketQueueUnderflowException If the queue is empty already
         */
        public ValidEventPacket removeFromHead() throws EventPacketQueueUnderflowException
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
         * This method returns the number of EventPackets in the queue
         * @return The number of EventPackets
         */
        public int getSize()
        {
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
     * This Exception shall be thrown, when the EventPacketQueue is empty
     * but an EventPacket is to be removed.
     *
     */
    private class EventPacketQueueUnderflowException extends Exception
    {
        private static final long serialVersionUID = 870916601241806158L;

    }
}
