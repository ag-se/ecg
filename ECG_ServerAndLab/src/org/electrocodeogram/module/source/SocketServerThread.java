package org.electrocodeogram.module.source;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.electrocodeogram.core.SensorShellInterface;
import org.electrocodeogram.event.ValidEventPacket;

/**
 * A ServerThread maintains communication with a single ECG sesnor.
 * Communication is done by (de)serialization over sockets.
 */
public class SocketServerThread extends Thread implements ISocketServerThread
{
    protected Logger logger = null;
    
    private static int count = 0;
    
    private int id = -1;
    
    protected Socket socketToSensor = null;
    
    protected boolean runningFlag = true;
    
    protected ObjectInputStream objectInputStream = null;
    
    private SocketServer sensorServer = null;

    protected String sensorName = null;
    
    protected SensorShellInterface shell = null;
   
    /* (non-Javadoc)
	 * @see org.electrocodeogram.module.source.ISocketServerThread#getSensorName()
	 */
    public String getSensorName()
    {
        return this.sensorName;
    }
    
    /* (non-Javadoc)
	 * @see org.electrocodeogram.module.source.ISocketServerThread#getSensorAddress()
	 */
    public InetAddress getSensorAddress()
    {
        if (this.socketToSensor != null)
        {
            return this.socketToSensor.getInetAddress();
        }
       
        return null;
       
    }
    
    /**
     * This creates a new ServerThread.
     * @param sensorServerPar A reference to the SensorServer that is managing this ServerThread
     * @param shellPar Is the SensorShell to which all incoming event data shall be passed
     * @param socketToSensorPar The socket to the ECG sensor
     * @throws IOException If the creation of the ObjectInputStream fails
     */
    public SocketServerThread(SocketServer sensorServerPar, SensorShellInterface shellPar, Socket socketToSensorPar) throws IOException
    {
        super();
        
        this.shell = shellPar;

        this.logger = Logger.getLogger("ECG Server");
        
        // give the ServerThread a unique ID
        this.id = ++count;
        
        this.sensorServer = sensorServerPar;
        
        this.socketToSensor = socketToSensorPar;
        
        this.objectInputStream = new ObjectInputStream(socketToSensorPar.getInputStream());
       
    }
    
    /* (non-Javadoc)
	 * @see org.electrocodeogram.module.source.ISocketServerThread#getServerThreadId()
	 */
    public int getServerThreadId()
    {
        return this.id;
    }
    
    /* (non-Javadoc)
	 * @see org.electrocodeogram.module.source.ISocketServerThread#stopSensorThread()
	 */
    public void stopSensorThread()
    {
        this.runningFlag = false;
        
        this.sensorServer.removeSensorThread(this.id);
        
        
        try {
            if(this.objectInputStream != null)
            {
                this.objectInputStream.close();
            }
            if(this.socketToSensor != null)
            {
                this.socketToSensor.close();
            }
        }
        catch (IOException e) {
            
            this.logger.log(Level.WARNING,"The ServerThread could not be stopped cleanly.");
        }
    }
    
    
    /* (non-Javadoc)
	 * @see org.electrocodeogram.module.source.ISocketServerThread#run()
	 */
    @Override
    public void run()
    {
        while(this.runningFlag)
        {
            try {
                
                // This method call blocks until a serialized object couls be received.
                ValidEventPacket e = (ValidEventPacket) this.objectInputStream.readObject();
                
                if (this.shell.doCommand(e.getTimeStamp(),e.getHsCommandName(),e.getArglist()))
                {
                    /* If the event data contains the "setTool" String, which is giving the name of the application
                     * the sensor runs in, this String is used as the sensor name.
                     */
                    if(e.getHsCommandName().equals(new String("Activity")) && e.getArglist().get(0).equals(new String("setTool")))
                    {
                        String tmpSensorName;
                        
                        if((tmpSensorName = (String)e.getArglist().get(1)) != null)
                        {
                            this.sensorName = tmpSensorName;
                            
                        }
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
                // If something elese then a ValidEventPacket is received, we don't care!
            }
        }
        
    }
}
