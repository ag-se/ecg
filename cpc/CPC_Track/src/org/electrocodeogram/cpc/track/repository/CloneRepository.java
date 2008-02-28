package org.electrocodeogram.cpc.track.repository;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.widgets.Display;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.hub.event.EclipseFileAccessEvent;
import org.electrocodeogram.cpc.core.api.provider.classification.IClassificationProvider;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.api.provider.similarity.ISimilarityProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IDebuggableStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProviderWriteLockHook;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.UpdateMode;
import org.electrocodeogram.cpc.core.api.provider.track.CPCPosition;
import org.electrocodeogram.cpc.core.api.provider.track.IFuzzyPositionToCloneMatchingProvider;
import org.electrocodeogram.cpc.core.api.provider.track.IPositionUpdateStrategyProvider;
import org.electrocodeogram.cpc.core.utils.CoreConfigurationUtils;
import org.electrocodeogram.cpc.core.utils.CoreEditorUtils;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.track.codediff.CPCPositionUpdater;
import org.electrocodeogram.cpc.track.listener.FileAccessListener;
import org.electrocodeogram.cpc.track.listener.PersistenceListener;
import org.electrocodeogram.cpc.track.utils.TrackUtils;


public class CloneRepository implements IStoreProviderWriteLockHook
{
	private static Log log = LogFactory.getLog(CloneRepository.class);

	/**
	 * Time of inactivity in milliseconds after which dirty position data in documents
	 * should be transmitted to the {@link IStoreProvider}.<br/>
	 * If this value is too large, CPC UI views will "lag".
	 */
	private static final long CLONE_POSITION_SYNCHRONISATION_TIMEOUT = 500;

	/**
	 * Delay in milliseconds between checks of the registered documents for dirty documents
	 * which exceeded the {@link CloneRepository#CLONE_POSITION_SYNCHRONISATION_TIMEOUT}.<br/>
	 */
	private static final long CLONE_POSITION_CHECK_INTERVALL = 250;

	private IStoreProvider storeProvider;
	private ICloneFactoryProvider cloneFactoryProvider;
	private IClassificationProvider classificationProvider;
	private IPositionUpdateStrategyProvider positionUpdateStrategyProvider;
	private ISimilarityProvider similiarityProvider;
	private IFuzzyPositionToCloneMatchingProvider fuzzyPositionToCloneMatchingProvider;

	private DocumentEvent lastDocumentEvent;

	/**
	 * Central registry of {@link DocumentDescriptor}s for all currently open {@link IDocument}s.<br/>
	 * <br/>
	 * <b>IMPORTANT:</b> To prevent concurrency problems all read and write access to this registry
	 * 		needs to be done in a <b>synchronized block</b> with <em>documentRegistry</em> as mutex.<br/>
	 * 		However, it is <u>crucial</u> that no {@link IStoreProvider} exclusive write lock is
	 * 		obtained <u>within</u> such a synchronzed block. Doing so <b>will result in deadlocks</b>.<br/>
	 * 		If you need an {@link IStoreProvider} exclusive write lock, acquire it outside of the
	 * 		<em>documentRegistry</em> synchronized block.<br/>
	 * 		The same holds true for locks on a synchronised {@link IDocument} instance. Such locks
	 * 		need to be obtained before obtaining the lock on <em>documentRegistry</em>.
	 */
	private Map<String, DocumentDescriptor> documentRegistry;

	private Timer dataSynchronisationTimer;

