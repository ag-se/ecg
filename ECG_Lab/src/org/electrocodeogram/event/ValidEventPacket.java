package org.electrocodeogram.event;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.system.SystemRoot;
import org.electrocodeogram.xml.ECGParser;
import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.kernel.sdt.SdtManager;
import org.hackystat.kernel.sdt.SensorDataType;
import org.hackystat.kernel.sdt.SensorDataTypeException;
import org.hackystat.kernel.sensordata.SensorDataEntryFactory;
import org.hackystat.kernel.shell.OfflineManagerClearer;
import org.hackystat.kernel.shell.SensorShell;
import org.hackystat.kernel.shell.command.ShellCommand;
import org.hackystat.stdext.activity.sdt.Activity;
import org.hackystat.stdext.build.sdt.Build;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * A TypedValidEventPacket is a ValidEventPacket which has a recognized and
 * valid MicroSensorDataType.
 */
public class ValidEventPacket extends WellFormedEventPacket
{
	private static Logger _logger = LogHelper.createLogger(ValidEventPacket.class.getName());

	/**
	 * This constant integer value is the location of the HackyStat ActivityType
	 * value in the event's argList.
	 */
	public static final int ACTIVITY_TYPE_INDEX = 1;

	/**
	 * This constant integer value gives the location of the MicroActivity in
	 * the event's argList.
	 */
	public static final int MICROACTIVITY_INDEX = 2;

	private static final long serialVersionUID = -2907957495470756557L;

	private MicroSensorDataType _msdt = null;

	public enum VALIDITY_LEVEL
	{
		INVALID,

		HACKYSTAT,

		ECG
	}

	public final static VALIDITY_LEVEL DEFAULT_VALIDITY_LEVEL = VALIDITY_LEVEL.ECG;

	private static VALIDITY_LEVEL _validityLevel = DEFAULT_VALIDITY_LEVEL;

	/**
	 * The delivery state is telling wether an event that is passed to the GUI
	 * for display packet was received or sent by a module.
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

	private Document _document;

	/**
	 * This creates a TypedValidEventPacket
	 * 
	 * @param sourceId
	 *            Is the id of the module that sent this packet
	 * @param timeStamp
	 *            Is the timeStamp of the event
	 * @param sensorDataType
	 *            Is the HackyStat SensorDataType of the event. It is always the
	 *            "Activity" type in a TypedValidEventPacket.
	 * @param argList
	 *            Is the argList of the event
	 * @param msdt
	 *            Is the MicroSensorDataType of the event
	 * @throws IllegalEventParameterException
	 *             If the parameters are invalid
	 */
	public ValidEventPacket(int sourceId, Date timeStamp, String sensorDataType, List argList) throws IllegalEventParameterException
	{
		super(sourceId, timeStamp, sensorDataType, argList);

		_logger.entering(this.getClass().getName(), "TypedValidEventPacket");

		validate(this);

		_logger.exiting(this.getClass().getName(), "TypedValidEventPacket");
	}

	public static VALIDITY_LEVEL getValidityLevel()
	{
		return _validityLevel;
	}

	public static void setValidityLevel(VALIDITY_LEVEL validityLevel)
	{
		_validityLevel = validityLevel;

	}

