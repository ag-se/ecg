package org.electrocodeogram.eclipse.core;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.electrocodeogram.eclipse.core.logging.ILogManager;
import org.electrocodeogram.eclipse.core.logging.PluginLogManager;
import org.electrocodeogram.eclipse.core.logging.PluginLogRegistry;
import org.electrocodeogram.eclipse.core.logging.StandaloneLogManager;
import org.osgi.framework.BundleContext;


public class ECGEclipseCorePlugin extends Plugin
{
	private static Logger log = null;

	// The plug-in ID
	public static final String PLUGIN_ID = "org.electrocodeogram.eclipse.core";

	private static final String LOG_PROPERTIES_FILE = "logging.properties";
	private ILogManager logManager;

	private static ECGEclipseCorePlugin plugin;
	private PluginLogRegistry pluginLogRegistry;

	public ECGEclipseCorePlugin()
	{
		super();
		plugin = this;
		pluginLogRegistry = new PluginLogRegistry();
	}

	public static ECGEclipseCorePlugin getDefault()
	{
		return plugin;
	}

	public PluginLogRegistry getPluginLogRegistry()
	{
		return pluginLogRegistry;
	}

	@Override
	public void start(BundleContext context) throws Exception
	{
		super.start(context);

		configureLogging();
	}

	/**
	 * Iterates over the list of active log managers and shutdowns each one
	 * before calling the base class implementation.
	 * 
	 * @see Plugin#stop
	 */
	@Override
	public void stop(BundleContext context) throws Exception
	{
		synchronized (this.pluginLogRegistry)
		{
			pluginLogRegistry.stop();
		}
		super.stop(context);
	}

	public ILogManager getPluginLogManager()
	{
		return logManager;
	}

	/*
	 * this method is public on order for standalone applications to be able initialize the logging code 
	 */
	public void configureLogging()
	{
		//logging initialization depends on whether we're being used standalone (testing) or
		//as an eclipse plugin (normal operation)

		if (Platform.isRunning())
		{
			//eclipse mode
			configureLoggingEclipse();
		}
		else
		{
			//standalone mode
			configureLoggingStandalone();
		}
	}

	private void configureLoggingStandalone()
	{
		try
		{
			InputStream propertiesInputStream = null;

			File propsFile = new File(LOG_PROPERTIES_FILE);
			if (propsFile.canRead())
				propertiesInputStream = new FileInputStream(propsFile);

			if (propertiesInputStream != null)
			{
				Properties props = new Properties();
				props.load(propertiesInputStream);
				propertiesInputStream.close();
				this.logManager = new StandaloneLogManager(props);
				/*this.logManager.hookPlugin(
				 TestPlugin.getDefault().getBundle().getSymbolicName(),
				 TestPlugin.getDefault().getLog());*/

				log = this.logManager.getLogger(ECGEclipseCorePlugin.class.getName());
				log.info("Logging Initialized");
			}
			else
			{
				System.err.println("ERROR - failed to load properties file - " + LOG_PROPERTIES_FILE);
			}
		}
		catch (Exception e)
		{
			System.err.println("Error while initializing log properties: " + e.toString());
			e.printStackTrace();
		}
	}

	private void configureLoggingEclipse()
	{
		try
		{
			URL url = getBundle().getEntry("/" + LOG_PROPERTIES_FILE);

			InputStream propertiesInputStream = url.openStream();
			if (propertiesInputStream != null)
			{
				Properties props = new Properties();
				props.load(propertiesInputStream);
				propertiesInputStream.close();
				this.logManager = new PluginLogManager(this, props);
				/*this.logManager.hookPlugin(
				 TestPlugin.getDefault().getBundle().getSymbolicName(),
				 TestPlugin.getDefault().getLog());*/

				log = this.logManager.getLogger(ECGEclipseCorePlugin.class.getName());
				log.info("Logging Initialized");
			}
			else
			{
				System.err.println("ERROR - failed to load properties file - " + LOG_PROPERTIES_FILE);
			}
		}
		catch (Exception e)
		{
			String message = "Error while initializing log properties: " + e.toString();
			System.err.println(message);

			IStatus status = new Status(IStatus.ERROR, getDefault().getBundle().getSymbolicName(), IStatus.ERROR,
					message, e);
			getLog().log(status);
			throw new RuntimeException("Error while initializing log properties.", e);
		}
	}

}
