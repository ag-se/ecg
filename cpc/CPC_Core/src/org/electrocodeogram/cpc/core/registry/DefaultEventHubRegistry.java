package org.electrocodeogram.cpc.core.registry;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubRegistry;
import org.electrocodeogram.cpc.core.api.hub.registry.IManagableEventHubRegistry;


/**
 * Default implementation for {@link IEventHubRegistry}.
 * 
 * @author vw
 */
public class DefaultEventHubRegistry implements IManagableEventHubRegistry
{
	private static Log log = LogFactory.getLog(DefaultEventHubRegistry.class);

	protected static final String EXTENSION_POINT_EVENTHUBLISTENERS = "org.electrocodeogram.cpc.core.eventHubListeners";
	//protected static final long DELAY_RECHECK_INTERVALL = 100;

	/**
	 * Registry for interested <b>synchronous</b> event listeners. Stores a list of subscribed
	 * listener callbacks for each {@link CPCEvent} type.
	 */
	protected Map<Class<? extends CPCEvent>, SortedSet<EventHubListenerDescriptorListenerWrapper>> syncListenerRegistry;

	/**
	 * Registry for interested <b>asynchronous</b> event listeners. Stores a list of subscribed
	 * listener callbacks for each {@link CPCEvent} type.
	 */
	protected Map<Class<? extends CPCEvent>, SortedSet<EventHubListenerDescriptorListenerWrapper>> aSyncListenerRegistry;

	/**
	 * Queue for all asynchronous events which need dispatching.
	 */
	protected PriorityQueue<AsyncDispatchTask> aSyncEventQueue;
	protected EventHubDispatcherThread backgroundThread;

	/**
	 * Cached value for performance optimisation. Only set to true,
	 * it there are actually any listeners.
	 * <br>
	 * This value is NOT reset to false if all listeners are removed.
	 * But that will not happen during normal use.
	 */
	protected volatile boolean listenersPresent = false;

