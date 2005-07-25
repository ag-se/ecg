package org.electrocodeogram.module.source;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.ValidEventPacket;

/**
 * A ServerThread maintains communication with a single ECG sensor.
 * Communication is done by (de)serialization over sockets.
 */
public class SocketServerThread extends Thread
{
    protected Logger logger = null;
    
    private static int count = 0;
    
    private int id = -1;
    
    protected Socket $socketToSensor = null;
    
    protected boolean runningFlag = true;
    
    protected ObjectInputStream objectInputStream = null;
    
    private SocketServer $sensorServer = null;

    protected String sensorName = null;
    
    protected SourceModule sourceModule;
   
    /**
     * This method returns the name of the currently connected ECG sensor.
     * @return The name of the currently connected ECG sensor
     */
    public String getSensorName()
    {
        return this.sensorName;
    }
    
    /**
     * This method returns the IP address of the currently connected ECG sensor.
     * @return The IP address of the currently connected ECG sensor
     */
    public InetAddress getSensorAddress()
    {
        if (this.$socketToSensor != null)
        {
            return this.$socketToSensor.getInetAddress();
        }
       
        return null;
       
    }
    
    /**
     * This creates a new ServerThread.
     * @param sensorServer A reference to the SensorServer that is managing this ServerThread
     * @param module Is the source module to which the received event data is passed
     * @param socketToSensor The socket to the ECG sensor
     * @throws IOException If the creation of the ObjectInputStream fails
     */
    public SocketServerThread(SocketServer sensorServer, SourceModule module, Socket socketToSensor) throws IOException
    {
        super();
        
        this.sourceModule = module;

        this.logger = Logger.getLogger("ECG Server");
        
        // give the ServerThread a unique ID
        this.id = ++count;
        
        this.$sensorServer = sensorServer;
        
        this.$socketToSensor = socketToSensor;
        
        this.objectInputStream = new ObjectInputStream(socketToSensor.getInputStream());
       
    }
    
    /**
     * This method returns the unique ID of the ServerThread.
     * @return The unique ID of the ServerThread
     */
    public int getServerThreadId()
    {
        return this.id;
    }
    
    /**
     * This method stops the the Thread.
     */
    public void stopSensorThread()
    {
        this.runningFlag = false;
        
        this.$sensorServer.removeSensorThread(this.id);
        
        
        try {
            if(this.objectInputStream != null)
            {
                this.objectInputStream.close();
            }
            if(this.$socketToSensor != null)
            {
                this.$socketToSensor.close();
            }
        }
        catch (IOException e) {
            
            this.logger.log(Level.WARNING,"The ServerThread could not be stopped cleanly.");
        }
    }
    
    
 
    /**
     * @see java.lang.Thread#run()
     * The receiving of event data is implemented here.
     */
    @Override
    public void run()
    {
        while(this.runningFlag)
        {
            try {
                
                // This method call blocks until a serialized object could be received.
                ValidEventPacket e = (ValidEventPacket) this.objectInputStream.readObject();
                
                this.sourceModule.append(e);
                
                    /* If the event data contains the "setTool" String, which is giving the name of the application
                     * the sensor runs in, this String is used as the sensor name.
                     */
                    if(e.getSensorDataType().equals(new String("Activity")) && e.getArglist().get(0).equals(new String("setTool")))
                    {
                        String tmpSensorName;
                        
                        if((tmpSensorName = (String)e.getArglist().get(1)) != null)
                        {
                            this.sensorName = tmpSensorName;
                            
                        }
                    }
                
            }
            catch(SocketException e)
            {
                this.runningFlag = false;
                
                this.logger.log(Level.WARNING,"The socket connection to the ECG Sensor is lost.");
            }
            catch (IOException e) {
                
                this.logger.log(Level.WARNING,"Error while reading from the ECG sensor.");
            }
            catch (ClassNotFoundException e) {

                this.logger.log(Level.WARNING,"Error while reading from the ECG sensor.");
                
            }
            catch(ClassCastException e)
            {
                // If something else then a ValidEventPacket is received, we don't care!
            }
        }
        
    }
}
