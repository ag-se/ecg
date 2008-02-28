package org.electrocodeogram.cpc.sensor.team.rp;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.RepositoryProviderType;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.ui.IStartup;
import org.electrocodeogram.cpc.sensor.team.rp.listener.CPCTeamListener;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class CPCSensorTeamRPPlugin extends Plugin implements IStartup
{
	private static Log log = LogFactory.getLog(CPCSensorTeamRPPlugin.class);

	// The plug-in ID
	public static final String PLUGIN_ID = "org.electrocodeogram.cpc.sensor.team.rp";

	static public final int DIFFCHANGE_INTERVAL = 250;

	// The shared instance
	private static CPCSensorTeamRPPlugin plugin;

	//private static final String LOG_PROPERTIES_FILE = "logging.properties";
	//	private ILogManager logManager;

	private CPCTeamListener subscriberChangeListener;

	/**
	 * The constructor
	 */
	public CPCSensorTeamRPPlugin()
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

		subscriberChangeListener = new CPCTeamListener();
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
	public static CPCSensorTeamRPPlugin getDefault()
	{
		return plugin;
	}

	//	public static ILogManager getLogManager()
	//	{
	//		return getDefault().logManager;
	//	}

	public void earlyStartup()
	{
		if (Platform.isRunning())
			setupListeners();
		else
			log.info("earlyStartup(): not starting Eclipse event listeners in standalone mode.");
	}

	//TODO: those CPC parts which really need earlyStartup should all check if the user might
	//have disabled earlyStartup execution and warn the user that this will break things.
	/*
	public static boolean isEarlyStartupDisabled() {
		   String plugins = PlatformUI.getPreferenceStore().getString(
		      /*
		       * Copy constant out of internal Eclipse interface
		       * IPreferenceConstants.PLUGINS_NOT_ACTIVATED_ON_STARTUP
		       * so that we are not accessing internal type.
		       * /
		      "PLUGINS_NOT_ACTIVATED_ON_STARTUP");
		   return plugins.indexOf(FavortesPlugin.ID) != -1;
		}
	*/

	private void setupListeners()
	{
		if (log.isTraceEnabled())
			log.trace("setupListeners()");

		/*
		 * Add a SubscriberChangeListener for all projects in our workspace.
		 */
		//		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		//		if (projects != null && projects.length != 0)
		//		{
		//			for (IProject project : projects)
		//				if (project.isAccessible())
		//					setupListeners(project);
		//		}
		for (String providerId : RepositoryProvider.getAllProviderTypeIds())
		{
			log.trace("setupListeners() - registered RepositoryProvider: " + providerId);
			RepositoryProviderType repProvType = RepositoryProviderType.getProviderType(providerId);
			if (repProvType != null)
			{
				/*
				 * TODO:/FIXME: currently this works with the Eclipse CVS team provider but NOT with
				 * the Subversive SVN team provider. (Subclipse is untested so far)
				 * 
				 * A fix for this seems to be pretty simple in Subversive SVN, the authors have been contacted
				 * on 2007-11-19.
				 */
				Subscriber subscriber = repProvType.getSubscriber();
				if (subscriber != null)
				{
					subscriber.addListener(subscriberChangeListener);
				}
				else
				{
					log.warn("setupListeners() - unable to get Subscriber for repository provider id: " + providerId);
				}
			}
			else
			{
				log.warn("setupListeners() - unable to get RepositoryProviderType for repository provider id: "
						+ providerId);
			}
		}

		/*
		 * Add job change listener. 
		 */
		Job.getJobManager().addJobChangeListener(subscriberChangeListener);
	}

	//	private void setupListeners(IProject project)
	//	{
	//		if (log.isTraceEnabled())
	//			log.trace("setupListeners() - project: " + project);
	//
	//		if (RepositoryProvider.isShared(project))
	//		{
	//			log.trace("Project is shared: " + project);
	//
	//			RepositoryProvider rprovider = RepositoryProvider.getProvider(project);
	//			log.trace("RepositoryProvider: " + rprovider);
	//
	//			Subscriber subscriber = rprovider.getSubscriber();
	//			log.trace("Subscriber: " + subscriber);
	//
	//			//RepositoryProviderType.getProviderType("providerid").getSubscriber();
	//
	//			if (subscriber != null)
	//			{
	//				log.trace("registering CPCSubscriberChangeListener...");
	//				subscriber.addListener(subscriberChangeListener);
	//			}
	//		}
	//		else if (log.isTraceEnabled())
	//		{
	//			log.trace("MyResourceDeltaVisitor.visit() - project is not shared: " + project);
	//		}
	//
	//	}

	//	private void configureLogging()
	//	{
	// we can either use a shared ECGEclipseCorePlugin logger or get our own
	// stand alone logger.

	// for now we use a shared logger.
	//		logManager = ECGEclipseCorePlugin.getDefault().getPluginLogManager();
	//		log = this.logManager.getLogger(CPCSensorTeamRPPlugin.class.getName());
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
