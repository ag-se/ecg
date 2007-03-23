/*
 * Class: Module
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
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
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.module.event.MessageEvent;
import org.electrocodeogram.module.intermediate.IIntermediateModule;
import org.electrocodeogram.module.registry.ModulePackageNotFoundException;
import org.electrocodeogram.module.registry.ModuleInstanceNotFoundException;
import org.electrocodeogram.module.source.SourceModule;
import org.electrocodeogram.module.source.SourceModuleException;
import org.electrocodeogram.module.target.ITargetModule;
import org.electrocodeogram.module.target.TargetModule;
import org.electrocodeogram.module.target.TargetModuleException;
import org.electrocodeogram.modulepackage.ModuleDescriptor;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.electrocodeogram.modulepackage.ModulePropertyException;
import org.electrocodeogram.modulepackage.ModuleType;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.msdt.MsdtProvider;
import org.electrocodeogram.msdt.registry.MicroSensorDataTypeRegistrationException;
import org.electrocodeogram.system.ISystem;
import org.electrocodeogram.system.ModuleSystem;
import org.electrocodeogram.ui.event.ProcessedEventPacket;

/**
 * This abstract class represents an <em>ElectroCodeoGram Module</em>.
 * A module is an object able to receive events from other modules and
 * to send events to other modules. For that purpose modules are
 * connected to each other. Multiple modules are allowed to be
 * connected to another module. In that case the multiple modules are
 * receiving events from the one module. The multiple modules are
 * called receiver modules and the one module is called the sender
 * module. The event sending is realized with use of the
 * <em>Observer</em> design pattern, where every module is an
 * <em>Observer</em> and an <em>Observable</em>. In fact each
 * module contains member classes that are acting as
 * <em>Observers</em> and <em>Observables</em>. These are
 * <ul>
 * <li> {@link Module.EventSender} in an <em>Observable</em> role
 * and
 * <li> {@link Module.EventReceiver} in an <em>Observer</em> role.
 * </ul>
 * When the <code>EventSender</code> of the sender module has a new
 * event, it notifies all registered <code>EventReceivers</code> of
 * other modules, passing the new event with the notification.
 * Additionally modules are observing the system state for relevant
 * state changes and modules are telling the system about their state
 * changes. This is realized with two other member classes:
 * <ul>
 * <li> {@link Module.SystemObserver} in is notified about system
 * state changes and
 * <li> {@link Module.GuiNotificator} tells the system about
 * changes in this module.
 * </ul>
 * The abstract class <code>Module</code> must be implemented. This
 * is done by implementing one of its abstract subclasses.
 * <ul>
 * <li>{@link org.electrocodeogram.module.source.SourceModule} must
 * be implemented for modules to read in events from locations
 * external to the <em>ECG Lab</em>.
 * <li>{@link org.electrocodeogram.module.intermediate.IntermediateModule}
 * must be implemented for modules to analyse incoming events and
 * <li>{@link org.electrocodeogram.module.target.TargetModule} must
 * be implemented for modules to write events into locations external
 * to the <em>ECG Lab</em>.
 * </ul>
 * Each module is created from classes defined in a
 * <em>ModulePackage</em> in the file system. From the module's
 * <em>module.properties.xml</em> inside the <em>ModulePackage</em>, each
 * module is given its defined <em>MicroSensorDataTypes</em>, its
 * <em>ModuleProperties</em> and the unique string id
 * of the <em>ModulePackage</em>. During its creation every
 * module instance also gets a unique int id.
 */
public abstract class Module implements MsdtProvider {

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
     * This map contains the modules, which are
     * connected to this module as a receiver of events.
     */
    private HashMap < Integer, Module > receiverModuleMap;

    /**
     * This map contains the modules, which this module
     * is connected to and that are sending events to this module.
     */
    private HashMap < Integer, Module > senderModuleMap;

    /**
     * This list contains the
     * <em>MicroSensorDataTypes</em> that are provided by this
     * module.
     */
    private ArrayList < MicroSensorDataType > providedMsdtList;

    /**
     * Is the state of the module.
     */
    private boolean active;

