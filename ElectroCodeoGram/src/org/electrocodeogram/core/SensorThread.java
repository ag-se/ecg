/*
 * Created on 02.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SensorThread extends Thread
{

    private Socket socketToSensor = null;
    
    private boolean runningFlag = true;
    
    private ObjectInputStream ois = null;
   
    public SensorThread(Socket socketToSensor)
    {
        super();
        
        this.socketToSensor = socketToSensor;
        
        try {
            ois = new ObjectInputStream(socketToSensor.getInputStream());
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void run()
    {
        while(runningFlag)
        {
            try {
                EventPacket e = (EventPacket) ois.readObject();
                
                SensorShellWrapper.getInstance().doCommand(e.getTimeStamp(),e.getCommandName(),e.getArglist());
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
    }



}
