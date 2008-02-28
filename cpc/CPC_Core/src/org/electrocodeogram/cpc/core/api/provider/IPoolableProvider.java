package org.electrocodeogram.cpc.core.api.provider;


import org.electrocodeogram.cpc.core.api.provider.registry.IProviderRegistry;


/**
 * A special extension interface of {@link IManagableProvider} for service providers which
 * want to request instance pooling.
 * <p>
 * A provider which prefers instance pooling over the creation of a new instance
 * for every client lookup should implement this interface.
 * <br>
 * Whenever possible it is recommended for providers to be registered as singletons.
 * <p>
 * An {@link IProviderRegistry} implementation is <b>not</b> required to support provider
 * instance pooling. If pooling is not supported, providers implementing
 * this interface are handled like normal {@link IManagableProvider}s.
 * 
 * @author vw
 */
public interface IPoolableProvider extends IManagableProvider
{
	/**
	 * Called when a new provider instance is first added to an instance pool.
	 * <br>
	 * This method is guaranteed to be called before {@link #leavingPool()} and
	 * {@link #removedFromPool()}.
	 * <p>
	 * A provider implementation can use a call to this method as indication
	 * of pooling support in the current provider registry. If the provider is
	 * used before this method is called, pooling is not supported.
	 */
	public void addedToPool();

	/**
	 * Called shortly before provider instance is removed from the pool and discarded.
	 * <br>
	 * This typically happens on shutdown or when the provider registry decides that
	 * there are too many unused instances in the pool.
	 */
	public void removedFromPool();

	/**
	 * Called shortly before this instance is handed out to a client.
	 */
	public void leavingPool();

	/**
	 * Called shortly after a client has indicated that it no longer needs
	 * this instance and the instance is about to be returned to the pool.
	 */
	public void returningToPool();
}
