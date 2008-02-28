package org.electrocodeogram.cpc.track.listener;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.hub.event.EclipseFileChangeEvent;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.track.CPCTrackPlugin;


/**
 * Listens for file moves and file deletions and updates the {@link IStoreProvider} accordingly,
 * if it hasn't already been updated.
 *  
 * @author vw
 */
public class EclipseFileChangeListener implements IEventHubListener
{
	private static Log log = LogFactory.getLog(EclipseFileChangeListener.class);

	private IStoreProvider storeProvider;

	public EclipseFileChangeListener()
	{
		log.trace("EclipseFileChangeListener()");

		storeProvider = CPCTrackPlugin.getCloneRepository().getStoreProvider();
		assert (storeProvider != null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener#processEvent(org.electrocodeogram.cpc.core.api.hub.event.CPCEvent)
	 */
	@Override
	public void processEvent(CPCEvent event)
	{
		if (event instanceof EclipseFileChangeEvent)
		{
			processEclipseFileChangeEvent((EclipseFileChangeEvent) event);
		}
		else
		{
			log.error("processEvent() - got event of wrong type: " + event, new Throwable());
		}
	}

	private void processEclipseFileChangeEvent(EclipseFileChangeEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("processEclipseFileChangeEvent() - event: " + event);

		//ignore events which we generated ourself
		if (event.isPostStoreProviderMoveUpdate())
		{
			log.trace("processEclipseFileChangeEvent() - ignoring self generated event.");
			return;
		}

		/*
		 * Note: EclipseFileChangeEvents are generated for _all_ files inside of a moved/renamed
		 *       folder/package or project too. Which means that there can be a very large number
		 *       of events of this type triggered by one user action.
		 *       As such this method is performance critical.
		 */

		//Get the clone file for this event.
		//Tell the store provider to not create new clone file instances, as we're only interested in existing
		//clone files here anyway.
		ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(event.getProject(), event.getFilePath(), false,
				false);

		//it is acceptable for events to refer to files which are unknown to the store provider
		if (cloneFile == null)
		{
			if (EclipseFileChangeEvent.Type.MOVED.equals(event.getType()))
			{
				//check if the move was handled or if the file is unknown
				ICloneFile cloneFile2 = storeProvider.lookupCloneFileByPath(event.getNewProject(), event
						.getNewFilePath(), false, false);

				if (cloneFile2 == null)
				{
					//file is unknown
					//the file did not contain any clone data, ignore it.
					log.trace("processEclipseFileChangeEvent() - file did not contain clone data, ignoring event.");
				}
				else
				{
					//move was already handled
					log.trace("processEclipseFileChangeEvent() - file move was already handled, ignoring event.");

					//dispatch a new post store provider update event
					dispatchNewEvent(event);
				}
			}

			return;
		}

		if (EclipseFileChangeEvent.Type.REMOVED.equals(event.getType()))
		{
			//the file was removed, drop all its clone data

			if (log.isDebugEnabled())
				log.debug("processEclipseFileChangeEvent() - deleting clone file: " + cloneFile);

			try
			{
				storeProvider.acquireWriteLock(LockMode.DEFAULT);
				storeProvider.purgeData(cloneFile, true);
			}
			catch (StoreLockingException e)
			{
				log.error("processEclipseFileChangeEvent() - locking error - " + e, e);
			}
			finally
			{
				storeProvider.releaseWriteLock();
			}
		}
		else if (EclipseFileChangeEvent.Type.MOVED.equals(event.getType()))
		{
			//the file was renamed or moved (could also be a folder/project move/rename)

			if (log.isDebugEnabled())
				log.debug("processEclipseFileChangeEvent() - moving clone file: " + cloneFile);

			try
			{
				storeProvider.acquireWriteLock(LockMode.DEFAULT);
				storeProvider.moveCloneFile(cloneFile, event.getNewProject(), event.getNewFilePath());
			}
			catch (StoreLockingException e)
			{
				log.error("processEclipseFileChangeEvent() - locking error - " + e, e);
			}
			finally
			{
				storeProvider.releaseWriteLock();
			}

			//dispatch a new post store provider update event
			dispatchNewEvent(event);
		}
		else
		{
			log.warn("processEclipseFileChangeEvent() - unknown event type: " + event.getType() + " - " + event);
		}
	}

	private void dispatchNewEvent(EclipseFileChangeEvent event)
	{
		try
		{
			EclipseFileChangeEvent newEvent = (EclipseFileChangeEvent) event.clone();
			newEvent.setPostStoreProviderMoveUpdate(true);
			CPCCorePlugin.getEventHubRegistry().dispatch(newEvent);
		}
		catch (CloneNotSupportedException e)
		{
			log.error("processEclipseFileChangeEvent() - error while cloning event - event: " + event + " - " + e, e);
		}
	}
}
