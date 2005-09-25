package org.electrocodeogram.event;

import java.util.Date;
import java.util.List;

import org.electrocodeogram.msdt.MicroSensorDataType;

/**
 * A TypedValidEventPacket is a ValidEventPacket which has a recognized
 * and valid MicroSensorDataType.
 */
public class TypedValidEventPacket extends ValidEventPacket
{
	/**
	 * This constant integer value is the location of the HackyStat ActivityType value in the event's argList.
	 */
	public static final int ACTIVITY_TYPE_INDEX = 1;

	/**
	 * This constant integer value gives the location of the MicroActivity in the event's argList.
	 */
	public static final int MICROACTIVITY_INDEX = 2;

	private static final long serialVersionUID = -2907957495470756557L;

	private MicroSensorDataType _msdt = null;

	/**
	 * The delivery state is telling wether an event that is passed to the GUI for display
	 * packet was received or sent by a module.
	 */
	public enum DELIVERY_STATE
	{
		/**
		 * The event was sent by a module.
		 */
		SENT,

		/**
		 * The event was received by a module. 
		 */
		RECEIVED;
	}

	private DELIVERY_STATE _deliveryState = null;

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

		this._msdt = msdt;
	}

	/**
	 * This method returns the MicroSensorDataType of the MicroActivity event that
	 * is packet in this TypedValidEventPacket.
	 * @return The MicroSensorDataType of the MicroActivity
	 */
	public MicroSensorDataType getMicroSensorDataType()
	{
		return this._msdt;
	}

	/**
	 * This returns the delivery state of this event.
	 * @return the delivery state of this event
	 */
	public DELIVERY_STATE getDeliveryState()
	{
		return this._deliveryState;
	}

	/**
	 * This sets the delivery state of this event.
	 * @param state Is the delivery state
	 */

	public void setDeliveryState(DELIVERY_STATE state)
	{
		this._deliveryState = state;
	}

}
