/*
 * Created on 02.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.sensorwrapper;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.electrocodeogram.sensorwrapper.SensorProperties;
/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SensorShell
{
    private ArrayList backubBuffer = null;
    
    private boolean connectedFlag = false;
    
    private ObjectOutputStream oos = null;
    
    private Socket socketToServer = null;
    
    private InetAddress host = null;
    
    private int port;
    /**
     * @param string
     * @param b
     * @param properties
     * 
     */
    public SensorShell(SensorProperties properties, boolean b, String string)
    {

        try
        {
            host = properties.getECGServerAddress();
            
            port = properties.getECGServerPort();
            
            connectToServer();
        }
        catch(IOException e)
        {
        }
        
    }

    /**
     * @throws IOException
     */
    private void connectToServer() throws IOException
    {
        socketToServer = new Socket(host,port);
        
        connectedFlag = true;
        
        oos = new ObjectOutputStream(socketToServer.getOutputStream());
    }

    public boolean doCommand(Date timeStamp, String commandName, List argList)
    {
        if(connectedFlag)
        {
            try
            {
                oos.flush();
                oos.writeObject(new EventPacket(0,timeStamp,commandName,argList));
                oos.flush();
            }
            catch(IOException e)
            {
                connectedFlag = false;
            
                if(backubBuffer == null)
                {
                    backubBuffer = new ArrayList();
                 
                }
                backubBuffer.add(new EventPacket(0,timeStamp,commandName,argList));
            }
        }
        else
        {
            try {
                connectToServer();
                
                connectedFlag = true;
                
                if(backubBuffer != null)
                {
                    for(int i=0;i<backubBuffer.size();i++)
                    {
                        oos.flush();
                        oos.writeObject((EventPacket)(backubBuffer.get(i)));
                        oos.flush();
                    }
                }
                
                oos.flush();
                oos.writeObject(new EventPacket(0,timeStamp,commandName,argList));
                oos.flush();
            }
            catch (IOException e)
            {
                
                if(backubBuffer == null)
                {
                    backubBuffer = new ArrayList();
                   
                }
                backubBuffer.add(new EventPacket(0,timeStamp,commandName,argList));
            }
        }
        
        return true;
        
    }
    
}
