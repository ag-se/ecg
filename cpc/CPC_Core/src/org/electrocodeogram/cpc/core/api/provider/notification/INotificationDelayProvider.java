package org.electrocodeogram.cpc.core.api.provider.notification;


import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.hub.event.CloneNotificationEvent;
import org.electrocodeogram.cpc.core.api.provider.IProvider;


/**
 * A notification delay provider takes {@link CloneNotificationEvent}s and queues them according to some internal
 * criteria. These {@link CloneNotificationEvent}s are then reexamined if certain conditions are met and are either
 * retransmitted as new {@link CloneNotificationEvent}s or discarded.
 * <br>
 * The {@link INotificationDelayProvider} interface is implemented by all notification delay providers.
 * <p>
 * A typical implementation approach would be to queue {@link CloneNotificationEvent}s and to reexamine each modified
 * clone once:
 * <ul>
 * 	<li>the corresponding file was closed</li>
 * 	<li>a specific amount of time has elapsed since the last modification on or near the clone</li>
 * </ul>
 * <br/>
 * This provider type typically creates an internal background thread.
 * <p>
 * Usage Example:
 * <br>
 * Providers of this type are needed to support delayed notification of the user about some potential
 * update anomaly. I.e. the user might still continue to modify code and might also modify the other group
 * members of the modified clone. Displaying a warning right away might lead to superfluous warnings
 * if the programmer is already well aware about the other clone instances. 
 * 
 * @author vw
 * 
 * @see INotificationEvaluationProvider
 * @see IEvaluationResult
 * @see CloneNotificationEvent
 */
public interface INotificationDelayProvider extends IProvider
{
	/**
	 * Takes an {@link IEvaluationResult} as {@link CloneNotificationEvent} and internally queues it for a
	 * specific time or until a specific condition arises. The corresponding {@link CloneNotificationEvent}
	 * is then reexamined and either dispatched as new event or discarded.
	 * <p>
	 * Only one {@link CloneNotificationEvent} per {@link IClone} is queued. A new {@link CloneNotificationEvent}
	 * will replace any existing {@link CloneNotificationEvent}s for the same {@link IClone}.
	 * <p>
	 * This method returns right away. The checking, re-examination and potential re-dispatching of the event
	 * is done in a background thread or job. 
	 * <p>
	 * The remaining aspects, especially the criteria used for delaying and re-dispatching, are unspecified
	 * and are likely to vary from implementation to implementation.
	 * 
	 * @param cloneNotificationEvent the {@link CloneNotificationEvent} to queue, never null.
	 * 		The event has to be of type {@link CloneNotificationEvent.Type#DELAY_NOTIFY} or
	 * 		{@link CloneNotificationEvent.Type#DELAY_WARN}. All other types are not permitted.
	 */
	public void enqueueNotification(CloneNotificationEvent cloneNotificationEvent);
}
