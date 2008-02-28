package org.electrocodeogram.cpc.store.remote.sql.listener;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.history.IFileHistory;
import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.team.core.history.IFileRevision;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.special.IRemoteStoreCloneFile;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.hub.event.EclipseTeamEvent;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener;
import org.electrocodeogram.cpc.core.api.provider.cpcrepository.CPCRepositoryException;
import org.electrocodeogram.cpc.core.api.provider.cpcrepository.ICPCRepositoryProvider;
import org.electrocodeogram.cpc.core.api.provider.cpcrepository.ICPCRevision;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeProvider;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeResult;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeTask;
import org.electrocodeogram.cpc.core.api.provider.merge.MergeException;
import org.electrocodeogram.cpc.core.api.provider.store.IRemotableStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.UpdateMode;
import org.electrocodeogram.cpc.core.utils.CoreConfigurationUtils;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


public class EclipseTeamEventListener implements IEventHubListener
{
	private static Log log = LogFactory.getLog(EclipseTeamEventListener.class);

	private IRemotableStoreProvider storeProvider;
	private ICPCRepositoryProvider cpcRepositoryProvider;
	private IMergeProvider mergeProvider;

	public EclipseTeamEventListener()
	{
		log.trace("EclipseTeamEventListener()");

		IStoreProvider tmpStoreProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				IStoreProvider.class);
		assert (tmpStoreProvider != null && tmpStoreProvider instanceof IRemotableStoreProvider);
		storeProvider = (IRemotableStoreProvider) tmpStoreProvider;

		cpcRepositoryProvider = (ICPCRepositoryProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				ICPCRepositoryProvider.class);
		assert (cpcRepositoryProvider != null);

