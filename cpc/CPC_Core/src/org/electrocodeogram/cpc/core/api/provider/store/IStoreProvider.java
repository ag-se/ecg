package org.electrocodeogram.cpc.core.api.provider.store;


import java.util.List;

import org.electrocodeogram.cpc.core.api.data.CloneDiff;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.ICloneGroup;
import org.electrocodeogram.cpc.core.api.data.ICloneObject;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionLazyMultiStatefulObject;
import org.electrocodeogram.cpc.core.api.hub.event.CloneModificationEvent;
import org.electrocodeogram.cpc.core.api.hub.event.ClonePersistenceEvent;
import org.electrocodeogram.cpc.core.api.provider.IProvider;


/**
 * A local storage provider provides persistence for arbitrary clone data objects.
 * <br>
 * The {@link IStoreProvider} interface specifies the API implemented by all local storage providers.
 * <p>
 * Only one local storage provider may be active at any given point in time. 
 * <p>
 * <b>All methods of this interface are thread safe.</b>
 * <p>
 * <b>All implementations of this interface also need to implement {@link IRemotableStoreProvider}.</b>
 * <p>
 * 
 * <b>Object Identity and Modification</b>
 * <blockquote>
 * All data returned by this API is detached from it's corresponding background storage. As such any
 * returned objects can be arbitrarily modified without changing the stored versions of the objects
 * and without affecting any other users of the API.
 * <br>
 * To persist any modifications made to an object the corresponding add/update or remove methods have
 * to be called. Making modifications requires the ownership of an exclusive write lock (see Locking).
 * </blockquote>
 * 
 * <b>Locking</b>
 * <blockquote>
 * Read access to clone data does not require the manual acquisition of a lock. Each lookup/get method
 * will internally acquire a shared read lock and release it at the end of the method, before returning
 * the result.
 * <br>
 * All write accesses require exclusive locks on the entire store provider.
 * <p>
 * A <b>repeatable read</b> is only guaranteed to the lock holder <b>AFTER</b> the lock was granted and
 * only for the duration of the lock. This means that the lock holder should <b>retrieve fresh versions
 * of all clone data objects it needs after the lock was granted</b>.
 * <p>
 * Read locks are only held for the duration of a method call. Any <b>read only</b> users which require
 * the data to remain unchanged during their runtime have to <b>acquire an exclusive write lock</b>.
 * Even though they do not intend to make any modifications to the data.
 * <p>
 * The owner of the registered {@link IStoreProviderWriteLockHook}, if any, may hold an implicit write
 * lock and might be concurrently modifying local cached versions of the clone data. If a read-only
 * client needs to ensure that it receives the latest version of the clone data it has to acquire an
 * exclusive write lock.
 * <br>
 * Once a client requests an exclusive write lock, a registered {@link IStoreProviderWriteLockHook} will
 * be notified and will be given the opportunity to transfer any dirty clone data back to the store
 * provider before the client's lock request is granted.
 * </blockquote>  
 * 
 * <b>Clone Modification and Persistence Events</b>
 * <blockquote>
 * In general, the store provider is responsible for the generation of {@link CloneModificationEvent}s for all
 * clone data modifications made by its clients. The events are generated once a client has relinquished its
 * exclusive write lock.
 * <br>
 * A client may choose to handle the modification event generation itself. This fact can be communicated to the store
 * provider by means of the {@link LockMode} parameter at lock acquisition time.
 * <br>
 * The store provider will furthermore generate {@link ClonePersistenceEvent}s whenever clone data is persisted
 * or purged.
 * </blockquote>
 * 
 * <b>Design Considerations:</b>
 * <blockquote>
 * Care has been taken in the design of this API interface to allow maximum implementation freedom
 * and flexibility for local storage providers. The internal storage representation is not disclosed
 * and may vary from implementation to implementation. As the internal storage may not allow concurrent writes
 * and may not be object oriented this enforces some API aspects which can feel somewhat cumbersome.
 * <br>
 * I.e. it is not possible to navigate through the returned object tree ({@link ICloneFile} and
 * {@link ICloneGroup} do not hold lists of clones)
 * and exclusive, pessimistic locking is enforced.
 * <br>
 * The normal mode of operation of the <em>CPC Sensor</em> ensures that almost all accesses to the store
 * provider are issued from the main thread. As such lock contention is not a major issue and exclusive
 * locks do are unlikely to introduce performance problems.
 * <br>
 * It is up to the user of the storage provider API to ensure that deadlocks are avoided.
 * <br>
 * During normal use a {@link StoreLockingException} will never occur. Always having to add a corresponding
 * try/catch block may thus seem somewhat bothersome. However, it is critical that locks are always released,
 * even in the event of other exceptions. Forcing the user of the {@link IStoreProvider} API to add a
 * try/catch block around each code block which acquires an exclusive lock does reduce the likely hood
 * of lock "leaking" quite considerably. The user will also receive direct feedback from the IDE during
 * programming if an exclusive lock-requiring operation is used outside of such a try/catch block.
 * </blockquote>  
 * 
 * @author vw
 * 
 * @see IDebuggableStoreProvider
 * @see IRemotableStoreProvider
 * @see IStoreProviderWriteLockHook
 */
