package org.electrocodeogram.cpc.core.registry;


import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.cfg.registry.IConfigurationRegistry;


/**
 * Default implementation for {@link IConfigurationRegistry}.
 * 
 * @author vw
 */
public class DefaultConfigurationRegistry implements IConfigurationRegistry
{
	private static final Log log = LogFactory.getLog(DefaultConfigurationRegistry.class);

	private Set<String> supportedFileTypes;

	public DefaultConfigurationRegistry()
	{
		log.trace("DefaultConfigurationRegistry()");

		//TODO: read supported file types data from an extension point
		supportedFileTypes = new HashSet<String>(1);
		supportedFileTypes.add("java");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.cfg.registry.IConfigurationRegistry#getSupportedFileTypes()
	 */
	@Override
	public Set<String> getSupportedFileTypes()
	{
		return supportedFileTypes;
	}

}