	public CloneRepository(IStoreProvider storeProvider, ICloneFactoryProvider cloneFactoryProvider,
			IClassificationProvider classificationProvider,
			IPositionUpdateStrategyProvider positionUpdateStrategyProvider, ISimilarityProvider similiarityProvider,
			IFuzzyPositionToCloneMatchingProvider fuzzyPositionToCloneMatchingProvider)
	{
		if (log.isTraceEnabled())
			log.trace("CloneRepository() - storeProvider: " + storeProvider + ", cloneFactoryProvider: "
					+ cloneFactoryProvider + ", classificationProvider: " + classificationProvider
					+ ", similiarityProvider: " + similiarityProvider + ", fuzzyPositionToCloneMatchingProvider: "
					+ fuzzyPositionToCloneMatchingProvider);

		if (storeProvider == null || cloneFactoryProvider == null || classificationProvider == null
				|| positionUpdateStrategyProvider == null || similiarityProvider == null
				|| fuzzyPositionToCloneMatchingProvider == null)
		{
			log.fatal("CloneRepository() - some providers are not available - storeProvider: " + storeProvider
					+ ", cloneFactoryProvider: " + cloneFactoryProvider + ", classificationProvider: "
					+ classificationProvider + ", positionUpdateStrategyProvider: " + positionUpdateStrategyProvider
					+ ", similiarityProvider: " + similiarityProvider + ", fuzzyPositionToCloneMatchingProvider: "
					+ fuzzyPositionToCloneMatchingProvider, new Throwable());
			return;
		}

		this.storeProvider = storeProvider;
		this.cloneFactoryProvider = cloneFactoryProvider;
		this.classificationProvider = classificationProvider;
		this.positionUpdateStrategyProvider = positionUpdateStrategyProvider;
		this.similiarityProvider = similiarityProvider;
		this.fuzzyPositionToCloneMatchingProvider = fuzzyPositionToCloneMatchingProvider;

		this.documentRegistry = new Hashtable<String, DocumentDescriptor>(20);

		//register write lock hook with store provider
		this.storeProvider.setWriteLockHook(this);

		//create a timer and background job which will synchronise the clone data
		//for all documents which have been idle for more than CLONE_POSITION_SYNCHRONISATION_TIMEOUT ms
		dataSynchronisationTimer = new Timer();
		dataSynchronisationTimer.schedule(new DocumentRegistrySynchronisationTimerTask(),
				CLONE_POSITION_CHECK_INTERVALL, CLONE_POSITION_CHECK_INTERVALL);
	}

	public ICloneFactoryProvider getCloneFactory()
	{
		return cloneFactoryProvider;
	}

	public IStoreProvider getStoreProvider()
	{
		return storeProvider;
	}

	public IClassificationProvider getClassificationProvider()
	{
		return classificationProvider;
	}

	public IPositionUpdateStrategyProvider getPositionUpdateStrategyProvider()
	{
		return positionUpdateStrategyProvider;
	}

