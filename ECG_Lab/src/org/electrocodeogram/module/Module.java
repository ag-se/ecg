/*
 * Class: Module Version: 1.0 Date: 18.10.2005 By:
 * Frank@Schlesinger.com
 */

package org.electrocodeogram.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.event.ValidEventPacket.DELIVERY_STATE;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.module.intermediate.IIntermediateModule;
import org.electrocodeogram.module.registry.ModuleClassException;
import org.electrocodeogram.module.registry.ModuleInstanceException;
import org.electrocodeogram.module.source.SourceModule;
import org.electrocodeogram.module.source.SourceModuleException;
import org.electrocodeogram.module.target.ITargetModule;
import org.electrocodeogram.module.target.TargetModule;
import org.electrocodeogram.module.target.TargetModuleException;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.msdt.registry.MicroSensorDataTypeRegistrationException;
import org.electrocodeogram.system.ISystem;
import org.electrocodeogram.system.ModuleSystem;
import org.electrocodeogram.xml.PropertyException;

/**
 * This abstract class represents an <em> ElectroCodeoGram Module</em>.
 * A module is an object able to receive events from other modules and
 * to send events to other modules. For that purpose modules are
 * connected to each other. Multiple modules are allowed to be
 * connected to another module. In that case the multiple moduleas are
 * receiving events from the one module. The multiple modules are
 * called receiver modules and the one module is called the sender
 * module. The event sending is realized with use of the
 * <em>Observer</em> design pattern, where every module is an
 * <em>Observer</em> and an <em>Observable</em>. In fact each
 * module contains nested classed that are acting as
 * <em>Observers</em> and <em>Observables</em>. These are
 * <ul>
 * <li> {@link Module.EventSender} in an <em>Observable</em> role
 * and
 * <li> {@link Module.EventReceiver} in an <em>Observer</em> role.
 * </ul>
 * When the <em>EventSender</em> of the sender module has a new
 * event, it notifies all registered <em>EventReceivers</em> of
 * other modules, passing the new event with the notification.
 * Additionally modules are observing the system state for relevant
 * state changes and modules are telling the system about their state
 * on state changes. This is realized with another two nested classes:
 * <ul>
 * <li> {@link Module.SystemObserver} in is notified about system
 * state changes and
 * <li> {@link Module.SystemNotificator} tells the system about
 * changes in this module.
 * </ul>
 * The abstract class <code>Module</code> must be implemented. This
 * is done by implementing one of thir abstract subclasses.
 * <ul>
 * <li>{@link org.electrocodeogram.module.source.SourceModule} must
 * be implemented for modules to read in events from locations
 * externalk to the <em>ECG Lab</em>.
 * <li>{@link org.electrocodeogram.module.intermediate.IntermediateModule}
 * must be implemented for modules to analyse incoming events and
 * <li>{@link org.electrocodeogram.module.target.TargetModule} must
 * be implemented for modules to write events into locations external
 * to the <em>ECG Lab</em>.
 * </ul>
 * Each module is created from classes defined in a
 * <em>ModulePacket</em> in the file system. From the module's
 * <em>PropertyFile</em> inside the <em>ModulePacket</em>, each
 * module is given its defined <em>MicroSensorDataTypes</em>, its
 * <em>ModuleProperties</em> and the uniquw <code>String</code> id
 * of the <em>ModulePacket</em> itself. During its creation every
 * module instance also gets a unique int id.
 */
public abstract class Module {

    /**
     * This is this class' logger.
     */
    private static Logger logger = LogHelper.createLogger(Module.class
        .getName());

    /**
     * The number of created modules.
     */
    private static int count;

    /**
     * This <code>enum</code> lists the three different types of
     * modules.
     */
    public enum ModuleType {
        /**
         * This is a module type that where the module is not able to
         * be connected to other modules, but other modules are able
         * to be connected to it. Its purpose is to read in events
         * from locations external to the <em>ECG Lab</em>.
         */
        SOURCE_MODULE,
        /**
         * This is a module type where the module is connectable to
         * other <em>SOURCE_MODULES</em> and
         * <em>INTERMEDIATE_MODULES</em>. Its purpose is to analyse
         * incoming events and pass events to other modules.
         */
        INTERMEDIATE_MODULE,
        /**
         * This is a module type where the module is only able to
         * beconnected to other <em>SOURCE_MODULES</em> ans
         * <em>INTERMEDIATE_MODULES</em>. Its purpose is to write
         * vents into locations external to the <em>ECG Lab</em>.
         */
        TARGET_MODULE
    }

    /**
     * The <em>MODULE_TYPE</em> of this module.
     */
    private ModuleType moduleType;

    /**
     * This module's unique int id.
     */
    private int moduleId;

    /**
     * This module's name.
     */
    private String moduleName;

    /**
     * This <code>Map</code> contains the modules, which are
     * connected to this module as a receiver of events.
     */
    private HashMap<Integer, Module> receiverModuleMap;

    /**
     * This <code>Map</code> contains the modules, which this module
     * is connected to and that are sending events to this module.
     */
    private HashMap<Integer, Module> senderModuleMap;

