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

import org.electrocodeogram.core.EventPacket;
import org.electrocodeogram.ui.Configurator;

/**
 * @author 7oas7er *  * TODO To change the template for this generated type comment go to * Window - Preferences - Java - Code Style - Code Templates
 */

public class Module extends Observable implements Observer
{

    public final static int SOURCE_MODULE = 0;

    public final static int INTERMEDIATE_MODULE = 1;

    /**
     * 
     * @uml.property name="moduleRegistry"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private static ModuleRegistry moduleRegistry = ModuleRegistry.getInstance();


    public final static int TARGET_MODULE = 2;
    
    private int moduleType = -1;
    
    private static int count = 0;
    
    private int id = 0;
    
    private String name = null;
    
    private HashMap connectedModuleMap = null;
     
    private Collection connectedModules = null;

    /**
     * 
     * @uml.property name="eventBuffer"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    protected EventBuffer eventBuffer = null;

    
    public Module(int moduleType, String name)
    {
      id = ++count;
      
      this.moduleType = moduleType;
      
      this.name = name;
      
      eventBuffer = new EventBuffer();
      
      connectedModuleMap = new HashMap();
      
      connectedModules = connectedModuleMap.values();

      assert(moduleRegistry != null);
      
      moduleRegistry.addModuleInstance(this);
      
      addObserver(Configurator.getInstance(this));
      

    }
    
    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable o, Object arg)
    {
        
    }

    /**
     * 
     * @uml.property name="id"
     */
    public int getId() {
        return id;
    }

    /**
     * 
     * @uml.property name="moduleType"
     */
    public int getModuleType() {
        return moduleType;
    }

    /**
     * 
     * @uml.property name="name"
     */
    public String getName() {
        return name;
    }

    
    /**
     * @param eventPacket
     */
    protected void processEventPacket(EventPacket eventPacket)
    {
        setChanged();
        notifyObservers(new EventPacket(this.getId(),eventPacket.getTimeStamp(),eventPacket.getCommandName(),eventPacket.getArglist()));
        clearChanged();
    }
    
    public int countConnectedModules()
    {
        return connectedModules.size();
    }
    
    public int connectModule(Module module)
    {
        addObserver(module);
        connectedModuleMap.put(new Integer(module.id),module);
       // moduleRegistry.add(module);
        setChanged();
        notifyObservers(module);
        clearChanged();
        return module.id;
    }

    /**
     * 
     * @uml.property name="connectedModules"
     */
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
	            //throw new UnknownModuleIDException();
	        }
	        else
	        {
		        Module module = (Module)connectedModuleMap.get(idObj);
		        
		        assert(module != null);
		        
		        deleteObserver(module);
		        
		        connectedModuleMap.remove(idObj);
		       
		        setChanged();
		       
		        notifyObservers(module);
		        
		        clearChanged();
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
    }
}
