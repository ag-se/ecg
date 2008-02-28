package org.electrocodeogram.cpc.store.provider;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.CloneDiff;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.ICloneGroup;
import org.electrocodeogram.cpc.core.api.data.ICloneObject;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.data.collection.ICloneFileInterfaces;
import org.electrocodeogram.cpc.core.api.data.collection.ICloneGroupInterfaces;
import org.electrocodeogram.cpc.core.api.data.collection.ICloneInterfaces;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionLazyMultiStatefulObject;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionStatefulObject;
import org.electrocodeogram.cpc.core.api.data.special.ICreatorCloneFile;
import org.electrocodeogram.cpc.core.api.data.special.IRemoteStoreCloneFile;
import org.electrocodeogram.cpc.core.api.data.special.IStatefulObject;
import org.electrocodeogram.cpc.core.api.data.special.IStoreCloneModificationHistoryExtension;
import org.electrocodeogram.cpc.core.api.data.special.IStoreCloneObject;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.hub.event.CloneModificationEvent;
import org.electrocodeogram.cpc.core.api.hub.event.ClonePersistenceEvent;
import org.electrocodeogram.cpc.core.api.provider.IManagableProvider;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IDebuggableStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IRemotableStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProviderWriteLockHook;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


/**
 * Abstract default implementation of {@link IStoreProvider}.
 * <br>
 * Concrete {@link IStoreProvider} implementations are encouraged to extend this class.
 * <p>
 * The abstract implementation takes care of issues like concurrency control, in memory caching,
 * event generation, integrity checking, ...
 * 
 * @author vw
 * 
 * @see IStoreProvider
 * @see IRemotableStoreProvider
 * @see IDebuggableStoreProvider
 */
