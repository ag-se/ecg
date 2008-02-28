package org.electrocodeogram.cpc.core.api.hub.registry;


import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.registry.DefaultEventHubRegistry;


/**
 * The central event dispatcher for {@link CPCEvent}s, a key component of the CPC Framework.
 * Each CPC subsystem can send events which may be of interest to other subsystems
 * to this event hub. They are then dispatched to all interested parties.
 * <p>
 * <b>Events are dispatched synchronously and asynchronously</b>, depending on the preferences of the subscribed listeners.
 * <p>
 * Programmatical subscription/unsubscription for arbitrary {@link CPCEvent} types is handled by the <em>subscribe</em> and
 * <em>unsubscribe</em> methods. Other modules interested in dynamically modifying their subscriptions should use these
 * methods to subscribe/unsubscribe for the event types they are interested in.
 * <p>
 * However, the <b>recommended</b> way for static subscription is the <em>eventHubListeners</em> extension point of the
 * <em>CPC Core</em> module.
 * <p>
 * A reference to the currently active {@link IEventHubRegistry} instance can be obtained via
 * {@link CPCCorePlugin#getEventHubRegistry()}.
 * <p>
 * <b>NOTE:</b> Any implementation of this interface also needs to implement {@link IManagableEventHubRegistry}.
 * 
 * @author vw
 * 
 * @see CPCCorePlugin#getEventHubRegistry()
 * @see IManagableEventHubRegistry
 * @see DefaultEventHubRegistry
 * @see IEventHubListener
 * @see CPCEvent
 */
public interface IEventHubRegistry
{
	/**
	 * Registers a listener callback to receive {@link CPCEvent} notifications. The class hierarchy is taken
	 * into account, i.e.
	 * <br>
	 * <code>subscribe(CPCEvent.class, this);</code>
	 * <br>
	 * will subscribe to all events.
	 * <br>
	 * If a specific event class is provided, then the listener will only be registered for that event type.
	 * 
	 * @param eventType the {@link CPCEvent} subclass for which the listener should receive notifications, never null.
	 * @param synchronous <em>true</em> if this listener expects events to be dispatched in a synchronous fashion.
	 * 		In this case the sender of the event will be blocked until all synchronous listeners have processed
	 * 		the event. If this is <em>false</em> the events are dispatched in a separate background thread.
	 * @param priority sorting attribute which affects the order in which multiple listeners registered for the same
	 * 		event type will be notified. Listeners are called in descending order of their priority. If multiple
	 * 		listeners have the same priority, the order in not specified.<br/>
	 * 		Values may be negative, a good default value is <em>0</em>.
	 * @param listener the listener callback, never null.
	 * 
	 * @deprecated It is highly recommended to use the <em>eventHubListeners</em> extension point to register
	 * 		listeners.
	 */
	@Deprecated
	public void subscribe(Class<? extends CPCEvent> eventType, boolean synchronous, byte priority,
			IEventHubListener listener);

	/**
	 * Unregisters a {@link CPCEvent} listener callback.
	 * <br>
	 * A programmatically subscribed listener, should always be unsubscribed once it is no longer needed.
	 * <br>
	 * Unsubscribing of listeners which were registered via the <em>eventHubListeners</em> extension point is not required.
	 * 
	 * @param eventType the {@link CPCEvent} subclass for which the listener was originally registered, never null
	 * @param synchronous should be set to the same value as was used during subscription of this listener.
	 * @param listener the listener to remove, never null
	 * @return true if the listener was registered, false if listener was unknown
	 */
	public boolean unsubscribe(Class<? extends CPCEvent> eventType, boolean synchronous, IEventHubListener listener);

	/**
	 * Dispatch the event to all interested parties.
	 * <p>
	 * The event needs to be <em>valid</em>, otherwise an error is logged and the event is ignored.
	 * <br>
	 * Events are automatically <em>sealed</em> once they are passed to this method.
	 * <br>
	 * It is up to the registered listeners whether this event is dispatched synchronously, asynchronously
	 * or both.
	 * <br>
	 * The caller is guaranteed to be shielded from any potential exceptions thrown by any of the listeners.
	 * An exception thrown by one listener will not affect other listeners.
	 * <p>
	 * The caller should try to release as many locks as possible prior to calling this method as
	 * the focus might be passed to some long running synchronous listener. If a caller absolutely must not
	 * be blocked by the event dispatching process, it should move the call to this method into a separate
	 * background thread. However, even in that case some of the then concurrently executed listeners may
	 * contend for the main UI thread.
	 * <br>
	 * Calling this method from the background event dispatching thread is explicitly permitted.
	 * The event dispatching order to asynchronous listeners will be correctly maintained.
	 * <p>
	 * An {@link IStoreProvider} exclusive write lock may only be held during event dispatching, if the
	 * specification of the {@link CPCEvent} specifically states this fact. In all other cases an event
	 * generating thread will have to queue events internally and release the {@link IStoreProvider}
	 * lock before actually dispatching them with this method.
	 * 
	 * @param event the event to dispatch, never null
	 * 
	 * @see CPCEvent#isValid()
	 * @see CPCEvent#seal()
	 * @see IEventHubListener
	 */
	public void dispatch(CPCEvent event);
}
