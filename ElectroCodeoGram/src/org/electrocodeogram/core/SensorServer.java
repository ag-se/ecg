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
import org.hackystat.kernel.admin.SensorProperties;

/**
 * @author 7oas7er
 *
 */
public class SensorServer extends Observable
{
    public static SensorServer theInstance = null;
    
    public static final int PORT = 22222;
    
    private boolean runningFlag = true;
    
    private HashMap sensorThreadPool = null;

    private SensorShellWrapper shellWrapper = null;
    
    public static SensorServer getInstance()
    {
        return theInstance;
    }
    
    public SensorServer()
    {
        sensorThreadPool = new HashMap();
                
        ModuleRegistry.getInstance();
        
        shellWrapper = new SensorShellWrapper(new SensorProperties("",""),false,"ECG");
        
    }
    
    public SensorShellWrapper getSensorShellWrapper()
    {
        return shellWrapper;
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
        SensorServer.theInstance = new SensorServer();
        
        theInstance.addObserver(Configurator.getInstance());
                   
        ServerSocket seso = null;
        
        try {
            seso = new ServerSocket(PORT);
            
            theInstance.doNotifyObservers();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        while(theInstance.runningFlag)
        {
            try {
                Socket socketToSensor = seso.accept();
                
              
                SensorThread st = new SensorThread(theInstance,socketToSensor);
                
                theInstance.sensorThreadPool.put(new Integer(st.getSensorThreadId()),st);
                
                theInstance.setChanged();
                theInstance.notifyObservers(theInstance);
                theInstance.clearChanged();
                
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