    /**
     * This <code>List</code> contains the
     * <em>MicroSensorDataTypes</em> that are provided by this
     * module.
     */
    private ArrayList<MicroSensorDataType> providedMsdtList;

    /**
     * Is the state of the module.
     */
    private boolean active;

    /**
     * Is the unique <code>String</code> id of this module's
     * <em>ModulePacket</em>.
     */
    private String modulePacketId;

    /**
     * A reference to the inner class object <em>EventSender</em>.
     */
    private EventSender eventSender;

    /**
     * A reference to the inner class object <em>EventReceiver</em>.
     */
    private EventReceiver eventReceiver;

    /**
     * A reference to the inner class object <em>SystemObserver</em>.
     */
    private SystemObserver systemObserver;

    /**
     * A reference to the inner class object
     * <em>SystemNotificator</em>.
     */
    private SystemNotificator systemNotificator;

    /**
     * A reference to the inner class <em>PropertyListener</em>.
     */
    private PropertyListener propertyListener;

    /**
     * Each ECG Module can have a set of properties which are declared
     * in the "module.properties.xml" file for each module. At runtime
     * the module's properties are stored in this field along with
     * their values.
     */
    private ModuleProperty[] runtimeProperties;

    /**
     * This constructor is not to be used directly. Instead use
     * {@link org.electrocodeogram.module.registry.ModuleRegistry#createRunningModule(String, String)}
     * to create a new module. This creates a new module of the given
     * type, with the given <em>ModulePacket</em> id and assigns the
     * given name to it. The module is registered with the
     * {@registry.ModuleRegistry} and if the any
     * {@org.electrocodeogram.msdt.MicroSensorDataType} is provided by
     * this module it is registered with the
     * {@link org.electrocodeogram.msdt.registry.MsdtRegistry}.
     * @param type
     *            Is the module type
     * @param id
     *            Is the unique <code>String</code> id of the
     *            <em>ModulePacket</em>
     * @param name
     *            Is the name to be assigned to the module
     */
    public Module(final ModuleType type, final String id, final String name) {
        logger.entering(this.getClass().getName(), "Module");

        if (type == null) {
            logger.log(Level.SEVERE,
                "The parameter moduleType is null. Can not create Module");

            return;
        }

        if (id == null) {
            logger.log(Level.SEVERE,
                "The parameter moduleClassId is null. Can not create Module");

            return;
        }

        if (name == null) {
            logger.log(Level.SEVERE,
                "The parameter moduleName is null. Can not create Module");

            return;
        }

        this.moduleId = ++count;

        this.moduleName = name;

        this.modulePacketId = id;

        this.moduleType = type;

        this.receiverModuleMap = new HashMap<Integer, Module>();

        this.senderModuleMap = new HashMap<Integer, Module>();

        this.providedMsdtList = new ArrayList<MicroSensorDataType>();

        this.systemObserver = new SystemObserver(this);

        this.systemNotificator = new SystemNotificator(this);

        this.propertyListener = new PropertyListener(this);

        if (this.moduleType == ModuleType.SOURCE_MODULE) {
            this.eventSender = new EventSender(this);

        } else if (this.moduleType == ModuleType.INTERMEDIATE_MODULE) {
            this.eventReceiver = new EventReceiver(this);

            this.eventSender = new EventSender(this);
        } else {
            this.eventReceiver = new EventReceiver(this);
        }

        try {
            initializeRuntimeProperties();

        } catch (ModulePropertyException e) {
            logger
                .log(Level.WARNING,
                    "An error occured while setting the initial value of a ModuleProperty.");
        }

        registerMSDTs();

        ModuleSystem.getInstance().registerModule(this);

        this.systemNotificator.fireStatechangeNotification();

        logger.exiting(this.getClass().getName(), "Module");
    }

    /**
     * This method is called whenever this module gets a notification
     * of a state change form the sytem. It is left to the actual
     * module implementation to react on such an event.
     */
    public abstract void analyseCoreNotification();

    /**
     * The nested {@link Module.PropertyListener} calls this, whenever
     * a <em>ModuleProperty</em> ha changed.
     * @param moduleProperty
     *            Is the <em>ModuleProperty</em> that has been
     *            changed
     * @throws ModulePropertyException
     *             If the property value is causing an
     *             <em>Exception</em> in the actual module
     *             implementation
     */
    protected abstract void propertyChanged(ModuleProperty moduleProperty)
        throws ModulePropertyException;

    /**
     * This method initializes the actual module. It must be
     * implementes by all modules.
     */
    public abstract void initialize();

    /**
     * This abstract method is to be implemented by all modules. Its
     * implementation tells what to do with a received event.
     * @param eventPacket
     *            Is the received event
     */
    protected abstract void receiveEventPacket(ValidEventPacket eventPacket);

    /**
     * This method returns the module's <em>SystemObserver</em>.
     * @return The module's <em>SystemObserver</em>
     */
    public final SystemObserver getSystemObserver() {
        logger.entering(this.getClass().getName(), "getSystemObserver");

        logger.exiting(this.getClass().getName(), "getSystemObserver");

        return this.systemObserver;
    }

