/*
 * Class: SocketServer
 * Version: 1.0
 * Date: 19.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.source;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * This <code>Thread</code> is continuously listening for connection
 * requests from ECG sensors. If a connection to a sensor could be
 * established a new {@link SocketServerThread} is created and started
 * to maintain the socket communication to the sensor. When this
 * <em>SocketServer</em> is shut down, all connections are closed
 * and all {@link SocketServerThread} are stopt.
 */
public class SocketServer extends Thread implements ISocketServer {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper.createLogger(SocketServer.class
        .getName());

    /**
     * The TCP-Port to listen on.
     */
    private int tcpPort = -1;

    /**
     * The state of this <code>Thread</code>.
     */
    private boolean run = true;

    /**
     * This map is a <em>ThreadPool</em>, where reference to all
     * running <em>SocketServerThreads</em> are kept.
     */
    private HashMap<Integer, SocketServerThread> threadPool;

    /**
     * This object is managing incoming socket connctions.
     */
    private ServerSocket serverSocket;

    /**
     * A reference to the {@link SocketSourceModule}.
     */
    private SourceModule sourceModule;

    /**
     * This creates a new <em>SensorServer</em>.
     * @param module
     *            Is the <em>SourceModule</em> to which the received
     *            events are passed
     * @param port
     *            Is the TCP-Port to listen on
     */
    public SocketServer(final SourceModule module, final int port) {
        logger.entering(this.getClass().getName(), "SocketServer",
            new Object[] {module, new Integer(port)});

        if (module == null) {
            logger
                .log(Level.SEVERE,
                    "The parameter module is null. Can not create the SocketServer");

            logger.exiting(this.getClass().getName(), "SocketServer");

            return;
        }

        if (port > SocketSourceModule.MIN_PORT
            && port < SocketSourceModule.MAX_PORT) {
            this.tcpPort = port;
        } else {
            logger.log(Level.WARNING,
                "The value for the port property must be a number greater than "
                                + SocketSourceModule.MIN_PORT
                                + " and less then "
                                + SocketSourceModule.MAX_PORT + ".");

            logger.log(Level.SEVERE, "Can not create the SocketServer");

            logger.exiting(this.getClass().getName(), "SocketServer");

            return;
        }

        this.sourceModule = module;

        this.threadPool = new HashMap<Integer, SocketServerThread>();

        logger.exiting(this.getClass().getName(), "SocketServer");

    }

    /**
     * This method retuns all known IP-Addresses of connected sensors.
     * @return All known IP-Addresses of connected sensors
     */
    public final InetAddress[] getSensorAddresses() {
        logger.entering(this.getClass().getName(), "getSensorAddress");

        int count = this.getSensorCount();

        InetAddress[] addresses = new InetAddress[count];

        if (this.threadPool.values() == null) {

            logger.exiting(this.getClass().getName(), "getSensorAddress",
                new InetAddress[0]);

            return new InetAddress[0];
        }

        Object[] sensorThreads = this.threadPool.values().toArray();

        for (int i = 0; i < count; i++) {
            addresses[i] = ((SocketServerThread) sensorThreads[i])
                .getSensorAddress();
        }

        logger
            .exiting(this.getClass().getName(), "getSensorAddress", addresses);

        return addresses;
    }

    /**
     * This method returns the current number of connected ECG
     * sensors.
     * @return The current number of connected ECG sensors
     */
    public final int getSensorCount() {
        logger.entering(this.getClass().getName(), "getSensorCount");

        logger.exiting(this.getClass().getName(), "getSensorCount",
            new Integer(this.threadPool.size()));

        return this.threadPool.size();

    }

    /**
     * This method removes a single <em>SocketServerThread</em> from
     * the <em>ThreadPool</em>.
     * @param id
     *            The unique ID of the <em>SocketServerThread</em>
     *            to remove.
     */
    public final void removeSensorThread(final int id) {
        logger.entering(this.getClass().getName(), "removeSensorThread",
            new Object[] {new Integer(id)});

        this.threadPool.remove(new Integer(id));

        logger.exiting(this.getClass().getName(), "removeSensorThread");
    }

