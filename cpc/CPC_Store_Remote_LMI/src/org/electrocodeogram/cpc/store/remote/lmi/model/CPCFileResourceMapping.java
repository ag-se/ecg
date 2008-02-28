package org.electrocodeogram.cpc.store.remote.lmi.model;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;


public abstract class CPCFileResourceMapping extends CPCResourceMapping
{
	private static Log log = LogFactory.getLog(CPCFileResourceMapping.class);

	/**
	 * How many resources make up this mapping?
	 */
	protected static final int RELATED_RESOURCE_COUNT = 2;

	public CPCFileResourceMapping(IResource fileResource)
	{
		super(fileResource);

		if (log.isTraceEnabled())
			log.trace("CPCFileResourceMapping() - fileResource: " + fileResource);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.ResourceMapping#getTraversals(org.eclipse.core.resources.mapping.ResourceMappingContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public ResourceTraversal[] getTraversals(ResourceMappingContext context, IProgressMonitor monitor)
			throws CoreException
	{
		if (log.isTraceEnabled())
			log.trace("getTraversals() - context: " + context + ", monitor: " + monitor + " - for: " + getResource());

		//generate a list of all CPC related resources for this file
		List<IResource> resources = new ArrayList<IResource>(RELATED_RESOURCE_COUNT);

		//add self
		resources.add(this.getResource());

		//check for .cpc directory and CPC data files within it
		collectAdditionalResources(this.getResource(), resources);

		if (log.isTraceEnabled())
			log.trace("getTraversals() - result: " + resources);

		if (!resources.isEmpty())
		{
			return new ResourceTraversal[] { new ResourceTraversal(resources.toArray(new IResource[resources.size()]),
					IResource.DEPTH_ZERO, IResource.NONE) };
		}
		else
		{
			return new ResourceTraversal[0];
		}
	}

	abstract protected void collectAdditionalResources(IResource baseResource, List<IResource> resources);

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.ResourceMapping#contains(org.eclipse.core.resources.mapping.ResourceMapping)
	 */
	@Override
	public boolean contains(ResourceMapping mapping)
	{
		if (log.isTraceEnabled())
			log.trace("contains() - mapping: " + mapping + " - for: " + getResource());

		if (this.equals(mapping))
		{
			log.trace("contains() - result: true");
			return true;
		}

		//check whether the given mapping is a data mapping for a file which
		//belongs to this java file
		if (mapping instanceof CPCResourceMapping)
		{
			IResource baseResource = ((CPCResourceMapping) mapping).getResource();
			List<IResource> resources = new ArrayList<IResource>(RELATED_RESOURCE_COUNT);
			collectAdditionalResources(this.getResource(), resources);
			if (resources.contains(baseResource))
			{
				log.trace("contains() - result: true");
				return true;
			}
		}

		log.trace("contains() - result: false");
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "CPCFileResourceMapping[" + resource + "]";
	}
}
