package org.electrocodeogram.cpc.core.api.provider;


import org.electrocodeogram.cpc.core.api.provider.registry.IProviderRegistry;


/**
 * Special extension interface for {@link IProvider} which provides internal life cycle management methods.
 * <p>
 * This interface lists provider instance management related methods which must only
 * be called by the active {@link IProviderRegistry} implementation.<br/>
 * <p>
 * All {@link IProvider} <u>implementations</u> must also implement this interface.
 * <br>
 * Their corresponding API interfaces should <b>not</b> extend this interface.
 * 
 * @author vw
 *
 * @see IProvider
 */
public interface IManagableProvider extends IProvider
{
	/**
	 * Called when this provider is first returned to a user by the {@link IProviderRegistry}.
	 * <br>
	 * The method will be called only once per provider instance.
	 * <p>
	 * If this provider was registered as a singleton, subsequent requests to {@link IProviderRegistry#lookupProvider(Class)}
	 * will always return the same instance and <em>onLoad()</em> will <b>not</b> be called again.
	 * <p>
	 * In other words, it is guaranteed that this method is called exactly once before this instance is first used.
	 * <p>
	 * Clients must not call this method.
	 */
	public void onLoad();

	/**
	 * Called when this provider is unregistered with the {@link IProviderRegistry}.
	 * <br>
	 * It is guaranteed that this provider will not be used again once this method was called.
	 * <p>
	 * For a provider instance which has not been registered as a singleton this method will be
	 * called once the client indicates that it no longer needs this provider. However, a client
	 * is <b>not</b> required to do so!
	 * <p>
	 * Providers should <b>not</b> depend on this method for their correct operation.
	 * There may be shutdown scenarios in which the {@link IProviderRegistry} will not be able
	 * to unregister all providers in time. And there may be situations in which clients are
	 * not notifying the provider registry about no longer needed provider instances.
	 * <br>
	 * A provider instance may thus be garbage collected without ever receiving a call to this method.
	 * <p>
	 * Clients must not call this method.
	 */
	public void onUnload();

}
