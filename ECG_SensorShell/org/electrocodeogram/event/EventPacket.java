package org.electrocodeogram.event;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * An EventPacket is a for a recorded event.
 */
public class EventPacket implements Serializable
{
    private static final long serialVersionUID = 2353171166739768704L;

    protected int sourceId = -1;

    protected Date timeStamp = null;

    protected String sensorDataType = null;

    protected List argList = null;

    public static final String HS_COMMAND_PREFIX = "HS_COMMAND:";

    public static final String HS_TYPE_PREFIX = "HS_ACTIVITY_TYPE:";

    public static final String ECG_TYPE_PREFIX = "mSDT:";

    //  begin: this will go into the MPE managment soon

    public static final String ECG_TYPE_WINDOW_DEACTIVATED = "Window deactivated";

    public static final String ECG_TYPE_WINDOW_ACTIVATED = "Window activated";

    public static final String ECG_TYPE_WINDOW_OPENED = "Window opened";

    public static final String ECG_TYPE_WINDOW_CLOSED = "Window closed";

    public static final String ECG_TYPE_EDITOR_ACTIVATED = "Editor activated";

    public static final String ECG_TYPE_EDITOR_DEACTIVATED = "Editor deactivated";

    public static final String ECG_TYPE_PART_ACTIVATED = "Part activated";

    public static final String ECG_TYPE_PART_DEACTIVATED = "Part deactivated";

    public static final String ECG_TYPE_EDITOR_OPENED = "Editor opened";

    public static final String ECG_TYPE_EDITOR_CLOSED = "Editor closed";

    public static final String ECG_TYPE_PART_OPENED = "Part opened";

    public static final String ECG_TYPE_PART_CLOSED = "Part closed";

    public static final String ECG_TYPE_CODECHANGE = "Codechange";

    public static final String ECG_TYPE_OPEN_FILE = "File opened";

    public static final String ECG_TYPE_BREAKPOINT_SET = "Breakpoint set";

    public static final String ECG_TYPE_BREAKPOINT_UNSET = "Breakpoint unset";

    public static final String ECG_TYPE_RUN = "Run";

    public static final String ECG_TYPE_DEBUG = "Debug";

    // end

    /**
     * This creates a new EventPacket object
     * @param id The module source ID identifies where the EventPacket comes from
     * @param timeStampPar The timeStamp tells when the event was recorded
     * @param sensorDataTypePar The HackyStat SensorDataType of the event
     * @param argListPar The argList of parameters containing all the relevant event data
     */
    public EventPacket(int id, Date timeStampPar, String sensorDataTypePar, List argListPar)
    {

        this.sourceId = id;

        this.timeStamp = timeStampPar;

        this.sensorDataType = sensorDataTypePar;

        this.argList = argListPar;

    }

    /**
     * This methis returns the ID that identifies the source module of this EventPacket object.
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
        return this.timeStamp;
    }

    /**
     * This method returns the HackyStat SensorDataType of the event.
     * @return The HackyStat SensorDataType
     */
    public String getSensorDataType()
    {
        return this.sensorDataType;
    }

    /**
     * This method returns the ECG MicroSensorDataType which is contained in the argList of this EventPacket.
     * @return The ECG MicroSensorDataType
     */
    public String getMicroSensorDataType()
    {
        for (int i = 0; i < this.argList.size(); i++) {
            String s = (String) this.argList.get(i);

            if (s == null) {
                return null;
            }

            if (s.startsWith(ECG_TYPE_PREFIX)) {
                return s.substring(ECG_TYPE_PREFIX.length());
            }
        }

        return null;
    }

    /**
     * This method returns the argList of the EventPacket
     * @return The argList as a List
     */
    public List getArglist()
    {
        return this.argList;
    }

    /**
     * This method checks the syntactically correctness of an EventPacket.
     * @param timeStamp The timeStamp tells when the event was recorded
     * @param commandNamePar The HackyStat The HackyStat SensorDataType of the event
     * @param argList The argList of parameters containing all the relevant event data
     * @return "true" if the eventPacket is syntactically correct nad "false" if not
     */
    public static boolean isSyntacticallyCorrect(Date timeStamp, String commandNamePar, List argList)
    {
        if (timeStamp == null || commandNamePar == null || argList == null || argList.isEmpty() || !(argList.get(0) instanceof String)) {
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
        String string = new String();

        string += "SourceID: " + this.getSourceId() + ", SDT: " + this.getSensorDataType() + ", MSDT: " + this.getMicroSensorDataType();

        for (int i = 0; i < this.getArglist().size(); i++) {
            string += ", ";
            string += (String) this.getArglist().get(i);
        }

        return string;
    }
}