    /**
     * This method returns the unique int id of this module.
     * @return The unique int id of this module
     */
    public final int getId() {
        logger.entering(this.getClass().getName(), "getId");

        logger.exiting(this.getClass().getName(), "getId");

        return this.moduleId;
    }

    /**
     * This method returns the <em>MODULE_TYPE</em> of this module.
     * @return The <em>MODULE_TYPE</em> of this module
     */
    public final ModuleType getModuleType() {
        logger.entering(this.getClass().getName(), "getModuleType");

        logger.exiting(this.getClass().getName(), "getModuleType");

        return this.moduleType;
    }

    /**
     * This method returns the name of this module.
     * @return The name of this module
     */
    public final String getName() {
        logger.entering(this.getClass().getName(), "getName");

        logger.exiting(this.getClass().getName(), "getName");

        return this.moduleName;
    }

    /**
     * This method returns the number of connected modules.
     * @return The number of connected modules
     */
    public final int getReceivingModuleCount() {
        logger.entering(this.getClass().getName(), "getReceivingModuleMap");

        logger.exiting(this.getClass().getName(), "getReceivingModuleMap");

        return this.receiverModuleMap.size();
    }

    /**
     * This method returns an <code>Array</code> of all modules that
     * are connected to this module.
     * @return An <code>Array</code> of all modules that are
     *         connected to this module
     */
    public final Module[] getReceivingModules() {
        logger.entering(this.getClass().getName(), "getReceivingModules");

        Collection<Module> receivingModules = this.receiverModuleMap.values();

        logger.exiting(this.getClass().getName(), "getReceivingModules");

        return receivingModules.toArray(new Module[receivingModules.size()]);
    }

    /**
     * This method gets information about the module and returns them
     * as a <code>String</code>.
     * @return A String of detailed information about the module
     */
    public final String getDetails() {

        logger.entering(this.getClass().getName(), "getDetails");

        String text = "Name: \t" + getName() + "\nID: \t " + getId()
                      + "\nTyp: \t";

        ModuleType type = getModuleType();

        switch (type) {
            case SOURCE_MODULE:
                text += "Source Module";
                break;

            case INTERMEDIATE_MODULE:
                text += "Intermediate Module";
                break;

            case TARGET_MODULE:
                text += "Target Module";
                break;
            default:
                return "";
        }

        if (this instanceof IIntermediateModule) {
            IIntermediateModule eventProcessor = (IIntermediateModule) this;

            text += "\nMode: \t";

            switch (eventProcessor.getProcessingMode()) {
                case ANNOTATOR:
                    text += "Annotator";
                    break;

                case FILTER:
                    text += "Filter";
                    break;

                default:
                    return "";

            }
        }

        ModuleDescriptor moduleDescriptor = null;
        try {
            moduleDescriptor = ModuleSystem.getInstance().getModuleDescriptor(
                this.modulePacketId);

            text += "\nDescription: \t";

            text += moduleDescriptor.getDescription();

            text += "\nAuthor: \t";

            text += moduleDescriptor.getProvider_name();

            text += "\nVersion: \t";

            text += moduleDescriptor.getVersion();

        } catch (ModuleClassException e) {
            logger.log(Level.WARNING,
                "An Exception has occured while reading the module description of: "
                                + this.getName());
        }

        logger.exiting(this.getClass().getName(), "getDetails");

        return text;

    }

    /**
     * This method returns the <em>MicroSensorDataTypes</em> that
     * are provided by this module.
     * @return The <em>MicroSensorDataTypes</em> that are provided
     *         by this module
     */
    public final MicroSensorDataType[] getProvidedMicroSensorDataType() {
        logger.entering(this.getClass().getName(),
            "getProvidedMicroSensorDataType");

        logger.exiting(this.getClass().getName(),
            "getProvidedMicroSensorDataType");

        return this.providedMsdtList
            .toArray(new MicroSensorDataType[this.providedMsdtList.size()]);
    }

    /**
     * This method returns the unique <code>String</code> id of the
     * <em>ModulePacket</em> of this module.
     * @return The <code>String</code> id of the
     *         <em>ModulePacket</em>
     */
    public final String getClassId() {
        logger.entering(this.getClass().getName(), "getClassId");

        logger.exiting(this.getClass().getName(), "getClassId");

        return this.modulePacketId;
    }

    /**
     * This returns the state of the module.
     * @return <code>true</code> if the module is active and
     *         <code>false</code> if not.
     */
    public final boolean getState() {
        logger.entering(this.getClass().getName(), "getState");

        logger.exiting(this.getClass().getName(), "getState", this.active);

        return this.active;
    }

