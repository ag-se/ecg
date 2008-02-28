package org.electrocodeogram.cpc.track;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.Plugin;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.provider.classification.IClassificationProvider;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.api.provider.similarity.ISimilarityProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.track.IFuzzyPositionToCloneMatchingProvider;
import org.electrocodeogram.cpc.core.api.provider.track.IPositionUpdateStrategyProvider;
import org.electrocodeogram.cpc.track.repository.CloneRepository;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class CPCTrackPlugin extends Plugin
{
	private static Log log = LogFactory.getLog(CPCTrackPlugin.class);

	// The plug-in ID
	public static final String PLUGIN_ID = "org.electrocodeogram.cpc.track";

	// The shared instance
	private static CPCTrackPlugin plugin;

	//private static final String LOG_PROPERTIES_FILE = "logging.properties";
	//	private ILogManager logManager;

	//reference to CloneRepository singelton
	private CloneRepository cloneRepository;

	/**
	 * The constructor
	 */
	public CPCTrackPlugin()
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

		cloneRepository = new CloneRepository((IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				IStoreProvider.class), (ICloneFactoryProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				ICloneFactoryProvider.class), (IClassificationProvider) CPCCorePlugin.getProviderRegistry()
				.lookupProvider(IClassificationProvider.class), (IPositionUpdateStrategyProvider) CPCCorePlugin
				.getProviderRegistry().lookupProvider(IPositionUpdateStrategyProvider.class),
				(ISimilarityProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(ISimilarityProvider.class),
				(IFuzzyPositionToCloneMatchingProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
						IFuzzyPositionToCloneMatchingProvider.class));
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
	public static CPCTrackPlugin getDefault()
	{
		return plugin;
	}

	//	public static ILogManager getLogManager()
	//	{
	//		return getDefault().logManager;
	//	}

	public static CloneRepository getCloneRepository()
	{
		CPCTrackPlugin _default = getDefault();
		if (_default != null)
			return _default.cloneRepository;
		else
		{
			log
					.warn("getCloneRepository() - returning NULL, CPCTrackPlugin is not available (currently shutting down?)");
			return null;
		}
	}

	//	private void configureLogging()
	//	{
	// we can either use a shared ECGEclipseCorePlugin logger or get our own
	// stand alone logger.

	// for now we use a shared logger.
	//		logManager = ECGEclipseCorePlugin.getDefault().getPluginLogManager();
	//		log = this.logManager.getLogger(CPCTrackPlugin.class.getName());
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
