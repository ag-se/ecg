package org.electrocodeogram.cpc.store.remote.lmi.listener;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.hub.event.EclipseFileChangeEvent;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.store.remote.lmi.utils.XMLPersistenceUtils;


public class EclipseFileChangeListener implements IEventHubListener
{
	private static Log log = LogFactory.getLog(EclipseFileChangeListener.class);

	private static IStoreProvider storeProvider;

	public EclipseFileChangeListener()
	{
		log.trace("EclipseFileChangeListener()");

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
		if (event instanceof EclipseFileChangeEvent)
		{
			processEclipseFileChangeEvent((EclipseFileChangeEvent) event);
		}
		else
		{
			log.error("processEvent() - got event of wrong type: " + event, new Throwable());
		}
	}

	/**
	 * Removes any existing old cpc xml data files which belong to a file which was deleted
	 * or moved. Ignores events for cpc xml data files. Creates new cpc xml data files for
	 * the new file location on file moves.
	 */
	private void processEclipseFileChangeEvent(EclipseFileChangeEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("processEclipseFileChangeEvent() - event: " + event);

		if (!EclipseFileChangeEvent.Type.MOVED.equals(event.getType())
				&& !EclipseFileChangeEvent.Type.REMOVED.equals(event.getType()))
		{
			log.trace("processEclipseFileChangeEvent() - ignoring unknown event type - type: " + event.getType());
			return;
		}

		/*
		 * For type MOVED we will get two events. Only with the second event is it guaranteed that
		 * the store provider is already up to date. We're therefore only interested in the
		 * second event.
		 */
		if (EclipseFileChangeEvent.Type.MOVED.equals(event.getType()) && !event.isPostStoreProviderMoveUpdate())
		{
			log
					.trace("processEclipseFileChangeEvent() - ignoring first MOVED event, waiting for second event for this file (after store provider update).");
			return;
		}

		//ignore cpc data files
		if (event.getFilePath().endsWith("." + XMLPersistenceUtils.XML_PERSISTENCE_EXTENSION))
		{
			log.trace("processEclipseFileChangeEvent() - ignoring cpc xml data file.");
			return;
		}

		//try to get a file handle for the old file
		IFile oldFileHandle = CoreFileUtils.getFile(event.getProject(), event.getFilePath(), true);
		if (oldFileHandle == null)
		{
			//this can happen if the project was deleted too
			if (!ResourcesPlugin.getWorkspace().getRoot().getProject(event.getProject()).isAccessible())
			{
				//the project was deleted or closed, there's nothing for us to do.
				//TODO: in cases where this project was closed but the source file was deleted,
				//	    we might end up with a unused cpc data file somewhere.
				//		If this happens, we might need to add some special checking code
				//		on startup which purges out unneeded cpc data files or something like it.
				if (log.isTraceEnabled())
					log
							.trace("processEclipseFileChangeEvent() - the project for this event was deleted, ignoring event - event: "
									+ event);
			}
			else
			{
				//this is strange
				log.warn(
						"processEclipseFileChangeEvent() - unable to obtain file handle for old file, ignoring event - event: "
								+ event, new Throwable());
			}
			return;
		}

		//no matter whether the file was moved or deleted, we want to remove the old cpc data files
		log.trace("processEclipseFileChangeEvent() - going to remove old cpc data files for file");

		try
		{
			XMLPersistenceUtils.clearXmlData(oldFileHandle);
		}
		catch (CoreException e)
		{
			log.error("processEclipseFileChangeEvent() - error while removing old cpc data file - oldFileHandle: "
					+ oldFileHandle + ", event: " + event + " - " + e, e);
		}

		if (EclipseFileChangeEvent.Type.MOVED.equals(event.getType()))
		{
			/*
			 * TODO: check if this is actually needed.
			 * If we're always going to see a file persistence event at some point during a file move
			 * this step is not needed, as the cpc xml data file will be generated whenever a file
			 * persistence event occurs.
			 * However, if there are cases where no such event is generated, then this part becomes
			 * important.
			 */

			//the file was moved, we'll also need to recreate the cpc data files in the new location
			log.trace("processEclipseFileChangeEvent() - file was moved, creating corresponding cpc data files");

			//get new file handle
			IFile newFileHandle = CoreFileUtils.getFile(event.getNewProject(), event.getNewFilePath());
			if (newFileHandle == null)
			{
				log.error(
						"processEclipseFileChangeEvent() - unable to obtain file handle for new file, ignoring event - event: "
								+ event, new Throwable());
				return;
			}

			try
			{
				storeProvider.acquireWriteLock(LockMode.DEFAULT);

				//try to get a handle for the new file
				ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(event.getNewProject(), event
						.getNewFilePath(), false, false);
				if (cloneFile == null)
				{
					log.error("processEclipseFileChangeEvent() - unable to obtain ICloneFile for event - event: "
							+ event, new Throwable());
					return;
				}

				//get clone data
				List<IClone> clones = storeProvider.getClonesByFile(cloneFile.getUuid());

				try
				{
					XMLPersistenceUtils.writeXmlData(newFileHandle, cloneFile, clones);
				}
				catch (CoreException e)
				{
					log.error(
							"processEclipseFileChangeEvent() - error while creating new cpc xml data file - newFileHandle: "
									+ newFileHandle + ", event: " + event + " - " + e, e);
				}
			}
			catch (StoreLockingException e)
			{
				log.error("processEclipseFileChangeEvent() - locking error while handling event - event: " + event
						+ " - " + e, e);
			}
			finally
			{
				storeProvider.releaseWriteLock();
			}
		}
	}
}
