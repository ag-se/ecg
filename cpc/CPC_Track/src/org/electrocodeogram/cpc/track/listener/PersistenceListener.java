package org.electrocodeogram.cpc.track.listener;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.hub.event.EclipseResourcePersistenceEvent;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.core.utils.CoreConfigurationUtils;
import org.electrocodeogram.cpc.track.CPCTrackPlugin;


/**
 * Listens for {@link EclipseResourcePersistenceEvent}s and persists/reverts the clone data
 * via the {@link IStoreProvider} accordingly.
 * 
 * @author vw
 * 
 * @see EclipseResourcePersistenceEvent
 * @see IStoreProvider
 */
public class PersistenceListener implements IEventHubListener
{
	private static Log log = LogFactory.getLog(PersistenceListener.class);

	public PersistenceListener()
	{
		if (log.isTraceEnabled())
			log.trace("PersistenceListener()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener#processEvent(org.electrocodeogram.cpc.core.api.hub.event.CPCEvent)
	 */
	@Override
	public void processEvent(CPCEvent event)
	{
		if (event instanceof EclipseResourcePersistenceEvent)
		{
			processPersistenceEvent((EclipseResourcePersistenceEvent) event);
		}
		else
		{
			log.error("processEvent() - got event of wrong type: " + event, new Throwable());
		}
	}

	protected void processPersistenceEvent(EclipseResourcePersistenceEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("processPersistenceEvent(): " + event);

		//OLD: we're now also tracking changes in closed files via document change events
		//		//we're only interested in events for files which are currently open in an editor
		//		if (!event.isOpenInEditor())
		//		{
		//			if (log.isDebugEnabled())
		//				log.debug("processPersistenceEvent() - ignoring event for closed file - " + event);
		//			return;
		//		}

		//make sure that this is a java file, we're not interested in any other file types
		if (!CoreConfigurationUtils.isSupportedFile(event.getFilePath()))
		{
			log.trace("processPersistenceEvent() - ignoring unsupported source file type");
			return;
		}

		IStoreProvider storeProvider = CPCTrackPlugin.getCloneRepository().getStoreProvider();

		//get the clone file handle
		ICloneFile cloneFile = storeProvider
				.lookupCloneFileByPath(event.getProject(), event.getFilePath(), true, false);
		//TODO: false or true here for followFileMove ?
		if (cloneFile == null)
		{
			log.fatal("processPersistenceEvent() - unable to retrieve clone file: " + cloneFile + ", project: "
					+ event.getProject() + ", path: " + event.getFilePath(), new Throwable());
			return;
		}

		if (log.isTraceEnabled())
			log.trace("processPersistenceEvent() - clone file: " + cloneFile);

		try
		{
			//get an exclusive lock on the clone data
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			if (event.getType().equals(EclipseResourcePersistenceEvent.Type.SAVED))
			{
				log.trace("processPersistenceEvent() - persisting clone data");

				//a currently open file was saved, we should now persist the file content
				storeProvider.persistData(cloneFile);
			}
			else if (event.getType().equals(EclipseResourcePersistenceEvent.Type.REVERTED))
			{
				log.trace("processPersistenceEvent() - reverting clone data");

				//a currently open file was reverted, we need to revert the clone data too
				storeProvider.revertData(cloneFile);
			}
			else
			{
				log.warn("processPersistenceEvent() - unsupported event type: " + event.getType() + " - " + event);
			}

		}
		catch (StoreLockingException e)
		{
			//this should never happen
			log.error("processPersistenceEvent() - locking error - " + e, e);
		}
		finally
		{
			//make sure we release the lock again
			storeProvider.releaseWriteLock();
		}

		if (event.getType().equals(EclipseResourcePersistenceEvent.Type.REVERTED))
		{
			/*
			 * There is a special case where a revert event might actually be the first event
			 * which we see for a file. In such a situation CloneRepository.documentInit() will
			 * not yet have been called and we need to do that here.
			 * 
			 * One possible situation in which this happens is if a file is out of sync with
			 * the file system (i.e. due to an external modification). Eclipse will ask the user
			 * whether he wants to refresh the file once he tries to open it in an editor.
			 * If the user refreshes the file, a revert event will be created.
			 */
			CPCTrackPlugin.getCloneRepository().ensureDocumentInit(event.getProject(), event.getFilePath(),
					event.getDocument());
		}
	}
}
