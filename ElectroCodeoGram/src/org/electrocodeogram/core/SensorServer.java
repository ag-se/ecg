/*
 * Created on 02.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import org.electrocodeogram.module.ModuleRegistry;
import org.electrocodeogram.ui.Configurator;

/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SensorServer
{
    public static final int PORT = 22222;
    
    private boolean runningFlag = true;
    
    private HashMap sensorThreadPool = null;

    public SensorServer()
    {
        sensorThreadPool = new HashMap();
        
        ModuleRegistry.getInstance();
        
        SensorShellWrapper.getInstance();
        
    }
    
    public static void main(String[] args)
    {
        SensorServer me = new SensorServer();
                   
        ServerSocket seso = null;
        
        try {
            seso = new ServerSocket(PORT);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        while(me.runningFlag)
        {
            try {
                Socket socketToSensor = seso.accept();
                SensorThread st = new SensorThread(socketToSensor);
                
            }
            catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }
}
