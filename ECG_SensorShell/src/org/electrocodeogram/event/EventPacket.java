package org.electrocodeogram.event;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * An EventPacket is a for a recorded event.
 */
public class EventPacket implements Serializable
{
	private static final long serialVersionUID = 2353171166739768704L;

	private static Logger _logger = LogHelper.createLogger(EventPacket.class.getName());

	private int sourceId = -1;

	private Date _timeStamp = null;

	private String _sensorDataType = null;

	private List _argList = null;

	/**
	 * This creates a new EventPacket object
	 * @param id The module source ID identifies where the EventPacket comes from
	 * @param timeStamp The timeStamp tells when the event was recorded
	 * @param sensorDataType The HackyStat SensorDataType of the event
	 * @param argList The argList of parameters containing all the relevant event data
	 */
	public EventPacket(int id, Date timeStamp, String sensorDataType, List argList)
	{

		_logger.entering(this.getClass().getName(), "EventPacket");

		this.sourceId = id;

		if (timeStamp != null)
		{
			this._timeStamp = new Date(timeStamp.getTime());
		}

		this._sensorDataType = sensorDataType;

		this._argList = argList;

		_logger.exiting(this.getClass().getName(), "EventPacket");

	}

	/**
	 * This method returns the ID that identifies the source module of this EventPacket object.
	 * @return The ID of the source module
	 */
	public int getSourceId()
	{
		_logger.entering(this.getClass().getName(), "getSourceId");

		_logger.exiting(this.getClass().getName(), "getSourceId");

		return this.sourceId;
	}

	/**
	 * This method returns the timestamp of the EventPacket.
	 * @return The timestamp as a Date object
	 */
	public Date getTimeStamp()
	{
		_logger.entering(this.getClass().getName(), "getTimeStamp");

		if (this._timeStamp != null)
		{
			_logger.exiting(this.getClass().getName(), "getTimeStamp");

			return new Date(this._timeStamp.getTime());
		}

		_logger.exiting(this.getClass().getName(), "getTimeStamp");

		return null;
	}

	/**
	 * This method returns the HackyStat SensorDataType of the event.
	 * @return The HackyStat SensorDataType
	 */
	public String getSensorDataType()
	{
		_logger.entering(this.getClass().getName(), "getSensorDataType");

		_logger.exiting(this.getClass().getName(), "getSensorDataType");

		return this._sensorDataType;
	}

	/**
	 * This method returns the argList of the EventPacket
	 * @return The argList as a List
	 */
	public List getArglist()
	{
		_logger.entering(this.getClass().getName(), "getArglist");

		_logger.exiting(this.getClass().getName(), "getArglist");

		return this._argList;
	}

	/**
	 * This method checks the syntactically correctness of an EventPacket.
	 * @param timeStamp The timeStamp tells when the event was recorded
	 * @param commandName The HackyStat The HackyStat SensorDataType of the event
	 * @param argList The argList of parameters containing all the relevant event data
	 * @return "true" if the EventPacket is syntactically correct and "false" if not
	 */
	public static boolean isValid(Date timeStamp, String commandName, List argList)
	{
		_logger.entering(EventPacket.class.getName(), "isValid");

		if (timeStamp == null)
		{
			_logger.log(Level.INFO, "Packet is not valid");

			_logger.log(Level.FINEST, "timeStamp is null");

			_logger.exiting(EventPacket.class.getName(), "isValid");

			return false;
		}

		if (commandName == null)
		{
			_logger.log(Level.INFO, "Packet is not valid");

			_logger.log(Level.FINEST, "commandName is null");

			_logger.exiting(EventPacket.class.getName(), "isValid");

			return false;
		}

		if (argList == null)
		{

			_logger.log(Level.INFO, "Packet is not valid");

			_logger.log(Level.FINEST, "argList is null");

			_logger.exiting(EventPacket.class.getName(), "isValid");

			return false;
		}

		if (argList.isEmpty())
		{

			_logger.log(Level.INFO, "Packet is not valid");

			_logger.log(Level.FINEST, "argList is empty");

			_logger.exiting(EventPacket.class.getName(), "isValid");

			return false;
		}

		if (!(argList.get(0) instanceof String))
		{
			_logger.log(Level.INFO, "Packet is not valid");

			_logger.log(Level.FINEST, "argList is not of type String");

			_logger.exiting(EventPacket.class.getName(), "isValid");

			return false;

		}

		_logger.log(Level.INFO, "Packet is valid");

		_logger.exiting(EventPacket.class.getName(), "isValid");

		return true;

	}

	/**
	 * This method returns a String representation of the EventPacket.
	 * @return A String representation of the EventPacket
	 * 
	 */
	@Override
	public String toString()
	{
		_logger.entering(this.getClass().getName(), "isValid");

		String string = "";

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				ValidEventPacket.DATE_FORMAT_PATTERN);

		String dateString = dateFormat.format(this.getTimeStamp());

		string += dateString + "#";

		string += this.getSensorDataType() + "#";

		StringBuffer stringBuffer = new StringBuffer();

		for (int i = 0; i < this.getArglist().size(); i++)
		{
			stringBuffer.append(";");

			stringBuffer.append((String) this.getArglist().get(i));
		}

		_logger.exiting(this.getClass().getName(), "isValid");

		return string + stringBuffer.toString();
	}

	/**
	 * This method compares a given EventPacket with this EventPacket.
	 * @param packet Is the EventPacket to compare
	 * @return "true" if the two EventPackets have identical timeStamps, SensorDataTypes and argLists; "false" otherwise
	 */
	public boolean isEqual(Object packet)
	{
		_logger.entering(this.getClass().getName(), "equals");

		boolean equals = false;

		if (!(packet instanceof EventPacket))
		{
			_logger.log(Level.FINEST, "Object is not an EventPacket");

			_logger.exiting(this.getClass().getName(), "equals");

			return false;
		}

		EventPacket eventPacket = (EventPacket) packet;

		if (this.getTimeStamp().equals(eventPacket.getTimeStamp()) && this.getSensorDataType().equals(eventPacket.getSensorDataType()))
		{
			if (this.getArglist().size() == eventPacket.getArglist().size())
			{
				int size = eventPacket.getArglist().size();

				for (int i = 0; i < size; i++)
				{
					String testString = (String) this.getArglist().get(i);

					String receivedString = (String) eventPacket.getArglist().get(i);

					if (testString.equals(receivedString))
					{
						equals = true;
					}
					else
					{
						equals = false;
					}
				}
			}

		}

		_logger.exiting(this.getClass().getName(), "equals");

		return equals;

	}
}
