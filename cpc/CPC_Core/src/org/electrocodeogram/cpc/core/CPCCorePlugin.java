package org.electrocodeogram.cpc.core;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.electrocodeogram.cpc.core.api.cfg.registry.IConfigurationRegistry;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubRegistry;
import org.electrocodeogram.cpc.core.api.hub.registry.IManagableEventHubRegistry;
import org.electrocodeogram.cpc.core.api.provider.IProvider;
import org.electrocodeogram.cpc.core.api.provider.registry.IManagableProviderRegistry;
import org.electrocodeogram.cpc.core.api.provider.registry.IProviderRegistry;
import org.electrocodeogram.cpc.core.preferences.CPCPreferenceConstants;
import org.electrocodeogram.cpc.core.utils.CPCCoreDebugThread;
import org.electrocodeogram.cpc.core.utils.CoreUtils;
import org.osgi.framework.BundleContext;


/**
 * This singleton class represents the central integration point of the <em>CPC Framework</em>.
 * Static methods on this class can be used to obtain references to the three key elements
 * of the <em>CPC Framework</em>:
 * <ul>
 * 	<li>{@link #getProviderRegistry()}<br>
 * 		Yields an instance of the currently active {@link IProviderRegistry}.
 * 		<em>CPC</em> modules use this class to register and obtain implementations for specific
 * 		API interfaces.<br>
 * 		The service provider concept is an essential part of the <em>CPC Framework</em>.
 * 	<li>{@link #getEventHubRegistry()}<br>
 * 		Yields and instance of the currently active {@link IEventHubRegistry}.
 * 		<em>CPC</em> modules use the event hub to exchange {@link CPCEvent}s in a
 * 		flexible and decoupled manner.<br>
 * 		The event hub concept is an essential part of the <em>CPC Framework</em>.
 * 	<li>{@link #getConfigurationRegistry()}<br>
 * 		Yields and instance of the currently active {@link IConfigurationRegistry}.
 * 		<em>CPC</em> modules use the configuration registry to obtain global configuration data.
 * </ul>
 * 
 * This class is also the activator class for the <em>CPC Core</em> module.
 * 
 * @author vw
 * 
 * @see #getProviderRegistry()
 * @see IProviderRegistry
 * @see #getEventHubRegistry()
 * @see IEventHubRegistry
 * @see #getConfigurationRegistry()
 * @see IConfigurationRegistry
 * @see org.electrocodeogram.cpc.core.api
 * @see org.electrocodeogram.cpc.core.utils
 */
public class CPCCorePlugin extends AbstractUIPlugin implements IPropertyChangeListener
{
	private static Log log = LogFactory.getLog(CPCCorePlugin.class);

	/**
	 * The plug-in ID of the CPC Core plug-in.
	 */
	public static final String PLUGIN_ID = "org.electrocodeogram.cpc.core";

	private static final String EXTENSION_POINT_PROVIDER_REGISTRY = PLUGIN_ID + ".providerRegistry";
	private static final String EXTENSION_POINT_EVENT_HUB_REGISTRY = PLUGIN_ID + ".eventHubRegistry";
	private static final String EXTENSION_POINT_CONFIGURATION_REGISTRY = PLUGIN_ID + ".configurationRegistry";

	// The shared instance
	private static CPCCorePlugin plugin;

	//private static final String LOG_PROPERTIES_FILE = "logging.properties";
	//	private ILogManager logManager;

	private IManagableProviderRegistry providerRegistry;

	private IManagableEventHubRegistry eventHubRegistry;
	private IConfigurationRegistry configurationRegistry;
	private CPCCoreDebugThread debugThread;

	/**
	 * Cached debug checking preference value.
	 * 
	 * @see CPCPreferenceConstants#PREF_DEBUG_MODE_ENABLED
	 */
	private static boolean debugChecking = false;

