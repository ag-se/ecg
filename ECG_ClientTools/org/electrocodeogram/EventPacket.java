package org.electrocodeogram;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * An EventPacket is a representation for a MicroProcessEvent (MPE).
 * It contains the events data and is conforming to the syntactical
 * rules for MPE.
 */
public class EventPacket implements Serializable
{
    private static final long serialVersionUID = 2353171166739768704L;

    // begin: this will go into the MPE managment soon
    public static final String HS_COMMAND_PREFIX = "HS_COMMAND:";

    public static final String HS_TYPE_PREFIX = "HS_ACTIVITY_TYPE:";

    public static final String ECG_TYPE_PREFIX = "ECG_TYPE:";

    public static final String ECG_TYPE_WINDOW_DEACTIVATED = "Window deactivated";

    public static final String ECG_TYPE_WINDOW_ACTIVATED = "Window activated";

    public static final String ECG_TYPE_WINDOW_OPENED = "Window opened";

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

    private int sourceId = -1;

    private Date timeStamp = null;

    private String hsCommandName = null;

    private List argList = null;

    /**
     * This creates a new EventPacket object
     * @param id The module source ID identifies where the EventPacket comes from
     * @param timeStampPar The timeStamp tells when the event was recorded
     * @param hsCommandNamePar The HackyStat comandName param the event is embedded in
     * @param argListPar The argList of parameters containing all the relevant event data
     * @throws IllegalEventParameterException If the given parameters are not conforming to the syntactical MPE rules
     */
    public EventPacket(int id, Date timeStampPar, String hsCommandNamePar, List argListPar) throws IllegalEventParameterException
    {
        if (!isSyntacticallyCorrect(timeStampPar, hsCommandNamePar, argListPar)) {
            throw new IllegalEventParameterException();
        }

        if (id < 0) {
            throw new IllegalEventParameterException();
        }

        this.sourceId = id;

        this.timeStamp = timeStampPar;

        this.hsCommandName = hsCommandNamePar;

        this.argList = argListPar;

        assert (isSyntacticallyCorrect(this.timeStamp, this.hsCommandName, this.argList));

        assert (this.sourceId >= 0);

    }

    /**
     * This methis returns th ID that identifies the source module of this EventPacket object.
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
     * This method returns the HackyStat commandName the event data is carried in
     * @return The HackyStat commandName
     */
    public String getHsCommandName()
    {
        return this.hsCommandName;
    }

    /**
     * This method returns the ECG commandName wich is contained in the argList of this EventObject.
     * @return The ECG commandName
     */
    public String getEcgCommandName()
    {
        for (int i = 0; i < this.argList.size(); i++) {
            String s = (String) this.argList.get(i);

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
     * This Classmethod proof the syntactcally corectness of an EventPacket.
     * @param timeStamp The timeStamp tells when the event was recorded
     * @param commandNamePar The HackyStat comandName param the event is embedded in
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

}