    /**
     * This method is used to get the <em>ModuleProperty</em> with the given name.
     * @param name Is the name of the <em>ModuleProperty</em>
     * @return The <em>ModuleProperty</em> with the given name
     * @throws PropertyException If the a <em>ModuleProperty</em> with the given name is not declared for this module
     */
    public final ModuleProperty getModuleProperty(final String name)
        throws PropertyException {
        logger.entering(this.getClass().getName(), "getModuleProperty",
            new Object[] {name});

        ModuleProperty toReturn = null;

        if (this.runtimeProperties == null) {
            logger.exiting(this.getClass().getName(), "getModuleProperty");

            throw new PropertyException("The module " + this.getName()
                                        + " has no ModuleProperties.");
        }

        for (ModuleProperty moduleProperty : this.runtimeProperties) {
            if (moduleProperty.getName().equals(name)) {
                toReturn = moduleProperty;

                logger.exiting(this.getClass().getName(), "getModuleProperty",
                    toReturn);

                return toReturn;
            }
        }

        logger
            .exiting(this.getClass().getName(), "getModuleProperty", toReturn);

        throw new PropertyException("The module " + this.getName()
                                    + " has no ModuleProperty with name "
                                    + name + ".");
    }

    /**
     * This checks if the module is of the given <em>MODULE_TYPE</em>.
     * @param type
     *            Is the <em>MODULE_TYPE</em> to check
     * @return <code>true</code> If the module is of the given
     *         ModuleType and <code>false</code> if not
     */
    public final boolean isModuleType(final ModuleType type) {
        logger.entering(this.getClass().getName(), "isModuleType");

        if (this.moduleType == type) {
            return true;
        }

        logger.exiting(this.getClass().getName(), "isModuleType");

        return false;

    }

    /**
     * This method deactivates the module. The module might be already
     * deactivated.
     */
    public final void deactivate() {

        logger.entering(this.getClass().getName(), "deactivate");

        if (!this.active) {

            logger.exiting(this.getClass().getName(), "deactivate");

            logger
                .log(Level.INFO, "Module allready inactive " + this.getName());

            return;

        }

        if (this instanceof SourceModule) {
            SourceModule module = (SourceModule) this;

            module.stopReader();
        }

        if (this instanceof TargetModule) {
            ITargetModule module = (ITargetModule) this;

            module.stopWriter();
        }

        this.active = false;

        this.systemNotificator.fireStatechangeNotification();

        logger.log(Level.INFO, "Module deactivated " + this.getName());

        logger.exiting(this.getClass().getName(), "deactivate");
    }

    /**
     * This method activates the module. The module might be already
     * activated.
     * @throws ModuleActivationException
     *             If an <code>Exception</code> occurs during module
     *             activation.
     */
    public final void activate() throws ModuleActivationException {
        logger.entering(this.getClass().getName(), "activate");

        if (this.active) {
            logger.exiting(this.getClass().getName(), "activate");

            logger.log(Level.INFO, "Module allready active " + this.getName());

            return;
        }

        if (this instanceof SourceModule) {
            SourceModule module = (SourceModule) this;

            try {
                module.startReader(module);
            } catch (SourceModuleException e) {
                throw new ModuleActivationException(e.getMessage());
            }
        } else if (this instanceof ITargetModule) {
            ITargetModule module = (ITargetModule) this;

            try {
                module.startWriter();
            } catch (TargetModuleException e) {
                throw new ModuleActivationException(e.getMessage());
            }
        }

        this.active = true;

        this.systemNotificator.fireStatechangeNotification();

        logger.log(Level.INFO, "Module activated " + this.getName());

        logger.exiting(this.getClass().getName(), "activate");
    }

    /**
     * This method is called when the module is removed from the ECG
     * Lab.
     */
    public final void remove() {
        logger.entering(this.getClass().getName(), "remove");

        if (this.senderModuleMap.size() != 0) {

            Module[] parentModules = getSendingModules();

            for (int i = 0; i < parentModules.length; i++) {

                Module module = parentModules[i];

                try {
                    module.disconnectReceiverModule(this);
                } catch (ModuleInstanceException e) {
                    logger.log(Level.WARNING,
                        "An Exception occured while disconnecting the module: "
                                        + this.getName());
                }
            }
        }

        for (MicroSensorDataType msdt : this.providedMsdtList) {
            try {
                msdt.removeProvidingModule(this);
            } catch (MicroSensorDataTypeRegistrationException e) {
                logger.log(Level.WARNING,
                    "An Exception occured while deregistering the module's MSDTs for: "
                                    + this.getName());

            }
        }

        ModuleSystem.getInstance().deregisterModule(this);

        logger.exiting(this.getClass().getName(), "remove");

    }

    /**
     * This method returns the <em>ModuleProperties</em> of this
     * module with their current runtime values in an
     * <code>Array</code>.
     * @return The <em>ModuleProperties</em> of this module
     */
    public final ModuleProperty[] getRuntimeProperties() {
        logger.entering(this.getClass().getName(), "getRuntimeProperties");

        if (this.runtimeProperties == null) {
            logger.exiting(this.getClass().getName(), "getRuntimeProperties");

            return null;

        }

        int size = this.runtimeProperties.length;

        ModuleProperty[] toReturn = new ModuleProperty[size];

        for (int i = 0; i < size; i++) {
            toReturn[i] = this.runtimeProperties[i];
        }

        logger.exiting(this.getClass().getName(), "getRuntimeProperties");

        return toReturn;
    }