	/**
	 * The default constructor.
	 * <br>
	 * Called by the <em>Eclipse Platform</em>, a client should <b>never</b> create instances of this class.
	 */
	public CPCCorePlugin()
	{
		plugin = this;

		//		configureLogging();
		log.info("using shared logging");

		log.trace("trace enabled");

		initialiseRegistries();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception
	{
		super.start(context);

		getPluginPreferences().addPropertyChangeListener(this);
		debugChecking = getPluginPreferences().getBoolean(CPCPreferenceConstants.PREF_DEBUG_MODE_ENABLED);

		debugThread = new CPCCoreDebugThread();
		debugThread.start();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception
	{
		//stop all event dispatching
		eventHubRegistry.shutdown();

		//tell all registered providers about the shutdown
		providerRegistry.shutdown();

		debugThread.shutdown();

		//needed if we only extend Plugin
		//savePluginPreferences();

		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared singleton instance of the <em>CPCCorePlugin</em>.
	 *
	 * @return the shared instance, usually not null. However, if called
	 * before the <em>CPC Core</em> startup sequence has completed or during
	 * shutdown, NULL may be returned.
	 */
	public static CPCCorePlugin getDefault()
	{
		if (plugin == null)
		{
			log
					.warn("getDefault() - trying to access CPCCorePlugin after shutdown or before startup.",
							new Throwable());
		}
		return plugin;
	}

	/**
	 * Retrieves the underlying shared {@link ILogManager} used by the <em>CPC Core</em> module.
	 * <br>
	 * Refer to the <em>ECG EclipseCore</em> module for more details.
	 * 
	 * @return shared log manager for the <em>CPC Core</em> plugin, may be NULL.
	 */
	//	public static ILogManager getLogManager()
	//	{
	//		if (getDefault() == null)
	//			return null;
	//
	//		return getDefault().logManager;
	//	}
	/**
	 * Yields an instance of the currently active {@link IProviderRegistry}.
	 * <br>
	 * <em>CPC</em> modules use this class to register and obtain implementations for specific
	 * API interfaces.
	 * <br>
	 * The service provider concept is an essential part of the <em>CPC Framework</em>.
	 * 
	 * @return current {@link IProviderRegistry}, usually not null. However, if called
	 * before the <em>CPC Core</em> startup sequence has completed or during
	 * shutdown, NULL may be returned.
	 * 
	 * @see IProviderRegistry
	 * @see IProvider
	 */
	public static IProviderRegistry getProviderRegistry()
	{
		if (getDefault() == null)
			return null;

		return getDefault().providerRegistry;
	}

	/**
	 * Yields and instance of the currently active {@link IEventHubRegistry}.
	 * <br>
	 * <em>CPC</em> modules use the event hub to exchange {@link CPCEvent}s in a
	 * flexible and decoupled manner.
	 * <br>
	 * The event hub concept is an essential part of the <em>CPC Framework</em>.
	 * 
	 * @return current {@link IEventHubRegistry}, usually not null. However, if called
	 * before the <em>CPC Core</em> startup sequence has completed or during
	 * shutdown, NULL may be returned.
	 * 
	 * @see IEventHubRegistry
	 * @see IEventHubListener
	 * @see CPCEvent
	 */
	public static IEventHubRegistry getEventHubRegistry()
	{
		if (getDefault() == null)
			return null;

		return getDefault().eventHubRegistry;
	}

	/**
	 * Yields and instance of the currently active {@link IConfigurationRegistry}.
	 * <br>
	 * <em>CPC</em> modules use the configuration registry to obtain global configuration data.
	 * 
	 * @return current {@link IEventHubRegistry}, usually not null. However, if called
	 * before the <em>CPC Core</em> startup sequence has completed or during
	 * shutdown, NULL may be returned.
	 *
	 * @see IConfigurationRegistry
	 */
	public static IConfigurationRegistry getConfigurationRegistry()
	{
		if (getDefault() == null)
			return null;

		return getDefault().configurationRegistry;
	}

	/**
	 * Configures internal logger which underlies the commons logging implementation.
	 */
	//	private void configureLogging()
	//	{
	// we can either use a shared ECGEclipseCorePlugin logger or get our own
	// stand alone logger.
	// for now we use a shared logger.
	//		logManager = ECGEclipseCorePlugin.getDefault().getPluginLogManager();
	//		log = this.logManager.getLogger(CPCCorePlugin.class.getName());
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
	/**
	 * Initialises the Provider and Event Hub Registries with the classes registered for the
	 * corresponding extension point. If multiple classes are registered for one registry,
	 * the one with the highest priority is used.
	 */
	private void initialiseRegistries()
	{
		log.trace("initialiseRegistries(): parsing extension point data");

		try
		{
			IConfigurationElement element = CoreUtils.getHighestPriorityExtensionFor(EXTENSION_POINT_PROVIDER_REGISTRY);
			providerRegistry = (IManagableProviderRegistry) element.createExecutableExtension("class");

			element = CoreUtils.getHighestPriorityExtensionFor(EXTENSION_POINT_EVENT_HUB_REGISTRY);
			eventHubRegistry = (IManagableEventHubRegistry) element.createExecutableExtension("class");

			element = CoreUtils.getHighestPriorityExtensionFor(EXTENSION_POINT_CONFIGURATION_REGISTRY);
			configurationRegistry = (IConfigurationRegistry) element.createExecutableExtension("class");
		}
		catch (Exception e)
		{
			log.fatal("initialiseRegistries() - error while initialising registries - " + e, e);
		}

		if (log.isTraceEnabled())
			log.trace("initialiseRegistries() - providerRegistry: " + providerRegistry + ", eventHubRegistry: "
					+ eventHubRegistry);
	}

	/**
	 * Checks whether additional internal consistency checkings should be performed.
	 * <br>
	 * Does not directly affect debug logging.
	 */
	public static boolean isDebugChecking()
	{
		return debugChecking;
	}

	/**
	 * Toggles some internal consistency checking on/off. Setting this value to
	 * <em>true</em> has serious performance implications.
	 * <br>
	 * Does not directly affect debug logging.
	 * 
	 * @param debugChecking <em>true</em> if additional consistency checks should be
	 * performed, <em>false</em> otherwise.
	 */
	public static void setDebugChecking(boolean debugChecking)
	{
		if (log.isTraceEnabled())
			log.trace("setDebugChecking() - debugChecking: " + debugChecking);

		CPCCorePlugin.debugChecking = debugChecking;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Preferences$IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
	 * 
	 * Listen for preference changes to keep our cached debugChecking value up to date.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		if (event.getProperty().equals(CPCPreferenceConstants.PREF_DEBUG_MODE_ENABLED))
		{
			debugChecking = getPluginPreferences().getBoolean(CPCPreferenceConstants.PREF_DEBUG_MODE_ENABLED);

			if (log.isTraceEnabled())
			{
				log.trace("propertyChange() - debug checking now set to: " + debugChecking);
			}
		}
	}
}
