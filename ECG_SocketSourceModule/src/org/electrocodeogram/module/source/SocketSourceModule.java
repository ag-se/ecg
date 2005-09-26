package org.electrocodeogram.module.source;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.ModulePropertyException;

/**
 * This module receives event data from multiple client sensors.
 * The communication is done over Sockets. Each new incoming
 * communication request starts a new SocketServerThread which
 * than receives the event data from the new client.
 *
 */
public class SocketSourceModule extends SourceModule
{

	/**
	 * 
	 */
	private static final int DEFAULT_PORT = 22222;

	/**
	 * 
	 */
	protected static final int MAX_PORT = 65536;

	/**
	 * 
	 */
	protected static final int MIN_PORT = 1024;

	private static Logger _logger = LogHelper.createLogger(SocketSourceModule.class.getName());

	private int _port;

	private SocketServer _socketServer;

	/**
	 * @param moduleClassId
	 * @param name
	 */
	public SocketSourceModule(String moduleClassId, String name)
	{
		super(moduleClassId, name);

		_logger.exiting(this.getClass().getName(), "SocketSourceModule");

	}

	/**
	 * @see org.electrocodeogram.module.source.SourceModule#startReader(org.electrocodeogram.module.source.SourceModule)
	 */
	@Override
	public void startReader(SourceModule sourceModule)
	{
		_logger.entering(this.getClass().getName(), "startReader");

		this._socketServer = new SocketServer(sourceModule, this._port);

		this._socketServer.start();

		_logger.exiting(this.getClass().getName(), "startReader");

	}

	public void stopReader()
	{
		_logger.entering(this.getClass().getName(), "stopReader");

		this._socketServer.shutDown();

		this._socketServer = null;

		_logger.exiting(this.getClass().getName(), "stopReader");
	}

	/**
	 * @throws ModulePropertyException 
	 * @see org.electrocodeogram.module.Module#setProperty(java.lang.String, java.lang.String)
	 */
	@Override
	public void setProperty(String propertyName, String propertyValue) throws ModulePropertyException
	{
		_logger.entering(this.getClass().getName(), "setProperty");

		if (propertyName == null)
		{
			_logger.log(Level.WARNING, "The value for the port property must be a number greater than " + MIN_PORT + " and less then " + MAX_PORT + ".");

			throw new ModulePropertyException(
					"The value for the port property must be a number greater than " + MIN_PORT + " and less then " + MAX_PORT + ".");

		}

		if (propertyName.equals("port"))
		{
			_logger.log(Level.INFO, "Request to set the property: " + propertyName);

			try
			{
				int portValue = Integer.parseInt(propertyValue);

				if (portValue > MIN_PORT && portValue < MAX_PORT)
				{
					this._port = portValue;

					_logger.log(Level.INFO, "Property: " + propertyName + " set.");

					if (this._socketServer != null)
					{
						this._socketServer.shutDown();
					}

					this.startReader(this);

				}
				else
				{
					_logger.log(Level.WARNING, "The value for the port property must be a number greater than " + MIN_PORT + " and less then " + MAX_PORT + ".");

					throw new ModulePropertyException(
							"The value for the port property must be a number greater than " + MIN_PORT + " and less then " + MAX_PORT + ".");
				}

			}
			catch (NumberFormatException e)
			{
				_logger.log(Level.WARNING, "The value for the port property must be a number greater than " + MIN_PORT + " and less then " + MAX_PORT + ".");

				throw new ModulePropertyException(
						"The value for the port property must be a number greater than " + MIN_PORT + " and less then " + MAX_PORT + ".");
			}

		}

		_logger.exiting(this.getClass().getName(), "setProperty");
	}

	@Override
	public void analyseCoreNotification()
	{
		_logger.entering(this.getClass().getName(), "analyseCoreNotification");

		// not implemented

		_logger.exiting(this.getClass().getName(), "analyseCoreNotification");
	}

	/**
	 * @see org.electrocodeogram.module.Module#initialize()
	 */
	@Override
	public void initialize()
	{
		_logger.entering(this.getClass().getName(), "initialize");

		this._port = DEFAULT_PORT;

		_logger.exiting(this.getClass().getName(), "initialize");
	}

}