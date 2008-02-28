package org.electrocodeogram.cpc.notification.listener;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.hub.event.CloneNotificationEvent;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener;
import org.electrocodeogram.cpc.core.api.provider.notification.INotificationDelayProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;


/**
 * Listens for {@link CloneNotificationEvent}s of type {@link CloneNotificationEvent.Type#DELAY_NOTIFY} and
 * {@link CloneNotificationEvent.Type#DELAY_WARN} which are usually created by the
 * {@link CloneModificationListener}.<br/>
 * <br/>
 * The corresponding notifications are then sent to the {@link INotificationDelayProvider} which will
 * enqueue them for later re-checking and conditional retransmission.
 * 
 * @author vw
 * 
 * @see CloneNotificationEvent
 * @see CloneModificationListener
 * @see INotificationDelayProvider
 */
public class CloneNotificationListener implements IEventHubListener
{
	private static Log log = LogFactory.getLog(CloneNotificationListener.class);

	private IStoreProvider storeProvider;
	private INotificationDelayProvider notificationDelayProvider;

	public CloneNotificationListener()
	{
		log.trace("CloneNotificationListener()");

		storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(IStoreProvider.class);
		assert (storeProvider != null);

		notificationDelayProvider = (INotificationDelayProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				INotificationDelayProvider.class);
		assert (notificationDelayProvider != null);
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
			log.trace("processCloneNotificationEvent() - event: " + event);

		/*
		 * For DELAY_NOTIFY and DELAY_WARN we enqueue the event with the notification delay provider.
		 */
		if (CloneNotificationEvent.Type.DELAY_NOTIFY.equals(event.getType())
				|| CloneNotificationEvent.Type.DELAY_WARN.equals(event.getType()))
		{
			log.trace("processCloneNotificationEvent() - enqueing event with notification delay provider.");

			//enqueue the event
			notificationDelayProvider.enqueueNotification(event);
		}
	}
}
