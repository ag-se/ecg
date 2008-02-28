package org.electrocodeogram.cpc.store.remote.lmi.model;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;


public abstract class CPCResourceMapping extends ResourceMapping
{
	private static Log log = LogFactory.getLog(CPCResourceMapping.class);

	protected IResource resource;

	public CPCResourceMapping(IResource resource)
	{
		if (log.isTraceEnabled())
			log.trace("CPCResourceMapping() - resource: " + resource);

		this.resource = resource;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.ResourceMapping#getModelObject()
	 */
	@Override
	public Object getModelObject()
	{
		if (log.isTraceEnabled())
			log.trace("getModelObject() - for: " + resource);

		//FIXME: no idea what we need to return here
		//but this seems to fit the example code
		return resource;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.ResourceMapping#getModelProviderId()
	 */
	@Override
	public String getModelProviderId()
	{
		if (log.isTraceEnabled())
			log.trace("getModelProviderId() - for: " + resource);

		return CPCModelProvider.PROVIDER_ID;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.ResourceMapping#getProjects()
	 */
	@Override
	public IProject[] getProjects()
	{
		if (log.isTraceEnabled())
			log.trace("getProjects() - for: " + resource);

		//FIXME: no idea what we need to retun here
		return new IProject[] { resource.getProject() };
	}

	protected IResource getResource()
	{
		log.trace("getResource()");
		return this.resource;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.ResourceMapping#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((resource == null) ? 0 : resource.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.ResourceMapping#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final CPCResourceMapping other = (CPCResourceMapping) obj;
		if (resource == null)
		{
			if (other.resource != null)
				return false;
		}
		else if (!resource.equals(other.resource))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "CPCResourceMapping[" + resource + "]";
	}
}
