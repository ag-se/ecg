package org.electrocodeogram.module.source;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.system.Core;

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

	private static Logger _logger = LogHelper.createLogger(SourceModule.class.getName());


	/**
	 * This creates the SourceModule.
	 * @param moduleClassId Is the id of the module's class as defined in the ModuleRegistry
	 * @param name Is the name given to this module instance
	 */
	public SourceModule(String moduleClassId, String name)
	{
		super(ModuleType.SOURCE_MODULE, moduleClassId, name);

		_logger.entering(this.getClass().getName(), "SourceModule");

		_logger.exiting(this.getClass().getName(), "SourceModule");

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
	public abstract void startReader(SourceModule sourceModule) throws SourceModuleException;

	/**
	 * This method is used to decalare whether event data that does not conform to
	 * a ECG MicroSensorDataType is allowed to pass validation.
	 * A value of "false" is ignored if the value for allowing non HackyStat conform
	 * event data is set to "true".
	 * @param allowNonECGmSDTConformEvents Is "true" if event data that does not conform to
	 * a ECG MicroSensorDataType is allowed and "false" if not
	 */
	public void setAllowNonECGmSDTConformEvents(boolean allowNonECGmSDTConformEvents)
	{
		_logger.entering(this.getClass().getName(), "setAllowNonECGmSDTConformEvents");

		//this.eventValidator.setAllowNonECGmSDTConformEvents(allowNonECGmSDTConformEvents);

		_logger.exiting(this.getClass().getName(), "setAllowNonECGmSDTConformEvents");
	}

	/**
	 * This method is used to declare whether event data that does not conform to
	 * a HackyStat SensorDataType is allowed to pass validation.
	 * @param allowNonHackyStatSDTConformEvents Is "true" if event data that does not conform to
	 * a HackyStat SensorDataType is allowed and "false" if not
	 */

	public void setAllowNonHackyStatSDTConformEvents(boolean allowNonHackyStatSDTConformEvents)
	{
		_logger.entering(this.getClass().getName(), "setAllowNonHackyStatSDTConformEvents");

		//this.eventValidator.setAllowNonHackyStatSDTConformEvents(allowNonHackyStatSDTConformEvents);

		_logger.exiting(this.getClass().getName(), "setAllowNonHackyStatSDTConformEvents");
	}

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
	public void append(WellFormedEventPacket eventPacket)
	{
		_logger.entering(this.getClass().getName(), "append");

		if (eventPacket == null)
		{
			_logger.log(Level.WARNING, "Parameter eventPacket is null. Ignoring event.");

			return;
		}

		ValidEventPacket validEventPacket = null;
		
		try
		{
			validEventPacket = new ValidEventPacket(this.getId(),eventPacket.getTimeStamp(),eventPacket.getSensorDataType(),eventPacket.getArglist());
			
			_logger.log(Level.INFO,"An event has been appended to the SourceModule: " + this.getName());
			
			_logger.log(ECGLevel.PACKET,validEventPacket.toString());
		}
		catch (IllegalEventParameterException e)
		{
			_logger.log(Level.WARNING, "An Exception occured while appending an event to the SourceModule: " + this.getName());
		}

		if (validEventPacket != null)
		{
			sendEventPacket(validEventPacket);
		}

		_logger.exiting(this.getClass().getName(), "append");
	}

	/**
	 * This method is not implemented for a SourceModule.
	 * @param eventPacket not used
	 */
	@Override
	public final void receiveEventPacket(@SuppressWarnings("unused")
	ValidEventPacket eventPacket)
	{
		_logger.entering(this.getClass().getName(), "receiveEventPacket");

		_logger.exiting(this.getClass().getName(), "receiveEventPacket");

		return;
	}

	/**
	 * @see org.electrocodeogram.module.Module#initialize()
	 */
	@Override
	public abstract void initialize();
}
