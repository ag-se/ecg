package org.electrocodeogram.test.server.mockClient;


import org.electrocodeogram.event.TypedValidEventPacket;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.msdt.EventValidator;


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
     * @param validator the EventValidator to test
     * @param eventPacket Is the EventPacket object carrieng the event data
     * @return "true" if the event data is valid and "false" if not.
     */
    public TypedValidEventPacket passEventData(EventValidator validator, ValidEventPacket eventPacket)
    {
        return validator.validate(eventPacket);
    }


   

}