    /**
     * This method is used to connect a given module to this module.
     * @param module
     *            Is the module that should be connected to this
     *            module.
     * @return The unique int id of the connected module
     * @throws ModuleConnectionException
     *             If the given module could not be connected to this
     *             module. This happens if this module is a target
     *             module or if the given module is already connected
     *             to this module.
     */
    public final int connectReceiverModule(final Module module)
        throws ModuleConnectionException {

        logger.entering(this.getClass().getName(), "connectReceivingModules");

        if (this.moduleType == ModuleType.TARGET_MODULE) {
            throw new ModuleConnectionException(
                "You can not connect another module to this module.");
        } else if (this.receiverModuleMap.containsKey(new Integer(module
            .getId()))) {
            throw new ModuleConnectionException(
                "These mdoules are connected already.");
        } else {

            this.eventSender.addObserver(module);

            this.receiverModuleMap.put(new Integer(module.moduleId), module);

            module.addParentModule(this);

            this.systemNotificator.fireStatechangeNotification();

            logger.log(Level.INFO, "Connetced module " + this.getName()
                                   + " to module " + module.getName());

            logger
                .exiting(this.getClass().getName(), "connectReceivingModules");

            return module.moduleId;

        }

    }

    /**
     * This method disconnects a connected module.
     * @param module
     *            The module to disconnect
     * @throws ModuleInstanceException
     *             If the given module is not connected to this
     *             module.
     */
    public final void disconnectReceiverModule(final Module module)
        throws ModuleInstanceException {
        logger
            .entering(this.getClass().getName(), "disconnectReceivingModules");

        if (module == null) {
            logger.log(Level.WARNING, "module is null");

            return;
        }

        if (!this.receiverModuleMap.containsKey(new Integer(module.getId()))) {
            throw new ModuleInstanceException("The given module id "
                                              + this.moduleId + " is unknown.");
        }

        this.eventSender.deleteObserver(module);

        this.receiverModuleMap.remove(new Integer(module.getId()));

        this.systemNotificator.fireStatechangeNotification();

        logger.log(Level.INFO, "Disconnetced module " + this.getName()
                               + " to module " + module.getName());

        logger.exiting(this.getClass().getName(), "disconnectReceivingModules");
    }

    /**
     * This method returns a reference to the <em>EventReceiver</em>
     * of the module.
     * @return The <em>EventReceiver</em> of the module
     */
    final EventReceiver getEventReceiver() {
        logger.entering(this.getClass().getName(), "getEventReceiver");

        logger.exiting(this.getClass().getName(), "getEventReceiver");

        return this.eventReceiver;
    }

    /**
     * Actual module implementations are using this method to send
     * events.
     * @param packet
     *            Is the event packet that contains the analysis
     *            result
     */
    protected final void sendEventPacket(final ValidEventPacket packet) {
        logger.entering(this.getClass().getName(), "sendEventPacket");

        this.eventSender.sendEventPacket(packet);

        logger.exiting(this.getClass().getName(), "sendEventPacket");
    }

    /**
     * Returns an <code>Array</code> of all modules this module is
     * connected to.
     * @return An <code>Array</code> of all modules this module is
     *         connected to
     */
    private Module[] getSendingModules() {
        logger.entering(this.getClass().getName(), "getSendingModules");

        Collection<Module> sendingModules = this.senderModuleMap.values();

        logger.exiting(this.getClass().getName(), "getSendingModules");

        return sendingModules.toArray(new Module[sendingModules.size()]);
    }

    /**
     * This puts the given module in this module's
     * {@link #senderModuleMap}.
     * @param module
     *            Is the module to which this module will be
     *            connected.
     */
    private void addParentModule(final Module module) {
        logger.entering(this.getClass().getName(), "addParentModule");

        this.senderModuleMap.put(new Integer(module.getId()), module);

        logger.exiting(this.getClass().getName(), "addParentModule");
    }

    /**
     * This method is used to register the
     * <em>MicroSensorDataTypes</em> that are provided by this
     * module.
     */
    private void registerMSDTs() {
        logger.entering(this.getClass().getName(), "registerMSDTs");

        if (this instanceof SourceModule) {

            logger.log(Level.INFO,
                "Registering predefined MSDTs dor SourceModule.");

            MicroSensorDataType[] msdts = ModuleSystem.getInstance()
                .getPredefinedMicroSensorDataTypes();

            if (msdts == null) {
                return;
            }

            for (MicroSensorDataType msdt : msdts) {
                try {
                    ModuleSystem.getInstance().requestMsdtRegistration(msdt,
                        this);

                    this.providedMsdtList.add(msdt);
                } catch (MicroSensorDataTypeRegistrationException e) {
                    logger
                        .log(
                            Level.SEVERE,
                            "An Exception occured while registering predefined MSDTs for this SourceModule: "
                                            + this.getName());

                    return;
                }
            }

        }

        ModuleDescriptor moduleDescriptor = null;

        logger.log(Level.INFO, "Going to register additional MSDTs...");

        try {
            moduleDescriptor = ModuleSystem.getInstance().getModuleDescriptor(
                this.modulePacketId);

            if (moduleDescriptor == null) {
                logger.log(Level.WARNING, "ModuleDescriptor was null for: "
                                          + this.getName());

                return;
            }

            MicroSensorDataType[] microSensorDataTypes = moduleDescriptor
                .getMicroSensorDataTypes();

            if (microSensorDataTypes != null) {
                logger.log(Level.INFO, "Found " + microSensorDataTypes.length
                                       + " additional MSDTs for: "
                                       + this.getName());

                for (MicroSensorDataType msdt : microSensorDataTypes) {
                    this.providedMsdtList.add(msdt);

                    ModuleSystem.getInstance().requestMsdtRegistration(msdt,
                        this);
                }

                logger.log(Level.INFO, "Registered "
                                       + microSensorDataTypes.length
                                       + " additional MSDTs for: "
                                       + this.getName());

            }
        } catch (ModuleClassException e) {
            logger.log(Level.INFO,
                "No ModuleDescriptor was found for the module "
                                + this.getName());
        }

        catch (MicroSensorDataTypeRegistrationException e) {
            logger
                .log(
                    Level.SEVERE,
                    "An Exception occured while registering predefined MSDTs for this SourceModule: "
                                    + this.getName());
        }

        logger.exiting(this.getClass().getName(), "registerMSDTs");
    }

