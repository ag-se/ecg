package org.electrocodeogram.msdt.validation;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Validator;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.TypedValidEventPacket;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.msdt.registry.ISystemMsdtRegistry;
import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.kernel.shell.OfflineManagerClearer;
import org.hackystat.kernel.shell.SensorShell;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class provides the functionality to verify that syntactically valid
 * ValidEventPacket objects are according to HackyStat SensorDataTypes and to
 * ECG MicroSensorDataTypes.
 */
public class EventValidator
{
	private static Logger _logger = LogHelper.createLogger(EventValidator.class.getName());

	private ISystemMsdtRegistry _msdtRegistry = null;

	private SensorShell _sensorShell;

	private boolean _allowNonHackyStatSDTConformEvents = false;

	private boolean _allowNonECGmSDTConformEvents = false;

	private int _count = 0;

	/**
	 * This creates a EventValidator object.
	 * 
	 * @param msdtRegistry
	 *            Is the MicroSensorDataType-Manager (MsdtManager) object that
	 *            keeps the MicroSensorDataType XML schema definitions which are used
	 *            to validate the MicroActivities against.
	 */
	public EventValidator(ISystemMsdtRegistry msdtRegistry)
	{
		_logger.entering(this.getClass().getName(), "EventValidator");

		this._sensorShell = new SensorShell(new SensorProperties("", ""),
				false, "ElectroCodeoGram", false);

		_logger.log(Level.INFO, "Created the HackyStat SensorShell.");

		this._msdtRegistry = msdtRegistry;

		_logger.exiting(this.getClass().getName(), "EventValidator");

	}

	/**
	 * This method checks if a given ValidEventPacket object complies to
	 * HackyStat and ECG standards. Checking is done in a sequence from
	 * the weakest condition to the strongest.
	 * First the event data is checked by the HackyStat SensorShell component
	 * for comliance to a HackyStat SensorDataType.
	 * If positive the event data is checked to be a HackyStat "Activity"
	 * event.
	 * If positive the event data is chekced to be an ECG "MicroActivity"
	 * event.
	 * At last the event data is checked for compliance to a ECG MicroSensorDataType.
	 * Only if the last stage checking is positive this method returns "true".
	 * 
	 * @param packet
	 *            Is the ValidEventPacket object to check
	 * @return "true" if the event data is according to a HackyStat
	 *         SensorDataType and an ECG MicroSensorDataType
	 */
	public TypedValidEventPacket validate(ValidEventPacket packet)
	{
		_logger.entering(this.getClass().getName(), "validate");

		TypedValidEventPacket toReturn;

		if (this._allowNonHackyStatSDTConformEvents)
		{
			try
			{
				toReturn = new TypedValidEventPacket(-1, packet.getTimeStamp(),
						packet.getSensorDataType(), packet.getArglist(), null);

				_logger.exiting(this.getClass().getName(), "validate");

				return toReturn;
			}
			catch (IllegalEventParameterException e1)
			{
				_logger.log(Level.WARNING, "An Exception occured while validating an event.");
			}
		}
		/*
		 * Is the incoming event according to a HackyStat SensorDataType?
		 */
		//boolean isHackyStatSensorDataTypeConform = this._sensorShell.doCommand(packet.getTimeStamp(), packet.getSensorDataType(), packet.getArglist());

//		this._count++;
//
//		if (this._count == 100)
//		{
//			this._sensorShell = new SensorShell(new SensorProperties("", ""),
//					false, "ElectroCodeoGram", false);
//
//			OfflineManagerClearer.clearOfflineManager();
//
//			System.gc();
//
//			this._count = 0;
//		}

		//boolean isHackyStatSensorDataTypeConform = true;

		//if (isHackyStatSensorDataTypeConform)
		if(true)
		{

			_logger.log(Level.INFO, "Event is conforming to a HackyStat SensorDataType and is processed.");

			_logger.log(Level.FINEST, packet.toString());

			if (this._allowNonECGmSDTConformEvents)
			{
				try
				{
					toReturn = new TypedValidEventPacket(0,
							packet.getTimeStamp(), packet.getSensorDataType(),
							packet.getArglist(), null);

					_logger.exiting(this.getClass().getName(), "validate");

					return toReturn;
				}
				catch (IllegalEventParameterException e)
				{
					_logger.log(Level.WARNING, "An Exception occured while validating an event.");

					_logger.exiting(this.getClass().getName(), "validate");

					return null;
				}
			}

			if (isActivityEvent(packet))
			{
				if (isMicroActivityEvent(packet))
				{
					toReturn = isMicroSensorDataType(packet);

					_logger.exiting(this.getClass().getName(), "validate");

					return toReturn;

				}
			}

		}

		_logger.exiting(this.getClass().getName(), "validate");

		return null;
	}

	/**
	 * This method checks if an ValidEventPacket is containing a ECG "MicroActivity" event.
	 * @param packet Is the ValidEventPacket to check
	 * @return "true" if the packet is a "MicroActivity" event and "false" if not
	 */
	private boolean isMicroActivityEvent(ValidEventPacket packet)
	{
		_logger.entering(this.getClass().getName(), "isMicroActivityEvent");

		if (packet == null)
		{
			_logger.log(Level.WARNING, "packet is null");

			_logger.exiting(this.getClass().getName(), "isMicroActivityEvent");

			return false;
		}

		if (packet.getArglist().get(1).equals("MicroActivity"))
		{
			_logger.log(Level.INFO, "The event is an ECG \"MicroActivity\" event.");

			_logger.exiting(this.getClass().getName(), "isMicroActivityEvent");

			return true;
		}

		_logger.log(Level.INFO, "The event is not an ECG \"MicroActivity\" event.");

		_logger.exiting(this.getClass().getName(), "isMicroActivityEvent");

		return false;
	}

