package org.electrocodeogram.cpc.ui.listener;


import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.hub.event.CloneModificationEvent;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener;
import org.electrocodeogram.cpc.ui.data.CloneDataModel;


public class CloneEventListener implements IEventHubListener
{
	private static Log log = LogFactory.getLog(CloneEventListener.class);

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

	protected void processCloneModificationEvent(CloneModificationEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("processCloneModificationEvent(): " + event);

		CloneDataModel cloneDataModel = CloneDataModel.getInstance();

		//We're only interested in this event, if it matches the currently active file
		//or if it matches all files.
		//Otherwise the clone model won't have any of the clones stored anyway.
		if (event.getCloneFile() != null && !event.getCloneFile().equals(cloneDataModel.getCurrentCloneFile()))
		{
			log.trace("processCloneModificationEvent() - ignoring event for inactive editor window");
			return;
		}

		//check if the clone data model is currently displaying clone data of any file
		ICloneFile cloneFile = cloneDataModel.getCurrentCloneFile();
		if (cloneFile == null)
		{
			//there's nothing for us to do			
			log
					.trace("processCloneModificationEvent() - ignoring event, clone view is currently not displaying any clone data");
			return;
		}

		if (event.isFullModification())
		{
			//we'll need to refresh all clone data
			cloneDataModel.loadCloneData(cloneFile);
		}
		else
		{
			//we only need to refresh part of the clone data
			List<IClone> updatedClones = null;

			if (((event.getModifiedClones() != null) && (!event.getModifiedClones().isEmpty()))
					|| ((event.getMovedClones() != null) && (!event.getMovedClones().isEmpty())))
			{
				//either some clones were modified or some clones were moved, or both
				updatedClones = new LinkedList<IClone>();

				if ((event.getModifiedClones() != null) && (!event.getModifiedClones().isEmpty()))
					updatedClones.addAll(event.getModifiedClones());
				if ((event.getMovedClones() != null) && (!event.getMovedClones().isEmpty()))
				{
					//make sure we don't add any clone twice, by removing any clones which we might
					//already have added in the step before
					updatedClones.removeAll(event.getMovedClones());
					updatedClones.addAll(event.getMovedClones());
				}

			}

			cloneDataModel.cloneDataModified(event.getAddedClones(), updatedClones, event.getRemovedClones());
		}

	}
}