    /**
     * During the module creation, this method reads the
     * <em>ModuleProperties</em> from the <em>ModulePackage</em>
     * and sets the module's properties to them.
     * @throws ModulePropertyException
     *             If an <em>Exception</em> occures while setting
     *             the <em>ModuleProperty</em> in the actual module
     *             implementation
     */
    private void initializeRuntimeProperties() throws ModulePropertyException {

        logger.entering(this.getClass().getName(),
            "initializeRuntimeProperties");

        try {
            this.runtimeProperties = ModuleSystem.getInstance()
                .getModuleDescriptor(this.getClassId()).getProperties();

            logger
                .log(
                    Level.FINE,
                    "The module "
                                    + this.getName()
                                    + " has successfully read its ModuleProperties.");

        } catch (ModuleClassException e) {

            logger.log(Level.SEVERE,
                "An error occured during ModuleProperties initialization.");

            return;

        }

        if (this.runtimeProperties == null) {
            logger.log(Level.FINE, "The module " + this.getName()
                                   + " has no ModuleProperties.");

            return;
        }

        logger.log(Level.FINE, "The module " + this.getName() + " has "
                               + this.runtimeProperties.length
                               + " ModuleProperties.");

        for (ModuleProperty moduleProperty : this.runtimeProperties) {
            moduleProperty.addObserver(this.propertyListener);

            logger.log(Level.FINE, "Going to set the property "
                                   + moduleProperty.getName());

            try {
                if (!moduleProperty.getType().equals(
                    Class.forName("java.lang.reflect.Method"))
                    && moduleProperty.getValue() != null) {
                    propertyChanged(moduleProperty);

                    logger.log(Level.FINE, "Property "
                                           + moduleProperty.getName()
                                           + " has been set.");
                } else {
                    logger.log(Level.FINE,
                        "Property " + moduleProperty.getName()
                                        + " is null or method and is ignored.");
                }
            } catch (ClassNotFoundException e) {

                logger.log(Level.SEVERE,
                    "An error occured during ModuleProperty initilization.");

                return;

            }
        }

        logger
            .exiting(this.getClass().getName(), "initializeRuntimeProperties");
    }

    /**
     * The <em>EventSender</em> is the part of a module that sends
     * events to other modules. It is an <em>Observable</em> and
     * notifies registered <em>Observers</em> about new events in
     * this module. Only <em>SOURCE_MODULES</em> or
     * <em>INTERMEDIATE_MODULE</em> are having are having an
     * <em>EventSender</em>, which is not <code>null</code>.
     */
    private static class EventSender extends Observable {

        /**
         * A reference to the sorrounding module instance.
         */
        private Module myModule;

        /**
         * This is this class' logger.
         */
        private static Logger eventSenderLogger = LogHelper
            .createLogger(EventSender.class.getName());

        /**
         * This creates the <em>EventSender<em>.
         * @param module
         *            Is the Module to which this EventSender is
         *            belonging
         */
        public EventSender(final Module module) {
            eventSenderLogger
                .entering(this.getClass().getName(), "eventSender");

            this.myModule = module;

            eventSenderLogger.exiting(this.getClass().getName(), "eventSender");
        }

        /**
         * This registers a module with the <em>EventSender</em>.
         * The module is than notified about new events in this
         * module.
         * @param module
         *            Is the module to register
         */
        public void addObserver(final Module module) {

            eventSenderLogger
                .entering(this.getClass().getName(), "addObserver");

            if (module == null) {
                eventSenderLogger.log(Level.WARNING, "module is null");

                return;
            }

            super.addObserver(module.getEventReceiver());

            eventSenderLogger.exiting(this.getClass().getName(), "addObserver");
        }

