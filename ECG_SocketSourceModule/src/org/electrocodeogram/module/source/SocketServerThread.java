package org.electrocodeogram.module.source;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;

/**
 * A ServerThread maintains communication with a single ECG sensor.
 * Communication is done by (de)serialization over sockets.
 */
public class SocketServerThread extends Thread
{
	private static Logger _logger = LogHelper.createLogger(SocketServerThread.class.getName());

	private static int _count = 0;

	private int _id = -1;

	private Socket _socketToSensor = null;

	private boolean _run = true;

	private ObjectInputStream _ois = null;

	private ISocketServer _sensorServer = null;

	private String _sensorName = null;

	private SourceModule _sourceModule;

	private Object _object;

	private ValidEventPacket _packet;

	/**
	 * This method returns the name of the currently connected ECG sensor.
	 * @return The name of the currently connected ECG sensor
	 */
	public String getSensorName()
	{
		_logger.entering(this.getClass().getName(), "getSensorName");

		_logger.exiting(this.getClass().getName(), "getSensorName");

		return this._sensorName;
	}

	/**
	 * This method returns the IP address of the currently connected ECG sensor.
	 * @return The IP address of the currently connected ECG sensor
	 */
	public InetAddress getSensorAddress()
	{
		_logger.entering(this.getClass().getName(), "getSensorAddress");

		if (this._socketToSensor != null)
		{
			return this._socketToSensor.getInetAddress();
		}

		_logger.exiting(this.getClass().getName(), "getSensorAddress");

		return null;

	}

	/**
	 * This creates a new ServerThread.
	 * @param sensorServer A reference to the SensorServer that is managing this ServerThread
	 * @param module Is the source module to which the received event data is passed
	 * @param socketToSensor The socket to the ECG sensor
	 * @throws IOException If the creation of the ObjectInputStream fails
	 */
	public SocketServerThread(ISocketServer sensorServer, SourceModule module, Socket socketToSensor) throws IOException
	{
		super();

		_logger.entering(this.getClass().getName(), "SocketServerThread");

		this._sourceModule = module;

		if (module == null)
		{
			_logger.log(Level.SEVERE, "The parameter module is null. Can not create the SocketServerThread");

			return;
		}

		if (sensorServer == null)
		{
			_logger.log(Level.SEVERE, "The parameter sensorServer is null. Can not create the SocketServerThread");

			return;
		}

		if (socketToSensor == null)
		{
			_logger.log(Level.SEVERE, "The parameter socketToSensor is null. Can not create the SocketServerThread");

			return;
		}

		// Assign the ServerThread a unique ID
		this._id = ++_count;

		this._sensorServer = sensorServer;

		this._socketToSensor = socketToSensor;

		this._ois = new ObjectInputStream(socketToSensor.getInputStream());

		_logger.exiting(this.getClass().getName(), "SocketServerThread");
	}

	/**
	 * This method returns the unique ID of the ServerThread.
	 * @return The unique ID of the ServerThread
	 */
	public int getServerThreadId()
	{
		_logger.entering(this.getClass().getName(), "getSensorThreadId");

		_logger.exiting(this.getClass().getName(), "getSensorThreadId");

		return this._id;
	}

	/**
	 * This method stops the the Thread.
	 */
	public void stopSensorThread()
	{
		_logger.entering(this.getClass().getName(), "stopSensorThread");

		this._run = false;

		this._sensorServer.removeSensorThread(this._id);

		try
		{
			if (this._ois != null)
			{
				this._ois.close();
			}
			if (this._socketToSensor != null)
			{
				this._socketToSensor.close();
			}
		}
		catch (IOException e)
		{

			_logger.log(Level.WARNING, "The ServerThread could not be stopped cleanly.");
		}

		_logger.exiting(this.getClass().getName(), "stopSensorThread");
	}

	/**
	 * @see java.lang.Thread#run()
	 * The receiving of event data is implemented here.
	 */
	@Override
	public void run()
	{
		_logger.entering(this.getClass().getName(), "run");

		while (this._run)
		{
			try
			{

				this._object = this._ois.readObject();

				this._packet = (ValidEventPacket) this._object;

				ValidEventPacket packet = new ValidEventPacket(0,
						this._packet.getTimeStamp(),
						this._packet.getSensorDataType(),
						this._packet.getArglist());

				if (this._packet != null)
				{
					this._sourceModule.append(packet);

				}

				/* If the event data contains the "setTool" String, which is giving the name of the application
				 * the sensor runs in, this String is used as the sensor name.
				 */
				if (this._packet.getSensorDataType().equals("Activity") && this._packet.getArglist().get(0).equals("setTool"))
				{
					String tmpSensorName;

					if ((tmpSensorName = (String) this._packet.getArglist().get(1)) != null)
					{
						this._sensorName = tmpSensorName;

					}
				}

			}
			catch (SocketException e)
			{

				this._run = false;

				_logger.log(Level.WARNING, "The socket connection to the ECG Sensor is lost.");
			}
			catch (IOException e)
			{

				_logger.log(Level.WARNING, "Error while reading from the ECG sensor.");
			}
			catch (ClassNotFoundException e)
			{

				_logger.log(Level.WARNING, "Error while reading from the ECG sensor.");

			}
			catch (ClassCastException e)
			{
				_logger.log(Level.ALL, "catch(ClassCastException e)");

				// If something else then a ValidEventPacket is received, we don't care!
			}
			catch (IllegalEventParameterException e)
			{
				_logger.log(Level.WARNING, "Error while reading from the ECG sensor.");

			}
		}

		_logger.exiting(this.getClass().getName(), "run");

	}
}
