/*
 * Created on 02.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import org.electrocodeogram.sensorwrapper.EventPacket;

/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SensorThread extends Thread
{

    private static int count = 0;
    
    private int id = -1;
    
    private Socket socketToSensor = null;
    
    private boolean runningFlag = true;
    
    private ObjectInputStream ois = null;
    
    private SensorServer seso = null;
   
    public InetAddress getSensorAddress()
    {
        if (socketToSensor != null)
        {
            return socketToSensor.getInetAddress();
        }
        else
        {
            return null;
        }
    }
    
    public SensorThread(SensorServer seso, Socket socketToSensor)
    {
        super();
                
        id = ++count;
        
        this.seso = seso;
        
        this.socketToSensor = socketToSensor;
        
        try {
            ois = new ObjectInputStream(socketToSensor.getInputStream());
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public int getId()
    {
        return id;
    }
    
    
    public void stopSensorThread()
    {
        runningFlag = false;
    }
    
    public void run()
    {
        while(runningFlag)
        {
            try {
                EventPacket e = (EventPacket) ois.readObject();
                
                SensorShellWrapper.getInstance().doCommand(e.getTimeStamp(),e.getCommandName(),e.getArglist());
            }
            catch(SocketException e)
            {
                runningFlag = false;
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        seso.removeSensorThread(id);
        
    }



}