        /**
         * This deregisters a registered module from the list of
         * <em>Observers</em>.
         * @param module
         *            Is the module to deregister
         */
        public void deleteObserver(final Module module) {
            eventSenderLogger.entering(this.getClass().getName(),
                "deleteObserver");

            if (module == null) {
                eventSenderLogger.log(Level.WARNING, "module is null");

                return;
            }

            super.deleteObserver(module.getEventReceiver());

            eventSenderLogger.exiting(this.getClass().getName(),
                "deleteObserver");
        }

        /**
         * This sends the given event to all connected modules. The
         * event's
         * {@link org.electrocodeogram.event.EventPacket#sourceId}
         * field of is set to this module's id. Before sending, the
         * given event is repackte into a new
         * {@link org.electrocodeogram.event.ValidEventPacket} and so
         * it is validated.
         * @param eventPacket
         *            Is the event to send
         */
        public void sendEventPacket(final ValidEventPacket eventPacket) {
            eventSenderLogger.entering(this.getClass().getName(),
                "sendEventPacket");

            if (this.myModule.getState() == true && (eventPacket != null)) {
                setChanged();

                try {
                    ValidEventPacket packet = new ValidEventPacket(
                        this.myModule.getId(), eventPacket.getTimeStamp(),
                        eventPacket.getSensorDataType(), eventPacket
                            .getArglist());

                    packet.setDeliveryState(DELIVERY_STATE.SENT);

                    eventSenderLogger.log(Level.INFO,
                        "An event is about to be send by thie module: "
                                        + this.myModule.getName());

                    eventSenderLogger.log(ECGLevel.PACKET, packet.toString());

                    notifyObservers(packet);

                    this.myModule.systemNotificator
                        .fireEventNotification(packet);
                } catch (IllegalEventParameterException e) {

                    clearChanged();

                    eventSenderLogger.log(Level.WARNING,
                        "An exception occured while sending an event by the module: "
                                        + this.myModule.getName());

                }

                clearChanged();
            }

            eventSenderLogger.exiting(this.getClass().getName(),
                "sendEventPacket");
        }

    }

    /**
     * The <em>EventReceiver</em> is the part of a module that
     * receives events from other modules. It is an <em>Observer</em>
     * and uses the {@link #update(Observable, Object)} method to
     * receive events from orther module's <em>EventSenders</em> it
     * has been registered to. Only <em>INTERMEDIATE_MODULES</em> or
     * <em>TARGET_MODULES</em> are having an <em>EventReceiver</em>,
     * which is not <code>null</code>. EventReceiver.
     */
    private static class EventReceiver implements Observer {

        /**
         * A reference to the sorrounding module instance.
         */
        private Module myModule;

        /**
         * This is this class' logger.
         */
        private static Logger eventReceiverLogger = LogHelper
            .createLogger(EventReceiver.class.getName());

        /**
         * This creates the <em>EventReceiver</em>.
         * @param module
         *            Is the module that the <em>EventReceiver</em>
         *            is belonging to
         */
        public EventReceiver(final Module module) {
            eventReceiverLogger.entering(this.getClass().getName(),
                "EventReceiver");

            this.myModule = module;

            eventReceiverLogger.exiting(this.getClass().getName(),
                "EventReceiver");
        }

        /**
         * @see java.util.Observer#update(java.util.Observable,
         *      java.lang.Object)
         */
        public void update(final Observable object, final Object data) {
            eventReceiverLogger.entering(this.getClass().getName(), "update");

            if (object == null) {
                eventReceiverLogger.log(Level.WARNING,
                    "Parameter object is null. Ignoring event notification.");

                return;
            }

            if (data == null) {
                eventReceiverLogger.log(Level.WARNING,
                    "Parameter data is null. Ignoring event notification.");

                return;
            }

            if ((object instanceof EventSender)
                && data instanceof ValidEventPacket) {
                try {
                    ValidEventPacket receivedPacketForProcessing = (ValidEventPacket) data;

                    receivedPacketForProcessing
                        .setDeliveryState(DELIVERY_STATE.RECEIVED);

                    ValidEventPacket receivedPacketForSystem = new ValidEventPacket(
                        this.myModule.getId(), receivedPacketForProcessing
                            .getTimeStamp(), receivedPacketForProcessing
                            .getSensorDataType(), receivedPacketForProcessing
                            .getArglist());

                    receivedPacketForSystem
                        .setDeliveryState(DELIVERY_STATE.RECEIVED);

                    eventReceiverLogger.log(Level.INFO,
                        "An event has benn recieved by the module: "
                                        + this.myModule.getName());

                    eventReceiverLogger.log(ECGLevel.PACKET,
                        receivedPacketForProcessing.toString());

                    this.myModule.systemNotificator
                        .fireEventNotification(receivedPacketForSystem);

                    this.myModule
                        .receiveEventPacket(receivedPacketForProcessing);
                } catch (IllegalEventParameterException e) {
                    eventReceiverLogger.log(Level.WARNING,
                        "An Eception occured while receiving an event in module: "
                                        + this.myModule.getName());

                }

            }

            eventReceiverLogger.exiting(this.getClass().getName(), "update");

        }

    }

    /**
     * The <em>SystemObserver</em> is the part of each module that
     * gets notifications from the system about system state changes.
     */
    private static class SystemObserver implements Observer {

