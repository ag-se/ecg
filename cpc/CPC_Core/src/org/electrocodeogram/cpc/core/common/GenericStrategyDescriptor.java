package org.electrocodeogram.cpc.core.common;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Preferences;
import org.electrocodeogram.cpc.core.api.provider.notification.INotificationEvaluationProvider;


/**
 * Generic descriptor object for strategy based providers, i.e. the <em>CPC Notification</em> implementation
 * of {@link INotificationEvaluationProvider}.
 * 
 * The ordering of this class is by descending priority and is not consistent with equals/hashcode.
 * 
 * @author vw
 */
public class GenericStrategyDescriptor implements Comparable<GenericStrategyDescriptor>
{
	private static final Log log = LogFactory.getLog(GenericStrategyDescriptor.class);

	private Preferences preferences;
	private String preferencePrefix;

	protected IConfigurationElement element = null;
	protected String name = null;
	protected String clazz = null;
	protected int priority = 0;

	/**
	 * Cached instance.
	 */
	protected Object instance = null;

	/**
	 * Initialises this descriptor or throws an exception on error.
	 * 
	 * @param preferences the plugin preferences object, never null.
	 * @param preferencePrefix the prefix string for strategy preferences, never null.
	 * @param element never null
	 * @throws IllegalArgumentException if any of the registration data is invalid.
	 */
	public GenericStrategyDescriptor(Preferences preferences, String preferencePrefix, IConfigurationElement element)
	{
		if (log.isTraceEnabled())
			log.trace("GenericStrategyDescriptor() - preferences: " + preferences + ", preferencePrefix: "
					+ preferencePrefix + ", element: " + element);
		assert (preferences != null && preferencePrefix != null && element != null);

		this.preferences = preferences;
		this.preferencePrefix = preferencePrefix;

		this.element = element;
		name = element.getAttribute("name");
		clazz = element.getAttribute("class");
		if (element.getAttribute("priority") != null)
		{
			try
			{
				priority = Integer.parseInt(element.getAttribute("priority"));
			}
			catch (NumberFormatException e)
			{
				log.error("GenericStrategyDescriptor() - illegal priority in extension registration - priority: "
						+ priority + ", name " + name + ", class: " + clazz + ", element: " + element, e);
				throw new IllegalArgumentException("illegal priority in extension registration - priority: " + priority);
			}
		}

		if (name == null || clazz == null)
		{
			log.error("GenericStrategyDescriptor() - data missing in extension registration - priority: " + priority
					+ ", name " + name + ", class: " + clazz + ", element: " + element, new Throwable());
			throw new IllegalArgumentException(
					"data missing in extension registration, name or class attribute not set");
		}
	}

	/**
	 * @return the human readable name of this strategy, never null.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return the priority of this strategy, default is 0.
	 */
	public int getPriority()
	{
		return priority;
	}

	/**
	 * Retrieves the fully qualified class name of the class implementing this strategy.
	 * A client must <b>not</b> use this value to create instances of the class.
	 * 
	 * @return fully qualified class name of implementation, never null.
	 */
	public String getImplementationClass()
	{
		return clazz;
	}

	/**
	 * Creates a new instance of the implementation class for this descriptor
	 * if none exists so far. Otherwise an existing, cached instance is returned.
	 * 
	 * @return instance of implementation class, never null.
	 * 
	 * @throws CoreException if implementation can't be instantiated
	 */
	protected synchronized Object getGenericInstance() throws CoreException
	{
		if (instance == null)
			instance = element.createExecutableExtension("class");

		return instance;
	}

	/**
	 * @return whether this strategy is currently activated.
	 */
	public boolean isActivated()
	{
		/*
		 * TODO: consider whether it would be worthwhile to cache the activation status
		 * here for performance reasons.
		 */
		return preferences.getBoolean(preferencePrefix + clazz);
	}

	/**
	 * Sets new activation state
	 * @param activated
	 */
	public void setActivated(boolean activated)
	{
		preferences.setValue(preferencePrefix + clazz, activated);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(GenericStrategyDescriptor o)
	{
		return o.priority - priority;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return clazz.hashCode();
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
		if (!clazz.equals(((GenericStrategyDescriptor) obj).clazz))
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
		return "GenericStrategyDescriptor[name: " + name + ", priority: " + priority + ", class: " + clazz + "]";
	}

}
