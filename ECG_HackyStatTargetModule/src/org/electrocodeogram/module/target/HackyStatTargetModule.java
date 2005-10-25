package org.electrocodeogram.module.target;

import java.util.logging.Logger;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.ModuleProperty;
import org.electrocodeogram.module.ModulePropertyException;
import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.kernel.shell.SensorShell;

/**
 * This class is an ECG module used to send ECG events to a HackyStat server.
 */
public class HackyStatTargetModule extends TargetModule
{

    private static Logger logger = LogHelper.createLogger(HackyStatTargetModule.class.getName());
    
	private SensorShell _shell;

	private SensorProperties _properties;

	private String _host;

	private String _key;

	/**
	 * The constructor creates the module instance. It is not to be called by
	 * developers, instead it is called from the ECG ModuleRegistry when the
	 * user requested a new instance of this module.
	 * 
	 * @param id
	 *            This is the unique String id of the module
	 * @param name
	 *            This is the name which is given to the module instance
	 */
	public HackyStatTargetModule(String id, String name)
	{
		super(id, name);

		logger.exiting(this.getClass().getName(), "HackyStatTargetModule");

	}

	/**
	 * @see org.electrocodeogram.module.target.TargetModule#write(org.electrocodeogram.event.TypedValidEventPacket)
	 */
	@Override
	public void write(ValidEventPacket arg0)
	{
		if (this._shell == null)
		{
			return;
		}

		this._shell.doCommand(arg0.getTimeStamp(), arg0.getSensorDataType(), arg0.getArgList());

		this._shell.send();

	}

	/**
	 * @see org.electrocodeogram.module.Module#setProperty(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public void propertyChanged(ModuleProperty moduleProperty) throws ModulePropertyException
	{
		if (moduleProperty.getName().equals("HackyStat Host"))
		{
			this._host = moduleProperty.getValue();
		}
		else if (moduleProperty.getName().equals("HackyStat Admin Key"))
		{
			this._key = moduleProperty.getValue();
		}
	}

	/**
	 * @see org.electrocodeogram.module.Module#analyseCoreNotification() This
	 * The method is not implemented in this module.
	 */
	@Override
	public void analyseCoreNotification()
	{
		// not implemented
	}

	/**
	 * @see org.electrocodeogram.module.Module#initialize()
	 */
	@Override
	public void initialize()
	{
		logger.entering(this.getClass().getName(), "initialize");

		logger.exiting(this.getClass().getName(), "initialize");
	}

	/**
	 * @see org.electrocodeogram.module.target.TargetModule#startWriter() This
	 *      method is not implemented in this module.
	 */
	@Override
	public void startWriter() throws TargetModuleException
	{
		if (this._host == null)
		{
			throw new TargetModuleException(
					"The HackyStat host property is not set yet.", this.getName());
		}

		if (this._key == null)
		{
			throw new TargetModuleException(
					"The HackyStat admin key property is not set yet.", this.getName());
		}

		this._properties = new SensorProperties(this._host, this._key);

		this._shell = new SensorShell(this._properties, false,
				"ElectroCodeoGram", false);

	}

	/**
	 * @see org.electrocodeogram.module.target.TargetModule#stopWriter() This
	 *      method is not implemented in this module.
	 */
	@Override
	public void stopWriter()
	{
		// not implemented
	}
}
