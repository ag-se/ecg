/*
 * Created on 08.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.module;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.*;
import java.util.logging.Level;

import org.electrocodeogram.EventPacket;
import org.electrocodeogram.module.annotator.EventProcessor;
import org.electrocodeogram.ui.Configurator;
import org.electrocodeogram.ui.GuiEventWriter;

/**
 * @author 7oas7er *  */

public abstract class Module extends Observable implements Observer
{

    protected Logger logger = null;
    
    public final static int SOURCE_MODULE = 0;

    public final static int INTERMEDIATE_MODULE = 1;

    private static ModuleRegistry moduleRegistry = ModuleRegistry.getInstance();

    public final static int TARGET_MODULE = 2;
    
    private int moduleType = -1;
    
    private static int count = 0;
    
    private int id = 0;
    
    private String name = null;
    
    private HashMap connectedModuleMap = null;
     
    private Collection connectedModules = null;

    protected EventBuffer eventBuffer = null;

    protected boolean runningFlag = false;
    
    // TODO : a module may have to know who it is connected to
    
    public Module(int moduleType, String name)
    {
      id = ++count;
      
      this.moduleType = moduleType;
      
      this.name = name;
      
      this.logger = Logger.getLogger(this.name);
      
      eventBuffer = new EventBuffer();
      
      connectedModuleMap = new HashMap();
      
      connectedModules = connectedModuleMap.values();

      assert(moduleRegistry != null);
      
      moduleRegistry.addModuleInstance(this);
      
      if (!(this instanceof GuiEventWriter))
      {
          addObserver(Configurator.getInstance(this));
          
          addObserver(GuiEventWriter.getInstance());
      }
      

    }
    
    public boolean isRunning()
    {
        return runningFlag;
    }
    
    public void stop()
    {
        this.runningFlag = false;
        
        notifyModuleChanged(this);
    }
    
    public void start()
    {
        this.runningFlag = true;
        
        notifyModuleChanged(this);
    }
    
    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public final void update(Observable o, Object arg)
    {
        analyseNotification(o,arg);
    }

    private void analyseNotification(Observable o, Object arg)
    {
        if(arg instanceof EventPacket)
        {
            EventPacket eventPacket = (EventPacket) arg;
            
            receiveEventPacket(eventPacket);
        }
    }
    
    public abstract void receiveEventPacket(EventPacket eventPacket);
    
    public int getId() {
        return id;
    }

    public int getModuleType() {
        return moduleType;
    }

   public String getName() {
        return name;
    }

    
    protected void sendEventPacket(EventPacket eventPacket)
    {
        if(runningFlag && (eventPacket != null))
        {
	        setChanged();
	        notifyObservers(new EventPacket(this.getId(),eventPacket.getTimeStamp(),eventPacket.getCommandName(),eventPacket.getArglist()));
	        clearChanged();
	    }
    }

    /**
     * @param e
     * @param string
     * @param string2
     * @return
     */
    protected boolean isPacketMatching(EventPacket e, String commmandName, String activityType)
    {
        assert(e != null);
        
        logger.log(Level.INFO,"isPacketMatching?");
        
        if(e.getCommandName().equals(commmandName) && e.getArglist().get(1).equals(activityType))
        {
            logger.log(Level.INFO,"Yes");
            return true;
        }
        else
        {
            logger.log(Level.INFO,"No");
            return false;
        }
    }
    
    public int countConnectedModules()
    {
        return connectedModules.size();
    }
    
    public int connectModule(Module module)
    {
        if (moduleType != Module.TARGET_MODULE)
        {
	        addObserver(module);
	        connectedModuleMap.put(new Integer(module.id),module);
	        notifyModuleChanged(module);
	        return module.id;
	    }
        else
        {
            throw new ModuleConnectionException("An diese Modul können Sie keine weiteren Module anhängen");
        }
    }

    /**
     * @param module
     */
    private void notifyModuleChanged(Module module)
    {
        setChanged();
        notifyObservers(module);
        clearChanged();
    }

    public Object[] getConnectedModules() {
        return connectedModules.toArray();
    }

    
    public void disconnectModule(int id) throws UnknownModuleIDException
    {

        if (! (id > 0))
        {
            throw new IllegalModuleIDException("A module id must be a positive integer.");
        }
        else
        {
	        Integer idObj = new Integer(id);
	        
	        if(!connectedModuleMap.containsKey(idObj))
	        {
	            throw new UnknownModuleIDException("The given module id " + id + " is unknown.");
	        }
	        else
	        {
		        Module module = (Module)connectedModuleMap.get(idObj);
		        
		        assert(module != null);
		        
		        deleteObserver(module);
		        
		        connectedModuleMap.remove(idObj);
		       
		        notifyModuleChanged(module);
	        }
        }
    }


    public String toString()
    {
        return name;
        
    }
    
    protected class EventBuffer
    {
        private final int bufferSize = 10;
        
        private LinkedList bufferList = new LinkedList();

        public void append(EventPacket eventPacket)
        {
            if(bufferList.size() > bufferSize)
            {
                bufferList.removeFirst();
            }
            
            bufferList.add(eventPacket);
        }
        
        public EventPacket getFirst()
        {
            return (EventPacket) bufferList.getFirst();
        }
        
        public EventPacket getLast()
        {
            return (EventPacket) bufferList.getLast();
        }
        
    }

    /**
     * @param currentPropertyName
     * @param propertyValue
     */
    public abstract void setProperty(String currentPropertyName, Object propertyValue);

    /**
     * @return
     */
    public String getDetails()
    {
        String text = "Name: \t" + getName() + "\nID: \t " + getId() + "\nTyp: \t";
        
        int type = getModuleType();
        switch(type)
        {
            case Module.SOURCE_MODULE:
                text += "Quellmodul";
            break;
            
            case Module.INTERMEDIATE_MODULE:
                text += "Zwischenmodul";
            break;
            
            case Module.TARGET_MODULE:
                text += "Zielmodul";
            break;
        }
        
        if(this instanceof EventProcessor)
        {
            EventProcessor eventProcessor = (EventProcessor) this;
            
            int mode = eventProcessor.getProcessorMode();
            
            text += "\nModus: \t";
            
            switch(mode)
            {
            	case EventProcessor.ANNOTATOR:
            	    text += "Annotation";
            	break;
            	
            	case EventProcessor.FILTER:
            	    text += "Filterung";
            	break;
            	    
            }
        }
        
       
        return text;
    
    }
    
}
