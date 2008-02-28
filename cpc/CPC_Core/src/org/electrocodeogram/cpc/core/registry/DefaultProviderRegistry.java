package org.electrocodeogram.cpc.core.registry;


import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.provider.IManagableProvider;
import org.electrocodeogram.cpc.core.api.provider.IProvider;
import org.electrocodeogram.cpc.core.api.provider.registry.IManagableProviderRegistry;
import org.electrocodeogram.cpc.core.api.provider.registry.IProviderDescriptor;
import org.electrocodeogram.cpc.core.api.provider.registry.IProviderRegistry;


/**
 * Default implementation of {@link IProviderRegistry}. Retrieves provider data from the corresponding
 * {@link CPCCorePlugin} extension point if running in Eclipse but also allows manual registration/unregistration.
 * 
 * @author vw
 */
public class DefaultProviderRegistry implements IManagableProviderRegistry, IRegistryChangeListener
{
	private static Log log = LogFactory.getLog(DefaultProviderRegistry.class);
	private static final String EXTENSION_POINT_PROVIDERS = "org.electrocodeogram.cpc.core.providers";

	private Map<String, SortedSet<ProviderDescriptor>> providerRegistry;

	public DefaultProviderRegistry()
	{
		if (log.isTraceEnabled())
			log.trace("PluginRegistry() - initializing...");

		providerRegistry = new HashMap<String, SortedSet<ProviderDescriptor>>();

		//initialisation differs depending on whether we are in standalone or eclipse plugin mode
		if (Platform.isRunning())
		{
			//initialise local provider registry from "providers" extension point
			osgiProviderInitialisation();

			Platform.getExtensionRegistry().addRegistryChangeListener(this, EXTENSION_POINT_PROVIDERS);
		}
		else
		{
			log
					.info("ProviderRegistry() - osgi provider initialization skipped, manual registration of provders required.");
		}

		if (log.isTraceEnabled())
			log.trace("PluginRegistry() - initialization finished");
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.registry.IProviderRegistry#lookupProvider(java.lang.Class)
	 */
	@Override
	public synchronized IProvider lookupProvider(Class<? extends IProvider> type)
	{
		if (log.isTraceEnabled())
			log.trace("lookupProvider() - type: " + type);
		assert (type != null);

		ProviderDescriptor highestProvider = null;

		//check registry
		SortedSet<ProviderDescriptor> providers = providerRegistry.get(type.getName());

		if ((providers != null) && (!providers.isEmpty()))
		{
			//the first provider in the list is always the one with the highest priority
			highestProvider = providers.first();
		}
		else
		{
			//we have no provider of this type?
			log.warn("lookupProvider() - no registered provider found, returning NULL - type: " + type);

			//we might need some additional debug data if this ever happens
			if (log.isTraceEnabled())
				log.trace("lookupProvider() - providerRegistry state: " + providerRegistry);

			return null;
		}

		return lookupProvider(highestProvider);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.registry.IProviderRegistry#lookupProvider(org.electrocodeogram.cpc.core.api.provider.registry.IProviderDescriptor)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public IProvider lookupProvider(IProviderDescriptor extProviderDescriptor)
	{
		if (log.isTraceEnabled())
			log.trace("lookupProvider() - providerDescriptor: " + extProviderDescriptor);
		assert (extProviderDescriptor != null && extProviderDescriptor instanceof ProviderDescriptor);

		ProviderDescriptor providerDescriptor = (ProviderDescriptor) extProviderDescriptor;
		IManagableProvider providerInstance = providerDescriptor.getProvider();

		//check if we need to instantiate a new instance
		if (providerInstance == null)
		{
			if (log.isTraceEnabled())
				log.trace("lookupProvider(): no cached singleton instance for provider - " + providerDescriptor);

			try
			{
				//ok, we don't have an instance yet
				if (Platform.isRunning())
				{
					//try to get an instance from eclipse
					if (providerDescriptor.getConfigurationElement() != null)
					{
						providerInstance = (IManagableProvider) providerDescriptor.getConfigurationElement()
								.createExecutableExtension("class");
					}
					else
					{
						//we should always have a config element
						log.fatal("lookupProvider() - no configuration element available - providerDescriptor: "
								+ providerDescriptor, new Throwable());
					}
				}
				else
				{
					//try to create an instance ourself
					Class providerClass = Class.forName(providerDescriptor.getProviderClass());
					providerInstance = (IManagableProvider) providerClass.newInstance();
				}

				//tell the instance to initialise
				providerInstance.onLoad();
			}
			catch (Exception e)
			{
				log.fatal("lookupProvider() - unable to create provider instance - providerDescriptor: "
						+ providerDescriptor + " - " + e, e);
			}

			if (log.isTraceEnabled())
				log.trace("lookupProvider() - new instance - providerInstance: " + providerInstance);

			//register this instance if singleton mode is enabled
			if ((providerInstance != null) && (providerDescriptor.isSingleton()))
			{
				if (log.isTraceEnabled())
					log.trace("lookupProvider() - registering singleton instance.");

				providerDescriptor.setProvider(providerInstance);
			}
		}

		if (log.isTraceEnabled())
			log.trace("lookupProvider() - result: " + providerInstance);

		return providerInstance;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.registry.IProviderRegistry#lookupProviders(java.lang.Class)
	 */
	@Override
	public synchronized List<IProviderDescriptor> lookupProviders(Class<? extends IProvider> type)
	{
		if (log.isTraceEnabled())
			log.trace("lookupProviders(): " + type);
		assert (type != null);

		List<IProviderDescriptor> result = new LinkedList<IProviderDescriptor>();

		//check registry
		SortedSet<ProviderDescriptor> providers = providerRegistry.get(type.getName());

		if ((providers != null) && (!providers.isEmpty()))
		{
			//we have registered providers of that type
			Iterator<ProviderDescriptor> iter = providers.iterator();
			while (iter.hasNext())
			{
				result.add(iter.next());
			}
		}

		if (log.isTraceEnabled())
			log.trace("lookupProviders() - result: " + result);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.registry.IManagableProviderRegistry#registerProvider(org.electrocodeogram.cpc.core.api.provider.registry.IProviderDescriptor)
	 */
	@Override
	public synchronized void registerProvider(IProviderDescriptor providerDescriptor)
	{
		if (log.isTraceEnabled())
			log.trace("registerProvider(): " + providerDescriptor);
		assert (providerDescriptor != null);

		//we always expect the descriptor to be of type ProviderDescriptor
		if (!(providerDescriptor instanceof ProviderDescriptor))
			throw new IllegalArgumentException("Expecting argument of type ProviderDescriptor");

		SortedSet<ProviderDescriptor> providers = providerRegistry.get(providerDescriptor.getTypeClass());

		// if we don't have any providers of this type yet, add a new list
		if (providers == null)
		{
			providers = new TreeSet<ProviderDescriptor>();
			providerRegistry.put(providerDescriptor.getTypeClass(), providers);
		}
		else if (!providers.isEmpty())
		{
			//there will now be more than one provider for this type
			if (log.isDebugEnabled())
				log.debug("registerProvider(): " + (providers.size() + 1) + " providers for "
						+ providerDescriptor.getTypeClass());
		}

		providers.add((ProviderDescriptor) providerDescriptor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.registry.IManagableProviderRegistry#unregisterProvider(org.electrocodeogram.cpc.core.api.registry.ProviderDescriptor)
	 */
	@Override
	public synchronized boolean unregisterProvider(IProviderDescriptor providerDescriptor)
	{
		if (log.isTraceEnabled())
			log.trace("unregisterProvider(): " + providerDescriptor);
		assert (providerDescriptor != null);

		boolean success = false;

		SortedSet<ProviderDescriptor> providers = providerRegistry.get(providerDescriptor.getTypeClass());

		//make sure we even have any registered providers of this type
		if (providers != null)
		{
			//we can't use remove() here because we don't have the priority for the provider
			//so we just sequentially check all providers of this type
			//(usually there will be very few of them, a good guess would be <=3) 
			Iterator<ProviderDescriptor> iter = providers.iterator();
			while (iter.hasNext())
			{
				ProviderDescriptor entry = iter.next();
				if (entry.equals(providerDescriptor))
				{
					//ok, we found it, this provider needs to be removed
					providers.remove(entry);
					success = true;

					//call the providers onUnload code, if it was already loaded
					if (entry.getProvider() != null)
						entry.getProvider().onUnload();

					//we don't stop here, we want to remove all providers of this provider class
					//break;
				}
			}

		}

		if (!success)
			log.warn("unregisterPlugin() - tried to unregister non-registered provider: " + providerDescriptor);

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.registry.IManagableProviderRegistry#shutdown()
	 */
	@Override
	public synchronized void shutdown()
	{
		if (log.isTraceEnabled())
			log.trace("shutdown() - notifiying all registered providers to shut down");

		//go through all provider types
		for (SortedSet<ProviderDescriptor> providers : providerRegistry.values())
		{
			//check each registered provider
			for (IProviderDescriptor provider : providers)
			{
				assert (provider instanceof ProviderDescriptor);

				//and if it has been instantiated
				if (((ProviderDescriptor) provider).getProvider() != null)
				{
					//notify the provider of the imminent shutdown
					((ProviderDescriptor) provider).getProvider().onUnload();
				}
			}
		}

		if (log.isTraceEnabled())
			log.trace("shutdown() - done");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IRegistryChangeListener#registryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)
	 */
	@Override
	public void registryChanged(IRegistryChangeEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("registryChanged() - event: " + event);

		//TODO: update cached providers here
		//this is somewhat tricky because we may already have to cached instances of providers
		//elsewhere and we may also have created instances of singleton providers which we
		//must not instantiate again, before we terminate the old instances.
	}

	/**
	 * Reads data for all extensions which were registered for the <em>providers</em> extension point and
	 * converts them into the internal data format of the provider registry.
	 * 
	 * TODO: depending on the performance we might actually always do this and skip the
	 * 		internal storage part.
	 * 		This would give dynamically loaded/unloaded plugins a chance to interface with CPC.
	 * 		
	 * 		However, that would mean that multiple lookups could yield
	 * 		different instances of the provider. Which would probably be a bad thing.
	 * 		That could be worked around of course, but we'd still have to handle priorities here then.
	 */
	private void osgiProviderInitialisation()
	{
		log.trace("osgiProviderLookup(): building provider registry from extension data");

		IConfigurationElement[] extensions = Platform.getExtensionRegistry().getConfigurationElementsFor(
				EXTENSION_POINT_PROVIDERS);
		for (IConfigurationElement element : extensions)
		{
			try
			{
				ProviderDescriptor descriptor = new ProviderDescriptor();
				descriptor.setName(element.getAttribute("name"));
				descriptor.setTypeClass(element.getAttribute("type"));
				descriptor.setProviderClass(element.getAttribute("class"));
				descriptor.setPriority(Byte.parseByte(element.getAttribute("priority")));
				if (element.getAttribute("singleton") != null)
					//true is the default
					descriptor.setSingleton(Boolean.parseBoolean(element.getAttribute("singleton")));

				//IProvider provider = (IProvider) element.createExecutableExtension("class");
				descriptor.setConfigurationElement(element);

				registerProvider(descriptor);
			}
			catch (Exception e)
			{
				log.error("registration of provider failed: " + element.getAttribute("class") + " - " + e, e);
			}
		}
	}

}
