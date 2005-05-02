package org.electrocodeogram.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.electrocodeogram.module.source.SensorSource;
import org.electrocodeogram.EventPacket;
import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.kernel.shell.SensorShell;

/**
 * @author Frank Schlesinger *  * This class is the is the project's main- and base-class as it is the entry point * of the ElectroCodeoGram (ECG) extension into the "HackyStat" (HS) framework. *  * It's function is to colllect all the event data that is captured by running sensors * and to process it into the ECG framework for analysis and storing. It is therefore * the source of event data in the ECG module modell.  *  * It extends the HS class SensorShell, so it conforms to the "SensorDataType" * (SDT) concept of HS. That means every konventionell HS sensor is able to communicate * with the ECG's SensorShellWrapper.
 */

public class SensorShellWrapper extends SensorShell
{
    
    private static final SensorShellWrapper theInstance = new SensorShellWrapper(new SensorProperties("",""),false,"ECG");
    
    private SensorSource sensorSource = null;
    
    /** The logging instance for SensorShells. */
    private Logger logger;

    
    public static SensorShellWrapper getInstance()
    {
        return theInstance;
        
    }
    
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
    private SensorShellWrapper(SensorProperties sensorProperties, boolean isInteractive, String toolName)
    {
        super(sensorProperties, isInteractive, toolName);
		       
        sensorSource = new SensorSource();
        
        sensorSource.start();
        
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
        
        // TODO : Make a HS writer here
        
        
        
        
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
	        assert(timeStamp != null);
	        
	        EventPacket toAppend = new EventPacket(0,timeStamp,commandName,argList);
	
            sensorSource.append(toAppend);
            
        }
     }
}