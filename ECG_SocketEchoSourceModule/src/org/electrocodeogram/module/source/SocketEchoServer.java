package org.electrocodeogram.module.source;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

/**
 * The SensorServer Thread is continuously listening for connection requests
 * by ECG sensors. If a connection attempt by a ECG sensor was successful
 * a new ServerThread is created and started to maintain the socket communication
 * to the ECG sensor.
 * All running ServerThreads are managed in a threadpool.
 *
 */
public class SocketEchoServer extends SocketServer 
{

    
    /**
     * This creates a new SensorServer and a new threadpool. 
     * @param module Is the source module to which the received event data is passed
    
     */
    public SocketEchoServer(SourceModule module)
    {
        super(module);
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
                SocketEchoServerThread serverThread = new SocketEchoServerThread(this,this.sourceModule,socketToSensor);
                
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

}