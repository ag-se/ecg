package org.electrocodeogram.cpc.core.api.provider.registry;


import org.electrocodeogram.cpc.core.api.provider.IProvider;


/**
 * API interface for the descriptors used to handle lazy loaded {@link IProvider} instances
 * in an {@link IProviderRegistry} implementation.
 * <p>
 * Given an {@link IProviderDescriptor} an instance of the corresponding provider can be
 * obtained by calling {@link IProviderRegistry#lookupProvider(IProviderDescriptor)}.
 * 
 * @author vw
 * 
 * @see IProviderRegistry
 * @see IProvider
 */
public interface IProviderDescriptor extends Comparable<IProviderDescriptor>
{
	/**
	 * A human readable name for this provider.
	 * 
	 * @return name of this provider, never null.
	 */
	public String getName();

	/**
	 * Sets a human readable name for this provider.
	 * 
	 * @see IProviderDescriptor#getName()
	 */
	public void setName(String name);

	/**
	 * The priority of this provider. The higher, the more likely it is to be used.
	 * <br>
	 * The priority of the default implementations is 0.
	 * 
	 * @return priority of this provider, may be negative.
	 */
	public byte getPriority();

	/**
	 * Sets the priority of this provider.
	 * 
	 * @see IProviderDescriptor#getPriority()
	 */
	public void setPriority(byte priority);

	/**
	 * The API interface which this provider implements.
	 * <br>
	 * This must be the fully qualified name of an {@link IProvider} sub-interface.
	 *  
	 * @return FQN of {@link IProvider} API interface which is implemented by this provider, never null.
	 */
	public String getTypeClass();

	/**
	 * Sets the API interface which this provider implements.
	 * 
	 * @see IProviderDescriptor#getTypeClass()
	 */
	public void setTypeClass(String typeClass);

	/**
	 * The implementing class for this provider.
	 * <br>
	 * This must be the fully qualified name of a class which implements the
	 * {@link IProviderDescriptor#getTypeClass()} interface.
	 * <p>
	 * <b>CAUTION:</b> <u>do not</u> use this value to <u>try to obtain an instance of this provider yourself</u>.
	 * All instances need to be retrieved via the corresponding {@link IProviderRegistry} methods.
	 * 
	 * @return FQN of the implementing class for this provider, never null.
	 */
	public String getProviderClass();

	/**
	 * Sets the implementing class for this provider.
	 * 
	 * @see IProviderDescriptor#getProviderClass()
	 */
	public void setProviderClass(String providerClass);

	/**
	 * Whether this provider is a singleton or whether a new instance is created for
	 * each lookup.
	 * <p>
	 * Most providers are singletons for performance reasons.
	 *  
	 * @return <em>true</em> if this provider is a singleton, <em>false</em> otherwise.
	 */
	public boolean isSingleton();

	/**
	 * Specifies whether this provider is a singleton or whether a new instance is created for
	 * each lookup.
	 * 
	 * @see IProviderDescriptor#isSingleton()
	 */
	public void setSingleton(boolean singleton);

}
