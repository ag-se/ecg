package org.electrocodeogram.cpc.core.registry;


import org.eclipse.core.runtime.IConfigurationElement;
import org.electrocodeogram.cpc.core.api.provider.IManagableProvider;
import org.electrocodeogram.cpc.core.api.provider.IProvider;
import org.electrocodeogram.cpc.core.api.provider.registry.IProviderDescriptor;
import org.electrocodeogram.cpc.core.api.provider.registry.IProviderRegistry;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;


/**
 * Descriptor POJO for {@link IProvider} meta data. Used by {@link IProviderRegistry} to register
 * and retrieve provider implementations.<br/>
 * Implements non standard <em>compareTo</em> and <em>equals/hashcode</em> semantics:<br/>
 * <ul>
 * 	<li>two descriptors are equal if their <em>providerClass</em> is equal (analog for hashcode)</li>
 * 	<li><em>compareTo</em> orders descriptor according to descending <em>priority</em>. If <em>priority</em> is equal,
 * descriptors are sorted by <em>providerClass</em></li>
 * </ul>
 * 
 * @author vw
 */
public class ProviderDescriptor implements IProviderDescriptor
{
	private String name;
	private byte priority;
	private String typeClass;
	private String providerClass;
	private boolean singleton = true;

	private IManagableProvider provider = null;
	private IConfigurationElement configurationElement = null;

	public ProviderDescriptor()
	{

	}

	/**
	 * Priorities:
	 * <ul>
	 * 	<li>0 is the default priority for original CPC modules.</li>
	 * 	<li>Byte.MAX_VALUE is the highest priority</li>
	 * 	<li>Byte.MIN_VALUE is the lowest priority</li>
	 * 	<li>Providers are generally used in order of decreasing priority. For provider types
	 * 		which require only one implementation (i.e. local storage/{@link IStoreProvider})
	 * 		only the one provider with the highest priority will be used and all other
	 * 		providers will have no effect.</li>
	 * 	<li>To maintain extensibility all provider authors are strongly encouraged to always leave some
	 * 		priorities above and below their providers priority. I.e. if you want to override the
	 * 		default {@link IStoreProvider} don't set your priority to 1 but use 50 instead.</li>
	 * 	<li>For certain provider types all registered providers  may be taken into account or all
	 * 		providers may be asked if they're applicable in a given context, in decreasing order according
	 * 		to priotity.</li>
	 * </ul>
	 *
	 * @param name the providers name, this will be displayed in configuration dialogs and debug messages, never null
	 * @param priority this providers priority, see above
	 * @param typeClass the providers type interface, this must be the fully qualified name of an {@link IProvider}
	 * 		subinterface.
	 * @param providerClass the implementing class for this provider, this must be the fully qualified name of a
	 * 		class which implements the <em>typeClass</em> interface.
	 * @param singleton true if there should be no more than one instance of this provider, false if a new instace should
	 * 		be returned for each lookup request to the provider registry.
	 */
	public ProviderDescriptor(String name, byte priority, String typeClass, String providerClass, boolean singleton)
	{
		assert (name != null && typeClass != null && providerClass != null);

		this.name = name;
		this.priority = priority;
		this.typeClass = typeClass;
		this.providerClass = providerClass;
		this.singleton = singleton;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.registry.IProviderDescriptor#getName()
	 */
	@Override
	public String getName()
	{
		return name;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.registry.IProviderDescriptor#setName(java.lang.String)
	 */
	@Override
	public void setName(String name)
	{
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.registry.IProviderDescriptor#getPriority()
	 */
	@Override
	public byte getPriority()
	{
		return priority;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.registry.IProviderDescriptor#setPriority(byte)
	 */
	@Override
	public void setPriority(byte priority)
	{
		this.priority = priority;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.registry.IProviderDescriptor#getTypeClass()
	 */
	@Override
	public String getTypeClass()
	{
		return typeClass;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.registry.IProviderDescriptor#setTypeClass(java.lang.String)
	 */
	@Override
	public void setTypeClass(String typeClass)
	{
		this.typeClass = typeClass;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.registry.IProviderDescriptor#getProviderClass()
	 */
	@Override
	public String getProviderClass()
	{
		return providerClass;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.registry.IProviderDescriptor#setProviderClass(java.lang.String)
	 */
	@Override
	public void setProviderClass(String providerClass)
	{
		this.providerClass = providerClass;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.registry.IProviderDescriptor#isSingleton()
	 */
	@Override
	public boolean isSingleton()
	{
		return singleton;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.registry.IProviderDescriptor#setSingleton(boolean)
	 */
	@Override
	public void setSingleton(boolean singleton)
	{
		this.singleton = singleton;
	}

	/**
	 * For internal use by the provider registry only.<br/>
	 * <br/>
	 * Caches a singleton provider instance, if this provider is a singleton and was
	 * already instantiated once.
	 */
	IManagableProvider getProvider()
	{
		return provider;
	}

	/**
	 * For internal use by the provider registry only.<br/>
	 * <br/>
	 * A provider instance may only be cached if the provider was registered as a singleton.
	 *
	 * @param provider the provider instance to cache, may be null
	 * @throws IllegalStateException if provider is != null and this is not a singleton provider.
	 */
	void setProvider(IManagableProvider provider)
	{
		if (provider != null && !singleton)
			throw new IllegalStateException(
					"Registering a cached provider instance is not allowed for non-singleton providers.");

		this.provider = provider;
	}

	/**
	 * For internal use by the provider registry only.<br/>
	 * <br/>
	 * Stores the Eclipse extension point element which was used to register this provider
	 * with the registry. This is needed because the implementing class may not be
	 * reachable by the CPC Core class loader and the element is thus needed to obtain
	 * an instance of the class. 
	 */
	IConfigurationElement getConfigurationElement()
	{
		return configurationElement;
	}

	void setConfigurationElement(IConfigurationElement configurationElement)
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
		result = prime * result + ((providerClass == null) ? 0 : providerClass.hashCode());
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
		final ProviderDescriptor other = (ProviderDescriptor) obj;
		if (providerClass == null)
		{
			if (other.providerClass != null)
				return false;
		}
		else if (!providerClass.equals(other.providerClass))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(IProviderDescriptor o)
	{
		/*
		 * Sorting is a bit tricky as we have to take into account the following situations:
		 * 	a) there are two different providers with the same priority
		 * 	b) the same provider is registered multiple times under different priorities
		 */

		/*
		 * First we sort by priority
		 */
		//for normal order: this.getPriority() - o.getPriority();
		//but we want to sort in decending order
		int diff = o.getPriority() - this.getPriority();
		//TODO: check that this works as intended

		//check if we have the same priority for different providers
		if ((diff == 0) && (!this.equals(o)))
		{
			//this is forbidden by set semantics, we'll fall back to sorting by class name
			return this.getProviderClass().compareTo(o.getProviderClass());
		}

		//ok, return the diff
		return diff;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "ProviderDescriptor[name: " + name + ", pri: " + priority + ", singleton: " + singleton + ", type: "
				+ typeClass + ", class: " + providerClass + "]";
	}
}
