package org.eclipse.team.core.subscribers;


import org.eclipse.core.runtime.IStatus;


/**
 * A subscriber operation validator is notified of pending repository/team provider operations
 * on resources (i.e.&nbsp;Commit/Update/Check out/...).
 * <br>
 * A validator can object to a given operation. In this case the entire operation is aborted
 * and the returned error message is displayed to the user. If multiple validators are present,
 * it is guaranteed that all validators will be executed, even in the face of validation errors.
 * A collection of all errors will be displayed to the user. 
 * <p>
 * Clients may implement this interface.
 *
 * @see Subscriber#addOperationValidator(ISubscriberOperationValidator, int)
 * @since 3.x
 */
public interface ISubscriberOperationValidator
{
	/**
	 * Notifies this validator that some resources are about to be affected by a subscriber operation.
	 * <br>
	 * To ensure to commutativity of validators, this method must <b>not</b> modify the resource or any of its
	 * properties. The resource tree is not open for modification when this method is invoked.
	 * <p>
	 * If multiple resources are affected by one operation (i.e. a commit within one transaction) they are
	 * guaranteed to all be part of one call to this method. 
	 *
	 * @param events detailing the affected resources and the kind of operation, never null.
	 * @return an error status indicates to the subscriber that the suggested operation is not valid.
	 * 		The entire operation should be aborted and an error dialog should be presented to the user. 
	 */
	public IStatus validateOperation(ISubscriberOperationEvent[] events);

}
