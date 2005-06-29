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
import java.util.Collection;
import java.util.HashMap;
import java.util.Observable;

import org.electrocodeogram.module.ModuleRegistry;
import org.electrocodeogram.ui.Configurator;
import org.hackystat.kernel.admin.SensorProperties;

/**
 * @author 7oas7er
 *
 */
public class SensorServer extends Thread
{
    private static SensorServer theInstance = null;
    
    public static final int PORT = 22222;
    
    private boolean runningFlag = true;
    
    private HashMap sensorThreadPool = null;

    private SensorShellWrapper shellWrapper = null;
    
    private ServerSocket seso = null;
    
    private SensorServer()
    {
        this.sensorThreadPool = new HashMap();
    }
    
    public static SensorServer getInstance()
    {
        if(theInstance == null)
        {
            theInstance = new SensorServer();
        }
        
        return theInstance;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Thread#start()
     */
    @Override
    public void start()
    {   if(!this.isAlive())
        {
           super.start();
        }
    }
    
    public InetAddress[] getSensorAddresses()
    {
        int count = this.getSensorCount();
        
        InetAddress[] addresses = new InetAddress[count];
        
        Object[] sensorThreads = sensorThreadPool.values().toArray();
        
        for(int i=0;i<count;i++)
        {
            addresses[i] = ((ServerThread)sensorThreads[i]).getSensorAddress();
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
        
        //doNotifyObservers();
    }
    
//    /**
//     * 
//     */
//    public void doNotifyObservers()
//    {
//        setChanged();
//        notifyObservers(this);
//        clearChanged();
//    }

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
    
    
    
    public void run()
    {
                   
        
        try {
            seso = new ServerSocket(PORT);
            
            //this.doNotifyObservers();
        }
        catch (IOException e) {
            
            e.printStackTrace();
            
            runningFlag = false;
            
        }
        while(this.runningFlag)
        {
            try {
                Socket socketToSensor = seso.accept();
                
              
                ServerThread st = new ServerThread(this,socketToSensor);
                
                this.sensorThreadPool.put(new Integer(st.getSensorThreadId()),st);
                
//                this.setChanged();
//                this.notifyObservers(this);
//                this.clearChanged();
                
                st.start();
                
            }
            catch (IOException e1) {
                System.err.println("Shut Down?!");
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
            names[i] = ((ServerThread)sensorThreads[i]).getSensorName();
        }
        
        return names;
    }

    public void shutDown()
    {
        Object[] threadArray = sensorThreadPool.values().toArray();
        
        for(Object threadObject : threadArray)
        {
            ServerThread thread = (ServerThread) threadObject;
            
            thread.stopSensorThread();
            
        }
                
        this.runningFlag = false;
        
        try {
            seso.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        theInstance = null;
        
    }
}