    /**
     * Is the unique string id of this module's
     * <em>ModulePackage</em>.
     */
    private String modulePacketId;

    /**
     * A reference to the member class object <em>EventSender</em>.
     */
    private EventSender eventSender;

    /**
     * A reference to the member class object <em>EventReceiver</em>.
     */
    private EventReceiver eventReceiver;

    /**
     * A reference to the member class object <em>SystemObserver</em>.
     */
    private SystemObserver systemObserver;

    /**
     * A reference to the member class object
     * <em>GuiNotificator</em>.
     */
    private GuiNotificator guiNotificator;

    /**
     * A reference to the member class <em>PropertyListener</em>.
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
     * {@link org.electrocodeogram.module.registry.ModuleRegistry#createModule(String, String)}
     * to create a new module. This creates a new module of the given
     * type, with the given <em>ModulePackage</em> id and assigns the
     * given name to it. The module is registered with the
     * {@registry.ModuleRegistry} and if any
     * {@org.electrocodeogram.msdt.MicroSensorDataType} are provided by
     * this module they are registered with the
     * {@link org.electrocodeogram.msdt.registry.MsdtRegistry}.
     * @param type
     *            Is the module type
     * @param id
     *            Is the unique string id of the
     *            <em>ModulePackage</em>
     * @param name
     *            Is the name to be assigned to the module
     */
    @SuppressWarnings("synthetic-access")
    protected Module(final ModuleType type, final String id, final String name) {
        logger.entering(this.getClass().getName(), "Module", new Object[] {
            type, id, name});

        if (type == null) {
            logger.log(Level.SEVERE,
                "The parameter \"type\" is null. Can not create Module");

            logger.exiting(this.getClass().getName(), "Module");

            return;
        }

        if (id == null) {
            logger.log(Level.SEVERE,
                "The parameter \"id\" is null. Can not create Module");

            logger.exiting(this.getClass().getName(), "Module");

            return;
        }

        if (name == null) {
            logger.log(Level.SEVERE,
                "The parameter \"name\" is null. Can not create Module");

            logger.exiting(this.getClass().getName(), "Module");

            return;
        }

        this.moduleId = ++count;

        this.moduleName = name;

        this.modulePacketId = id;

        this.moduleType = type;

        this.receiverModuleMap = new HashMap < Integer, Module >();

        this.senderModuleMap = new HashMap < Integer, Module >();

        this.providedMsdtList = new ArrayList < MicroSensorDataType >();

        this.systemObserver = new SystemObserver(this);

        this.guiNotificator = new GuiNotificator(this);

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

        this.guiNotificator.fireStatechangeNotification();

        logger.exiting(this.getClass().getName(), "Module");
    }

    /**
     * This method is called whenever this module gets a notification
     * of a state change form the system. It is left to the actual
     * module implementation to react to such an event.
     */
    public abstract void update();

    /**
     * The nested {@link Module.PropertyListener} calls this, whenever
     * a <em>ModuleProperty</em> has changed.
     * @param moduleProperty
     *            Is the <em>ModuleProperty</em> that has been
     *            changed
     * @throws ModulePropertyException
     *             If the property value is causing an
     *             exception in the actual module
     *             implementation
     */
    protected abstract void propertyChanged(ModuleProperty moduleProperty)
        throws ModulePropertyException;

    /**
     * This method initializes the actual module. It can be
     * implemented by all modules that are in need of additional initialisation.
     */
    public abstract void initialize();

    /**
     * This abstract method is implemented by the three direct subclasses od <code>Module</code>. Its
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

        logger.exiting(this.getClass().getName(), "getSystemObserver",
            this.systemObserver);

        return this.systemObserver;
    }

    /**
     * This method returns the unique int id of this module.
     * @return The unique int id of this module
     */
    public final int getId() {
        logger.entering(this.getClass().getName(), "getId");

        logger.exiting(this.getClass().getName(), "getId", Integer.valueOf(
            this.moduleId));

        return this.moduleId;
    }

    /**
     * This method returns the <em>MODULE_TYPE</em> of this module.
     * @return The <em>MODULE_TYPE</em> of this module
     */
    public final ModuleType getModuleType() {
        logger.entering(this.getClass().getName(), "getModuleType");

        logger.exiting(this.getClass().getName(), "getModuleType",
            this.moduleType);

        return this.moduleType;
    }

