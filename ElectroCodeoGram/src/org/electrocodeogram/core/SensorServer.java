/*
 * Created on 02.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Observable;

import org.electrocodeogram.module.ModuleRegistry;
import org.electrocodeogram.ui.Configurator;

/**
 * @author 7oas7er
 *
 */
public class SensorServer extends Observable
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
    
    public InetAddress[] getSensorAddresses()
    {
        int count = this.getSensorCount();
        
        InetAddress[] addresses = new InetAddress[count];
        
        Object[] sensorThreads = sensorThreadPool.values().toArray();
        
        for(int i=0;i<count;i++)
        {
            addresses[i] = ((SensorThread)sensorThreads[i]).getSensorAddress();
        }
        
        return addresses;
    }
    
    public int getSensorCount()
    {
        return sensorThreadPool.size();
    }
    
    public void removeSensorThread(int id)
    {
        sensorThreadPool.remove(new Integer(id));
        
        doNotifyObservers();
    }
    
    /**
     * 
     */
    public void doNotifyObservers()
    {
        setChanged();
        notifyObservers(this);
        clearChanged();
    }

    public String[] getAddress()
    {
        String[] toReturn = null;
        
        try {
            toReturn = new String[] {InetAddress.getLocalHost().toString(),new Integer(SensorServer.PORT).toString()};
        }
        catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return toReturn;
    }
    
    public static void main(String[] args)
    {
        SensorServer me = new SensorServer();
        
        me.addObserver(Configurator.getInstance());
                   
        ServerSocket seso = null;
        
        try {
            seso = new ServerSocket(PORT);
            
            me.doNotifyObservers();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        while(me.runningFlag)
        {
            try {
                Socket socketToSensor = seso.accept();
                
              
                SensorThread st = new SensorThread(me,socketToSensor);
                
                me.sensorThreadPool.put(new Integer(st.getId()),st);
                
                me.setChanged();
                me.notifyObservers(me);
                me.clearChanged();
                
                st.start();
                
            }
            catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    /**
     * @return
     */
    public String[] getSensorNames()
    {
        int count = this.getSensorCount();
        
        String[] names = new String[count];
        
        Object[] sensorThreads = sensorThreadPool.values().toArray();
        
        for(int i=0;i<count;i++)
        {
            names[i] = ((SensorThread)sensorThreads[i]).getSensorName();
        }
        
        return names;
    }
}
