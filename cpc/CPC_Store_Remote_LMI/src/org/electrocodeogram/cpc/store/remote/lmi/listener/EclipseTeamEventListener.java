package org.electrocodeogram.cpc.store.remote.lmi.listener;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.hub.event.EclipseTeamEvent;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeProvider;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeResult;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeTask;
import org.electrocodeogram.cpc.core.api.provider.merge.MergeException;
import org.electrocodeogram.cpc.core.api.provider.store.IRemotableStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.UpdateMode;
import org.electrocodeogram.cpc.core.api.provider.xml.IMappingRegistry;
import org.electrocodeogram.cpc.core.api.provider.xml.MappingException;
import org.electrocodeogram.cpc.core.api.provider.xml.MappingStore;
import org.electrocodeogram.cpc.core.utils.CoreConfigurationUtils;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;
import org.electrocodeogram.cpc.store.remote.lmi.utils.LMIUtils;
import org.electrocodeogram.cpc.store.remote.lmi.utils.XMLPersistenceUtils;


public class EclipseTeamEventListener implements IEventHubListener
{
	private static Log log = LogFactory.getLog(EclipseTeamEventListener.class);

	private IRemotableStoreProvider storeProvider;
	private IMappingRegistry mappingRegistry;
	private IMergeProvider mergeProvider;

	public EclipseTeamEventListener()
	{
		log.trace("EclipseTeamEventListener()");

		IStoreProvider tmpStoreProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				IStoreProvider.class);
		assert (tmpStoreProvider != null && tmpStoreProvider instanceof IRemotableStoreProvider);
		storeProvider = (IRemotableStoreProvider) tmpStoreProvider;

		mappingRegistry = (IMappingRegistry) CPCCorePlugin.getProviderRegistry().lookupProvider(IMappingRegistry.class);
		assert (mappingRegistry != null);

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

		//we're only interested in UPDATE events
		if (!EclipseTeamEvent.Type.UPDATE.equals(event.getType()))
		{
			log.trace("processEclipseTeamEvent() - ignoring non-Update event.");
			return;
		}

		//first get a file handle
		IFile file = CoreFileUtils.getFile(event.getProject(), event.getFilePath());
		if (file == null)
		{
			log.error("processEclipseTeamEvent() - can't access file, ignoring event - file: " + event.getFilePath()
					+ ", project: " + event.getProject() + ", event: " + event, new Throwable());
			return;
		}

		/*
		 * Now check whether this file is a supported source file.
		 * We're only interested in events for such files here.
		 */
		if (!CoreConfigurationUtils.isSupportedFile(file))
		{
			log.trace("processEclipseTeamEvent() - ignoring event for non-supported source file.");
			return;
		}

		/*
		 * PROBLEM:
		 * 	We need to know the files UUID here in order to be able to check the temporary storage
		 * 	area. This is no problem if the file already existed locally, we can simply do a store
		 * 	provider lookup.
		 * 	However, if the file was created remotely and is transferred to this system due to
		 * 	an update operation we have no knowledge about its file UUID.
		 */

		String fileUuid = null;