	/**
	 * This method checks if an ValidEventPacket is containing a HackyStat "Activity" event.
	 * @param packet Is the ValidEventPacket to check
	 * @return "true" if the packet is an "Activity" event and "false" if not
	 */
	private boolean isActivityEvent(ValidEventPacket packet)
	{

		_logger.entering(this.getClass().getName(), "isActivityEvent");

		if (packet == null)
		{
			_logger.log(Level.WARNING, "packet is null");

			_logger.exiting(this.getClass().getName(), "isActivityEvent");

			return false;
		}

		if (packet.getSensorDataType().equals("Activity"))
		{
			_logger.log(Level.INFO, "The event is a HackyStat \"Activity\" event.");

			_logger.exiting(this.getClass().getName(), "isActivityEvent");

			return true;
		}

		_logger.log(Level.INFO, "The event is not a HackyStat \"Activity\" event.");

		_logger.exiting(this.getClass().getName(), "isActivityEvent");

		return false;
	}

	private TypedValidEventPacket isMicroSensorDataType(ValidEventPacket packet)
	{

		_logger.entering(this.getClass().getName(), "isMicroSensorDataType");

		if (packet == null)
		{
			_logger.log(Level.WARNING, "packet is null");

			_logger.exiting(this.getClass().getName(), "isMicroSensorDataType");

			return null;
		}

		List argList = packet.getArglist();

		String microActivityString = (String) argList.get(2);

		if (microActivityString == null || microActivityString.equals(""))
		{

			_logger.log(Level.INFO, "No MicroActivity data found.");

			_logger.log(Level.INFO, "Event data is not conforming to a HackyStat SensorDataType and is discarded.");

			_logger.log(Level.INFO, packet.toString());

			_logger.exiting(this.getClass().getName(), "isMicroSensorDataType");

			return null;
		}

		MicroSensorDataType[] microSensorDataTypes = this._msdtRegistry.getMicroSensorDataTypes();

		if (microSensorDataTypes == null || microSensorDataTypes.length == 0)
		{

			_logger.log(Level.INFO, "No MicroSensorDataTypes are found.");

			_logger.log(Level.INFO, "Event data is not conforming to a HackyStat SensorDataType and is discarded.");

			_logger.log(Level.INFO, packet.toString());

			_logger.exiting(this.getClass().getName(), "isMicroSensorDataType");

			return null;
		}

		for (int i = 0; i < microSensorDataTypes.length; i++)
		{

			SAXSource saxSource = new SAXSource(new InputSource(
					new StringReader(microActivityString)));

			Validator validator = microSensorDataTypes[i].getSchema().newValidator();

			try
			{

				_logger.log(Level.FINEST, "Validating MicroActivity against " + microSensorDataTypes[i].getName() + " XML schema.");

				validator.validate(saxSource);

				_logger.log(Level.INFO, "The MicroActivity is a valid " + microSensorDataTypes[i].getName() + " event.");

				TypedValidEventPacket typedValidEventPacket = null;

				try
				{
					typedValidEventPacket = new TypedValidEventPacket(0,
							packet.getTimeStamp(), packet.getSensorDataType(),
							packet.getArglist(), microSensorDataTypes[i]);
				}
				catch (IllegalEventParameterException e)
				{
					_logger.log(Level.WARNING, "An Exception occured while validating an event.");
				}

				_logger.exiting(this.getClass().getName(), "isMicroSensorDataType");

				return typedValidEventPacket;
			}
			catch (SAXException e)
			{

				_logger.log(Level.FINEST, "The event could is not valid against a Schema");

				_logger.log(Level.FINEST, e.getMessage());

			}
			catch (IOException e)
			{
				_logger.log(Level.WARNING, "The MicroActivity event could not be read.");

				_logger.log(Level.FINEST, e.getMessage());

			}

		}

		_logger.log(Level.INFO, "The MicroActivity is not conforming to a known MicroSensorDataType.");

		_logger.exiting(this.getClass().getName(), "isMicroSensorDataType");

		return null;
	}

	/**
	 * This method tells wheter event data that does not conform to
	 * a ECG MicroSensorDataType is allowed to pass validation or not.
	 * @return "true" if event data that does not conform to
	 * a ECG MicroSensorDataType is allowed and "false" if not
	 */
	public boolean areNonECGmSDTConformEventsAllowed()
	{
		_logger.entering(this.getClass().getName(), "areNonECGmSDTConformEventsAllowed");

		_logger.exiting(this.getClass().getName(), "areNonECGmSDTConformEventsAllowed");

		return this._allowNonECGmSDTConformEvents;
	}

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

		this._allowNonECGmSDTConformEvents = allowNonECGmSDTConformEvents;

		_logger.exiting(this.getClass().getName(), "setAllowNonECGmSDTConformEvents");
	}

	/**
	 * This method tells wheter event data that does not conform to
	 * a HackyStat SensorDataType is allowed to pass validation or not.
	 * @return "true" if event data that does not conform to
	 * a HackyStat SensorDataType is allowed and "false" if not
	 */

	public boolean areNonHackyStatSDTConformEventsAllowed()
	{
		_logger.entering(this.getClass().getName(), "areNonHackyStatSDTConformEventsAllowed");

		_logger.exiting(this.getClass().getName(), "areNonHackyStatSDTConformEventsAllowed");

		return this._allowNonHackyStatSDTConformEvents;
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

		this._allowNonHackyStatSDTConformEvents = allowNonHackyStatSDTConformEvents;

		_logger.exiting(this.getClass().getName(), "setAllowNonHackyStatSDTConformEvents");
	}

}