public interface IStoreProvider extends IProvider
{
	/**
	 * Determines the behaviour of the
	 * {@link #acquireWriteLock(LockMode)}
	 * exclusive write lock.
	 * 
	 * @see #acquireWriteLock(LockMode)
	 * @see #releaseWriteLock()
	 */
	public enum LockMode
	{
		/**
		 * The default locking behaviour.
		 * <br>
		 * A registered {@link IStoreProviderWriteLockHook} will be notified and will be given
		 * the opportunity to transfer any dirty clone data back to the store provider before
		 * this lock request is granted.
		 * <br>
		 * {@link CloneModificationEvent}s for all modified clones will be generated when the lock is released.
		 */
		DEFAULT,

		/**
		 * A registered {@link IStoreProviderWriteLockHook} will be notified and will be given
		 * the opportunity to transfer any dirty clone data back to the store provider before
		 * this lock request is granted.
		 * <br>
		 * <b>No</b> {@link CloneModificationEvent}s will be generated. It is up to the caller to ensure
		 * that such events are generated and dispatched as required.
		 */
		NO_MODIFICATION_EVENT,

		/**
		 * {@link CloneModificationEvent}s for all modified clones will be generated when the lock is released.
		 * <br>
		 * Does not notify a registered {@link IStoreProviderWriteLockHook} about the lock request.
		 * <p>
		 * <b>IMPORTANT:</b> This mode may <b>only</b> be used by the module which owns the currently registered
		 * 		write lock hook (in practice this means that it should not be used outside of <em>CPC Track</em>).
		 * <p>
		 * Rationale:
		 * <blockquote>
		 * This mode allows the owner of the write lock hook which was registered via
		 * {@link #setWriteLockHook(IStoreProviderWriteLockHook)} to acquire a lock without
		 * having to filter out its own lock requests in the write lock hook.
		 * </blockquote>
		 */
		NO_WRITE_LOCK_HOOK_NOTIFY,

		/**
		 * Combination of {@link LockMode#NO_WRITE_LOCK_HOOK_NOTIFY} and {@link LockMode#NO_MODIFICATION_EVENT}.
		 * <br>
		 * Neither are {@link CloneModificationEvent}s generated nor is any registered {@link IStoreProviderWriteLockHook}
		 * notified about this request.
		 */
		NO_WRITE_LOCK_HOOK_NOTIFY_NO_MODIFICATION_EVENT
	}

	/**
	 * Used to provide {@link #updateClone(IClone, UpdateMode)}
	 * with information on the type of modification done to the given clone.
	 * <br>
	 * The updateClone method may be called multiple times for the same clone, in that case the given update modes
	 * are accumulated. A call with {@link UpdateMode#MOVED} and a call with {@link UpdateMode#MODIFIED} together are
	 * equivalent to a single call with {@link UpdateMode#MOVED_MODIFIED}.
	 */
	public enum UpdateMode
	{
		/**
		 * The clone was only moved or some other data was modified, i.e. an extension.
		 * <br>
		 * Its length and contents remain unchanged.
		 */
		MOVED,

		/**
		 * The clone content (and potentially length) was modified.
		 * <br>
		 * Its offset remains unchanged.
		 */
		MODIFIED,

		/**
		 * Combination of {@link UpdateMode#MOVED} and {@link UpdateMode#MODIFIED}.
		 * <br>
		 * The clones position and content was changed.
		 */
		MOVED_MODIFIED
	}

