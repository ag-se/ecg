package org.electrocodeogram.sensor;

import java.util.Date;

import org.electrocodeogram.event.EventPacket;
import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.kernel.shell.SensorShell;

/**
 * This is the ECG TestSensor used for automated JUnit tests. It is capable of
 * generating valid and different kinds of invalid EventPackets and it defines
 * methods to send these EventPackets.
 */
public class TestSensor
{

    private SensorShell shell = null;

    private SensorProperties properties = null;
    
    private Date sendingTime = null;

    /**
     * Creates a TestSensor instance and initializes it with a SensorShell.
     *
     */
    public TestSensor()
    {
        this.properties = new SensorProperties("TestSensor");

        this.shell = new SensorShell(this.properties, false, "TestSensor");
    }

   
    
    /**
     * This method passes a single given EventPacket to the ECG SensorShell.
     * 
     * @param eventPacket
     *            The EventPacket to pass
     * @return The result as given by the ECG Sensorshell. "true" means the
     *         EventPacket is syntactically valid and accepted. "false" means
     *         the eventPacket is syntactically invalid and not acccepted.
     */
    public boolean sendEvent(EventPacket eventPacket)
    {
        this.sendingTime = new Date();
        
        return this.shell.doCommand(eventPacket.getTimeStamp(), eventPacket.getHsCommandName(), eventPacket.getArglist());
    }

    public Date getSendingTime()
    {
        Date toReturn = this.sendingTime;
        
        this.sendingTime = null;
        
        return toReturn;
    }

}