	public ISimilarityProvider getSimiliarityProvider()
	{
		return similiarityProvider;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IStoreProviderWriteLockHook#aboutToGrantWriteLock()
	 */
	@Override
	public void aboutToGrantWriteLock() throws StoreLockingException
	{
		log.trace("aboutToGrantWriteLock()");

		//make sure we don't run into any concurrency issues

		/*
		 * We have a locking problem here.
		 * It would seems sensible to acquire a lock on the documentRegistry first
		 * and to then iterate over all its elements and to process them one by one.
		 * However, doing so might lead to a deadlock situation.
		 * 
		 * To avoid deadlocks it is crucial that we always acquire locks in the same
		 * order everywhere. For the methods used here we need (at the time of this writing)
		 * 3 locks.
		 * 	1) an exclusive store provider write lock
		 * 	2) a lock on the document being modified
		 * 	3) a lock on the document registry
		 * And we need the locks in exactly that order.
		 * We already hold the 1) lock at this point.
		 * 
		 * The fact that 2) needs to be acquired before 3) may seem unintuitive. This is
		 * enforced by the IDocument lock handling during the execution of the CPC
		 * IPositionUpdater. At that point the document lock is acquired outside of our
		 * control. We thus need to follow this locking order everywhere else too.
		 * 
		 * Thus we can't just lock the document registry here (3), we need to lock each
		 * document first. We therefore make use of the synchronisation of the Hashtable
		 * which underlies the documentRegistry and knowingly accept the possibility that
		 * elements might be concurrently added or removed after our call to
		 * documentRegistry.values().
		 * The question is whether any application failure could possibly result from this.
		 * 
		 * TODO: reconsider implications of not acquiring documentRegistry lock for loop.
		 */

		//		log.trace("aboutToGrantWriteLock() - acquiring documentRegistry lock");
		//		synchronized (documentRegistry)
		//		{
		//			log.trace("aboutToGrantWriteLock() - got documentRegistry lock");
		//make sure data for all currently open documents is transfered to the store provider if needed
		for (DocumentDescriptor docDesc : documentRegistry.values())
		{
			log.trace("aboutToGrantWriteLock() - acquiring document lock");
			synchronized (TrackUtils.getDocumentLockObject(docDesc))
			{
				log.trace("aboutToGrantWriteLock() - got document lock");

				log.trace("aboutToGrantWriteLock() - acquiring documentRegistry lock");
				synchronized (documentRegistry)
				{
					log.trace("aboutToGrantWriteLock() - got documentRegistry lock");
					//all documents which are not already stale will need to be marked stale
					//and if they were dirty the changes also need to be written back
					if (!docDesc.isStale())
					{
						if (docDesc.isDirty())
						{
							if (log.isTraceEnabled())
								log.trace("aboutToGrantWriteLock() - syncing dirty document - " + docDesc);

							//the positions for this document were modified, we need to write them back
							//before the lock requester regains control.
							storePositionData(docDesc, true);
						}

						//storePositionData might already have set stale to true
						if (!docDesc.isStale())
						{
							//all cached documents will be potentially out of date once the lock requester
							//does its writes to the store provider
							if (log.isTraceEnabled())
								log.trace("aboutToGrantWriteLock() - marking document as stale - " + docDesc);

							docDesc.setStale(true);
						}
					}
					else
					{
						//stale
						if (docDesc.isDirty())
							log.error("aboutToGrantWriteLock() - stale but dirty document - " + docDesc,
									new Throwable());
					}
					log.trace("aboutToGrantWriteLock() - releasing documentRegistry lock");
				}
				log.trace("aboutToGrantWriteLock() - releasing document lock");
			}
		}

		//			log.trace("aboutToGrantWriteLock() - releasing documentRegistry lock");
		//		}

		log.trace("aboutToGrantWriteLock() - done");
	}

	/**
	 * Checks whether the given document was already initialised.<br/>
	 * The call will be delegated to {@link CloneRepository#documentInit(String, String, IDocument)} if the document
	 * hasn't been initialised yet.<br/>
	 * <br/>
	 * This is needed in certain situations where no initial buffer creation event
	 * was sent. I.e. in certain out-of-sync cases of workspace and filesystem.
	 * 
	 * @param project name of the project, never null.
	 * @param filePath project relative path of the file, never null.
	 * @param document the document object for the file, never null.
	 * 
	 * @see PersistenceListener
	 */
	public void ensureDocumentInit(String project, String filePath, IDocument document)
	{
		if (log.isTraceEnabled())
			log.trace("ensureDocumentInit() - project: " + project + ", filePath: " + filePath + ", document: "
					+ document);

		//make sure that this is a java file, we're not interested in any other file types
		if (!CoreConfigurationUtils.isSupportedFile(filePath))
		{
			log.trace("ensureDocumentInit() - ignoring unsupported source file type");
			return;
		}

		/*
		 * We'll need to get a lot of locks here x_X
		 * Even though we don't use them directly, documentInit() will need them,
		 * if we decide to call it.
		 */
		try
		{
			storeProvider.acquireWriteLock(LockMode.NO_WRITE_LOCK_HOOK_NOTIFY);

			//make sure we don't run into any concurrency issues
			log.trace("ensureDocumentInit() - acquiring document lock");
			synchronized (CoreEditorUtils.getDocumentLockObject(document))
			{
				log.trace("ensureDocumentInit() - got document lock");

				log.trace("ensureDocumentInit() - acquiring documentRegistry lock");
				synchronized (documentRegistry)
				{
					log.trace("ensureDocumentInit() - got documentRegistry lock");

					/*
					 * Now we can finally check whether all this effort was worth it :P
					 * 
					 */
					if (!documentRegistry.containsKey(project + "/" + filePath))
					{
						//ok, this document wasn't initialised yet, do that now!
						if (log.isDebugEnabled())
							log
									.debug("ensureDocumentInit() - document was not yet initialised, calling documentInit() - project: "
											+ project + ", filePath: " + filePath);

						documentInit(project, filePath, document);
					}

					log.trace("ensureDocumentInit() - releasing documentRegistry lock");
				}
				log.trace("ensureDocumentInit() - releasing document lock");
			}
		}
		catch (StoreLockingException e)
		{
			log.error("ensureDocumentInit() - locking error - " + e, e);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}
	}

	/**
	 * Called whenever a text file is opened.<br/>
	 * Not limited to files opened in an editor, automated background edits are also covered.
	 * 
	 * @param project name of the project, never null.
	 * @param filePath project relative path of the file, never null.
	 * @param document the document object for the file, never null.
	 * 
	 * @see FileAccessListener
	 * @see EclipseFileAccessEvent
	 */
	public void documentInit(String project, String filePath, IDocument document)
	{
		if (log.isTraceEnabled())
			log.trace("documentInit() - project: " + project + ", filePath: " + filePath + ", document: " + document);

		//make sure that this is a java file, we're not interested in any other file types
		if (!CoreConfigurationUtils.isSupportedFile(filePath))
		{
			log.trace("documentInit() - ignoring unsupported source file type");
			return;
		}

		try
		{
			storeProvider.acquireWriteLock(LockMode.NO_WRITE_LOCK_HOOK_NOTIFY);

			//make sure we don't run into any concurrency issues
			log.trace("documentInit() - acquiring document lock");
			synchronized (CoreEditorUtils.getDocumentLockObject(document))
			{
				log.trace("documentInit() - got document lock");

				log.trace("documentInit() - acquiring documentRegistry lock");
				synchronized (documentRegistry)
				{
					log.trace("documentInit() - got documentRegistry lock");

					//get a clone file instance for the file
					ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(project, filePath, true, false);
					if (cloneFile == null)
					{
						log.fatal("documentInit() - unable to obtain clone file for file - project: " + project
								+ ", filePath: " + filePath, new Throwable());
						log.trace("documentInit() - releasing document & documentRegistry lock");
						return;
					}

					DocumentDescriptor docDesc = new DocumentDescriptor(cloneFile, document);

					//add new CPC position category
					document.addPositionCategory(CPCPosition.CPC_POSITION_CATEGORY);

					//create cpc position updater
					CPCPositionUpdater positionUpdater = new CPCPositionUpdater(docDesc);
					//remember it
					docDesc.setCpcPositionUpdater(positionUpdater);

					//add a position updater for the CPC position category
					document.addPositionUpdater(positionUpdater);

					//also add it as a document change listener, in order for it to be able to
					//refresh stale position data _BEFORE_ the document is modified
					document.addDocumentListener(positionUpdater);

					//load clone position data
					loadPositionData(docDesc);

					//register document
					documentRegistry.put(project + "/" + filePath, docDesc);

					//hook a listener into the undo manager for this document
					IDocumentUndoManager undoManager = DocumentUndoManagerRegistry.getDocumentUndoManager(document);
					if (undoManager != null)
					{
						if (log.isTraceEnabled())
							log.trace("documentInit() - got document undo manager, registering undo listener: "
									+ undoManager);

						undoManager.addDocumentUndoListener(positionUpdater);
						//undoManager.connect(positionUpdater);
					}

					log.trace("documentInit() - releasing documentRegistry lock");
				}
				log.trace("documentInit() - releasing document lock");
			}
		}
		catch (StoreLockingException e)
		{
			log.error("loadPositionData() - locking error - " + e, e);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}

	}

	/**
	 * Called whenever a text file is closed.<br/>
	 * Not limited to files opened in an editor, automated background edits are also covered.
	 * 
	 * @param project name of the project, never null.
	 * @param filePath project relative path of the file, never null.
	 * @param document the document object for the file, never null.
	 * @param bufferDirty whether the underlying file buffer was dirty, when the file was closed.
	 * 
	 * @see FileAccessListener
	 * @see EclipseFileAccessEvent
	 */
	public void documentShutdown(String project, String filePath, IDocument document, boolean bufferDirty)
	{
		if (log.isTraceEnabled())
			log.trace("documentShutdown() - project: " + project + ", filePath: " + filePath + ", document: "
					+ document + ", bufferDirty: " + bufferDirty);

		//make sure that this is a java file, we're not interested in any other file types
		if (!CoreConfigurationUtils.isSupportedFile(filePath))
		{
			log.trace("documentShutdown() - ignoring unsupported source file type");
			return;
		}

		try
		{
			storeProvider.acquireWriteLock(LockMode.NO_WRITE_LOCK_HOOK_NOTIFY);

			//make sure we don't run into any concurrency issues
			log.trace("documentShutdown() - acquiring document lock");
			synchronized (CoreEditorUtils.getDocumentLockObject(document))
			{
				log.trace("documentShutdown() - got document lock");

				log.trace("documentShutdown() - acquiring documentRegistry lock");
				synchronized (documentRegistry)
				{
					log.trace("documentShutdown() - got documentRegistry lock");

					DocumentDescriptor docDesc = documentRegistry.get(project + "/" + filePath);
					if (docDesc == null)
					{
						log
								.error("documentShutdown() - unable to obtain document descriptor for - project: "
										+ project + ", filePath:" + filePath + ", documentRegistry: "
										+ documentRegistry, new Throwable());
						log.trace("documentShutdown() - releasing document & documentRegistry lock");
						return;
					}

					//Make sure all clone data for the file transmitted back to the store provider
					//and also remove all CPCPosition entries from the document.
					if (docDesc.isDirty())
						if (!docDesc.isStale())
							storePositionData(docDesc, true);
						else
							log.error("documentShutdown() - stale but dirty document - " + docDesc, new Throwable());

					//unregister document
					documentRegistry.remove(project + "/" + filePath);

					if (!bufferDirty)
					{
						/*
						 * The underlying file buffer of this file was not dirty when it was saved.
						 * This means that any modifications which were made to the clone data, after
						 * it was last saved (i.e. by a delayed notification or by user actions within
						 * the clone views) should also be saved.
						 * This also affects files from which data was only copied.
						 * 
						 * We persist the file here to ensure that we don't loose such data.
						 */
						storeProvider.persistData(docDesc.getCloneFile());
					}
					else
					{
						/*
						 * Ok, the buffer was dirty, which means that the user did not save his
						 * modifications. In effect this equals a revert action.
						 */
						storeProvider.revertData(docDesc.getCloneFile());
					}

					//tell the store provider that this file does no longer need to be cached
					storeProvider.hintPurgeCache(docDesc.getCloneFile());

					log.trace("documentShutdown() - releasing documentRegistry lock");
				}
				log.trace("documentShutdown() - releasing document lock");
			}
		}
		catch (StoreLockingException e)
		{
			log.error("documentShutdown() - locking error - " + e, e);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}

	}

	/**
	 * Clears all {@link CPCPosition}s from the given document and creates new {@link CPCPosition}s for all
	 * clones of the given file.
	 * <p>
	 * <b>NOTE:</b> The caller of this methods needs to hold an exclusive IStoreProvider write lock.
	 * 
	 * @param docDesc the {@link DocumentDescriptor} of the document to load the clone data for, never null.
	 */
	public void loadPositionData(DocumentDescriptor docDesc) throws StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("loadPositionData() - docDesc: " + docDesc);
		assert (docDesc != null && docDesc.getDocument() != null);

		if (!storeProvider.holdingWriteLock())
			throw new StoreLockingException("Use of this method requires an exclusive store provider write lock.");

		IDocument document = docDesc.getDocument();

		if (Thread.holdsLock(documentRegistry) && CoreEditorUtils.hasDocumentLockObject(document)
				&& !Thread.holdsLock(TrackUtils.getDocumentLockObject(docDesc)))
			throw new StoreLockingException("Use of this method requires a lock on the document object.");

		//We are not going to modify the documentRegistry here, however, we are changing
		//one of its elements. Most callers of this method already acquire a lock for the
		//document registry, however, some can't (i.e. CPCPositionUpdater).
		//We therefore get a lock here again, just in case.
		log.trace("loadPositionData() - acquiring document lock");
		synchronized (CoreEditorUtils.getDocumentLockObject(document))
		{
			log.trace("loadPositionData() - got document lock");

			log.trace("loadPositionData() - acquiring documentRegistry lock");
			synchronized (documentRegistry)
			{
				log.trace("loadPositionData() - got documentRegistry lock");

				//clear all old CPC clone positions in the document
				try
				{
					Position[] positions = document.getPositions(CPCPosition.CPC_POSITION_CATEGORY);
					if (positions != null && positions.length > 0)
					{
						if (log.isTraceEnabled())
							log.trace("loadPositionData() - removing " + positions.length + " existing positions.");

						for (Position pos : positions)
						{
							document.removePosition(CPCPosition.CPC_POSITION_CATEGORY, pos);
						}
					}
				}
				catch (BadPositionCategoryException e)
				{
					log.error("loadPositionData() - error while clearing old positions for file - "
							+ docDesc.getCloneFile(), e);
					log.trace("loadPositionData() - releasing documentRegistry lock");
					return;
				}

				//get new clones
				List<IClone> clones = storeProvider.getClonesByFile(docDesc.getCloneFile().getUuid());

				//add positions for all clones
				for (IClone clone : clones)
				{
					try
					{
						document.addPosition(CPCPosition.CPC_POSITION_CATEGORY, new CPCPosition(clone));
					}
					catch (BadLocationException e)
					{
						log.info("DETAILS - clone startOffset: " + clone.getOffset() + ", endOffset: "
								+ clone.getEndOffset() + ", len: " + clone.getLength() + " - document len: "
								+ document.getLength() + ", content: " + CoreStringUtils.quoteString(document.get()));
						log.error("loadPositionData() - unable to create position for clone - " + clone + " - " + e, e);
					}
					catch (BadPositionCategoryException e)
					{
						log.error("loadPositionData() - unable to create position for clone - " + clone + " - " + e, e);
					}
				}

				//the document is now in sync with the store provider data
				docDesc.setDirty(false);
				docDesc.setStale(false);

				if (log.isTraceEnabled())
					log.trace("loadPositionData() - loaded " + clones.size() + " clone positions.");

				log.trace("loadPositionData() - releasing documentRegistry lock");
			}
			log.trace("loadPositionData() - releasing document lock");
		}
	}

