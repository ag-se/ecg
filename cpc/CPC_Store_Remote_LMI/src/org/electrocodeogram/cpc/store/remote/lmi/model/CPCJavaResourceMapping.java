package org.electrocodeogram.cpc.store.remote.lmi.model;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.electrocodeogram.cpc.store.remote.lmi.utils.XMLPersistenceUtils;


public class CPCJavaResourceMapping extends CPCFileResourceMapping
{
	private static Log log = LogFactory.getLog(CPCJavaResourceMapping.class);

	public CPCJavaResourceMapping(IFile fileResource)
	{
		super(fileResource);

		if (log.isTraceEnabled())
			log.trace("CPCJavaResourceMapping() - fileResource: " + fileResource);
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

		IFolder cpcDir = baseResource.getParent().getFolder(new Path(XMLPersistenceUtils.XML_PERSISTENCE_DIRECTORY));
		if (cpcDir.exists())
		{
			if (log.isTraceEnabled())
				log.trace("collectAdditionalResources() - folder contains CPC directory: " + cpcDir);

			//check for data file
			IFile cpcDataFile = cpcDir.getFile(new Path(getResource().getName() + "."
					+ XMLPersistenceUtils.XML_PERSISTENCE_EXTENSION));
			if (cpcDataFile.exists())
			{
				if (log.isTraceEnabled())
					log.trace("collectAdditionalResources() - found CPC data file: " + cpcDataFile);
				resources.add(cpcDataFile);
			}

			//check for history file
			//			IFile cpcHistoryFile = cpcDir.getFile(new Path(getResource().getName() + ".hist.cpc"));
			//			if (cpcHistoryFile.exists())
			//			{
			//				if (log.isTraceEnabled())
			//					log.trace("found CPC history file: " + cpcHistoryFile);
			//				resources.add(cpcHistoryFile);
			//			}

			//we never want to checkin the cpc source files, they are for local use only
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "CPCJavaResourceMapping[" + resource + "]";
	}
}
