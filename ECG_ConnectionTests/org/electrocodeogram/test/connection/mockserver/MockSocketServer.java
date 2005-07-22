package org.electrocodeogram.test.connection.mockserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

import org.electrocodeogram.core.SensorShellInterface;
import org.electrocodeogram.module.source.ISocketServerThread;
import org.electrocodeogram.module.source.SocketServer;
import org.electrocodeogram.module.source.SocketServerThread;

public class MockSocketServer extends SocketServer
{

	public MockSocketServer(SensorShellInterface shellPar) {
		super(shellPar);
		// TODO Auto-generated constructor stub
	}

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
                
                // this mathod call blocks until a new incoming connection request
                Socket socketToSensor = this.serverSocket.accept();
                
                this.logger.log(Level.INFO,"New connection request");
                
                // create a new ServerThread to communicate on the given Socket
                ISocketServerThread serverThread = new MockSocketServerThread(this,this.shell,socketToSensor);
                
                // put the Serverthread in the threadpool
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
