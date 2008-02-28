package org.electrocodeogram.cpc.track.listener;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.hub.event.EclipseFileAccessEvent;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener;
import org.electrocodeogram.cpc.track.CPCTrackPlugin;
import org.electrocodeogram.cpc.track.repository.CloneRepository;


/**
 * Listens for {@link EclipseFileAccessEvent}s and notifies the {@link CloneRepository}
 * in order to load/store the clone position data for the file which was just
 * opened/closed.
 * 
 * @author vw
 * 
 * @see EclipseFileAccessEvent
 * @see CloneRepository#documentInit(String, String, org.eclipse.jface.text.IDocument)
 * @see CloneRepository#documentShutdown(String, String, org.eclipse.jface.text.IDocument)
 */
public class FileAccessListener implements IEventHubListener
{
	private static Log log = LogFactory.getLog(FileAccessListener.class);

	public FileAccessListener()
	{
		log.trace("FileAccessListener()");
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
			processFileAccessEvent((EclipseFileAccessEvent) event);
		}
		else
		{
			log.error("processEvent() - got event of wrong type: " + event, new Throwable());
		}
	}

	private void processFileAccessEvent(EclipseFileAccessEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("processFileAccessEvent() - event: " + event);

		if (EclipseFileAccessEvent.Type.OPENED.equals(event.getType()))
		{
			//fill in CPCPositions for all clones
			CPCTrackPlugin.getCloneRepository().documentInit(event.getProject(), event.getFilePath(),
					event.getDocument());
		}
		else if (EclipseFileAccessEvent.Type.CLOSED.equals(event.getType()))
		{
			//make sure that we write back any potentially
			CPCTrackPlugin.getCloneRepository().documentShutdown(event.getProject(), event.getFilePath(),
					event.getDocument(), event.isDirty());
		}
		else
		{
			log.debug("processFileAccessEvent() - unknown event type, ignoring: " + event.getType());
		}
	}
}
