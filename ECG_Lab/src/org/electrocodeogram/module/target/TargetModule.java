package org.electrocodeogram.module.target;

import org.electrocodeogram.event.TypedValidEventPacket;
import org.electrocodeogram.module.Module;

/**
 * This abstract class shall be subclassed by all target modules that
 * are intended to rite out the event data they receive.
 * The abstract method write is to be implemented to do the actual writing
 *
 */
public abstract class TargetModule extends Module
{
	

	/**
	 * This creates the module.
	 * @param moduleClassId Is the id of this module's class as declare din the ModuleRegistry
	 * @param name Is the name given to this module
	 *
	 */
	public TargetModule(String moduleClassId, String name)
	{
		super(ModuleType.TARGET_MODULE, moduleClassId, name);
		
		initialize();
		
		this.getLogger().exiting(this.getClass().getName(),"TargetModule");
	}

	/**
	 * @see org.electrocodeogram.module.Module#receiveEventPacket(org.electrocodeogram.event.TypedValidEventPacket)
	 * In addition to its superclass method this method writes out every event it
	 * receives, by calling the module's write method.
	 */
	@Override
	public void receiveEventPacket(TypedValidEventPacket eventPacket)
	{
		this.getLogger().entering(this.getClass().getName(),"receiveEventPacket");
		
		if (eventPacket != null)
		{
			write(eventPacket);
		}
		
		this.getLogger().exiting(this.getClass().getName(),"receiveEventPacket");
	}

	/**
	 * This method is to be implemented to do the actual writing of incoming events. 
	 * @param eventPacket Is the incoming event that is to be written out
	 */
	public abstract void write(TypedValidEventPacket eventPacket);

	/**
	 * @see org.electrocodeogram.module.Module#initialize()
	 */
	@Override
	public abstract void initialize();

}