	/**
	 * Reads all {@link CPCPosition}s from the given document and updates the clone data of the
	 * given file accordingly.<br/>
	 * Also notifies all interested parties about the modifications.<br/>
	 * <br/>
	 * <b>NOTE:</b> The caller of this methods needs to hold an exclusive IStoreProvider write lock.
	 *
	 * @param docDesc the {@link DocumentDescriptor} of the document for which the obtained clone data should be stored, never null.
	 * @param clearPositions if true, all {@link CPCPosition}s will be removed from the document once
	 * 		the position data has been stored. This is typically set when a modification by another
	 * 		module is imminent (see {@link CloneRepository#aboutToGrantWriteLock()}).
	 */
	protected void storePositionData(DocumentDescriptor docDesc, boolean clearPositions) throws StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("storePositionData() - docDesc: " + docDesc + ", clearPositions: " + clearPositions);
		assert (docDesc != null);

		if (!storeProvider.holdingWriteLock())
			throw new StoreLockingException("Use of this method requires an exclusive write lock.");

		IDocument document = docDesc.getDocument();

		if (Thread.holdsLock(documentRegistry) && CoreEditorUtils.hasDocumentLockObject(document)
				&& !Thread.holdsLock(TrackUtils.getDocumentLockObject(docDesc)))
			throw new StoreLockingException("Use of this method requires a lock on the document object.");

