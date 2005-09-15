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
import org.electrocodeogram.event.TypedValidEventPacket.DELIVERY_STATE;
import org.electrocodeogram.module.intermediate.IIntermediateModule;
import org.electrocodeogram.module.registry.ModuleClassException;
import org.electrocodeogram.module.registry.ModuleInstanceException;
import org.electrocodeogram.module.source.SourceModule;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.msdt.registry.MicroSensorDataTypeRegistrationException;
import org.electrocodeogram.system.ISystemRoot;
import org.electrocodeogram.system.SystemRoot;

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
public abstract class Module
{

	private Logger _logger;

	/**
	 * These values are indicating the type of the module.
	 */
	public enum ModuleType
	{
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

	private ModuleType _moduleType;

	private static int _count;

	private int _id;

	private String name;

	private HashMap<Integer, Module> _receiverModuleMap;

	private HashMap<Integer, Module> _senderModuleMap;

	private ArrayList<MicroSensorDataType> _providedMsdtList;

	private boolean _activeFlag;

	private String _moduleClassId;

	private EventSender _eventSender;

	private EventReceiver _eventReceiver;

	private SystemObserver _systemObserver;

	SystemNotificator _systemNotificator;

	/**
	 * This creates a new Module of the given module type, module class id ans module name and registers it with the ModuleRegistry.
	 * @param moduleType Is the module type
	 * @param moduleClassId Is the unique id of this module's class as registered with the ModuleRegistry
	 * @param moduleName Is the name to give to this module instance
	 */
	public Module(ModuleType moduleType, String moduleClassId, String moduleName)
	{
		this._id = ++_count;

		this.name = moduleName;

		this._moduleClassId = moduleClassId;

		this._moduleType = moduleType;

		this._logger = Logger.getLogger(this.name);

		this._receiverModuleMap = new HashMap<Integer, Module>();

		this._senderModuleMap = new HashMap<Integer, Module>();

		this._providedMsdtList = new ArrayList<MicroSensorDataType>();

		this._systemObserver = new SystemObserver(this);

		this._systemNotificator = new SystemNotificator();

		if (this._moduleType == ModuleType.SOURCE_MODULE)
		{
			this._eventSender = new EventSender(this);

		}
		else if (this._moduleType == ModuleType.INTERMEDIATE_MODULE)
		{
			this._eventReceiver = new EventReceiver(this);

			this._eventSender = new EventSender(this);
		}
		else
		{
			this._eventReceiver = new EventReceiver(this);
		}

		registerMSDTs();

		SystemRoot.getModuleInstance().getModuleModuleRegistry().registerRunningModule(this);

		activate();

		initialize();
	}

	/**
	 * The EventSender is the part of a Module that sends events to other Modules.
	 * It extends Observable and uses the notfiy method to send events to
	 * registered Observer Modules.
	 * Only Modules with the ModuleType of SOURCE_MODULE or INTERMEDIATE_MODULE
	 * are having a not null EventSender.
	 */
	private static class EventSender extends Observable
	{

		private Module _module;

		/**
		 * This creates the EventSender for the given Module.
		 * @param module Is the Module to which this EventSender is belonging
		 */
		public EventSender(Module module)
		{
			this._module = module;
		}

		/**
		 * This registers a Module with the EventSender for receiving events.
		 * @param module Id the Module to register
		 */
		public void addObserver(Module module)
		{
			super.addObserver(module.getEventReceiver());
		}

		/**
		 * This deregisters a registered Module.
		 * @param module Is the Module to deregister
		 */
		public void deleteObserver(Module module)
		{
			super.deleteObserver(module.getEventReceiver());
		}

