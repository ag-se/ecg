/*
 * Created on 02.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.hackystat.kernel.shell;
import java.util.Date;
import java.util.List;

import org.electrocodeogram.EventPacket;
import org.electrocodeogram.IllegalEventParameterException;
import org.electrocodeogram.IllegalHostOrPortException;
import org.electrocodeogram.SendingThread;
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
    
    /*
     * This creates a SensorShelll instance.
     */
    public SensorShell(SensorProperties properties, boolean b, String string)
    {
        // assert parameters
        assert(properties != null);
        
        this.properties = properties;
    }

    /*
     * This method is called by the ECG sensors whenever they record an event. The data
     * of the event is then passed over to the singleton SendingThread and therefore
     * processed asynchroneously.
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
            sendingThread = SendingThread.getInstance(properties.getECGServerAddress(),properties.getECGServerPort());
        }
        catch (IllegalHostOrPortException e) {
            
        }
      
        // must not be "null"
        assert(sendingThread != null);
        
        // pass EventPacket to SendingThread
        try {
            sendingThread.addEventPacket(new EventPacket(0,timeStamp,commandName,argList));
        }
        catch (IllegalEventParameterException e) {
            
            // As parameters are proofed above, this should never occur.
            
            e.printStackTrace();
        }
        return true;
        
    }
    
    
    
}
