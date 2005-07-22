package org.electrocodeogram.test.connection.mockserver;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;

import org.electrocodeogram.core.SensorShellInterface;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.module.source.SocketServer;
import org.electrocodeogram.module.source.SocketServerThread;

public class MockSocketServerThread extends SocketServerThread{

	private ObjectOutputStream objectOutputStream = null;
	
	public MockSocketServerThread(SocketServer sensorServerPar, SensorShellInterface shellPar, Socket socketToSensorPar) throws IOException {
		
		super(sensorServerPar, shellPar, socketToSensorPar);
		
		this.objectOutputStream = new ObjectOutputStream(this.socketToSensor.getOutputStream());
	}

	public void run()
	{
		while(this.runningFlag)
        {
            try {
                
                // This method call blocks until a serialized object couls be received.
                ValidEventPacket e = (ValidEventPacket) this.objectInputStream.readObject();
               
                this.objectOutputStream.writeObject(e);
                
                this.objectOutputStream.flush();
               
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
	
	