	/**
	 * Retrieves or creates an {@link ICloneFile} handle for the given file.
	 * <p>
	 * The file itself may no longer exist. I.e. because it was recently deleted.
	 * <br>
	 * In this case the method returns the old {@link ICloneFile} handle, if one
	 * exists in the cache or persistent storage. Otherwise it returns NULL.
	 * <p>
	 * If a file was recently moved and <em>followFileMove</em> is set to <em>true</em>
	 * this method <b>may</b> also return the {@link ICloneFile} handle for the
	 * new location of the file. In this case the {@link ICloneFile}'s project and
	 * path values might not match the given <em>project</em> and <em>path</em>
	 * parameters.
	 * <br>
	 * It is up to the implementation to decide whether and how long to store
	 * file move information.
	 * <p>
	 * 
	 * @param project project the file in question belongs to, never null
	 * @param path path to the file in question, relative to project, never null
	 * @param createNewUuidIfNeeded in situations where no existing {@link ICloneFile} UUID can be
	 * 		found for the file in question, a new {@link ICloneFile} instance with a new UUID is
	 * 		generated if this value is <em>true</em>. If this value is <em>false</em>, NULL will be returned instead
	 * 		of generating a new UUID and {@link ICloneFile} instance.
	 * 		<br>
	 * 		Setting this to <em>true</em> should be the default. A <em>false</em> value is useful if you only want
	 * 		to check whether a given file location is of interest to CPC.
	 * @param followFileMove whether the store provider should internally check if the file was moved (<em>true</em>)
	 * 		to another location and return the {@link ICloneFile} instance for the new file location if this is
	 * 		the case or whether NULL should be returned (<em>false</em>).
	 * 		<br>
	 * 		An {@link IStoreProvider} is not required to support this feature. If tracking of moved files
	 * 		is not supported, this parameter is silently ignored.
	 * @return a CloneFile instance for the given path or NULL on error (i.e. file not found/not readable)
	 */
	//TODO: recheck all calling locations and decide whether createNewUuidIfNeeded should be set to false.
	public ICloneFile lookupCloneFileByPath(String project, String path, boolean createNewUuidIfNeeded,
			boolean followFileMove);

	/**
	 * Retrieves an {@link ICloneFile} handle by file uuid.
	 * <p>
	 * This method does not check whether the underlying file exists.
	 * <br>
	 * If an {@link ICloneFile} with the given uuid is found in the cache or in
	 * the persistent storage, it is returned. Otherwise NULL is returned.
	 *
	 * @param fileUuid the file uuid to lookup, never null.
	 * @return an {@link ICloneFile} instance for the given uuid or NULL if no file with this uuid was found.
	 */
	public ICloneFile lookupCloneFile(String fileUuid);

	/**
	 * Retrieves an {@link IClone} object by clone uuid.
	 * 
	 * @param cloneUuid UUID of the clone to lookup, never null.
	 * @return an {@link IClone} instance for the given UUID or NULL if no clone with this UUID was found.
	 */
	public IClone lookupClone(String cloneUuid);

	/**
	 * Retrieves an {@link ICloneGroup} object by clone group uuid.
	 * 
	 * @param groupUuid UUID of the clone group to lookup, never null.
	 * @return an {@link ICloneGroup} instance for the given UUID or NULL if no clone group with this UUID was found.
	 */
	public ICloneGroup lookupCloneGroup(String groupUuid);

	/**
	 * Convenience method, equals {@link #getClonesByFile(String, int, int)} with
	 * a <em>startOffset</em> and <em>endOffset</em> of -1.
	 * 
	 * @see #getClonesByFile(String, int, int)
	 */
	public List<IClone> getClonesByFile(String fileUuid);

	/**
	 * Retrieves all clones for a given offset range in a given file.
	 * <br>
	 * Offsets start at 0.
	 * <br>
	 * All clones which collide with the given offset range will be returned. Even if they are only partly included
	 * in the range.
	 * <p>
	 * Automatically acquires and releases a shared read lock.
	 * 
	 * @param fileUuid UUID of the file to retrieve the clone data for, never null.
	 * @param startOffset the offset from which on all clones should be returned, any clone ending on this offset is still included.
	 * 		Must be &gt;= 0.
	 * @param endOffset the offset till which all clones should be returned, any clone starting on this offset is still included.
	 * 		Must be &gt;= <em>startOffset</em> or -1 for all clones till end of file. 
	 * @return a sorted (by start offset), distinct list of clones for the given interval. Never null.
	 */
	public List<IClone> getClonesByFile(String fileUuid, int startOffset, int endOffset);

	/**
	 * Retrieves all clones which are part of the given clone group.
	 * <p>
	 * Automatically acquires and releases a shared read lock.
	 * 
	 * @param groupUuid uuid of the group for which all clones should be returned, never null.
	 * @return <b>unsorted</b> list of group members, never null.
	 */
	public List<IClone> getClonesByGroup(String groupUuid);

