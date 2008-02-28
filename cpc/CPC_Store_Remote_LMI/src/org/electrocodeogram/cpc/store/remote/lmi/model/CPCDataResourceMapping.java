package org.electrocodeogram.cpc.store.remote.lmi.model;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.electrocodeogram.cpc.store.remote.lmi.utils.XMLPersistenceUtils;


public class CPCDataResourceMapping extends CPCFileResourceMapping
{
	private static Log log = LogFactory.getLog(CPCDataResourceMapping.class);

	public CPCDataResourceMapping(IFile fileResource)
	{
		super(fileResource);

		if (log.isTraceEnabled())
			log.trace("CPCDataResourceMapping() - fileResource: " + fileResource);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.remote.lmi.model.CPCResourceMapping#collectAdditionalResources(org.eclipse.core.resources.IResource, java.util.List)
	 */
	@Override
	protected void collectAdditionalResources(IResource baseResource, List<IResource> resources)
	{
		if (log.isTraceEnabled())
			log.trace("collectAdditionalResources() - baseResource: " + baseResource + ", resources: " + resources);

		//generate the matching source file name for this cpc data file
		IFile sourceFile = XMLPersistenceUtils.getSourceFileForCPCDataResource(baseResource);
		if (sourceFile != null && sourceFile.exists())
		{
			if (log.isTraceEnabled())
				log.trace("collectAdditionalResources() - parent folder contains source file: " + sourceFile);

			resources.add(sourceFile);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "CPCDataResourceMapping[" + resource + "]";
	}

}
