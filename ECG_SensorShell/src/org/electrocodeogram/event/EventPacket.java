package org.electrocodeogram.event;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * An EventPacket is a for a recorded event.
 */
public class EventPacket implements Serializable
{
    private static final long serialVersionUID = 2353171166739768704L;

    private int sourceId = -1;

    private Date $timeStamp = null;

    private String $sensorDataType = null;

    private List $argList = null;
    

    
   

    /**
     * This creates a new EventPacket object
     * @param id The module source ID identifies where the EventPacket comes from
     * @param timeStamp The timeStamp tells when the event was recorded
     * @param sensorDataType The HackyStat SensorDataType of the event
     * @param argList The argList of parameters containing all the relevant event data
     */
    public EventPacket(int id, Date timeStamp, String sensorDataType, List argList)
    {

        this.sourceId = id;

        if (timeStamp != null) {
            this.$timeStamp = new Date(timeStamp.getTime());
        }

        this.$sensorDataType = sensorDataType;

        this.$argList = argList;

    }

    /**
     * This method returns the ID that identifies the source module of this EventPacket object.
     * @return The ID of the source module
     */
    public int getSourceId()
    {
        return this.sourceId;
    }

    /**
     * This method returns the timestamp of the EventPacket.
     * @return The timestamp as a Date object
     */
    public Date getTimeStamp()
    {
        if (this.$timeStamp != null) {
            return new Date(this.$timeStamp.getTime());
        }

        return null;
    }

    /**
     * This method returns the HackyStat SensorDataType of the event.
     * @return The HackyStat SensorDataType
     */
    public String getSensorDataType()
    {
        return this.$sensorDataType;
    }

    /**
     * This method returns the argList of the EventPacket
     * @return The argList as a List
     */
    public List getArglist()
    {
        return this.$argList;
    }

    /**
     * This method checks the syntactically correctness of an EventPacket.
     * @param timeStamp The timeStamp tells when the event was recorded
     * @param commandName The HackyStat The HackyStat SensorDataType of the event
     * @param argList The argList of parameters containing all the relevant event data
     * @return "true" if the EventPacket is syntactically correct and "false" if not
     */
    public static boolean isSyntacticallyCorrect(Date timeStamp, String commandName, List argList)
    {
        if (timeStamp == null || commandName == null || argList == null || argList.isEmpty() || !(argList.get(0) instanceof String)) {
            return false;
        }

        return true;

    }

    /**
     * This method returns a String representation of the EventPacket.
     * @return A String representation of the EventPacket
     * 
     */
    @Override
    public String toString()
    {
        String string = "";

        SimpleDateFormat dateFormat = new SimpleDateFormat(ValidEventPacket.DATE_FORMAT_PATTERN);
        
        String dateString = dateFormat.format(this.getTimeStamp());
        
        string += dateString + "#";
        
        string += this.getSensorDataType() + "#";

        StringBuffer stringBuffer = new StringBuffer();

        for (int i = 0; i < this.getArglist().size(); i++) {
            stringBuffer.append(";");

            stringBuffer.append((String) this.getArglist().get(i));
        }

        return string + stringBuffer.toString();
    }

    /**
     * This method compares a given EventPacket with this EventPacket.
     * @param packet Is the EventPacket to compare
     * @return "true" if the two EventPackets have identical timeStamps, SensorDataTypes and argLists; "false" otherwise
     */
    @Override
    public boolean equals(Object packet)
    {
        boolean equals = false;

        if(!(packet instanceof EventPacket))
        {
            return false;
        }
        
        EventPacket eventPacket = (EventPacket) packet;
        
        if (this.getTimeStamp().equals(eventPacket.getTimeStamp()) && this.getSensorDataType().equals(eventPacket.getSensorDataType())) {
            if (this.getArglist().size() == eventPacket.getArglist().size()) {
                int size = eventPacket.getArglist().size();

                for (int i = 0; i < size; i++) {
                    String testString = (String) this.getArglist().get(i);

                    String receivedString = (String) eventPacket.getArglist().get(i);

                    if (testString.equals(receivedString)) {

                        equals = true;
                    }
                    else {
                        equals = false;
                    }
                }
            }
            
        }
        
        return equals;
        
    }
}
