/*
 * Created on 10.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.module.writer;

import java.util.Observable;

import org.electrocodeogram.core.EventPacket;
import org.electrocodeogram.module.Module;


/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class EventWriter extends Module
{

    /**
     * @param name
     */
    public EventWriter(String name)
    {
        super(Module.TARGET_MODULE,name);
        // TODO Auto-generated constructor stub
    }
    
    public void update(Observable o, Object arg)
    {
        if (arg instanceof EventPacket)
        {
            EventPacket eventPacket = (EventPacket) arg;
            
            setChanged();
            notifyObservers(new EventPacket(this.getId(),eventPacket.getTimeStamp(),eventPacket.getCommandName(),eventPacket.getArglist()));
            clearChanged();
            
            write(eventPacket); 
        }
    }

    /**
     * @param arg
     */
    public abstract void write(EventPacket eventPacket);

}