		//first check if it is a known file
		ICloneFile cloneFile = storeProvider
				.lookupCloneFileByPath(event.getProject(), event.getFilePath(), false, true);
		if (cloneFile == null)
		{
			/*
			 * Ok, now things are getting tricky. We don't know this files UUID. It might simply mean that the file
			 * contains no clone data, but it might be a new remotely-added file with clone data!
			 */

			//lets see if there is a corresponding cpc data file (for an LMI merge this should always be the case)
			IFile cpcFile = XMLPersistenceUtils.getXmlDataFile(file);
			if (cpcFile.exists())
			{
				//ok, we should be able to obtain the file UUID from the cpc data file.
				String xml = CoreUtils.readFileContent(cpcFile);
				if (xml == null)
				{
					log.error("processEclipseTeamEvent() - failed to read cpc data file - data file: " + cpcFile
							+ ", source file: " + file + ", event: " + event, new Throwable());
					return;
				}

				try
				{
					fileUuid = mappingRegistry.extractCloneObjectUuidFromString(xml);
					if (fileUuid == null)
					{
						log.error("processEclipseTeamEvent() - failed to parse cpc data file - data file: " + cpcFile
								+ ", source file: " + file + ", event: " + event, new Throwable());
						return;
					}
				}
				catch (MappingException e)
				{
					log.error("processEclipseTeamEvent() - failed to parse cpc data file - data file: " + cpcFile
							+ ", source file: " + file + ", event: " + event + " - " + e, e);
					return;
				}

				//ok, now we have the file uuid. Usually this would mean that it is a new file.
				//but lets check to be sure
				cloneFile = storeProvider.lookupCloneFile(fileUuid);
				if (cloneFile != null)
				{
					//this wouldn't really be an error, but it would be strange, wouldn't it?
					log
							.warn("processEclipseTeamEvent() - file could not be identified by path but was accessible by UUID, maybe the file was moved remotely? - cloneFile: "
									+ cloneFile
									+ ", data file: "
									+ cpcFile
									+ ", source file: "
									+ file
									+ ", event: "
									+ event);
				}
			}
			else
			{
				/*
				 * There is no cpc data file, the file might still have been remotely added.
				 * But the cpc data file might not yet have been updated/merged.
				 * 
				 * In that case we wouldn't be able to find any data in the temp directory
				 * anyway. As it wouldn't have been written there yet. (or never might).
				 * 
				 * Another possibility would be that there really was no clone data
				 * stored for that file (neither remotely nor locally).
				 * 
				 * Anyway, there isn't anything we can do here right now.
				 * 
				 * TODO: We should probably listen for team events for cpc data files and
				 * do a cross check to see whether we've already processed the update/merge.
				 */

				log
						.warn("processEclipseTeamEvent() - unable to determine file UUID, maybe the file has no clone data or the cpc data file was not yet updated/merged? Ignoring event. - data file: "
								+ cpcFile + ", source file: " + file + ", event: " + event);

				return;
			}
		}
		else
		{
			fileUuid = cloneFile.getUuid();
		}

		/*
		 * Check whether what kind of data we have at hand for this source file.
		 * 
		 * 1) LMI merge
		 * If the file was updated/merged due to a headless LMI merge, we'll be able
		 * to load the local, base and remote versions of the source file and the
		 * corresponding data file from the temporary storage folder of this plugin.
		 * 
		 * 2) sync view, merge
		 * If the file's cpc data file was merged via the synchronise view, we might
		 * have the remote cpc data file in our temp folder at this point.
		 * However, that is not guaranteed. The team event for the java source file might
		 * have been generated before the update/merge of the cpc data file has taken place.
		 * (see 4)
		 * 
		 * 3) sync view, simple update
		 * If the local cpc data file wasn't dirty, we won't have any data in our temp folder.
		 * 
		 * 4) sync view, the corresponding cpc data file was not part of the team operation
		 * or was not yet processed.
		 */

		//read the merged source file content
		String mergedSource = CoreUtils.readFileContent(file);

		/*
		 * 1) is relatively simple to handle.
		 * Lets check if we have that case here.
		 */
		if (LMIUtils.isFullMergeDataAvailable(fileUuid))
		{
			//YAY!
			log.trace("processEclipseTeamEvent() - full LMI merge/update detected, all data available.");

			//get all data
			//TODO: local data might not be available if file was remotely added?
			String localSource = LMIUtils.readTemporaryFile(fileUuid, "local.src");
			String localCpc = LMIUtils.readTemporaryFile(fileUuid, "local.cpc");
			String baseSource = LMIUtils.readTemporaryFile(fileUuid, "base.src");
			String baseCpc = LMIUtils.readTemporaryFile(fileUuid, "base.cpc");
			String remoteSource = LMIUtils.readTemporaryFile(fileUuid, "remote.src");
			String remoteCpc = LMIUtils.readTemporaryFile(fileUuid, "remote.cpc");

			//processLMIMerge(event, localSource, localCpc, remoteSource, remoteCpc, baseSource, baseCpc, mergedSource);
		}
		else
		{
			log
					.warn("processEclipseTeamEvent() - NOT YET IMPLEMENTED - only partial data available, ignoring event - fileUuid: "
							+ fileUuid + ", source file: " + file + ", event: " + event);
			return;
		}

