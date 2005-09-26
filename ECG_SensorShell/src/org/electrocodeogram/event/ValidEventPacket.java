package org.electrocodeogram.event;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * A ValidEventPacket is a subclass of EventPacket. The data
 * in a ValidEventPacket has been checked for compliance
 * with the syntactical rules for event data.
 * So a ValidEventPacket can be trusted to be syntactically valid.
 */
public class ValidEventPacket extends EventPacket
{

	private static Logger _logger = LogHelper.createLogger(ValidEventPacket.class.getName());

	/**
	 * This constant holds the HackyStat activity-type String which indicates
	 * that an event is in ECG MicroActivity event.
	 */
	public static final String MICRO_ACTIVITY_STRING = "MicroActivity";

	/**
	 * This constand holds the HackyStat add-command, which tells the HackyStat
	 * server to add this event to its list of events.
	 */
	public static final String HACKYSTAT_ADD_COMMAND = "add";

	/**
	 * This constant holds the HackyStat Activity String which indicates that an
	 * event is in HackyStat Activity event. This is also true for all ECG
	 * MicroActivity events.
	 */
	public static final String HACKYSTAT_ACTIVITY_STRING = "Activity";

	/**
	 * This String constant is used to separate the components of the string representation of this event.
	 */
	public static final String EVENT_SEPARATOR = "#";

	/**
	 * This String separates the argList entrys in teh string representation of this event.
	 */
	public static final String ARGLIST_SEPARATOR = ";";

	/**
	 * This is the pattern used to format the timeStamp Date values. The pattern symbols are
	 * accroding to the java.text.DataFormatSymbols class.
	 */
	public static final String DATE_FORMAT_PATTERN = "EE dd.MM.yyyy HH:mm:ss z";

	private static final long serialVersionUID = 2507406265346291700L;

	/**
	 * This creates a new EventPacket object
	 * @param id The module source ID identifies where the EventPacket comes from
	 * @param timeStamp The timeStamp tells when the event was recorded
	 * @param sensorDataType The HackyStat SensorDataType of the event
	 * @param argList The argList of parameters containing all the relevant event data
	 * @throws IllegalEventParameterException If the given parameters are not conforming to the syntactical MPE rules
	 */
	public ValidEventPacket(int id, Date timeStamp, String sensorDataType, List argList) throws IllegalEventParameterException
	{
		super(id, timeStamp, sensorDataType, argList);

		_logger.entering(this.getClass().getName(), "ValidEventPacket");

		if (!isValid(timeStamp, sensorDataType, argList))
		{
			_logger.log(Level.INFO, "EventPacket is not valid");

			throw new IllegalEventParameterException();
		}

		if (id < 0)
		{
			_logger.log(Level.INFO, "EventPacket is not valid");

			throw new IllegalEventParameterException();
		}

		_logger.exiting(this.getClass().getName(), "ValidEventPacket");

	}

}
