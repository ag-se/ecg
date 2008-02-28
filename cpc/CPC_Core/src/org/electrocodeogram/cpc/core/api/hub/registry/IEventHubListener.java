package org.electrocodeogram.cpc.core.api.hub.registry;


import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;


/**
 * This interface is to be implemented by listeners who want to register callbacks with the {@link IEventHubRegistry}
 * of the {@link CPCCorePlugin} in order to receive {@link CPCEvent} notifications.
 * 
 * @author vw
 * 
 * @see IEventHubRegistry
 * @see CPCEvent
 */
//TODO: add usage example
public interface IEventHubListener
{
	/**
	 * The callback function which will be called with a {@link CPCEvent} if this
	 * listener has subscribed for that type of event.
	 * <br>
	 * A listener is guaranteed to only receive events of the types which it
	 * has subscribed for.
	 * <p>
	 * A listener <b>MUST NOT</b> modify any of the data contained in an event object.
	 * The same event object is reused for all registered listeners. A listener must
	 * not keep a reference to the event object beyond the duration of this call.
	 * <br>
	 * For performance reasons references to the contents of certain events may be
	 * retained (to avoid unnecessary cloning).
	 * <p>
	 * If multiple listeners have registered for the same event type, listeners are notified
	 * in descending order of their priority. If multiple listeners have the same priority,
	 * the order of notification is not specified.
	 * <br>
	 * Synchronous listeners are notified first. Asynchronous dispatching is delay till
	 * the last synchronous listener finished the processing of the event.
	 * <p>
	 * Depending on the kind of subscription events are dispatched either synchronously
	 * or asynchronously.
	 * <p>
	 * If synchronous dispatching was requested events are dispatched synchronous to the
	 * thread which generated the event. This means that the generator of this event is
	 * blocked until processing of the event has been finished. A listener should thus
	 * dispatch any long running work in a separate background job, as it might be
	 * blocking the main UI thread.
	 * <br>
	 * Another aspect of synchronous dispatching is that if multiple threads are
	 * dispatching events, a listener may be executed concurrently with different events.
	 * <p>
	 * In asynchronous dispatching mode all events are dispatched from special background
	 * dispatching threads. A listener does not have to be thread safe in this mode unless
	 * it especially set the <em>threadsafe</em> parameter to <em>true</em> when registering
	 * the listener callback object. Listeners which claimed to be thread safe may receive
	 * asynchronous events concurrently from different background dispatching threads.
	 * It is up to the {@link IEventHubRegistry} implementation whether to make use of
	 * multiple dispatching threads or not.
	 * <br>
	 * Very long running tasks should be executed as background jobs even in this mode. Otherwise
	 * other listeners might starve.
	 * <p>
	 * <b>IMPORTANT NOTE:</b> In synchronous mode events are dispatched in order of arrival, but
	 * new synchronous events may interrupt the normal sequence of events. This means that
	 * if any of the registered listeners generates new events of the same type,
	 * events may be delivered out of order.
	 * <br>
	 * If possible a listener should try to avoid generating events of its own type.
	 * Repeated generation of such events may lead to endless loops and starvation of
	 * other listeners.
	 * <br>
	 * It is the responsibility of the listener to ensure that this handled correctly.
	 * <p>
	 * In the asynchronous dispatching mode a new asynchronous event will not be processed
	 * until all listeners for the last event were successfully executed. A new event can
	 * thus not force its way in between as is possible with synchronous events.
	 * <p>
	 * In general asynchronous dispatching should be used whenever possible.
	 * <p>
	 * Synchronous listeners should take careful note of the locks which the event generator
	 * might be holding. There may be situations which can easily lead into a deadlock
	 * scenario if a listener behaves incorrectly.
	 * <br>
	 * Actions which require synchronisation with the main UI thread tend to be
	 * especially dangerous.
	 * 
	 * @param event a new event which the listener may process, never null.
	 */
	public void processEvent(CPCEvent event);
}
