package org.electrocodeogram.cpc.notification.ui.listener;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.hub.event.EclipseFileAccessEvent;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.notification.ui.marker.NotificationMarker;


public class FileAccessListener implements IEventHubListener
{
	private static Log log = LogFactory.getLog(FileAccessListener.class);

	private IStoreProvider storeProvider;

	public FileAccessListener()
	{
		log.trace("FileAccessListener()");

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

	private void processEclipseFileAccessEvent(EclipseFileAccessEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("processEclipseFileAccessEvent() - event: " + event);

		/*
		 * Make sure the workspace is not locked.
		 */
		if (ResourcesPlugin.getWorkspace().isTreeLocked())
		{
			if (log.isDebugEnabled())
				log
						.debug("processEclipseFileAccessEvent() - workspace is currently locked, dispatching marker update to background job.");

			CloneMarkerUpdateJob job = new CloneMarkerUpdateJob(event);
			job.schedule(500);
		}
		else
		{
			updateMarkers(event);
		}
	}

	private void updateMarkers(EclipseFileAccessEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("updateMarkers() - event: " + event);

		//get the clone file for this event
		ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(event.getProject(), event.getFilePath(), true, true);
		if (cloneFile == null)
		{
			log
					.warn("processEclipseFileAccessEvent() - unable to acquire clone file for event, file might have been deleted/moved - project: "
							+ event.getProject() + ", filePath: " + event.getFilePath());
			return;
		}

		/*
		 * TODO: add a configuration option which allows the user to choose between two modes of operation:
		 * a) create markers on file open and drop them on file close (what this listener is doing)
		 * 		This means that the problems view will always display only cpc notifications for
		 * 		files which are currently open.
		 * b) always display all markers.
		 * 		The problems view would always show cpc notifications for all files. Similar to how it is
		 * 		behaving for java compiler errors/warnings.
		 * 		One possible approach for this would be to mark CPCNotificationMarkers persistent markers
		 * 		and to only update them on clone modifications and maybe also on file open.
		 */

		/*
		 * Recreate all markers on file open.
		 */
		if (EclipseFileAccessEvent.Type.OPENED.equals(event.getType()))
		{
			//drop and recreate all markers for this file.

			log.trace("processEclipseFileAccessEvent() - dropping and recreating all markers for document (OPENED).");

			NotificationMarker.createMarkers(storeProvider, cloneFile);
		}

		/*
		 * Drop all markers on file clone.
		 */
		else if (EclipseFileAccessEvent.Type.CLOSED.equals(event.getType()))
		{
			//remove all markers from the file

			log.trace("processEclipseFileAccessEvent() - dropping all markers from document (CLOSED).");

			NotificationMarker.removeMarkers(cloneFile);
		}

		else
		{
			log.warn("processEclipseFileAccessEvent() - unknown event type: " + event.getType() + " - " + event);
		}
	}

	private class CloneMarkerUpdateJob extends Job
	{
		private EclipseFileAccessEvent event;

		public CloneMarkerUpdateJob(EclipseFileAccessEvent event)
		{
			super("FileAccessListener.CPCCloneMarkerUpdateJob");

			this.event = event;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor)
		{
			//TODO:/FIXME: make sure the workspace is not locked
			if (ResourcesPlugin.getWorkspace().isTreeLocked())
				log.warn("FileAccessListener.CPCCloneMarkerUpdateJob.run() - workspace is still locked!");

			updateMarkers(event);

			return Status.OK_STATUS;
		}

	}

}