	private void validate(ValidEventPacket packet) throws IllegalEventParameterException
	{

		_logger.entering(this.getClass().getName(), "validate");

		if (_validityLevel == VALIDITY_LEVEL.INVALID)
		{
			_logger.log(Level.FINE, "Packet is valid with respect to level " + _validityLevel.toString());

			_logger.log(Level.FINER, this.toString());

			_logger.exiting(this.getClass().getName(), "validate");

			return;
		}

		if (_validityLevel == VALIDITY_LEVEL.HACKYSTAT)
		{
			new WellFormedEventPacket(this.getSourceId(), this.getTimeStamp(),this.getSensorDataType(), this.getArglist());

			List<String> entryList = new ArrayList<String>();

			List argList = packet.getArglist();

			String tool = "ECG Validator";

			long timestamp = packet.getTimeStamp().getTime();

			entryList.add(new Long(timestamp).toString());

			entryList.add(tool);

			for (Object elem : argList)
			{
				if (elem instanceof String)
				{
					String str = (String) elem;

					entryList.add(str);
				}
				else
				{
					throw new IllegalEventParameterException(
							"The data is illegal.");
				}

			}

			String sensorType = packet.getSensorDataType();

			try
			{
				SensorDataEntryFactory.getEntry(sensorType, entryList);
			}
			catch (SensorDataTypeException e)
			{
				throw new IllegalEventParameterException(e.getMessage());
			}

			_logger.log(Level.FINE, "Packet is valid with respect to level " + _validityLevel.toString());

			_logger.log(Level.FINER, this.toString());

			_logger.exiting(this.getClass().getName(), "validate");

			return;
		}

		else if (_validityLevel == VALIDITY_LEVEL.ECG)
		{

			if (packet == null)
			{
				_logger.log(Level.FINE, "Packet is invalid with respect to level " + _validityLevel.toString());

				_logger.log(Level.FINER, this.toString());

				_logger.exiting(this.getClass().getName(), "validate");

				throw new IllegalEventParameterException(
						"Packet is invalid with respect to level " + _validityLevel.toString());
			}

			new WellFormedEventPacket(this.getSourceId(), this.getTimeStamp(),
					this.getSensorDataType(), this.getArglist());

			if (!packet.getSensorDataType().equals(WellFormedEventPacket.HACKYSTAT_ACTIVITY_STRING))
			{
				_logger.log(Level.FINE, "Packet is invalid with respect to level " + _validityLevel.toString());

				_logger.log(Level.FINER, this.toString());

				_logger.exiting(this.getClass().getName(), "validate");

				throw new IllegalEventParameterException(
						"Packet is invalid with respect to level " + _validityLevel.toString());

			}

			List argList = packet.getArglist();

			String microActivityType = (String) argList.get(1);

			if (microActivityType == null || microActivityType.equals(""))
			{
				_logger.log(Level.FINE, "Packet is invalid with respect to level " + _validityLevel.toString());

				_logger.log(Level.FINER, this.toString());

				_logger.exiting(this.getClass().getName(), "validate");

				throw new IllegalEventParameterException(
						"Packet is invalid with respect to level " + _validityLevel.toString());

			}

			if (!microActivityType.startsWith(WellFormedEventPacket.MICRO_ACTIVITY_PREFIX))
			{
				_logger.log(Level.FINE, "Packet is invalid with respect to level " + _validityLevel.toString());

				_logger.log(Level.FINER, this.toString());

				_logger.exiting(this.getClass().getName(), "validate");

				throw new IllegalEventParameterException(
						"Packet is invalid with respect to level " + _validityLevel.toString());
			}

			microActivityType = microActivityType.substring(WellFormedEventPacket.MICRO_ACTIVITY_PREFIX.length());

			String microActivityString = (String) argList.get(2);

			if (microActivityString == null || microActivityString.equals(""))
			{

				_logger.log(Level.FINE, "Packet is invalid with respect to level " + _validityLevel.toString());

				_logger.log(Level.FINER, this.toString());

				_logger.exiting(this.getClass().getName(), "validate");

				throw new IllegalEventParameterException(
						"Packet is invalid with respect to level " + _validityLevel.toString());

			}

			MicroSensorDataType[] microSensorDataTypes = SystemRoot.getSystemInstance().getSystemMsdtRegistry().getMicroSensorDataTypes();

			if (microSensorDataTypes == null || microSensorDataTypes.length == 0)
			{

				_logger.log(Level.FINE, "Packet is invalid with respect to level " + _validityLevel.toString());

				_logger.log(Level.FINER, this.toString());

				_logger.exiting(this.getClass().getName(), "validate");

				throw new IllegalEventParameterException(
						"Packet is invalid with respect to level " + _validityLevel.toString());

			}

			Document document = null;

			File defFile = null;

			for (MicroSensorDataType microSensorDataType : microSensorDataTypes)
			{
				if (microSensorDataType.getName().equals(microActivityType))
				{
					defFile = microSensorDataType.getDefFile();

					this._msdt = microSensorDataType;

					break;
				}
			}

			if (defFile == null)
			{
				_logger.log(Level.FINE, "Packet is invalid with respect to level " + _validityLevel.toString());

				_logger.log(Level.FINER, this.toString());

				_logger.exiting(this.getClass().getName(), "validate");

				throw new IllegalEventParameterException(
						"Packet is invalid with respect to level " + _validityLevel.toString());

			}

			try
			{
				document = ECGParser.parseAsMicroActivity(microActivityString, defFile.getAbsolutePath());

				this._document = document;

				_logger.log(Level.FINE, "Packet is valid with respect to level " + _validityLevel.toString());

				_logger.log(Level.FINER, this.toString());

				_logger.exiting(this.getClass().getName(), "validate");

				return;
			}
			catch (Exception e)
			{
				_logger.log(Level.FINE, "Packet is invalid with respect to level " + _validityLevel.toString());

				_logger.log(Level.FINER, this.toString());

				_logger.log(Level.FINE, "Going for next MSDT if any.");
			}

		}

		_logger.log(Level.FINE, "Packet is invalid with respect to level " + _validityLevel.toString());

		_logger.log(Level.FINER, this.toString());

		_logger.exiting(this.getClass().getName(), "validate");

		throw new IllegalEventParameterException(
				"Packet is invalid with respect to level " + _validityLevel.toString());

	}

	/**
	 * This method returns the MicroSensorDataType of the MicroActivity event
	 * that is packet in this TypedValidEventPacket.
	 * 
	 * @return The MicroSensorDataType of the MicroActivity
	 */
	public MicroSensorDataType getMicroSensorDataType()
	{
		_logger.entering(this.getClass().getName(), "getMicroSensorDataType");

		_logger.exiting(this.getClass().getName(), "getMicroSensorDataType");

		return this._msdt;
	}

	/**
	 * This returns the delivery state of this event.
	 * 
	 * @return the delivery state of this event
	 */
	public DELIVERY_STATE getDeliveryState()
	{
		_logger.entering(this.getClass().getName(), "getDeliveryState");

		_logger.exiting(this.getClass().getName(), "getDeliveryState");

		return this._deliveryState;
	}

	/**
	 * This sets the delivery state of this event.
	 * 
	 * @param state
	 *            Is the delivery state
	 */

	public void setDeliveryState(DELIVERY_STATE state)
	{
		_logger.entering(this.getClass().getName(), "setDeliveryState");

		if (state == null)
		{
			_logger.log(Level.WARNING, "state is null");

			return;
		}

		this._deliveryState = state;

		_logger.exiting(this.getClass().getName(), "setDeliveryState");
	}

	public Document getDocument()
	{
		return this._document;
	}
}
