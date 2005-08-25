package org.electrocodeogram.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.TypedValidEventPacket;
import org.electrocodeogram.module.intermediate.IIntermediateModule;
import org.electrocodeogram.module.registry.ModuleClassException;
import org.electrocodeogram.module.registry.ModuleInstanceException;
import org.electrocodeogram.module.source.SourceModule;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.msdt.registry.MicroSensorDataTypeRegistrationException;
import org.electrocodeogram.system.ISystemRoot;
import org.electrocodeogram.system.SystemRoot;
import org.electrocodeogram.ui.messages.GuiWriter;
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
     * This is the module's Logger.
     */
    protected Logger logger;

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

    private ModuleType $moduleType;

    private static int count;

    private int id;

    private String name;

    private HashMap<Integer, Module> receiverModuleMap;

    private HashMap<Integer, Module> senderModuleMap;

    private ArrayList<MicroSensorDataType> providedMsdt;

    private boolean activeFlag;

    private String $moduleClassId;

    /**
     * This creates a new Module of the given module type, module class id ans module name and registers it with the ModuleRegistry.
     * @param moduleType Is the module type
     * @param moduleClassId Is the unique id of this module's class as registered with the ModuleRegistry
     * @param moduleName Is the name to give to this module instance
     */
    public Module(ModuleType moduleType, String moduleClassId, String moduleName)
    {
        this.id = ++count;

        this.name = moduleName;

        this.$moduleClassId = moduleClassId;

        this.$moduleType = moduleType;

        this.logger = Logger.getLogger(this.name);

        this.receiverModuleMap = new HashMap<Integer, Module>();

        this.senderModuleMap = new HashMap<Integer, Module>();

        this.providedMsdt = new ArrayList<MicroSensorDataType>();

        if (this instanceof SourceModule) {

            MicroSensorDataType[] msdts = SystemRoot.getModuleInstance().getModuleMsdtRegistry().getPredefinedMicroSensorDataTypes();

            for (MicroSensorDataType msdt : msdts) {
                try {
                    SystemRoot.getModuleInstance().getModuleMsdtRegistry().requestMsdtRegistration(msdt, this);

                    this.providedMsdt.add(msdt);
                }
                catch (MicroSensorDataTypeRegistrationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                           }

        }

        if (!(this instanceof GuiWriter)) {
            try {

                ModuleDescriptor moduleDescriptor = null;
                try {
                    moduleDescriptor = SystemRoot.getModuleInstance().getModuleModuleRegistry().getModuleDescriptor(this.$moduleClassId);
                }
                catch (ModuleClassException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                MicroSensorDataType[] microSensorDataTypes = moduleDescriptor.getMicroSensorDataTypes();

                if (microSensorDataTypes != null) {

                    for (MicroSensorDataType msdt : microSensorDataTypes) {
                        this.providedMsdt.add(msdt);

                        SystemRoot.getModuleInstance().getModuleMsdtRegistry().requestMsdtRegistration(msdt, this);
                    }
                }

            }
            catch (MicroSensorDataTypeRegistrationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
           
        }

        SystemRoot.getModuleInstance().getModuleModuleRegistry().registerRunningModule(this);

        if (!(this instanceof IGuiWriter)) {

            addObserver(SystemRoot.getSystemInstance().getGui());

            addObserver(SystemRoot.getSystemInstance().getGui().getGuiEventWriter());
        }

        activate();

        initialize();

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
     * Additonally this update method is also invoked by a notification from the
     * system core because of a statechange of the system.
     * This gives the module the chance to react on statechanges of the system. 
     */
    public final void update(Observable object, Object data)
    {
        if ((object instanceof Module)) {
            analyseModuleNotification(object, data);
        }
        else if (object instanceof ISystemRoot) {
            analyseCoreNotification();
        }

    }

    private void analyseModuleNotification(Observable module, Object event)
    {
        assert (module instanceof Module);

        if (event instanceof TypedValidEventPacket) {

            TypedValidEventPacket eventPacket = (TypedValidEventPacket) event;

            receiveEventPacket(eventPacket);
        }
    }

    /**
     * This method is called whenever this module gets a notification of
     * a statechange form the sytem core.
     * It is left to the actual module implementation to react on such an event.
     *
     */
    public abstract void analyseCoreNotification();

    /**
     * This abstract method is to be implemented by all actual modules.
     * Its implementation tells what to do with a received event.
     * @param eventPacket Is the received event
     */
    protected abstract void receiveEventPacket(TypedValidEventPacket eventPacket);

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
    protected final void sendEventPacket(TypedValidEventPacket eventPacket)
    {
        if (this.activeFlag && (eventPacket != null)) {
            setChanged();

            try {
                notifyObservers(new TypedValidEventPacket(this.getId(),
                        eventPacket.getTimeStamp(),
                        eventPacket.getSensorDataType(),
                        eventPacket.getArglist(),
                        eventPacket.getMicroSensorDataType()));
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
     * @throws ModuleInstanceException 
     */
    public void disconnectReceiverModule(Module module) throws ModuleInstanceException
    {
        if (module == null) {
            return;
        }

        if (!this.receiverModuleMap.containsKey(new Integer(module.getId()))) {
            throw new ModuleInstanceException(
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
     * @throws ModulePropertyException 
     */
    public abstract void setProperty(String currentPropertyName, Object propertyValue) throws ModulePropertyException;

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

        if (this instanceof IIntermediateModule) {
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

        String moduleDescription;

        ModuleDescriptor moduleDescriptor = null;
        try {
            moduleDescriptor = SystemRoot.getModuleInstance().getModuleModuleRegistry().getModuleDescriptor(this.$moduleClassId);
        }
        catch (ModuleClassException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        moduleDescription = moduleDescriptor.getDescription();

        if (moduleDescription != null) {
            text += "\nBeschreibung: \t";

            text += moduleDescription;

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
                catch (ModuleInstanceException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        for (MicroSensorDataType msdt : this.providedMsdt) {
            try {
                msdt.removeProvidingModule(this);
            }
            catch (MicroSensorDataTypeRegistrationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            SystemRoot.getModuleInstance().getModuleModuleRegistry().deregisterRunningModule(this.getId());
        }
        catch (ModuleInstanceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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
     * This method returns the MicroSensorDataTypes that are provided by this module.
     * @return The MicroSensorDataTypes that are provided by this module
     */
    public MicroSensorDataType[] getProvidedMicroSensorDataType()
    {
        return this.providedMsdt.toArray(new MicroSensorDataType[0]);
    }

    /**
     * This method returns the unique id of the module's class as
     * registered with the ModuleRegistry.
     * @return The unique id of the module's class
     */
    public String getClassId()
    {
        return this.$moduleClassId;
    }

    /**
     * This method initializes the actual module. It must be implementes by all module
     * subclasses.
     *
     */
    public abstract void initialize();
}
