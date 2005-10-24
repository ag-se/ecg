package org.electrocodeogram.test.server.mockClient;


import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.event.ValidEventPacket.VALIDATION_LEVEL;


/**
 * This is the ECG TestSensor used for automated JUnit tests. It is capable of
 * generating valid and different kinds of invalid EventPackets and it defines
 * methods to send these EventPackets.
 */
public class MockClient
{
    
   private VALIDATION_LEVEL _validityLevel = ValidEventPacket.DEFAULT_VALIDITY_LEVEL;
   
   public void setValidityLevel(VALIDATION_LEVEL validityLevel)
   {
	   this._validityLevel = validityLevel;
   }

    /**
     * Instead of sending an event from the ECG clientside to the ECG server, an event can also
     * be passed directly into the ECG server's receiving component. This method does exactly this for
     * testing purposes.
     * @param validator the EventValidator to test
     * @param eventPacket Is the EventPacket object carrieng the event data
     * @return "true" if the event data is valid and "false" if not.
     * @throws IllegalEventParameterException 
     */
    public void passEventData(WellFormedEventPacket eventPacket) throws IllegalEventParameterException
    {
    	ValidEventPacket.setValidityLevel(this._validityLevel);
    	
        new ValidEventPacket(eventPacket.getSourceId(),eventPacket.getTimeStamp(),eventPacket.getSensorDataType(),eventPacket.getArglist()); 
    }


   

}