        /**
         * A reference to the sorrounding module instance.
         */
        private Module myModule;

        /**
         * This is this class' logger.
         */
        private static Logger systemObserverLogger = LogHelper
            .createLogger(SystemObserver.class.getName());

        /**
         * This creates the <em>SystemObserver</em>.
         * @param module
         *            Is the Module that the SystemObserver is
         *            belonging to
         */
        public SystemObserver(final Module module) {
            systemObserverLogger.entering(this.getClass().getName(),
                "SystemObserver");

            this.myModule = module;

            systemObserverLogger.exiting(this.getClass().getName(),
                "SystemObserver");
        }

        /**
         * @see java.util.Observer#update(java.util.Observable,
         *      java.lang.Object)
         */
        public void update(final Observable object, @SuppressWarnings("unused")
        final Object arg) {
            systemObserverLogger.entering(this.getClass().getName(), "update");

            if (object == null) {
                systemObserverLogger.log(Level.WARNING, "object is null");

                return;
            }

            if (object instanceof ISystem) {
                this.myModule.analyseCoreNotification();
            }

            systemObserverLogger.exiting(this.getClass().getName(), "update");

        }

    }

    /**
     * The <em>SystemNotificator</em> is used by the module to
     * notify the system about state changes in the module.
     */
    private static class SystemNotificator extends Observable {

        /**
         * A reference to the sorrounding module.
         */
        private Module myModule;

        /**
         * This is this class' logger.
         */
        private static Logger systemNotificatorlogger = LogHelper
            .createLogger(SystemNotificator.class.getName());

        /**
         * The constructor registers the ECG GUI component with the
         * SystemNotificator.
         * @param module
         *            Is the surrounding module
         */
        public SystemNotificator(final Module module) {
            systemNotificatorlogger.entering(this.getClass().getName(),
                "SystemNotificator");

            this.myModule = module;

            if (org.electrocodeogram.system.System.getInstance().getGui() != null) {
                this.addObserver(org.electrocodeogram.system.System
                    .getInstance().getGui());
            }

            systemNotificatorlogger.exiting(this.getClass().getName(),
                "SystemNotificator");

        }

        /**
         * The method is called to notify the system of a new event
         * beeing processed by thie module.
         * @param packet
         *            Is the last sent event packet.
         */
        public void fireEventNotification(final ValidEventPacket packet) {
            systemNotificatorlogger.entering(this.getClass().getName(),
                "fireEventNotification");

            if (packet == null) {
                systemNotificatorlogger.log(Level.WARNING, "packet is null");

                return;
            }

            setChanged();

            notifyObservers(packet);

            clearChanged();

            systemNotificatorlogger.exiting(this.getClass().getName(),
                "fireEventNotification");

        }

        /**
         * The method is called to notify the ECG system of state
         * changes in the module.
         */
        public void fireStatechangeNotification() {
            systemNotificatorlogger.entering(this.getClass().getName(),
                "fireStatechangeNotification");

            setChanged();

            notifyObservers(this.myModule);

            clearChanged();

            systemNotificatorlogger.exiting(this.getClass().getName(),
                "fireStatechangeNotification");
        }

    }

    /**
     * When a <em>ModuleProperty</em> is changed at runtime by the
     * user, this <em>PropertyListener</em> is notfied about it. Its
     * {@link #update(Observable, Object)} method is calling the
     * abstract method ..., which handles the property change for the
     * actual odule implementation.
     */
    private static class PropertyListener implements Observer {

        /**
         * This is ths class' logger.
         */
        private static Logger propertyListenerLogger = LogHelper
            .createLogger(PropertyListener.class.getName());

        /**
         * This is the module that is sorrounding this class.
         */
        private Module myModule;

        /**
         * Creates the <em>PropertyListener</em>.
         * @param module
         *            Is the sorrounding module
         */
        public PropertyListener(final Module module) {
            propertyListenerLogger.entering(this.getClass().getName(),
                "propertyListener");

            this.myModule = module;

            propertyListenerLogger.exiting(this.getClass().getName(),
                "propertyListener");
        }

        /**
         * @see java.util.Observer#update(java.util.Observable,
         *      java.lang.Object)
         */
        public void update(final Observable o, @SuppressWarnings("unused")
        final Object arg) {

            propertyListenerLogger
                .entering(this.getClass().getName(), "update");

            if (o == null) {
                propertyListenerLogger.log(Level.WARNING,
                    "The parameter Observable is null.");

                return;
            }

            if (o instanceof ModuleProperty) {
                ModuleProperty moduleProperty = (ModuleProperty) o;

                try {
                    this.myModule.propertyChanged(moduleProperty);
                } catch (ModulePropertyException e) {
                    moduleProperty.restore();
                }
            } else {
                propertyListenerLogger.log(Level.WARNING,
                    "The parameter Observable is not a ModuleProperty.");

                propertyListenerLogger.exiting(this.getClass().getName(),
                    "update");

                return;
            }

            propertyListenerLogger.exiting(this.getClass().getName(), "update");

        }

    }
}
