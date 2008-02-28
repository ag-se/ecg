package org.electrocodeogram.cpc.core.api.provider.registry;


import java.util.List;

import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.provider.IManagableProvider;
import org.electrocodeogram.cpc.core.api.provider.IProvider;
import org.electrocodeogram.cpc.core.registry.DefaultProviderRegistry;


/**
 * The {@link IProviderRegistry} is the central point of integration for most CPC plugins/extensions.
 * In order to allow loose coupling and easy replacement of the different CPC subsystem implementations
 * this interface provides a central registry service which CPC subsystems use to acquire provider instances
 * for the different CPC subsystem services.
 * <p>
 * I.e. the CPC Track plugin will use the <em>lookupProvider()</em> method to receive a CPC Store implementation
 * which is then used for local clone storage.
 * <p>
 * Provider classes are never addressed directly, all interaction between <em>IProviderRegistry</em> based
 * CPC subsystems should occur only via <em>org.electrocodeogram.cpc.core.api</em> interfaces or 3rd party
 * API interfaces for new provider types.
 * <p>
 * A reference to the currently active {@link IProviderRegistry} instance can be obtained via
 * {@link CPCCorePlugin#getProviderRegistry()}.
 * <p>
 * <b>NOTE:</b> Any class implementing this interface should also implement {@link IManagableProviderRegistry}.
 * 
 * @author vw
 * 
 * @see CPCCorePlugin#getProviderRegistry()
 * @see DefaultProviderRegistry
 * @see IManagableProviderRegistry
 * @see IManagableProvider
 */
public interface IProviderRegistry
{
	/**
	 * Returns the provider with the highest priority for the given type.
	 * <p>
	 * Any non-null result is guaranteed to be castable to the given <em>providerType</em>.
	 * 
	 * @param providerType the type of the provider to lookup, never null
	 * @return provider of <em>providerType</em> type or NULL if no such provider was found.
	 */
	public IProvider lookupProvider(Class<? extends IProvider> providerType);

	/**
	 * Returns an instance of the provider which corresponds to the given {@link IProviderDescriptor}.
	 * <p>
	 * The {@link IProviderDescriptor} needs to be compatible with the {@link IProviderRegistry} implementation.
	 * <br>
	 * Custom implementations of {@link IProviderDescriptor} are not supported.
	 * 
	 * @param providerDescriptor a valid {@link IProviderDescriptor}, never null.
	 * @return provider of the corresponding type or NULL if no such provider was found.
	 * 
	 * @see #lookupProvider(Class)
	 * @see IProviderDescriptor
	 */
	public IProvider lookupProvider(IProviderDescriptor providerDescriptor);

	/**
	 * Returns a list of descriptors for all registered providers of the given type, ordered descending
	 * according to priority.
	 * <br>
	 * I.e the first element of the list is the descriptor of the provider with the highest priority
	 * (the one which would be returned by <em>lookupProvider()</em>).
	 * <p>
	 * An instance for a given {@link IProviderDescriptor} can be obtained by calling
	 * {@link #lookupProvider(IProviderDescriptor)}.
	 * 
	 * @param providerType the type of the provider to lookup, never null
	 * @return list of descriptors for all providers type <em>providerType</em> or empty list if no such
	 * 			providers were found, never null.
	 * 
	 * @see #lookupProvider(IProviderDescriptor)
	 * @see IProviderDescriptor
	 */
	public List<IProviderDescriptor> lookupProviders(Class<? extends IProvider> providerType);

}
