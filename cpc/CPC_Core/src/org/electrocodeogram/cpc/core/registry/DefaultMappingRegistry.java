package org.electrocodeogram.cpc.core.registry;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.provider.IManagableProvider;
import org.electrocodeogram.cpc.core.api.provider.registry.IProviderDescriptor;
import org.electrocodeogram.cpc.core.api.provider.xml.IMappingProvider;
import org.electrocodeogram.cpc.core.api.provider.xml.IMappingRegistry;
import org.electrocodeogram.cpc.core.api.provider.xml.MappingException;
import org.electrocodeogram.cpc.core.api.provider.xml.MappingStore;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;


/**
 * Default implementation of {@link IMappingRegistry}.<br/>
 * <br/>
 * Does a simple linear search through all registered {@link IMappingProvider}s by descending priority
 * and returns the first {@link IMappingProvider} which claims to support the given cpc data mapping. 
 * 
 * @author vw
 * 
 * @see IMappingRegistry
 * @see IMappingProvider
 * @see IMappingProvider#isSupportedMappingFormat(String)
 */
public class DefaultMappingRegistry implements IMappingRegistry, IManagableProvider
{
	private static final Log log = LogFactory.getLog(DefaultMappingRegistry.class);

	public DefaultMappingRegistry()
	{
		log.trace("DefaultMappingRegistry()");
	}

	/*
	 * IMappingRegistry
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.xml.IMappingRegistry#lookupMappingProviderForDataFormat(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public IMappingProvider lookupMappingProviderForDataFormat(String data)
	{
		if (log.isTraceEnabled())
			log.trace("lookupMappingProviderForDataFormat() - data: " + CoreStringUtils.truncateString(data));

		//first get a list of all mapping providers
		List<IProviderDescriptor> providerDescrs = CPCCorePlugin.getProviderRegistry().lookupProviders(
				IMappingProvider.class);

		//the list is sorted descending by priority, which means that we can simply return the first match. 
		for (IProviderDescriptor providerDescr : providerDescrs)
		{
			IMappingProvider provider = (IMappingProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
					providerDescr);
			if (provider == null)
			{
				log.error(
						"lookupMappingProviderForDataFormat() - unable to obtain provider instance, skipping provider - provider descriptor: "
								+ providerDescr, new Throwable());
				continue;
			}

			if (provider.isSupportedMappingFormat(data))
			{
				if (log.isTraceEnabled())
					log.trace("lookupMappingProviderForDataFormat() - found matching mapping provider: " + provider);

				return provider;
			}
		}

		//this isn't really an error but during normal operations this usually shouldn't happen.
		log.warn(
				"lookupMappingProviderForDataFormat() - no registered mapping provider supports the given cpc data mapping - data: "
						+ CoreStringUtils.truncateString(data), new Throwable());

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.xml.IMappingRegistry#mapFromString(java.lang.String)
	 */
	@Override
	public MappingStore mapFromString(String data) throws MappingException
	{
		if (log.isTraceEnabled())
			log.trace("mapFromString() - data: " + CoreStringUtils.truncateString(data));

		//first do a normal lookup
		IMappingProvider mappingProvider = lookupMappingProviderForDataFormat(data);
		if (mappingProvider == null)
		{
			log.trace("mapFromString() - no matching mapping provider found, returning NULL.");
			return null;
		}

		//then process the data
		MappingStore result = mappingProvider.mapFromString(data);

		if (log.isTraceEnabled())
			log.trace("mapFromString() - result: " + result);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.xml.IMappingRegistry#extractCloneObjectUuidFromString(java.lang.String)
	 */
	@Override
	public String extractCloneObjectUuidFromString(String data) throws MappingException
	{
		if (log.isTraceEnabled())
			log.trace("extractCloneObjectUuidFromString() - data: " + CoreStringUtils.truncateString(data));

		//first do a normal lookup
		IMappingProvider mappingProvider = lookupMappingProviderForDataFormat(data);
		if (mappingProvider == null)
		{
			log.trace("extractCloneObjectUuidFromString() - no matching mapping provider found, returning NULL.");
			return null;
		}

		//then process the data
		String result = mappingProvider.extractCloneObjectUuidFromString(data);

		if (log.isTraceEnabled())
			log.trace("extractCloneObjectUuidFromString() - result: " + result);

		return result;
	}

	/*
	 * IProvider
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#getProviderName()
	 */
	@Override
	public String getProviderName()
	{
		return "CPC Core - Default Mapping Registry";
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IManagableProvider#onLoad()
	 */
	@Override
	public void onLoad()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IManagableProvider#onUnload()
	 */
	@Override
	public void onUnload()
	{
	}

}
