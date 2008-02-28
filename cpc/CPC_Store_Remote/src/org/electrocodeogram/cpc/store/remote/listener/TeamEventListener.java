package org.electrocodeogram.cpc.store.remote.listener;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.special.IRemoteStoreCloneFile;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.hub.event.EclipseTeamEvent;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener;
import org.electrocodeogram.cpc.core.api.provider.store.IRemotableStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;


/**
 * Simple listener for {@link EclipseTeamEvent}s which updates the repository
 * revision value of the corresponding files in the {@link IStoreProvider} storage.
 * 
 * @author vw
 */
public class TeamEventListener implements IEventHubListener
{
	private static Log log = LogFactory.getLog(TeamEventListener.class);

	private IRemotableStoreProvider storeProvider = null;

	public TeamEventListener()
	{
		log.trace("TeamEventListener()");

		IStoreProvider tmpStoreProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				IStoreProvider.class);
		assert (tmpStoreProvider != null && tmpStoreProvider instanceof IRemotableStoreProvider);
		storeProvider = (IRemotableStoreProvider) tmpStoreProvider;
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
		//		if (event instanceof TeamOperationEvent)
		//		{
		//			processTeamOperationEvent((TeamOperationEvent) event);
		//		}
		//		else
		//		{
		//			log.error("processEvent() - got event of wrong type: " + event, new Throwable());
		//		}
	}

	/**
	 * Checks each <em>EclipseTeamEvent</em> to update the revision information
	 * of the corresponding {@link ICloneFile} instance.
	 */
	protected void processEclipseTeamEvent(EclipseTeamEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("processEclipseTeamEvent() - event: " + event);

		//we need the revision data, otherwise we can only reset the revision data
		if (event.getNewRevision() == null)
			log.warn("processEclipseTeamEvent() - no revision data in event - event: " + event);

		try
		{
			//get a write lock
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			//get the file for this event
			IRemoteStoreCloneFile cloneFile = (IRemoteStoreCloneFile) storeProvider.lookupCloneFileByPath(event
					.getProject(), event.getFilePath(), true, true);
			if (cloneFile == null)
			{
				log.warn("processEclipseTeamEvent() - unable to obtain clone file for event, ignoring - event: "
						+ event);
				return;
			}

			//check if we need to update the repository version data for this file
			if ((event.getNewRevision() == null && cloneFile.getRepositoryVersion() != null)
					|| (event.getNewRevision() != null && !event.getNewRevision().equals(
							cloneFile.getRepositoryVersion())))
			{
				if (log.isTraceEnabled())
					log.trace("processEclipseTeamEvent() - updating rep. revision of file - old: "
							+ cloneFile.getRepositoryVersion() + ", new: " + event.getNewRevision() + ", clone file: "
							+ cloneFile);

				//set the new revision identifier
				cloneFile.setRepositoryVersion(event.getNewRevision());

				if (EclipseTeamEvent.Type.COMMIT.equals(event.getType()))
				{
					//during a commit we'll also need to reset the remote dirty flag.
					cloneFile.setRemoteDirty(false);
				}

				//update file entry
				storeProvider.updateCloneFile(cloneFile);
			}
			else
			{
				log.trace("processEclipseTeamEvent() - repository revision up to date, nothing to do.");
			}

		}
		catch (StoreLockingException e)
		{
			log.error("processEclipseTeamEvent() - locking error - " + e, e);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}
	}

	//	protected void processTeamOperationEvent(TeamOperationEvent event)
	//	{
	//		if (log.isTraceEnabled())
	//			log.trace("processTeamOperationEvent(): " + event);
	//
	//		try
	//		{
	//			//get a write lock
	//			storeProvider.acquireWriteLock(LockMode.DEFAULT);
	//
	//			for (TeamOperationFile affectedFile : event.getAffectedFiles())
	//			{
	//				if (log.isTraceEnabled())
	//					log.trace("processTeamOperationEvent() - affected file: " + affectedFile);
	//
	//				//we need the revision data, otherwise we can only reset the revision data
	//				if (affectedFile.getRevision() == null)
	//					log.warn("processTeamOperationEvent() - no revision data in event - file: " + affectedFile
	//							+ ", event: " + event);
	//
	//				//get the file for this event
	//				IRemoteStoreCloneFile cloneFile = (IRemoteStoreCloneFile) storeProvider.lookupCloneFileByPath(
	//						affectedFile.getProject(), affectedFile.getFilePath(), true);
	//				if (cloneFile == null)
	//				{
	//					log.warn("processTeamOperationEvent() - unable to obtain clone file for event, ignoring - file: "
	//							+ affectedFile + ", event: " + event);
	//					return;
	//				}
	//
	//				//check if we need to update the repository version data for this file
	//				if ((affectedFile.getRevision() == null && cloneFile.getRepositoryVersion() != null)
	//						|| (affectedFile.getRevision() != null && !affectedFile.getRevision().equals(
	//								cloneFile.getRepositoryVersion())))
	//				{
	//					if (log.isTraceEnabled())
	//						log.trace("processTeamOperationEvent() - updating rep. revision of file - old: "
	//								+ cloneFile.getRepositoryVersion() + ", new: " + affectedFile.getRevision()
	//								+ ", clone file: " + cloneFile);
	//
	//					//set the new revision identifier
	//					cloneFile.setRepositoryVersion(affectedFile.getRevision());
	//
	//					//update file entry
	//					storeProvider.updateCloneFile(cloneFile);
	//				}
	//			}
	//		}
	//		catch (StoreLockingException e)
	//		{
	//			log.error("processTeamOperationEvent() - locking error - " + e, e);
	//		}
	//		finally
	//		{
	//			storeProvider.releaseWriteLock();
	//		}
	//	}
}
