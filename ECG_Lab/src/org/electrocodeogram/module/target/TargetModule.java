package org.electrocodeogram.module.target;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.module.Module;

/**
 * This abstract class shall be subclassed by all target modules that
 * are intended to rite out the event data they receive.
 * The abstract method write is to be implemented to do the actual writing
 *
 */
public abstract class TargetModule extends Module implements ITargetModule
{
	
	private static Logger _logger = LogHelper.createLogger(TargetModule.class.getName()); 
	
	/**
	 * This creates the module.
	 * @param moduleClassId Is the id of this module's class as declare din the ModuleRegistry
	 * @param name Is the name given to this module
	 *
	 */
	public TargetModule(String moduleClassId, String name)
	{
		super(ModuleType.TARGET_MODULE, moduleClassId, name);
		
		_logger.entering(this.getClass().getName(),"TargetModule");
		
		initialize();
		
		_logger.exiting(this.getClass().getName(),"TargetModule");
	}

	/**
	 * @see org.electrocodeogram.module.Module#receiveEventPacket(org.electrocodeogram.event.ValidEventPacket)
	 * In addition to its superclass method this method writes out every event it
	 * receives, by calling the module's write method.
	 */
	@Override
	public void receiveEventPacket(ValidEventPacket eventPacket)
	{
		_logger.entering(this.getClass().getName(),"receiveEventPacket");
		
		if (eventPacket != null)
		{
			_logger.log(Level.INFO,"An event has been received by the TargetModule: " + this.getName());
			
			_logger.log(ECGLevel.PACKET,eventPacket.toString());
			
			write(eventPacket);
			
			_logger.log(Level.INFO,"The event has been writen by the TargetModule: " + this.getName());
		}
		else
		{
			_logger.log(Level.WARNING,"Parameter eventPacket is null.");
		}
		
		_logger.exiting(this.getClass().getName(),"receiveEventPacket");
	}

	/**
	 * This method is to be implemented to do the actual writing of incoming events. 
	 * @param eventPacket Is the incoming event that is to be written out
	 */
	public abstract void write(ValidEventPacket eventPacket);

	/**
	 * @see org.electrocodeogram.module.Module#initialize()
	 */
	@Override
	public abstract void initialize();

	/* (non-Javadoc)
	 * @see org.electrocodeogram.module.target.ITargetModule#startWriter()
	 */
	public abstract void startWriter() throws TargetModuleException;
	
	/* (non-Javadoc)
	 * @see org.electrocodeogram.module.target.ITargetModule#stopWriter()
	 */
	public abstract void stopWriter();
	
}
