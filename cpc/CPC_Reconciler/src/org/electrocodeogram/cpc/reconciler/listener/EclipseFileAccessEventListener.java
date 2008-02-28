package org.electrocodeogram.cpc.reconciler.listener;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.hub.event.EclipseFileAccessEvent;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IReconcilerProvider;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IReconciliationResult;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.UpdateMode;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


/**
 * High priority {@link EclipseFileAccessEvent} listener which listens for files which are newly opened
 * and checks for each file whether it is still in sync with the persisted clonefile content.<br/>
 * <br/>
 * Files which are out of sync are delegated to the default {@link IReconcilerProvider}
 * for reconciliation.
 * 
 * @author vw
 */
public class EclipseFileAccessEventListener implements IEventHubListener
{
	private static Log log = LogFactory.getLog(EclipseFileAccessEventListener.class);

	private IStoreProvider storeProvider;

	public EclipseFileAccessEventListener()
	{
		if (log.isTraceEnabled())
			log.trace("EclipseFileAccessEventListener()");

		storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(IStoreProvider.class);
		assert (storeProvider != null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener#processEvent(org.electrocodeogram.cpc.core.api.hub.event.CPCEvent)
	 */
	@Override
	public void processEvent(CPCEvent event)
	{
		if (event instanceof EclipseFileAccessEvent)
		{
			processEclipseFileAccessEvent((EclipseFileAccessEvent) event);
		}
		else
		{
			log.error("processEvent() - got event of wrong type: " + event, new Throwable());
		}
	}

	protected void processEclipseFileAccessEvent(EclipseFileAccessEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("processEclipseFileAccessEvent(): " + event);

		//we're only interested in files which are being opened
		if (!EclipseFileAccessEvent.Type.OPENED.equals(event.getType()))
		{
			log.trace("processEclipseFileAccessEvent() - ignoring non-OPENED event.");
			return;
		}

		//get a clone file handle for the affected file
		ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(event.getProject(), event.getFilePath(), false,
				false);
		if (cloneFile == null)
		{
			log.trace("processEclipseFileAccessEvent() - unable to get clone file handle for file, ignoring event.");
			return;
		}

		/*
		 * Now check if the file is still in sync with the persisted content.
		 */
		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			if (checkForExternalModification(cloneFile, event.getDocument()))
			{
				/*
				 * The file was externally modified, try to reconcile the changes.
				 */
				reconcileExternalModification(cloneFile, event.getDocument());
			}
		}
		catch (StoreLockingException e)
		{
			log.error("processEclipseFileAccessEvent() - store locking error - " + e, e);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}
	}

	/**
	 * Checks whether the given file was externally modified.<br/>
	 * <br/>
	 * The caller of this method needs to hold an exclusive {@link IStoreProvider} write lock.
	 * 
	 * @param cloneFile the clone file to check, never null.
	 * @param document the document object for the file which should be checked for external modifications, never null.
	 * @return true if the file was externally modified, false otherwise.
	 */
	protected boolean checkForExternalModification(ICloneFile cloneFile, IDocument document)
			throws StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("checkForExternalModification() - cloneFile: " + cloneFile + ", document: " + document);
		assert (cloneFile != null && document != null);

		//find the underlying file
		IFile fileHandle = CoreFileUtils.getFileForCloneFile(cloneFile);
		if (fileHandle == null)
		{
			/*
			 * This can happen if the file was deleted or its project was closed.
			 */
			if (log.isDebugEnabled())
				log.debug("checkForExternalModification() - unable to obtain file handle for clone file, ignoring: "
						+ cloneFile);
			return false;
		}

		//get the current modification timestamp
		IFileInfo fileInfo;
		try
		{
			fileInfo = EFS.getStore(fileHandle.getLocationURI()).fetchInfo();
		}
		catch (CoreException e)
		{
			//WTF? something strange happened
			log.warn("checkForExternalModification() - unable to get file info - " + e, e);
			return false;
		}

		/*
		 * Ok, now we finally have a clone file entry with modification date and a file handle for the file on disk.
		 * Now we can do the real checking.
		 */

		if (cloneFile.getModificationDate() != fileInfo.getLastModified()
				|| cloneFile.getSize() != fileInfo.getLength())
		{
			//Ok, the modification dates differ. It is possible that the file was externally modified.
			//Lets see if the content was changed.

			String persistedContent = storeProvider.getPersistedCloneFileContent(cloneFile);

			//TODO: should we use the file content or the current document content here?
			//		is this only a question of speed or is there a functional difference?
			//String currentContent = CoreUtils.readFileContent(fileHandle);
			String currentContent = document.get();

			//integrity checking
			if (CPCCorePlugin.isDebugChecking() && !currentContent.equals(CoreUtils.readFileContent(fileHandle)))
			{
				log
						.warn(
								"checkForExternalModification() - current file content and document content are not in sync!, using document content - clonefile: "
										+ cloneFile
										+ ", document content: "
										+ CoreStringUtils.quoteString(document.get())
										+ ", file content: "
										+ CoreUtils.readFileContent(fileHandle), new Throwable());
			}

			//make sure we did get the contents
			if (persistedContent == null || currentContent == null)
			{
				/*
				 * There will be no persisted content if the file was never saved after an clone file
				 * handle was created for it.
				 * This is therefore not an error but a sometimes to be expected condition.
				 */
				log.warn("checkForExternalModification() - unable to get file content - cloneFile: " + cloneFile
						+ ", persistedContent: " + CoreStringUtils.truncateString(persistedContent)
						+ ", currentContent: " + CoreStringUtils.truncateString(currentContent), new Throwable());
				return false;
			}

			if (!persistedContent.equals(currentContent))
			{
				//Seems as if there really was an external modification.
				log
						.info("checkForExternalModification() - file content was externally modified, trying to reconcile changes - fileUuid: "
								+ cloneFile.getUuid()
								+ ", project: "
								+ cloneFile.getProject()
								+ ", path: "
								+ cloneFile.getPath());

				//try to reconcile the external modification
				return true;
			}
			else
			{
				if (log.isDebugEnabled())
					log
							.debug("checkForExternalModification() - modification date changed but content is still equal - cloneFile: "
									+ cloneFile
									+ ", persisted modDate: "
									+ cloneFile.getModificationDate()
									+ ", persisted size: "
									+ cloneFile.getSize()
									+ " - current modDate: "
									+ fileInfo.getLastModified() + ", current size: " + fileInfo.getLength());

				return false;
			}
		}
		else
		{
			if (log.isTraceEnabled())
				log
						.trace("checkForExternalModification() - modification date and size of clone file entry and filesystem file match - modificationDate: "
								+ cloneFile.getModificationDate() + ", size: " + cloneFile.getSize());

			return false;
		}

	}