    /**
     * This method returns the name of this module.
     * @return The name of this module
     */
    public final String getName() {
        logger.entering(this.getClass().getName(), "getName");

        logger.exiting(this.getClass().getName(), "getName", this.moduleName);

        return this.moduleName;
    }

    /**
     * This method returns the number of connected modules.
     * @return The number of connected modules
     */
    public final int getReceivingModuleCount() {
        logger.entering(this.getClass().getName(), "getReceivingModuleMap");

        logger.exiting(this.getClass().getName(), "getReceivingModuleMap",
            Integer.valueOf(this.receiverModuleMap.size()));

        return this.receiverModuleMap.size();
    }

    /**
     * This method returns an array of all modules that
     * are connected to this module.
     * @return An array of all modules that are
     *         connected to this module
     */
    public final Module[] getReceivingModules() {
        logger.entering(this.getClass().getName(), "getReceivingModules");

        Collection < Module > receivingModules = this.receiverModuleMap
            .values();

        logger.exiting(this.getClass().getName(), "getReceivingModules",
            receivingModules.toArray(new Module[receivingModules.size()]));

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

            text += moduleDescriptor.getProviderName();

            text += "\nVersion: \t";

            text += moduleDescriptor.getVersion();

        } catch (ModulePackageNotFoundException e) {
            logger.log(Level.WARNING,
                "An Exception has occured while reading the module description of: "
                                + this.getName());
        }

        logger.exiting(this.getClass().getName(), "getDetails", text);

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

        logger
            .exiting(this.getClass().getName(),
                "getProvidedMicroSensorDataType", this.providedMsdtList
                    .toArray(new MicroSensorDataType[this.providedMsdtList
                        .size()]));

