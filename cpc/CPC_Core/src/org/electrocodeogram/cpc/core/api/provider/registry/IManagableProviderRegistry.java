package org.electrocodeogram.cpc.core.api.provider.registry;


import org.electrocodeogram.cpc.core.CPCCorePlugin;


/**
 * This interface lists additional methods which are required to manage a {@link IProviderRegistry}.
 * As these methods are not meant for use outside of {@link CPCCorePlugin} but are never the less
 * required for all potential alternative provider registry implementations, they are separately
 * listed in this interface.
 * <p>
 * All users of the provider registry will always only see an {@link IProviderRegistry} interface.
 * 
 * @author vw
 * 
 * @see IProviderRegistry
 * @see CPCCorePlugin
 */
public interface IManagableProviderRegistry extends IProviderRegistry
{

	/**
	 * Registers a provider with the given priority.
	 * <p>
	 * When used inside of Eclipse this method is usually not needed as the provider registry will
	 * retrieve information on the registered providers directly from the Eclipse extension point framework.
	 * 
	 * @param providerDescriptor the provider to register, never null
	 */
	public void registerProvider(IProviderDescriptor providerDescriptor);

	/**
	 * Unregisters a provider with the registry.
	 * <br>
	 * This operation has no effect if the provider was not registered.
	 * <p>
	 * Only the <em>typeClass</em> and <em>providerClass</em> values of the given {@link IProviderDescriptor}
	 * are used. If multiple providers match these criteria, all are unregistered.
	 * 
	 * @param providerDescriptor the provider to unregister, never null
	 * @return true if the provider was unregistered, false if the provider wasn't registered in the first place
	 */
	public boolean unregisterProvider(IProviderDescriptor providerDescriptor);

	/**
	 * Called when the provider registry is being shut down.
	 * <br>
	 * This typically only happens when the Eclipse IDE is being shutdown.
	 * <p>
	 * All registered an instantiated providers need to be notified of this fact as they may need
	 * to do some cleanup work on shutdown.
	 */
	public void shutdown();
}
