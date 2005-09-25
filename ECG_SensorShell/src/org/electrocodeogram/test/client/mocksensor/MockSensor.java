package org.electrocodeogram.test.client.mocksensor;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.EventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.kernel.shell.SensorShell;

/**
 * This is the ECG TestSensor used for automated JUnit tests. It is capable of
 * generating valid and different kinds of invalid EventPackets and it defines
 * methods to send these EventPackets.
 */
public class MockSensor
{

	private static Logger _logger = LogHelper.createLogger(MockSensor.class.getName());

	private SensorShell _shell = null;

	private SensorProperties _properties = null;

	protected Date sendingTime = null;

	/**
	 * Creates a TestSensor instance and initializes it with a SensorShell.
	 *
	 */
	public MockSensor()
	{
		_logger.entering(this.getClass().getName(), "MockSensor");

		this._properties = new SensorProperties("TestSensor");

		this._shell = new SensorShell(this._properties, false, "");

		_logger.exiting(this.getClass().getName(), "MockSensor");
	}

	/**
	 * This method passes a single given EventPacket to the ECG SensorShell.
	 * 
	 * @param eventPacket
	 *            The EventPacket to pass
	 * @return The result as given by the ECG SensorShell. "true" means the
	 *         EventPacket is syntactically valid and accepted. "false" means
	 *         the EventPacket is syntactically invalid and not accepted.
	 */
	public boolean sendEvent(EventPacket eventPacket)
	{
		_logger.entering(this.getClass().getName(), "sendEvent");

		if (eventPacket == null)
		{
			_logger.log(Level.FINEST, "eventPacket is null");

			_logger.exiting(this.getClass().getName(), "sendEvent");

			return false;

		}

		this.sendingTime = new Date();

		boolean result = this._shell.doCommand(eventPacket.getTimeStamp(), eventPacket.getSensorDataType(), eventPacket.getArglist());

		_logger.exiting(this.getClass().getName(), "sendEvent");

		return result;
	}

	public Date getSendingTime()
	{
		_logger.entering(this.getClass().getName(), "getSendingTime");

		Date toReturn = this.sendingTime;

		this.sendingTime = null;

		_logger.exiting(this.getClass().getName(), "getSendingTime");

		return toReturn;
	}

}