	/**
	 * Takes an {@link ICloneObjectExtensionLazyMultiStatefulObject} which is only partially loaded and
	 * returns a <b>new cloned copy</b> with all sub-elements fully loaded.
	 * <p>
	 * The given clone and extension instances itself are <b>not</b> modified.
	 * 
	 * @param cloneObject the clone object to which this extension is added, never null.
	 * @param extension the extension to load all sub-elements for, never null.
	 * @return a new cloned copy of the extension with all sub-elements loaded, never null.
	 */
	public ICloneObjectExtensionLazyMultiStatefulObject getFullCloneObjectExtension(ICloneObject cloneObject,
			ICloneObjectExtensionLazyMultiStatefulObject extension);

	/**
	 * Takes an {@link IClone} object and an {@link ICloneObjectExtension} interface class and
	 * retrieves the extension for the given interface class from the clone.
	 * <br>
	 * Convenience method.
	 * <br>
	 * It is then passed to the {@link #getFullCloneObjectExtension(ICloneObject, ICloneObjectExtensionLazyMultiStatefulObject)}
	 * method.
	 * <br>
	 * If no extension of that interface class type is added, NULL is returned. 
	 * 
	 * @param cloneObject the clone object to which this extension is added, never null.
	 * @param extensionClass the {@link ICloneObjectExtension} interface class for which the currently
	 * 		added extension should be fully loaded, never null.
	 * @return a new cloned copy of the extension with all sub-elements loaded or NULL if no extension
	 * 		for that interface class was present.
	 * 
	 * @see #getFullCloneObjectExtension(ICloneObject, ICloneObjectExtensionLazyMultiStatefulObject)
	 */
	public ICloneObjectExtensionLazyMultiStatefulObject getFullCloneObjectExtension(ICloneObject cloneObject,
			Class<? extends ICloneObjectExtension> extensionClass);

	/**
	 * Adds a given clone to the data store. If the clone already exists, nothing is done.
	 * <br>
	 * A {@link ICloneGroup} or {@link ICloneFile} referenced by the clone instance is not
	 * automatically persisted!
	 * <p>
	 * If the clone contains an {@link ICloneModificationHistoryExtension} any {@link CloneDiff} objects
	 * it contains will be added to the internal modification history for this clone. The history can
	 * be retrieved via a call to {@link #getFullCloneObjectExtension(ICloneObject, Class)}.
	 * <p>
	 * If {@link LockMode} does not disable events, this clone will be included in the
	 * {@link CloneModificationEvent#getAddedClones()} list of a {@link CloneModificationEvent}
	 * once the lock is released.
	 * <p>
	 * An exclusive write lock is required before this method may be called.
	 * 
	 * @param clone the clone to store, never null
	 * @throws StoreLockingException thrown if the current thread does not hold an exclusive write lock.
	 * 
	 * @see ICloneModificationHistoryExtension
	 * @see #acquireWriteLock(LockMode)
	 * @see LockMode
	 */
	public void addClone(IClone clone) throws StoreLockingException;

	/**
	 * Convenience method, see: {@link #addClone(IClone)}.
	 * <p>
	 * An exclusive write lock is required before this method may be called.
	 * 
	 * @param clones list of clones to add, may be empty, never null.
	 * @throws StoreLockingException thrown if the current thread does not hold an exclusive write lock.
	 */
	public void addClones(List<IClone> clones) throws StoreLockingException;

	/**
	 * Adds the given {@link ICloneGroup} to the data store.
	 * <p>
	 * An exclusive write lock is required before this method may be called.
	 * 
	 * @param group the group to add, never null.
	 * @throws StoreLockingException thrown if the current thread does not hold an exclusive write lock.
	 */
	public void addCloneGroup(ICloneGroup group) throws StoreLockingException;