	public DefaultEventHubRegistry()
	{
		if (log.isTraceEnabled())
			log.trace("DefaultEventHubRegistry()");

		this.syncListenerRegistry = new HashMap<Class<? extends CPCEvent>, SortedSet<EventHubListenerDescriptorListenerWrapper>>();
		this.aSyncListenerRegistry = new HashMap<Class<? extends CPCEvent>, SortedSet<EventHubListenerDescriptorListenerWrapper>>();

		this.aSyncEventQueue = new PriorityQueue<AsyncDispatchTask>(20);
		this.backgroundThread = new EventHubDispatcherThread();
		this.backgroundThread.start();

		osgiListenerInitialisation();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.hub.registry.IEventHubRegistry#dispatch(org.electrocodeogram.cpc.core.api.hub.event.CPCEvent)
	 */
	@Override
	public void dispatch(CPCEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("dispatch(): " + event);
		assert (event != null);

		//first seal the event
		event.seal();

		//make sure the event was fully initialised
		if (!event.isValid())
		{
			log.error("dispatch() - trying to sent non-valid event: " + event, new Throwable());
			return;
		}

		//dispatch the event to all interested listeners
		//we only need to do this if we actually have any listeners registered 
		if (listenersPresent)
			dispatchListeners(event);
		else
			log.trace("dispatch() - no listeners present");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.hub.registry.IEventHubRegistry#subscribe(java.lang.Class, boolean, org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener)
	 */
	@Override
	public void subscribe(Class<? extends CPCEvent> eventType, boolean synchronous, byte priority,
			IEventHubListener listener)
	{
		if (log.isTraceEnabled())
			log.trace("subscribe() - eventType: " + eventType + ", priority: " + priority + ", listener: " + listener);
		assert (eventType != null && listener != null);

		subscribe(eventType, synchronous, priority, new EventHubListenerDescriptor(eventType, listener));
	}

	protected void subscribe(Class<? extends CPCEvent> eventType, boolean synchronous, byte priority,
			EventHubListenerDescriptor descriptor)
	{
		if (log.isTraceEnabled())
			log.trace("subscribe() - eventType: " + eventType + ", synchronous: " + synchronous + ", priority: "
					+ priority + ", descriptor: " + descriptor);
		assert (eventType != null && descriptor != null);

		Map<Class<? extends CPCEvent>, SortedSet<EventHubListenerDescriptorListenerWrapper>> registry;
		if (synchronous)
			registry = syncListenerRegistry;
		else
			registry = aSyncListenerRegistry;

		//lock listener registry while we add the new listener
		synchronized (registry)
		{
			//get existing listener list, if available
			SortedSet<EventHubListenerDescriptorListenerWrapper> listeners = registry.get(eventType);

			//create a new list if it doesn't exist yet
			if (listeners == null)
			{
				listeners = new TreeSet<EventHubListenerDescriptorListenerWrapper>();
				//register list
				registry.put(eventType, listeners);
			}

			//add the new listener
			listeners.add(new EventHubListenerDescriptorListenerWrapper(priority, descriptor));

			//we now have listeners
			listenersPresent = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.hub.registry.IEventHubRegistry#unsubscribe(java.lang.Class, boolean, org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener)
	 */
	@Override
	public boolean unsubscribe(Class<? extends CPCEvent> eventType, boolean synchronous, IEventHubListener listener)
	{
		if (log.isTraceEnabled())
			log.trace("unsubscribe() - eventType: " + eventType + ", listener: " + listener);
		assert (eventType != null && listener != null);

		Map<Class<? extends CPCEvent>, SortedSet<EventHubListenerDescriptorListenerWrapper>> registry;
		if (synchronous)
			registry = syncListenerRegistry;
		else
			registry = aSyncListenerRegistry;

		//lock listener registry while we remove the listener
		synchronized (registry)
		{
			//get existing listener list, if available
			SortedSet<EventHubListenerDescriptorListenerWrapper> listeners = registry.get(eventType);

			//if we don't have any listeners for that type, we're done.
			if (listeners == null)
			{
				log.trace("unsubscribe() - no listeners registered for this type");
				return false;
			}

			//remove the listener, if it is part of the set
			return listeners.remove(new EventHubListenerDescriptorListenerWrapper((byte) 0,
					new EventHubListenerDescriptor(eventType, listener)));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.hub.registry.IManagableEventHubRegistry#shutdown()
	 */
	@Override
	public void shutdown()
	{
		log.trace("shutdown()");

		//stop the background dispatcher
		backgroundThread.shutdown();
	}

	/* 
	 * INTERNAL 
	 */

	/**
	 * Sends a {@link CPCEvent} to all interested listeners who have subscribed
	 * for that specific event type.
	 * 
	 * @param event the event to dispatch, never null
	 */
	protected void dispatchListeners(CPCEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("dispatchListeners() - event: " + event);
		assert (event != null);

		/*
		 * First dispatch the event to synchronous listeners.
		 * (They might be updating some data structures which the async listeners will need.)
		 */

		//generate a set of synchronous listeners who are interested in this event
		List<IEventHubListener> interestedSyncListeners = collectInterestedListeners(syncListenerRegistry, event);

		if (!interestedSyncListeners.isEmpty())
		{
			if (log.isTraceEnabled())
				log.trace("dispatchListeners() - queueing event for " + interestedSyncListeners.size()
						+ " synchronous listeners.");

			// now dispatch the event to all interested listeners
			for (IEventHubListener listener : interestedSyncListeners)
			{
				if (log.isTraceEnabled())
					log.trace("dispatchListeners() - notifying listener - listener: " + listener + ", event: " + event);

				try
				{
					listener.processEvent(event);
				}
				catch (ThreadDeath e)
				{
					throw e;
				}
				catch (Exception e)
				{
					log.error("dispatchListeners() - execution of listener failed - listener: " + listener
							+ ", event: " + event + " - " + e, e);
				}
			}
		}

		/*
		 * Now enqueue event for asynchronous listeners.
		 */

		//generate a set of listeners who are interested in this event
		List<IEventHubListener> interestedASyncListeners = collectInterestedListeners(aSyncListenerRegistry, event);

		if (!interestedASyncListeners.isEmpty())
		{
			if (log.isTraceEnabled())
				log.trace("dispatchListeners() - queueing event for " + interestedASyncListeners.size()
						+ " asynchronous listeners.");

			synchronized (aSyncEventQueue)
			{
				aSyncEventQueue.add(new AsyncDispatchTask(event, interestedASyncListeners));

				//wake up the dispatching thread, if it is waiting atm
				aSyncEventQueue.notifyAll();
			}
		}

	}

	/**
	 * Collects a list of listeners from the given listener registry which are interested
	 * in events of the given event type.
	 * <br>
	 * This method is thread safe.
	 * 
	 * @param event
	 * @return a list of interested listeners, ordered in descending order according to priority, never null.
	 */
	@SuppressWarnings("unchecked")
	protected List<IEventHubListener> collectInterestedListeners(
			Map<Class<? extends CPCEvent>, SortedSet<EventHubListenerDescriptorListenerWrapper>> registry,
			CPCEvent event)
	{
		SortedSet<EventHubListenerDescriptorListenerWrapper> interestedListeners = new TreeSet<EventHubListenerDescriptorListenerWrapper>();

		//make sure that no listeners are added/removed while we generate the listener list
		synchronized (registry)
		{
			//collect all listeners which have subscribed for exactly this type of event
			SortedSet<EventHubListenerDescriptorListenerWrapper> directListeners = registry.get(event.getClass());
			if (directListeners != null)
			{
				if (log.isTraceEnabled())
					log.trace("collectInterestedListeners() - directListeners: " + directListeners);

				//IEventHubListener listener = null;
				for (EventHubListenerDescriptorListenerWrapper desciptorWrapper : directListeners)
				{
					//					listener = desciptor.getListener();
					//					if (listener != null)
					//						interestedListeners.add(listener);
					interestedListeners.add(desciptorWrapper);
				}
			}

			//now check for listeners which have subscribed for one of the superclasses of
			//this class
			//go through all superclasses till we reach MicroActivityEvent
			Class superClass = event.getClass().getSuperclass();
			do
			{
				SortedSet<EventHubListenerDescriptorListenerWrapper> superListeners = registry.get(superClass);
				if (superListeners != null)
				{
					if (log.isTraceEnabled())
						log.trace("collectInterestedListeners() - superListeners: " + superListeners + " - for: "
								+ superClass);

					//IEventHubListener listener = null;
					for (EventHubListenerDescriptorListenerWrapper desciptorWrapper : superListeners)
					{
						//						listener = desciptor.getListener();
						//						if (listener != null)
						//							interestedListeners.add(listener);
						interestedListeners.add(desciptorWrapper);
					}
				}
				superClass = superClass.getSuperclass();
			}
			while ((superClass != null) && (!CPCEvent.class.equals(superClass)));
		}

		//now build the result list
		List<IEventHubListener> result = new ArrayList<IEventHubListener>(interestedListeners.size());

		IEventHubListener listener = null;
		for (EventHubListenerDescriptorListenerWrapper desciptorWrapper : interestedListeners)
		{
			listener = desciptorWrapper.getDescriptor().getListener();
			if (listener != null)
				result.add(listener);
		}

		// we now got all listeners
		if (log.isTraceEnabled())
			log.trace("collectInterestedListeners() - result: " + result);

		return result;
	}

	/**
	 * Reads data for all extensions which were registered for the <em>eventHubListeners</em> extension point and
	 * converts them into the internal data format of the event hub registry.
	 */
	/*
	 * TODO: depending on the performance we might actually always do this and skip the
	 * 		internal storage part.
	 * 		This would give dynamically loaded/unloaded plugins a chance to interface with CPC.
	 */
	@SuppressWarnings("unchecked")
	protected void osgiListenerInitialisation()
	{
		log.trace("osgiListenerInitialisation() - building provider registry from extension data.");

		IConfigurationElement[] extensions = Platform.getExtensionRegistry().getConfigurationElementsFor(
				EXTENSION_POINT_EVENTHUBLISTENERS);
		for (IConfigurationElement element : extensions)
		{
			try
			{
				EventHubListenerDescriptor descriptor = new EventHubListenerDescriptor(element);

				//now process all sub-elements to get the event type class names
				for (IConfigurationElement subElement : element.getChildren())
				{

					/*
					 * FIXME: / TODO: there should be a better way to do this
					 * 
					 * This is somewhat tricky now. In order to be able to make use of the CPCEvent class
					 * hierarchy for listener subscriptions we need to resolve the given eventTypeClass
					 * (a sub-class of CPCEvent).
					 * However, it is unclear were that class is located. It might be:
					 * a) a CPC Core base event class.
					 * b) an event class which is located in the plugin which registered the listener.
					 * c) an event class which is located elsewhere (in one of the plugins, which the
					 *    listener plugin depends on)
					 * 
					 * Possible approaches:
					 * a) Class.forName()
					 * b) element.createExecutableExtension()
					 * c) ???
					 * 		FIXME: c) is not supported at the moment. A plugin in such a situation would need
					 * 				to register its listener programatically.
					 */
					Class<?> eventTypeClass = null;
					try
					{
						eventTypeClass = Class.forName(subElement.getAttribute("class"));
					}
					catch (ClassNotFoundException e)
					{
						//ok, it doesn't seem to be a CPC Core base class, try the plugins class loader

						//for c) this will throw an exception and the listener registration will fail
						CPCEvent cpcEventType = (CPCEvent) subElement.createExecutableExtension("class");
						eventTypeClass = cpcEventType.getClass();
					}

					boolean synchronous = false;
					if (subElement.getAttribute("synchronous") != null
							&& Boolean.parseBoolean(subElement.getAttribute("synchronous")))
						synchronous = true;

					byte priority = 0;
					if (subElement.getAttribute("priority") != null)
						priority = Byte.parseByte(subElement.getAttribute("priority"));

					subscribe((Class<? extends CPCEvent>) eventTypeClass, synchronous, priority, descriptor);
				}
			}
			catch (Exception e)
			{
				log.error("osgiListenerInitialisation() - registration of listener failed - class: "
						+ element.getAttribute("class") + ", element: " + element + " - " + e, e);
			}
		}
	}

	/**
	 * A wrapper class which bundles a pending {@link CPCEvent} which still needs to be dispatched
	 * and a list of {@link IEventHubListener}s which are interested in the event. 
	 */
	protected class AsyncDispatchTask implements Comparable<AsyncDispatchTask>
	{
		private CPCEvent event;
		private List<IEventHubListener> interestedListeners;

		/**
		 * 
		 * @param event never null
		 * @param interestedListeners {@link IEventHubListener}s interested in this event, ordered descending
		 * 		according to priority, never null.
		 */
		public AsyncDispatchTask(CPCEvent event, List<IEventHubListener> interestedListeners)
		{
			assert (event != null && interestedListeners != null);

			this.event = event;
			this.interestedListeners = interestedListeners;
		}

		/**
		 * 
		 * @return never null
		 */
		public CPCEvent getEvent()
		{
			return event;
		}

		/**
		 * @return {@link IEventHubListener}s interested in this event, ordered descending
		 * 		according to priority, never null.
		 */
		public List<IEventHubListener> getInterestedListeners()
		{
			return interestedListeners;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			return event.hashCode();
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			return event.equals(((AsyncDispatchTask) obj).event);
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(AsyncDispatchTask o)
		{
			return event.compareTo(o.event);
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "AsyncDispatchTask[event: " + event + ", listeners: " + interestedListeners + "]";
		}
	}

	/**
	 * Internal event dispatching thread which listens for new additions to the
	 * {@link DefaultEventHubRegistry#aSyncEventQueue} and automatically dispatches
	 * them in the background. 
	 */
	protected class EventHubDispatcherThread extends Thread
	{
		private final Log log;

		private boolean running = true;

		public EventHubDispatcherThread()
		{
			super("CPCEventHubDispatcherThread");

			log = LogFactory.getLog(EventHubDispatcherThread.class);
			log.trace("EventHubDispatcherThread()");
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run()
		{
			log.trace("run() - starting.");

			//we're doing this forever
			while (running)
			{
				AsyncDispatchTask task = null;

				/*
				 * NOTE: it is important that we relinquish the lock on the
				 * event queue before we start to dispatch a specific event!
				 */

				//check if there is anything in queue
				synchronized (aSyncEventQueue)
				{
					log.trace("run() - " + aSyncEventQueue.size() + " entries in queue.");
					//check if there is a waiting dispatching task
					task = aSyncEventQueue.poll();
				}

				//if there is, do something!
				if (task != null)
				{
					//dispatch the event
					if (log.isTraceEnabled())
						log.trace("run() - dispatching queued event - task: " + task);

					for (IEventHubListener listener : task.getInterestedListeners())
					{
						if (log.isTraceEnabled())
							log.trace("run() - notifying listener - listener: " + listener + ", event: "
									+ task.getEvent());

						try
						{
							listener.processEvent(task.getEvent());
						}
						catch (ThreadDeath e)
						{
							throw e;
						}
						catch (Exception e)
						{
							log.error("dispatchListeners() - execution of listener failed - listener: " + listener
									+ ", event: " + task.getEvent() + " - " + e, e);
						}
					}
				}

				synchronized (aSyncEventQueue)
				{
					if (aSyncEventQueue.isEmpty())
					{
						//wait for the queue to be filled up again
						try
						{
							log.trace("run() - waiting for new asynchronous events.");
							aSyncEventQueue.wait();
						}
						catch (InterruptedException e)
						{
							log.info("run() - interrupted while waiting for queue to fill.");
						}
					}
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

	/**
	 * A simple wrapper class which bundles an {@link EventHubListenerDescriptor} instance together
	 * with the priority for the corresponding event type.
	 * <p>
	 * <b>NOTE:</b> this class has a natural ordering that is inconsistent with equals.
	 */
	protected class EventHubListenerDescriptorListenerWrapper implements
			Comparable<EventHubListenerDescriptorListenerWrapper>
	{
		private byte priority;
		private EventHubListenerDescriptor descriptor;

		/**
		 * 
		 * @param priority
		 * @param descriptor never null
		 */
		public EventHubListenerDescriptorListenerWrapper(byte priority, EventHubListenerDescriptor descriptor)
		{
			assert (descriptor != null);
			this.priority = priority;
			this.descriptor = descriptor;
		}

		public byte getPriority()
		{
			return priority;
		}

		/**
		 * @return never null
		 */
		public EventHubListenerDescriptor getDescriptor()
		{
			return descriptor;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object other)
		{
			return descriptor.equals(other);
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			return descriptor.hashCode();
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(EventHubListenerDescriptorListenerWrapper o)
		{
			//return this.priority - o.priority;
			//descending order
			int result = o.priority - this.priority;
			if (result != 0)
				return result;

			//if the priority is equal, descriptors are sorted by class name
			return descriptor.getListenerClass().compareTo(o.descriptor.getListenerClass());
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "[pri: " + priority + ", descr: " + descriptor + "]";
		}
	}
}