	/**
	 * Tries to reconcile a persisted file and its persisted clone data with the new content of the file
	 * after an external modification.<br/>
	 * <br/>
	 * The caller of this method needs to hold an exclusive {@link IStoreProvider} write lock.
	 * 
	 * @param cloneFile the clone file to reconcile, never null.
	 * @param document the document object for the file which should be reconciled, never null.
	 * @return true if any clone data was modified due to the reconciler run, false otherwise.
	 */
	protected boolean reconcileExternalModification(ICloneFile cloneFile, IDocument document)
			throws StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("reconcileExternalModification() - cloneFile: " + cloneFile + ", document: " + document);
		assert (cloneFile != null && document != null);

		log.info("reconcileExternalModification() - reconciling ext. modifications - clone file: " + cloneFile);

		//get an IExternalModificationReconcilerProvider reference 
		IReconcilerProvider extModReconcilerProvider = (IReconcilerProvider) CPCCorePlugin
				.getProviderRegistry().lookupProvider(IReconcilerProvider.class);
		if (extModReconcilerProvider == null)
		{
			log
					.warn("reconcileExternalModification() - unable to obtain external modification reconciler provider, not reconciling external change.");
			return false;
		}

		IReconciliationResult result = null;
		boolean cloneDataModified = false;

		//TODO: remove this, this is just here to make the default store provider implementation happy,
		//it is not really needed
		storeProvider.getClonesByFile(cloneFile.getUuid());