		mergeProvider = (IMergeProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(IMergeProvider.class);
		assert (mergeProvider != null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener#processEvent(org.electrocodeogram.cpc.core.api.hub.event.CPCEvent)
	 */
	@Override
	public void processEvent(CPCEvent event)
	{
		if (event instanceof EclipseTeamEvent)
		{
			processEclipseTeamEvent((EclipseTeamEvent) event);
		}
		else
		{
			log.error("processEvent() - got event of wrong type: " + event, new Throwable());
		}
	}

	private void processEclipseTeamEvent(EclipseTeamEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("processEclipseTeamEvent() - event: " + event);

		/*
		 * We're only interested in supported source files here.
		 */
		if (!CoreConfigurationUtils.isSupportedFile(event.getProject(), event.getFilePath()))
		{
			log.trace("processEclipseTeamEvent() - ignoring event for non-supported source file.");
			return;
		}

		if (EclipseTeamEvent.Type.COMMIT.equals(event.getType()))
		{
			processCommit(event);
		}
		else if (EclipseTeamEvent.Type.UPDATE.equals(event.getType()))
		{
			try
			{
				cpcRepositoryProvider.hintStartTransaction();
				processUpdate(event);
			}
			catch (CPCRepositoryException e)
			{
				log.error("processEclipseTeamEvent() - cpc repository provider error - event: " + event + " - " + e, e);
			}
			finally
			{
				cpcRepositoryProvider.hintEndTransaction();
			}
		}
		else
		{
			log.warn("processEclipseTeamEvent() - unknown team event type, ignoring event - type: " + event.getType()
					+ ", event: " + event, new Throwable());
		}
	}

	private void processCommit(EclipseTeamEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("processCommit() - event: " + event);

		/*
		 * Ok, the file should exist and if it doesn't have a clonefile entry yet, we can create it now.
		 * We need to do this with an exclusive lock. As we'll also be fetching a current copy of
		 * the clone data.
		 */
		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			//get the clone file entry
			ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(event.getProject(), event.getFilePath(), true,
					false);
			if (cloneFile == null)
			{
				//This would mean that the file doesn't exist in the workspace. Can that happen?
				log.error(
						"processCommit() - unable to get clone file entry for affected source file - event: " + event,
						new Throwable());
				return;
			}

			//get the clone data
			List<IClone> clones = storeProvider.getClonesByFile(cloneFile.getUuid());

			cpcRepositoryProvider.hintStartTransaction();

			//now create and fill a new cpc revision for this data
			ICPCRevision cpcRevision = cpcRepositoryProvider.createRevision();

			cpcRevision.setRevisionId(event.getNewRevision());
			cpcRevision.setCloneFile(cloneFile);
			cpcRevision.setClones(clones);

			//sent this data to the cpc repository
			cpcRepositoryProvider.putRevision(cpcRevision);
		}
		catch (StoreLockingException e)
		{
			log.error("processCommit() - locking error - event: " + event + " - " + e, e);
			return;
		}
		catch (CPCRepositoryException e)
		{
			log.error("processCommit() - failed to send new cpc revision data to cpc repository - event: " + event
					+ " - " + e, e);
			return;
		}
		finally
		{
			cpcRepositoryProvider.hintEndTransaction();
			storeProvider.releaseWriteLock();
		}

	}

	private void processUpdate(EclipseTeamEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("processUpdate() - event: " + event);

		/*
		 * Ok, there are multiple possible situations which we might face here.
		 * 
		 * 1) This is a remotely added file, we do not have any corresponding local file.
		 * (or rather did not, as the file should have been written to disk at this point)
		 * 		-> the event's old revision will be NULL
		 * 
		 * 2) The file existed locally and remotely and was not modified locally.
		 * This is thus a simple update and no merging is needed.
		 * 		-> the base revision clone data matches the local clone data
		 * 
		 * 3) The file existed locally and remotely and was locally modified.
		 * We will therefore need to merge the clone data.
		 * 		-> we need to do a 3-way merge
		 * 
		 * We're only looking at 1) - 3) for now.
		 * 
		 * Other cases (TODO: are they of any importance here?)
		 * - file was switched to a different tag/branch
		 * - the file was remotely deleted
		 * - the file was locally deleted
		 * 
		 * TODO: - what about moves/renames?
		 */

		//first try to get the remote data
		//the remote data should always exist
		ICPCRevision remoteRevision = null;
		try
		{
			remoteRevision = cpcRepositoryProvider.getRevision(event.getNewRevision(), event.getProject(), event
					.getFilePath());
			if (remoteRevision == null)
			{
				log.error("processUpdate() - unable to find remote cpc data in cpc repository - revision: "
						+ event.getNewRevision() + ", project: " + event.getProject() + ", path: "
						+ event.getFilePath() + ", event: " + event, new Throwable());
				return;
			}
		}
		catch (CPCRepositoryException e)
		{
			log.error("processUpdate() - failed to obtain cpc data from cpc repository - event: " + event + " - " + e,
					e);
			return;
		}

		try
		{
			//get a store provider lock
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			//we know that our files are likely to be out of sync during this operation.
			//tell the store provider to ignore this fact.
			//storeProvider.setExternalModificationCheckEnabled(false);

			/*
			 * Decide whether this is case 1), 2) or 3)
			 */

			if (event.getOldRevision() == null)
			{
				/*
				 * case 1) - file was remotely added and did not exist locally
				 * 
				 * We can just persist the remote clone data.
				 */
				log.trace("processUpdate() - remotely added file detected, overwriting local clone data.");

				overwriteLocalCloneData(event, remoteRevision, true);
			}
			else
			{
				/*
				 * case 2) or 3) - we have a base revision and a clone file
				 * 
				 * first retrieve the clone file and check if it was modified
				 */

				ICloneFile cloneFile = storeProvider.lookupCloneFile(remoteRevision.getCloneFile().getUuid());
				if (cloneFile == null)
				{
					log.error("processUpdate() - unable to obtain clone file entry - fileUuid: "
							+ remoteRevision.getCloneFile().getUuid() + ", event: " + event, new Throwable());

					//Lets try to "save" this situation somehow.
					handleError(event, remoteRevision);

					return;
				}

				if (!((IRemoteStoreCloneFile) cloneFile).isRemoteDirty())
				{
					/*
					 * case 2) - this is a simple update, no merging is required,
					 * we can just overwrite the local clone data.
					 */
					log.trace("processUpdate() - locally unmodified file detected, overwriting local clone data.");

					overwriteLocalCloneData(event, remoteRevision, false);
				}
				else
				{
					/*
					 * case 3) - we need to merge the clone data
					 */

					log.trace("processUpdate() - remotely and locally modified file detected, merging clone data.");

					processMerge(event, remoteRevision, cloneFile);
				}
			}
		}
		catch (CPCRepositoryException e)
		{
			log.error("processUpdate() - failed to obtain cpc data from cpc repository (2) - event: " + event + " - "
					+ e, e);
			return;
		}
		catch (StoreLockingException e)
		{
			log.error("processUpdate() - locking error - " + e, e);
			return;
		}
		finally
		{
			//reenable external modification checking
			//storeProvider.setExternalModificationCheckEnabled(true);

			storeProvider.releaseWriteLock();
		}

	}

	/**
	 * Triggers a full merge processing for the corresponding file. 
	 */
	private void processMerge(EclipseTeamEvent event, ICPCRevision remoteRevision, ICloneFile localCloneFile)
			throws CPCRepositoryException, StoreLockingException
	{
		//get the base revision cpc data
		ICPCRevision baseRevision = cpcRepositoryProvider.getRevision(event.getOldRevision(), remoteRevision
				.getCloneFile().getUuid());
		if (baseRevision == null)
		{
			log.error("processMerge() - unable to find base cpc data in cpc repository - revision: "
					+ event.getOldRevision() + ", fileUuid: " + remoteRevision.getCloneFile().getUuid() + ", event: "
					+ event, new Throwable());
			return;
		}

		//get the old content for the file
		String localSource = storeProvider.getPersistedCloneFileContent(localCloneFile);
		if (localSource == null)
		{
			log.error("processMerge() - unable to obtain persisted local source - clone file: " + localCloneFile
					+ ", event: " + event, new Throwable());
			return;
		}

		//get the merged content for the file
		IFile fileHandle = CoreFileUtils.getFileForCloneFile(localCloneFile);
		if (fileHandle == null || !fileHandle.exists())
		{
			log.error("processMerge() - unable to obtain source file handle or file does not exist - fileHandle: "
					+ fileHandle + ", clone file: " + localCloneFile + ", event: " + event, new Throwable());
			return;
		}

		String mergedSource = CoreUtils.readFileContent(fileHandle);
		if (mergedSource == null)
		{
			log.error("processMerge() - unable to obtain current local source - clone file: " + localCloneFile
					+ ", event: " + event, new Throwable());
			return;
		}

		//get the old clone data for the local file
		//List<IClone> localClones = storeProvider.getPersistedClonesForFile(localCloneFile.getUuid());
		/*
		 * As a simplification we assume that the user always persists his clone changes before
		 * engaging in team operations.
		 * We enforce this by explicitly reverting the clone data for the affected file.
		 */
		List<IClone> preRevertLocalClones = storeProvider.getClonesByFile(localCloneFile.getUuid());
		storeProvider.revertData(localCloneFile);
		List<IClone> localClones = storeProvider.getClonesByFile(localCloneFile.getUuid());

		//TODO: if the two clone lists differ, print a warning
		//we only do a very simple check for now
		if (preRevertLocalClones.size() != localClones.size())
		{
			log
					.warn(
							"processMerge() - the affected file was not saved prior to the team operation! This can lead to clone data loss! - clone file: "
									+ localCloneFile
									+ ", event: "
									+ event
									+ ", dirty clone data: "
									+ preRevertLocalClones + ", persisted clone data: " + localClones, new Throwable());
		}

		/*
		 * At this point we have:
		 *  - remote cpc data
		 *  - base cpc data
		 *  - local cpc data
		 *  - local source
		 *  - merged source
		 *  
		 * We're missing
		 * - remote source
		 * - base source
		 */

		RepositoryProvider teamRepositoryProvider = RepositoryProvider.getProvider(fileHandle.getProject());
		if (teamRepositoryProvider == null)
		{
			log.error("processMerge() - unable to obtain team repository provider for project - project: "
					+ fileHandle.getProject() + ", event: " + event, new Throwable());
			return;
		}

		IFileHistoryProvider fileHistoryProvider = teamRepositoryProvider.getFileHistoryProvider();
		if (fileHistoryProvider == null)
		{
			log.error("processMerge() - unable to obtain team file history provider - teamRepositoryProvider: "
					+ teamRepositoryProvider + ", event: " + event, new Throwable());
			return;
		}

		IFileHistory fileHistory = fileHistoryProvider.getFileHistoryFor(fileHandle, IFileHistoryProvider.NONE,
				new NullProgressMonitor());
		if (fileHistory == null)
		{
			log.error(
					"processMerge() - unable to obtain team file history - fileHistoryProvider: " + fileHistoryProvider
							+ ", teamRepositoryProvider: " + teamRepositoryProvider + ", event: " + event,
					new Throwable());
			return;
		}

		IFileRevision remoteFileRevision = fileHistory.getFileRevision(remoteRevision.getRevisionId());
		IFileRevision baseFileRevision = fileHistory.getFileRevision(baseRevision.getRevisionId());
		if (remoteFileRevision == null || baseFileRevision == null)
		{
			log.error("processMerge() - unable to obtain team file revision - remoteFileRevision ("
					+ remoteRevision.getRevisionId() + "): " + remoteFileRevision + ", baseFileRevision ("
					+ baseRevision.getRevisionId() + "):  " + baseFileRevision + ", fileHistory: " + fileHistory
					+ ", fileHistoryProvider: " + fileHistoryProvider + ", teamRepositoryProvider: "
					+ teamRepositoryProvider + ", event: " + event, new Throwable());
			return;
		}

		String remoteSource;
		String baseSource;
		try
		{
			remoteSource = CoreFileUtils.readStreamContent(remoteFileRevision.getStorage(new NullProgressMonitor())
					.getContents());
			baseSource = CoreFileUtils.readStreamContent(baseFileRevision.getStorage(new NullProgressMonitor())
					.getContents());
			if (remoteSource == null || baseSource == null)
			{
				log.error("processMerge() - unable to obtain remote or base source - remoteSource: "
						+ CoreStringUtils.truncateString(remoteSource) + ", baseSource: "
						+ CoreStringUtils.truncateString(baseSource) + ", remoteFileRevision: " + remoteFileRevision
						+ ", baseFileRevision:  " + baseFileRevision + ", event: " + event, new Throwable());
				return;
			}
		}
		catch (CoreException e)
		{
			log.error(
					"processMerge() - unable to obtain remote or base source - remoteFileRevision: "
							+ remoteFileRevision + ", baseFileRevision:  " + baseFileRevision + ", event: " + event
							+ " - " + e, e);
			return;
		}

		/*
		 * Ok, now we _finally_ have everything we need x_X
		 */

		IMergeTask mergeTask = mergeProvider.createTask();

		mergeTask.setLocalCloneFile(localCloneFile);
		mergeTask.setLocalClones(localClones);
		mergeTask.setLocalSourceFileContent(localSource);

		mergeTask.setRemoteCloneFile(remoteRevision.getCloneFile());
		mergeTask.setRemoteClones(remoteRevision.getClones());
		mergeTask.setRemoteSourceFileContent(remoteSource);

		mergeTask.setBaseCloneFile(baseRevision.getCloneFile());
		mergeTask.setBaseClones(baseRevision.getClones());
		mergeTask.setBaseSourceFileContent(baseSource);

		mergeTask.setMergedSourceFileContent(mergedSource);

		/*
		 * Now do the merge.
		 */
		handleMerge(event, mergeTask);
	}

	private void handleMerge(EclipseTeamEvent event, IMergeTask mergeTask) throws StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("handleMerge() - event: " + event + ", mergeTask: " + mergeTask);
		assert (event != null && mergeTask != null && mergeTask.isValid());

		IMergeResult mergeResult;
		try
		{
			mergeResult = mergeProvider.merge(mergeTask);
		}
		catch (IllegalArgumentException e)
		{
			//this shouldn't happen
			log.error("handleMerge() - invalid merge task - mergeTask: " + mergeTask + ", event: " + event + " - " + e,
					e);
			return;
		}
		catch (MergeException e)
		{
			log.error("handleMerge() - error while merging cpc data - mergeTask: " + mergeTask + ", event: " + event
					+ " - " + e, e);
			return;
		}

		if (log.isTraceEnabled())
			log.trace("handleMerge() - merge result: " + mergeResult);

		if (!mergeResult.getLocalPerspective().getLostClones().isEmpty())
		{
			log.info("handleMerge() - the following local clones were lost due to merge conflicts - lost clones: "
					+ mergeResult.getLocalPerspective().getLostClones());
		}
		if (!mergeResult.getRemotePerspective().getLostClones().isEmpty())
		{
			log.info("handleMerge() - the following remote clones were lost due to merge conflicts - lost clones: "
					+ mergeResult.getRemotePerspective().getLostClones());
		}

		/*
		 * Now update the store provider data.
		 */

		//update the clonefile entry itself
		storeProvider.updateCloneFile(mergeResult.getCloneFile());

		//new clones
		if (!mergeResult.getLocalPerspective().getAddedClones().isEmpty())
			storeProvider.addClones(mergeResult.getLocalPerspective().getAddedClones());

		//removed clones
		if (!mergeResult.getLocalPerspective().getRemovedClones().isEmpty())
			storeProvider.removeClones(mergeResult.getLocalPerspective().getRemovedClones());

		//lost/dropped clones
		if (!mergeResult.getLocalPerspective().getLostClones().isEmpty())
			storeProvider.removeClones(mergeResult.getLocalPerspective().getLostClones());

		//moved clones
		if (!mergeResult.getLocalPerspective().getMovedClones().isEmpty())
			storeProvider.updateClones(mergeResult.getLocalPerspective().getMovedClones(), UpdateMode.MOVED);

		//modified clones
		if (!mergeResult.getLocalPerspective().getModifiedClones().isEmpty())
			storeProvider.updateClones(mergeResult.getLocalPerspective().getModifiedClones(), UpdateMode.MODIFIED);

		//now persist the new data
		//this will also store the new merged source file content in the store provider
		storeProvider.persistData(mergeResult.getCloneFile());

	}

