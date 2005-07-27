package org.electrocodeogram.module;

import java.util.Collection;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.core.Core;
import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.module.intermediate.IIntermediateModule;
import org.electrocodeogram.module.registry.IllegalModuleIDException;
import org.electrocodeogram.module.registry.UnknownModuleIDException;
import org.electrocodeogram.ui.messages.IGuiWriter;

/**
 * This abstract class represents an ECG module. A module is an entity able
 * of receiving events from modules it is connected to and sending events to modules
 * that are connected to it.
 * Additionally a module can be implemented to modify the data of received events
 * and to generate new outgoing events.
 * There are three distinct module types defined:
 * Source modules are not able to be connected to other modules, but other (non-source)
 * modules are able to be connected to it.
 * Intermediate modules are connectable to each other.
 * And target modules are only able to be connected to other modules, but no module
 * can be connected to them.
 * The connection degree, which is the number of modules connected to another module, is
 * not limited by the implementation. 
 * Each module gets an unique integer id during its object creation.
 *
 */
public abstract class Module extends Observable implements Observer
{

    /**
     * This is the Logger instance.
     */
    protected Logger logger = null;

    /**
     * These values are indicating the type of the module.
     */
    public enum ModuleType {
        /**
         * This is a module type value that indicates that the module
         * is a source module.
         * Source modules are not able to be connected to other modules, but other (non-source)
         * modules are able to be connected to it.
         */
        SOURCE_MODULE,
        /**
         * This is a module type value that indicates that the module
         * is a intermediate module.
         * Intermediate modules are connectable to each other.
         */
        INTERMEDIATE_MODULE,
        /**
         * This is a module type value that indicates that the module
         * is a target module.
         * And target modules are only able to be connected to other modules, but no module
         * can be connected to them.
         */
        TARGET_MODULE
    }

    private String moduleDescription = null;
    
    private ModuleType $moduleType;

    private static int count = 0;

    private int id = 0;

    private String name = null;

    private HashMap<Integer, Module> receiverModuleMap = null;

    private HashMap<Integer, Module> senderModuleMap = null;

    /**
     * Indicates if this Module is activated or deactivated.
     */
    protected boolean activeFlag = false;

    /**
     * This creates a new Module of the given module type and registers it with the ModuleRegistry.
     * @param moduleType Is the module type
     */
    public Module(ModuleType moduleType)
    {
        this.id = ++count;

        this.name = this.getClass().getName();

        this.$moduleType = moduleType;

        this.logger = Logger.getLogger(this.name);

        this.receiverModuleMap = new HashMap<Integer, Module>();

        this.senderModuleMap = new HashMap<Integer, Module>();

        Core.getInstance().getModuleRegistry().registerModuleInstance(this);

        if (!(this instanceof IGuiWriter)) {

            addObserver(Core.getInstance().getGui());

            addObserver(Core.getInstance().getGui().getGuiEventWriter());
        }
        
        activate();

    }

    /**
     * This method returns "true" if the module is active and "false" if not.
     * @return "true" if the module is active and "false" if not
     */
    public boolean isActive()
    {
        return this.activeFlag;
    }

    /**
     * This method deactivates the module. The module might be already deactivated.
     *
     */
    public void deactivate()
    {
        if (this.activeFlag == false)
            return;

        this.activeFlag = false;

        notifyModuleChanged(this);
    }

    /**
     * This method activates the module. The module might be already activated.
     *
     */
    public void activate()
    {
        if (this.activeFlag == true)
            return;

        this.activeFlag = true;

        notifyModuleChanged(this);
    }

    /**
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     * As this is the Observer's update method it is called whenever this module is
     * notified of a change of state in an Observable this module is observing.
     * This mechanism is used in the module communication to transport events.
     * When a module is receiving an event its state has changed and it notifies all
     * connected modules and passes the event to them as a parameter.
     */
    public final void update(Observable module, Object event)
    {
        if (!(module instanceof Module)) {
            return;
        }

        analyseNotification(module, event);
    }

    private void analyseNotification(Observable module, Object event)
    {
        assert (module instanceof Module);

        if (event instanceof ValidEventPacket) {

            ValidEventPacket eventPacket = (ValidEventPacket) event;

            receiveEventPacket(eventPacket);
        }
    }

    /**
     * This abstract method is to be implemented by all actual modules.
     * Its implementation tells what to do with a received event.
     * @param eventPacket Is the received event
     */
    protected abstract void receiveEventPacket(ValidEventPacket eventPacket);

    /**
     * This method returns the unique id of this module.
     * @return The unique id of this module
     */
    public int getId()
    {
        return this.id;
    }

    /**
     * This method returns the ModuleType of this module.
     * @return The ModuleType of this module
     */
    public ModuleType getModuleType()
    {
        return this.$moduleType;
    }

    /**
     * This method returns the name of this module.
     * @return The name of this module
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * This method is inherited to all extending modules. It sends the given event to
     * all connected modules. The sourceId attribute of the event is changed to the id
     * of this the sending module.
     * @param eventPacket Is the event to send
     */
    protected final void sendEventPacket(ValidEventPacket eventPacket)
    {
        if (this.activeFlag && (eventPacket != null)) {
            setChanged();

            try {
                notifyObservers(new ValidEventPacket(this.getId(),
                        eventPacket.getTimeStamp(),
                        eventPacket.getSensorDataType(),
                        eventPacket.getArglist()));
            }
            catch (IllegalEventParameterException e) {

                // As only the id of a ValidEventPackets is changed, it has to stay valid

                clearChanged();

                e.printStackTrace();

                this.logger.log(Level.SEVERE, "An unexpected exception has occurred. Please report this at www.electrocodeogram.org");

            }
            clearChanged();
        }
    }

 
    /**
     * This method returns the number of connected modules.
     * @return The number of connected modules
     */
    public int getReceivingModuleCount()
    {
        return this.receiverModuleMap.size();
    }

