/*
 * Created on 02.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.hackystat.kernel.shell;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.electrocodeogram.client.IllegalHostOrPortException;
import org.electrocodeogram.client.SendingThread;
import org.electrocodeogram.event.EventPacket;
import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.hackystat.kernel.admin.SensorProperties;

/**
 * This class is the ECG SensorShell. It is named org.hackystat.kernel.shell.SensorShell
 * after the class provided by the HackyStat project (please visit www.hackystat.org for more information).
 * 
 * It's purpose is to be used by every ECG sensor that is written in Java to take
 * the sensors recorded data and send it to the server. So a sensor developer must not
 * implement the functionality of sending data to the ECG server.
 * 
 * Because the ECG framework directly supports the usage of original HackyStat sensors
 * this class acts like the original HackyStat SensorShell class including the naming.
 * 
 * Instead of processing the sensor data into the HackyStat environment, it is passed
 * over to the ECG server.
 */
public class SensorShell
{
    private SensorProperties properties = null;
    
    private Logger logger = null;
    
    /**
     * This creates a ECG SensorShell instance with the given properties.
     * @param propertiesPar The properties to configure the ECG SensorShell
     * @param b not used
     * @param s not used
     */
    public SensorShell(SensorProperties propertiesPar, @SuppressWarnings("unused") boolean b, @SuppressWarnings("unused") String s)
    {
        // assert parameters
        assert(propertiesPar != null);
        
        this.properties = propertiesPar;
        
        this.logger = Logger.getLogger("ECG_SensorShell");
    }

    /**
     * This method is called by the ECG sensors whenever they record an event. The data
     * of the event is then passed over to the singleton SendingThread and therefore
     * processed asynchroneously.
     * @param timeStamp The timeStamp of the event
     * @param commandName The HackyStat commandName of the event
     * @param argList The argList of the event
     * @return "true" if the event's data is syntactically valid and "false" otherwises
     */
    public boolean doCommand(Date timeStamp, String commandName, List argList)
    {
        // check parameters
        if(!EventPacket.isSyntacticallyCorrect(timeStamp,commandName,argList))
        {
            return false;
        }
        
        // assert parameters
        assert(EventPacket.isSyntacticallyCorrect(timeStamp,commandName,argList));
        
        // get SendingThread
        SendingThread sendingThread = null;
        
        try {
        	
            sendingThread = SendingThread.getInstance(this.properties.getECGServerAddress(),this.properties.getECGServerPort());
        }
        catch (IllegalHostOrPortException e) {
            
            this.logger.log(Level.SEVERE,"Die Adresse des ECG Servers ist ungültig.\nPrüfen Sie, ob in der Datei \".hackystat/sensor.properties\" in Ihrem Heimatverzeichnis gültige Werte für die Parameter ECG_SERVER_ADDRESS und ECG_SERVER_PORT angegeben sind");

            
        } catch (UnknownHostException e) {
			
            this.logger.log(Level.SEVERE,"Die Adresse des ECG Servers ist ungültig.\nPrüfen Sie, ob in der Datei \".hackystat/sensor.properties\" in Ihrem Heimatverzeichnis gültige Werte für die Parameter ECG_SERVER_ADDRESS und ECG_SERVER_PORT angegeben sind");
	}
      
        // must not be "null"
        assert(sendingThread != null);
        
        // pass EventPacket to SendingThread
        try {
            sendingThread.addEventPacket(new ValidEventPacket(0,timeStamp,commandName,argList));
        }
        catch (IllegalEventParameterException e) {
            
            // As parameters are proofed above, this should never occur.
            
            e.printStackTrace();
        }
        return true;
        
    }
    
    /**
     * 
     * @return
     */
    public SensorProperties getSensorProperties()
    {
        return this.properties;
    }
    
    /**
     * 
     */
    public void send()
    {
        
    }
    
    /**
     * @param str
     */
    public void println(String str)
    {
        
    }
}
