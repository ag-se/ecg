package org.electrocodeogram.cpc.core.api.provider.store;


import org.electrocodeogram.cpc.core.api.data.ICloneFile;


/**
 * Extension interface for {@link IStoreProvider} which contains additional internal methods
 * which must not be used by normal CPC modules.
 * <p>
 * These methods are chiefly of interest to remote store providers.
 * 
 * @author vw
 * 
 * @see IStoreProvider
 * @see IDebuggableStoreProvider
 */
public interface IRemotableStoreProvider extends IStoreProvider
{
	/**
	 * Adds a new {@link ICloneFile} instance to the data store.
	 * <p>
	 * This method is used by remote store providers to synchronise local clone data with
	 * a remote location.
	 * <p>
	 * This method is not meant to be used to create/persist new {@link ICloneFile} instances
	 * for files. Look at {@link IStoreProvider#lookupCloneFileByPath(String, String, boolean, boolean)}
	 * for that.
	 * <p>
	 * An exclusive write lock is required before this method may be called.
	 * 
	 * @param cloneFile clone file instance to add, never null.
	 * @throws StoreLockingException thrown if the current thread does not hold an exclusive write lock.
	 * 
	 * @see IStoreProvider#lookupCloneFileByPath(String, String, boolean, boolean)
	 */
	public void addCloneFile(ICloneFile cloneFile) throws StoreLockingException;

	/**
	 * Updates an existing {@link ICloneFile} instance in the data store.
	 * <p>
	 * This method is used by remote store providers to synchronise local clone data with
	 * a remote location.
	 * <p>
	 * This method is not needed for the updating of {@link ICloneFile} values during normal,
	 * local operation. {@link IStoreProvider#persistData(ICloneFile)} and
	 * {@link IStoreProvider#moveCloneFile(ICloneFile, String, String)} update these values.
	 * <p>
	 * An exclusive write lock is required before this method may be called.
	 * 
	 * @param cloneFile clone file instance to update, the instance should already exist in
	 * 		local storage, never null.
	 * @throws StoreLockingException thrown if the current thread does not hold an exclusive write lock.
	 * 
	 * @see IStoreProvider#persistData(ICloneFile)
	 * @see IStoreProvider#moveCloneFile(ICloneFile, String, String)
	 */
	public void updateCloneFile(ICloneFile cloneFile) throws StoreLockingException;

}
