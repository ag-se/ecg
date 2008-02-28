package org.electrocodeogram.eclipse.core.logging;


/**
 * 
 * @author vw
 * @deprecated no longer in use
 *
 */
@Deprecated
public abstract class AbstractCommonsLogFactory //extends LogFactory
{
	//	//	public AbstractCommonsLogFactory()
	//	//	{
	//	//		System.err.println("AbstractCommonsLogFactory()");
	//	//	}
	//
	//	/**
	//	 * this method needs to be overridden in a LogFactory implementation in each plugin.
	//	 * the full name of that implementation needs to be added to a commons-logging.properties
	//	 * file in the form:
	//	 * 		org.apache.commons.logging.LogFactory=package.Classname
	//	 * @return a log manager reference, never null.
	//	 */
	//	protected abstract ILogManager getPluginLogManager();
	//
	//	/*
	//	 * (non-Javadoc)
	//	 * @see org.apache.commons.logging.LogFactory#getAttribute(java.lang.String)
	//	 */
	//	@Override
	//	public Object getAttribute(String name)
	//	{
	//		return null;
	//	}
	//
	//	/*
	//	 * (non-Javadoc)
	//	 * @see org.apache.commons.logging.LogFactory#getAttributeNames()
	//	 */
	//	@Override
	//	public String[] getAttributeNames()
	//	{
	//		return null;
	//	}
	//
	//	/*
	//	 * (non-Javadoc)
	//	 * @see org.apache.commons.logging.LogFactory#getInstance(java.lang.Class)
	//	 */
	//	@SuppressWarnings("unchecked")
	//	@Override
	//	public Log getInstance(Class classObj) throws LogConfigurationException
	//	{
	//		return getInstance(classObj.getName());
	//	}
	//
	//	/*
	//	 * (non-Javadoc)
	//	 * @see org.apache.commons.logging.LogFactory#getInstance(java.lang.String)
	//	 */
	//	@Override
	//	public Log getInstance(String className) throws LogConfigurationException
	//	{
	//		return new Log4JLogger(getPluginLogManager().getLogger(className));
	//	}
	//
	//	/*
	//	 * (non-Javadoc)
	//	 * @see org.apache.commons.logging.LogFactory#release()
	//	 */
	//	@Override
	//	public void release()
	//	{
	//	}
	//
	//	/*
	//	 * (non-Javadoc)
	//	 * @see org.apache.commons.logging.LogFactory#removeAttribute(java.lang.String)
	//	 */
	//	@Override
	//	public void removeAttribute(String name)
	//	{
	//	}
	//
	//	/*
	//	 * (non-Javadoc)
	//	 * @see org.apache.commons.logging.LogFactory#setAttribute(java.lang.String, java.lang.Object)
	//	 */
	//	@Override
	//	public void setAttribute(String name, Object value)
	//	{
	//	}
	//
}
