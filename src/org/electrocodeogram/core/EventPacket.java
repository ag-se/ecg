package org.electrocodeogram.core;

import java.util.Date;
import java.util.List;

/*
 * Created on 12.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EventPacket
{

    //private String eventString = null;
    
    private int eventSourceId = -1;

    private Date timeStamp = null;
    
    private String commandName = null;
    
    private List argList = null;
    
    
    /**
     * @param i
     * @param timeStamp
     * @param commandName
     * @param argList
     */
    public EventPacket(int id, Date timeStamp, String commandName, List argList)
    {
        
        this.eventSourceId = id;
        
        this.timeStamp = timeStamp;
        
        this.commandName = commandName;
        
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
    public String getCommandName() {
        return commandName;
    }

    
    public List getArglist()
    {
        return argList;
    }
}