	/**
	 * Updates a given clone in the data store. Throws an exception if the clone doesn't exist.
	 * <p>
	 * If the clone contains an {@link ICloneModificationHistoryExtension} any {@link CloneDiff} objects
	 * it contains will be added to the internal modification history for this clone. The history can
	 * be retrieved via a call to {@link #getFullCloneObjectExtension(ICloneObject, Class)}.
	 * <p>
	 * If {@link LockMode} does not disable events, this clone will be included in the
	 * {@link CloneModificationEvent#getMovedClones()} and/or {@link CloneModificationEvent#getModifiedClones()}
	 * list of a {@link CloneModificationEvent} once the lock is released. Depending on the given {@link UpdateMode}.
	 * <p>
	 * An exclusive write lock is required before this method may be called.
	 *
	 * @param clone the clone to update, never null
	 * @param mode specifies how this clone was modified, see: {@link UpdateMode}, never null.
	 * 
	 * @throws StoreLockingException thrown if the current thread does not hold an exclusive write lock.
	 * @throws IllegalArgumentException thrown if a clone with that uuid doesn't exists in the data store
	 * 
	 * @see ICloneModificationHistoryExtension
	 * @see LockMode
	 * @see UpdateMode
	 */
	public void updateClone(IClone clone, UpdateMode mode) throws StoreLockingException, IllegalArgumentException;

	/**
	 * Convenience method, see: {@link #updateClone(IClone, UpdateMode)}.
	 * <p>
	 * An exclusive write lock is required before this method may be called.
	 * 
	 * @throws StoreLockingException thrown if the current thread does not hold an exclusive write lock.
	 * @throws IllegalArgumentException thrown if a clone with that uuid doesn't exists in the data store
	 */
	public void updateClones(List<IClone> clones, UpdateMode mode) throws StoreLockingException,
			IllegalArgumentException;

	/**
	 * Updates the given group in the data store.
	 * <p>
	 * An exclusive write lock is required before this method may be called.
	 * 
	 * @param group the group to update, never null.
	 * @throws StoreLockingException thrown if the current thread does not hold an exclusive write lock.
	 */
	public void updateCloneGroup(ICloneGroup group) throws StoreLockingException;

	/**
	 * Removes the given clone from the data store. If it does not exist, the call is ignored.
	 * <p>
	 * If {@link LockMode} does not disable events, this clone will be included in the
	 * {@link CloneModificationEvent#getRemovedClones()} list of a {@link CloneModificationEvent}
	 * once the lock is released.
	 * <p>
	 * An exclusive write lock is required before this method may be called.
	 * 
	 * @param clone the clone to remove, never null
	 * @throws StoreLockingException thrown if the current thread does not hold an exclusive write lock.
	 * 
	 * @see LockMode
	 */
	public void removeClone(IClone clone) throws StoreLockingException;

	/**
	 * Convenience method, see: {@link #removeClone(IClone)}.
	 * <p>
	 * An exclusive write lock is required before this method may be called.
	 * 
	 * @throws StoreLockingException thrown if the current thread does not hold an exclusive write lock.
	 */
	public void removeClones(List<IClone> clones) throws StoreLockingException;

	/**
	 * TODO:/FIXME: clarify semantics of this operation.
	 * Does it delete the clone members of this group? Does it reset them to no-group?
	 * <p>
	 * An exclusive write lock is required before this method may be called.
	 * 
	 * @param group
	 * @throws StoreLockingException thrown if the current thread does not hold an exclusive write lock.
	 */
	public void removeCloneGroup(ICloneGroup group) throws StoreLockingException;

	/**
	 * Moves the given clone file to the given project and path.
	 * <p>
	 * This method is used to indicate that:
	 * <ul>
	 * 	<li>the file was renamed or moved</li>
	 *  <li>a folder/package which contains the file was renamed or moved</li>
	 *  <li>the project which contains the file was renamed</li>
	 * </ul>
	 * A clone file's <em>uuid</em> is not affected by a move.
	 * <br>
	 * <b>The given {@link ICloneFile} instance itself is not updated.</b>
	 * <p>
	 * This change is instantly persisted. A call to {@link #persistData(ICloneFile)} is not needed.
	 * <p>
	 * An exclusive write lock is required before this method may be called.
	 * 
	 * @param cloneFile the clone file which should be moved, never null.
	 * @param project the new project name for the file, never null.
	 * @param path the new project relative path for the file, never null.
	 * @throws StoreLockingException thrown if the caller does not currently hold an exclusive write lock
	 */
	public void moveCloneFile(ICloneFile cloneFile, String project, String path) throws StoreLockingException;