		//Get a handle for the corresponding source file
		//		IFile sourceFile = XMLPersistenceUtils.getSourceFileForCPCDataResource(file);
		//		if (sourceFile == null || !sourceFile.exists())
		//		{
		//			log.error("processEclipseTeamEvent() - can't access source file for cpc data file - data file: " + file
		//					+ ", source file: " + sourceFile, new Throwable());
		//			return;
		//		}
		//just log the file content for now
		//		String content = CoreUtils.readFileContent(file);
		//		log.trace("CONTENT-DATA: " + CoreStringUtils.truncateString(content));
		//
		//		String mergedSourceContent = CoreUtils.readFileContent(sourceFile);
		//		log.trace("CONTENT-SRC : " + CoreStringUtils.truncateString(mergedSourceContent));
		//
		//		if (content == null || content.length() == 0)
		//		{
		//			log.warn("processEclipseTeamEvent() - unable to get cpc xml data file content - data file: " + file
		//					+ ", source file: " + sourceFile, new Throwable());
		//			return;
		//		}
		/*
		 * Now map the xml data back to the remote clone data.
		 */

		//TODO: decide on merge handling
		//log.info("processEclipseTeamEvent() - NOTE - merge is currently deactivated, skipping event.");
		//processMerge(event, file, sourceFile, content, mergedSourceContent);
	}

	private void processLMIMerge(EclipseTeamEvent event, String localSource, String localCpc, String remoteSource,
			String remoteCpc, String baseSource, String baseCpc, String mergedSource)
	{
		if (log.isTraceEnabled())
			log.trace("processMerge() - event: " + event + ", localSource: "
					+ CoreStringUtils.truncateString(localSource) + ", localCpc: "
					+ CoreStringUtils.truncateString(localCpc) + ", remoteSource: "
					+ CoreStringUtils.truncateString(remoteSource) + ", remoteCpc: "
					+ CoreStringUtils.truncateString(remoteCpc) + ", baseSource: "
					+ CoreStringUtils.truncateString(baseSource) + ", baseCpc: "
					+ CoreStringUtils.truncateString(baseCpc));

		ICloneFile localCloneFile = null;
		List<IClone> localClones = null;

		ICloneFile remoteCloneFile = null;
		List<IClone> remoteClones = null;

		ICloneFile baseCloneFile = null;
		List<IClone> baseClones = null;

		try
		{
			//TODO: local data might not be available if file was remotely added?
			MappingStore mappingStore = mappingRegistry.mapFromString(localCpc);
			localCloneFile = mappingStore.getCloneFile();
			localClones = mappingStore.getClones();

			mappingStore = mappingRegistry.mapFromString(remoteCpc);
			remoteCloneFile = mappingStore.getCloneFile();
			remoteClones = mappingStore.getClones();

			mappingStore = mappingRegistry.mapFromString(baseCpc);
			baseCloneFile = mappingStore.getCloneFile();
			baseClones = mappingStore.getClones();
		}
		catch (MappingException e)
		{
			log.error("processMerge() - error while parsing cpc data - event: " + event + " - " + e, e);
			return;
		}

		//TODO: local data might not be available if file was remotely added?
		if (localCloneFile == null || localClones == null || remoteCloneFile == null || remoteClones == null
				|| baseCloneFile == null || baseClones == null)
		{
			log.error("processMerge() - cpc data contained no ICloneFile/IClone data - event: " + event
					+ ", localCloneFile: " + localCloneFile + ", remoteCloneFile: " + remoteCloneFile
					+ ", baseCloneFile: " + baseCloneFile + ", localClones: " + localClones + ", remoteClones: "
					+ remoteClones + ", baseClones: " + baseClones, new Throwable());
			return;
		}

		/*
		 * Ok, now we have the current local, remote and base clone data.
		 * Begin the real merge.
		 */

		processMerge(event, localCloneFile, localClones, localSource, remoteCloneFile, remoteClones, remoteSource,
				baseCloneFile, baseClones, baseSource, mergedSource);

	}

	private void processMerge(EclipseTeamEvent event, ICloneFile localCloneFile, List<IClone> localClones,
			String localSource, ICloneFile remoteCloneFile, List<IClone> remoteClones, String remoteSource,
			ICloneFile baseCloneFile, List<IClone> baseClones, String baseSource, String mergedSource)
	{
		if (log.isTraceEnabled())
			log.trace("processMerge() - event: " + event + ", localCloneFile: " + localCloneFile + ", localClones: "
					+ localClones + ", localSource: " + CoreStringUtils.truncateString(localSource)
					+ ", remoteCloneFile: " + remoteCloneFile + ", remoteClones: " + remoteClones + ", remoteSource: "
					+ CoreStringUtils.truncateString(remoteSource) + ", baseCloneFile: " + baseCloneFile
					+ ", baseClones: " + baseClones + ", baseSource: " + CoreStringUtils.truncateString(baseSource));

		//first get a store provider lock
		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			//get latest copy of local clone file, if it exists
			//			ICloneFile localCloneFile = storeProvider.lookupCloneFile(remoteCloneFile.getUuid());
			//			if (localCloneFile == null)
			//			{
			//				//				log.error("processMerge() - unable to obtain local clone file instance for file - remote clone file: "
			//				//						+ remoteCloneFile + ", data file: " + xmlFile + ", source file: " + sourceFile + ", event: "
			//				//						+ event, new Throwable());
			//				return;
			//			}
			//
			//			//get old local clone data
			//			List<IClone> localClones = storeProvider.getClonesByFile(localCloneFile.getUuid());

			//get old local source file content
			String localSourceContent = storeProvider.getPersistedCloneFileContent(localCloneFile);

			/*
			 * Now try to merge the data.
			 */
			IMergeTask mergeTask = mergeProvider.createTask();

			mergeTask.setLocalCloneFile(localCloneFile);
			mergeTask.setLocalClones(localClones);
			mergeTask.setLocalSourceFileContent(localSourceContent);
			//mergeTask.setLocalBaseInSyncHint(localBaseInSyncHint);

			mergeTask.setRemoteCloneFile(remoteCloneFile);
			mergeTask.setRemoteClones(remoteClones);
			mergeTask.setRemoteSourceFileContent(remoteSource);

			mergeTask.setBaseCloneFile(baseCloneFile);
			mergeTask.setBaseClones(baseClones);
			mergeTask.setBaseSourceFileContent(baseSource);

			mergeTask.setMergedSourceFileContent(mergedSource);

			if (log.isTraceEnabled())
				log.trace("processMerge() - trying to merge data - merge task: " + mergeTask);

			IMergeResult mergeResult;
			try
			{
				mergeResult = mergeProvider.merge(mergeTask);
			}
			catch (IllegalArgumentException e)
			{
				//this should never happen
				log
						.error("processMerge() - failed to create valid merge task - mergeTask: " + mergeTask + " - "
								+ e, e);
				return;
			}
			catch (MergeException e)
			{
				log.error("processMerge() - merge failed - mergeTask: " + mergeTask + " - " + e, e);
				return;
			}

			if (log.isTraceEnabled())
				log.trace("processMerge() - merge result: " + mergeResult);

			if (!mergeResult.getLocalPerspective().getLostClones().isEmpty())
			{
				log.info("processMerge() - the following local clones were lost due to merge conflicts - lost clones: "
						+ mergeResult.getLocalPerspective().getLostClones());
			}
			if (!mergeResult.getRemotePerspective().getLostClones().isEmpty())
			{
				log
						.info("processMerge() - the following remote clones were lost due to merge conflicts - lost clones: "
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
		catch (StoreLockingException e)
		{
			log.error("processMerge() - locking error - " + e, e);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}

	}
}
