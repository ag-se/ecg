package org.electrocodeogram.cpc.notification.ui.listener;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.hub.event.CloneModificationEvent;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.notification.ui.marker.NotificationMarker;


public class CloneModificationListener implements IEventHubListener
{
	private static Log log = LogFactory.getLog(CloneModificationListener.class);

	private IStoreProvider storeProvider;

	public CloneModificationListener()
	{
		log.trace("CloneModificationListener()");

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
		if (event instanceof CloneModificationEvent)
		{
			processCloneModificationEvent((CloneModificationEvent) event);
		}
		else
		{
			log.error("processEvent() - got event of wrong type: " + event, new Throwable());
		}
	}

	private void processCloneModificationEvent(final CloneModificationEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("processCloneModificationEvent() - event: " + event);

		/*
		 * We have a potential resource access problem here.
		 * We may be called indirectly due to a resource change event.
		 * However, during such an event the entire resource tree is locked and any attempt
		 * to add or remove markers would throw a ResourceException.
		 * 
		 * TODO:/FIXME: Check whether the resource tree is currently locked.
		 * 		If it is, delegate the marker update to a background thread.
		 * 		Another possibility would be to always do all marker updates in a background
		 * 		thread. That might work. 
		 */

		if (ResourcesPlugin.getWorkspace().isTreeLocked())
		{
			//ok, the resource tree is indeed locked.
			if (log.isDebugEnabled())
				log
						.debug("processCloneModificationEvent() - workspace is locked, dispatching clone marker update to background thread.");

			CloneMarkerUpdateJob job = new CloneMarkerUpdateJob(event);
			job.schedule(1000);
		}
		else
		{
			//nothing is locked, we can do the marker update inline
			updateCloneMarkers(event);
		}

	}

