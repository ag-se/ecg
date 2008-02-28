package org.electrocodeogram.cpc.store.remote.lmi.model;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.electrocodeogram.cpc.store.remote.lmi.utils.XMLPersistenceUtils;


public class CPCFolderResourceMapping extends CPCResourceMapping
{
	private static Log log = LogFactory.getLog(CPCFolderResourceMapping.class);

	public CPCFolderResourceMapping(IFolder folderResource)
	{
		super(folderResource);

		if (log.isTraceEnabled())
			log.trace("CPCPackageResourceMapping() - folderResource: " + folderResource);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.remote.lmi.model.CPCResourceMapping#getTraversals(org.eclipse.core.resources.mapping.ResourceMappingContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public ResourceTraversal[] getTraversals(ResourceMappingContext context, IProgressMonitor monitor)
			throws CoreException
	{
		if (log.isTraceEnabled())
			log.trace("getTraversals() - context: " + context + ", monitor: " + monitor + " - for: " + getResource());

		List<ResourceTraversal> result = new ArrayList<ResourceTraversal>(2);

		//add the directory itself
		result.add(new ResourceTraversal(new IResource[] { getResource() }, IResource.DEPTH_ONE, IResource.NONE));

		//what kind of folder are we?
		if (getResource().getName().equals(XMLPersistenceUtils.XML_PERSISTENCE_DIRECTORY))
		{
			//cpc data folder

			//add parent folder
			IResource parentFolder = getResource().getParent();
			result.add(new ResourceTraversal(new IResource[] { parentFolder }, IResource.DEPTH_ONE, IResource.NONE));
		}
		else
		{
			//other folder

			//add the cpc data directoy if one exists
			IFolder cpcFolder = ((IFolder) getResource()).getFolder(new Path(
					XMLPersistenceUtils.XML_PERSISTENCE_DIRECTORY));
			if (cpcFolder.exists())
			{
				result.add(new ResourceTraversal(new IResource[] { cpcFolder }, IResource.DEPTH_ONE, IResource.NONE));
			}
		}

		return result.toArray(new ResourceTraversal[result.size()]);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.remote.lmi.model.CPCResourceMapping#contains(org.eclipse.core.resources.mapping.ResourceMapping)
	 */
	@Override
	public boolean contains(ResourceMapping mapping)
	{
		if (log.isTraceEnabled())
			log.trace("contains() - mapping: " + mapping + " - for: " + getResource());

		if (mapping.equals(this))
			return true;

		return false;
	}
}
