package org.electrocodeogram.event;

import java.util.Date;
import java.util.List;

import org.electrocodeogram.msdt.MicroSensorDataType;

/**
 *
 */
public class TypedValidEventPacket extends ValidEventPacket
{
	/**
	 * This constant integer value is the location of the HackyStat ActivityType value in the event's argList.
	 */
	public static int ACTIVITY_TYPE_INDEX = 1;

	/**
	 * This constant integer value gives the location of the MicroActivity in the event's argList.
	 */
	public static int MICROACTIVITY_INDEX = 2;

	private static final long serialVersionUID = -2907957495470756557L;

	private MicroSensorDataType $msdt = null;

	/**
	 * This creates a TypedValidEventPacket
	 * @param sourceId Is the id of the module that sent this packet
	 * @param timeStamp Is the timeStamp of the event
	 * @param sensorDataType Is the HackyStat SensorDataType of the event. It is always the "Activity" type in a TypedValidEventPacket.
	 * @param argList Is the argList of the event
	 * @param msdt Is the MicroSensorDataType of the event 
	 * @throws IllegalEventParameterException If the parameters are invalid
	 */
	public TypedValidEventPacket(int sourceId, Date timeStamp, String sensorDataType, List argList, MicroSensorDataType msdt) throws IllegalEventParameterException
	{
		super(sourceId, timeStamp, sensorDataType, argList);

		this.$msdt = msdt;
	}

	/**
	 * This method returns the MicroSensorDataType of the MicroActivity event that
	 * is packet in this TypedValidEventPacket.
	 * @return The MicroSensorDataType of the MicroActivity
	 */
	public MicroSensorDataType getMicroSensorDataType()
	{
		return this.$msdt;
	}

}
