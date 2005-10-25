package org.electrocodeogram.module.source;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;

/**
 * A ServerThread maintains communication with a single ECG sensor.
 * Communication is done by (de)serialization over sockets.
 */
public class SocketServerThread extends EventReader {

    private static Logger logger = LogHelper
        .createLogger(SocketServerThread.class.getName());

    private static int _count = 0;

    private int _id = -1;

    private Socket _socketToSensor = null;

    private ObjectInputStream _ois = null;

    private ISocketServer _sensorServer = null;

    private String _sensorName = null;

    private Object _object;

    private WellFormedEventPacket _packet;

    /**
     * This method returns the name of the currently connected ECG
     * sensor.
     * @return The name of the currently connected ECG sensor
     */
    public String getSensorName() {
        logger.entering(this.getClass().getName(), "getSensorName");

        logger.exiting(this.getClass().getName(), "getSensorName");

        return this._sensorName;
    }

    /**
     * This method returns the IP address of the currently connected
     * ECG sensor.
     * @return The IP address of the currently connected ECG sensor
     */
    public InetAddress getSensorAddress() {
        logger.entering(this.getClass().getName(), "getSensorAddress");

        if (this._socketToSensor != null) {
            return this._socketToSensor.getInetAddress();
        }

        logger.exiting(this.getClass().getName(), "getSensorAddress");

        return null;

    }

    /**
     * This creates a new ServerThread.
     * @param sensorServer
     *            A reference to the SensorServer that is managing
     *            this ServerThread
     * @param module
     *            Is the source module to which the received event
     *            data is passed
     * @param socketToSensor
     *            The socket to the ECG sensor
     * @throws IOException
     *             If the creation of the ObjectInputStream fails
     */
    public SocketServerThread(ISocketServer sensorServer, SourceModule module,
        Socket socketToSensor) throws IOException {
        super(module);

        logger.entering(this.getClass().getName(), "SocketServerThread");

        if (module == null) {
            logger
                .log(Level.SEVERE,
                    "The parameter module is null. Can not create the SocketServerThread");

            return;
        }

        if (sensorServer == null) {
            logger
                .log(Level.SEVERE,
                    "The parameter sensorServer is null. Can not create the SocketServerThread");

            return;
        }

        if (socketToSensor == null) {
            logger
                .log(Level.SEVERE,
                    "The parameter socketToSensor is null. Can not create the SocketServerThread");

            return;
        }

        // Assign the ServerThread a unique ID
        this._id = ++_count;

        this._sensorServer = sensorServer;

        this._socketToSensor = socketToSensor;

        this._ois = new ObjectInputStream(socketToSensor.getInputStream());

        this.startReader();

        logger.exiting(this.getClass().getName(), "SocketServerThread");
    }

    /**
     * This method returns the unique ID of the ServerThread.
     * @return The unique ID of the ServerThread
     */
    public int getServerThreadId() {
        logger.entering(this.getClass().getName(), "getSensorThreadId");

        logger.exiting(this.getClass().getName(), "getSensorThreadId");

        return this._id;
    }

    /**
     * This method stops the the Thread.
     */
    public void stopSensorThread() {
        logger.entering(this.getClass().getName(), "stopSensorThread");

        this._sensorServer.removeSensorThread(this._id);

        try {
            if (this._ois != null) {
                this._ois.close();
            }
            if (this._socketToSensor != null) {
                this._socketToSensor.close();
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
    public WellFormedEventPacket read() throws EventReaderException {

        logger.entering(this.getClass().getName(), "read");

        try {
            this._object = this._ois.readObject();

            logger
                .log(Level.FINE,
                    "An object has been received over the socket and deserialized.");

            this._packet = (WellFormedEventPacket) this._object;

            logger.log(Level.FINE, "The object is a WellFormedeventPacket.");

            WellFormedEventPacket packet = new WellFormedEventPacket(0,
                this._packet.getTimeStamp(), this._packet.getSensorDataType(),
                this._packet.getArgList());

            logger.log(Level.FINE,
                "A new WellFormedEventPacket has been created to be returned.");

            logger.exiting(this.getClass().getName(), "read", packet);

            return packet;
        } catch (Exception e) {

            logger
                .log(Level.WARNING,
                    "An Exception occured while reading and deserializing an object.");

            logger.log(Level.WARNING, e.getMessage());

            logger.exiting(this.getClass().getName(), "read");

            throw new EventReaderException("");
        }

    }

    // /**
    // * @see java.lang.Thread#run()
    // * The receiving of event data is implemented here.
    // */
    // @Override
    // public void run()
    // {
    // _logger.entering(this.getClass().getName(), "run");
    //
    // while (this._run)
    // {
    // try
    // {
    //
    // this._object = this._ois.readObject();
    //
    // this._packet = (WellFormedEventPacket) this._object;
    //
    // WellFormedEventPacket packet = new WellFormedEventPacket(0,
    // this._packet.getTimeStamp(),
    // this._packet.getSensorDataType(),
    // this._packet.getArglist());
    //
    // if (this._packet != null)
    // {
    // this._sourceModule.append(packet);
    //
    // }
    //
    // /* If the event data contains the "setTool" String, which is
    // giving the name of the application
    // * the sensor runs in, this String is used as the sensor name.
    // */
    // if (this._packet.getSensorDataType().equals("Activity") &&
    // this._packet.getArglist().get(0).equals("setTool"))
    // {
    // String tmpSensorName;
    //
    // if ((tmpSensorName = (String) this._packet.getArglist().get(1))
    // != null)
    // {
    // this._sensorName = tmpSensorName;
    //
    // }
    // }
    //
    // }
    // catch (SocketException e)
    // {
    //
    // this._run = false;
    //
    // _logger.log(Level.WARNING, "The socket connection to the sensor
    // is lost.");
    // }
    // catch (IOException e)
    // {
    //
    // this._run = false;
    //				
    // _logger.log(Level.WARNING, "The socket connection to the sensor
    // is lost.");
    //			}
    //			catch (ClassNotFoundException e)
    //			{
    //
    //				_logger.log(Level.WARNING, "The " + this._sourceModule.getName() + " received an object of an unknown class and ignores it.");
    //
    //			}
    //			catch (ClassCastException e)
    //			{
    //				_logger.log(Level.WARNING, "The " + this._sourceModule.getName() + " received an object class that is not WellformedEventPacket and ignores it.");
    //			}
    //			catch (IllegalEventParameterException e)
    //			{
    //				_logger.log(Level.WARNING, "An error occured while reading an event from the sensor.");
    //
    //			}
    //		}
    //
    //		_logger.exiting(this.getClass().getName(), "run");
    //
    //	}
}
