package org.electrocodeogram.module.source;

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

	private int port;

	private SocketServer socketServer;

	/**
	 * @param moduleClassId
	 * @param name
	 */
	public SocketSourceModule(String moduleClassId, String name)
	{
		super(moduleClassId, name);
		
		this.getLogger().exiting(this.getClass().getName(),"SocketSourceModule");
		
	}

	/**
	 * @see org.electrocodeogram.module.source.SourceModule#startReader(org.electrocodeogram.module.source.SourceModule)
	 */
	@Override
	public void startReader(SourceModule sourceModule)
	{
		this.getLogger().entering(this.getClass().getName(),"startReader");
		
		this.socketServer = new SocketServer(sourceModule, this.port);

		this.socketServer.start();
		
		this.getLogger().exiting(this.getClass().getName(),"startReader");

	}
	
	public void stopReader()
	{
		this.getLogger().entering(this.getClass().getName(),"stopReader");
		
		this.socketServer.shutDown();
		
		this.socketServer = null;
		
		this.getLogger().exiting(this.getClass().getName(),"stopReader");
	}

	/**
	 * @throws ModulePropertyException 
	 * @see org.electrocodeogram.module.Module#setProperty(java.lang.String, java.lang.String)
	 */
	@Override
	public void setProperty(String propertyName, String propertyValue) throws ModulePropertyException
	{
		if (propertyName.equals("port"))
		{
			try
			{
				int portValue = Integer.parseInt(propertyValue);

				if (portValue > 1024 && portValue < 65536)
				{
					this.port = portValue;

					if(this.socketServer != null)
					{
						this.socketServer.shutDown();
					}

					this.startReader(this);

				}
				else
				{
					throw new ModulePropertyException(
						"The value for the port property must be a number greater than 1024 and less then 65536.");
				}

			}
			catch (NumberFormatException e)
			{
				throw new ModulePropertyException(
						"The value for the port property must be a number greater than 1024 and less then 65536.");
			}

		}
	}

	public void analyseCoreNotification()
	{

	}

	/**
	 * @see org.electrocodeogram.module.Module#initialize()
	 */
	@Override
	public void initialize()
	{
		this.getLogger().entering(this.getClass().getName(),"initialize");
		
		this.port = 22222;

		//this.startReader(this);

		this.getLogger().exiting(this.getClass().getName(),"initialize");
	}

	/**
	 * @see org.electrocodeogram.module.Module#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String propertyName)
	{
		this.getLogger().entering(this.getClass().getName(),"getProperty");
		
		if (propertyName.equals("port"))
		{
			return "" + this.port;
		}

		this.getLogger().exiting(this.getClass().getName(),"getProperty");
		
		return null;
	}

}