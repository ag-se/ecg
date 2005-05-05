package org.electrocodeogram;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/*
 * Created on 12.03.2005
 *
 */

/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EventPacket implements Serializable
{

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

        
    private int eventSourceId = -1;

    private Date timeStamp = null;
    
    private String hsCommandName = null;
    
    private List argList = null;
    
    
    /**
     * @param i
     * @param timeStamp
     * @param commandName
     * @param argList
     */
    public EventPacket(int id, Date timeStamp, String hsCommandName, List argList)
    {
        
        this.eventSourceId = id;
        
        this.timeStamp = timeStamp;
        
        this.hsCommandName = hsCommandName;
        
        this.argList = argList;    
    }

    /**
     * 
     * @uml.property name="eventSourceId"
     */
    public int getEventSourceId() {
        return eventSourceId;
    }

    /**
     * 
     * @uml.property name="timeStamp"
     */
    public Date getTimeStamp() {
        return timeStamp;
    }

    /**
     * 
     * @uml.property name="commandName"
     */
    public String getHsCommandName() {
        return hsCommandName;
    }

    public String getEcgCommandName()
    {
        for(int i=0;i<argList.size();i++)
        {
            String s = (String) argList.get(i);
            
            if(s.startsWith(ECG_TYPE_PREFIX))
            {
                return s.substring(ECG_TYPE_PREFIX.length());
            }
        }
        
        return null;
    }
    
    public List getArglist()
    {
        return argList;
    }
}

