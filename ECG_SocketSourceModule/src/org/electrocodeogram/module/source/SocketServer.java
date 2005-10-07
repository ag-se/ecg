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
 * The SensorServer Thread is continuously listening for connection requests
 * by ECG sensors. If a connection attempt by a ECG sensor was successful
 * a new ServerThread is created and started to maintain the socket communication
 * to the ECG sensor.
 * All running ServerThreads are managed in a threadpool.
 *
 */
public class SocketServer extends Thread implements ISocketServer
{
    
	private static Logger _logger = LogHelper.createLogger(SocketServer.class.getName());
    
    private int _port = -1;
    
    private boolean _run = true;
    
    private HashMap<Integer,SocketServerThread> _serverThreadPool;
    
    private ServerSocket _serverSocket;
    
    private SourceModule _sourceModule;
    
    /**
     * This creates a new SensorServer and a new threadpool. 
     * @param module Is the source module to which the received event data is passed
    
     */
    public SocketServer(SourceModule module, int port)
    {
    	_logger.entering(this.getClass().getName(),"SocketServer");
    	
    	if(module == null)
    	{
    		_logger.log(Level.SEVERE,"The parameter module is null. Can not create the SocketServer");
    		
    		return;
    	}
    	
    	if (port > SocketSourceModule.MIN_PORT && port < SocketSourceModule.MAX_PORT)
		{
			this._port = port;
		}
    	else
    	{
    		_logger.log(Level.WARNING, "The value for the port property must be a number greater than " + SocketSourceModule.MIN_PORT + " and less then " + SocketSourceModule.MAX_PORT + ".");
    		
    		_logger.log(Level.SEVERE,"Can not create the SocketServer");
    		
    		return;
    	}
        
        
        this._sourceModule = module;
        
        this._serverThreadPool = new HashMap<Integer,SocketServerThread>();
     
        _logger.exiting(this.getClass().getName(),"SocketServer");

    }
    
    /**
     * This method is returning all known IP addresses of connected ECG sensors.
     * @return An Array of all known IP addresses of connected ECG sensors
     */
    public InetAddress[] getSensorAddresses()
    {
    	_logger.entering(this.getClass().getName(),"getSensorAddress");
    	
        int count = this.getSensorCount();
        
        InetAddress[] addresses = new InetAddress[count];
        
        if (this._serverThreadPool.values() == null)
        {
        	return new InetAddress[0];
        }
        
        Object[] sensorThreads = this._serverThreadPool.values().toArray();
        
        for(int i=0;i<count;i++)
        {
            addresses[i] = ((SocketServerThread)sensorThreads[i]).getSensorAddress();
        }
        
        _logger.exiting(this.getClass().getName(),"getSensorAddress");
        
        return addresses;
    }
    
    /**
     * This method returns the current number of connected ECG sensors.
     * @return The current number of connected ECG sensors
     */
    public int getSensorCount()
    {
    	_logger.entering(this.getClass().getName(),"getSensorCount");
    	
    	_logger.exiting(this.getClass().getName(),"getSensorCount");
    	
        return this._serverThreadPool.size();

    }
    
    /**
     * This method removes a single ServerThread from the threadpool
     * in the case the ServerThread is not needed anymore.
     * @param id The unique ID of the ServerThread to remove. 
     */
    public void removeSensorThread(int id)
    {
    	_logger.entering(this.getClass().getName(),"removeSensorThread");
    	
        this._serverThreadPool.remove(new Integer(id));
        
        _logger.exiting(this.getClass().getName(),"removeSensorThread");
    }

    /**
     * This method returns a String containing the IP address and TCP port this SensorServer
     * is listening on.
     * @return The IP address and TCP port this SensorServer
     * is listening on as a String.
     */
    public String[] getAddress()
    {
    	 _logger.entering(this.getClass().getName(),"getAddress");
    	
        String[] toReturn = null;
        
        try {
            toReturn = new String[] {InetAddress.getLocalHost().toString(),Integer.toString(this._port)};
        }
        catch (UnknownHostException e) {
            
            // As the localhost should not be unknown, this should never happen. 
            
            _logger.log(Level.SEVERE,"An unexpected exception has occurred. Please report this at www.electrocodeogram.org");
        }
        
        _logger.exiting(this.getClass().getName(),"getAddress");
        
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
    	 _logger.entering(this.getClass().getName(),"run");
    	
        try {

            this._serverSocket = new ServerSocket(this._port);

            _logger.log(Level.INFO,"ECG Server is up and listening on port: " + this._port);
            
        }
        catch (IOException e) {
            
            _logger.log(Level.SEVERE,"The ECG Server could not be started. (Maybe port " + this._port + " is in use?");
            
            this.shutDown();
            
        }
        while(this._run)
        {
            try {
                
                // this method call blocks until a new incoming connection request
                Socket socketToSensor = this._serverSocket.accept();
                
                _logger.log(Level.INFO,"New connection request from: " + socketToSensor.getInetAddress().toString());
                
                // create a new ServerThread to communicate on the given Socket
                SocketServerThread serverThread = new SocketServerThread(this,this._sourceModule,socketToSensor);
                
                _logger.log(Level.INFO,"A new ServerThread has been created.");
                
                // put the ServerThread in the threadpool
                this._serverThreadPool.put(new Integer(serverThread.getServerThreadId()),serverThread);
                
                // start the ServerThread
                serverThread.start();
                
                _logger.log(Level.INFO,"The new ServerThread has been started.");
                
            }
            catch (IOException e) {
                
                _logger.log(Level.WARNING,"New connection request failed.");
                
            }
        }
        
        _logger.exiting(this.getClass().getName(),"run");
    }

    /**
     * This method returns all known ECG sensor names of connected ECG sensors.
     * @return All known ECG sensor names of connected ECG sensors in an Array of Strings
     */
    public String[] getSensorNames()
    {
    	 _logger.entering(this.getClass().getName(),"getSensorName");
    	
        int count = this.getSensorCount();
        
        String[] names = new String[count];
        
        Object[] sensorThreads = this._serverThreadPool.values().toArray();
        
        for(int i=0;i<count;i++)
        {
            names[i] = ((SocketServerThread)sensorThreads[i]).getSensorName();
        }
        
        _logger.exiting(this.getClass().getName(),"getSensorName");
        
        return names;
    }

    /**
     * This method is used to shut down the SensorServer thread and all
     * running ServerThreads.
     *
     */
    public void shutDown()
    {
    	
    	_logger.entering(this.getClass().getName(),"shutDown");
    	
        _logger.log(Level.INFO,"Shutting down SocketServer at port: " + this._port);
        
        Object[] threadArray = this._serverThreadPool.values().toArray();
        
        _logger.log(Level.INFO,"Going to stop " + threadArray.length + " running ServerThreads.");
        
        for(Object threadObject : threadArray)
        {
            SocketServerThread thread = (SocketServerThread) threadObject;
            
            thread.stopSensorThread();
            
        }

        _logger.log(Level.INFO,"All ServerThreads have been stopped.");
        
        this._run = false;
        
        if(this._serverSocket != null)
        {
            try {
                this._serverSocket.close();
                
                _logger.log(Level.INFO,"The Socket has been closed.");
            }
            catch (IOException e) {
                
                _logger.log(Level.WARNING,"The socket could not be closed. Shutdown was not clean.");
            }
        }
        
        _logger.log(Level.INFO,"Shutdown complete");
        
        _logger.exiting(this.getClass().getName(),"shutDown");
    }
}
