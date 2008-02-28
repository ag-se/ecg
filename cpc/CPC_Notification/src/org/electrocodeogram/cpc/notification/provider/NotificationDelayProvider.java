package org.electrocodeogram.cpc.notification.provider;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.hub.event.CloneNotificationEvent;
import org.electrocodeogram.cpc.core.api.hub.event.EclipseFileAccessEvent;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener;
import org.electrocodeogram.cpc.core.api.provider.IManagableProvider;
import org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult;
import org.electrocodeogram.cpc.core.api.provider.notification.INotificationDelayProvider;
import org.electrocodeogram.cpc.core.api.provider.notification.INotificationEvaluationProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.notification.CPCNotificationPlugin;
import org.electrocodeogram.cpc.notification.preferences.CPCPreferenceConstants;
import org.electrocodeogram.cpc.notification.utils.NotificationUtils;


/**
 * Default {@link INotificationDelayProvider} implementation.
 * <p>
 * This is a rather simple implementation which will queue events till:
 * <ul>
 * 	<li>they are older than <em>DELAY_EVENT_TIMEOUT</em></li>
 * 	<li>the corresponding file is closed by the user</li>
 * </ul> 
 * Once either of these conditions is reached, the event is reevaluated by passing it
 * to the {@link NotificationEvaluationProvider} once again.
 * 
 * @author vw
 */
public class NotificationDelayProvider implements INotificationDelayProvider, IManagableProvider, IEventHubListener
{
	private static final Log log = LogFactory.getLog(NotificationDelayProvider.class);

	/**
	 * How often should the queued threads be checked for events which have
	 * reached the configured event timeout?
	 */
	protected static final long DELAY_RECHECK_INTERVALL = 5 * 1000; // every 5 seconds

	protected IStoreProvider storeProvider;
	protected INotificationEvaluationProvider notificationEvaluationProvider;
	protected NotificationDelayProviderThread backgroundThread;

	/**
	 * Simple lookup structure for clone file uuid -&gt; clone uuid lookups. 
	 */
	protected Map<String, Set<String>> cloneFileUuidToCloneUuids;

	/**
	 * Simple lookup structure for clone uuid -&gt; clone notification event lookups.
	 */
	protected Map<String, CloneNotificationEventWrapper> cloneUuidToCloneNotificationEvent;

	/**
	 * The main event queue. Events are ordered by creation timestamp.
	 * <br>
	 * Events are removed from the queue and reevaluated by the
	 * {@link NotificationDelayProviderThread} once they exceed the
	 * {@link CPCPreferenceConstants#PREF_NOTIFICATIONDELAY_DELAYINMINUTES}. 
	 */
	protected PriorityQueue<CloneNotificationEventWrapper> eventQueue;

	/**
	 * Queues new events which still need to be dispatched to the event hub.
	 */
	protected List<CloneNotificationEvent> dispatchQueue;

	public NotificationDelayProvider()
	{
		log.trace("NotificationDelayProvider()");

		cloneFileUuidToCloneUuids = new HashMap<String, Set<String>>(10);
		cloneUuidToCloneNotificationEvent = new HashMap<String, CloneNotificationEventWrapper>(50);
		eventQueue = new PriorityQueue<CloneNotificationEventWrapper>(50);
		dispatchQueue = new LinkedList<CloneNotificationEvent>();
	}

	/*
	 * INotificationDelayProvider
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.notification.INotificationDelayProvider#enqueueNotification(org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult)
	 */
	@Override
	public synchronized void enqueueNotification(CloneNotificationEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("enqueueNotification() - event: " + event);
		assert (event != null && (CloneNotificationEvent.Type.DELAY_NOTIFY.equals(event.getType()) || CloneNotificationEvent.Type.DELAY_WARN
				.equals(event.getType())));

		CloneNotificationEventWrapper eventWrapper = new CloneNotificationEventWrapper(event);

		//add event to queue
		eventQueue.add(eventWrapper);

		/*
		 * Update lookup structure for clone uuid lookups.
		 * There may only be one queued event per clone instance at any given time.
		 * The last queued event, will always replace the previous one.
		 */
		CloneNotificationEventWrapper oldQueuedEvent = cloneUuidToCloneNotificationEvent.get(event.getModifiedClone()
				.getUuid());
		if (oldQueuedEvent != null)
		{
			//make sure the old event is no longer used anywhere.
			oldQueuedEvent.invalidate();
			eventQueue.remove(oldQueuedEvent);
		}
		//store new event
		cloneUuidToCloneNotificationEvent.put(event.getModifiedClone().getUuid(), eventWrapper);

		//update lookup structure for file uuid lookups
		Set<String> cloneUuidsForFile = cloneFileUuidToCloneUuids.get(event.getModifiedClone().getFileUuid());
		if (cloneUuidsForFile == null)
		{
			//first clone for this file, initialise this lookup structure
			cloneUuidsForFile = new HashSet<String>(10);
			cloneFileUuidToCloneUuids.put(event.getModifiedClone().getFileUuid(), cloneUuidsForFile);
		}
		cloneUuidsForFile.add(event.getModifiedClone().getUuid());

		if (log.isTraceEnabled())
			log.trace("enqueueNotification() - current queue length: " + eventQueue.size());
	}