	private void updateCloneMarkers(CloneModificationEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("updateCloneMarkers() - event: " + event);

		/*
		 * Keeping the CPCNotificationMarkers up2date isn't all that easy at this point.
		 * We could drop and recreate all markers for a file on each CloneModificationEvent.
		 * Another approach would be to drop removed markers and recreate those which belong
		 * to modified clones. Usually we can ignore markers which belong to moved clones
		 * as the default position updater which is used for the markers should yield equal
		 * results as our cpc position updater in this case.
		 * However, if the clone's state has changed, even moved clones are of interest to us.
		 * 
		 * It is important to keep in mind that there can be a very large number of
		 * CloneModificationEvents during large scale automated actions, i.e. refactorings
		 * or source code reformats.
		 * 
		 * We'll try the incremental updating approach for now.
		 * Note: we do not need to create any markers for newly added clones, that is done
		 * elsewhere.
		 */

		if (event.getCloneFile() == null)
		{
			//this is an overall clone data reset.
			//TODO: This is not yet fully supported. We only drop all markers for now.
			//What would be needed here is a way to get a list of all clones with NOTIFY or WARN
			//state from the store provider
			log.trace("updateCloneMarkers() - full workspace modification, dropping markers.");
			log.warn("updateCloneMarkers() - TODO - recreation or markers not yet implemented.");

			NotificationMarker.removeAllMarkers();
			return;
		}

		if (event.isFullModification())
		{
			//just drop and recreate all markers for this file
			log.trace("updateCloneMarkers() - full modification, dropping and recreating all markers.");

			NotificationMarker.createMarkers(storeProvider, event.getCloneFile());
			return;
		}

		//as a performance improvement: check that there is anything of interest in this event for us
		if ((event.getRemovedClones() == null || event.getRemovedClones().isEmpty())
				&& (event.getModifiedClones() == null || event.getModifiedClones().isEmpty())
				&& (event.getMovedClones() == null || event.getMovedClones().isEmpty()))
		{
			//the event neither removed nor modified clones, there is nothing for us to do
			//this event only adds clones
			log.trace("updateCloneMarkers() - event does not remove or modifiy clones, ignoring.");
			return;
		}

		//get a list of all markers in this document
		List<IMarker> markers = NotificationMarker.getMarkers(event.getCloneFile());

		if (markers.isEmpty())
		{
			//there are no markers in this document, so there is nothing for us to do.
			log.trace("updateCloneMarkers() - document has no markers, ignoring.");
			return;
		}

		/*
		 * Create a temporary lookup structure for markers.
		 * Contains one entry per marker. Once markers are deleted, they are also need to be
		 * removed from this lookup structure.
		 * Newly created markers are not added to the structure, as they should be up to date
		 * and there should be no need to modify them any further.
		 */
		Map<String, IMarker> markerLookup = new HashMap<String, IMarker>(markers.size());
		for (IMarker marker : markers)
		{
			try
			{
				markerLookup.put((String) marker.getAttribute(NotificationMarker.MARKER_FIELD_CLONE_UUID), marker);
			}
			catch (CoreException e)
			{
				log.error(
						"updateCloneMarkers() - error while creating marker lookup structure - " + marker + " - " + e,
						e);
			}
		}

		/*
		 * TODO: It is unclear whether this lookup structure is actually beneficial.
		 * As we can expect to usually have only very few markers per file and only small number of clones.
		 * Just doing a normal search in the markers array might be faster than creating a hash
		 * for each event.
		 * Another approach would be a "global" lookup structure which stores all currently
		 * existing cpc notification markers and which would be reused from event to event.
		 * Somewhat like the CloneDataModel... a MarkerDataModel.
		 */

		/*
		 * REMOVE
		 * Check if we need to remove some markers.
		 */
		if (event.getRemovedClones() != null && !event.getRemovedClones().isEmpty())
		{
			if (log.isTraceEnabled())
				log.trace("updateCloneMarkers() - removing markers for " + event.getRemovedClones().size()
						+ " clones, if any exist.");

			//remove any markers which we might have added for the deleted clones.
			for (IClone clone : event.getRemovedClones())
			{
				//check if there is any marker for this clone.
				IMarker marker = markerLookup.get(clone.getUuid());

				if (marker != null)
				{
					//ok, we have a match. Delete this marker.

					if (log.isTraceEnabled())
						log.trace("updateCloneMarkers() - deleting marker: " + marker);

					try
					{
						marker.delete();
						//also remove from lookup structure
						markerLookup.remove(clone.getUuid());
					}
					catch (CoreException e)
					{
						log.error("updateCloneMarkers() - unable to delete marker: " + marker + " - " + e, e);
					}
				}
			}
		}

		/*
		 * MODIFIED
		 * Check if we should recreate/remove some markers for clones for which the content changed. 
		 */
		if (event.getModifiedClones() != null && !event.getModifiedClones().isEmpty())
		{
			if (log.isTraceEnabled())
				log.trace("updateCloneMarkers() - removing/recreating markers for " + event.getModifiedClones().size()
						+ " clones, if any exist.");

			//remove any markers which we might have added for the deleted clones.
			for (IClone clone : event.getModifiedClones())
			{
				//check if there is any marker for this clone.
				IMarker marker = markerLookup.get(clone.getUuid());

				if (marker != null)
				{
					//ok, we have a match. Delete and recreate this marker.

					if (log.isTraceEnabled())
						log.trace("updateCloneMarkers() - deleting marker: " + marker);

					try
					{
						//delete old marker
						marker.delete();
						//also remove from lookup structure
						markerLookup.remove(clone.getUuid());

						//now create a new marker, if the clone still needs one.
						if (IClone.State.NOTIFY.equals(clone.getCloneState())
								|| IClone.State.WARN.equals(clone.getCloneState()))
						{
							if (log.isTraceEnabled())
								log.trace("updateCloneMarkers() - recreating marker: " + marker);

							NotificationMarker.createMarker(event.getCloneFile(), clone, NotificationMarker.Type
									.valueOf(clone.getCloneState().toString()), NotificationMarker
									.buildMarkerMessage(clone.getCloneStateMessage()));
						}
					}
					catch (CoreException e)
					{
						log.error("updateCloneMarkers() - unable to delete marker: " + marker + " - " + e, e);
					}
				}
			}
		}

		/*
		 * MOVED
		 * We don't need to update any positions here.
		 * We're only interested in state changes
		 */
		if (event.getMovedClones() != null && !event.getMovedClones().isEmpty())
		{
			if (log.isTraceEnabled())
				log.trace("updateCloneMarkers() - rechecking markers for " + event.getMovedClones().size()
						+ " clones, if any exist.");

			//remove any markers which we might have added for the deleted clones.
			for (IClone clone : event.getMovedClones())
			{
				//check if there is any marker for this clone.
				IMarker marker = markerLookup.get(clone.getUuid());

				if (marker != null)
				{
					//ok, we have a match. Check if this marker is still up to date

					try
					{
						if (!marker.getAttribute(NotificationMarker.MARKER_FIELD_TYPE).equals(
								clone.getCloneState().toString()))
						{
							//the clone state does not match the state of the marker, remove and
							//recreate (if needed) the marker

							if (log.isTraceEnabled())
								log.trace("updateCloneMarkers() - deleting marker: " + marker);

							//delete old marker
							marker.delete();
							//also remove from lookup structure
							markerLookup.remove(clone.getUuid());

							//now create a new marker, if the clone still needs one.
							if (IClone.State.NOTIFY.equals(clone.getCloneState())
									|| IClone.State.WARN.equals(clone.getCloneState()))
							{
								if (log.isTraceEnabled())
									log.trace("updateCloneMarkers() - recreating marker: " + marker);

								NotificationMarker.createMarker(event.getCloneFile(), clone, NotificationMarker.Type
										.valueOf(clone.getCloneState().toString()), NotificationMarker
										.buildMarkerMessage(clone.getCloneStateMessage()));
							}
						}
					}
					catch (CoreException e)
					{
						log.error("updateCloneMarkers() - unable to delete marker: " + marker + " - " + e, e);
					}
				}
			}
		}

		log.trace("updateCloneMarkers() - done.");
	}

	private class CloneMarkerUpdateJob extends Job
	{
		private CloneModificationEvent event;

		public CloneMarkerUpdateJob(CloneModificationEvent event)
		{
			super("CloneModificationListener.CPCCloneMarkerUpdateJob");

			log.trace("CPCCloneMarkerUpdateJob()");

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
				log.warn("CPCCloneMarkerUpdateJob.run() - workspace is still locked!");

			//double check that we haven't been shut down in the mean time
			if (CPCCorePlugin.getDefault() == null)
			{
				log.warn("CPCCloneMarkerUpdateJob.run() - CPCCorePlugin was shut down, ignoring marker update.");
				return Status.OK_STATUS;
			}

			updateCloneMarkers(event);

			return Status.OK_STATUS;
		}

	}
}