        return this.providedMsdtList
            .toArray(new MicroSensorDataType[this.providedMsdtList.size()]);
    }

    /**
     * This method returns the unique string id of the
     * <em>ModulePackage</em> of this module.
     * @return The string id of the
     *         <em>ModulePackaga</em>
     */
    public final String getModulePacketId() {
        logger.entering(this.getClass().getName(), "getClassId");

        logger.exiting(this.getClass().getName(), "getClassId",
            this.modulePacketId);

        return this.modulePacketId;
    }

    /**
     * This returns the state of the module.
     * @return <code>true</code> if the module is active and
     *         <code>false</code> if not.
     */
    public final boolean isActive() {
        logger.entering(this.getClass().getName(), "getState");

        logger.exiting(this.getClass().getName(), "getState", Boolean.valueOf(this.active));

        return this.active;
    }

    /**
     * This method is used to get the <em>ModuleProperty</em> with
     * the given name.
     * @param name
     *            Is the name of the <em>ModuleProperty</em>
     * @return The <em>ModuleProperty</em> with the given name
     * @throws ModulePropertyException
     *             If the a <em>ModuleProperty</em> with the given
     *             name is not declared for this module
     */
    public final ModuleProperty getModuleProperty(final String name)
        throws ModulePropertyException {
        logger.entering(this.getClass().getName(), "getModuleProperty",
            new Object[] {name});

        ModuleProperty toReturn = null;

        if (this.runtimeProperties == null) {
            logger.exiting(this.getClass().getName(), "getModuleProperty");

            throw new ModulePropertyException(
                "The module has no ModuleProperties.", this.getName(), this
                    .getId(), "", "");
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
            .exiting(this.getClass().getName(), "getModuleProperty");

        throw new ModulePropertyException(
            "The module has no ModuleProperty with name " + name + ".", this
                .getName(), this.getId(), "", "");
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

            logger.exiting(this.getClass().getName(), "isModuleType",
               Boolean.valueOf(true));

            return true;
        }

        logger.exiting(this.getClass().getName(), "isModuleType", Boolean.valueOf(false));

        return false;

    }

    /**
     * This method deactivates the module. The module might be already
     * deactivated. In that case nothing happens.
     */
    public final void deactivate() {

        logger.entering(this.getClass().getName(), "deactivate");

        if (!this.active) {

            logger.exiting(this.getClass().getName(), "deactivate");

            logger
                .log(Level.INFO, "Module allready inactive " + this.getName());

            logger.exiting(this.getClass().getName(), "deactivate");

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

        this.guiNotificator.fireStatechangeNotification();

        logger.log(Level.INFO, "Module deactivated " + this.getName());

        logger.exiting(this.getClass().getName(), "deactivate");
    }

    /**
     * This method activates the module. The module might be already
     * activated. In that case nothing happens.
     * @throws ModuleActivationException
     *             If an exception occurs during module
     *             activation.
     */
    public final void activate() throws ModuleActivationException {
        logger.entering(this.getClass().getName(), "activate");

        if (this.active) {
            logger.exiting(this.getClass().getName(), "activate");

            logger.log(Level.INFO, "Module allready active " + this.getName());

            logger.exiting(this.getClass().getName(), "activate");

            return;
        }

        if (this instanceof SourceModule) {
            SourceModule module = (SourceModule) this;

            try {
                module.startReader();
            } catch (SourceModuleException e) {
                throw new ModuleActivationException(e.getMessage(), this
                    .getName(), this.getId());
            }

        } else if (this instanceof ITargetModule) {
            ITargetModule module = (ITargetModule) this;

            try {
                module.startWriter();
            } catch (TargetModuleException e) {
                throw new ModuleActivationException(e.getMessage(), this
                    .getName(), this.getId());
            }
        }

        this.active = true;

        this.guiNotificator.fireStatechangeNotification();

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
                    module.disconnectModule(this);
                } catch (ModuleInstanceNotFoundException e) {
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

        deactivate();

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

            return new ModuleProperty[0];

        }

        int size = this.runtimeProperties.length;

        ModuleProperty[] toReturn = new ModuleProperty[size];

        for (int i = 0; i < size; i++) {
            toReturn[i] = this.runtimeProperties[i];
        }

        logger.exiting(this.getClass().getName(), "getRuntimeProperties",
            toReturn);

        return toReturn;
    }

    /**
     * This method is used to connect a given module to this module.
     * @param module
     *            Is the module that should be connected to this
     *            module
     * @return The unique int id of the connected module
     * @throws ModuleConnectionException
     *             If the given module could not be connected to this
     *             module. This happens if this module is a target
     *             module or if the given module is already connected
     *             to this module.
     */
    public final int connectModule(final Module module)
        throws ModuleConnectionException {

        logger.entering(this.getClass().getName(), "connectReceivingModules",
            new Object[] {module});

        if (this.moduleType == ModuleType.TARGET_MODULE) {

            logger
                .exiting(this.getClass().getName(), "connectReceivingModules");

            throw new ModuleConnectionException(
                "You can not connect another module to this module.", this
                    .getName(), this.getId(), module.getName(), module.getId());
        } else if (this.receiverModuleMap.containsKey(Integer.valueOf(module
            .getId()))) {

            logger
                .exiting(this.getClass().getName(), "connectReceivingModules");

            throw new ModuleConnectionException(
                "These mdoules are connected already.", this.getName(), this
                    .getId(), module.getName(), module.getId());
        } else {

            this.eventSender.addObserver(module);

            this.receiverModuleMap.put(Integer.valueOf(module.moduleId), module);

            module.addParentModule(this);

            this.guiNotificator.fireStatechangeNotification();

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
     * @throws ModuleInstanceNotFoundException
     *             If the given module is not connected to this
     *             module
     */
    public final void disconnectModule(final Module module)
        throws ModuleInstanceNotFoundException {
        logger.entering(this.getClass().getName(),
            "disconnectReceivingModules", new Object[] {module});

        if (module == null) {
            logger.log(Level.WARNING, "module is null");

            logger.exiting(this.getClass().getName(),
                "disconnectReceivingModules");

            return;
        }

        if (!this.receiverModuleMap.containsKey(Integer.valueOf(module.getId()))) {

            logger.exiting(this.getClass().getName(),
                "disconnectReceivingModules");

            throw new ModuleInstanceNotFoundException("The given module id "
                                                      + this.moduleId
                                                      + " is unknown.", module
                .getId());
        }

        this.eventSender.deleteObserver(module);

        this.receiverModuleMap.remove(Integer.valueOf(module.getId()));

        this.guiNotificator.fireStatechangeNotification();

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

        logger.exiting(this.getClass().getName(), "getEventReceiver",
            this.eventReceiver);

        return this.eventReceiver;
    }

    /**
     * This method returns a reference to the
     * <em>SystemNotificator</em> of the module.
     * @return The <em>SystemNotificator</em> of the module
     */
    final GuiNotificator getSystemNotificator() {

        logger.entering(this.getClass().getName(), "getSystemNotificator");

        logger.exiting(this.getClass().getName(), "getSystemNotificator",
            this.guiNotificator);

        return this.guiNotificator;
    }

    /**
     * Uused to send events.
     * @param packet
     *            Is the event packet to send
     */
    protected final void sendEventPacket(final ValidEventPacket packet) {
        logger.entering(this.getClass().getName(), "sendEventPacket",
            new Object[] {packet});

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

        Collection < Module > sendingModules = this.senderModuleMap.values();

        logger.exiting(this.getClass().getName(), "getSendingModules",
            sendingModules.toArray(new Module[sendingModules.size()]));

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
        logger.entering(this.getClass().getName(), "addParentModule",
            new Object[] {module});

        this.senderModuleMap.put(Integer.valueOf(module.getId()), module);

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

                logger.log(Level.FINE, "There are no MSDTs for module "
                                       + this.getName() + ".");

                logger.exiting(this.getClass().getName(), "registerMSDTs");

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

                    logger.exiting(this.getClass().getName(), "registerMSDTs");

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

                logger.exiting(this.getClass().getName(), "registerMSDTs");

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
        } catch (ModulePackageNotFoundException e) {
            logger.log(Level.INFO,
                "No ModuleDescriptor was found for the module "
                                + this.getName());
        } catch (MicroSensorDataTypeRegistrationException e) {
            logger
                .log(
                    Level.SEVERE,
                    "An Exception occured while registering predefined MSDTs for this SourceModule: "
                                    + this.getName());
        }

        logger.exiting(this.getClass().getName(), "registerMSDTs");
    }

    /**
     * During module creation, this method reads the
     * <em>ModuleProperties</em> from the <em>ModulePackage</em>
     * and sets the {@link #runtimeProperties} accordignly.
     * @throws ModulePropertyException
     *             If an exception occurs while setting
     *             a <em>ModuleProperty</em>
     */
    private void initializeRuntimeProperties() throws ModulePropertyException {

        logger.entering(this.getClass().getName(),
            "initializeRuntimeProperties");

        try {
            this.runtimeProperties = ModuleSystem.getInstance()
                .getModuleDescriptor(this.getModulePacketId()).getProperties();

            logger
                .log(
                    Level.FINE,
                    "The module "
                                    + this.getName()
                                    + " has successfully read its ModuleProperties.");

        } catch (ModulePackageNotFoundException e) {

            logger.log(Level.SEVERE,
                "An error occured during ModuleProperties initialization.");

            logger.exiting(this.getClass().getName(),
                "initializeRuntimeProperties");

            return;

        }

        if (this.runtimeProperties == null) {
            logger.log(Level.FINE, "The module " + this.getName()

            + " has no ModuleProperties.");

            logger.exiting(this.getClass().getName(),
                "initializeRuntimeProperties");

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

                logger.exiting(this.getClass().getName(),
                    "initializeRuntimeProperties");

                return;

            }
        }

        logger
            .exiting(this.getClass().getName(), "initializeRuntimeProperties");
    }

    /**
     * Makes the <em>GuiNotificator</em> accesible for all module implementations.
     * @return The <em>GuiNotificator</em> of this module
     */
    public final GuiNotificator getGuiNotifiator() {
        logger.entering(this.getClass().getName(), "GuiNotificator");

        logger.exiting(this.getClass().getName(), "GuiNotificator",
            this.guiNotificator);

        return this.guiNotificator;

    }

    /**
     * The <code>EventSender</code> is the part of a module that sends
     * events to other modules. It is an <em>Observable</em> and
     * notifies registered <em>Observers</em> about new events in
     * this module. Only <em>SOURCE_MODULES</em> or
     * <em>INTERMEDIATE_MODULE</em> are having are having an
     * <code>EventSender</code>, which is not <code>null</code>.
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
         * This creates the <em>EventSender</em>.
         * @param module
         *            Is the Module to which this EventSender is
         *            belonging
         */
        public EventSender(final Module module) {
            eventSenderLogger.entering(this.getClass().getName(),
                "eventSender", new Object[] {module});

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

            eventSenderLogger.entering(this.getClass().getName(),
                "addObserver", new Object[] {module});

            if (module == null) {
                eventSenderLogger.log(Level.WARNING, "module is null");

                eventSenderLogger.exiting(this.getClass().getName(),
                    "addObserver");

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
                "deleteObserver", new Object[] {module});

            if (module == null) {
                eventSenderLogger.log(Level.WARNING, "module is null");

                eventSenderLogger.exiting(this.getClass().getName(),
                    "deleteObserver");

                return;
            }

            super.deleteObserver(module.getEventReceiver());

            eventSenderLogger.exiting(this.getClass().getName(),
                "deleteObserver");
        }

        /**
         * This sends the given event to all connected modules. The
         * event's
         * {@link org.electrocodeogram.event.EventPacket#getSourceId()}
         * field of is set to this module's id. Before sending, the
         * given event is repackte into a new
         * {@link org.electrocodeogram.event.ValidEventPacket} and so
         * it is validated.
         * @param eventPacket
         *            Is the event to send
         */
        public void sendEventPacket(final ValidEventPacket eventPacket) {
            eventSenderLogger.entering(this.getClass().getName(),
                "sendEventPacket", new Object[] {eventPacket});

            if (this.myModule.isActive() && (eventPacket != null)) {
                setChanged();

                ProcessedEventPacket packet = new ProcessedEventPacket(
                	this.myModule.moduleId,
                    eventPacket,
                    ProcessedEventPacket.DELIVERY_STATE.SENT);

                eventSenderLogger.log(Level.INFO,
                    "An event is about to be send by thie module: "
                                    + this.myModule.getName());

                eventSenderLogger.log(ECGLevel.PACKET, packet.toString());

                this.myModule.getSystemNotificator().fireEventNotification(
                    packet);

                notifyObservers(packet);

                clearChanged();
            }

            eventSenderLogger.exiting(this.getClass().getName(),
                "sendEventPacket");
        }

    }

    /**
     * The <code>EventReceiver</code> is the part of a module that
     * receives events from other modules. It is an <em>Observer</em>
     * and uses the {@link #update(Observable, Object)} method to
     * receive events from orther module's <code>EventSenders</code> it
     * has been registered to. Only <em>INTERMEDIATE_MODULES</em> or
     * <em>TARGET_MODULES</em> are having an <code>EventReceiver</code>,
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
                "EventReceiver", new Object[] {module});
        }

        /**
         * @see java.util.Observer#update(java.util.Observable,
         *      java.lang.Object)
         */
        public void update(final Observable object, final Object data) {
            eventReceiverLogger.entering(this.getClass().getName(), "update",
                new Object[] {object, data});

            if (object == null) {
                eventReceiverLogger.log(Level.WARNING,
                    "Parameter object is null. Ignoring event notification.");

                eventReceiverLogger
                    .exiting(this.getClass().getName(), "update");

                return;
            }

            if (data == null) {
                eventReceiverLogger.log(Level.WARNING,
                    "Parameter data is null. Ignoring event notification.");

                eventReceiverLogger
                    .exiting(this.getClass().getName(), "update");

                return;
            }

            if ((object instanceof EventSender)
                && data instanceof ProcessedEventPacket) {

            	ProcessedEventPacket processedEventPacket = (ProcessedEventPacket) data;

            	if (processedEventPacket.getEventPacket() instanceof ValidEventPacket) {
                	
            		ValidEventPacket validEventPacket = (ValidEventPacket)processedEventPacket.getEventPacket();	

                    eventReceiverLogger.log(Level.INFO,
                            "An event has been recieved by the module: "
                                            + this.myModule.getName());
                    eventReceiverLogger.log(ECGLevel.PACKET,
                        validEventPacket.toString());

                	processedEventPacket.setDeliveryState(
                			ProcessedEventPacket.DELIVERY_STATE.RECEIVED);

                    this.myModule.getSystemNotificator().fireEventNotification(
                        processedEventPacket);

                    this.myModule
                        .receiveEventPacket(validEventPacket);
            	}
            	else {
                    eventReceiverLogger.log(Level.WARNING,
                            "An event has been recieved by the Module " + this.myModule.getName() 
                            + " which is not ValidEventPacket: " 
                            + processedEventPacket.toString());
            	}

            }

            eventReceiverLogger.exiting(this.getClass().getName(), "update");

        }

    }

    /**
     * The <code>SystemObserver</code> is the part of each module that
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
                "SystemObserver", new Object[] {module});

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
            systemObserverLogger.entering(this.getClass().getName(), "update",
                new Object[] {object, arg});

            if (object == null) {
                systemObserverLogger.log(Level.WARNING, "object is null");

                systemObserverLogger.exiting(this.getClass().getName(),
                    "update");

                return;
            }

            if (object instanceof ISystem) {
                this.myModule.update();
            }

            systemObserverLogger.exiting(this.getClass().getName(), "update");

        }

    }

    /**
     * The <code>SystemNotificator</code> is used by the module to
     * notify the system about state changes in the module.
     */
    public static final class GuiNotificator extends Observable {

        /**
         * A reference to the sorrounding module.
         */
        private Module myModule;

        /**
         * This is this class' logger.
         */
        private static Logger systemNotificatorlogger = LogHelper
            .createLogger(GuiNotificator.class.getName());

        /**
         * The constructor registers the ECG GUI component with the
         * SystemNotificator.
         * @param module
         *            Is the surrounding module
         */
        private GuiNotificator(final Module module) {
            systemNotificatorlogger.entering(this.getClass().getName(),
                "SystemNotificator", new Object[] {module});

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
        public void fireEventNotification(final ProcessedEventPacket packet) {
            systemNotificatorlogger.entering(this.getClass().getName(),
                "fireEventNotification", new Object[] {packet});

            if (packet == null) {
                systemNotificatorlogger.log(Level.WARNING, "packet is null");

                systemNotificatorlogger.exiting(this.getClass().getName(),
                    "fireEventNotification");

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

        /**
         * Used in a module implementation to tell the GUI to bring up a message
         * dialog with the given message.
         * @param event Is the message
         */
        public void fireMessageNotification(final MessageEvent event) {
            systemNotificatorlogger.entering(this.getClass().getName(),
                "fireMessageNotification");

            setChanged();

            notifyObservers(event);

            clearChanged();

            systemNotificatorlogger.exiting(this.getClass().getName(),
                "fireMessageNotification");
        }

        /**
         * This returns the module of this <code>SystemNotificator</code>.
         * @return The module of this <code>SystemNotificator</code>
         */
        public Module getModule() {
            systemNotificatorlogger.entering(this.getClass().getName(),
                "getModule");

            systemNotificatorlogger.exiting(this.getClass().getName(),
                "getModule", this.myModule);

            return this.myModule;
        }
    }

    /**
     * When a <em>ModuleProperty</em> is changed at runtime by the
     * user, this <em>PropertyListener</em> is notfied about it. Its
     * {@link #update(Observable, Object)} method is calling the
     * abstract method {@link Module#propertyChanged(ModuleProperty)} which handles the property change for the
     * actual module implementation.
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
                "propertyListener", new Object[] {module});

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

            propertyListenerLogger.entering(this.getClass().getName(),
                "update", new Object[] {o, arg});

            if (o == null) {
                propertyListenerLogger.log(Level.WARNING,
                    "The parameter Observable is null.");

                propertyListenerLogger.exiting(this.getClass().getName(),
                    "update");

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

                propertyListenerLogger.exiting(this.getClass().getName(),
                    "update");

                return;
            }

            propertyListenerLogger.exiting(this.getClass().getName(), "update");

        }

    }
}
