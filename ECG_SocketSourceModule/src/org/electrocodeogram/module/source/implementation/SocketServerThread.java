/*
 * Class: SocketSourceModule
 * Version: 1.0
 * Date: 19.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.source.implementation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.source.EventReader;
import org.electrocodeogram.module.source.EventReaderException;
import org.electrocodeogram.module.source.SourceModule;

/**
 * Each sensor has one own <em>SocketServerThread</em> as his communication partner.
 * They are all created and destroyed from the {@link SocketServer}.
 */
public class SocketServerThread extends EventReader {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(SocketServerThread.class.getName());

    /**
     * A static ounter for <em>SocketServerThread</em> instances.
     */
    private static int count = 0;

    /**
     * The unique int id of this <code>Thread</code>.
     */
    private int id = -1;

    /**
     * A reference to the socket from this <code>Thread</code> to a sensor.
     */
    private Socket socketToSensor;

    /**
     * A reference to the stream from which to read in events.
     */
    private ObjectInputStream ois;

    /**
     * A reference to the <em>SocketServer</em>.
     */
    private ISocketServer socketServer;

    /**
     * The name of the sensor communicating with this <code>Thread</code>.
     */
    private String sensorName;

    /**
     * This is a buffer object, where the last read event is kept.
     */
    private Object object;

    /**
     * This is a buffer object, where the last created <em>WellFormedeventPacket</em> is kept.
     */
    private WellFormedEventPacket packet;

    /**
     * This method returns the name of the connected ECG
     * sensor.
     * @return The name of the currently connected ECG sensor
     */
    public final String getSensorName() {
        logger.entering(this.getClass().getName(), "getSensorName");

        logger.exiting(this.getClass().getName(), "getSensorName",
            this.sensorName);

        return this.sensorName;
    }

    /**
     * This method returns the IP-Address of the currently connected
     * ECG sensor.
     * @return The IP-Address of the currently connected ECG sensor
     */
    public final InetAddress getSensorAddress() {
        logger.entering(this.getClass().getName(), "getSensorAddress");

        if (this.socketToSensor != null) {
            return this.socketToSensor.getInetAddress();
        }

        logger.exiting(this.getClass().getName(), "getSensorAddress", null);

        return null;

    }

    /**
     * This creates a new <em>SocketServerThread</em> and is called from {@link SocketServer}
     * for every new incomming connection request.
     * @param server
     *            A reference to the <em>SocketServer</em>, which is managing
     *            this <em>SocketServerThread</em>
     * @param module
     *            Is the source module to which the received event
     *            data is passed
     * @param socket
     *            The socket to the ECG sensor
     * @throws IOException
     *             If the creation of the ObjectInputStream fails
     */
    public SocketServerThread(final ISocketServer server,
        final SourceModule module, final Socket socket) throws IOException {
        super(module);

        logger.entering(this.getClass().getName(), "SocketServerThread",
            new Object[] {server, module, socket});

        if (module == null) {
            logger
                .log(Level.SEVERE,
                    "The parameter \"module\" is null. Can not create the SocketServerThread");

            logger.exiting(this.getClass().getName(), "SocketServerThread");

            return;
        }

        if (server == null) {
            logger
                .log(Level.SEVERE,
                    "The parameter \"server\" is null. Can not create the SocketServerThread");

            logger.exiting(this.getClass().getName(), "SocketServerThread");

            return;
        }

        if (socket == null) {
            logger
                .log(Level.SEVERE,
                    "The parameter \"socket\" is null. Can not create the SocketServerThread");

            logger.exiting(this.getClass().getName(), "SocketServerThread");

            return;
        }

        // Assign the ServerThread a unique ID
        this.id = ++count;

        this.socketServer = server;

        this.socketToSensor = socket;

        this.ois = new ObjectInputStream(socket.getInputStream());

        this.startReader();

        logger.exiting(this.getClass().getName(), "SocketServerThread");
    }

    /**
     * This method returns the unique îd of the <em>SokctetServerThread</em>.
     * @return The unique id of the <em>SokctetServerThread</em>
     */
    public final int getServerThreadId() {
        logger.entering(this.getClass().getName(), "getSensorThreadId");

        logger.exiting(this.getClass().getName(), "getSensorThreadId", new Integer(this.id));

        return this.id;
    }

    /**
     * This method stops the the <code>Thread</code>.
     */
    public final void stopSensorThread() {
        logger.entering(this.getClass().getName(), "stopSensorThread");

        this.socketServer.removeSensorThread(this.id);

        try {
            if (this.ois != null) {
                this.ois.close();
            }
            if (this.socketToSensor != null) {
                this.socketToSensor.close();
            }
        } catch (IOException e) {

            logger.log(Level.WARNING,
                "The ServerThread could not be stopped cleanly.");
        }

        logger.exiting(this.getClass().getName(), "stopSensorThread");
    }

    /**
     * @see org.electrocodeogram.module.source.EventReader#read()
     */
    @Override
    public final WellFormedEventPacket read() throws EventReaderException {

        logger.entering(this.getClass().getName(), "read");

        try {
            this.object = this.ois.readObject();

            logger
                .log(Level.FINE,
                    "An object has been received over the socket and deserialized.");

            this.packet = (WellFormedEventPacket) this.object;

            logger.log(Level.FINE, "The object is a WellFormedeventPacket.");

            WellFormedEventPacket readPacket = new WellFormedEventPacket(0,
                this.packet.getTimeStamp(), this.packet.getSensorDataType(),
                this.packet.getArgList());

            logger.log(Level.FINE,
                "A new WellFormedEventPacket has been created to be returned.");

            logger.exiting(this.getClass().getName(), "read", readPacket);

            return readPacket;
        } catch (Exception e) {

            logger
                .log(Level.WARNING,
                    "An Exception occured while reading and deserializing an object.");

            logger.log(Level.WARNING, e.getMessage());

            logger.exiting(this.getClass().getName(), "read");

            throw new EventReaderException("");
        }

    }
}
