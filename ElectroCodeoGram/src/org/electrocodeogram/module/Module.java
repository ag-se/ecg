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

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
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
    
    private HashMap childModuleMap = null;
     
    private Collection childModules = null;
    
    private HashMap parentModuleMap = null;
    
   private Collection parentModules = null;

    protected EventBuffer eventBuffer = null;

    protected boolean runningFlag = false;
   
    public Module(int moduleType)
    {
      id = ++count;
      
      this.name = this.getClass().getName();
      
      this.moduleType = moduleType;
      
      this.logger = Logger.getLogger(this.name);
      
      eventBuffer = new EventBuffer();
      
      childModuleMap = new HashMap();
      
      childModules = childModuleMap.values();
      
      parentModuleMap = new HashMap();
      
      parentModules = childModuleMap.values();
      
      if (!(this instanceof GuiEventWriter))
      {
          addObserver(Configurator.getInstance(this));
          
          addObserver(GuiEventWriter.getInstance());
      }
      
      start();

    }
    
    public boolean isRunning()
    {
        return runningFlag;
    }
    
    public void stop()
    {
        if(runningFlag == false) return;
        
        this.runningFlag = false;
        
        notifyModuleChanged(this);
    }
    
    public void start()
    {
        if(runningFlag == true) return;
        
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
        if(arg instanceof ValidEventPacket)
        {
            ValidEventPacket eventPacket = (ValidEventPacket) arg;
            
            receiveEventPacket(eventPacket);
        }
    }
    
    public abstract void receiveEventPacket(ValidEventPacket eventPacket);
    
    public int getId() {
        return id;
    }

    public int getModuleType() {
        return moduleType;
    }

   public String getName() {
        return name;
    }

    
    protected void sendEventPacket(ValidEventPacket eventPacket)
    {
        if(runningFlag && (eventPacket != null))
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
	    }
    }

    /**
     * @param e
     * @param string
     * @param string2
     * @return
     */
    protected boolean isPacketMatching(ValidEventPacket e, String hsCommmandName, String ecgCommandName)
    {
        assert(e != null);
        
        logger.log(Level.INFO,"isPacketMatching?");
        
        if(e.getHsCommandName().equals(hsCommmandName) && e.getEcgCommandName().equals(ecgCommandName))
        {
            // TODO : handle NullPointerException by no ECGCommand
            logger.log(Level.INFO,"Yes");
            return true;
        }
        else
        {
            logger.log(Level.INFO,"No");
            return false;
        }
    }
    
    public int countChildModules()
    {
        return childModules.size();
    }
    
    /**
     * @param module
     * @return
     * @throws ModuleConnectionException
     */
    public int connectChildModule(Module module) throws ModuleConnectionException
    {
        
        if (moduleType == Module.TARGET_MODULE)
        {
            throw new ModuleConnectionException("An diese Modul können Sie keine weiteren Module anhängen");
        }
        else if(childModuleMap.containsKey(new Integer(module.getId())))
        {
            throw new ModuleConnectionException("Diese Module sind bereits verbunden.");
        }
        else
        {
	        
        	addObserver(module);
	        
	        childModuleMap.put(new Integer(module.id),module);
	        
	        module.addParentModule(this);
	        
	        notifyModuleChanged(this);
	        
	        return module.id;
	        
        }
        
        
    }

    /**
     * @param module
     */
    private void addParentModule(Module module)
    {
        this.parentModuleMap.put(new Integer(module.getId()),module);
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

    public Object[] getChildModules() {
        
        childModules = childModuleMap.values();
        
        return childModules.toArray();
    }

    private Object[] getParentModules() {
        
        parentModules = parentModuleMap.values();
        
        return parentModules.toArray();
    }
    
    public void disconnectChildModule(Module module) throws UnknownModuleIDException
    {
        	if(!childModuleMap.containsKey(new Integer(module.getId())))
	        {
	            throw new UnknownModuleIDException("The given module id " + id + " is unknown.");
	        }
	        else
	        {
		        assert(module != null);
		        
		        deleteObserver(module);
		        
		        childModuleMap.remove(new Integer(module.getId()));
		       
		        notifyModuleChanged(this);
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

        public void append(ValidEventPacket eventPacket)
        {
            if(bufferList.size() > bufferSize)
            {
                bufferList.removeFirst();
            }
            
            bufferList.add(eventPacket);
        }
        
        public ValidEventPacket getFirst()
        {
            return (ValidEventPacket) bufferList.getFirst();
        }
        
        public ValidEventPacket getLast()
        {
            return (ValidEventPacket) bufferList.getLast();
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
        
        String moduleDescription = ModuleRegistry.getInstance().getModuleDescription(this.getName());
        
        if(moduleDescription != null)
        {
            text += "\nBeschreibung: \t";
            
            text += moduleDescription;
            
        }
        
        return text;
    
    }

    /**
     * @throws UnknownModuleIDException
     * 
     */
    public void remove() throws UnknownModuleIDException
    {
        
        if(parentModuleMap.size() != 0)
        {
        
        Object[] parentModules = getParentModules();
        
        for(int i=0;i<parentModules.length;i++)
        {
            Module module = (Module) parentModules[i];
            
            module.disconnectChildModule(this);
        }
        }
    }

    /**
     * @param moduleName
     */
    public void setName(String moduleName)
    {
        this.name = moduleName;
        
    }

   
    
}
