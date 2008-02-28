package org.electrocodeogram.cpc.store.remote.lmi.model;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.team.core.mapping.IResourceMappingMerger;


public class CPCAdapterFactory implements IAdapterFactory
{
	private static Log log = LogFactory.getLog(CPCAdapterFactory.class);

	private CPCResourceMappingMerger resourceMappingMerger;

	public CPCAdapterFactory()
	{
		if (log.isTraceEnabled())
			log.trace("CPCAdapterFactory()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType)
	{
		if (log.isTraceEnabled())
			log.trace("getAdapter() - adaptableObj: " + adaptableObject + ", adapterType: " + adapterType);

		if (adapterType == IResourceMappingMerger.class && adaptableObject instanceof CPCModelProvider)
		{
			if (resourceMappingMerger == null)
			{
				resourceMappingMerger = new CPCResourceMappingMerger((CPCModelProvider) adaptableObject);
			}

			if (log.isTraceEnabled())
				log.trace("getAdapter() - result: " + resourceMappingMerger);

			return resourceMappingMerger;
		}

		log.trace("getAdapter() - result: null");

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class[] getAdapterList()
	{
		log.trace("getAdapterList()");

		return new Class[] { IResourceMappingMerger.class };
	}

}
