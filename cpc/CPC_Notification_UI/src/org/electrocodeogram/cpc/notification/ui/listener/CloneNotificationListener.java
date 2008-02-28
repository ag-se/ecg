package org.electrocodeogram.cpc.notification.ui.listener;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.hub.event.CloneNotificationEvent;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener;
import org.electrocodeogram.cpc.notification.ui.marker.NotificationMarker;


public class CloneNotificationListener implements IEventHubListener
{
	private static Log log = LogFactory.getLog(CloneNotificationListener.class);

	public CloneNotificationListener()
	{
		log.trace("UiCloneNotificationListener()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener#processEvent(org.electrocodeogram.cpc.core.api.hub.event.CPCEvent)
	 */
	@Override
	public void processEvent(CPCEvent event)
	{
		if (event instanceof CloneNotificationEvent)
		{
			processCloneNotificationEvent((CloneNotificationEvent) event);
		}
		else
		{
			log.error("processEvent() - got event of wrong type: " + event, new Throwable());
		}
	}

	private void processCloneNotificationEvent(CloneNotificationEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("procesCloneNotificationEvent() - event: " + event);

		/*
		 * For NOTIFY and WARN events, we add a new problem marker.
		 */
		if (CloneNotificationEvent.Type.NOTIFY.equals(event.getType())
				|| CloneNotificationEvent.Type.WARN.equals(event.getType()))
		{
			log.trace("procesCloneNotificationEvent() - creating a new cpc problem marker.");

			//make sure that there is no old marker for this clone still present
			NotificationMarker.removeMarker(event.getCloneFile(), event.getModifiedClone().getUuid());

			//decide on a type
			NotificationMarker.Type type = NotificationMarker.Type.valueOf(event.getType().toString());

			//decide on a message
			String message = NotificationMarker.buildMarkerMessage(event.getMessage());

			//create the marker
			NotificationMarker.createMarker(event.getCloneFile(), event.getModifiedClone(), type, message);
		}
	}
}
