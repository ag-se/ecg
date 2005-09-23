package org.electrocodeogram.module.source;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.TypedValidEventPacket;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.msdt.validation.EventValidator;
import org.electrocodeogram.system.SystemRoot;

/**
 * This is the abstract class SourceModule that shall be subclassed
 * by all source modules. The abstract method startReader is to be implemented
 * in the actual source module.
 * It shall create an object that is able to receive and read ValidEventPacket
 * objects. Every read event shall then be passed back to this module's append
 * method. 
 */
public abstract class SourceModule extends Module
{

	private EventValidator eventValidator = null;

	/**
	 * This creates the SourceModule.
	 * @param moduleClassId Is the id of the module's class as defined in the ModuleRegistry
	 * @param name Is the name given to this module instance
	 */
	public SourceModule(String moduleClassId, String name)
	{
		super(ModuleType.SOURCE_MODULE, moduleClassId, name);

		this.eventValidator = new EventValidator(
				SystemRoot.getSystemInstance().getSystemMsdtRegistry());

		this.getLogger().exiting(this.getClass().getName(), "SourceModule");

		initialize();
	}

	/**
	 * This method shall create an object able to read in ValidEventPacket objects.
	 * For example one could implement a reader that reads ValidEventPackets from
	 * a text-file or one could implement a server that receives ValidEventPackets
	 * from different client sources.
	 * The reader must call the SourceModule's append method to pass read/received
	 * ValidEventPackets to the module.
	 * This method is called during SourceModule's
	 * creation and the parameter is passed as a reference back to the SourceModule
	 * itself.
	 * @param sourceModule Is the backward reference to the SourceModule 
	 */
	public abstract void startReader(SourceModule sourceModule);

	/**
	 * 
	 *
	 */
	public abstract void stopReader();
	
	/**
	 * This method is called by the reader implementation to pass over read or
	 * received ValidEventPackets to this SourceModule.
	 * @param eventPacket Is the received or read event
	 */
	public void append(ValidEventPacket eventPacket)
	{
		//this.getLogger().entering(this.getClass().getName(), "append");

			TypedValidEventPacket typedValidEventPacket = this.eventValidator.validate(eventPacket);

			if (typedValidEventPacket != null)
			{
				sendEventPacket(typedValidEventPacket);
			}

		//this.getLogger().exiting(this.getClass().getName(), "append");
	}

	/**
	 * This method is not implemented for a SourceModule.
	 * @param eventPacket not used
	 */
	@Override
	public final void receiveEventPacket(@SuppressWarnings("unused")
	TypedValidEventPacket eventPacket)
	{
		this.getLogger().entering(this.getClass().getName(), "receiveEventPacket");

		this.getLogger().exiting(this.getClass().getName(), "receiveEventPacket");

		return;
	}

	/**
	 * @see org.electrocodeogram.module.Module#initialize()
	 */
	@Override
	public abstract void initialize();
}