		/**
		 * This method is inherited to all extending modules. It sends the given event to
		 * all connected modules. The sourceId attribute of the event is changed to the id
		 * of this the sending module.
		 * @param eventPacket Is the event to send
		 */
		public void sendEventPacket(TypedValidEventPacket eventPacket)
		{
			if (this._module.isActive() && (eventPacket != null))
			{
				setChanged();

				try
				{
					TypedValidEventPacket packet = new TypedValidEventPacket(
							this._module.getId(), eventPacket.getTimeStamp(),
							eventPacket.getSensorDataType(),
							eventPacket.getArglist(),
							eventPacket.getMicroSensorDataType());

					packet.setDeliveryState(DELIVERY_STATE.SENT);

					notifyObservers(packet);

					this._module._systemNotificator.fireEventNotification(packet);
				}
				catch (IllegalEventParameterException e)
				{

					// As only the id of a ValidEventPackets is changed, it has to stay valid

					clearChanged();

					e.printStackTrace();

					this._module.getLogger().log(Level.SEVERE, "An unexpected exception has occurred. Please report this at www.electrocodeogram.org");

				}
				clearChanged();
			}
		}
	}

	/**
	 * The EventReceiver is the part of a Module that receives events from other Modules.
	 * It implements Observer and uses the update method to receive events from
	 * Observable Modules it has been registered to.
	 * Only Modules with the ModuleType of INTERMEDIATE_MODULE or TARGET_MODULE
	 * are having a not null EventReceiver.
	 */
	private static class EventReceiver implements Observer
	{

		private Module _module;

		/**
		 * This creates the EventReceiver for the given Module.
		 * @param module Is the Module that the EventReceiver is belonging to
		 */
		public EventReceiver(Module module)
		{
			this._module = module;
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
		public void update(Observable object, Object data)
		{
			if ((object instanceof EventSender) && data instanceof TypedValidEventPacket)
			{
				try
				{
					TypedValidEventPacket receivedPacketForProcessing = (TypedValidEventPacket) data;

					receivedPacketForProcessing.setDeliveryState(DELIVERY_STATE.RECEIVED);

					TypedValidEventPacket receivedPacketForSystem = new TypedValidEventPacket(
							this._module.getId(),
							receivedPacketForProcessing.getTimeStamp(),
							receivedPacketForProcessing.getSensorDataType(),
							receivedPacketForProcessing.getArglist(),
							receivedPacketForProcessing.getMicroSensorDataType());

					receivedPacketForSystem.setDeliveryState(DELIVERY_STATE.RECEIVED);

					this._module._systemNotificator.fireEventNotification(receivedPacketForSystem);

					this._module.receiveEventPacket(receivedPacketForProcessing);
				}
				catch (IllegalEventParameterException e)
				{
					e.printStackTrace();

					this._module.getLogger().log(Level.SEVERE, "An unexpected exception has occurred. Please report this at www.electrocodeogram.org");

				}

			}

		}

	}

	/**
	 * The SystemObserver is the part of each Module that
	 * gets notifications from the ISystemRoot about system
	 * statechanges.
	 *
	 */
	private static class SystemObserver implements Observer
	{

		private Module $module;

		/**
		 * This creates the SystemObserver for the given Module.
		 * @param module Is the Module that the SystemObserver is belonging to
		 */
		public SystemObserver(Module module)
		{
			this.$module = module;
		}

		/**
		 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
		 */
		public void update(Observable o, @SuppressWarnings("unused")
		Object arg)
		{
			if (o instanceof ISystemRoot)
			{
				this.$module.analyseCoreNotification();
			}

		}

	}

	/**
	 * The SystemNotificator is used by the Module to notify registered Observers
	 * about statechanges in the Module.
	 *
	 */
	private static class SystemNotificator extends Observable
	{

		/**
		 * The constructor registers the ECG GUI component with the SystemNotificator
		 *
		 */
		public SystemNotificator()
		{
			this.addObserver(SystemRoot.getSystemInstance().getGui());

		}

		/**
		 * The method is called to notify the ECG system of a new event beeing processed by thie module.
		 * @param packet Is the last sent event packet.
		 */
		public void fireEventNotification(TypedValidEventPacket packet)
		{
			setChanged();

			notifyObservers(packet);

			clearChanged();

		}

		/**
		 * The method is called to notify the ECG system of state changes in the module.
		 */
		public void fireStatechangeNotification()
		{
			setChanged();

			notifyObservers(this);

			clearChanged();
		}

	}

