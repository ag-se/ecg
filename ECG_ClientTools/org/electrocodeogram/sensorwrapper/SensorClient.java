/*
 * Created on 02.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.sensorwrapper;
import java.util.Date;
import java.util.List;

import java.net.InetAddress;
import java.net.Socket;
import java.io.ObjectOutputStream;	

import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.kernel.shell.SensorShell;

import java.net.UnknownHostException;
import java.io.IOException;
/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SensorClient extends SensorShell
{
    
    private boolean connectedFlag = false;
    
    private ObjectOutputStream oos = null;
    
    private Socket socketToServer = null;
    
    public static InetAddress HOST = null;
    
    public static final int PORT = 22222;
    /**
     * @param string
     * @param b
     * @param properties
     * 
     */
    public SensorClient(SensorProperties properties, boolean b, String string)
    {
        super(properties, b, string);
        
        try
        {
            HOST = InetAddress.getLocalHost();
            
            socketToServer = new Socket(HOST,PORT);
            
            connectedFlag = true;
            
            oos = new ObjectOutputStream(socketToServer.getOutputStream());
        }
        catch(UnknownHostException e)
        {          
        }
        catch(IOException e)
        {
        }
        
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
                return false;
            }
        }
        return true;
        
    }
    
}