		//We are not going to modify the documentRegistry here, however, we are changing
		//one of its elements. Most callers of this method already acquire a lock for the
		//document registry, however, some can't (i.e. CPCPositionUpdater).
		//We therefore get a lock here again, just in case.
		log.trace("storePositionData() - acquiring document lock");
		synchronized (CoreEditorUtils.getDocumentLockObject(document))
		{
			log.trace("storePositionData() - got document lock");

			log.trace("storePositionData() - acquiring documentRegistry lock");
			synchronized (documentRegistry)
			{
				log.trace("storePositionData() - got documentRegistry lock");

				//make sure that the position updater has processed all queued document events
				docDesc.getCpcPositionUpdater().refreshPositionsAndProcessBufferedEvents();

				//recheck that the document is dirty and not stale
				if (!docDesc.isDirty() || docDesc.isStale())
				{
					if (log.isDebugEnabled())
						log.debug("storePositionData() - ignoring stale or non-dirty document - " + docDesc);
					log.trace("storePositionData() - releasing document & documentRegistry lock");
					return;
				}

				try
				{
					//get all CPCPositions
					Position[] positions = document.getPositions(CPCPosition.CPC_POSITION_CATEGORY);
					if (positions == null)
					{
						//this shouldn't happen
						log.error("storePositionData() - unable to obtain position data from document.",
								new Throwable());
						log.trace("storePositionData() - releasing document & documentRegistry lock");
						return;
					}

					if (log.isTraceEnabled())
						log.trace("storePositionData() - going to store " + positions.length + " positions.");

					//build a list of updated and removed clones for the document
					List<IClone> movedClones = new LinkedList<IClone>();
					List<IClone> modifiedClones = new LinkedList<IClone>();
					List<IClone> removedClones = new LinkedList<IClone>();

					positionUpdateStrategyProvider.extractCloneData(positions, movedClones, modifiedClones,
							removedClones, document);

					//check if we actually need to update anything, maybe this diff didn't lead to any changes
					if ((!movedClones.isEmpty()) || (!modifiedClones.isEmpty()) || (!removedClones.isEmpty()))
					{
						//now update the stored clone data
						log.trace("storePositionData() - updating stored clone data.");
						if (!removedClones.isEmpty())
							storeProvider.removeClones(removedClones);
						if (!movedClones.isEmpty())
							storeProvider.updateClones(movedClones, UpdateMode.MOVED);
						if (!modifiedClones.isEmpty())
							storeProvider.updateClones(modifiedClones, UpdateMode.MODIFIED);
					}
					else
					{
						log
								.trace("storePositionData() - no changes were made - NOT updating stored data or sending out notifications");
					}

					//check if we should delete all CPCPositions from the document
					if (clearPositions)
					{
						log.trace("storePositionData() - clearing positions from document.");

						//clear all old CPC clone positions in the document
						try
						{
							for (Position pos : document.getPositions(CPCPosition.CPC_POSITION_CATEGORY))
							{
								document.removePosition(CPCPosition.CPC_POSITION_CATEGORY, pos);
							}
						}
						catch (BadPositionCategoryException e)
						{
							log.error("storePositionData() - error while clearing old positions for file - "
									+ docDesc.getCloneFile(), e);
							log.trace("storePositionData() - releasing document & documentRegistry lock");
							return;
						}

						//the document is stale if we drop all position data
						if (!docDesc.isStale())
							docDesc.setStale(true);
					}
					else
					{
						//we kept the position data, the document is in sync now
						if (docDesc.isStale())
							docDesc.setStale(false);
					}

					//the document data is no longer dirty now
					if (docDesc.isDirty())
						docDesc.setDirty(false);
				}
				catch (BadPositionCategoryException e)
				{
					log.fatal("storePositionData() - failed to obtain CPCPositions for document - "
							+ docDesc.getCloneFile() + " - " + e, e);
				}

				log.trace("storePositionData() - releasing documentRegistry lock");
			}
			log.trace("storePositionData() - releasing document lock");
		}
	}

	/**
	 * Checks the internal clone data repository for a clone at the given
	 * position in the given file.
	 * 
	 * @param file CloneFile in question. Never null.
	 * @param offset the start offset of the area in question.
	 * @param length the length of the area in question.
	 * @param fileContent the current content of the file, never null.
	 * @return a matching {@link IClone} instance or NULL if no such instance was found.
	 */
	public IClone findClone(ICloneFile file, int offset, int length, String fileContent)
	{
		if (log.isTraceEnabled())
			log.trace("findClone() - file: " + file + ", offset: " + offset + ", length: " + length + ", fileContent: "
					+ CoreStringUtils.truncateString(fileContent));
		assert (file != null && offset >= 0 && length >= 1 && fileContent != null);

		//first get all clones from the store provider
		List<IClone> clones = storeProvider.getClonesByFile(file.getUuid());

		//then delegate the decision to the fuzzy matcher
		return fuzzyPositionToCloneMatchingProvider.findClone(file, clones, offset, length, fileContent);
	}

	public DocumentEvent getLastDocumentEvent()
	{
		return lastDocumentEvent;
	}

	public void setLastDocumentEvent(DocumentEvent lastDocumentEvent)
	{
		this.lastDocumentEvent = lastDocumentEvent;
	}

	/*
	 * Some convenience methods for the store provider interface.
	 * All calls to these methods are directly delegated to the storeProvider instance.
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IDebuggableStoreProvider#checkCacheIntegrity()
	 */
	public boolean checkCacheIntegrity()
	{
		if (storeProvider instanceof IDebuggableStoreProvider)
			return ((IDebuggableStoreProvider) storeProvider).checkCacheIntegrity();

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IDebuggableStoreProvider#checkDataIntegrity()
	 */
	public boolean checkDataIntegrity() throws StoreLockingException
	{
		if (storeProvider instanceof IDebuggableStoreProvider)
			return ((IDebuggableStoreProvider) storeProvider).checkDataIntegrity();

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.store.IDebuggableStoreProvider#getCacheStats()
	 */
	public String getCacheStats()
	{
		if (storeProvider instanceof IDebuggableStoreProvider)
			return ((IDebuggableStoreProvider) storeProvider).getCacheStats();

		return "cache stats not available";
	}

	/**
	 * Returns the "lock object" instance on which all code which reads or modifies
	 * CPCPositions from a document needs to synchronise.
	 *  
	 * @return a lock object, must not be modified in any way, never null.
	 */
	public Object getCPCPositionLockObject()
	{
		assert (documentRegistry != null);
		return documentRegistry;
	}

	/**
	 * This timer task regularly checks whether any of the currently registered documents
	 * is dirty and has exceeded the synchronisation timeout.<br/>
	 * For all documents which fulfil this criteria the {@link CloneRepository#storePositionData(ICloneFile, IDocument, boolean)}
	 * method is called (inside the main thread). 
	 */
	private class DocumentRegistrySynchronisationTimerTask extends TimerTask
	{
		public DocumentRegistrySynchronisationTimerTask()
		{
			log.trace("DocumentRegistrySynchronisationTimerTask()");
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run()
		{
			//log.trace("DocumentRegistrySynchronisationTimerTask.run()");
			try
			{
				long currTime = System.currentTimeMillis();

				//prevent concurrent modification exceptions by copying the value list
				//before the iteration
				List<DocumentDescriptor> docDescs;
				synchronized (documentRegistry)
				{
					docDescs = new ArrayList<DocumentDescriptor>(documentRegistry.values());
				}
				//NOTE: it is important that we relinquish the lock on the documentRegistry before calling the runnable!

				for (DocumentDescriptor docDesc : docDescs)
				{
					if (!docDesc.isDirty())
						//ignore all unchanged entries
						continue;

					if ((currTime - docDesc.getLastModification()) < CLONE_POSITION_SYNCHRONISATION_TIMEOUT)
						//timeout not yet reached, ignore
						continue;

					if (log.isTraceEnabled())
						log.trace("DocumentRegistrySynchronisationTimerTask.run() - going to sync dirty document - "
								+ docDesc);

					//ok, this document's clone data should be transmitted to the store provider
					//To prevent concurrency problems, we execute the actual code in the main thread.
					//TODO: this decision should be reevaluated if it leads to poor performance
					Display.getDefault().syncExec(new StorePositionDataRunnable(docDesc));
				}
			}
			catch (Exception e)
			{
				//it is crucial that this thread is not killed by some intermitted error.
				log.error(
						"DocumentRegistrySynchronisationTimerTask.run() - error during document registry synchronisation - "
								+ e, e);
			}
		}
	}

	/**
	 * Simple runnable wrapper which caches the required clone file and document variables
	 * until the runnable is executed.  
	 */
	private class StorePositionDataRunnable implements Runnable
	{
		private DocumentDescriptor docDesc;

		public StorePositionDataRunnable(DocumentDescriptor docDesc)
		{
			this.docDesc = docDesc;
		}

		public void run()
		{
			if (docDesc.isDirty())
			{
				if (!docDesc.isStale())
				{
					//store positions
					try
					{
						storeProvider.acquireWriteLock(LockMode.NO_WRITE_LOCK_HOOK_NOTIFY);

						storePositionData(docDesc, false);
					}
					catch (StoreLockingException e)
					{
						log.error("loadPositionData() - locking error - " + e, e);
					}
					finally
					{
						storeProvider.releaseWriteLock();
					}
				}
				else
					log.error("StorePositionDataRunnable.run() - stale but dirty document - " + docDesc,
							new Throwable());
			}
		}
	}
}
