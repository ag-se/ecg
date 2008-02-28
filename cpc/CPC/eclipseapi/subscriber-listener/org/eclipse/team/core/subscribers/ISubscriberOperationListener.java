package org.eclipse.team.core.subscribers;


/**
 * A subscriber operation listener is notified of repository/team provider operations
 * on resources (i.e.&nbsp;Commit/Update/Check out/...).
 * <br>
 * Notifications are sent out before and after each operation. 
 * <p>
 * Clients may implement this interface.
 *
 * @see Subscriber#addOperationListener(ISubscriberOperationListener, int)
 * @since 3.x
 */
public interface ISubscriberOperationListener
{
	/**
	 * Notifies this listener that some resources are about to be or were
	 * affected by a subscriber operation.
	 * <br>
	 * A listener may freely modify workspace resources. The resource tree is
	 * open for modification when this method is invoked.
	 * <p>
	 * If multiple resources are affected by one operation (i.e. a commit within one
	 * transaction) they are guaranteed to all be part of one call to this method. 
	 *
	 * @param events detailing the affected resources and the kind of operation
	 */
	public void subscriberOperation(ISubscriberOperationEvent[] events);

}