	/**
	 * Stores the current clone data for the given file in persistent storage.
	 * <br>
	 * This should be called when a user saves a modified source file.
	 * <p>
	 * This method will update the {@link ICloneFile#getModificationDate()} and {@link ICloneFile#getSize()}
	 * values.
	 * <br>
	 * <b>The given {@link ICloneFile} instance itself is not updated.</b>
	 * <br>
	 * If the latest version is required, it should be obtained by calling {@link #lookupCloneFile(String)}.
	 * <p>
	 * NOTE: this method may depend on internal caching data which may only be available
	 * if all clones for the given file were loaded once via <em>getClonesByFile()</em>
	 * before any modifications on the clone data were made.
	 * <p>
	 * An exclusive write lock is required before this method may be called.
	 * 
	 * @param file the file in question, never null
	 * @throws StoreLockingException thrown if the caller does not currently hold an exclusive write lock
	 */
	public void persistData(ICloneFile file) throws StoreLockingException;

	/**
	 * Reverts the clone data for the given file to the latest version from persistent storage.
	 * <br>
	 * This is called if a user closes a modified source file without saving it or if he reverts to the
	 * saved version.
	 * <p>
	 * If {@link LockMode} does not disable events, a single {@link CloneModificationEvent} with {@link CloneModificationEvent#isFullModification()}
	 * set to true for this file will be generated for this action once the lock is released.
	 * <p>
	 * An exclusive write lock is required before this method may be called.
	 * 
	 * @param file the file in question, never null
	 * @throws StoreLockingException thrown if the caller does not currently hold an exclusive write lock
	 * 
	 * @see LockMode
	 */
	public void revertData(ICloneFile file) throws StoreLockingException;

	/**
	 * Permanently removes all clones which are associated with the given file from persistent storage
	 * and from all internal cache structures. This action is irreversible.
	 * <p>
	 * This modification is automatically persisted. No call to {@link #persistData(ICloneFile)} is required.
	 * <p>
	 * If {@link LockMode} does not disable events, a single {@link CloneModificationEvent} with {@link CloneModificationEvent#isFullModification()}
	 * set to true for this file will be generated for this action once the lock is released.
	 * <p>
	 * An exclusive write lock is required before this method may be called.
	 * 
	 * @param cloneFile the file in question, never null
	 * @param removeCloneFile if true the clone file handle will also be purged from storage. In that case
	 * 		a future {@link #lookupCloneFileByPath(String, String, boolean, boolean)} for the project and path
	 * 		of this file would return a new {@link ICloneFile} with a new unique uuid and a call to
	 * 		{@link #lookupCloneFile(String)} with the uuid of this clone file would fail.
	 * 		If this is false, the clone file entry will be retained and only the clone data for the file
	 * 		will be purged.
	 * @throws StoreLockingException thrown if thread is now the owner of the current exclusive write lock
	 * 
	 * @see LockMode
	 */
	public void purgeData(ICloneFile cloneFile, boolean removeCloneFile) throws StoreLockingException;

	/**
	 * Retrieves the persisted content of the given file. The content represents the state of the file
	 * at the time of the last call of {@link #persistData(ICloneFile)}.
	 * <p>
	 * An exclusive write lock is required before this method may be called.
	 * 
	 * @param file the clone file to retrieve the content for, never null.
	 * @return the persisted clone content or NULL if no content is available.
	 * @throws StoreLockingException thrown if the caller does not currently hold an exclusive write lock
	 * 
	 * @see #persistData(ICloneFile)
	 */
	public String getPersistedCloneFileContent(ICloneFile file) throws StoreLockingException;

	/**
	 * Retrieves the persisted clone entries for the given file.
	 * <br>
	 * The result represents the clone data state for the file at the time of the last call
	 * of {@link #persistData(ICloneFile)}.
	 * <p>
	 * Use of this method will not perform any checks for external modifications of the file
	 * and will not trigger any reconciliation operations.
	 * <p>
	 * This method may be slower than {@link #getClonesByFile(String)}.
	 * 
	 * @param fileUuid UUID of the {@link ICloneFile} to retrieve the clone data for, never null.
	 * @return a list of persisted {@link IClone} instances for this file, may be empty, never null.
	 */
	public List<IClone> getPersistedClonesForFile(String fileUuid);