	private void registerMSDTs()
	{
		if (this instanceof SourceModule)
		{

			MicroSensorDataType[] msdts = SystemRoot.getModuleInstance().getModuleMsdtRegistry().getPredefinedMicroSensorDataTypes();

			for (MicroSensorDataType msdt : msdts)
			{
				try
				{
					SystemRoot.getModuleInstance().getModuleMsdtRegistry().requestMsdtRegistration(msdt, this);

					this._providedMsdtList.add(msdt);
				}
				catch (MicroSensorDataTypeRegistrationException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		ModuleDescriptor moduleDescriptor = null;
		try
		{
			moduleDescriptor = SystemRoot.getModuleInstance().getModuleModuleRegistry().getModuleDescriptor(this._moduleClassId);

			MicroSensorDataType[] microSensorDataTypes = moduleDescriptor.getMicroSensorDataTypes();

			if (microSensorDataTypes != null)
			{

				for (MicroSensorDataType msdt : microSensorDataTypes)
				{
					this._providedMsdtList.add(msdt);

					SystemRoot.getModuleInstance().getModuleMsdtRegistry().requestMsdtRegistration(msdt, this);
				}
			}
		}
		catch (ModuleClassException e)
		{
			this._logger.log(Level.INFO, "No ModuleDescriptor was found for the module " + this.getName());
		}

		catch (MicroSensorDataTypeRegistrationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Returns the module's logger.
	 * @return The module's logger
	 */
	protected Logger getLogger()
	{
		return this._logger;
	}

	/**
	 * This method returns the EventReceiver of the module.
	 * @return The EventReceiver of the module
	 */
	EventReceiver getEventReceiver()
	{
		return this._eventReceiver;
	}

	/**
	 * This method returns the module's SystemObserver, which is an Observer that
	 * handles notifications from the ISystemRoot Observable.
	 * @return The module's SystemObserver
	 */
	public SystemObserver getSystemObserver()
	{
		return this._systemObserver;
	}

	/**
	 * This method returns the unique id of this module.
	 * @return The unique id of this module
	 */
	public int getId()
	{
		return this._id;
	}

	/**
	 * This method returns the ModuleType of this module.
	 * @return The ModuleType of this module
	 */
	public ModuleType getModuleType()
	{
		return this._moduleType;
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
	 * This method returns the number of connected modules.
	 * @return The number of connected modules
	 */
	public int getReceivingModuleCount()
	{
		return this._receiverModuleMap.size();
	}

	/**
	 * This method returns an Array of all modules that are connected to this module.
	 * @return An Array of all modules that are connected to this module
	 */
	public Module[] getReceivingModules()
	{
		Collection<Module> receivingModules = this._receiverModuleMap.values();

		return receivingModules.toArray(new Module[0]);
	}

	private Module[] getSendingModules()
	{

		Collection<Module> sendingModules = this._senderModuleMap.values();

		return sendingModules.toArray(new Module[0]);
	}

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

		String moduleDescription;

		ModuleDescriptor moduleDescriptor = null;
		try
		{
			moduleDescriptor = SystemRoot.getModuleInstance().getModuleModuleRegistry().getModuleDescriptor(this._moduleClassId);
		}
		catch (ModuleClassException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		moduleDescription = moduleDescriptor.getDescription();

		if (moduleDescription != null)
		{
			text += "\nBeschreibung: \t";

			text += moduleDescription;

		}

		return text;

	}

	/**
	 * This method returns the MicroSensorDataTypes that are provided by this module.
	 * @return The MicroSensorDataTypes that are provided by this module
	 */
	public MicroSensorDataType[] getProvidedMicroSensorDataType()
	{
		return this._providedMsdtList.toArray(new MicroSensorDataType[0]);
	}

	/**
	 * This method returns the unique id of the module's class as
	 * registered with the ModuleRegistry.
	 * @return The unique id of the module's class
	 */
	public String getClassId()
	{
		return this._moduleClassId;
	}

	/**
	 * This method returns "true" if the module is active and "false" if not.
	 * @return "true" if the module is active and "false" if not
	 */
	public boolean isActive()
	{
		return this._activeFlag;
	}

	/**
	 * This checks if the module is of the given ModuleType
	 * @param moduleType
	 * @return "true" If the module is of the given ModuleType and "false" if not
	 */
	public boolean isModuleType(ModuleType moduleType)
	{
		if (this._moduleType == moduleType)
		{
			return true;
		}

		return false;

	}

	/**
	 * This method deactivates the module. The module might be already deactivated.
	 *
	 */
	public void deactivate()
	{
		if (this._activeFlag == false) return;

		this._activeFlag = false;

	}

	/**
	 * This method activates the module. The module might be already activated.
	 *
	 */
	public void activate()
	{
		if (this._activeFlag == true) return;

		this._activeFlag = true;

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
	 * This method is called hen the module is to be removed from the ECG Lab.
	 *
	 */
	public void remove()
	{
		if (this._senderModuleMap.size() != 0)
		{

			Module[] parentModules = getSendingModules();

			for (int i = 0; i < parentModules.length; i++)
			{

				Module module = parentModules[i];

				try
				{
					module.disconnectReceiverModule(this);
				}
				catch (ModuleInstanceException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		for (MicroSensorDataType msdt : this._providedMsdtList)
		{
			try
			{
				msdt.removeProvidingModule(this);
			}
			catch (MicroSensorDataTypeRegistrationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try
		{
			SystemRoot.getModuleInstance().getModuleModuleRegistry().deregisterRunningModule(this.getId());
		}
		catch (ModuleInstanceException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Actual module implementations use this method to send analysis results
	 * or other events.
	 * @param packet Is the event packet that contains the analysis result
	 */
	protected void sendEventPacket(TypedValidEventPacket packet)
	{
		this._eventSender.sendEventPacket(packet);
	}

	/**
	 * This abstract method is to be implemented by all actual modules.
	 * Its implementation tells what to do with a received event.
	 * @param eventPacket Is the received event
	 */
	protected abstract void receiveEventPacket(TypedValidEventPacket eventPacket);

	/**
	 * This method is called whenever this module gets a notification of
	 * a statechange form the sytem core.
	 * It is left to the actual module implementation to react on such an event.
	 *
	 */
	public abstract void analyseCoreNotification();

	/**
	 * @param currentPropertyName
	 * @param propertyValue
	 * @throws ModulePropertyException 
	 */
	public abstract void setProperty(String currentPropertyName, Object propertyValue) throws ModulePropertyException;

	/**
	 * This method initializes the actual module. It must be implementes by all module
	 * subclasses.
	 *
	 */
	public abstract void initialize();

	/**
	 * This method is used to connect a given module to this module.
	 * @param module Is the module that should be connected to this module.
	 * @return The id of the connected module
	 * @throws ModuleConnectionException If the given module could not be connected to this module.
	 * This happens if this module is a target module or if the given module is already connected to this module.
	 */
	public int connectReceiverModule(Module module) throws ModuleConnectionException
	{

		if (this._moduleType == ModuleType.TARGET_MODULE)
		{
			throw new ModuleConnectionException(
					"You can not connect another module to this module.");
		}
		else if (this._receiverModuleMap.containsKey(new Integer(module.getId())))
		{
			throw new ModuleConnectionException(
					"These mdoules are connected already.");
		}
		else
		{

			this._eventSender.addObserver(module);

			this._receiverModuleMap.put(new Integer(module._id), module);

			module.addParentModule(this);

			this._systemNotificator.fireStatechangeNotification();

			return module._id;

		}

	}

	/**
	 * This method disconnects a connected module.
	 * @param module The module to disconnect
	 * @throws ModuleInstanceException 
	 */
	public void disconnectReceiverModule(Module module) throws ModuleInstanceException
	{
		if (module == null)
		{
			return;
		}

		if (!this._receiverModuleMap.containsKey(new Integer(module.getId())))
		{
			throw new ModuleInstanceException(
					"The given module id " + this._id + " is unknown.");
		}

		this._eventSender.deleteObserver(module);

		this._receiverModuleMap.remove(new Integer(module.getId()));

		this._systemNotificator.fireStatechangeNotification();

	}

	private void addParentModule(Module module)
	{
		this._senderModuleMap.put(new Integer(module.getId()), module);
	}
}
