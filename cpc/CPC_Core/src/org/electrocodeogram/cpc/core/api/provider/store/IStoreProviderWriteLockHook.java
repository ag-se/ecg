package org.electrocodeogram.cpc.core.api.provider.store;


/**
 * A special interface for exclusive write lock hook callbacks registered via the
 * {@link IStoreProvider#setWriteLockHook(IStoreProviderWriteLockHook)} method.
 * <p>
 * There should be no reason why any plugin besides the <em>CPC Track</em> module
 * should implement this interface.
 * 
 * @author vw
 * 
 * @see IStoreProvider
 */
public interface IStoreProviderWriteLockHook
{
	/**
	 * The callback method which will be called once any party tries to acquire an exclusive
	 * write lock on the {@link IStoreProvider}.
	 * Once this method is called the write lock will already have been granted, but the control
	 * will not yet have been transfered back to the requester of the lock.
	 * <br>
	 * An implementation should use a callback to this method as a chance to write back any
	 * internally cached, potentially dirty data to the {@link IStoreProvider}.
	 * 
	 * @see IStoreProvider#setWriteLockHook(IStoreProviderWriteLockHook)
	 */
	public void aboutToGrantWriteLock() throws StoreLockingException;
}
