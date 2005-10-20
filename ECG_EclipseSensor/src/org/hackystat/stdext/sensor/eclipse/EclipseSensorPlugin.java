package org.hackystat.stdext.sensor.eclipse;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.sensor.eclipse.ECGEclipseSensor;
import org.hackystat.kernel.shell.SensorShell;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * This is the ECG EclipseSensor plug-in. At Eclipse startup this class creates
 * the actual ECG EclipseSensor.
 * 
 */
public class EclipseSensorPlugin extends AbstractUIPlugin implements IStartup
{

	private static EclipseSensorPlugin _plugin;

	private static Logger _logger = LogHelper.createLogger(EclipseSensorPlugin.class.getName());
	
	public static String eclipsePluginDir;

	/**
	 * The constructor creates the PlugIn instance. It is not to be used by developers,
	 * instead it is called from the Eclipse runtime at PlugIn initialisation.
	 * 
	 * @param descriptor
	 *            Is the PlugIn descriptor.
	 */
	@SuppressWarnings( { "deprecation", "deprecation" })
	public EclipseSensorPlugin(IPluginDescriptor descriptor)
	{
		super(descriptor);

		_logger.entering(this.getClass().getName(), "EclipseSensorPlugin");

		EclipseSensorPlugin._plugin = this;

		_logger.exiting(this.getClass().getName(), "EclipseSensorPlugin");
	}

	/**
	 * If an Eclipse PlugIn must be started when Eclipse starts, it has to
	 * implement the IStartup Interface by implementing this method.
	 */
	public void earlyStartup()
	{

		_logger.entering(this.getClass().getName(), "earlyStartup");

		Bundle bundle = Platform.getBundle(this.getDescriptor().getUniqueIdentifier());
		
		Path path = new Path(".");
		
		URL fileURL = Platform.find(bundle, path);
		
		URL url = null;
		
		try
		{
			url = Platform.resolve(fileURL);
			
			eclipsePluginDir = url.getFile();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
		// Create the ECG EclipseSensor
		ECGEclipseSensor.getInstance();
				
		_logger.exiting(this.getClass().getName(), "earlyStartup");
	}

	
	@Override
	public void stop(BundleContext context) throws Exception
	{
		
		_logger.log(Level.INFO,"ECGEclipsePlugin stopping...");
		
		SensorShell.stop();
		
		super.stop(context);
	}
	
	/**
	 * This returns the singleton instance of the ECG EclipseSensor PlugIn. This
	 * method is needed by the HackyStat Eclipse sensor.
	 * 
	 * @return The singleton instance of the ECG EclipseSensor PlugIn
	 */
	public static EclipseSensorPlugin getInstance()
	{
		_logger.entering(EclipseSensorPlugin.class.getName(), "getInstance");

		_logger.exiting(EclipseSensorPlugin.class.getName(), "getInstance");

		return _plugin;
	}

	/**
	 * This returns the current Eclipse workspace.
	 * 
	 * @return The current Eclipse workspace
	 */
	public static IWorkspace getWorkspace()
	{
		_logger.entering(EclipseSensorPlugin.class.getName(), "getWorkspace");

		_logger.exiting(EclipseSensorPlugin.class.getName(), "getWorkspace");

		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * This method can be used to log an Exception into the Eclipse "Error Log".
	 * 
	 * @param e
	 *            Is an Excpetion to log
	 */
	@SuppressWarnings("deprecation")
	public void log(Exception e)
	{
		if(e == null)
		{
			return;
		}
		
		IStatus status = new Status(IStatus.ERROR,
				this.getDescriptor().getUniqueIdentifier(), 0, e.getMessage(),
				e);
		_plugin.getLog().log(status);
	}

}
