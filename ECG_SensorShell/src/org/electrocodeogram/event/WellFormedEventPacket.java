package org.electrocodeogram.event;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.logging.LogHelper.ECGLevel;

/**
 * A ValidEventPacket is a subclass of EventPacket. The data
 * in a ValidEventPacket has been checked for compliance
 * with the syntactical rules for event data.
 * So a ValidEventPacket can be trusted to be syntactically valid.
 */
public class WellFormedEventPacket extends EventPacket
{

	private static Logger _logger = LogHelper.createLogger(WellFormedEventPacket.class.getName());

	public static final String MICRO_ACTIVITY_PREFIX = "MicroActivity#";
	
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

	public static final String MICRO_ACTIVITY_SUFFIX = "#MicroActivity";

	/**
	 * This creates a new EventPacket object
	 * @param id The module source ID identifies where the EventPacket comes from
	 * @param timeStamp The timeStamp tells when the event was recorded
	 * @param sensorDataType The HackyStat SensorDataType of the event
	 * @param argList The argList of parameters containing all the relevant event data
	 * @throws IllegalEventParameterException If the given parameters are not conforming to the syntactical MPE rules
	 */
	public WellFormedEventPacket(int id, Date timeStamp, String sensorDataType, List argList) throws IllegalEventParameterException
	{
		super(id, timeStamp, sensorDataType, argList);

		_logger.entering(this.getClass().getName(), "ValidEventPacket");

		validate();

		_logger.exiting(this.getClass().getName(), "ValidEventPacket");

	}

	/**
	 * This method checks the syntactically correctness of an EventPacket.
	 * @param timeStamp The timeStamp tells when the event was recorded
	 * @param commandName The HackyStat The HackyStat SensorDataType of the event
	 * @param argList The argList of parameters containing all the relevant event data
	 * @return "true" if the EventPacket is syntactically correct and "false" if not
	 * @throws IllegalEventParameterException 
	 */
	private void validate() throws IllegalEventParameterException
	{
		_logger.entering(EventPacket.class.getName(), "validate");

		_logger.log(ECGLevel.PACKET,this.toString());
		
		if (this.getTimeStamp() == null)
		{
			_logger.log(Level.FINE, "The event is not wellformed. The timestamp is null.");
			
			_logger.exiting(EventPacket.class.getName(), "validate");

			throw new IllegalEventParameterException("The event is not wellformed");
		}

		if (this.getSensorDataType() == null)
		{
			_logger.log(Level.FINE, "The event is not wellformed. The SensorDataType is null.");
			
			_logger.exiting(EventPacket.class.getName(), "validate");
			
			throw new IllegalEventParameterException("The event is not wellformed");
		}

		if (this.getArglist() == null)
		{

			_logger.log(Level.FINE, "The event is not wellformed. The argList is null.");
			
			_logger.exiting(EventPacket.class.getName(), "validate");
			
			throw new IllegalEventParameterException("The event is not wellformed");
		}

		if (this.getArglist().isEmpty())
		{

			_logger.log(Level.FINE, "The event is not wellformed. The argList is empty.");
			
			_logger.exiting(EventPacket.class.getName(), "validate");
			
			throw new IllegalEventParameterException("The event is not wellformed");
		}

		if (!(this.getArglist().get(0) instanceof String))
		{
			_logger.log(Level.FINE, "The event is not wellformed. The argList is no List<String>.");

			_logger.exiting(EventPacket.class.getName(), "validate");

			throw new IllegalEventParameterException("The event is not wellformed");

		}

		_logger.log(Level.FINE, "The event is wellformed.");
		
		_logger.exiting(EventPacket.class.getName(), "validate");

		

	}
	
}
