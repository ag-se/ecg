/*
 * Created on 10.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.module.writer;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.ModuleRegistry;


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
    public EventWriter(ModuleRegistry moduleRegistryPar, String name)
    {
        super(moduleRegistryPar,ModuleType.TARGET_MODULE);
    }
    
//    public void update(Observable o, Object arg)
//    {
//        if (arg instanceof EventPacket)
//        {
//            EventPacket eventPacket = (EventPacket) arg;
//            
//            setChanged();
//            notifyObservers(new EventPacket(this.getId(),eventPacket.getTimeStamp(),eventPacket.getCommandName(),eventPacket.getArglist()));
//            clearChanged();
//            
//            write(eventPacket); 
//        }
//    }

    public void receiveEventPacket(ValidEventPacket eventPacket)
    {
        setChanged();
        try {
            notifyObservers(new ValidEventPacket(this.getId(),eventPacket.getTimeStamp(),eventPacket.getHsCommandName(),eventPacket.getArglist()));
        }
        catch (IllegalEventParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        clearChanged();
        
        write(eventPacket); 
    }
    
    /**
     * @param arg
     */
    public abstract void write(ValidEventPacket eventPacket);

}
