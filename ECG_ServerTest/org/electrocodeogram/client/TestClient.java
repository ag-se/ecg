package org.electrocodeogram.client;

import org.electrocodeogram.event.EventPacket;
import org.hackystat.kernel.shell.SensorShell;

/**
 * This is the ECG TestSensor used for automated JUnit tests. It is capable of
 * generating valid and different kinds of invalid EventPackets and it defines
 * methods to send these EventPackets.
 */
public class TestClient
{
    
   

    /**
     * Instead of sending an event from the ECG clientside to the ECG server, an event can also
     * be passed directly into the ECG server's receiving component. This method does exactly this for
     * testing purposes.
     * @param shellPar Is the SensorShell object that shall receive the event data
     * @param eventPacket Is the EventPacket object carrieng the event data
     * @return "true" if the event data is valid and "false" if not.
     */
    public boolean passEventData(SensorShell shellPar, EventPacket eventPacket)
    {
        return shellPar.doCommand(eventPacket.getTimeStamp(),eventPacket.getHsCommandName(),eventPacket.getArglist());
    }


   

}