public abstract class AbstractStoreProvider implements IRemotableStoreProvider, IDebuggableStoreProvider,
		IManagableProvider
{
	private static Log log = LogFactory.getLog(AbstractStoreProvider.class);

	protected static final List<IClone> EMPTY_CLONE_LIST = Collections.unmodifiableList(new ArrayList<IClone>(0));

	/**
	 * After this number of reentrant exclusive write locks we start to complain.
	 * The value could really be much higher, but there is probably no use for higher values.
	 * We really need some value here though to prevent overruns of the depth counter.
	 */
	protected static final int MAX_LOCK_DEPTH = 1000;

	/**
	 * Central lock for shared read and exclusive write locks.
	 */
	protected final ReentrantReadWriteLock lock;

	/**
	 * Write lock hook instance which was set by {@link IStoreProvider#setWriteLockHook(IStoreProviderWriteLockHook)}.
	 */
	protected IStoreProviderWriteLockHook writeLockHook;

	/**
	 * Special counter which counts the number of times the current write lock holding thread has acquired
	 * the write lock. This is needed to make the IStoreProvider write lock reentrant.<br/>
	 * <br/>
	 * For each call to {@link IStoreProvider#acquireWriteLock(org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode)}
	 * the value is increased by one. For each call to {@link IStoreProvider#releaseWriteLock()} the level is
	 * decreased by one.
	 * The exclusive lock is only released once the value reaches 0.
	 */
	protected int writeLockDepth = 0;

	/**
	 * For reentrant write locks we need to keep track of the {@link IStoreProvider.LockMode}.<br/>
	 * For now we throw an exception if different lock modes are used.
	 */
	protected LockMode writeLockMode = null;

	/**
	 * Lookup structure for file path =&gt; <em>CloneFile</em> lookups.
	 * This is a cache structure which does not need to be persisted and which is never purged.
	 */
	protected Map<String, String> pathToFileUuidRegistry;

	/**
	 * Caches old file path to uuid data.<br/>
	 * Needed for lookups in cases where the underlying file might already have been
	 * moved but where the client does only know the file's old path and not it's uuid.<br/>
	 * <br/>
	 * NOTE: this data is NOT persisted and currently not cleared. It will live as long as the IDE
	 * runs. In normal situations that should not be a problem. For users who never close
	 * their IDE it might. But it seems very unlikely that this could become a serious
	 * storage issue.
	 */
	protected Map<String, String> oldPathToFileUuidCache;

	/**
	 * Lookup structure for file uuid =&gt; <em>CloneFile</em> lookups.
	 */
	protected Map<String, ICloneFile> fileUuidToFileRegistry;

	/**
	 * Lookup structure for clone uuid =&gt; <em>Clone</em> lookups.
	 */
	protected Map<String, IClone> cloneUuidToCloneRegistry;

	/**
	 * Lookup structure for group uuid =&gt; <em>CloneGroup</em> lookups.
	 */
	protected Map<String, ICloneGroup> groupUuidToGroupRegistry;

	/**
	 * Lookup structure for file uuid =&gt; list of clones in that file
	 * 
	 * NOTE: the TreeSet underlying the sorted set will break if one tries to access an entry
	 * 		by providing a Clone instance for which the position has been modified since it
	 * 		was retrieved from the set.
	 * 		In other words, it is crucial to always retrieve the currently cached instance
	 * 		via <em>cloneUuidToCloneRegistry</em> and use that instance to do a removal from
	 * 		the sorted set.
	 */
	protected Map<String, SortedSet<IClone>> fileUuidToClonesRegistry;

	/**
	 * Lookup structure for group uuid =&gt; list of clones in that group
	 */
	protected Map<String, Set<IClone>> groupUuidToClonesRegistry;

	/**
	 * Maps which map clone file uuids to lists of clone uuids for modifications of the given type.<br/>
	 * These lists are used to keep track of clone modifications which have taken place
	 * within an exclusive write lock session of a client.<br/>
	 * If {@link CloneModificationEvent} creation was enabled (see: {@link LockMode}),
	 * the clone data from this lists is converted into a {@link CloneModificationEvent}
	 * once the client relinquishes its lock.<br/>
	 * The lists are cleared at the end of each exclusive lock.
	 */
	protected Map<String, List<String>> transactionAddedClones;
	protected Map<String, List<String>> transactionMovedClones;
	protected Map<String, List<String>> transactionModifiedClones;
	protected Map<String, List<String>> transactionRemovedClones;

	/**
	 * A set of file uuids for files which were completely modified.
	 */
	protected Set<String> transactionFullModifications;

	/**
	 * Keeps references to {@link ICloneFile} entries which have been purged but which
	 * may still be required for event generation at the end of the current exclusive lock.<br/>
	 * <br/>
	 * This buffer is cleared by {@link IStoreProvider#releaseWriteLock()}.
	 */
	protected Map<String, ICloneFile> transactionDeletedFileDataBuffer;

	/**
	 * Keeps references to {@link IClone} entries which have been removed but which
	 * may still be required for event generation at the end of the current exclusive lock.<br/>
	 * <br/>
	 * This buffer is cleared by {@link IStoreProvider#releaseWriteLock()}.
	 */
	protected Map<String, IClone> transactionDeletedCloneDataBuffer;

	/**
	 * Keeps <b>cloned</b> copies of {@link ICloneModificationHistoryExtension}s which contained new {@link CloneDiff}
	 * entries for this transaction but which were "cleaned out" by a call to {@link AbstractStoreProvider#persistData(ICloneFile)}.<br/>
	 * They need to be buffered because the {@link CloneModificationEvent} is supposed to contain clones with a history
	 * that matches the latest modification.<br/>
	 * <br/>
	 * This buffer is cleared by {@link IStoreProvider#releaseWriteLock()}.
	 */
	//protected Map<String, ICloneModificationHistoryExtension> transactionPersistedModificationHistoryBuffer;
	//OLD: we're now simply not purging out the modification history unless it contains only old entries.
	/**
	 * Whether a {@link IStoreProvider#purgeData()} call removed all clone data.
	 */
	protected boolean transactionRemovedAll;

	/**
	 * Whether {@link CloneModificationEvent} creation was enabled (see: {@link LockMode}) for this lock.
	 */
	protected boolean cloneModificationEventEnabled;

	/*
	 * Great care is taken to ensure that there is always only one instance for each uuid in cache.
	 * Meaning i.e. that all dirty checking can be done by checking all values of the
	 * <em>cloneUuidToCloneRegistry</em>. If a clone is stored in one of the sets of
	 * <em>fileUuidToClonesRegistry</em> or <em>groupUuidToClonesRegistry</em> it will always
	 * also be stored in <em>cloneUuidToCloneRegistry</em> and all three will point to the same instance
	 * for a particular uuid.
	 */

	protected boolean standaloneTestMode = false;

	protected ICloneFactoryProvider cloneFactoryProvider = null;

	public AbstractStoreProvider()
	{
		if (log.isTraceEnabled())
			log.trace("AbstractStoreProvider()");

		lock = new ReentrantReadWriteLock();

		/*
		 * for path => file uuid
		 */
		pathToFileUuidRegistry = new Hashtable<String, String>();
		oldPathToFileUuidCache = new Hashtable<String, String>();

		/*
		 * for X uuid => X object
		 */
		fileUuidToFileRegistry = new Hashtable<String, ICloneFile>();
		cloneUuidToCloneRegistry = new Hashtable<String, IClone>();
		groupUuidToGroupRegistry = new Hashtable<String, ICloneGroup>();

		/*
		 * data caches
		 */
		fileUuidToClonesRegistry = new Hashtable<String, SortedSet<IClone>>();
		groupUuidToClonesRegistry = new Hashtable<String, Set<IClone>>();

		/*
		 * misc data
		 */

		cloneFactoryProvider = (ICloneFactoryProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				ICloneFactoryProvider.class);

		/*
		 * Create clone modification event lists.
		 */
		transactionAddedClones = new HashMap<String, List<String>>(5);
		transactionMovedClones = new HashMap<String, List<String>>(5);
		transactionModifiedClones = new HashMap<String, List<String>>(5);
		transactionRemovedClones = new HashMap<String, List<String>>(5);

		transactionFullModifications = new HashSet<String>(5);
		transactionDeletedFileDataBuffer = new HashMap<String, ICloneFile>(10);
		transactionDeletedCloneDataBuffer = new HashMap<String, IClone>(20);
		transactionRemovedAll = false;
		cloneModificationEventEnabled = false;
	}

	protected AbstractStoreProvider(boolean standaloneTestMode)
	{
		this();

		this.standaloneTestMode = standaloneTestMode;
	}

	/*
	 * IStoreProvider Methods
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider#lookupCloneFileByPath(java.lang.String, java.lang.String, boolean, boolean)
	 */
	@Override
	public ICloneFile lookupCloneFileByPath(String project, String filePath, boolean createNewUuidIfNeeded,
			boolean followFileMove)
	{
		if (log.isTraceEnabled())
			log.trace("lookupCloneFileByPath()- project: " + project + ", filePath: " + filePath
					+ ", createHandleIfNeeded: " + createNewUuidIfNeeded + ", followFileMove: " + followFileMove);
		assert (project != null && filePath != null);

		ICloneFile file = null;
		ICloneFile result = null;
		String fullFilePath = project + "/" + filePath;

		rLock("lookupCloneFileByPath");
		try
		{
			//make sure no one else tries to do a file lookup at the same time
			//we might otherwise end up trying to parse the same file concurrently
			synchronized (pathToFileUuidRegistry)
			{
				//check if we already know the file
				String fileUuid = pathToFileUuidRegistry.get(fullFilePath);
				if (fileUuid != null)
					file = fileUuidToFileRegistry.get(fileUuid);

				if (file == null)
				{
					//nope, we need to create a new CloneFile instance for this file.
					log.trace("lookupCloneFileByPath() - cache miss for CloneFile");

					//try to find the file in our data store
					file = subGetCloneFileByPath(project, filePath);

					if (file == null)
					{
						//we don't have that file path in our data store, that's strange
						//this could mean that the file is new or that it was moved/renamed
						//outside of eclipse

						if (log.isDebugEnabled())
							log.debug("lookupCloneFileByPath() - file path is not in data store - project: " + project
									+ ", path: " + filePath);

						//this returns a new clonefile object, either with it's original uuid
						//or with a new one if no uuid could be extracted from the file.
						file = createCloneFileFromFile(project, filePath, createNewUuidIfNeeded);

						if (file == null)
						{
							//the path does either not exist or is not readable

							if (followFileMove)
							{
								/*
								 * Check if this might be a file move.
								 * We do this check here and not in createCloneFileFromFile() because a new
								 * file might already have taken the place of the old file. In such a
								 * situation we give precedence to the new file.
								 * Only if there is really no file at the new location (or at least no file
								 * with an existing file uuid) do we fall back to the moved file cache.
								 */
								String movedFileUUid = oldPathToFileUuidCache.get(fullFilePath);
								if (movedFileUUid != null)
								{
									if (log.isTraceEnabled())
										log
												.trace("lookupCloneFileByPath() - file was moved to new location - redoing lookup by uuid - file uuid: "
														+ movedFileUUid);

									ICloneFile subResult = lookupCloneFile(movedFileUUid);

									if (log.isTraceEnabled())
										log.trace("lookupCloneFileByPath() - relookup result: " + subResult);

									return subResult;
								}
							}

							log.trace("lookupCloneFileByPath() - result: NULL");

							return null;
						}

						//if we were able to extract a uuid we might have to update other data
						//(i.e. modify the path of the old entry for this uuid)
						if (!file.isMarked())
							//it was possible to extract the file's old UUID, it is therefore not a new file.
							file = processPotentialFileMove(file);

						if (file == null)
						{
							log.trace("lookupCloneFileByPath() - file copy detected, restarting file lookup.");
							//TODO: this might be a place where we could treat the file copying like a copy&paste action.
							return lookupCloneFileByPath(project, filePath, createNewUuidIfNeeded, followFileMove);
						}

						//now update or add the new clonefile to the data store
						subPersistCloneFile(file);
					}

					//store the new file object in cache
					fileUuidToFileRegistry.put(file.getUuid(), file);
					pathToFileUuidRegistry.put(fullFilePath, file.getUuid());

					if (CPCCorePlugin.isDebugChecking())
					{
						//do a double check to verify that the ICloneFile's UUID matches the
						//persistent UUID property of the underlying IFile.
						IFile fileHandle = CoreFileUtils.getFileForCloneFile(file);
						if (fileHandle != null)
						{
							String persistentUuid = CoreFileUtils.getFileUuidProperty(fileHandle);
							if (!file.getUuid().equals(persistentUuid))
							{
								log.error(
										"lookupCloneFileByPath() - clone file uuid and persistent uuid property do not match - clone file: "
												+ file + ", persistentUuid: " + persistentUuid, new Throwable());

								//check if a persistent uuid has been set at all
								if (persistentUuid == null)
								{
									log.info("lookupCloneFileByPath() - persistent uuid was not set, setting it now.");
									CoreFileUtils.setFileUuidProperty(fileHandle, file.getUuid());
								}
							}
						}
					}
				}
			}

			//now clone it
			if (file != null)
				result = (ICloneFile) file.clone();
		}
		catch (CloneNotSupportedException e)
		{
			log.fatal("lookupCloneFileByPath() - cloning of result failed - project: " + project + ", filePath: "
					+ filePath + ", file: " + file + " - " + e, e);
		}
		finally
		{
			rUnLock("lookupCloneFileByPath");
		}

		//debug checking
		if (CPCCorePlugin.isDebugChecking())
		{
			//make sure that the project and path of the file entry match the
			//request
			if (!file.getProject().equals(project) || !file.getPath().equals(filePath))
			{
				log.fatal(
						"lookupCloneFileByPath() - clonefile project and path don't match original request - project: "
								+ project + ", filePath: " + filePath + ", file: " + file, new Throwable());
			}
		}

		if (log.isTraceEnabled())
			log.trace("lookupCloneFileByPath() - result: " + result);

		return result;
	}

	protected abstract ICloneFile subGetCloneFileByPath(String project, String filePath);

	/**
	 * Persists (add or update) the given {@link ICloneFile} instance.<br/>
	 * No clone data is persisted by this method.<br/>
	 * <br/>
	 * <b>IMPORTANT NOTE:</b> The caller of this method might <b>not</b> always hold an exclusive write lock
	 * 		but may sometimes <u>only be holding a read lock</u>. Any implementation will therefore
	 * 		need to ensure proper thread safety.<br/>
	 * 		The implementation <b>must not</b> try to acquire a store provider write lock.
	 * 
	 * @param file the clone file to instance to add or update in/to persistent storage, never null.
	 * @return true on success, false on failure.
	 * 		Failures are logged as errors by the method implementation.
	 */
	protected abstract boolean subPersistCloneFile(ICloneFile file);

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.store.IStoreProvider#lookupCloneFile(java.lang.String)
	 */
	@Override
	public ICloneFile lookupCloneFile(String uuid)
	{
		if (log.isTraceEnabled())
			log.trace("lookupCloneFile(): " + uuid);
		assert (uuid != null);

		ICloneFile result = null;
		ICloneFile file = _lookupCloneFile(uuid);

		try
		{
			//now clone it
			if (file != null)
				result = (ICloneFile) file.clone();
		}
		catch (CloneNotSupportedException e)
		{
			log
					.fatal("lookupCloneFile() - cloning of result failed - uuid: " + uuid + ", file: " + file + " - "
							+ e, e);
		}

		if (log.isTraceEnabled())
			log.trace("lookupCloneFile() - result: " + result);

		return result;
	}

	protected abstract ICloneFile subGetCloneFileByUuid(String fileUuid);

	/**
	 * Similar to {@link #lookupCloneFile(String)} but returns the internal cached version.
	 * 
	 * @param fileUuid the file uuid to lookup, never null.
	 * @return
	 */
	protected ICloneFile _lookupCloneFile(String fileUuid)
	{
		ICloneFile file = null;

		rLock("_lookupCloneFile");
		try
		{
			//check cache first
			file = fileUuidToFileRegistry.get(fileUuid);

			if (file == null)
			{
				if (log.isTraceEnabled())
					log.trace("_lookupCloneFile(): cache MISS for " + fileUuid);

				//not in cache, get from data store
				file = subGetCloneFileByUuid(fileUuid);

				//and cache it, if it exists
				if (file != null)
					fileUuidToFileRegistry.put(fileUuid, file);
			}
		}
		finally
		{
			rUnLock("_lookupCloneFile");
		}

		return file;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.store.IStoreProvider#lookupClone(java.lang.String)
	 */
	@Override
	public IClone lookupClone(String uuid)
	{
		if (log.isTraceEnabled())
			log.trace("lookupClone(): " + uuid);
		assert (uuid != null);

		IClone clone = null;
		IClone result = null;

		rLock("lookupClone");
		try
		{
			//check cache first
			clone = cloneUuidToCloneRegistry.get(uuid);

			if (clone == null)
			{
				if (log.isTraceEnabled())
					log.trace("lookupClone(): cache MISS for " + uuid);

				//not in cache, get from data store
				clone = subGetCloneByUuid(uuid, null);

				//and cache it, if it exists
				if (clone != null)
					cloneUuidToCloneRegistry.put(uuid, clone);
			}

			//now clone it
			if (clone != null)
				result = (IClone) clone.clone();
		}
		catch (CloneNotSupportedException e)
		{
			log.fatal("lookupClone() - cloning of result failed - uuid: " + uuid + ", clone: " + clone, e);
		}
		finally
		{
			rUnLock("lookupClone");
		}

		if (log.isTraceEnabled())
			log.trace("lookupClone() - result: " + result);

		return result;
	}

	/**
	 * Retrieves the data for a clone from persistent storage. If the file location of the clone is
	 * known it may be provided to improve performance.
	 * 
	 * @param cloneUuid uuid of the clone to load from persistent storage, never null
	 * @param fileUuid uuid of the file the clone is located in, if known. THIS MAY BE NULL
	 * @return clone for the given uuid or NULL if not found
	 */
	protected abstract IClone subGetCloneByUuid(String cloneUuid, String fileUuid);

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.store.IStoreProvider#lookupCloneGroup(java.lang.String)
	 */
	@Override
	public ICloneGroup lookupCloneGroup(String uuid)
	{
		if (log.isTraceEnabled())
			log.trace("lookupCloneGroup(): " + uuid);
		assert (uuid != null);

		ICloneGroup group = null;
		ICloneGroup result = null;

		rLock("lookupCloneGroup");
		try
		{
			//check cache first
			group = groupUuidToGroupRegistry.get(uuid);

			if (group == null)
			{
				if (log.isTraceEnabled())
					log.trace("lookupCloneGroup(): cache MISS for " + uuid);

				//not in cache, get from data store
				group = subGetCloneGroupByUuid(uuid);

				//and cache it, if it exists
				if (group != null)
					groupUuidToGroupRegistry.put(uuid, group);
			}

			//now clone it
			if (group != null)
				result = (ICloneGroup) group.clone();

		}
		catch (CloneNotSupportedException e)
		{
			log.fatal("lookupCloneGroup() - cloning of result failed - uuid: " + uuid + ", group: " + group, e);
		}
		finally
		{
			rUnLock("lookupCloneGroup");
		}

		if (log.isTraceEnabled())
			log.trace("lookupCloneGroup() - result: " + result);

		return result;
	}

	protected abstract ICloneGroup subGetCloneGroupByUuid(String groupUuid);

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.store.IStoreProvider#getClones(org.electrocodeogram.cpc.core.api.data.ICloneFile)
	 */
	@Override
	public List<IClone> getClonesByFile(String fileUuid)
	{
		return getClonesByFile(fileUuid, -1, -1);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.store.IStoreProvider#getClones(org.electrocodeogram.cpc.core.api.data.ICloneFile, int, int)
	 */
	public List<IClone> getClonesByFile(String fileUuid, int startOffset, int endOffset)
	{
		if (log.isTraceEnabled())
			log.trace("getClonesByFile() - uuid: " + fileUuid + ", startOffset: " + startOffset + ", endOffset: "
					+ endOffset);
		assert (fileUuid != null && startOffset >= -1 && (endOffset == -1 || endOffset >= startOffset));

		List<IClone> result = new LinkedList<IClone>();
		//		ICloneFile externallyFileModificationCloneFile = null;

		rLock("getClonesByFile");
		try
		{
			synchronized (fileUuidToClonesRegistry)
			{
				//first check if we have those clones in cache
				SortedSet<IClone> clones = fileUuidToClonesRegistry.get(fileUuid);

				//was it in cache?
				if (clones != null)
				{
					//ok, use cached version
					if (log.isTraceEnabled())
						log.trace("getClonesByFile(): cache HIT - " + clones.size() + " clones");
				}
				else
				{
					//we need to get that data from our data store
					log.trace("getClonesByFile(): cache MISS");

					/*
					 * Next we need to check whether the file might have been externally modified.
					 * This is the case if the modification date of the file on disk and the
					 * modification date of the clone file entry don't match.
					 * 
					 * At this point we can't directly update the clone data because this method
					 * only holds a read lock, but a write lock would be required. However, the used
					 * locking implementation does not allow upgrades from a read-only to a read-write lock.
					 */
					//					if (checkForExternalModification)
					//						externallyFileModificationCloneFile = checkForExternalModification(fileUuid);
					//we only update the cache if there was no external modification
					//					if (externallyFileModificationCloneFile == null)
					//					{
					//we always need to fetch and cache ALL clones
					List<IClone> subClones = subGetClonesByFile(fileUuid);

					//store in cache
					clones = new TreeSet<IClone>();
					for (IClone clone : subClones)
					{
						//things get a bit tricky here, we want to ensure that we have at most one instance
						//for each uuid in cache. (otherwise things like dirty checking will become much more
						//complicated).
						//this means that we have to make sure we only use new instances if we don't have one
						//in cache already

						IClone cachedClone = cloneUuidToCloneRegistry.get(clone.getUuid());
						if (cachedClone != null)
						{
							//use cached instance instead of the one returned by the backend
							clones.add(cachedClone);

							//the cached clone and the one from the storage backend should have
							//exactly the same values at this point if the cached version is not
							//marked as dirty. If not, something is wrong.
							if (!cachedClone.equalsAll((IClone) clone))
							{
								if (!((IStoreCloneObject) cachedClone).isDirty())
									/*
									 * This shouldn't happen.
									 */
									log.error(
											"getClonesByFile() - cached clone instance and instance from backend differ - backend instance: "
													+ clone + " - cached instance: " + cachedClone, new Throwable());
								else
									/*
									 * This is ok. It can happen if a clone is cached by group before this call.
									 * It maybe modified, i.e. resetting origin uuid to null, even if the file is not
									 * cached. As the cached instance is marked as dirty, it is only to be expected
									 * that it differs from the backend instance. 
									 */
									log
											.debug("getClonesByFile() - cached clone instance and instance from backend differ - backend instance: "
													+ clone + " - cached instance: " + cachedClone);
							}
						}
						else
						{
							//not cached yet, use new instance
							clones.add(clone);

							//we also cache this clone individually
							cloneUuidToCloneRegistry.put(clone.getUuid(), clone);
						}

					}
					fileUuidToClonesRegistry.put(fileUuid, clones);
					//					}
				}

				/*
				 * Now clones is filled (either from cache or from data store).
				 * Clones can be null if an external modification was detected.
				 */

				//Append to result, run through all clones, they are sorted by start offset number
				//But only if there was no external modification.
				//				if (externallyFileModificationCloneFile == null)
				//				{
				for (IClone clone : clones)
				{
					if ((startOffset != -1) && (clone.getEndOffset() < startOffset))
					{
						//this clone is located in front of our range, we're not interested in it
						continue;
					}

					if (endOffset != -1)
					{
						//we have an additional end offset restriction, skip all clones which do not match that
						if (clone.getOffset() > endOffset)
						{
							//ok, as the clones are ordered by start offset, there should be no more
							//clones which we're interested in
							break;
						}
					}

					try
					{
						result.add((IClone) clone.clone());
					}
					catch (CloneNotSupportedException e)
					{
						//should never happen
						log.fatal("getClonesByFile() - unable to clone Clone instance - uuid: " + fileUuid
								+ ", startOffset: " + startOffset + ", endOffset: " + endOffset + " - clone: " + clone,
								e);
					}
				}
				//				}
			}
		}
		finally
		{
			rUnLock("getClonesByFile");
		}

		//		if (externallyFileModificationCloneFile != null)
		//		{
		//			if (log.isDebugEnabled())
		//				log.debug("getClonesByFile() - external modification detected, running reconciler.");
		//
		//			//ok, the file was externally modified, lets try to reconcile the modification
		//			//NOTE: this method internally acquires a write lock, make sure it's not called from within
		//			//		a read locked code part!
		//			boolean wasModified = reconcileExternalModification(externallyFileModificationCloneFile);
		//
		//			if (wasModified && log.isTraceEnabled())
		//				log.trace("getClonesByFile() - clone data was modified, re-executing lookup.");
		//
		//			//We need to redo the lookup here, no matter what state wasModified holds.
		//			//We didn't update the cache and the results list earlier, so even if nothing was modified
		//			//those still need to be retrieved again.
		//			result = getClonesByFile(fileUuid, startOffset, endOffset, false);
		//		}

		/*
		 * Optional debug check, make sure that there are no double entries in the clone list.
		 * Each clone should only appear once.
		 */
		//TODO: remove this?
		if (CPCCorePlugin.isDebugChecking())
		{
			//we're in debug mode, check that the number of elements does not decrease when we
			//convert the clone list into a set.
			Set<IClone> tmpSet = new HashSet<IClone>(result);
			if (tmpSet.size() != result.size())
			{
				//this should never happen. There is at least one clone which appears twice in the list
				log.fatal("getClonesByFile() - double entry in result list - list size: " + result.size()
						+ ", set size: " + tmpSet.size() + " - result: " + result, new Throwable());
				assert (false);
			}
		}

		if (log.isTraceEnabled())
			log.trace("getClonesByFile() - result: " + result);

		return result;
	}

	/**
	 * Returns a list of all clones contained in the file with the given file uuid.<br/>
	 * All returned instances will be cloned before they are passed to any storage provider user.<br/>
	 * The returned instances themselves may be held in a cache structure by the <em>AbstractStoreProvider</em>.<br/>
	 * If no file with the given UUID exists, an empty list is returned.<br/>
	 * <br/>
	 * This method may be called concurrently. The caller is only holding a shared read lock on the store provider.
	 * 
	 * @param fileUuid the file uuid to retrieve all clones for, never null
	 * @return a list with all clones contained in the file, never null
	 */
	protected abstract List<IClone> subGetClonesByFile(String fileUuid);

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.store.IStoreProvider#getClones(org.electrocodeogram.cpc.core.api.data.ICloneGroup)
	 */
	@Override
	public List<IClone> getClonesByGroup(String groupUuid)
	{
		if (log.isTraceEnabled())
			log.trace("getClonesByGroup(): " + groupUuid);
		assert (groupUuid != null);

		List<IClone> result = new LinkedList<IClone>();

		rLock("getClonesByGroup");
		try
		{
			synchronized (groupUuidToClonesRegistry)
			{
				//first check if we have those clones in cache
				Set<IClone> clones = groupUuidToClonesRegistry.get(groupUuid);

				//was it in cache?
				if (clones != null)
				{
					//ok, use cached version
					if (log.isTraceEnabled())
						log.trace("getClonesByGroup(): cache HIT - " + clones.size() + " clones");
				}
				else
				{
					//we need to get that data from our data store
					log.trace("getClonesByGroup(): cache MISS");

					//we always need to fetch and cache ALL clones
					List<IClone> subClones = subGetClonesByGroup(groupUuid);

					//store in cache
					clones = new HashSet<IClone>();
					for (IClone clone : subClones)
					{
						//things get a bit tricky here, we want to ensure that we have at most one instance
						//for each uuid in cache. (otherwise things like dirty checking will become much more
						//complicated).
						//this means that we have to make sure we only use new instances if we don't have one
						//in cache already

						IClone cachedClone = cloneUuidToCloneRegistry.get(clone.getUuid());
						if (cachedClone != null)
						{
							//use cached instance instead of the one returned by the backend
							clones.add(cachedClone);

							//the cached clone and the one from the storage backend should have
							//exactly the same values at this point. If not, something is wrong.
							if (!cachedClone.equalsAll((IClone) clone))
								log.error(
										"getClonesByGroup() - cached clone instance and instance from backend differ - "
												+ clone + " - " + cachedClone, new Throwable());
						}
						else
						{
							//not cached yet, use new instance
							clones.add(clone);

							//we also cache this clone individually
							cloneUuidToCloneRegistry.put(clone.getUuid(), clone);
						}

					}
					groupUuidToClonesRegistry.put(groupUuid, clones);
				}

				//now clones is filled (either from cache or from data store)

				//append to result
				for (IClone clone : clones)
				{
					try
					{
						result.add((IClone) clone.clone());
					}
					catch (CloneNotSupportedException e)
					{
						//should never happen
						log.fatal("getClonesByGroup() - unable to clone Clone instance - groupUuid: " + groupUuid
								+ ", clone: " + clone, e);
					}
				}
			}
		}
		finally
		{
			rUnLock("getClonesByGroup");
		}

		if (log.isTraceEnabled())
			log.trace("getClonesByGroup() - result: " + result);

		return result;
	}

	protected abstract List<IClone> subGetClonesByGroup(String groupUuid);

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider#getFullCloneObjectExtension(org.electrocodeogram.cpc.core.api.data.IClone, org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionLazyMultiStatefulObject)
	 */
	@Override
	public ICloneObjectExtensionLazyMultiStatefulObject getFullCloneObjectExtension(ICloneObject cloneObject,
			ICloneObjectExtensionLazyMultiStatefulObject extension)
	{
		if (log.isTraceEnabled())
			log.trace("getFullCloneObjectExtension() - cloneObject: " + cloneObject + ", extension: " + extension);
		assert (cloneObject != null && extension != null && cloneObject instanceof IStatefulObject);
		//clone object and extension should be compatible
		assert ((IStatefulObject) cloneObject).getPersistenceClassIdentifier().equals(
				extension.getPersistenceParentClassIdentifier());

		ICloneObjectExtensionLazyMultiStatefulObject result = null;

		rLock("getFullCloneObjectExtension");
		try
		{
			//get all full persisted extension data
			result = subGetFullCloneObjectExtension(cloneObject, extension);

			//there may be some additional sub-element data in our cached clone object, lets check that
			//get the cached version
			ICloneObject cachedCloneObject = lookupCachedCloneObject(cloneObject);
			if (cachedCloneObject != null)
			{
				ICloneObjectExtension cachedExtension = cloneObject
						.getExtension(extension.getExtensionInterfaceClass());
				if (cachedExtension != null)
				{
					//ok, we do have a cached version, check if we need to copy over any values
					assert (cachedExtension instanceof ICloneObjectExtensionLazyMultiStatefulObject && cachedExtension
							.getClass() == extension.getClass());

					//merge the two extensions together
					result = (ICloneObjectExtensionLazyMultiStatefulObject) CoreUtils.mergeMultiExtensions(
							cloneFactoryProvider, result,
							(ICloneObjectExtensionLazyMultiStatefulObject) cachedExtension);
				}
			}
		}
		finally
		{
			rUnLock("getFullCloneObjectExtension");
		}

		//make sure the result is marked as non-partial
		result.setPartial(false);

		if (log.isTraceEnabled())
			log.trace("getFullCloneObjectExtension() - result: " + result);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider#getFullCloneObjectExtension(org.electrocodeogram.cpc.core.api.data.ICloneObject, java.lang.Class)
	 */
	@Override
	public ICloneObjectExtensionLazyMultiStatefulObject getFullCloneObjectExtension(ICloneObject cloneObject,
			Class<? extends ICloneObjectExtension> extensionClass)
	{
		if (log.isTraceEnabled())
			log.trace("getFullCloneObjectExtension() - cloneObject: " + cloneObject + ", extensionClass: "
					+ extensionClass);
		assert (cloneObject != null && extensionClass != null && cloneObject instanceof IStatefulObject);

		ICloneObjectExtension extension = cloneObject.getExtension(extensionClass);
		if (extension == null)
			return null;

		//the extension must be an ICloneObjectExtensionLazyMultiStatefulObject extension
		assert (extension instanceof ICloneObjectExtensionLazyMultiStatefulObject);

		return getFullCloneObjectExtension(cloneObject, (ICloneObjectExtensionLazyMultiStatefulObject) extension);
	}

	/**
	 * Takes an {@link ICloneObjectExtensionLazyMultiStatefulObject} which is only partially loaded and
	 * returns a <b>new cloned copy</b> with all sub-elements fully loaded.<br/>
	 * <br/>
	 * The given clone and extension instances itself are <b>not</b> modified.
	 * 
	 * @param cloneObject the clone object to which this extension is added, never null.
	 * @param extension the extension to load all sub-elements for, never null.
	 * @return a new cloned copy of the extension with all sub-elements loaded, never null.
	 */
	protected abstract ICloneObjectExtensionLazyMultiStatefulObject subGetFullCloneObjectExtension(
			ICloneObject cloneObject, ICloneObjectExtensionLazyMultiStatefulObject extension);

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.store.IStoreProvider#addClone(org.electrocodeogram.cpc.core.api.data.IClone)
	 */
	@Override
	public void addClone(IClone extClone) throws StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("addClone(): " + extClone);
		assert (extClone != null && extClone instanceof ICloneInterfaces);

		if (!isWriteLockedByCurrentThread())
			throw new StoreLockingException("method requires exclusive write lock");

		//make sure this ICloneObject has not been sealed.
		if (extClone.isSealed())
			throw new IllegalArgumentException("Sealed ICloneObjects may not be passed to the IStoreProvider.");

		//make a clone for internal use
		IClone clone;
		try
		{
			clone = (IClone) extClone.clone();
		}
		catch (CloneNotSupportedException e)
		{
			log.fatal("addClone() - unable to clone instance - clone: " + extClone, e);

			return;
		}

		//remove any non-stateful extensions from the clone
		stripNonStatefulExtensions(clone);

		//make sure all clone data for the file is cached
		if (!fileUuidToClonesRegistry.containsKey(clone.getFileUuid()))
		{
			//we'll need to cache the file data first
			if (log.isTraceEnabled())
				log.trace("addClone(): clone data for file not yet cached - " + clone.getFileUuid());

			//we can ignore the return value, as this method will directly update the cache
			getClonesByFile(clone.getFileUuid());
		}

		//make sure all clone data for the group is cached
		if (clone.getGroupUuid() != null)
		{
			if (!groupUuidToClonesRegistry.containsKey(clone.getGroupUuid()))
			{
				//we'll need to cache the clone group data first
				if (log.isTraceEnabled())
					log.trace("addClone(): clone data for group not yet cached - " + clone.getGroupUuid());

				//we can ignore the return value, as this method will directly update the cache
				getClonesByGroup(clone.getGroupUuid());
			}
		}

		//make sure that this is really a new clone
		if (cloneUuidToCloneRegistry.containsKey(clone.getUuid()))
		{
			//this shouldn't happen, according to the API spec we just ignore this call
			//but we should print a warning to the log
			log.warn("addClone(): ignoring already persisted clone instance - " + clone, new Throwable());
			return;
		}

		//we only add the clone to the cache for now
		SortedSet<IClone> clones = fileUuidToClonesRegistry.get(clone.getFileUuid());
		clones.add(clone);

		//also add it to the group cache, if a group is set
		if (clone.getGroupUuid() != null)
		{
			Set<IClone> groupClones = groupUuidToClonesRegistry.get(clone.getGroupUuid());

			//add the clone instance to the group cache and update orphan flags if needed
			_addCloneToGroup(groupClones, clone);
		}

		//cache individual clone
		cloneUuidToCloneRegistry.put(clone.getUuid(), clone);

		if (cloneModificationEventEnabled)
		{
			//remember this clone for later modification events
			rememberCloneModificationInTransaction(transactionAddedClones, clone);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.store.IStoreProvider#addClones(java.util.List)
	 */
	@Override
	public void addClones(List<IClone> clones) throws StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("addClones(): " + clones);

		if (!isWriteLockedByCurrentThread())
			throw new StoreLockingException("method requires exclusive write lock");

		if (clones != null)
			for (IClone c : clones)
				addClone(c);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.store.IStoreProvider#addCloneGroup(org.electrocodeogram.cpc.core.api.data.ICloneGroup)
	 */
	@Override
	public void addCloneGroup(ICloneGroup extGroup) throws StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("addCloneGroup(): " + extGroup);
		assert (extGroup != null && extGroup instanceof ICloneGroupInterfaces);

		if (!isWriteLockedByCurrentThread())
			throw new StoreLockingException("method requires exclusive write lock");

		//make sure this ICloneObject has not been sealed.
		if (extGroup.isSealed())
			throw new IllegalArgumentException("Sealed ICloneObjects may not be passed to the IStoreProvider.");

		//make a clone for internal use
		ICloneGroup group;
		try
		{
			group = (ICloneGroup) extGroup.clone();
		}
		catch (CloneNotSupportedException e)
		{
			log.fatal("addCloneGroup() - unable to clone instance - group: " + extGroup, e);

			return;
		}

		//remove any non-stateful extensions from the group
		stripNonStatefulExtensions(group);

		//make sure this clone group has been cached, if it exists (which it probably doesn't)
		//so in a way this call will almost never return any value
		if (!groupUuidToGroupRegistry.containsKey(group.getUuid()))
		{
			//we'll need to cache the file data first
			if (log.isTraceEnabled())
				log.trace("addCloneGroup(): data for group not yet cached - " + group.getUuid());

			//we can ignore the return value, as this method will directly update the cache
			lookupCloneGroup(group.getUuid());
		}

		//make sure that this is really a new clone group
		if (groupUuidToGroupRegistry.containsKey(group.getUuid()))
		{
			//this usually shouldn't happen, according to the API spec we just ignore this call
			//but we should print a warning to the log
			log.warn("addCloneGroup(): ignoring already persisted clone group instance - " + group);
			return;
		}

		//we only add the group to the cache for now
		groupUuidToGroupRegistry.put(group.getUuid(), group);

		//the group has no clones yet
		//TODO: this isn't needed, right?
		//groupUuidToClonesRegistry.put(group.getUuid(), new HashSet<IClone>());
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IRemotableStoreProvider#addCloneFile(org.electrocodeogram.cpc.core.api.data.ICloneFile)
	 */
	@Override
	public void addCloneFile(ICloneFile extFile) throws StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("addCloneFile(): " + extFile);
		assert (extFile != null && extFile instanceof ICloneFileInterfaces);

		if (!isWriteLockedByCurrentThread())
			throw new StoreLockingException("method requires exclusive write lock");

		//make sure this ICloneObject has not been sealed.
		if (extFile.isSealed())
			throw new IllegalArgumentException("Sealed ICloneObjects may not be passed to the IStoreProvider.");

		//make a clone for internal use
		ICloneFile file;
		try
		{
			file = (ICloneFile) extFile.clone();
		}
		catch (CloneNotSupportedException e)
		{
			log.fatal("addCloneFile() - unable to clone instance - file: " + extFile, e);
			return;
		}

		//remove any non-stateful extensions from the file
		stripNonStatefulExtensions(file);

		//make sure this clone file has been cached, if it exists (which it probably doesn't)
		//so in a way this call will almost never return any value
		if (!fileUuidToFileRegistry.containsKey(file.getUuid()))
		{
			//we'll need to cache the file data first
			if (log.isTraceEnabled())
				log.trace("addCloneFile(): data for file not yet cached - " + file.getUuid());

			//we can ignore the return value, as this method will directly update the cache
			lookupCloneFile(file.getUuid());
		}

		//make sure that this is really a new clone file
		if (fileUuidToFileRegistry.containsKey(file.getUuid()))
		{
			//this usually shouldn't happen, according to the API spec we just ignore this call
			//but we should print a warning to the log
			log.warn("addCloneFile(): ignoring already persisted clone file instance - " + file, new Throwable());
			return;
		}

		//store the file's uuid as a persistent property
		IFile cloneFileHandle = CoreFileUtils.getFileForCloneFile(file);
		//make sure that no uuid is set yet
		String oldUuid = CoreFileUtils.getFileUuidProperty(cloneFileHandle);
		if (oldUuid != null)
		{
			/*
			 * This shouldn't happen.
			 * It means that we're trying to "update" data for a file which already had a different
			 * uuid in the past. However, file uuids should _never_ change.
			 */
			log.fatal("addCloneFile() - trying to set a new uuid for file - file: " + cloneFileHandle + ", old uuid: "
					+ oldUuid + ", new clone file instance: " + file, new Throwable());
			return;
		}
		//now set the new uuid
		CoreFileUtils.setFileUuidProperty(cloneFileHandle, file.getUuid());

		//persist the entry
		subPersistCloneFile(file);

		//add the file to the cache
		fileUuidToFileRegistry.put(file.getUuid(), file);

		//the file does not yet have any clone data
		fileUuidToClonesRegistry.put(file.getUuid(), new TreeSet<IClone>());
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider#updateClone(org.electrocodeogram.cpc.core.api.data.IClone, org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.UpdateMode)
	 */
	@Override
	public void updateClone(IClone extClone, UpdateMode mode) throws StoreLockingException, IllegalArgumentException
	{
		if (log.isTraceEnabled())
			log.trace("updateClone() - clone: " + extClone + ", mode: " + mode);
		assert (extClone != null && extClone instanceof ICloneInterfaces && mode != null);

		if (!isWriteLockedByCurrentThread())
			throw new StoreLockingException("method requires exclusive write lock");

		_updateClone(extClone, mode);

		List<IClone> list = new LinkedList<IClone>();
		list.add(extClone);

		//make sure that this change did not corrupt internal integrity of our caches
		//this is a debug only operation
		_checkIntegrityAfterChange(list);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider#updateClones(java.util.List, org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.UpdateMode)
	 */
	@Override
	public void updateClones(List<IClone> clones, UpdateMode mode) throws StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("updateClones() - clones: " + clones + ", mode: " + mode);
		assert (clones != null && mode != null);

		if (!isWriteLockedByCurrentThread())
			throw new StoreLockingException("method requires exclusive write lock");

		if (clones != null)
			for (IClone c : clones)
				_updateClone(c, mode);

		//make sure that this change did not corrupt internal integrity of our caches
		//this is a debug only operation
		_checkIntegrityAfterChange(clones);
	}

	/**
	 * Implementation for updateClone().
	 * 
	 * Split from updateClone() to allow for reuse in updateClones() and optional integrity checking
	 * after a batch operation.
	 */
	protected void _updateClone(IClone extClone, UpdateMode mode) throws StoreLockingException,
			IllegalArgumentException
	{
		if (log.isTraceEnabled())
			log.trace("_updateClone() - clone: " + extClone + ", mode: " + mode);
		assert (extClone != null && mode != null);

		//make sure this ICloneObject has not been sealed.
		if (extClone.isSealed())
			throw new IllegalArgumentException("Sealed ICloneObjects may not be passed to the IStoreProvider.");

		//make a clone for internal use
		IClone clone;
		try
		{
			clone = (IClone) extClone.clone();
		}
		catch (CloneNotSupportedException e)
		{
			log.fatal("_updateClone() - unable to clone instance - clone: " + extClone, e);

			return;
		}

		//remove any non-stateful extensions from the clone
		stripNonStatefulExtensions(clone);

		//make sure all clone data for the file is cached
		if (!fileUuidToClonesRegistry.containsKey(clone.getFileUuid()))
		{
			//we'll need to cache the file data first
			if (log.isTraceEnabled())
				log.trace("_updateClone(): clone data for file not yet cached - " + clone.getFileUuid());

			//we can ignore the return value, as this method will directly update the cache
			getClonesByFile(clone.getFileUuid());
		}

		//make sure all clone data for the group is cached
		if (clone.getGroupUuid() != null)
		{
			if (!groupUuidToClonesRegistry.containsKey(clone.getGroupUuid()))
			{
				//we'll need to cache the clone group data first
				if (log.isTraceEnabled())
					log.trace("_updateClone(): clone data for group not yet cached - " + clone.getGroupUuid());

				//we can ignore the return value, as this method will directly update the cache
				getClonesByGroup(clone.getGroupUuid());
			}
		}

		//make sure that this is really an existing clone
		IClone oldClone = cloneUuidToCloneRegistry.get(clone.getUuid());
		if (oldClone == null)
		{
			//this is a violation of the API specs!
			log.error("_updateClone(): clone instance does not exist - " + clone, new Throwable());

			throw new IllegalArgumentException("clone instance does not exist - " + clone);
		}

		//if this is a modification, make sure we have clone diff entries in our history
		if (UpdateMode.MODIFIED.equals(mode) || UpdateMode.MOVED_MODIFIED.equals(mode))
		{
			ICloneModificationHistoryExtension history = (ICloneModificationHistoryExtension) clone
					.getExtension(ICloneModificationHistoryExtension.class);
			if (history == null || history.getCloneDiffs().size() == 0)
			{
				log.warn("_updateClone(): modified clone contains no clone modification history entries - clone: "
						+ clone + ", UpdateMode: " + mode + ", history: " + history, new Throwable());
			}
		}

		//make sure we don't loose any clone modification end of transaction data during this update
		checkCloneDiffTransactionMarker(clone, oldClone);

		//NOTE: a clone is always fixed to a file, it can not move from one file to another
		//		as such the file uuid will never change, however the path/filename may change.

		//we only update the clone in the cache for now
		SortedSet<IClone> clones = fileUuidToClonesRegistry.get(clone.getFileUuid());
		if (log.isTraceEnabled())
			log.trace("_updateClone() - fileUuidToClonesRegistry: " + clone.getFileUuid() + " -> " + clones);

		//replacement of the old instance
		boolean removed = clones.remove(oldClone);
		/*
		 * NOTE: it is crucial that we use oldClone and not clone for the removal.
		 * 		The SortedSet is a TreeSet which will break if we try to find the old entry using clone
		 * 		if the position was modified!
		 */
		if (!removed)
			log.error(
					"_updateClone(): clone instance exists in cloneUuidToClone registry but not in fileUuidToClones registry - "
							+ clone + " via " + oldClone, new Throwable());
		clones.add(clone);

		//also update it in the group cache, if a group is set
		if (clone.getGroupUuid() != null)
		{
			if (log.isTraceEnabled())
				log.trace("_updateClone(): also updating group cache - " + clone.getGroupUuid());

			Set<IClone> groupClones = groupUuidToClonesRegistry.get(clone.getGroupUuid());
			if (log.isTraceEnabled())
				log.trace("_updateClone() - groupUuidToClonesRegistry: " + clone.getGroupUuid() + " -> " + groupClones);

			//replace any potential previous entry for this clone
			boolean removedGC = groupClones.remove(oldClone);
			if (!removedGC && clone.getGroupUuid().equals(oldClone.getGroupUuid()))
				//this check only makes sense if the group did not change
				log
						.error(
								"_updateClone(): clone instance exists in cloneUuidToClone and fileUuidToClones registry but not in groupUuidToClones registry - "
										+ clone + ", groupClones: " + groupClones + ", clones: " + clones,
								new Throwable());

			//add the clone instance to the group cache and update orphan flags if needed
			_addCloneToGroup(groupClones, clone);
		}

		//we may need to update the old group cache, if the group was changed
		if ((oldClone.getGroupUuid() != null) && (!oldClone.getGroupUuid().equals(clone.getGroupUuid())))
		{
			if (log.isDebugEnabled())
				log.debug("_updateClone(): group was changed in update, updating old group - "
						+ oldClone.getGroupUuid());

			//remove from old group, if cached
			Set<IClone> oldGroupClones = groupUuidToClonesRegistry.get(oldClone.getGroupUuid());
			if (oldGroupClones != null)
			{
				oldGroupClones.remove(oldClone);

				//also check the size of this group. if there is only one clone remaining, it should be marked
				//as orphan.
				if (oldGroupClones.size() == 1)
				{
					if (log.isTraceEnabled())
						log.trace("_updateClone() - marking sole remaining clone in group as orphan - group: "
								+ oldGroupClones + " - clone removed from group: " + oldClone);
					IClone groupClone = oldGroupClones.iterator().next();
					groupClone.setCloneState(IClone.State.ORPHAN, 0, null);
					if (cloneModificationEventEnabled)
						rememberCloneModificationInTransaction(transactionMovedClones, groupClone);
				}

			}
		}

		//cache individual clone, will also overwrite old instance
		cloneUuidToCloneRegistry.put(clone.getUuid(), clone);

		if (cloneModificationEventEnabled)
		{
			//remember this clone for later modification events
			if (UpdateMode.MOVED.equals(mode) || UpdateMode.MOVED_MODIFIED.equals(mode))
				rememberCloneModificationInTransaction(transactionMovedClones, clone);
			if (UpdateMode.MODIFIED.equals(mode) || UpdateMode.MOVED_MODIFIED.equals(mode))
				rememberCloneModificationInTransaction(transactionModifiedClones, clone);
		}
	}

	/**
	 * Takes a group cache and a clone and adds the clone to the group cache,
	 * while updating orphan flags of the clone and the other group members, as needed.<br/>
	 * If this is an update of the clone in that group, the clone needs to be removed
	 * from the group, before this method is called.
	 * 
	 * @param groupClones the group cache to add the clone to, never null.
	 * @param clone the clone to add, never null.
	 */
	protected void _addCloneToGroup(Set<IClone> groupClones, IClone clone)
	{
		assert (groupClones != null && clone != null);

		//also check the size of this group. if there is currently only one clone in the group, it might have
		//been marked as orphan. But it will no longer be an orphan after this addition.
		if (groupClones.size() == 1)
		{
			IClone groupClone = groupClones.iterator().next();
			if (IClone.State.ORPHAN.equals(groupClone.getCloneState()))
			{
				if (log.isTraceEnabled())
					log.trace("_addCloneToGroup() - marking existing clone in group as non-orphan - group: "
							+ groupClones + " - clone to be added to group: " + clone);

				groupClone.setCloneState(IClone.State.MODIFIED, 0, null);
				if (cloneModificationEventEnabled)
					rememberCloneModificationInTransaction(transactionMovedClones, groupClone);
			}
		}

		//add the new clone instance to the list of clones for this clone group
		groupClones.add(clone);

		//make sure that the orphan flag is set correctly for the newly added clone
		if (groupClones.size() == 1 && !IClone.State.ORPHAN.equals(clone.getCloneState()))
		{
			//this clone is the only member of the group, it should be marked as orphan.
			log.trace("_addCloneToGroup() - marking clone as orphan.");
			clone.setCloneState(IClone.State.ORPHAN, 0, null);
		}
		else if (groupClones.size() > 1 && IClone.State.ORPHAN.equals(clone.getCloneState()))
		{
			//this clone is no longer an orphan
			log.trace("_addCloneToGroup() - marking clone as non-orphan.");
			clone.setCloneState(IClone.State.MODIFIED, 0, null);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.store.IStoreProvider#updateCloneGroup(org.electrocodeogram.cpc.core.api.data.ICloneGroup)
	 */
	@Override
	public void updateCloneGroup(ICloneGroup extGroup) throws StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("updateCloneGroup(): " + extGroup);
		assert (extGroup != null && extGroup instanceof ICloneGroupInterfaces);

		if (!isWriteLockedByCurrentThread())
			throw new StoreLockingException("method requires exclusive write lock");

		//make sure this ICloneObject has not been sealed.
		if (extGroup.isSealed())
			throw new IllegalArgumentException("Sealed ICloneObjects may not be passed to the IStoreProvider.");

		//make a clone for internal use
		ICloneGroup group;
		try
		{
			group = (ICloneGroup) extGroup.clone();
		}
		catch (CloneNotSupportedException e)
		{
			log.fatal("updateCloneGroup() - unable to clone instance - group: " + extGroup, e);

			return;
		}

		//remove any non-stateful extensions from the group
		stripNonStatefulExtensions(group);

		//make sure this clone group has been cached, if it exists (which it should)
		if (!groupUuidToGroupRegistry.containsKey(group.getUuid()))
		{
			//we'll need to cache the file data first
			if (log.isTraceEnabled())
				log.trace("updateCloneGroup(): data for group not yet cached - " + group.getUuid());

			//we can ignore the return value, as this method will directly update the cache
			lookupCloneGroup(group.getUuid());
		}

		//make sure that this is really an existing clone group
		if (!groupUuidToGroupRegistry.containsKey(group.getUuid()))
		{
			//this is a violation of the API specs!
			log.error("updateCloneGroup(): clone group instance does not exist - " + group, new Throwable());

			throw new IllegalArgumentException("clone group instance does not exist - " + group);
		}

		//we only add the group to the cache for now, will also overwrite old instance
		groupUuidToGroupRegistry.put(group.getUuid(), group);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IRemotableStoreProvider#updateCloneFile(org.electrocodeogram.cpc.core.api.data.ICloneFile)
	 */
	@Override
	public void updateCloneFile(ICloneFile extFile) throws StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("updateCloneFile(): " + extFile);
		assert (extFile != null && extFile instanceof ICloneFileInterfaces);

		if (!isWriteLockedByCurrentThread())
			throw new StoreLockingException("method requires exclusive write lock");

		//make sure this ICloneObject has not been sealed.
		if (extFile.isSealed())
			throw new IllegalArgumentException("Sealed ICloneObjects may not be passed to the IStoreProvider.");

		//make a clone for internal use
		ICloneFile file;
		try
		{
			file = (ICloneFile) extFile.clone();
		}
		catch (CloneNotSupportedException e)
		{
			log.fatal("updateCloneFile() - unable to clone instance - file: " + extFile, e);
			return;
		}

		//remove any non-stateful extensions from the file
		stripNonStatefulExtensions(file);

		//make sure this clone file has been cached, if it exists (which it should)
		if (!fileUuidToFileRegistry.containsKey(file.getUuid()))
		{
			//we'll need to cache the file data first
			if (log.isTraceEnabled())
				log.trace("updateCloneFile(): data for file not yet cached - " + file.getUuid());

			//we can ignore the return value, as this method will directly update the cache
			lookupCloneFile(file.getUuid());
		}

		//make sure that this is really an existing clone file
		ICloneFile oldFile = fileUuidToFileRegistry.get(file.getUuid());
		if (oldFile == null)
		{
			//this is a violation of the API specs!
			log.error("updateCloneFile(): clone file instance does not exist - " + file, new Throwable());

			throw new IllegalArgumentException("clone file instance does not exist - " + file);
		}

		//make sure the file uuid wasn't modified
		if (!oldFile.getUuid().equals(file.getUuid()))
		{
			//this should never happen!
			log.fatal(
					"updateCloneFile() - trying to change file uuid - old entry: " + oldFile + ", new entry: " + file,
					new Throwable());
			return;
		}

		//make sure the client isn't trying to "move" a file with this command
		if (!oldFile.getProject().equals(file.getProject()) || !oldFile.getPath().equals(file.getPath()))
		{
			//this shouldn't happen. moveCloneFile() should be used instead
			log.error("updateCloneFile() - trying to change file project or path - old entry: " + oldFile
					+ ", new entry: " + file, new Throwable());
			return;
		}

		//persist the entry
		subPersistCloneFile(file);

		//add the file to the cache
		fileUuidToFileRegistry.put(file.getUuid(), file);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.store.IStoreProvider#removeClone(org.electrocodeogram.cpc.core.api.data.IClone)
	 */
	@Override
	public void removeClone(IClone clone) throws StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("removeClone(): " + clone);
		assert (clone != null && clone instanceof ICloneInterfaces);

		if (!isWriteLockedByCurrentThread())
			throw new StoreLockingException("method requires exclusive write lock");

		//make sure all clone data for the file is cached
		if (!fileUuidToClonesRegistry.containsKey(clone.getFileUuid()))
		{
			//we'll need to cache the file data first
			if (log.isTraceEnabled())
				log.trace("removeClone(): clone data for file not yet cached - " + clone.getFileUuid());

			//we can ignore the return value, as this method will directly update the cache
			getClonesByFile(clone.getFileUuid());
		}

		//make sure all clone data for the group is cached
		if (clone.getGroupUuid() != null)
		{
			if (!groupUuidToClonesRegistry.containsKey(clone.getGroupUuid()))
			{
				//we'll need to cache the clone group data first
				if (log.isTraceEnabled())
					log.trace("removeClone(): clone data for group not yet cached - " + clone.getGroupUuid());

				//we can ignore the return value, as this method will directly update the cache
				getClonesByGroup(clone.getGroupUuid());
			}
		}

		//make sure that this clone really exists and get a reference
		IClone oldClone = cloneUuidToCloneRegistry.get(clone.getUuid());
		if (oldClone == null)
		{
			//this shouldn't happen, according to the API spec we just ignore this call
			//but we should print a warning to the log
			log.warn("removeClone(): ignoring non existing clone instance - " + clone, new Throwable());
			return;
		}

		//we only remove the clone from the cache for now
		SortedSet<IClone> clones = fileUuidToClonesRegistry.get(clone.getFileUuid());
		clones.remove(oldClone);
		/*
		 * NOTE: it is crucial to use oldClone for the deletion here, the underlying TreeSet will break
		 * 		if one tries to delete an existing clone for which the position was modified.
		 */

		if (clone.getGroupUuid() != null)
		{
			//remove it from the group cache too
			Set<IClone> groupClones = groupUuidToClonesRegistry.get(clone.getGroupUuid());
			boolean removed = groupClones.remove(oldClone);
			//this is a HashSet, using clone is safe, but just in case we change that in the future, let's use the
			//old instance here too
			if (!removed)
				log.warn("removeClone(): clone was not registered with group - " + clone + " via " + oldClone,
						new Throwable());

			//now check all group members, if any of them points to this clone as origin clone, reset the
			//origin pointer to null
			for (IClone groupClone : groupClones)
			{
				if (clone.getUuid().equals(groupClone.getOriginUuid()))
				{
					groupClone.setOriginUuid(null);
				}
			}

			//also check the size of this group. if there is only one clone remaining, it should be marked
			//as orphan.
			if (groupClones.size() == 1)
			{
				if (log.isTraceEnabled())
					log.trace("removeClone() - marking sole remaining clone in group as orphan - group: " + groupClones
							+ " - removed clone: " + oldClone);
				IClone groupClone = groupClones.iterator().next();
				groupClone.setCloneState(IClone.State.ORPHAN, 0, null);
				if (cloneModificationEventEnabled)
					rememberCloneModificationInTransaction(transactionMovedClones, groupClone);
			}
		}

		//remove it from individual clone cache
		cloneUuidToCloneRegistry.remove(clone.getUuid());

		if (cloneModificationEventEnabled)
			//remember this clone for later modification events
			rememberCloneModificationInTransaction(transactionRemovedClones, oldClone);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.store.IStoreProvider#removeClones(java.util.List)
	 */
	@Override
	public void removeClones(List<IClone> clones) throws StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("removeClones(): " + clones);

		if (!isWriteLockedByCurrentThread())
			throw new StoreLockingException("method requires exclusive write lock");

		if (clones != null)
			for (IClone c : clones)
				removeClone(c);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.store.IStoreProvider#removeCloneGroup(org.electrocodeogram.cpc.core.api.data.ICloneGroup)
	 */
	@Override
	public void removeCloneGroup(ICloneGroup group) throws StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("removeCloneGroup(): " + group);
		assert (group != null && group instanceof ICloneGroupInterfaces);

		if (!isWriteLockedByCurrentThread())
			throw new StoreLockingException("method requires exclusive write lock");

		//make sure this clone group has been cached, if it exists (which it probably does)
		if (!groupUuidToGroupRegistry.containsKey(group.getUuid()))
		{
			//we'll need to cache the file data first
			if (log.isTraceEnabled())
				log.trace("removeCloneGroup(): data for group not yet cached - " + group.getUuid());

			//we can ignore the return value, as this method will directly update the cache
			lookupCloneGroup(group.getUuid());
		}

		//make sure that this is really an existing clone group
		if (!groupUuidToGroupRegistry.containsKey(group.getUuid()))
		{
			//this usually shouldn't happen, according to the API spec we just ignore this call
			//but we should print a warning to the log
			log.warn("removeCloneGroup(): ignoring non existing clone group instance - " + group);
			return;
		}

		//we only remove the group from the cache for now
		groupUuidToGroupRegistry.remove(group.getUuid());

		/*
		 * TODO:/FIXME: we should probably reset all members of this group to no-group.
		 * And clear the group uuid to clones cache here.
		 */
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider#moveCloneFile(org.electrocodeogram.cpc.core.api.data.ICloneFile, java.lang.String, java.lang.String)
	 */
	@Override
	public void moveCloneFile(ICloneFile extCloneFile, String project, String path) throws StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("moveCloneFile() - cloneFile: " + extCloneFile + ", project: " + project + ", path: " + path);
		assert (extCloneFile != null && project != null && path != null);

		if (!isWriteLockedByCurrentThread())
			throw new StoreLockingException("method requires exclusive write lock");

		//get our internal copy of this clone file entry
		ICloneFile cloneFile = _lookupCloneFile(extCloneFile.getUuid());
		if (cloneFile == null)
		{
			log.error("moveCloneFile() - unable to retrieve clone file entry for file uuid: " + extCloneFile.getUuid(),
					new Throwable());
			return;
		}

		//Make sure that the file really needs to be updated. The move might already have found its way into
		//the store provider data on other ways. (i.e. via processPotentialFileMove())
		if (cloneFile.getProject().equals(project) && cloneFile.getPath().equals(path))
		{
			log.warn("moveCloneFile() - file was already moved, nothing to do - int. cloneFile: " + cloneFile
					+ ", ext. cloneFile: " + extCloneFile);
			return;
		}

		/*
		 * Now do the actual move.
		 */
		_moveCloneFile(cloneFile, project, path);
	}

	/**
	 * Dispatches the file move call to the underlying store provider implementation
	 * and updates the given {@link ICloneFile} instance as well as the path to file uuid
	 * registry.<br/>
	 * <br/>
	 * The caller of this method needs to either hold a monitor lock on <em>pathToFileUuidRegistry</em>
	 * and a read lock on this store provider or an exclusive write lock on this store provider.
	 * 
	 * @param cloneFile the {@link ICloneFile} instance to update, the given object is <u>updated in place</u>, never null.
	 * @param project the new project name, never null.
	 * @param path the new project relative path, never null.
	 */
	protected void _moveCloneFile(ICloneFile cloneFile, String project, String path)
	{
		if (log.isTraceEnabled())
			log.trace("_moveCloneFile() - cloneFile: " + cloneFile + ", project: " + project + ", path: " + path);
		assert (cloneFile != null && project != null && path != null);

		String oldFullFilePath = cloneFile.getProject() + "/" + cloneFile.getPath();
		String newFullFilePath = project + "/" + path;

		//delegate update to store provider
		subMoveCloneFile(cloneFile, project, path);

		//update project and path data of the internal copy in cache
		((ICreatorCloneFile) cloneFile).setProject(project);
		((ICreatorCloneFile) cloneFile).setPath(path);

		//update path to file uuid registry
		//add new entry
		pathToFileUuidRegistry.put(newFullFilePath, cloneFile.getUuid());
		//remove old entry
		pathToFileUuidRegistry.remove(oldFullFilePath);

		//remember the old full path for potential lookups of clients who only know the
		//files old path. This typically happens on delayed processing of events.
		oldPathToFileUuidCache.put(oldFullFilePath, cloneFile.getUuid());
	}

	/**
	 * Indicates that a file was renamed or moved to a new location.<br/>
	 * <br/>
	 * Once this method is called future calls of
	 * {@link AbstractStoreProvider#subGetCloneFileByPath(String, String)} and
	 * {@link AbstractStoreProvider#subGetCloneFileByUuid(String)} will use the updated
	 * position information.<br/>
	 * <br/>
	 * <b>IMPORTANT NOTE:</b> The caller of this method might <b>not</b> always hold an exclusive write lock
	 * 		but may sometimes <u>only be holding a read lock</u>. Any implementation will therefore
	 * 		need to ensure proper thread safety.<br/>
	 * 		The implementation <b>must not</b> try to acquire a store provider write lock.
	 * 
	 * @param cloneFile the file which was moved, still contains old position, never null.
	 * @param project the new project name, never null.
	 * @param path the new project relative path of the file, never null.
	 */
	protected abstract void subMoveCloneFile(ICloneFile cloneFile, String project, String path);

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider#purgeData(org.electrocodeogram.cpc.core.api.data.ICloneFile, boolean)
	 */
	@Override
	public void purgeData(ICloneFile file, boolean removeCloneFile) throws StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("purgeData() - file: " + file + ", removeCloneFile: " + removeCloneFile);
		assert (file != null);

		if (!isWriteLockedByCurrentThread())
			throw new StoreLockingException("method requires exclusive write lock");

		//make sure all clone data for the file is cached
		if (!fileUuidToClonesRegistry.containsKey(file.getUuid()))
		{
			//we'll need to cache the file data first
			if (log.isTraceEnabled())
				log.trace("purgeData(): clone data for file not yet cached - " + file.getUuid());

			//we can ignore the return value, as this method will directly update the cache
			getClonesByFile(file.getUuid(), -1, -1);
		}

		SortedSet<IClone> clones = fileUuidToClonesRegistry.get(file.getUuid());
		for (IClone clone : clones)
		{
			//remove from individual cache
			cloneUuidToCloneRegistry.remove(clone.getUuid());

			//if this clone belongs to a group, remove it from the group cache too
			if (clone.getGroupUuid() != null)
			{
				//make sure we have this group in cache
				getClonesByGroup(clone.getGroupUuid());

				Set<IClone> groupClones = groupUuidToClonesRegistry.get(clone.getGroupUuid());
				if (groupClones != null)
				{
					boolean removed = groupClones.remove(clone);
					if (!removed)
						//some internal error, the clone should have been in that list
						log.error("purgeData(): clone was not registered with group - " + clone, new Throwable());
				}
				else
				{
					//this shouldn't happen. This clone has a group uuid set, which means that
					//it should be part of the clone group and it should therefore be in the
					//cache at this point.
					log.error("purgeData(): clone group could not be retrieved - " + clone, new Throwable());
				}
			}
		}
		//clear clone cache for file
		clones.clear();

		if (removeCloneFile)
		{
			log.trace("purgeData(): going to purge all data for clone file.");

			//the client wants to completely remove all data about this clone file entry
			subPurgeCloneFile(file);

			//clear persistent UUID property of the file
			IFile cloneFileHandle = CoreFileUtils.getFileForCloneFile(file);
			if (cloneFileHandle != null && cloneFileHandle.exists())
				CoreFileUtils.setFileUuidProperty(cloneFileHandle, null);

			//we may still need the clone file instance for event generation once the lock is released
			transactionDeletedFileDataBuffer.put(file.getUuid(), file);

			//clear some caches
			String fullPath = file.getProject() + "/" + file.getPath();
			pathToFileUuidRegistry.remove(fullPath);
			oldPathToFileUuidCache.remove(fullPath);
			fileUuidToFileRegistry.remove(file.getUuid());

		}
		else
		{
			log.trace("purgeData(): persisting modifications (clone deletions).");

			//persist the changes
			persistData(file);
		}

		//remove cache
		fileUuidToClonesRegistry.remove(file.getUuid());

		if (cloneModificationEventEnabled)
			//remember this clone file for later modification events
			transactionFullModifications.add(file.getUuid());

		/*
		 * Now notify any interested parties about this persistence event.
		 * I.e. a remote store provider might want to always keep an update
		 * to date copy of this data inside the workspace/project directory
		 * (in order for it to be checked in/out of CVS/SVN)
		 */
		ClonePersistenceEvent newEvent = new ClonePersistenceEvent(file);
		newEvent.setClones(EMPTY_CLONE_LIST);
		CPCCorePlugin.getEventHubRegistry().dispatch(newEvent);
	}

	/**
	 * Removes all data for the given file from persistent storage and all internal caches.<br/>
	 * This is typically called if a file was permanently deleted.
	 * 
	 * @param file the file to remove all data for, never null.
	 */
	protected abstract void subPurgeCloneFile(ICloneFile file);

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.store.IStoreProvider#persistData(org.electrocodeogram.cpc.core.api.data.ICloneFile)
	 */
	@Override
	public void persistData(ICloneFile extCloneFile) throws StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("persistData(): " + extCloneFile);
		assert (extCloneFile != null && extCloneFile instanceof ICloneFileInterfaces);

		if (!isWriteLockedByCurrentThread())
			throw new StoreLockingException("method requires exclusive write lock");

		//get our internal copy of this clone file entry
		ICloneFile file = _lookupCloneFile(extCloneFile.getUuid());
		if (file == null)
		{
			log.error("persistData() - unable to retrieve clone file entry for file uuid: " + extCloneFile.getUuid(),
					new Throwable());
			return;
		}

		/*
		 * Mark the file as being dirty in relation to the remote resource.
		 * This will also mark files dirty if they are persisted as is, without
		 * any changes. This is obviously not perfect but doesn't pose a real problem
		 * either.
		 * Such files will just require some additional effort on a team update action
		 * as they will be passed to a merge provider even though no merge is required.
		 * 
		 * TODO: Reconsider whether we shouldn't move this somewhere else. As it is
		 * clearly only of concern to the remote store provider, it might be more sensible
		 * for the marking to be done there. However, the remote store provider does not
		 * have to possibility to intervene here. We'd need a new event type for that. 
		 */
		((IRemoteStoreCloneFile) file).setRemoteDirty(true);

		//check if we have the file in cache
		SortedSet<IClone> clones = fileUuidToClonesRegistry.get(file.getUuid());
		if (clones != null)
		{
			//yes, it's cached, this should always be the case

			//make a copy of the file itself
			setStoredCloneFileContent(file);

			//persist the entire cache content
			//checking or resetting of any dirty flags is handled by this method
			subPersistData(file, clones);

			//TODO: do we need to notify sub-classes about potentially dirty clone groups too?

			//clear out any sub-element data for lazy loaded clone object extensions
			for (IClone clone : clones)
				clearLazyMultiStatefulExtensions(clone, true);

			/*
			 * Now notify any interested parties about this persistence event.
			 * I.e. a remote store provider might want to always keep an update
			 * to date copy of this data inside the workspace/project directory
			 * (in order for it to be checked in/out of CVS/SVN)
			 */
			ClonePersistenceEvent newEvent = new ClonePersistenceEvent(file);
			newEvent.setClones(new ArrayList<IClone>(clones));
			CPCCorePlugin.getEventHubRegistry().dispatch(newEvent);
		}
		else
		{
			//we got a call to persistData() even though the user never called getClonesByFile()?
			//this might be a bug
			log
					.error(
							"persistData(): potential incorrect usage of store provider - clone data for file was not found in cache, nothing was persisted!",
							new Throwable());

			/*
			 * To handle this we could iterate over all cached clones and collect all clones which belong
			 * to this file. However, we couldn't just call subPersistData with such a set, as it might not
			 * be complete.
			 * In fact, we have no good way of knowing which of the clones which are in the persistent storage
			 * for that file were deleted and which were just not loaded into the cache.
			 * Overall it does not seem prudent to implement a solution for this.
			 * A user of this store provider could just take care to load all clones for this file first.
			 * 
			 * For now we just don't persist anything.
			 */

			//TODO: think about this again
		}

	}

	/**
	 * Gets a clone file and a set of all clones in that file and persists the data.<br/>
	 * Implicit addition, deletion and updating should be done. All already persisted clones which are
	 * not in the set need to be deleted and all of them which are in the set need to be updated.
	 * Any clone in the set which was not persisted before needs to be added.
	 * 
	 * @param file the clone file the clones belong to, never null
	 * @param clones a set of <b>all</b> clones belonging to that file.
	 */
	//TODO: what about annotations?
	protected abstract void subPersistData(ICloneFile file, Set<IClone> clones);

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.store.IStoreProvider#revertData(org.electrocodeogram.cpc.core.api.data.ICloneFile)
	 */
	@Override
	public void revertData(ICloneFile file) throws StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("revertData(): " + file);
		assert (file != null);

		if (!isWriteLockedByCurrentThread())
			throw new StoreLockingException("method requires exclusive write lock");

		//this is somewhat tricky because even though we may not have the file cached,
		//we may still have some of the individual clones in the cache
		//i.e. because they were members of a specific clone group which was retrieved

		//first check if we have the clone data for the file cached
		SortedSet<IClone> clones = fileUuidToClonesRegistry.get(file.getUuid());
		if (clones != null)
		{
			//yep, it's cached. This makes things much easier.
			//because any clone which might appear in some of the clone group collections
			//will definitely also be listed in this set
			for (IClone clone : clones)
			{
				//remove from individual cache
				cloneUuidToCloneRegistry.remove(clone.getUuid());

				//if this clone belongs to a group, we need to revert the version in that group too
				if (clone.getGroupUuid() != null)
				{
					Set<IClone> groupClones = groupUuidToClonesRegistry.get(clone.getGroupUuid());

					//check if there are some cached clones for the group
					if (groupClones != null)
					{
						//this is tricky again, we can't just delete the entry from the set because
						//other users of the group cache expect it to contain all clones for a group
						//or be null
						//we can't just clear it either, as some of the other clones might be dirty

						//so we have to reload the instace from our persistent store
						IClone oldClone = subGetCloneByUuid(clone.getUuid(), clone.getFileUuid());

						//and replace the version in the clone cache
						groupClones.remove(clone);
						if (oldClone != null)
							//there might not be an old version of the clone, if it was just recently added
							groupClones.add(oldClone);
					}

					//remove all clone diffs which we might have stored for this clone
					//cloneUuidToCloneDiffsRegistry.remove(clone.getUuid());
				}
			}

			//clear clone cache for file
			clones.clear();

			//remove from cache
			fileUuidToClonesRegistry.remove(file.getUuid());
		}
		else
		{
			/*
			 * We're being asked to revert the clone data of a file which is currently not cached
			 * this usually should not happen. As a typical user of the storage proivder would first
			 * request the clone data for a file which is being edited and then revert it if the user
			 * does not save the modifications.
			 * 
			 * Supporting this operation is not easy. Even though we do not have the file itself in our
			 * clone file cache, we may have loaded some of the clones from within that file into our
			 * clone group cache.
			 * So we do need to do something here. However, we can expect this to happen very seldom.
			 */

			log
					.warn("revertData(): called with a file handle which is not yet cached - THIS IS SLOW. You should always try to load the clone data for a file which you may want to revert later.");

			//due to the lack of any better lookup structure we just iterate through all our
			//cached clones here
			for (IClone clone : cloneUuidToCloneRegistry.values())
			{
				if (file.getUuid().equals(clone.getFileUuid()))
				{
					if (log.isTraceEnabled())
						log.trace("revertData(): found an affected clone - " + clone);

					//ok, we need to revert this clone
					//again, we can't just delete the clone, we have to replace it with a version from
					//stable storage
					IClone oldClone = subGetCloneByUuid(clone.getUuid(), clone.getFileUuid());

					if (oldClone != null)
					{
						if (log.isTraceEnabled())
							log.trace("revertData(): clone replaced with old version - " + oldClone);

						//ok, replace clone
						cloneUuidToCloneRegistry.put(clone.getUuid(), oldClone);
					}
					else
					{
						if (log.isTraceEnabled())
							log.trace("revertData(): no old version available, clone removed");

						//there is no old version of this clone, it was probably just recently added
						cloneUuidToCloneRegistry.remove(clone.getUuid());

						//usually this shouldn't happen because all our add methods do initialize the file cache
						log
								.warn("WARNING: revertData(): potential internal error - file cache is missing for recently added clone");
					}

					//remove the clone from any group cache in which it might be stored
					if (clone.getGroupUuid() != null)
					{
						Set<IClone> groupClones = groupUuidToClonesRegistry.get(clone.getGroupUuid());
						if (groupClones != null)
							groupClones.remove(clone);
					}

					//re-add the old clone to it's corresponding group cache if needed
					if ((oldClone != null) && (oldClone.getGroupUuid() != null))
					{
						Set<IClone> oldGroupClones = groupUuidToClonesRegistry.get(oldClone.getGroupUuid());
						if (oldGroupClones != null)
							oldGroupClones.add(clone);
					}

					//remove all clone diffs which we might have stored for this clone
					//cloneUuidToCloneDiffsRegistry.remove(clone.getUuid());
				}
			}
		}

		if (cloneModificationEventEnabled)
			//remember this clone file for later modification events
			transactionFullModifications.add(file.getUuid());
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider#getPersistedCloneFileContent(org.electrocodeogram.cpc.core.api.data.ICloneFile)
	 */
	@Override
	public String getPersistedCloneFileContent(ICloneFile file) throws StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("getPersistedCloneFileContent() - file: " + file);
		assert (file != null);

		String content = subGetCloneFileContent(file.getUuid());

		if (log.isTraceEnabled())
			log.trace("getPersistedCloneFileContent() - result: " + CoreStringUtils.truncateString(content));

		return content;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider#getPersistedClonesForFile(java.lang.String)
	 */
	@Override
	public List<IClone> getPersistedClonesForFile(String fileUuid)
	{
		if (log.isTraceEnabled())
			log.trace("getPersistedClonesForFile() - uuid: " + fileUuid);
		assert (fileUuid != null);

		List<IClone> result = null;

		rLock("getPersistedClonesForFile");
		try
		{
			List<IClone> subClones = subGetClonesByFile(fileUuid);
			result = CoreUtils.cloneCloneList(subClones, false);
		}
		finally
		{
			rUnLock("getPersistedClonesForFile");
		}

		if (log.isTraceEnabled())
			log.trace("getPersistedClonesForFile() - result: " + result);

		return result;
	}

	/**
	 * Writes the current content (on disk, ignores editor buffer state) of the specified file to the
	 * internal clone data store.
	 * 
	 * @param cloneFile the clone file to store the content for, never null.
	 */
	protected void setStoredCloneFileContent(ICloneFile cloneFile)
	{
		if (log.isTraceEnabled())
			log.trace("setStoredCloneFileContent() - cloneFile: " + cloneFile);
		assert (cloneFile != null);

		//first get the file handle
		IFile fileHandle = CoreFileUtils.getFileForCloneFile(cloneFile);
		if (fileHandle == null)
		{
			log.fatal("setStoredCloneFileContent() - unable to obtain clone file handle - cloneFile: " + cloneFile,
					new Throwable());
			return;
		}

		//update the file size and file modification 
		IFileInfo fileInfo;
		try
		{
			fileInfo = EFS.getStore(fileHandle.getLocationURI()).fetchInfo();
		}
		catch (CoreException e)
		{
			//WTF? something strange happened
			log.fatal("setStoredCloneFileContent() - result: unable to get file info - " + e, e);
			return;
		}

		//update size and modification time of file
		((ICreatorCloneFile) cloneFile).setSize(fileInfo.getLength());
		((ICreatorCloneFile) cloneFile).setModificationDate(fileInfo.getLastModified());

		//then get the contents
		String content = CoreUtils.readFileContent(fileHandle);

		//delegate to the store provider implementation
		subPersistCloneFileContent(cloneFile.getUuid(), content);
	}

	/**
	 * Stores the given content for the given file.
	 * 
	 * @param fileUuid uuid of the clone file to store content for, never null.
	 * @param content the content to store, never null.
	 */
	protected abstract void subPersistCloneFileContent(String fileUuid, String content);

	/**
	 * Retrieves the last content stored for the given file.
	 * 
	 * @param fileUuid uuid of the clone file to retrieve content for, never null.
	 * @return the stored content or NULL if no content is available.
	 */
	protected abstract String subGetCloneFileContent(String fileUuid);

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider#acquireWriteLock(org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode)
	 */
	@Override
	public void acquireWriteLock(LockMode mode) throws StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("acquireWriteLock() - mode: " + mode + ", lock: " + lock);

		try
		{
			_acquireWriteLock(mode, true, 0);
		}
		catch (InterruptedException e)
		{
			//can't happen
			log.error("acquireWriteLock() - interrupted while not expecting any interruption - " + e, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider#acquireWriteLockNonBlocking(org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode, long)
	 */
	@Override
	public boolean acquireWriteLockNonBlocking(LockMode mode, long maxWait) throws StoreLockingException,
			InterruptedException
	{
		if (log.isTraceEnabled())
			log.trace("acquireWriteLockNonBlocking() - mode: " + mode + ", maxWait: " + maxWait + ", lock: " + lock);

		return _acquireWriteLock(mode, false, maxWait);
	}

	protected boolean _acquireWriteLock(LockMode mode, boolean blocking, long maxWait) throws StoreLockingException,
			InterruptedException
	{
		if (mode == null)
			mode = LockMode.DEFAULT;

		//check if this is a reentrant locking request (the thread already holds the lock)
		if (lock.isWriteLockedByCurrentThread())
		{
			//this thread already has an exclusive write lock for the store provider.

			//log a message if someone tries to modify the lock mode
			if (!mode.equals(writeLockMode))
			{
				if (log.isDebugEnabled())
					log
							.debug("_acquireWriteLock() - different LockModes requested, current LockMode will be kept - current LockMode: "
									+ writeLockMode
									+ ", requested LockMode: "
									+ mode
									+ ", lock depth: "
									+ writeLockDepth);
			}

			//increase the reentrant lock counter
			assert (writeLockDepth >= 0);
			++writeLockDepth;

			if (writeLockDepth > MAX_LOCK_DEPTH)
			{
				//WTF? probably an endless loop/recursion somewhere?
				throw new StoreLockingException(
						"Maximal number of allowed IStoreProvider reentrant exclusive write locks exceeded - lock depth: "
								+ writeLockDepth);
			}

			//we're done
			if (log.isDebugEnabled())
				log
						.debug("_acquireWriteLock() - reentrant lock detected, ignoring - writeLockDepth: "
								+ writeLockDepth);

			return true;
		}

		/*
		 * The thread is not currently holding an exclusive write lock.
		 * Request one now.
		 */
		if (blocking)
		{
			lock.writeLock().lock();
		}
		else
		{
			if (!lock.writeLock().tryLock())
			{
				if (log.isTraceEnabled())
					log.trace("_acquireWriteLock(): write lock is currently held by another thread, going to wait for "
							+ maxWait + " ms.");

				if (!lock.writeLock().tryLock(maxWait, TimeUnit.MILLISECONDS))
				{
					//lock acquisition failed
					if (log.isTraceEnabled())
						log.trace("_acquireWriteLock(): FAILED to obtain write lock - " + lock);

					return false;
				}
			}
		}
		//lock acquired

		//remember the LockMode for potential future reentrant locking requests
		writeLockMode = mode;
		assert (writeLockDepth == 0);

		if (log.isTraceEnabled())
			log.trace("_acquireWriteLock(): got WRITE lock - " + lock);

		//check whether we should generate clone modification events
		if (LockMode.NO_MODIFICATION_EVENT.equals(mode)
				|| LockMode.NO_WRITE_LOCK_HOOK_NOTIFY_NO_MODIFICATION_EVENT.equals(mode))
			cloneModificationEventEnabled = false;
		else
			cloneModificationEventEnabled = true;

		//check if we need to notify any other module (aka CPC Track) about this lock
		if (writeLockHook != null && !LockMode.NO_WRITE_LOCK_HOOK_NOTIFY.equals(mode)
				&& !LockMode.NO_WRITE_LOCK_HOOK_NOTIFY_NO_MODIFICATION_EVENT.equals(mode))
		{
			//ok, notify hook
			log.trace("_acquireWriteLock() - notifying write lock hook");
			try
			{
				writeLockHook.aboutToGrantWriteLock();
			}
			catch (StoreLockingException e)
			{
				//this should never happen
				log.error("_acquireWriteLock() - locking error - " + e, e);
			}
			log.trace("_acquireWriteLock() - write lock hook returned");
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.store.IStoreProvider#releaseWriteLock()
	 */
	@Override
	public void releaseWriteLock()
	{
		if (log.isTraceEnabled())
			log.trace("releaseWriteLock(): " + lock);

		if (!lock.isWriteLockedByCurrentThread())
		{
			//we want a stack trace here, which is why we're throwing and catching a random exception here
			try
			{
				throw new IllegalStateException("trying to release a foreign lock");
			}
			catch (IllegalStateException e)
			{
				log.error("releaseWriteLock() - trying to release a foreign lock - " + e, e);
			}
			return;
		}

		/*
		 * Check if the lock was acquired multiple times. In that case, we only need to do anything
		 * here if this is the last/outermost release call.
		 */
		assert (writeLockDepth >= 0);
		if (writeLockDepth > 0)
		{
			--writeLockDepth;

			//we're done
			if (log.isDebugEnabled())
				log.debug("releaseWriteLock() - reentrant lock detected, ignoring - writeLockDepth: " + writeLockDepth);

			return;
		}

		/*
		 * Clone modification event generation, if needed.
		 *
		 * We can't dispatch events right away, as we need to make sure that
		 * all the transaction data structures are cleared up before we release
		 * the write lock. We have to wait till the very end.
		 * At this point we only enqueue the generated events.
		 */

		//queue for all generated events
		List<CPCEvent> eventQueue = null;

		try
		{

			if (cloneModificationEventEnabled)
			{
				eventQueue = new LinkedList<CPCEvent>();

				if (log.isTraceEnabled())
					log.trace("releaseWriteLock() - creating clone modification event(s) - added: "
							+ transactionAddedClones + ", moved: " + transactionMovedClones + ", modified: "
							+ transactionModifiedClones + ", removed: " + transactionRemovedClones);

				//check whether all clone data might have been cleared
				if (transactionRemovedAll)
				{
					//this is a special case, purgeData() was called and all clone data was removed
					CloneModificationEvent newEvent = new CloneModificationEvent(null);

					newEvent.setFullModification(true);

					if (log.isTraceEnabled())
						log.trace("releaseWriteLock() - created event: " + newEvent);

					eventQueue.add(newEvent);

					//TODO: do we need to somehow call ICloneModificationHistoryExtension.endOfTransaction() for all
					//clones here?
				}
				else
				{

					//Make sure that some data was modified
					if (transactionAddedClones.isEmpty() && transactionMovedClones.isEmpty()
							&& transactionModifiedClones.isEmpty() && transactionRemovedClones.isEmpty()
							&& transactionFullModifications.isEmpty())
					{
						log.trace("releaseWriteLock() - no clone data was modified, not generating any events.");
						return;
					}

					//get a list of all file uuids
					Set<String> fileUuids = new HashSet<String>(5);
					fileUuids.addAll(transactionFullModifications);
					fileUuids.addAll(transactionAddedClones.keySet());
					fileUuids.addAll(transactionMovedClones.keySet());
					fileUuids.addAll(transactionModifiedClones.keySet());
					fileUuids.addAll(transactionRemovedClones.keySet());

					if (log.isTraceEnabled())
						log.trace("releaseWriteLock() - affected file uuids: " + fileUuids);

					//create an event for each file
					for (String fileUuid : fileUuids)
					{
						//get the clone file
						ICloneFile cloneFile = _lookupCloneFile(fileUuid);
						if (cloneFile == null)
						{
							//check if this file was recently deleted
							cloneFile = transactionDeletedFileDataBuffer.get(fileUuid);
							if (cloneFile == null)
							{
								//We have no clone data info. This shouldn't happen.
								log.error("releaseWriteLock() - unable to retrieve clone file data for file uuid: "
										+ fileUuid, new Throwable());
								continue;
							}
						}

						//create a cloned and sealed copy for the event 
						ICloneFile newCloneFile;
						try
						{
							newCloneFile = (ICloneFile) cloneFile.clone();
						}
						catch (CloneNotSupportedException e)
						{
							log.error("releaseWriteLock() - unable to clone cloneFile: " + cloneFile + " - " + e, e);
							continue;
						}
						newCloneFile.seal();
						CloneModificationEvent newEvent = new CloneModificationEvent(newCloneFile);

						//check if this was a complete modification
						if (transactionFullModifications.contains(fileUuid))
						{
							newEvent.setFullModification(true);
						}
						else
						{
							List<IClone> addedClones = buildCloneChangeList(transactionAddedClones, fileUuid, false);
							if (!addedClones.isEmpty())
								newEvent.setAddedClones(addedClones);

							List<IClone> movedClones = buildCloneChangeList(transactionMovedClones, fileUuid, false);
							if (!movedClones.isEmpty())
								newEvent.setMovedClones(movedClones);

							List<IClone> modifiedClones = buildCloneChangeList(transactionModifiedClones, fileUuid,
									true);
							if (!modifiedClones.isEmpty())
								newEvent.setModifiedClones(modifiedClones);

							//OLD:
							//								/*
							//								 * We can't just copy over the modified clones as we did with the other
							//								 * clone lists. Modified clones in an CloneModificationEvent are required
							//								 * to contain an ICloneModificationHistoryExtension with CloneDiffs that
							//								 * describe the modifications.
							//								 * These CloneDiff events should already be cached in the transactionModifiedCloneDiffs
							//								 * lookup structure. We'll need to extract them and attach a new 
							//								 * ICloneModificationHistoryExtension object to the clone.
							//								 * 
							//								 * Some other things to consider:
							//								 * 
							//								 * If the client called persistData() before releasing the lock, the clone history
							//								 * extension of all these clones will have been cleared (the extension object is present,
							//								 * but it contains no diff data).
							//								 * 
							//								 * However, if the data was not persisted, then these modified clone objects may
							//								 * contin history objects which contain more diffs than the one from this transaction.
							//								 * They are all cleared out here and replaced with the transaction diffs.
							//								 * It is important to keep in mind that a clone returned from lookupClone() or some
							//								 * other method may contain more diff elements than a clone which is part of
							//								 * a clone modification event.
							//								 */
							//								List<IClone> newModifiedClones = CoreUtils.cloneCloneList(modifiedClones, false);
							//
							//								for (IClone modifiedClone : newModifiedClones)
							//								{
							//									//get the pending CloneDiff objects which were added during this transaction
							//									SortedSet<CloneDiff> cloneDiffs = transactionModifiedCloneDiffs.get(modifiedClone
							//											.getUuid());
							//									if (cloneDiffs != null && !cloneDiffs.isEmpty())
							//									{
							//										//create a new history object
							//										ICloneModificationHistoryExtension history = (ICloneModificationHistoryExtension) cloneFactoryProvider
							//												.getInstance(ICloneModificationHistoryExtension.class);
							//										assert (history != null);
							//
							//										//add the pending CloneDiffs of this transaction
							//										history.addCloneDiffs(cloneDiffs);
							//
							//										//store the history with the clone object
							//										modifiedClone.addExtension(history);
							//
							//										if (log.isTraceEnabled())
							//											log
							//													.trace("releaseWriteLock() - new temporary modification history extension for clone modification event - clone: "
							//															+ modifiedClone + ", history: " + history);
							//									}
							//									else
							//									{
							//										//this shouldn't happen. Each modified clone is supposed to have some clonediffs
							//										log.error("releaseWriteLock() - modified clone without clone diffs: "
							//												+ modifiedClone);
							//
							//										//make sure there is no old history extension present
							//										modifiedClone.removeExtension(ICloneModificationHistoryExtension.class);
							//									}
							//
							//									//now seal the clone
							//									modifiedClone.seal();
							//								}
							//
							//								//now add all those freshly cloned clones with history to the event
							//								newEvent.setModifiedClones(newModifiedClones);

							List<IClone> removedClones = buildCloneChangeList(transactionRemovedClones, fileUuid, false);
							if (!removedClones.isEmpty())
								newEvent.setRemovedClones(removedClones);
						}

						if (log.isTraceEnabled())
							log.trace("releaseWriteLock() - created event: " + newEvent);

						eventQueue.add(newEvent);
					}
				}

				//clear lists
				transactionDeletedFileDataBuffer.clear();
				transactionFullModifications.clear();
				transactionAddedClones.clear();
				transactionMovedClones.clear();
				transactionModifiedClones.clear();
				transactionRemovedClones.clear();
				//transactionModifiedCloneDiffs.clear();
				transactionRemovedAll = false;
			}
		}
		finally
		{
			/*
			 * Release exclusive lock
			 */

			if (log.isTraceEnabled())
				log.trace("releaseWriteLock(): releasing WRITE lock - " + lock);

			lock.writeLock().unlock();
		}

		/*
		 * Dispatch enqueued events
		 */

		if (cloneModificationEventEnabled && eventQueue != null)
		{
			if (log.isTraceEnabled())
				log.trace("releaseWriteLock() - going to dispatch " + eventQueue.size() + " events.");

			//dispatch all enqueued events
			for (CPCEvent event : eventQueue)
				CPCCorePlugin.getEventHubRegistry().dispatch(event);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider#holdingWriteLock()
	 */
	@Override
	public boolean holdingWriteLock()
	{
		return lock.isWriteLockedByCurrentThread();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider#setWriteLockHook(org.electrocodeogram.cpc.core.api.provider.store.IStoreProviderWriteLockHook)
	 */
	@Override
	public void setWriteLockHook(IStoreProviderWriteLockHook writeLockHook)
	{
		if (log.isTraceEnabled())
			log.trace("setWriteLockHook() - writeLockHook: " + writeLockHook);

		if (this.writeLockHook != null && this.writeLockHook != writeLockHook)
			//the write lock is being replaced, this is most likely a misuse of the IStoreProvider API
			//let's warn the user/developer
			log.warn("setWriteLockHook() - write lock hook was overwritten - old hook: " + this.writeLockHook
					+ ", new hook: " + writeLockHook);

		this.writeLockHook = writeLockHook;
	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see org.electrocodeogram.cpc.core.api.provider.store.IRemotableStoreProvider#setExternalModificationCheckEnables(boolean)
	//	 */
	//	@Override
	//	public void setExternalModificationCheckEnabled(boolean extModCheckEnabled)
	//	{
	//		if (log.isTraceEnabled())
	//			log.trace("setExternalModificationCheckEnables() - extModCheckEnabled: " + extModCheckEnabled);
	//
	//		externalModificationDetectionEnabled = extModCheckEnabled;
	//	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.store.IStoreProvider#hintPurgeData(org.electrocodeogram.cpc.core.api.data.ICloneFile)
	 */
	@Override
	public void hintPurgeCache(ICloneFile file)
	{
		if (log.isTraceEnabled())
			log.trace("hintPurgeCache(): " + file);
		assert (file != null);

		int purgedCloneCount = 0;
		int purgedGroupCount = 0;

		synchronized (fileUuidToClonesRegistry)
		{
			//this means we can drop the corresponding file cache entry, if it does not contain any dirty clones
			SortedSet<IClone> clones = fileUuidToClonesRegistry.get(file.getUuid());
			if (clones == null)
			{
				log.trace("hintPurgeCache(): file was not in cache");

				//nothing is cached for this file => nothing to do
				return;
			}

			//check each clone for it's dirty state
			boolean anyDirty = false;
			for (IClone clone : clones)
			{
				if (((IStoreCloneObject) clone).isDirty())
				{
					if (log.isTraceEnabled())
						log.trace("hintPurgeCache() - dirty clone: " + clone);
					anyDirty = true;
				}
			}

			if (anyDirty)
			{
				log.trace("hintPurgeCache(): file cache is dirty, not purged.");

				//we can't clear this file cache, it's dirty
				return;
			}

			//ok, it's not dirty. We can kill it.
			fileUuidToClonesRegistry.remove(file.getUuid());

			//we should also kill all individual clone instances, if they're not needed for the group cache
			CLONE_LOOP: for (IClone clone : clones)
			{
				//check if the clone belongs to a group
				if (clone.getGroupUuid() != null)
				{
					//make sure this clone isn't needed for the group cache
					Set<IClone> groupClones = groupUuidToClonesRegistry.get(clone.getGroupUuid());
					if (groupClones != null)
					{
						//this group's clone data is in cache

						/*
						 * Decide whether we can drop the clone (and its group) from cache.
						 * There are multiple cases where this is possible.
						 * a) All group members are also located within this file (and will thus all be removed).
						 * b) For all group members, which are not located within this file, the corresponding file is
						 *    not in cache. This happens if the group data was only cached because of this file.
						 *    Thus, the group cache can be cleared without problems.
						 * 
						 * In both cases we need to make sure that the group is not dirty.
						 * In case b) we also need to ensure that none of the other group members is dirty
						 * (though if they were, that would probably be a bug)
						 */

						//check a) and b) for all group members
						for (IClone groupClone : groupClones)
						{
							//check if this clone is located within the same file
							if (groupClone.getFileUuid().equals(file.getUuid()))
								//ok, same file
								continue;

							//check if the other file is not cached and the clone is not dirty
							if (!((IStoreCloneObject) clone).isDirty()
									&& !fileUuidToClonesRegistry.containsKey(groupClone.getFileUuid()))
								//ok, the file's clone data is not cached and the clone is not dirty
								continue;

							//don't remove the clone from cache, it is still needed
							if (log.isTraceEnabled())
								log.trace("hintPurgeCache() - not removing clone from cache, still part of a group: "
										+ clone.getUuid() + ", group: " + clone.getGroupUuid());
							continue CLONE_LOOP;
						}

						//ok, all group members will be cleared together or are no longer needed, there is no need to keep
						//the group cache either
						if (log.isTraceEnabled())
							log.trace("hintPurgeCache() - removing unused group from cache: " + clone.getGroupUuid());
						groupUuidToClonesRegistry.remove(clone.getGroupUuid());
						groupUuidToGroupRegistry.remove(clone.getGroupUuid());
						++purgedGroupCount;
					}
				}

				//ok, remove the clone
				cloneUuidToCloneRegistry.remove(clone.getUuid());
				++purgedCloneCount;
			}

			clones.clear();
		}

		if (log.isTraceEnabled())
			log.trace("hintPurgeCache() - purged - " + purgedCloneCount + " clones, " + purgedGroupCount + " groups.");

	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.store.IStoreProvider#hintPurgeData(org.electrocodeogram.cpc.core.api.data.ICloneGroup)
	 */
	@Override
	public void hintPurgeCache(ICloneGroup group)
	{
		if (log.isTraceEnabled())
			log.trace("hintPurgeCache(): " + group);

	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider#purgeCache()
	 */
	@Override
	public void purgeCache()
	{
		log.trace("purgeCache()");

		//FIXME: maybe we need a write lock instead?
		rLock("purgeCache");
		try
		{
			//TODO: implement me

			log.warn("purgeCache() - not yet implemented");

			subPurgeCache();
		}
		finally
		{
			rUnLock("purgeCache");
		}
	}

	/**
	 * Purges all cached data which is not dirty.
	 * 
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider#purgeCache()
	 */
	protected abstract void subPurgeCache();

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider#purgeData()
	 */
	@Override
	public void purgeData() throws StoreLockingException
	{
		log.trace("purgeData()");

		if (!isWriteLockedByCurrentThread())
			throw new StoreLockingException("method requires exclusive write lock");

		//first clear all caches
		pathToFileUuidRegistry.clear();
		oldPathToFileUuidCache.clear();
		fileUuidToFileRegistry.clear();
		cloneUuidToCloneRegistry.clear();
		groupUuidToGroupRegistry.clear();
		fileUuidToClonesRegistry.clear();
		groupUuidToClonesRegistry.clear();

		//now tell the underlying implementation to purge all data from storage
		subPurgeData();

		if (cloneModificationEventEnabled)
			//remember this purge event for later modification events
			transactionRemovedAll = true;

		/*
		 * Now notify any interested parties about this persistence event.
		 * I.e. a remote store provider might want to always keep an update
		 * to date copy of this data inside the workspace/project directory
		 * (in order for it to be checked in/out of CVS/SVN)
		 */
		ClonePersistenceEvent newEvent = new ClonePersistenceEvent(null);
		newEvent.setClones(EMPTY_CLONE_LIST);
		CPCCorePlugin.getEventHubRegistry().dispatch(newEvent);
	}

	/**
	 * Purges all cached an persisted data. Returns the store provider into it's initial
	 * installation state.
	 * 
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider#purgeData()
	 */
	protected abstract void subPurgeData();

	/*
	 * Utility methods for child classes
	 */

	protected boolean isWriteLockedByCurrentThread()
	{
		return lock.isWriteLockedByCurrentThread();
	}

	protected void rLock(String task)
	{
		if (log.isTraceEnabled())
			log.trace("  requesting READ lock: " + task + " ON " + lock);

		lock.readLock().lock();

		if (log.isTraceEnabled())
			log.trace("  got READ lock: " + task + " ON " + lock);
	}

	protected void rUnLock(String task)
	{
		if (log.isTraceEnabled())
			log.trace("  releasing READ lock: " + task + " ON " + lock);

		lock.readLock().unlock();
	}

	protected void wLock(String task)
	{
		if (log.isTraceEnabled())
			log.trace("  requesting WRITE lock: " + task + " ON " + lock);

		lock.writeLock().lock();

		if (log.isTraceEnabled())
			log.trace("  got WRITE lock: " + task + " ON " + lock);
	}

	protected void wUnLock(String task)
	{
		if (log.isTraceEnabled())
			log.trace("  releasing WRITE lock: " + task + " ON " + lock);

		lock.writeLock().unlock();
	}

	/**
	 * Takes the path to a file (relative to workspace) and generates a matching CloneFile instance.<br/>
	 * If the file does not exist or is not readable, NULL is returned. If the file exists it will be searched
	 * for any CPC UUID comment. If such a comment is found the corresponding UUID is used. Otherwise a
	 * new UUID is generated.
	 * 
	 * @param project name of the project, never null.
	 * @param filePath the file to create a CloneFile for, relative to project, never null.
	 * @param createNewUuidIfNeeded whether to create a new UUID if the old UUID can not be determined.
	 * @return a new {@link ICloneFile} instance (new or old uuid) or NULL on error or if
	 * 		<em>createNewUuidIfNeeded</em> was <em>false</em> and the UUID for the file could not be
	 * 		determined.<br/>
	 * 		The returned file will be "marked" ({@link ICloneObject#isMarked()} is <em>true</em>) if
	 * 		it was newly created with a new UUID. If the file is not "marked" the file's UUID could
	 * 		still be extracted and was reused. 
	 */
	protected ICloneFile createCloneFileFromFile(String project, String filePath, boolean createNewUuidIfNeeded)
	{
		if (log.isTraceEnabled())
			log.trace("createCloneFileFromFile() - project: " + project + ", filePath: " + filePath
					+ ", createHandleIfNeeded: " + createNewUuidIfNeeded);
		assert (project != null && filePath != null);

		ICreatorCloneFile file;
		String fullFilePath = project + "/" + filePath;
		String uuid = null;
		long fileSize;
		long fileModDate;

		//these two will only be used in eclipse mode
		IFile fileHandle = null;
		String propertyUuid = null;

		if (!standaloneTestMode)
		{
			//get a resource for that file
			fileHandle = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fullFilePath));
			if ((fileHandle == null) || (!fileHandle.exists()))
			{
				//the file does not exist (this is not an error)
				if (log.isDebugEnabled())
					log.debug("createCloneFileFromFile() - result: file not found - project: " + project
							+ ", filePath: " + filePath);
				return null;
			}

			IFileInfo fileInfo;
			try
			{
				fileInfo = EFS.getStore(fileHandle.getLocationURI()).fetchInfo();
			}
			catch (CoreException e)
			{
				//WTF? something strange happened
				log.warn("createCloneFileFromFile() - result: unable to get file info - " + e);
				return null;
			}

			//check if we have persisted the uuid in the files preferences
			propertyUuid = CoreFileUtils.getFileUuidProperty(fileHandle);

			if (propertyUuid != null)
				//use this uuid
				uuid = propertyUuid;

			//if we still don't have a file uuid, we'll need to check the file
			//			if (uuid == null)
			//			{
			//				//check whether there is any file UUID comment present in the file
			//				//TODO: decide whether we really want to append file uuids comments to files.
			//				//If yes, do it somewhere. If no, remove this check.
			//				uuid = FileUtils.extractUuid(fileHandle);
			//			}

			fileSize = fileInfo.getLength();
			fileModDate = fileInfo.getLastModified();
		}
		else
		{
			//we're in test mode and don't have access to any resources, use random data
			fileSize = (long) (Math.random() * 10000000);
			fileModDate = System.currentTimeMillis() - (long) (Math.random() * 100000);
		}

		//create new CloneFile
		if (uuid != null)
		{
			//use existing uuid
			if (log.isTraceEnabled())
				log.trace("createCloneFileFromFile() - existing UUID for file - " + uuid);

			file = (ICreatorCloneFile) cloneFactoryProvider.getInstance(ICloneFile.class, uuid);
		}
		else
		{
			//create new uuid

			if (!createNewUuidIfNeeded)
			{
				//we were instructed not to generate a new file UUID, skip it.
				log
						.trace("createCloneFileFromFile() - no existing uuid could be found and createNewUuidIfNeeded is false, returning null.");
				return null;
			}

			if (log.isTraceEnabled())
				log.trace("createCloneFileFromFile() - unable to extract old UUID for file, generating new UUID.");

			file = (ICreatorCloneFile) cloneFactoryProvider.getInstance(ICloneFile.class);
			file.setMarked(true);

			//TODO: we need to append the new file uuid to the file at some point.
			//		we could either do that here or (if the file is open in an editor) we could
			//		append the uuid to the editor buffer.
			//		doing it here might have some issues if the files are not in sync (i.e. unsaved?)
			//		doing it in the editor buffer would only work for open files
		}

		//set data
		file.setProject(project);
		file.setPath(filePath);
		file.setSize(fileSize);
		file.setModificationDate(fileModDate);

		//persist uuid in file properties if not yet set
		if ((fileHandle != null) && (propertyUuid == null))
			CoreFileUtils.setFileUuidProperty(fileHandle, file.getUuid());

		if (log.isTraceEnabled())
			log.trace("createCloneFileFromFile() - result: " + file);

		return file;
	}

	/**
	 * Takes an {@link ICloneFile} which could not be found by path but for which an
	 * UUID could be extracted. The given clone file instance is a newly created instance
	 * that was not yet added to the internal cache.<br/>
	 * <br/>
	 * This method checks whether the clone file UUID is already present in our local storage
	 * and if it is, the corresponding file entry is updated (moved) and returned.<br/>
	 * <br/>
	 * It also checks whether the file might have been copied. As this could lead to
	 * multiple files with the same UUID. If the file was copied, the persistent UUID
	 * property of the new file is cleared and NULL is returned.
	 * In this case, no file move is performed and the old file is left untouched.<br/>
	 * <br/>
	 * This is needed as some store provider implementations may depend on receiving a call
	 * to {@link AbstractStoreProvider#subMoveCloneFile(ICloneFile, String, String)} in case of
	 * a file move and not just a call to {@link AbstractStoreProvider#subPersistCloneFile(ICloneFile)}
	 * with updated project and path values. 
	 * 
	 * @param file {@link ICloneFile} instance belonging to a file which was potentially moved, never null.
	 * @return either the provided {@link ICloneFile} instance or, if one exists, the corresponding
	 * 		{@link ICloneFile} instance from the cache or persistent storage. In cases where the
	 * 		file was copied NULL is returned.
	 */
	protected ICloneFile processPotentialFileMove(ICloneFile file)
	{
		if (log.isTraceEnabled())
			log.trace("processPotentialFileMove() - file: " + file);

		//look for any old version
		ICloneFile oldFile = _lookupCloneFile(file.getUuid());
		if (oldFile == null)
		{
			log
					.warn("processPotentialFileMove() - unable to find corresponding file entry in storage, assuming this is not a move - file: "
							+ file);
			return file;
		}

		//make sure this is a move
		if (oldFile.getProject().equals(file.getProject()) && oldFile.getPath().equals(file.getPath()))
		{
			//strange, if the file hasn't moved, we should have been able to find it!
			log.error(
					"processPotentialFileMove() - no change in project and path between old and new file entry - old: "
							+ oldFile + ", new: " + file, new Throwable());
			return file;
		}

		//file was moved, make sure the old version no longer exists. If it does
		//the file was copied and not moved!
		IFile oldFileHandle = CoreFileUtils.getFile(oldFile.getProject(), oldFile.getPath());
		if (oldFileHandle != null && oldFileHandle.exists())
		{
			String oldFileUuid = CoreFileUtils.getFileUuidProperty(oldFileHandle);

			log.info("processPotentialFileMove() - old file still exists, not a move? - newFile: " + file
					+ ", oldFile: " + oldFile + ", oldFileUuid: " + oldFileUuid);

			//check if it has a persistent uuid property
			if (oldFileUuid != null && oldFileUuid.equals(file.getUuid()))
			{
				/*
				 * Ok, we have a problem.
				 * We now have two files with the same UUID.
				 * Most likely the new file is a _copy_ of the old file.
				 * So this wouldn't be a real move.
				 */
				log
						.warn(
								"processPotentialFileMove() - old and new file use the same UUID, probably a copy operation, reseting UUID of new file - newFile: "
										+ file + ", oldFile: " + oldFile + ", oldFileUuid: " + oldFileUuid,
								new Throwable());

				//get an IFile handle for the new (copied!) file
				IFile newFileHandle = CoreFileUtils.getFile(file.getProject(), file.getPath());
				if (newFileHandle == null)
				{
					log
							.error(
									"processPotentialFileMove() - unable to obtain file handle for new file in order to reset the persistent file UUID - newFile: "
											+ file + ", oldFile: " + oldFile + ", oldFileUuid: " + oldFileUuid,
									new Throwable());
					//there is nothing we can do
					return file;
				}

				//delete persistent UUID from new file
				CoreFileUtils.setFileUuidProperty(newFileHandle, null);

				//tell our caller that the file lookup should be restarted
				return null;
			}
		}

		//ok the file has moved, tell the store provider implementation about it
		_moveCloneFile(oldFile, file.getProject(), file.getPath());

		if (log.isDebugEnabled())
			log
					.debug("processPotentialFileMove() - file move detected, returning updated old clone file intance - updated old instance: "
							+ oldFile + ", discarded instance: " + file);

		return oldFile;
	}

	//	/**
	//	 * Checks whether the file corresponding to the given fileUuid was externally modified
	//	 * since the last time the clone file entry was persisted.
	//	 * 
	//	 * @param fileUuid the file uuid of the clone file to check for external modification, never null.
	//	 * @return a {@link ICloneFile} object if an external modification was detected, NULL otherwise.
	//	 */
	//	protected ICloneFile checkForExternalModification(String fileUuid)
	//	{
	//		if (log.isTraceEnabled())
	//			log.trace("checkForExternalModification() - fileUuid: " + fileUuid);
	//		assert (fileUuid != null);
	//
	//		//make sure the clone file entry is cached
	//		if (!fileUuidToFileRegistry.containsKey(fileUuid))
	//		{
	//			if (log.isDebugEnabled())
	//				log.debug("checkForExternalModification() - clone file entry not yet in cache - fileUuid: " + fileUuid);
	//
	//			//we're ignoring the return value as this clone will fill in the required fileUuidToFileRegistry entry
	//			lookupCloneFile(fileUuid);
	//		}
	//
	//		//first get the clone file
	//		ICloneFile cloneFile = fileUuidToFileRegistry.get(fileUuid);
	//		if (cloneFile == null)
	//		{
	//			//the file uuid seems to be illegal/unknown, that shouldn't happen
	//			log.error("checkForExternalModification() - unable to obtain clone file for fileUuid: " + fileUuid,
	//					new Throwable());
	//			return null;
	//		}
	//
	//		if (cloneFile.getModificationDate() <= 0)
	//		{
	//			//for some reason we don't have a modification date, we can therefore only skip this check
	//			log.warn("checkForExternalModification() - clone file entry has no modification date: " + cloneFile);
	//			return null;
	//		}
	//
	//		//now find the underlying file
	//		IFile fileHandle = CoreFileUtils.getFileForCloneFile(cloneFile);
	//		if (fileHandle == null)
	//		{
	//			/*
	//			 * This can happen if the file was deleted or its project was closed.
	//			 */
	//			if (log.isDebugEnabled())
	//				log.debug("checkForExternalModification() - unable to obtain file handle for clone file, ignoring: "
	//						+ cloneFile);
	//			return null;
	//		}
	//
	//		//get the current modification timestamp
	//		IFileInfo fileInfo;
	//		try
	//		{
	//			fileInfo = EFS.getStore(fileHandle.getLocationURI()).fetchInfo();
	//		}
	//		catch (CoreException e)
	//		{
	//			//WTF? something strange happened
	//			log.warn("checkForExternalModification() - unable to get file info - " + e, e);
	//			return null;
	//		}
	//
	//		/*
	//		 * Ok, now we finally have a clone file entry with modification date and a file handle for the file on disk.
	//		 * Now we can do the real checking.
	//		 */
	//
	//		if (cloneFile.getModificationDate() != fileInfo.getLastModified()
	//				|| cloneFile.getSize() != fileInfo.getLength())
	//		{
	//			//Ok, the modification dates differ. It is possible that the file was externally modified.
	//			//Lets see if the content was changed.
	//			String persistedContent = subGetCloneFileContent(fileUuid);
	//			String currentContent = CoreUtils.readFileContent(fileHandle);
	//			if (persistedContent == null || currentContent == null)
	//			{
	//				/*
	//				 * There will be no persisted content if the file was never saved after an clone file
	//				 * handle was created for it.
	//				 * This is therefore not an error but a sometimes to be expected condition.
	//				 */
	//				log.debug("checkForExternalModification() - unable to get file content - cloneFile: " + cloneFile
	//						+ ", persistedContent: " + CoreStringUtils.truncateString(persistedContent)
	//						+ ", currentContent: " + CoreStringUtils.truncateString(currentContent));
	//				return null;
	//			}
	//
	//			if (!persistedContent.equals(currentContent))
	//			{
	//				//Seems as if there really was an external modification.
	//				log
	//						.info("checkForExternalModification() - file content was externally modified, trying to reconcile changes - fileUuid: "
	//								+ cloneFile.getUuid()
	//								+ ", project: "
	//								+ cloneFile.getProject()
	//								+ ", path: "
	//								+ cloneFile.getPath());
	//
	//				//try to reconcile the external modification
	//				return cloneFile;
	//			}
	//			else
	//			{
	//				if (log.isDebugEnabled())
	//					log
	//							.debug("checkForExternalModification() - modification date changed but content is still equal - cloneFile: "
	//									+ cloneFile
	//									+ ", persisted modDate: "
	//									+ cloneFile.getModificationDate()
	//									+ ", persisted size: "
	//									+ cloneFile.getSize()
	//									+ " - current modDate: "
	//									+ fileInfo.getLastModified() + ", current size: " + fileInfo.getLength());
	//
	//				return null;
	//			}
	//		}
	//		else
	//		{
	//			if (log.isTraceEnabled())
	//				log
	//						.trace("checkForExternalModification() - modification date and size of clone file entry and filesystem file match - modificationDate: "
	//								+ cloneFile.getModificationDate() + ", size: " + cloneFile.getSize());
	//
	//			return null;
	//		}
	//
	//	}
	//	/**
	//	 * Tries to reconcile a persisted file and its persisted clone data with the new content of the file
	//	 * after an external modification.<br/>
	//	 * <br/>
	//	 * <b>IMPORTANT:</b> This method acquires an exclusive write lock an <b>MUST NOT be called from within a block which
	//	 * already holds a read lock</b>.
	//	 * 
	//	 * @param cloneFile the clone file to reconcile, never null.
	//	 * @return true if any clone data was modified due to the reconciler run, false otherwise.
	//	 */
	//	protected boolean reconcileExternalModification(ICloneFile cloneFile)
	//	{
	//		if (log.isTraceEnabled())
	//			log.trace("reconcileExternalModification() - cloneFile: " + cloneFile);
	//		assert (cloneFile != null);
	//
	//		log.info("reconcileExternalModification() - reconciling ext. modifications - clone file: " + cloneFile);
	//
	//		//get an IExternalModificationReconcilerProvider reference 
	//		IExternalModificationReconcilerProvider extModReconcilerProvider = (IExternalModificationReconcilerProvider) CPCCorePlugin
	//				.getProviderRegistry().lookupProvider(IExternalModificationReconcilerProvider.class);
	//		if (extModReconcilerProvider == null)
	//		{
	//			log
	//					.warn("reconcileExternalModification() - unable to obtain external modification reconciler provider, not reconciling external change.");
	//			return false;
	//		}
	//
	//		IReconciliationResult result = null;
	//		boolean cloneDataModified = false;
	//
	//		//we'll need to get a lock for this work
	//		try
	//		{
	//			acquireWriteLock(LockMode.DEFAULT);
	//
	//			//get the persisted clones for this file
	//			List<IClone> persistedClones = getClonesByFile(cloneFile.getUuid(), -1, -1, false);
	//			if (persistedClones.isEmpty())
	//			{
	//				log
	//						.info("reconcileExternalModification() - file contained no clones, no reconciliation needed - cloneFile: "
	//								+ cloneFile);
	//
	//				//this also updates the persistedFileContent, we therefore need to call it even if
	//				//we did not modify any clone data
	//				persistData(cloneFile);
	//
	//				return false;
	//			}
	//
	//			//now get the persisted and file content
	//			String persistedFileContent = getPersistedCloneFileContent(cloneFile);
	//
	//			//and the current file content
	//			IFile fileHandle = CoreFileUtils.getFileForCloneFile(cloneFile);
	//			String newFileContent = CoreUtils.readFileContent(fileHandle);
	//
	//			if (persistedFileContent == null || newFileContent == null)
	//			{
	//				log.warn("reconcileExternalModification() - unable to get file content - cloneFile: " + cloneFile
	//						+ ", persistedFileContent: " + CoreStringUtils.truncateString(persistedFileContent)
	//						+ ", newFileContent: " + CoreStringUtils.truncateString(newFileContent));
	//				return false;
	//			}
	//
	//			//now delegate to the external modification reconciler provider
	//			result = extModReconcilerProvider.reconcile(cloneFile, persistedClones, persistedFileContent,
	//					newFileContent, true);
	//			//TODO: instead of always setting notifyUser to true here, we should be getting the value from
	//			//		some preference setting.
	//
	//			if (log.isTraceEnabled())
	//				log.trace("reconcileExternalModification() - extModReconciler result: " + result);
	//
	//			log
	//					.info("reconcileExternalModification() - reconciliation finished - total: "
	//							+ persistedClones.size() + ", lost: " + result.getLostClones().size() + ", removed: "
	//							+ result.getRemovedClones().size() + ", moved: " + result.getMovedClones().size()
	//							+ ", modified: " + result.getModifiedClones().size());
	//
	//			/*
	//			 * Now apply the new data.
	//			 */
	//
	//			//remove any clones which were lost or removed
	//			if (!result.getLostClones().isEmpty())
	//			{
	//				log.info("reconcileExternalModification() - lost clones: " + result.getLostClones());
	//				removeClones(result.getLostClones());
	//				cloneDataModified = true;
	//			}
	//
	//			if (!result.getRemovedClones().isEmpty())
	//			{
	//				log.info("reconcileExternalModification() - removed clones: " + result.getLostClones());
	//				removeClones(result.getRemovedClones());
	//				cloneDataModified = true;
	//			}
	//
	//			//update any clones which were moved or modified
	//			if (!result.getMovedClones().isEmpty())
	//			{
	//				log.info("reconcileExternalModification() - moved clones: " + result.getLostClones());
	//				updateClones(result.getMovedClones(), UpdateMode.MOVED);
	//				cloneDataModified = true;
	//			}
	//			if (!result.getModifiedClones().isEmpty())
	//			{
	//				log.info("reconcileExternalModification() - modified clones: " + result.getLostClones());
	//				updateClones(result.getModifiedClones(), UpdateMode.MODIFIED);
	//				cloneDataModified = true;
	//			}
	//
	//			/*
	//			 * Now persist the modification.
	//			 */
	//
	//			//this also updates the persistedFileContent, we therefore need to call it even if
	//			//we did not modify any clone data
	//			persistData(cloneFile);
	//
	//		}
	//		catch (StoreLockingException e)
	//		{
	//			//this should never happen
	//			log.error("reconcileExternalModification() - locking error - " + e, e);
	//		}
	//		finally
	//		{
	//			//make sure we release the lock
	//			releaseWriteLock();
	//		}
	//
	//		if (log.isTraceEnabled())
	//			log.trace("reconcileExternalModification() - result: " + cloneDataModified);
	//
	//		return cloneDataModified;
	//	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IDebuggableStoreProvider#checkCacheIntegrity()
	 * 
	 * Debugging method. It checks all caches for specific integrity constraints.
	 * This is SLOW!
	 */
	@Override
	public boolean checkCacheIntegrity()
	{
		boolean integrityOk = true;

		/*
		 * Make sure that we have at most one instance per uuid
		 */

		log.trace("checkCacheIntegrity(): checking uniquness of uuids over Clone instances");

		//check for all file caches
		for (Set<IClone> fileClones : fileUuidToClonesRegistry.values())
		{
			for (IClone clone : fileClones)
			{
				IClone otherClone = cloneUuidToCloneRegistry.get(clone.getUuid());

				//we expect that the other clone is ALWAYS cached
				if (otherClone == null)
				{
					log.error(
							"checkCacheIntegrity(): fileUuidToClonesRegistry contains a clone which is not in cloneUuidToCloneRegistry: "
									+ clone, new Throwable());
					integrityOk = false;
				}
				else if (clone != otherClone)
				{
					//we expect that clone and otherClone are the same instance
					log.error("checkCacheIntegrity(): INTERNAL ERROR - different instances for same uuid: " + clone
							+ " AND " + otherClone + " VIA " + fileUuidToClonesRegistry, new Throwable());
					integrityOk = false;
				}
			}
		}

		//check for all group caches
		for (Set<IClone> groupClones : groupUuidToClonesRegistry.values())
		{
			for (IClone clone : groupClones)
			{
				IClone otherClone = cloneUuidToCloneRegistry.get(clone.getUuid());

				//we expect that the other clone is ALWAYS cached
				if (otherClone == null)
				{
					log.error(
							"checkCacheIntegrity(): groupUuidToClonesRegistry contains a clone which is not in cloneUuidToCloneRegistry: "
									+ clone, new Throwable());
					integrityOk = false;
				}
				else if (clone != otherClone)
				{
					//we expect that clone and otherClone are the same instance
					log.error("checkCacheIntegrity(): INTERNAL ERROR - different instances for same uuid: " + clone
							+ " AND " + otherClone + " VIA " + groupUuidToClonesRegistry, new Throwable());
					integrityOk = false;
				}
			}
		}

		log.debug("checkCacheIntegrity() - result: " + integrityOk);

		return integrityOk;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IDebuggableStoreProvider#checkDataIntegrity()
	 */
	@Override
	public boolean checkDataIntegrity() throws StoreLockingException
	{
		log.trace("checkDataIntegrity()");

		if (!isWriteLockedByCurrentThread())
			throw new StoreLockingException("method requires exclusive write lock");

		if (!checkCacheIntegrity())
			return false;

		return subCheckDataIntegrity();
	}

	/**
	 * Backend implementation of the <em>checkDataIntegrity()</em> method.<br/>
	 * <em>checkCacheIntegrity()</em> has already been executed at this point.<br/>
	 * <br/>
	 * It is guaranteed that no concurrent access to the store provider takes place
	 * during the execution time of this method.
	 * 
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IDebuggableStoreProvider#checkDataIntegrity()
	 */
	protected abstract boolean subCheckDataIntegrity();

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IDebuggableStoreProvider#getCacheStats()
	 */
	@Override
	public String getCacheStats()
	{
		StringBuilder stat = new StringBuilder();

		stat.append("STATS - maps: ");

		stat.append(pathToFileUuidRegistry.size());
		stat.append(" path->f, ");

		stat.append(fileUuidToFileRegistry.size());
		stat.append(" fuuid->f, ");

		stat.append(cloneUuidToCloneRegistry.size());
		stat.append(" cuuid->c, ");

		stat.append(groupUuidToGroupRegistry.size());
		stat.append(" guuid->g, ");

		stat.append(fileUuidToClonesRegistry.size());
		stat.append(" fuuid->cS, ");

		stat.append(groupUuidToClonesRegistry.size());
		stat.append(" guuid->cS");

		stat.append(" - lock: ");
		stat.append(lock.getQueueLength());
		stat.append(" queued");

		return stat.toString();
	}

	/**
	 * Takes a list of recently added/updated clones and checks whether they might have
	 * introduced any inconsistencies into the caching structures.<br/>
	 * <br/>
	 * This method is meant for debugging purposes only.
	 * 
	 * TODO: this method might become a good place to check for merging candidates
	 * 		 it would then no longer be a debug only method.
	 * 		 see also: Clone.compareTo() comments
	 * 
	 * @param list list of recently added/updated clones, never null
	 */
	protected void _checkIntegrityAfterChange(List<IClone> clones)
	{
		if (log.isTraceEnabled())
			log.trace("_checkIntegrityAfterChange(): " + clones);
		assert (clones != null);

		//this check is potentially expensive, only do it if we're in debug checking mode
		if (!CPCCorePlugin.isDebugChecking())
			return;

		for (IClone clone : clones)
		{
			//make sure that there are no other clones in the file with the same start and end offset
			SortedSet<IClone> clonesInFile = fileUuidToClonesRegistry.get(clone.getFileUuid());

			if (clonesInFile == null)
			{
				//we're looking at a list of clones which were recently added or updated, all clone
				//data for the corresponding files should be cached at this point!
				log.error("_checkIntegrityAfterChange(): file is not cached for clone - " + clone, new Throwable());
				continue;
			}

			//we want all clones in the set which start at the exact same offset
			//create a new temp clone for lookup purposes only
			IClone tmpClone = (IClone) cloneFactoryProvider.getInstance(IClone.class, "TEMP");
			tmpClone.setOffset(clone.getOffset() + 1); //+1 because subSet() is exclusive
			tmpClone.setLength(clone.getLength());
			//new Clone(clone.getPosition().getStartOffset() + 1, clone.getPosition().getEndOffset());

			SortedSet<IClone> clonesInRange = clonesInFile.subSet(clone, tmpClone);

			if (clonesInRange == null || clonesInRange.isEmpty())
			{
				//we should always find the clone itself in the list
				log.error("_checkIntegrityAfterChange(): clone was not found in clone list for file - " + clone,
						new Throwable());
				continue;
			}

			for (IClone otherClone : clonesInRange)
			{
				if (clone.equals(otherClone))
					continue;

				//ok, this is a different clone and it starts at the same offset, it should
				//therefore end at a different offset
				if (clone.getEndOffset() == otherClone.getEndOffset())
				{
					//we have two distinct clones with the same start and end points
					//this can happen in certain cases, see also clone compareTo() 
					log.debug("_checkIntegrityAfterChange(): two different clones with equal position found - " + clone
							+ ", " + otherClone);
				}
			}
		}
	}

	/**
	 * Adds a given clone to the given data store map. The file uuid is used as key.<br/>
	 * Semantics differ depending on the data store used, see below.<br/>
	 * <br/>
	 * Data stores:
	 * <ul>
	 * 	<li>transactionAddedClones - no special actions</li>
	 * 	<li>transactionMovedClones - no special actions</li>
	 *  <li>transactionModifiedClones - no special actions</li>
	 *  <li>transactionRemovedClones - removes the given clone from all other data stores.
	 *  	If it is listed in the transactionAddedClones datastore too, it won't be added to
	 *  	the transactionRemovedClones datastore at all. (It will seems as if this clone
	 *  	never existed)</li>
	 * </ul>
	 */
	protected void rememberCloneModificationInTransaction(Map<String, List<String>> dataStore, IClone clone)
	{
		assert (dataStore != null && clone != null);

		List<String> clones = dataStore.get(clone.getFileUuid());
		if (clones == null)
		{
			clones = new LinkedList<String>();
			dataStore.put(clone.getFileUuid(), clones);
		}

		// Make sure that we don't add the same clone multiple times.
		// Instead, we only want to keep the latest addition.
		// So we just try to remove the clone first.
		clones.remove(clone.getUuid());

		/*
		 * There are some special semantics in case the data store is the removed clones
		 * data store.
		 */
		if (dataStore == transactionRemovedClones)
		{
			/*
			 * We don't want to list a removed clone in any of the other lists.
			 */
			log
					.trace("rememberCloneModificationInTransaction() - clone is being added to the removed clones store, clearing it from modified, moved and added stores.");

			//remove it from the moved list, if it exist
			List<String> movedClones = transactionMovedClones.get(clone.getFileUuid());
			if (movedClones != null)
				movedClones.remove(clone.getUuid());

			//remove it from the modified list, if it exist
			List<String> modifiedClones = transactionModifiedClones.get(clone.getFileUuid());
			if (modifiedClones != null)
				modifiedClones.remove(clone.getUuid());

			//remove it from the added list, if it exists
			boolean wasInAdded = false;
			List<String> addedClones = transactionAddedClones.get(clone.getFileUuid());
			if (addedClones != null)
				wasInAdded = addedClones.remove(clone.getUuid());

			if (wasInAdded)
			{
				/*
				 * The clone was added and removed within this transaction. We don't want to list it
				 * in any clone modification event. By returning here, we also never add it to
				 * the removed list.
				 * In effect the clone never existed, from the view of the final clone
				 * modification events.
				 */
				log
						.trace("rememberCloneModificationInTransaction() - clone was also in the added store, forgetting everything about the clone.");
				return;
			}

			//keep a copy of this clone for use at end of transaction
			transactionDeletedCloneDataBuffer.put(clone.getUuid(), clone);
		}
		/*
		 * If we're in debug mode, make sure that a modified clone contains clonediff data.
		 */
		else if (CPCCorePlugin.isDebugChecking() && dataStore == transactionModifiedClones)
		{
			ICloneModificationHistoryExtension history = (ICloneModificationHistoryExtension) clone
					.getExtension(ICloneModificationHistoryExtension.class);
			if (history == null || history.getCloneDiffs().isEmpty())
			{
				log.warn(
						"rememberCloneModificationInTransaction() - modified clone has no or empty history extension - clone: "
								+ clone + ", history: " + history, new Throwable());
			}
		}

		// Now add the new clone.
		clones.add(clone.getUuid());
	}

	/**
	 * Takes an {@link ICloneObject} and removes all {@link ICloneObjectExtension}s from it which are
	 * non-stateful. That is, they do not implement {@link ICloneObjectExtensionStatefulObject}.<br/>
	 * <br/>
	 * This is typically done whenever an {@link ICloneObject} is passwd to the store provider, i.e.
	 * in {@link IStoreProvider#addClone(IClone)}.
	 * 
	 * @param cloneObject the clone object to strip all non-static extensions from, never null.
	 */
	protected void stripNonStatefulExtensions(ICloneObject cloneObject)
	{
		assert (cloneObject != null);

		if (!cloneObject.hasExtensions())
			return;

		for (ICloneObjectExtension extension : cloneObject.getExtensions())
		{
			if (extension instanceof ICloneObjectExtensionStatefulObject)
				//keep it
				continue;

			if (log.isTraceEnabled())
				log.trace("stripNonStatefulExtensions() - removing non-stateful extension: " + extension);

			//all else is a non-stateful extension and need to be dropped.
			cloneObject.removeExtension(extension);
		}
	}

	/**
	 * Takes an {@link ICloneObject} and clears out all sub-element data from its registered
	 * {@link ICloneObjectExtensionLazyMultiStatefulObject} extensions.<br/>
	 * <br/>
	 * If <em>keepHistoryIfNewlyModified</em> is <em>true</em> this method will not purge out
	 * the sub-elements of an {@link ICloneModificationHistoryExtension} if it contains
	 * {@link CloneDiff} elements which were added within this transaction.<br/>
	 * <br/>
	 * This method is typically called when a file was just persisted.
	 * 
	 * @param cloneObject the clone object to clear out lazy multi stateful clone object extension sub-elements for, never null.
	 * @param keepHistoryIfNewlyModified <em>true</em> if newly modified history extensions should be kept, <em>false</em> otherwise.
	 */
	protected void clearLazyMultiStatefulExtensions(ICloneObject cloneObject, boolean keepHistoryIfNewlyModified)
	{
		assert (cloneObject != null);

		if (!cloneObject.hasExtensions())
			return;

		for (ICloneObjectExtension extension : cloneObject.getExtensions())
		{
			if (!(extension instanceof ICloneObjectExtensionLazyMultiStatefulObject))
				//ignore it
				continue;

			/*
			 * Check whether this is an ICloneModificationHistoryExtension with pending CloneDiff entries
			 * for the current transaction.
			 * If it is, don't purge the sub-elements. They are still needed until this transaction terminates.
			 * There is also no need to explicitly purge them once the transaction was terminated. They will
			 * be purged next time the file is persisted and no new diff data is pending. 
			 */
			if (keepHistoryIfNewlyModified && (extension instanceof ICloneModificationHistoryExtension))
			{
				ICloneModificationHistoryExtension history = (ICloneModificationHistoryExtension) extension;
				if (!history.getCloneDiffsForTransaction().isEmpty())
				{
					if (log.isTraceEnabled())
						log
								.trace("clearLazyMultiStatefulExtensions() - history extension contains new diff entries, not purging out sub-elements - history: "
										+ history);
					continue;

					//OLD:
					//					if (log.isTraceEnabled())
					//						log
					//								.trace("clearLazyMultiStatefulExtensions() - history extension contains new diff entries, storing copy in transaction buffer - history: "
					//										+ history);
					//
					//					try
					//					{
					//						transactionPersistedModificationHistoryBuffer.put(cloneObject.getUuid(),
					//								(ICloneModificationHistoryExtension) history.clone());
					//					}
					//					catch (CloneNotSupportedException e)
					//					{
					//						log.error(
					//								"clearLazyMultiStatefulExtensions() - unable to clone history extension - cloneObject: "
					//										+ cloneObject + ", history: " + history + " - " + e, e);
					//					}
				}
			}

			if (log.isTraceEnabled())
				log.trace("clearLazyMultiStatefulExtensions() - clearing lazy multi extension sub-elements for: "
						+ extension);

			//tell the extension to remove all sub-element data.
			((ICloneObjectExtensionLazyMultiStatefulObject) extension).setMultiState(null);

			//and mark it as partial
			((ICloneObjectExtensionLazyMultiStatefulObject) extension).setPartial(true);
		}
	}

	/**
	 * Takes an {@link ICloneObject} instance and returns its cached instance
	 * (same type & same UUID), if any exists.
	 * 
	 * @param cloneObject the {@link ICloneObject} to lookup, never null.
	 * @return cached version of the {@link ICloneObject} if no cached version exists, may be NULL.
	 */
	protected ICloneObject lookupCachedCloneObject(ICloneObject cloneObject)
	{
		assert (cloneObject != null);

		if (cloneObject instanceof IClone)
			return cloneUuidToCloneRegistry.get(cloneObject.getUuid());
		else if (cloneObject instanceof ICloneGroup)
			return groupUuidToGroupRegistry.get(cloneObject.getUuid());
		else
			log.warn("_lookupCachedCloneObject() - unknown clone object type: " + cloneObject);

		return null;
	}

	/**
	 * Takes a list of {@link IClone}s and checks whether they contain an {@link ICloneModificationHistoryExtension}.<br/>
	 * If they do, the end of the current transaction is indicated to the extension via a call to
	 * {@link ICloneModificationHistoryExtension#endOfTransaction()}. 
	 *  
	 * @param clones a list of {@link IClone}s, never null.
	 */

	/**
	 * Takes a transaction data store and a clone file UUID and returns a list of <b>cloned and sealed</b>
	 * {@link IClone} objects for the uuid data in the data store.<br/>
	 * <br/>
	 * Furthermore, if <em>endTransaction</em> is true, the internal copies of the {@link IClone} objects will
	 * be checked for the presence of an {@link ICloneModificationHistoryExtension}.
	 * If one is present, the end of the current transaction is indicated to the extension via a call to
	 * {@link IStoreCloneModificationHistoryExtension#endOfTransaction()}. 
	 * 
	 * @param dataStore a transaction data store, i.e. <em>transactionAddedClones</em>, never null.
	 * @param fileUuid the clone file UUID for the current file, never null.
	 * @param endTransaction true if the any clone modification history extension should be notified about the end of transaction, false otherwise.
	 * 		This is typically set to true for clone modification lists and to false for add, move and delete lists.
	 * @return a list of cloned and sealed clones from the given data store, never null.
	 */
	protected List<IClone> buildCloneChangeList(Map<String, List<String>> dataStore, String fileUuid,
			boolean endTransaction)
	{
		//get a list of affected clone UUIDs
		List<String> cloneUuids = dataStore.get(fileUuid);

		if (cloneUuids == null || cloneUuids.isEmpty())
			//return empty list if there are no clone UUIDs for this file in the data store
			return EMPTY_CLONE_LIST;

		List<IClone> result = new ArrayList<IClone>(cloneUuids.size());

		//process each clone uuid
		for (String cloneUuid : cloneUuids)
		{
			IClone clone = cloneUuidToCloneRegistry.get(cloneUuid);
			if (clone == null)
			{
				//check if it was deleted
				clone = transactionDeletedCloneDataBuffer.get(cloneUuid);
				if (clone == null)
				{
					//this shouldn't happen.
					log.error("buildCloneModificationList() - unable to find clone for clone uuid: " + cloneUuid,
							new Throwable());
					continue;
				}
			}

			/*
			 * We now have the latest version of the clone object.
			 * We now:
			 * a) create a cloned and sealed copy
			 * b) update the end of transaction mark of the internal copy (not the cloned one!)
			 */

			//cloned copy
			IClone newClone;
			try
			{
				newClone = (IClone) clone.clone();
			}
			catch (CloneNotSupportedException e)
			{
				log.error("buildCloneModificationList() - unable to clone clone - " + clone + " - " + e, e);
				continue;
			}

			//seal it, we don't want any event recipients to modify shared clone copies
			newClone.seal();

			//add it to the result list
			result.add(newClone);

			if (endTransaction)
			{
				//update end of transaction mark of the internal copy
				IStoreCloneModificationHistoryExtension history = (IStoreCloneModificationHistoryExtension) clone
						.getExtension(ICloneModificationHistoryExtension.class);

				if (history == null || history.getCloneDiffsForTransaction().isEmpty())
				{
					log.debug(
							"buildCloneChangeList() - modified clone contains no edit history for this transaction - clone: "
									+ clone + ", history: " + history, new Throwable());
				}

				if (history != null)
				{
					history.endOfTransaction();
				}

			}
		}

		return result;
	}

	/**
	 * Takes a clone instance which was just passed to the store provider (update) and its old
	 * copy from the internal cache and copies over the end of transaction mark data of the
	 * clone modification history extension, if needed. 
	 * 
	 * @param newClone the new clone instance, never null.
	 * @param oldClone the old clone instance from internal cache, never null.
	 */
	protected void checkCloneDiffTransactionMarker(IClone newClone, IClone oldClone)
	{
		if (log.isTraceEnabled())
			log.trace("checkCloneDiffTransactionMarker() - newClone: " + newClone + ", oldClone: " + oldClone);

		//get modification history of old clone
		IStoreCloneModificationHistoryExtension oldHistory = (IStoreCloneModificationHistoryExtension) oldClone
				.getExtension(ICloneModificationHistoryExtension.class);
		if (oldHistory == null)
		{
			//cached version has no history, nothing to do
			log.trace("checkCloneDiffTransactionMarker() - cached clone has no history, skipping.");
			return;
		}

		//get modification history of new clone
		IStoreCloneModificationHistoryExtension newHistory = (IStoreCloneModificationHistoryExtension) newClone
				.getExtension(ICloneModificationHistoryExtension.class);
		if (newHistory == null)
		{
			/*
			 * The new entry does not yet have a clone history extension.
			 * We simply copy over the cached version.
			 */
			log.trace("checkCloneDiffTransactionMarker() - new clone has no history, reusing cached history.");

			//TODO: do we need to clone the old history object here?
			newClone.addExtension(oldHistory);
			return;
		}

		/*
		 * Due to the way the history extension is used, the following should hold:
		 * a) The new history should always have the same number or more clone diff entries.
		 * 		Diffs can't be removed and diffs are never added internally by the store provider.
		 * 		Furthermore, any action which could "loose" diffs would be an incorrect usage of the
		 * 		store provider API.
		 * 		HOWEVER:
		 * 			A diff history may be completely cleared and recreated, i.e. by the CPC Optimiser.
		 * 			This case needs to be handled separately.
		 * b) The old history should always have the most up2date endOfTransaction date.
		 */

		//assert a)
		if (!newHistory.wasCleared() && newHistory.getCloneDiffs().size() < oldHistory.getCloneDiffs().size())
		{
			log
					.error(
							"checkCloneDiffTransactionMarker() - new history has less diffs than cached history - possible IStoreProvider API violation - new history: "
									+ newHistory + ", cached history: " + oldHistory + ", clone: " + newClone,
							new Throwable());
			return;
		}

		//assert b)
		if (oldHistory.getEndOfTransactionCloneDiffCreationDate() != null
				&& newHistory.getEndOfTransactionCloneDiffCreationDate() != null)
		{
			if (newHistory.getEndOfTransactionCloneDiffCreationDate().after(
					oldHistory.getEndOfTransactionCloneDiffCreationDate()))
			{
				log
						.error(
								"checkCloneDiffTransactionMarker() - new history has younger endOfTransaction date than cached cached history - possible IStoreProvider API violation - new history: "
										+ newHistory + ", cached history: " + oldHistory + ", clone: " + newClone,
								new Throwable());
				return;
			}
		}
		else if (oldHistory.getEndOfTransactionCloneDiffCreationDate() == null
				&& newHistory.getEndOfTransactionCloneDiffCreationDate() != null)
		{
			log
					.error(
							"checkCloneDiffTransactionMarker() - new history has endOfTransaction date, cached cached history does not - possible IStoreProvider API violation - new history: "
									+ newHistory + ", cached history: " + oldHistory + ", clone: " + newClone,
							new Throwable());
			return;
		}

		/*
		 * Now copy over the end of transaction date from the cached history to the new history.
		 */
		newHistory.setEndOfTransactionCloneDiffCreationDate(oldHistory.getEndOfTransactionCloneDiffCreationDate());

		if (log.isTraceEnabled())
			log.trace("checkCloneDiffTransactionMarker() - updated new history: " + newHistory);
	}

	/*
	 * IProvider methods.
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#onLoad()
	 */
	@Override
	public void onLoad()
	{
		//currently nothing to do here		
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#onUnload()
	 */
	@Override
	public void onUnload()
	{
		//currently nothing to do here		
	}

}
