package org.electrocodeogram.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.module.ModuleRegistry;
import org.electrocodeogram.module.source.Source;
import org.electrocodeogram.ui.Configurator;
import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.kernel.sdt.SdtManager;
import org.hackystat.kernel.shell.SensorShell;

/**
 * This class is the is the project's main- and base-class as it is the entry point * of the ElectroCodeoGram (ECG) extension into the "HackyStat" (HS) framework. *  * It's function is to colllect all the event data that is captured by running sensors * and to process it into the ECG framework for analysis and storing. It is therefore * the source of event data in the ECG module modell.  *  * It extends the HS class SensorShell, so it conforms to the "SensorDataType" * (SDT) concept of HS. That means every konventionell HS sensor is able to communicate * with the ECG's SensorShellWrapper.
 */
public class SensorShellWrapper extends SensorShell
{
    private static SensorShellWrapper theInstance = null;
    
    private Source sensorSource = null;
     
    private int eventPacketCount = 0;
    
    private SensorServer seso = null;
    
    private ModuleRegistry more = null;

    
    
    
    /**
     * The constructor takes the same parameters as the HS SensorShell and sipmly passes
     * them to it's constructor.
     * 
     * It also instanciates the ECG's ModuleRegistry which looks for installed ECG modules
     * and makes them available to the ECG framework.
     * 
     * @param sensorProperties The HS SensorProperties object build from a configuration file
     * at startup time
     * @param isInteractive 
     * @param toolName The name of the developing tool the sending sensor is working inside
     */
    private SensorShellWrapper()
    {
        super(new SensorProperties("",""),false,"ElectroCodeoGram");
      
        Console gob = new Console();
         
        seso = SensorServer.getInstance();
        
        seso.start();
        
        sensorSource = new Source();
        
        sensorSource.setName("Sensor Source");
        
        more = ModuleRegistry.getInstance();
        
        more.addModuleInstance(sensorSource);
        
        gob.start();
        
    }
   
    public SensorShellWrapper(File file)
    {
       this();
       
       ModuleRegistry.getInstance(file);
    }

    public static SensorShellWrapper getInstance()
    {
        if(theInstance == null)
        {
            theInstance = new SensorShellWrapper();
        }
        
        return theInstance;
    }

    /** 
     * This method overwrites the Hs doCommand method
     * @see org.hackystat.kernel.shell.SensorShell#doCommand(java.util.Date, java.lang.String, java.util.List)
     * 
     * Instead of forwarding the event into the HS framework it is forwardied into the ECG framework her.
     */
    public synchronized boolean doCommand(Date timeStamp, String commandName, List argList)
    {
        
        boolean result =  super.doCommand(timeStamp, commandName, argList);
        
        if(result)
        {
            eventPacketCount++;
            
            List newArgList = argList;
            
            if(commandName.equals("Activity"))
            {
                newArgList = new ArrayList(argList.size());
                
                Object[] entries = argList.toArray();
                
                for(int i=0;i<entries.length;i++)
                {
                    if (i == 0)
                    {
                        String activityType = (String) entries[i];
                        
                        activityType = "HS_ACTIVITY_TYPE:" + activityType;
                        
                        entries[i] = activityType;
                    }
                    newArgList.add(entries[i]);
                }
                
            }
            
            appendToEventSource(timeStamp, "HS_COMMAND:" + commandName, newArgList);
            
            return true;
        }
        else
        {
            return false;
        }
        
    }

    /**
     * This method reads out the event string and passes the parts to the EventSource.
     * 
     * @param timeStamp The event's timestamp as reported by the sensor
     * @param commandName The name of the command reported as defined by a SDT
     * @param argList The list of arguments as defined by a SDT
     */
    private void appendToEventSource(Date timeStamp, String commandName, List argList)
    {
        if (sensorSource != null)
        {
	        ValidEventPacket toAppend;
            try {
                
                toAppend = new ValidEventPacket(0,timeStamp,commandName,argList);
                
                sensorSource.append(toAppend);
            }
            catch (IllegalEventParameterException e) {
                
                e.printStackTrace();
            }
	    }
     }
   
   
    
    private class Console extends Thread
    {
        
        private BufferedReader bufferedReader = null;
        
        public Console()
        {
            System.out.println("ElectroCodeoGram Server & Lab is starting...");
            
            this.bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            
        }
        
        public void run()
        {
            
            while (true) {
                
                System.out.println(">>");
                
                String inputString = "" + this.readLine();
                
                System.out.println("Echo: " + inputString);
                
                if (inputString.equalsIgnoreCase("quit")) {
                  this.quit();
                  return;
                }
            }
        }
        
        private String readLine() {
            try {
               return this.bufferedReader.readLine();
            }
            catch (IOException e) {
              return "quit";
            }
          }
        
        
        private void quit()
        {
            System.exit(0);
        }
    }
    
    public static void main(String[] args)
    {
     
        File file = null;
        
        if(args != null && args.length > 0)
        {
            
            file = new File(args[0]);
            
            
            
            if(file.exists() && file.isDirectory())
            {
                SensorShellWrapper shell = new SensorShellWrapper(file);
            }
            else
            {
                SensorShellWrapper shell = new SensorShellWrapper();
            }
            
        }
        else
        {
            SensorShellWrapper shell = new SensorShellWrapper();
        }
        
        
    }
}