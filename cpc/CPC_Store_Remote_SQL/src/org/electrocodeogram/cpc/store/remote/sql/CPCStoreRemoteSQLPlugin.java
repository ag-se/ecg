package org.electrocodeogram.cpc.store.remote.sql;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class CPCStoreRemoteSQLPlugin extends Plugin
{
	private static Log log = LogFactory.getLog(CPCStoreRemoteSQLPlugin.class);

	// The plug-in ID
	public static final String PLUGIN_ID = "org.electrocodeogram.cpc.store.remote.sql";

	// The shared instance
	private static CPCStoreRemoteSQLPlugin plugin;

	//private static final String LOG_PROPERTIES_FILE = "logging.properties";
	//	private ILogManager logManager;

	/**
	 * Toggles some internal consistency checking on/off.
	 * Does not directly affect debug logging.
	 */
	//TODO: default value in finished version should be false
	private boolean debugChecking = true;

	/**
	 * The constructor
	 */
	public CPCStoreRemoteSQLPlugin()
	{
		plugin = this;

		//		configureLogging();
		log.info("using shared logging");

		log.trace("trace enabled");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception
	{
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static CPCStoreRemoteSQLPlugin getDefault()
	{
		return plugin;
	}

	//	public static ILogManager getLogManager()
	//	{
	//		return getDefault().logManager;
	//	}

	/**
	 * Toggles some internal consistency checking on/off.
	 * Does not directly affect debug logging.
	 */
	public boolean isDebugChecking()
	{
		return debugChecking;
	}

	public void setDebugChecking(boolean debugChecking)
	{
		this.debugChecking = debugChecking;
	}

	//	private void configureLogging()
	//	{
	// we can either use a shared ECGEclipseCorePlugin logger or get our own
	// stand alone logger.

	// for now we use a shared logger.
	//		logManager = ECGEclipseCorePlugin.getDefault().getPluginLogManager();
	//		log = this.logManager.getLogger(CPCStoreRemoteSQLPlugin.class.getName());
	//		log.info("using shared logging");

	// standalone logger
	/*
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

			log = this.logManager.getLogger(CPCCorePlugin.class.getName());
			log.info("Logging Initialized");
		}
		else
		{
			System.err.println("ERROR - failed to load properties file - " + LOG_PROPERTIES_FILE);
		}
	}
	catch (Exception e)
	{
		String message = "Error while initializing log properties." + e.getMessage();
		System.err.println(message);

		IStatus status = new Status(IStatus.ERROR, getDefault().getBundle().getSymbolicName(), IStatus.ERROR,
				message, e);
		getLog().log(status);
		throw new RuntimeException("Error while initializing log properties.", e);
	}
	*/
	//	}
}
