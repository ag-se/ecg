package org.eclipse.team.core.subscribers;


/**
 * Existing class, this file just lists additional methods.
 */
public class Subscriber
{
	/**
	 * Adds the given listener for the specified operation events to this
	 * subscriber. Has no effect if an identical listener is already registered
	 * for these events. After completion of this method, the given listener
	 * will be registered for exactly the specified events. If they were
	 * previously registered for other events, they will be de-registered.
	 * <p>
	 * Once registered, a listener starts receiving notification of subscriber
	 * operations which affect resources in the workspace. The
	 * listener continues to receive notifications until it is replaced or
	 * removed.
	 * <p>
	 * Listeners can listen for several types of events as defined in
	 * <code>ISubscriberOperationEvent</code>. Clients are free to register for
	 * any number of event types however if they register for more than one, it
	 * is their responsibility to ensure they correctly handle the case where
	 * the same resource shows up in multiple notifications. Clients are
	 * guaranteed to receive only the events for which they are registered.
	 * 
	 * @param listener the listener to register, never null.
	 * @param eventMask the bit-wise OR of all event types of interest to the
	 * listener
	 * 
	 * @see ISubscriberOperationEvent#PRE_OPERATION_PRELIMINARY
	 * @see ISubscriberOperationEvent#PRE_OPERATION
	 * @see ISubscriberOperationEvent#POST_OPERATION
	 * @since 3.x
	 */
	public void addOperationListener(ISubscriberOperationListener listener, int eventMask)
	{
		//...
	}

	/**
	 * Removes the given operation listener if it is currently registered.
	 * <br>
	 * This method has no effect if the given operation listener is not subscribed.
	 * 
	 * @see #addOperationListener(ISubscriberOperationListener, int)
	 */
	public void removeOperationListener(ISubscriberOperationListener listener)
	{
		//...
	}

	/**
	 * Adds the given validator for the specified operation events to this
	 * subscriber. Has no effect if an identical validator is already registered
	 * for these events. After completion of this method, the given validator
	 * will be registered for exactly the specified events. If they were
	 * previously registered for other events, they will be de-registered.
	 * <p>
	 * Once registered, a validator starts receiving notification of subscriber
	 * operations which affect resources in the workspace. The
	 * validator continues to receive notifications until it is replaced or
	 * removed.
	 * <p>
	 * Validators can listen for several types of before-the-fact events as defined in
	 * <code>ISubscriberOperationEvent</code>. Clients are free to register for
	 * any number of event types however if they register for more than one, it
	 * is their responsibility to ensure they correctly handle the case where
	 * the same resource shows up in multiple notifications. Clients are
	 * guaranteed to receive only the events for which they are registered.
	 * 
	 * @param validator the validator to register, never null.
	 * @param eventMask the bit-wise OR of all before-the-fact event types of interest to the
	 * listener
	 * 
	 * @see ISubscriberOperationEvent#PRE_OPERATION_PRELIMINARY
	 * @see ISubscriberOperationEvent#PRE_OPERATION
	 * @since 3.x
	 */
	public void addOperationValidator(ISubscriberOperationValidator validator, int eventMask)
	{
		//...
	}

	/**
	 * Removes the given operation validator if it is currently registered.
	 * <br>
	 * This method has no effect if the given operation validator is not registered.
	 * 
	 * @see #addOperationValidator(ISubscriberOperationValidator, int)
	 */
	public void removeOperationValidator(ISubscriberOperationValidator validator)
	{
		//...
	}
}
