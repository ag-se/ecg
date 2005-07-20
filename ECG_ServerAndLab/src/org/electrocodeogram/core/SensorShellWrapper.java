package org.electrocodeogram.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.EventPacket;
import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.msdt.MicroSensorDataTypeNotFoundException;
import org.electrocodeogram.msdt.MsdtManager;
import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.kernel.shell.SensorShell;

/**
 * It is also the entry point for the event data that is recorded by all running ECG sensors
 * to be processed through the ECG Lab's modules for analysis and storage.
 *  
 * The SensorShellWrapper extends and uses the HackyStat SensorShell class to validate
 * incoming events as beeing events of a legal HackyStat SensorDataType.
 */
public class SensorShellWrapper extends SensorShell implements SensorShellInterface
{
    private Logger logger = null;
   
   
    private Core core = null;
   
    private int processingID = 0;

    /*
     * The private constructot creates one instance of the SensorShellWrapper and
     * also creates all other needed ECG Server & Lab components.
     */
    public SensorShellWrapper(Core corePar)
    {
        super(new SensorProperties("", ""), false, "ElectroCodeoGram");

        this.core = corePar;
        
        this.logger = Logger.getLogger("ECG Server");
            
    }
    
    
    /**
     * @see org.hackystat.kernel.shell.SensorShell#doCommand(java.util.Date, java.lang.String, java.util.List)
     * 
     * This is the overriden version of the HackyStat SensorShell's method. After calling the original
     * method it performs further steps to pass the event data into the ECG Lab. It is marked
     * as synchronized as it can be called by multiple running ServerThreads at the same time.
     */
    @Override
    public synchronized boolean doCommand(Date timeStamp, String commandName, List argList)
    {
        this.processingID++;
        
        this.logger.log(Level.INFO,this.processingID + ": Begin to process new event data at " +  new Date().toString());
        
        // validate the incoming event data by using the HackyStat framework
        boolean result = super.doCommand(timeStamp, commandName, argList);
        
        if (result) {

            this.logger.log(Level.INFO,this.processingID + ": Event data is conforming to a HackyStat SensorDataType and is processed.");
            
            EventPacket eventPacket = new EventPacket(0,timeStamp,commandName,argList);
            
            this.logger.log(Level.INFO,this.processingID + " : " + eventPacket.toString());
            
            List<String> newArgList = new ArrayList<String>(argList.size());

            Object[] entries = argList.toArray();

            for (int i = 0; i < entries.length; i++) {
                String entryString = (String) entries[i];

                if (commandName.equals("Activity") && i == 0) {
                    entryString = "" + entryString;
                     
                }

                newArgList.add(entryString);
            }

            isMsdt(newArgList);
            
            appendToEventSource(timeStamp, "" + commandName, newArgList);

            return true;
        }
        
        this.logger.log(Level.INFO,this.processingID + ": Event data is not conforming to a HackyStat SensorDataType and is discarded.");
        
        this.logger.log(Level.INFO,this.processingID + ":" + new EventPacket(0,timeStamp,commandName,argList).toString());
        
        return false;

    }

    private void isMsdt(List<String> argList) {
		
    	String mSdtName = argList.get(1);
    	
    	if(mSdtName == null || mSdtName == "")
    	{
    		this.logger.log(Level.WARNING,this.processingID + ": Event data is not conforming to a ECG MicroSensorDataType.");
    		
    		return;
    	}
    	
    	MicroSensorDataType microSensorDataType = null;
    	
    	try {
    		
    		
    		
			MsdtManager msdtManager = this.core.getMsdtManager();
			
			if(msdtManager == null)
			{
				this.logger.log(Level.WARNING,this.processingID + ": Event data is not conforming to a ECG MicroSensorDataType.");
	    		
	    		return;
			}
			
			microSensorDataType = msdtManager.getMicroSensorDataType(mSdtName);
			
		} catch (MicroSensorDataTypeNotFoundException e) {
			
			this.logger.log(Level.WARNING,this.processingID + ": Event data is not conforming to a ECG MicroSensorDataType.");
    		
    		return;
		}
    	
		this.logger.log(Level.INFO,this.processingID + ": Event data is conforming to the " + microSensorDataType.getName() + " ECG MicroSensorDataType.");
		
		return;
		
	}


	/*
     * This method is used to pass the event data to the first module.
     *
     */
    private void appendToEventSource(Date timeStamp, String commandName, List argList)
    {
        if (this.core.getSensorSource() != null) {
            ValidEventPacket toAppend;
            try {

                toAppend = new ValidEventPacket(0, timeStamp, commandName,
                        argList);
                
                this.core.getSensorSource().append(toAppend);
            }
            catch (IllegalEventParameterException e) {

                // As only ValidEventPackets come this far, this should never happen
                
                e.printStackTrace();
                
                this.logger.log(Level.SEVERE,"An unexpected exception has occured. Please report this at www.electrocodeogram.org");
            }
        }
    }

    /*
     * This thread maintains the console of the ECG Server & Lab application.
s     * 
     */
    
}