    /**
     * This method is used to connect a given module to this module.
     * @param module Is the module that should be connected to this module.
     * @return The id of the connected module
     * @throws ModuleConnectionException If the given module could not be connected to this module.
     * This happens if this module is a target module or if the given module is already connected to this module.
     */
    public int connectReceiverModule(Module module) throws ModuleConnectionException
    {

        if (this.$moduleType == ModuleType.TARGET_MODULE) {
            throw new ModuleConnectionException(
                    "You can not connect another module to this module.");
        }
        else if (this.receiverModuleMap.containsKey(new Integer(module.getId()))) {
            throw new ModuleConnectionException(
                    "These mdoules are connected already.");
        }
        else {

            addObserver(module);

            this.receiverModuleMap.put(new Integer(module.id), module);

            module.addParentModule(this);

            notifyModuleChanged(this);

            return module.id;

        }

    }

    private void addParentModule(Module module)
    {
        this.senderModuleMap.put(new Integer(module.getId()), module);
    }

    private void notifyModuleChanged(Module module)
    {
        setChanged();
        notifyObservers(module);
        clearChanged();
    }

    /**
     * This method returns an Array of all modules that are connected to this module.
     * @return An Array of all modules that are connected to this module
     */
    public Module[] getReceivingModules()
    {
        Collection<Module> receivingModules = this.receiverModuleMap.values();

        return receivingModules.toArray(new Module[0]);
    }

    private Module[] getSendingModules()
    {

        Collection<Module> sendingModules = this.senderModuleMap.values();

        return sendingModules.toArray(new Module[0]);
    }

    /**
     * This method disconnects a connected module.
     * @param module The module to disconnect
     * @throws UnknownModuleIDException If the given module is not connected to this module
     */
    public void disconnectReceiverModule(Module module) throws UnknownModuleIDException
    {
        if(module == null)
        {
            return;
        }
        
        if (!this.receiverModuleMap.containsKey(new Integer(module.getId()))) {
            throw new UnknownModuleIDException(
                    "The given module id " + this.id + " is unknown.");
        }

        deleteObserver(module);

        this.receiverModuleMap.remove(new Integer(module.getId()));

        notifyModuleChanged(this);

    }

    
    /**
     * @see java.lang.Object#toString()
     * This implementation of the toString method returns the module-name.
     */
    @Override
    public String toString()
    {
        return this.name;

    }


    /**
     * @param currentPropertyName
     * @param propertyValue
     */
    public abstract void setProperty(String currentPropertyName, Object propertyValue);

    /**
     * This method collects detailed information about the module and returns them as a String.
     * @return A String of detailed information about the module
     */
    public String getDetails()
    {
        String text = "Name: \t" + getName() + "\nID: \t " + getId() + "\nTyp: \t";

        ModuleType type = getModuleType();
        
        switch (type)
        {
        case SOURCE_MODULE:
            text += "Quellmodul";
            break;

        case INTERMEDIATE_MODULE:
            text += "Zwischenmodul";
            break;

        case TARGET_MODULE:
            text += "Zielmodul";
            break;
        }

        if (this instanceof IIntermediateModule) 
        {
            IIntermediateModule eventProcessor = (IIntermediateModule) this;

            

            text += "\nModus: \t";

            switch (eventProcessor.getProcessingMode())
            {
            case ANNOTATOR:
                text += "Annotation";
                break;

            case FILTER:
                text += "Filterung";
                break;

            }
        }

        if (this.moduleDescription != null) {
            text += "\nBeschreibung: \t";

            text += this.moduleDescription;

        }

        return text;

    }

    /**
     * This method is called hen the module is to be removed from the ECG Lab.
     *
     */
    public void remove()
    {
        if (this.senderModuleMap.size() != 0) {

            Module[] parentModules = getSendingModules();

            for (int i = 0; i < parentModules.length; i++) {
                
                Module module = parentModules[i];

                try {
                    module.disconnectReceiverModule(this);
                }
                catch (UnknownModuleIDException e) {
                    
                    e.printStackTrace();

                    this.logger.log(Level.SEVERE, "An unexpected exception has occured. Please report this at www.electrocodeogram.org");
                }
            }
        }
        
        try {
        	Core.getInstance().getModuleRegistry().deregisterModuleInstance(this.getId());
        }
        catch (UnknownModuleIDException e) {
            
            e.printStackTrace();
            
            this.logger.log(Level.SEVERE, "An unexpected exception has occured. Please report this at www.electrocodeogram.org");

        }
        catch (IllegalModuleIDException e) {

            e.printStackTrace();

            this.logger.log(Level.SEVERE, "An unexpected exception has occured. Please report this at www.electrocodeogram.org");

        }
       
    }

    /**
     * This method sets the name of this module to the given name.
     * @param moduleName the new name for the module. 
     */
    public void setName(String moduleName)
    {
        this.name = moduleName;

    }
    
    
    /**
     * This checks if the module is of the given ModuleType
     * @param moduleType
     * @return "true" If the module is of the given ModuleType and "false" if not
     */
    public boolean isModuleType(ModuleType moduleType)
    {
        if (this.$moduleType == moduleType) {
            return true;
        }
       
        return false;
        
    }

    /**
     * Sets the description of the module to the given string.
     * @param description Is the description string of the module
     */
    public void setDescription(String description)
    {
        this.moduleDescription = description;
        
    }

}