	/**
	 * Acquires an exclusive write lock for the entire Store repository.
	 * <p>
	 * If a user of this store provider has the intention of modifying <b>ANY</b> of the data stored by this
	 * provider it is mandatory that an exclusive write lock is obtained <b>BEFORE</b> any data
	 * is retrieved from the store provider.
	 * <p>
	 * <b>Read locks are automatically acquired and released</b> by the lookup/get methods. Each read lock is only
	 * held for the duration of the method call. If any read only user of the storage provider needs
	 * to ensure that data is not changed over a specific period (i.e. to guarantee repeatable reads),
	 * such a read only user should also acquire an exclusive write lock.
	 * <p>
	 * The owner of the registered {@link IStoreProviderWriteLockHook}, if any, may be holding an implicit write lock
	 * and may be modifying local cached data while no other party is holding a write lock. Such modified data will
	 * be transfered back to the store provider when a 3rd party requests a write lock. Acquiring and releasing a
	 * write lock is therefore required if a 3rd party needs to ensure that it will obtain the latest version of
	 * the data.
	 * <p>
	 * <b>This method will block</b> until a lock can be obtained.
	 * <p>
	 * Exclusive write locks are <u>reentrant</u>. However, the <u>{@link LockMode} can't be changed</u> during a re-entry.
	 * The lock mode used when initially acquiring the exclusive lock will remain in effect until the lock is released.
	 * <p>
	 * See {@link LockMode} for a description of the possible modes.
	 * <p>
	 * <b>NOTE:</b> In situations where a client needs additional locks it is advisable to ensure that either:
	 * <ul>
	 * 	<li>No other thread which may need exclusive {@link IStoreProvider} write locks is also obtaining any
	 * 		of these additional locks.</li>
	 * 	<li>All threads will always obtain the locks in the same order.</li>
	 * </ul>
	 * Otherwise deadlocks may occur.
	 * 
	 * @param mode configures specific behaviour of this lock, null is equal to LockMode.DEFAULT.
	 * @throws StoreLockingException If maximum number of allowed re-entries is exceeded.
	 * 		Please also refer to the design considerations section in {@link IStoreProvider}.
	 * 
	 * @see #releaseWriteLock()
	 * @see LockMode
	 */
	public void acquireWriteLock(LockMode mode) throws StoreLockingException;

	/**
	 * Similar to {@link #acquireWriteLock(LockMode)}
	 * but does not block in case the lock can not be obtained.
	 * <br>
	 * If this method returns <em>true</em> an exclusive write lock was granted to this thread.
	 * <br>
	 * <em>False</em> is returned if another thread already holds an exclusive write lock. In which case, this method
	 * has no effect.
	 * <p>
	 * <b>NOTE:</b> This method does not honour any fairness conditions. The lock is re-entrant.
	 * <br>
	 * <b>NOTE:</b> Please refer to the javadoc for {@link #acquireWriteLock(LockMode)}.
	 * 
	 * @param mode configures specific behaviour of this lock, null is equal to LockMode.DEFAULT.
	 * @param maxWait the maximum amount of time in milliseconds that the caller is willing to wait to acquire a
	 * 		lock. Set to 0 to try once and fail instantly if the lock is currently held.
	 * @return true if the lock could be obtained, false if the lock is currently held by another thread.
	 * @throws StoreLockingException If maximum number of allowed re-entries is exceeded.
	 * 		Please also refer to the design considerations section in {@link IStoreProvider}.
	 * @throws InterruptedException If the thread is interrupted while waiting for the lock.
	 * 
	 * @see #acquireWriteLock(LockMode)
	 */
	public boolean acquireWriteLockNonBlocking(LockMode mode, long maxWait) throws StoreLockingException,
			InterruptedException;

	/**
	 * Releases the exclusive write lock.
	 * <p>
	 * Unless the generation of {@link CloneModificationEvent}s was disabled at lock acquisition time by the
	 * given {@link LockMode}, a call to this method may generate one or more {@link CloneModificationEvent}s.
	 * <br>
	 * The <b>exclusive lock is relinquished first</b>, then all interested parties are notified about the modification
	 * and then the control is returned to the caller.  
	 * <p>
	 * This method may only be called by the current holder of the write lock, otherwise an error is logged and
	 * the call is ignored.
	 * 
	 * @see #acquireWriteLock(LockMode)
	 * @see LockMode
	 */
	public void releaseWriteLock();

	/**
	 * Checks whether the current thread is holding the exclusive write lock for the store provider.
	 * <br>
	 * This method does not block.
	 * 
	 * @return true if the current thread holds the exclusive write lock for the store provider, false otherwise.
	 */
	public boolean holdingWriteLock();