		//first make sure that the current store provider data matches the persisted data
		storeProvider.revertData(cloneFile);

		//TODO: remove this, this is just here to make the default store provider implementation happy,
		//it is not really needed
		storeProvider.getClonesByFile(cloneFile.getUuid());

		//get the persisted clones for this file
		List<IClone> persistedClones = storeProvider.getPersistedClonesForFile(cloneFile.getUuid());
		if (persistedClones.isEmpty())
		{
			log
					.info("reconcileExternalModification() - file contained no clones, no reconciliation needed - cloneFile: "
							+ cloneFile);

			//this also updates the persistedFileContent, we therefore need to call it even if
			//we did not modify any clone data
			storeProvider.persistData(cloneFile);

			return false;
		}

		//now get the persisted and file content
		String persistedFileContent = storeProvider.getPersistedCloneFileContent(cloneFile);

		//and the current file content
		//IFile fileHandle = CoreFileUtils.getFileForCloneFile(cloneFile);
		//String newFileContent = CoreUtils.readFileContent(fileHandle);
		String newFileContent = document.get();

		if (persistedFileContent == null || newFileContent == null)
		{
			log.warn("reconcileExternalModification() - unable to get file content - cloneFile: " + cloneFile
					+ ", persistedFileContent: " + CoreStringUtils.truncateString(persistedFileContent)
					+ ", newFileContent: " + CoreStringUtils.truncateString(newFileContent));
			return false;
		}

		//now delegate to the external modification reconciler provider
		result = extModReconcilerProvider.reconcile(cloneFile, persistedClones, persistedFileContent, newFileContent,
				true);
		//TODO: instead of always setting notifyUser to true here, we should be getting the value from
		//		some preference setting.

		if (log.isTraceEnabled())
			log.trace("reconcileExternalModification() - extModReconciler result: " + result);

		log.info("reconcileExternalModification() - reconciliation finished - total: " + persistedClones.size()
				+ ", lost: " + result.getLostClones().size() + ", removed: " + result.getRemovedClones().size()
				+ ", moved: " + result.getMovedClones().size() + ", modified: " + result.getModifiedClones().size());

		/*
		 * Now apply the new data.
		 */

		//remove any clones which were lost or removed
		if (!result.getLostClones().isEmpty())
		{
			log.info("reconcileExternalModification() - lost clones: " + result.getLostClones());
			storeProvider.removeClones(result.getLostClones());
			cloneDataModified = true;
		}

		if (!result.getRemovedClones().isEmpty())
		{
			log.info("reconcileExternalModification() - removed clones: " + result.getLostClones());
			storeProvider.removeClones(result.getRemovedClones());
			cloneDataModified = true;
		}

		//update any clones which were moved or modified
		if (!result.getMovedClones().isEmpty())
		{
			log.info("reconcileExternalModification() - moved clones: " + result.getLostClones());
			storeProvider.updateClones(result.getMovedClones(), UpdateMode.MOVED);
			cloneDataModified = true;
		}
		if (!result.getModifiedClones().isEmpty())
		{
			log.info("reconcileExternalModification() - modified clones: " + result.getLostClones());
			storeProvider.updateClones(result.getModifiedClones(), UpdateMode.MODIFIED);
			cloneDataModified = true;
		}

		/*
		 * Now persist the modification.
		 */

		//this also updates the persistedFileContent, we therefore need to call it even if
		//we did not modify any clone data
		storeProvider.persistData(cloneFile);

		if (log.isTraceEnabled())
			log.trace("reconcileExternalModification() - result: " + cloneDataModified);

		return cloneDataModified;
	}

}