	/**
	 * Tries to somehow "save the day" after something went really really wrong.<br/>
	 * At this point we're unable to do a normal update/merge of the clone data.
	 * 
	 * @param event
	 * @param remoteRevision
	 * @throws StoreLockingException
	 */
	private void handleError(EclipseTeamEvent event, ICPCRevision remoteRevision) throws StoreLockingException
	{
		log
				.warn("handleError() - merge/update impossible - purging all local clone data and falling back to remote clone data.");
		overwriteLocalCloneData(event, remoteRevision, false);
	}

	/**
	 * Overwrites the current clone data for the file corresponding to the given {@link ICPCRevision}
	 * and optionally logs a warning if current clone data does exist. 
	 */
	private void overwriteLocalCloneData(EclipseTeamEvent event, ICPCRevision remoteRevision, boolean warnIfExists)
			throws StoreLockingException
	{
		//first check whether the file was already added to CPC store for some reason
		ICloneFile cloneFile = storeProvider.lookupCloneFile(remoteRevision.getCloneFile().getUuid());
		if (cloneFile != null)
		{
			//purge the clone data
			if (warnIfExists)
				log.warn(
						"processUpdate() - remotly added file already has a local clonefile entry, purging... - cloneFile: "
								+ cloneFile + ", event: " + event + ", remoteRevision: " + remoteRevision,
						new Throwable());

			storeProvider.purgeData(cloneFile, true);
		}

		//it might also have been added with a different uuid
		cloneFile = storeProvider.lookupCloneFileByPath(remoteRevision.getCloneFile().getProject(), remoteRevision
				.getCloneFile().getPath(), false, false);
		if (cloneFile != null)
		{
			//purge the clone data
			if (warnIfExists)
				log
						.warn(
								"processUpdate() - remotly added file already has a local clonefile entry WITH DIFFERENT UUID!, purging... - cloneFile: "
										+ cloneFile + ", event: " + event + ", remoteRevision: " + remoteRevision,
								new Throwable());

			storeProvider.purgeData(cloneFile, true);
		}

		//now add the new clone file
		storeProvider.addCloneFile(remoteRevision.getCloneFile());

		//and its clones
		storeProvider.addClones(remoteRevision.getClones());

		//and then persist the data
		storeProvider.persistData(remoteRevision.getCloneFile());
	}

}
