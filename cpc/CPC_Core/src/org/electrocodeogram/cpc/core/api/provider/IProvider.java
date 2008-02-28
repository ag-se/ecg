package org.electrocodeogram.cpc.core.api.provider;


import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.provider.registry.IProviderRegistry;
import org.electrocodeogram.cpc.core.registry.DefaultProviderRegistry;


/**
 * General provider interface implemented by all service providers, used to allow loose coupling of CPC subsystems.
 * <p>
 * The different CPC subsystems can provide or require specific providers. The {@link IProviderRegistry} of the {@link CPCCorePlugin}
 * is used as a central registry to register and obtain providers.
 * <p>
 * All {@link IProvider} implementations need to provide a zero argument constructor.
 * <p>
 * If the provider interface specification does not explicitly state otherwise, all providers need
 * to be thread safe.
 * <p>
 * <b>NOTE:</b> All implementations of this interface also have to implement {@link IManagableProvider}.
 * However, the API interfaces should only extend {@link IProvider}. The methods of
 * {@link IManagableProvider} are only meant for internal use by the {@link IProviderRegistry}.
 * 
 * @author vw
 *
 * @see IManagableProvider
 * @see IProviderRegistry
 * @see DefaultProviderRegistry
 */
public interface IProvider
{
	/**
	 * Retrieves the name of this provider.
	 * <p>
	 * Used in configuration dialogs to identify the provider
	 * (if it is already loaded, otherwise the name from the plugin.xml will be used,
	 * it is probably a good idea to keep the two names equal at all times).
	 * Also used for debug output.
	 * 
	 * @return a short string describing this provider, never null. 
	 */
	public String getProviderName();

	/**
	 * For debugging purposes all provider authors are encouraged to provide a meaningful toString method. 
	 */
	public String toString();
}
