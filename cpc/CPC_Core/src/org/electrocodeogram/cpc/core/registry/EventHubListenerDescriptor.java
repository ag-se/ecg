package org.electrocodeogram.cpc.core.registry;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener;


/**
 * Container class to wrap registered {@link IEventHubListener}s in {@link DefaultEventHubRegistry}.<br/>
 * <br/>
 * Equality is based only on {@link EventHubListenerDescriptor#getListenerClass()}.
 * 
 * @author vw
 */
public class EventHubListenerDescriptor
{
	private static final Log log = LogFactory.getLog(EventHubListenerDescriptor.class);

	private String listenerClass;

	private IEventHubListener listener = null;
	private IConfigurationElement configurationElement = null;

	public EventHubListenerDescriptor(IConfigurationElement element)
	{
		assert (element != null);

		this.listenerClass = element.getAttribute("class");
		this.configurationElement = element;
	}

	public EventHubListenerDescriptor(Class<? extends CPCEvent> eventTypeClass, IEventHubListener listener)
	{
		this.listenerClass = listener.getClass().getName();
		this.listener = listener;
		this.configurationElement = null;
	}

	public String getListenerClass()
	{
		return listenerClass;
	}

	public void setListenerClass(String listenerClass)
	{
		this.listenerClass = listenerClass;
	}

	/**
	 * 
	 * @return may return NULL on error
	 */
	public IEventHubListener getListener()
	{
		if (listener == null && configurationElement != null)
		{
			//try to instantiate the listener class
			try
			{
				listener = (IEventHubListener) configurationElement.createExecutableExtension("class");
			}
			catch (CoreException e)
			{
				log.error("getListener() - unable to instantiate listener class: " + listenerClass + " - " + e, e);
				return null;
			}
		}

		return listener;
	}

	public void setListener(IEventHubListener listener)
	{
		this.listener = listener;
	}

	public IConfigurationElement getConfigurationElement()
	{
		return configurationElement;
	}

	public void setConfigurationElement(IConfigurationElement configurationElement)
	{
		this.configurationElement = configurationElement;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((listenerClass == null) ? 0 : listenerClass.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final EventHubListenerDescriptor other = (EventHubListenerDescriptor) obj;
		if (listenerClass == null)
		{
			if (other.listenerClass != null)
				return false;
		}
		else if (!listenerClass.equals(other.listenerClass))
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
		return "EventHubListenerDescriptor[class: " + listenerClass + ", listener: " + listener + "]";
	}
}
