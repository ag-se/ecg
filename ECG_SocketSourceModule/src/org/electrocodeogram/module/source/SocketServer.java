package org.electrocodeogram.module.source;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The SensorServer Thread is continuously listening for connection requests
 * by ECG sensors. If a connection attempt by a ECG sensor was successful
 * a new ServerThread is created and started to maintain the socket communication
 * to the ECG sensor.
 * All running ServerThreads are managed in a threadpool.
 *
 */
public class SocketServer extends Thread implements ISocketServer
{
    
    protected Logger logger = null;
    
    // TODO : Make this configurable
    
    /**
     * This is the TCP port to listen on
     */
    public static final int PORT = 22222;
    
    protected boolean runningFlag = true;
    
    protected HashMap<Integer,SocketServerThread> serverThreadPool = null;
    
    protected ServerSocket serverSocket = null;
    
    protected SourceModule sourceModule;
    
    /**
     * This creates a new SensorServer and a new threadpool. 
     * @param module Is the source module to which the received event data is passed
    
     */
    public SocketServer(SourceModule module)
    {
        this.sourceModule = module;
        
        this.serverThreadPool = new HashMap<Integer,SocketServerThread>();
     
        this.logger = Logger.getLogger("ECG Server");

    }
    
    /**
     * This method is returning all known IP addresses of connected ECG sensors.
     * @return An Array of all known IP addresses of connected ECG sensors
     */
    public InetAddress[] getSensorAddresses()
    {
        int count = this.getSensorCount();
        
        InetAddress[] addresses = new InetAddress[count];
        
        Object[] sensorThreads = this.serverThreadPool.values().toArray();
        
        for(int i=0;i<count;i++)
        {
            addresses[i] = ((SocketServerThread)sensorThreads[i]).getSensorAddress();
        }
        
        return addresses;
    }
    
    /**
     * This method returns the current number of connected ECG sensors.
     * @return The current number of connected ECG sensors
     */
    public int getSensorCount()
    {
        return this.serverThreadPool.size();
    }
    
    /**
     * This method removes a single ServerThread from the threadpool
     * in the case the ServerThread is not needed anymore.
     * @param id The unique ID of the ServerThread to remove. 
     */
    public void removeSensorThread(int id)
    {
        this.serverThreadPool.remove(new Integer(id));
    }

    /**
     * This method returns a String containing the IP address and TCP port this SensorServer
     * is listening on.
     * @return The IP address and TCP port this SensorServer
     * is listening on as a String.
     */
    public String[] getAddress()
    {
        String[] toReturn = null;
        
        try {
            toReturn = new String[] {InetAddress.getLocalHost().toString(),new Integer(SocketServer.PORT).toString()};
        }
        catch (UnknownHostException e) {
            
            // As the localhost should not be unknown, this should never happen. 
            
            this.logger.log(Level.SEVERE,"An unexpected exception has occurred. Please report this at www.electrocodeogram.org");
        }
        return toReturn;
    }
    
    
    
    /**
     * @see java.lang.Thread#run()
     * 
     * Here the listening for connection requests and starting of new ServerThreads is done.
     */
    @Override
    public void run()
    {
        try {

            this.serverSocket = new ServerSocket(PORT);

            System.out.println("ECG Server is up and listening on port: " + PORT);
            
        }
        catch (IOException e) {
            
            this.logger.log(Level.SEVERE,"The ECG Server could not be started. (Maybe port " + PORT + " is in use?");
            
            this.runningFlag = false;
            
        }
        while(this.runningFlag)
        {
            try {
                
                // this method call blocks until a new incoming connection request
                Socket socketToSensor = this.serverSocket.accept();
                
                this.logger.log(Level.INFO,"New connection request");
                
                // create a new ServerThread to communicate on the given Socket
                SocketServerThread serverThread = new SocketServerThread(this,this.sourceModule,socketToSensor);
                
                // put the ServerThread in the threadpool
                this.serverThreadPool.put(new Integer(serverThread.getServerThreadId()),serverThread);
                
                // start the ServerThread
                serverThread.start();
                
            }
            catch (IOException e) {
                
                this.logger.log(Level.WARNING,"New connection request failed");
                
            }
        }
    }

    /**
     * This method returns all known ECG sensor names of connected ECG sensors.
     * @return All known ECG sensor names of connected ECG sensors in an Array of Strings
     */
    public String[] getSensorNames()
    {
        int count = this.getSensorCount();
        
        String[] names = new String[count];
        
        Object[] sensorThreads = this.serverThreadPool.values().toArray();
        
        for(int i=0;i<count;i++)
        {
            names[i] = ((SocketServerThread)sensorThreads[i]).getSensorName();
        }
        
        return names;
    }

    /**
     * This method is used to shut down the SensorServer thread and all
     * running ServerThreads.
     *
     */
    public void shutDown()
    {
        Object[] threadArray = this.serverThreadPool.values().toArray();
        
        for(Object threadObject : threadArray)
        {
            SocketServerThread thread = (SocketServerThread) threadObject;
            
            thread.stopSensorThread();
            
        }
                
        this.runningFlag = false;
        
        if(this.serverSocket != null)
        {
            try {
                this.serverSocket.close();
            }
            catch (IOException e) {
                
                this.logger.log(Level.WARNING,"The socket could not be closed. Shut down was not clean.");
            }
        }
        
    }
}
