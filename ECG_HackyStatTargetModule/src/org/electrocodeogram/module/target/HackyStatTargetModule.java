package org.electrocodeogram.module.target;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;

import org.electrocodeogram.event.TypedValidEventPacket;
import org.electrocodeogram.module.ModuleProperty;
import org.electrocodeogram.module.ModulePropertyException;
import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.kernel.shell.SensorShell;

/**
 *
 */
public class HackyStatTargetModule extends TargetModule
{

	private SensorShell _shell;
	
	private SensorProperties _properties;
	
	private String _host;
	
	private String _key;
	
	/**
	 * @param arg0
	 * @param arg1
	 */
	public HackyStatTargetModule(String arg0, String arg1)
	{
		super(arg0, arg1);
		
		this.getLogger().exiting(this.getClass().getName(), "HackyStatTargetModule");

	}

	/**
	 * @see org.electrocodeogram.module.target.TargetModule#write(org.electrocodeogram.event.TypedValidEventPacket)
	 */
	@Override
	public void write(TypedValidEventPacket arg0)
	{
		if(this._shell == null)
		{
			return;
		}
			
		this._shell.doCommand(arg0.getTimeStamp(),arg0.getSensorDataType(),arg0.getArglist());
		
		this._shell.send();
		
	}

	/**
	 * @param propertyName 
	 * @param propertyValue 
	 * @throws ModulePropertyException 
	 * 
	 */
	@Override
	public void setProperty(String propertyName, String propertyValue) throws ModulePropertyException
	{
		if(propertyName.equals("HackyStat Host"))
		{
			this._host = propertyValue;
		}
		else if(propertyName.equals("HackyStat Admin Key"))
		{
			this._key = propertyValue;
		}
		else
		{
			throw new ModulePropertyException("The property " + propertyName + " is not supported.");
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
		this.getLogger().entering(this.getClass().getName(), "initialize");

		
		this.getLogger().exiting(this.getClass().getName(), "initialize");
	}

	/**
	 * @see org.electrocodeogram.module.Module#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String propertyName)
	{
		this.getLogger().entering(this.getClass().getName(), "getProperty");

		this.getLogger().exiting(this.getClass().getName(), "getProperty");

		return null;
	}
	
	public void startWriter() throws TargetModuleException
	{
		if(this._host == null)
		{
			throw new TargetModuleException("The HackyStat host property is not set yet.");
		}
		
		if(this._key == null)
		{
			throw new TargetModuleException("The HackyStat admin key property is not set yet.");
		}
		
		this._properties = new SensorProperties(this._host,this._key);
		
		this._shell = new SensorShell(this._properties,false,"ElectroCodeoGram",false);
		
		
	}
	
	public void stopWriter()
	{
		
	}
}
