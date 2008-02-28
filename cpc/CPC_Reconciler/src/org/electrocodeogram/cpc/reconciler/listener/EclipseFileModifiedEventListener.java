package org.electrocodeogram.cpc.reconciler.listener;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.hub.event.EclipseFileModifiedEvent;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IReconcilerProvider;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IReconciliationResult;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.UpdateMode;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


/**
 * Listens for {@link EclipseFileModifiedEvent}s and calls the currently active
 * {@link IReconcilerProvider} for each java file.<br/>
 * The result returned by the reconciler is then used to update the stored clone data.<br/>
 * Finally notifications about clone changes are sent to all interested parties, if any.
 * 
 * NO LONGER IN USE - The modification reconciler is called by CPC Track once a file is opened,
 * 					  if an external modification is detected.
 * 
 * @see EclipseFileModifiedEvent
 * @see IReconcilerProvider
 * @deprecated
 */
@Deprecated
public class EclipseFileModifiedEventListener implements IEventHubListener
{
	private static Log log = LogFactory.getLog(EclipseFileModifiedEventListener.class);

	private IStoreProvider storeProvider;
	private IReconcilerProvider extModReconcilerProvider;

	public EclipseFileModifiedEventListener()
	{
		//get a store provider reference
		storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(IStoreProvider.class);

		//get an IExternalModificationReconcilerProvider reference 
		extModReconcilerProvider = (IReconcilerProvider) CPCCorePlugin.getProviderRegistry()
				.lookupProvider(IReconcilerProvider.class);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener#processEvent(org.electrocodeogram.cpc.core.api.hub.event.CPCEvent)
	 */
	@Override
	public void processEvent(CPCEvent event)
	{
		if (event instanceof EclipseFileModifiedEvent)
		{
			processFileModifiedEvent((EclipseFileModifiedEvent) event);
		}
		else
		{
			log.error("processEvent() - got event of wrong type: " + event, new Throwable());
		}
	}

	/**
	 * Handles an {@link EclipseFileModifiedEvent}.
	 * 
	 * @param event the event to process, never null.
	 */
	private void processFileModifiedEvent(EclipseFileModifiedEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("processFileModifiedEvent(): " + event);

		//make sure that this is a java file, we're not interested in any other file types
		if (!"java".equals((new Path(event.getFilePath())).getFileExtension()))
		{
			log.trace("processFileModifiedEvent() - ignoring non JAVA file");
			return;
		}

		//get the clone file handle
		ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(event.getProject(), event.getFilePath(), true, true);
		if (cloneFile == null)
		{
			log.fatal("processFileModifiedEvent() - unable to retrieve clone file: " + cloneFile + ", project: "
					+ event.getProject() + ", path: " + event.getFilePath(), new Throwable());
			return;
		}

		if (log.isTraceEnabled())
			log.trace("processFileModifiedEvent() - clone file: " + cloneFile);

		IReconciliationResult result = null;

		//we'll need to get a lock for this work
		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			//get the persisted clones for this file
			List<IClone> persistedClones = storeProvider.getClonesByFile(cloneFile.getUuid());

			//now get the persisted and file content
			String persistedFileContent = storeProvider.getPersistedCloneFileContent(cloneFile);

			//and the current file content
			IFile fileHandle = CoreFileUtils.getFileForCloneFile(cloneFile);
			String newFileContent = CoreUtils.readFileContent(fileHandle);

			//now delegate to the external modification reconciler provider
			result = extModReconcilerProvider.reconcile(cloneFile, persistedClones, persistedFileContent,
					newFileContent, true);

			if (log.isTraceEnabled())
				log.trace("processFileModifiedEvent() - extModReconciler result: " + result);

			/*
			 * Now apply the new data.
			 */

			//remove any clones which were lost or removed
			if (!result.getLostClones().isEmpty())
				storeProvider.removeClones(result.getLostClones());
			if (!result.getRemovedClones().isEmpty())
				storeProvider.removeClones(result.getRemovedClones());

			//update any clones which were moved or modified
			if (!result.getMovedClones().isEmpty())
				storeProvider.updateClones(result.getMovedClones(), UpdateMode.MOVED);
			if (!result.getModifiedClones().isEmpty())
				storeProvider.updateClones(result.getModifiedClones(), UpdateMode.MODIFIED);

			/*
			 * Now persist the modification.
			 */

			storeProvider.persistData(cloneFile);

		}
		catch (StoreLockingException e)
		{
			//this should never happen
			log.error("processFileModifiedEvent() - locking error - " + e, e);
		}
		finally
		{
			//make sure we release the lock
			storeProvider.releaseWriteLock();
		}
	}
}