	/*
	 * IProvider
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#getProviderName()
	 */
	@Override
	public String getProviderName()
	{
		return "CPC Notification - Default Notification Delay Provider";
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#onLoad()
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void onLoad()
	{
		log.trace("onLoad()");

		storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(IStoreProvider.class);
		assert (storeProvider != null);
		notificationEvaluationProvider = (INotificationEvaluationProvider) CPCCorePlugin.getProviderRegistry()
				.lookupProvider(INotificationEvaluationProvider.class);
		assert (notificationEvaluationProvider != null);

		//we're interested in file-close actions by the user, register a corresponding listener
		CPCCorePlugin.getEventHubRegistry().subscribe(EclipseFileAccessEvent.class, false, (byte) 0, this);

		//start background thread for periodic checks of queued notficiation events
		backgroundThread = new NotificationDelayProviderThread(this);
		backgroundThread.start();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#onUnload()
	 */
	@Override
	public void onUnload()
	{
		log.trace("onUnload()");

		//cleanup
		CPCCorePlugin.getEventHubRegistry().unsubscribe(EclipseFileAccessEvent.class, false, this);

		//stop background thread
		backgroundThread.shutdown();
	}

	/*
	 * IEventHubListener
	 */

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

	/**
	 * Processes {@link EclipseFileAccessEvent}s in order to detect whether a file
	 * which we have queued notifications for was just closed.
	 */
	protected void processEclipseFileAccessEvent(EclipseFileAccessEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("processEclipseFileAccessEvent() - event: " + event);

		//we're only interested in file close events
		if (!EclipseFileAccessEvent.Type.CLOSED.equals(event.getType()))
		{
			log.trace("processEclipseFileAccessEvent() - ignoring non-CLOSED event.");
			return;
		}

		//get the clone file entry for this file
		ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(event.getProject(), event.getFilePath(), true, true);
		if (cloneFile == null)
		{
			log.warn("processEclipseFileAccessEvent() - unable to identify file, ignoring event - project: "
					+ event.getProject() + ", path: " + event.getFilePath());
			return;
		}

		List<CloneNotificationEventWrapper> eventsToProcess = new LinkedList<CloneNotificationEventWrapper>();
		synchronized (this)
		{

			//check if we're interested in events for this file
			Set<String> cloneUuidsForFile = cloneFileUuidToCloneUuids.get(cloneFile.getUuid());
			if (cloneUuidsForFile == null || cloneUuidsForFile.isEmpty())
			{
				//we don't have any events for this file in our queue, ignore it.
				log.trace("processEclipseFileAccessEvent() - ignoring event for file with no events in queue.");
				return;
			}

			/*
			 * Ok, at this point we know that a file containing clones for which we still have events queued
			 * was just closed by the user. This is a strong indication that the user considers this modification
			 * to be complete.
			 * We should notify the user about the delayed notifications in queue now.
			 */

			for (String cloneUuid : cloneUuidsForFile)
			{
				CloneNotificationEventWrapper queuedNotificationEvent = cloneUuidToCloneNotificationEvent
						.get(cloneUuid);

				if (log.isTraceEnabled())
					log.trace("processEclipseFileAccessEvent() - event needs re-evaluation for clone UUID: "
							+ cloneUuid + ", event: " + queuedNotificationEvent);

				if (queuedNotificationEvent == null)
				{
					//this shouldn't happen. It would be an indication for a concurrent
					//modification of the data structures. However, they're all supposed to only
					//be read/modified within synchronized blocks with the NotificationDelayProvider
					//instance as mutex.
					log
							.error(
									"processEclipseFileAccessEvent() - possible illegal concurrent modification of data structure, no cloneUuidToCloneNotificationEvent entry for cloneUuid: "
											+ cloneUuid, new Throwable());
					continue;
				}

				eventsToProcess.add(queuedNotificationEvent);

				//cleanup
				cloneUuidToCloneNotificationEvent.remove(cloneUuid);
			}

			//cleanup
			cloneUuidsForFile.clear();
			cloneFileUuidToCloneUuids.remove(cloneFile.getUuid());
		}

		if (!eventsToProcess.isEmpty())
		{
			boolean needDispatch = false;

			for (CloneNotificationEventWrapper queuedEvent : eventsToProcess)
			{
				if (log.isTraceEnabled())
					log.trace("processEclipseFileAccessEvent() - going to reevaluate event: " + event);

				if (reevaluateNotification(queuedEvent))
					needDispatch = true;
			}

			if (needDispatch)
				dispatchQueuedEvents();
		}
	}

	/*
	 * Private
	 */

	/**
	 * Takes a queued {@link CloneNotificationEventWrapper} and reevaluates it in order to decide
	 * whether it should be dispatched or discarded.
	 * <br>
	 * This method is called whenever some condition indicated that a queued event has
	 * been delayed long enough.
	 * <p>
	 * This method does not send any events directly (as it might be called from within a synchronized
	 * block). Instead {@link NotificationDelayProvider#dispatchQueuedEvents()} needs to be called
	 * separately.
	 * <p>
	 * <b>IMPORTANT:</b> This method <u>may modify clone data</u> and will thus acquire an <u>exclusive IStoreProvider
	 * 		write lock</u> if needed. This may lead to potential deadlocks if this method is called from within
	 * 		a synchronized block. It is therefore recommended to never call this method from any location
	 * 		which is holding any kind of lock.
	 * 
	 * @param notificationEvent the queued event to reevaluate, never null.
	 * @return true if a new event was created and queued by this method call, this indicates that
	 * 		a call to {@link NotificationDelayProvider#dispatchQueuedEvents()} is required.
	 * 
	 * @see NotificationDelayProvider#dispatchQueuedEvents()
	 */
	protected boolean reevaluateNotification(CloneNotificationEventWrapper notificationEvent)
	{
		if (log.isTraceEnabled())
			log.trace("reevaluateNotification() - notificationEvent: " + notificationEvent);

		IClone modifiedClone = null;
		synchronized (this)
		{
			//ignore any event which was already invalidated/handled
			if (!notificationEvent.isValid())
			{
				log.trace("reevaluateNotification() - event was already invalidated, ignoring.");
				return false;
			}

			//mark the event as invalid/handled
			notificationEvent.invalidate();

			//get the latest copy of the modified clone
			modifiedClone = storeProvider.lookupClone(notificationEvent.getEvent().getModifiedClone().getUuid());
		}

		if (modifiedClone == null)
		{
			log.trace("reevaluateNotification() - clone was concurrently deleted, ignoring.");
			return false;
		}

		IEvaluationResult result = notificationEvaluationProvider.evaluateModification(modifiedClone, null, false);

		//now process the evaluation result
		List<CloneNotificationEvent> newEvents = NotificationUtils.processEvaluationResult(storeProvider,
				notificationEvent.getEvent().getCloneFile(), modifiedClone, result, false);

		synchronized (dispatchQueue)
		{
			dispatchQueue.addAll(newEvents);
		}

		//return true if event list is not empty
		return !(newEvents.isEmpty());
	}

	/**
	 * Dispatches any {@link CloneNotificationEvent}s which were queued by
	 * {@link NotificationDelayProvider#reevaluateNotification(org.electrocodeogram.cpc.notification.provider.NotificationDelayProvider.CloneNotificationEventWrapper)}.
	 */
	protected void dispatchQueuedEvents()
	{
		log.trace("dispatchQueuedEvents()");

		List<CloneNotificationEvent> events = null;

		//acquire queued events inside a synchronized block
		synchronized (dispatchQueue)
		{
			if (dispatchQueue.isEmpty())
			{
				log.trace("dispatchQueuedEvents() - queue is empty.");
				return;
			}

			if (log.isTraceEnabled())
				log.trace("dispatchQueuedEvents() - dispatching " + dispatchQueue.size() + " events.");

			//copy over all queued events
			events = new ArrayList<CloneNotificationEvent>(dispatchQueue);

			dispatchQueue.clear();
		}

		//now dispatch the events outside of any synchronized block
		if (events != null)
		{
			for (CloneNotificationEvent event : events)
			{
				CPCCorePlugin.getEventHubRegistry().dispatch(event);
			}
		}
	}

	/**
	 * Simple wrapper around {@link CloneNotificationEvent}s which keeps track of invalidation of events
	 * and contains some convenience methods.
	 */
	protected class CloneNotificationEventWrapper implements Comparable<CloneNotificationEventWrapper>
	{
		private CloneNotificationEvent event;
		private boolean valid = true;

		public CloneNotificationEventWrapper(CloneNotificationEvent event)
		{
			assert (event != null);

			this.event = event;
		}

		/**
		 * @return true if this event is older than {@link CPCPreferenceConstants#PREF_NOTIFICATIONDELAY_DELAYINMINUTES}
		 * 		or if it is no longer valid.
		 */
		public boolean hasExpired()
		{
			return (System.currentTimeMillis() - event.getCreationTime()) > CPCNotificationPlugin.getDefault()
					.getPluginPreferences().getInt(CPCPreferenceConstants.PREF_NOTIFICATIONDELAY_DELAYINMINUTES) * 60 * 1000;
		}

		/**
		 * @return true if this event has not yet been handled.
		 */
		public boolean isValid()
		{
			return valid;
		}

		/**
		 * Marks this event as handled. This is mainly used for performance reasons in situations
		 * where it is expensive to directly remove the event from the queue.
		 */
		public void invalidate()
		{
			valid = false;
		}

		public CloneNotificationEvent getEvent()
		{
			return event;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "CloneNotificationEventWrapper[valid: " + valid + ", event: " + event + "]";
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(CloneNotificationEventWrapper o)
		{
			return event.compareTo(o.getEvent());
		}

	}

	/**
	 * This background thread will query the event queue every {@link NotificationDelayProvider#DELAY_RECHECK_INTERVALL}
	 * milliseconds to see whether any event has reached its {@link CPCPreferenceConstants#PREF_NOTIFICATIONDELAY_DELAYINMINUTES} time. 
	 */
	protected class NotificationDelayProviderThread extends Thread
	{
		private final Log log = LogFactory.getLog(NotificationDelayProviderThread.class);

		private NotificationDelayProvider notificationDelayProvider;
		private boolean running = true;

		public NotificationDelayProviderThread(NotificationDelayProvider notificationDelayProvider)
		{
			super("CPCNotificationDelayProviderThread");

			log.trace("NotificationDelayProviderThread()");

			this.notificationDelayProvider = notificationDelayProvider;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run()
		{
			log.trace("run() - starting.");

			boolean needDispatch = false;

			List<CloneNotificationEventWrapper> eventsToProcess = new LinkedList<CloneNotificationEventWrapper>();

			//we're doing this forever
			while (running)
			{
				//log.trace("run() - rechecking queued notifications.");

				//the queue is not thread safe, get a lock
				synchronized (notificationDelayProvider)
				{
					while (!notificationDelayProvider.eventQueue.isEmpty()
							&& notificationDelayProvider.eventQueue.peek().hasExpired())
					{
						CloneNotificationEventWrapper event = notificationDelayProvider.eventQueue.poll();
						assert (event != null);

						if (log.isTraceEnabled())
							log.trace("run() - event ready for re-evaluation: " + event);

						if (!event.isValid())
						{
							//this event was already handled
							log.trace("run() - event was already invalidated, ignoring.");
							continue;
						}

						eventsToProcess.add(event);

						/*
						 * Cleanup
						 */
						if (cloneUuidToCloneNotificationEvent
								.containsKey(event.getEvent().getModifiedClone().getUuid()))
						{
							//remove the event
							cloneUuidToCloneNotificationEvent.remove(event.getEvent().getModifiedClone().getUuid());

							//there are no more events for this clone, lets remove the entry in the
							//file uuid->clone uuids lookup structure too
							Set<String> cloneUuids = cloneFileUuidToCloneUuids.get(event.getEvent().getCloneFile()
									.getUuid());
							if (cloneUuids != null && !cloneUuids.isEmpty())
							{
								cloneUuids.remove(event.getEvent().getModifiedClone().getUuid());
							}
							else
							{
								log.error("run() - clone uuid in cache during cleanup for clone file uuid: "
										+ event.getEvent().getCloneFile().getUuid(), new Throwable());
							}
						}
						else
						{
							log.error("run() - event not in cache during cleanup for clone uuid: "
									+ event.getEvent().getModifiedClone().getUuid(), new Throwable());
						}
					}
				}

				if (!eventsToProcess.isEmpty())
				{
					for (CloneNotificationEventWrapper event : eventsToProcess)
					{
						if (log.isTraceEnabled())
							log.trace("run() - re-evaluating event: " + event);

						if (reevaluateNotification(event))
							needDispatch = true;

					}
					eventsToProcess.clear();

					//dispatch any events which we might have created
					if (needDispatch)
					{
						dispatchQueuedEvents();
						needDispatch = false;
					}
				}

				//now sleep a bit then check again
				try
				{
					Thread.sleep(DELAY_RECHECK_INTERVALL);
				}
				catch (InterruptedException e)
				{
					log.trace("run() - sleep interrupted");
				}
			}

			log.trace("run() - exiting.");
		}

		/**
		 * Indicates to this background thread that is should wrap up and
		 * terminate itself.
		 */
		public void shutdown()
		{
			log.trace("shutdown()");

			running = false;
			this.interrupt();
		}
	}
}
