package org.electrocodeogram.core;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.electrocodeogram.module.source.SensorSource;
import org.electrocodeogram.sensorwrapper.EventPacket;
import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.kernel.shell.SensorShell;

/**
 * @author Frank Schlesinger
 */

public class SensorShellWrapper extends SensorShell
{
    
    private static final SensorShellWrapper theInstance = new SensorShellWrapper(new SensorProperties("",""),false,"ECG");
    
    /**
     * 
     * @uml.property name="sensorSource"
     * @uml.associationEnd multiplicity="(0 1)"
     */
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
            appendToEventSource(timeStamp, commandName, argList);
            
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