    /**
     * This method returns a <code>String</code> containing the
     * IP-Address and TCP-Port this <em>SocketServer</em> is
     * listening on.
     * @return The IP-Address and TCP-Port this <em>SocketServer</em>
     *         is listening on.
     */
    public final String[] getAddress() {
        logger.entering(this.getClass().getName(), "getAddress");

        String[] toReturn = null;

        try {
            toReturn = new String[] {InetAddress.getLocalHost().toString(),
                Integer.toString(this.tcpPort)};
        } catch (UnknownHostException e) {

            // As the localhost should not be unknown, this should
            // never happen.

            logger
                .log(
                    Level.SEVERE,
                    "An unexpected exception has occurred. Please report this at www.electrocodeogram.org");

            logger.exiting(this.getClass().getName(), "getAddress");
        }

        logger.exiting(this.getClass().getName(), "getAddress", toReturn);

        return toReturn;
    }

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public final void run() {
        logger.entering(this.getClass().getName(), "run");

        try {

            this.serverSocket = new ServerSocket(this.tcpPort);

            logger.log(Level.INFO, "ECG Server is up and listening on port: "
                                   + this.tcpPort);

        } catch (IOException e) {

            logger.log(Level.SEVERE,
                "The ECG Server could not be started. (Maybe port "
                                + this.tcpPort + " is in use?");

            this.shutDown();

        }
        while (this.run) {
            try {

                // this method call blocks until a new incoming
                // connection request
                Socket socketToSensor = this.serverSocket.accept();

                logger.log(Level.INFO, "New connection request from: "
                                       + socketToSensor.getInetAddress()
                                           .toString());

                // create a new ServerThread to communicate on the
                // given Socket
                SocketServerThread serverThread = new SocketServerThread(this,
                    this.sourceModule, socketToSensor);

                logger.log(Level.INFO, "A new ServerThread has been created.");

                // put the ServerThread in the threadpool
                this.threadPool.put(new Integer(serverThread
                    .getServerThreadId()), serverThread);

                // start the ServerThread
                serverThread.start();

                logger
                    .log(Level.INFO, "The new ServerThread has been started.");

            } catch (IOException e) {

                logger.log(Level.WARNING, "New connection request failed.");

            }
        }

        logger.exiting(this.getClass().getName(), "run");
    }

    /**
     * This method returns all known ECG sensor names of connected ECG
     * sensors.
     * @return All known ECG sensor names of connected ECG sensors in
     *         an Array of Strings
     */
    public final String[] getSensorNames() {
        logger.entering(this.getClass().getName(), "getSensorName");

        int count = this.getSensorCount();

        String[] names = new String[count];

        Object[] sensorThreads = this.threadPool.values().toArray();

        for (int i = 0; i < count; i++) {
            names[i] = ((SocketServerThread) sensorThreads[i]).getSensorName();
        }

        logger.exiting(this.getClass().getName(), "getSensorName", names);

        return names;
    }

    /**
     * This method is used to shut down the <em>SocketServer</em> and
     * all running <em>SocketServerThreads</em>.
     */
    public final void shutDown() {

        logger.entering(this.getClass().getName(), "shutDown");

        logger.log(Level.INFO, "Shutting down SocketServer at port: "
                               + this.tcpPort);

        Object[] threadArray = this.threadPool.values().toArray();

        logger.log(Level.INFO, "Going to stop " + threadArray.length
                               + " running ServerThreads.");

        for (Object threadObject : threadArray) {
            SocketServerThread thread = (SocketServerThread) threadObject;

            thread.stopSensorThread();

        }

        logger.log(Level.INFO, "All ServerThreads have been stopped.");

        this.run = false;

        if (this.serverSocket != null) {
            try {
                this.serverSocket.close();

                logger.log(Level.INFO, "The Socket has been closed.");
            } catch (IOException e) {

                logger.log(Level.WARNING,
                    "The socket could not be closed. Shutdown was not clean.");
            }
        }

        logger.log(Level.INFO, "Shutdown complete");

        logger.exiting(this.getClass().getName(), "shutDown");
    }

    /**
     * This is called from {@link SocketSourceModule#getEventReader()}.
     * The <em>EventReader</em> are managed in this <em>SocketServer's</em>
     * ThreadPool.
     * @return All running <em>SocketServerThreads</em> as <em>Eventreader</em>
     */
    public final EventReader[] getEventReader() {

        logger.entering(this.getClass().getName(), "getEventReader");

        SocketServerThread[] threads = this.threadPool.values().toArray(
            new SocketServerThread[this.threadPool.size()]);

        logger.entering(this.getClass().getName(), "getEventReader", threads);

        return threads;
    }
}
