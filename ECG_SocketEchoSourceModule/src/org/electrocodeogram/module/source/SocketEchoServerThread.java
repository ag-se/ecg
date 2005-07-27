package org.electrocodeogram.module.source;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;

import org.electrocodeogram.event.ValidEventPacket;

/**
 * A ServerThread maintains communication with a single ECG sensor.
 * Communication is done by (de)serialization over sockets.
 */
public class SocketEchoServerThread extends SocketServerThread
{

    private ObjectOutputStream objectOutputStream = null;
    
    private int count = 0;

    /**
     * This creates a new ServerThread.
     * @param sensorServer A reference to the SensorServer that is managing this ServerThread
     * @param module Is the source module to which the received event data is passed
     * @param socketToSensor The socket to the ECG sensor
     * @throws IOException If the creation of the ObjectInputStream fails
     */
    public SocketEchoServerThread(SocketEchoServer sensorServer, SourceModule module, Socket socketToSensor) throws IOException
    {
        super(sensorServer, module, socketToSensor);

        this.objectOutputStream = new ObjectOutputStream(
                socketToSensor.getOutputStream());
    }

    /**
     * @see java.lang.Thread#run()
     * The receiving of event data is implemented here.
     */
    @Override
    public void run()
    {
        while (this.runningFlag) {
            try {

                // This method call blocks until a serialized object could be received.
                ValidEventPacket e = null;

                Object object = this.objectInputStream.readObject();

                e = (ValidEventPacket) object;

                this.logger.log(Level.INFO, "Received ValidEventPacket: " + ++this.count);

                this.logger.log(Level.INFO, "Initiating Echo for: " + this.count);

                this.objectOutputStream.flush();

                this.objectOutputStream.writeObject(e);

                this.objectOutputStream.flush();
                
                this.logger.log(Level.INFO, "Send Echo for: " + this.count);
                
            }
            catch (SocketException e) {
                this.runningFlag = false;

                this.logger.log(Level.WARNING, "The socket connection to the ECG Sensor is lost.");
            }
            catch (IOException e) {

                this.logger.log(Level.WARNING, "Error while reading from the ECG sensor.");
            }
            catch (ClassNotFoundException e) {

                this.logger.log(Level.WARNING, "Error while reading from the ECG sensor.");

            }
            catch (ClassCastException e) {
                // If something else then a ValidEventPacket is received, we don't care!
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
