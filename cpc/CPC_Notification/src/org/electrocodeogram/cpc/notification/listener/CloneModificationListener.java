package org.electrocodeogram.cpc.notification.listener;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.hub.event.CloneModificationEvent;
import org.electrocodeogram.cpc.core.api.hub.event.CloneNotificationEvent;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener;
import org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult;
import org.electrocodeogram.cpc.core.api.provider.notification.INotificationEvaluationProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.notification.utils.NotificationUtils;


/**
 * Simple harness for an {@link INotificationEvaluationProvider}.<br/>
 * <br/>
 * Listens for {@link CloneModificationEvent}s, checks them for clone content
 * modifications and delegates any such modifications to the notification evaluation
 * provider.<br/>
 * Clone additions are also handed to the notification eval. provider.<br/>
 * <br/>
 * Generates {@link CloneNotificationEvent}s based on the results returned by the
 * notification evaluation provider. 
 * 
 * @author vw
 * 
 * @see INotificationEvaluationProvider
 * @see CloneModificationEvent
 * @see CloneNotificationEvent
 */
public class CloneModificationListener implements IEventHubListener
{
	private static Log log = LogFactory.getLog(CloneModificationListener.class);

	private IStoreProvider storeProvider;
	private INotificationEvaluationProvider notificationEvaluationProvider;

	public CloneModificationListener()
	{
		log.trace("CloneModificationListener()");

		storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(IStoreProvider.class);
		assert (storeProvider != null);

		notificationEvaluationProvider = (INotificationEvaluationProvider) CPCCorePlugin.getProviderRegistry()
				.lookupProvider(INotificationEvaluationProvider.class);
		assert (notificationEvaluationProvider != null);
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

	private void processCloneModificationEvent(CloneModificationEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("procesCloneModificationEvent() - event: " + event);

		//check if this event represents a clone content modification
		if ((event.getModifiedClones() == null || event.getModifiedClones().isEmpty())
				&& (event.getAddedClones() == null || event.getAddedClones().isEmpty()))
		{
			log.trace("procesCloneModificationEvent() - event contained no modified or added clones, ignoring.");
			return;
		}

		List<CloneNotificationEvent> eventQueue = new LinkedList<CloneNotificationEvent>();

		/*
		 * TODO: if this part proves to be too time consuming, we could delegate just queue the event here
		 * and delegate all the evaluation work to a background thread.  
		 */

		int maxLen = (event.getModifiedClones() != null ? event.getModifiedClones().size() : 0)
				+ (event.getAddedClones() != null ? event.getAddedClones().size() : 0);
		List<IClone> clonesOfInterest = new ArrayList<IClone>(maxLen);
		if (event.getModifiedClones() != null)
			clonesOfInterest.addAll(event.getModifiedClones());
		if (event.getAddedClones() != null)
			clonesOfInterest.addAll(event.getAddedClones());

		//handle each clone individually
		for (IClone clone : clonesOfInterest)
		{
			if (log.isTraceEnabled())
				log.trace("procesCloneModificationEvent() - evaluating: " + clone);

			/*
			 * TODO: decide whether we should re-classify the modified clone here.
			 * 
			 * This may not be an easy choice though. On one hand the clone might have been
			 * modified to an extend which makes its original classification useless but on
			 * the other hand the original classification might give us an indication
			 * about the type of relation of the clone to its group members.
			 * 
			 * I.e. if the original classification was TEMPLATE a reclassification might no
			 * longer assign that classification as modifications made in the mean time might
			 * not support it. But the relation to the other members of the clone group might
			 * still warrant the clone as being handled like a TEMPLATE.
			 * 
			 * However, changes made to multiple members of the clone group over time might
			 * well introduce meaningful new classifications.
			 */

			//do a quick check to ensure that there is actually anything worth evaluating.
			//stand alone clones are of no interest to us.
			if (clone.getGroupUuid() == null)
			{
				log.trace("procesCloneModificationEvent() - skipping stand alone clone.");
				continue;
			}

			//delegate the application of specific evaluation strategies to the notification evaluation provider
			IEvaluationResult result = notificationEvaluationProvider.evaluateModification(clone, null, true);

			if (log.isTraceEnabled())
				log.trace("procesCloneModificationEvent() - evaluation result: " + result);

			//now process the evaluation result
			List<CloneNotificationEvent> newEvents = NotificationUtils.processEvaluationResult(storeProvider, event
					.getCloneFile(), clone, result, true);
			eventQueue.addAll(newEvents);
		}

		//if we have generated any notification events above, dispatch them now.
		if (!eventQueue.isEmpty())
		{
			log.trace("procesCloneModificationEvent() - dispatching " + eventQueue.size() + " notification events.");

			for (CloneNotificationEvent newEvent : eventQueue)
				CPCCorePlugin.getEventHubRegistry().dispatch(newEvent);
		}
	}
}
