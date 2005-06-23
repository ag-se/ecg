package org.electrocodeogram;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * The singleton class SendingThread supports the asynchrneous transfer of
 * event data to the ECG server. It is a singleton class, because it is
 * implemented as a (nearly) ever running Thread, to avoid creating new Threads
 * for every single event data to transfer.
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

    // the singleton instance
    private static SendingThread theInstance = null;

    /**
     * The FIFO eventPacket buffer to store event data temporarilly.
     * It is implemented as an generic ArrayList with a type-parameter
     * of EventPacket.
     */
    protected EventPacketQueue queue = null;

    /**
     * Is the SendingThread connected to a ECG server?
     */
    protected boolean connectedFlag = false;

    /**
     * How often did the SendingThread tried to connect since its creation
     */
    protected int connectionTrials = 0;

    private ObjectOutputStream oos = null;

    private Socket socketToServer = null;

    private InetAddress host = null;

    private int port = -1;

    /**
     * If a connection attemp fails, when shall the next connection attemp be made? 
     */
    protected int connectionDelay = 5000;

    private boolean runningFlag = false;

    // This is a private constructor only used internally
    private SendingThread(InetAddress host, int port)
    {

        this();

        // assert parameter value are legal
        assert (host != null && port > 0 && port < 65565);

        this.host = host;

        this.port = port;
    }

    //  This is a private constructor only used internally
    private SendingThread()
    {
        this.queue = new EventPacketQueue();
    }

    /**
     * This method returns the singleton instance of the sending thread.
     * If the instance of the SendingThread does not have legal values for
     * "host" and "port", the parameter values are given to it.
     * 
     * @param host The InetAddress object giving the IP-Address/Hostname of the ECG server
     * @param port The TCP port of the ECG server as an int
     * @return This method returns the instance of the SendingThread
     * @throws IllegalHostOrPortException, when the parameter values are illegal e.g. host is "null" or port is "-1". This exception is not thrown every time the parameter values are illegal, but only when the parameter values are used for the SedningThread beacuse the SendingThread's "host" and "port" values have not been set yet.
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
     * This method is used to pass a new EventPacket over to the
     * SendingThread for trasnmission to the ECG server.
     * 
     * @param packet This is the EventPacket to transmit.
     * @return "true" if adding the EventPacket was successfulll and "false" otherwise
     */
    public boolean addEventPacket(EventPacket packet)
    {
        if (packet == null)
        {
            return false;
        }
        else
        {
            return this.queue.add(packet);
        }
    }

    /*
     * This private method tries to establish a Socket connection
     * to the ECG server. If it fais it throws an IOException.
     */
    private void connectToServer()
    {
        connectionTrials++;
        
        try {

            // open a new socket
            socketToServer = new Socket(host, port);

            // set the flag
            connectedFlag = true;

            // create a stream upon the socket
            oos = new ObjectOutputStream(socketToServer.getOutputStream());

        }
        catch (IOException e) {

            System.err.println("Es konnte keine Verbindung zum ECG Server unter " + host.toString() + ":" + port + " hergestellt werden.\nNächster Verbindungsversuch in " + connectionDelay / 1000 + " Sekunden.");

            try {
                
                // delay for another attemp 
                Thread.sleep(connectionDelay);
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
     * Sended EventPackets are then removed from the buffer. If the connection
     * is lost reestablishment is initialized.
     */
    public void run()
    {
        runningFlag = true;

        // first attempt to connect to server
        connectToServer();

        // is the SendngThread running?
        while (runningFlag) {
            // any EventPackets to transmit?
            if (queue.getSize() > 0) {
                // is the connection up?
                if (connectedFlag) {
                    try {
                        // assert new EventPackets
                        assert (queue.getSize() > 0);

                        // for every new EventPacket
                        // throws an ConcurrentModificationException after the first sent EventPacket!
                        //for (EventPacket packet : backupBuffer)

                        EventPacket packet = null;

                        while (queue.getSize() > 0) {

                            try {
                                packet = queue.removeFromHead();
                            }
                            catch (EventPacketQueueUnderflowException e) {
                                
                                // As the headcondition in the while proofs for the element count, this should never occur
                                
                                e.printStackTrace();
                                
                            }

                            oos.flush();

                            // send serialized over socket
                            oos.writeObject(packet);
                            
                            oos.flush();
                        }

                        // assert buffer is empty

                        assert (queue.getSize() == 0);
                    }
                    catch (IOException e) {
                        // connection is lost

                        connectedFlag = false;
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
     * This method indicates wether the SendingThread is running or not.
     * @return "true" if the SendingThread is running and "false" otherwise
     */
    public boolean isRunning()
    {
        return runningFlag;
    }
    
    /**
     * This class represents a queue with FIFO characteristic for buffering incoming EventPackets.
     */
    private class EventPacketQueue extends ArrayList<EventPacket>
    {
        private static final long serialVersionUID = -7457045862890074109L;

        /**
         * This method add a single EventPacket to the tail of the queue.
         * @param The EventPacket to queue
         * @return "true if queueing succeded and "false" otherwise
         */
        public boolean addToTail(EventPacket packet)
        {
            return this.add(packet);
        }
        
        /**
         * This method returns and removes the headmost EventPacket of the queue. 
         * @return The head EventPacket
         * @throws EventPacketQueueUnderflowException If the queue is empty allready
         */
        public EventPacket removeFromHead() throws EventPacketQueueUnderflowException
        {
            int sizeBefore;
            
            if((sizeBefore = this.size()) > 0)
            {
                                
                EventPacket packet = this.get(0);
                
                this.remove(0);
                
                assert(this.size() == sizeBefore-1);
                
                return packet;
                
            }
            else
            {
                throw new EventPacketQueueUnderflowException();
            }
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
}