	/**
	 * Registers a special write lock hook which will receive a callback whenever a user of this
	 * provider requests an exclusive write lock.
	 * <br>
	 * The registered hook will be called on every call to {@link #acquireWriteLock(LockMode)} directly
	 * <b>AFTER</b> the write lock has been granted but before the control is returned to the caller.
	 * <br>
	 * Registration of a hook is optional.
	 * <p>
	 * <b>IMPORTANT:</b> there can be <b>only one registered write lock hook at any given time</b>. Calling this method
	 * 		multiple times will remove any write look hook registered earlier.
	 * <p>
	 * <b>NOTE:</b> registration of a write lock hook is <b>reserved for the <em>CPC Track</em> module</b>.
	 * <p>
	 * Rationale:
	 * <blockquote>
	 * The majority of all clone data modifications is made by the <em>CPC Track</em> module. A single
	 * user action, i.e. a refactoring or source reformat can trigger thousands of clone updates. There are
	 * also some user actions inside of an editor which can potentially create a large number of clone
	 * data updates.
	 * <br>
	 * In theory the existing API would be enough even for these extreme cases. However, due to performance
	 * considerations it seems highly prudent to allow a certain amount of clone modification caching inside
	 * of the <em>CPC Track</em> module.
	 * <br>
	 * The write lock hook is meant as a best effort, extended-write lock for the <em>CPC Track</em> module. Once it
	 * has registered its hook it can release any exclusive lock which it might be holding on the clone data
	 * and can still continue to internally update and modify its cached clone data. Should any other party
	 * wish to modify clone data, the store provider will acquire the exclusive write lock and will then
	 * delegate control to the registered hook which in turn will give the <em>CPC Track</em> module the chance
	 * to transmit all its clone modifications back to the store provider.
	 * <br>
	 * The effect is similar to a situation where the <em>CPC Track</em> module keeps a permanent exclusive lock on
	 * the store provider and only temporarily relinquishes it, whenever some other code needs to access
	 * the store provider exclusively.
	 * </blockquote>
	 * 
	 * @param hook a write lock hook reference, never null.
	 */
	public void setWriteLockHook(IStoreProviderWriteLockHook hook);

	/**
	 * All users of the store provider API are encouraged to give certain hints to the store provider implementation
	 * which can internally be used to improve performance and to reduce memory usage.
	 * <p>
	 * This method should be called if it is unlikely that the clone data for a specific file will be needed
	 * again shortly. This is typically the case if the user closes the corresponding file.
	 * <p>
	 * This method does not affect the persistence state of clone data in any way and does not lead to data loss.
	 * If the clone data for the given clone file is dirty, it will remain in cache. 
	 * 
	 * @param file the clone file which is most likely not being accessed again in the near future
	 */
	public void hintPurgeCache(ICloneFile file);

	/**
	 * All users of the store provider API are encouraged to give certain hints to the store provider implementation
	 * which can internally be used to improve performance and to reduce memory usage.
	 * <p>
	 * This method should be called if it is unlikely that the clone data for a specific clone group will be needed
	 * again shortly.
	 * <p>
	 * This method does not affect the persistence state of clone data in any way and does not lead to data loss.
	 * If the clone data for the given clone group is dirty, it will remain in cache. 
	 * 
	 * @param group the clone group which is most likely not being accessed again in the near future
	 */
	public void hintPurgeCache(ICloneGroup group);

	/**
	 * Called to indicate that the store provider implementation should purge all it's internal caches.
	 * <br>
	 * This method may be called if another CPC part detects a low memory condition.
	 * <p>
	 * This method does not affect the persistence state of clone data in any way and does not lead to data loss.
	 * If the clone data for the given clone group is dirty, it will remain in cache.
	 * <p>
	 * Store provider implementations which do not allocate potentially large in-memory caches, do not need to
	 * do anything in this method.
	 * <p>
	 * All store provider implementations are encouraged to keep track of available memory on their own and to
	 * reduce their memory consumption if the available system resources reach critical levels.
	 */
	public void purgeCache();

	/**
	 * Called to indicate that the store provider implementation should delete <b>ALL</b> data.
	 * <br>
	 * The result of a call to this method is equivalent to a freshly installed store provider.
	 * No data of prior sessions may remain in storage.
	 * <p>
	 * This method is typically called in unit tests to ensure a defined starting state.
	 * <p>
	 * If {@link LockMode} does not disable events, a single {@link CloneModificationEvent} with {@link CloneModificationEvent#isFullModification()}
	 * set to true and {@link CloneModificationEvent#getCloneFile()} set to NULL will be generated for this action
	 * once the lock is released.
	 * <p>
	 * An exclusive write lock is required before this method may be called.
	 *
	 * @throws StoreLockingException thrown if thread is now the owner of the current exclusive write lock
	 * 
	 * @see LockMode
	 */
	public void purgeData() throws StoreLockingException;

}
