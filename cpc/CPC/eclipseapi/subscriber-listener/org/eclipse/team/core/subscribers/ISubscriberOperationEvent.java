package org.eclipse.team.core.subscribers;


import org.eclipse.core.resources.IResource;


/**
 * An event that describes a subscriber operation which affected a resource
 * in the workspace.
 * <p>
 * Clients are not intended to implement. Instead subclass {@link SubscriberOperationEvent}.
 *
 * @see ISubscriberOperationListener
 * @see ISubscriberOperationValidator
 * @since 3.x
 */
public interface ISubscriberOperationEvent
{
	/**
	 * Event type constant (bit mask) indicating a before-the-fact 
	 * report about an impeding subscriber operation <b>might</b> affect the
	 * given resource.
	 * <br>
	 * This event is generated once the potential set of resources affected by
	 * a subscriber operation is known. The set of resources does not necessarily
	 * match the set of resources which are part of the final operation
	 * (i.e. the user could deselect some resources in the commit dialog).
	 * <br>
	 * In case of commit/checkin operations this event is generated before the user
	 * has entered a commit comment.
	 * <br>
	 * A listener can object to the operation.
	 *
	 * @see #getType()
	 */
	public static final int PRE_OPERATION_PRELIMINARY = 1;

	/**
	 * Event type constant (bit mask) indicating a before-the-fact 
	 * report about an impeding subscriber operation which <b>will</b>
	 * affect the given resource.
	 * <br>
	 * This event type is generated once the final set of resources affected by
	 * this operation is known.
	 * <br>
	 * In case of commit/checkin operations this event is generated after the user
	 * has entered a commit comment.
	 * <br>
	 * A listener can object to the operation.
	 *
	 * @see #getType()
	 */
	public static final int PRE_OPERATION = 2;

	/**
	 * Event type constant (bit mask) indicating an after-the-fact 
	 * report about a subscriber operation affecting the given resource.
	 *
	 * @see #getType()
	 */
	public static final int POST_OPERATION = 3;

	/*====================================================================
	 * Constants defining the kinds of team changes to resources:
	 *====================================================================*/

	/**
	 * Event kind constant (bit mask) indicating that the resource is about to be/has been updated by the
	 * subscriber operation.
	 * 
	 * @see #getFlags
	 */
	public static final int UPDATE = 0;

	/**
	 * Event kind constant (bit mask) indicating that the resource is about to be/has been merged by the
	 * subscriber operation. This flag is present if the subscriber has identified a local modification
	 * of the resource. The {@link #UPDATE} flag will always be set, if this flag is set.
	 * 
	 * @see #getFlags
	 */
	public static final int UPDATE_MERGE = 0x1;

	/**
	 * Event kind constant (bit mask) indicating that the resource could not be merged by the
	 * subscriber operation due to merge conflicts. The {@link #UPDATE} and {@link #UPDATE_MERGE}
	 * flags will always be set, if this flag is set.
	 * <br>
	 * This flag is only meaningful for {@link #POST_OPERATION} events.
	 * 
	 * @see #getFlags
	 */
	public static final int UPDATE_MERGE_UNRESOLVED = 0x2;

	/**
	 * Event kind constant (bit mask) indicating that the resource is about to be/has been reverted back to its
	 * base revision by the subscriber operation.
	 * 
	 * @see  #getFlags
	 */
	public static final int REVERT = 0x3;

	/**
	 * Event kind constant (bit mask) indicating that the resource is about to be/has been replaced with another
	 * revision by the subscriber operation (i.e. a switch to another branch/tag).
	 * 
	 * @see  #getFlags
	 */
	public static final int REPLACE = 0x4;

	/**
	 * Event kind constant (bit mask) indicating that the resource is about to be/has been checked out by the
	 * subscriber operation.
	 * 
	 * @see  #getFlags
	 */
	public static final int CHECK_OUT = 0x5;

	/**
	 * Event kind constant (bit mask) indicating that the resource is about to be/has been committed by the
	 * subscriber operation.
	 * 
	 * @see #getFlags
	 */
	public static final int COMMIT = 0x6;

	/**
	 * Event kind constant (bit mask) indicating that the resource is about to be/has been checked in by the
	 * subscriber operation.
	 * 
	 * @see #getFlags
	 */
	public static final int CHECK_IN = 0x7;

	/*
	 * ... did I forget any interesting flags?
	 */

	/**
	 * Returns the type of this event.
	 * <br>
	 * Indicates whether the subscriber operation is about to be performed or whether
	 * it was already executed.
	 * 
	 * @return one of the event type constants
	 * @see #PRE_OPERATION
	 * @see #POST_OPERATION
	 */
	public abstract int getType();

	/**
	 * Return the flags that describe the type of operation (about to be) performed.
	 * The returned value should be ANDed with the change type
	 * flags to determine whether the change event is of 
	 * a particular type. For example,
	 * <pre>
	 *   if (event.getFlags() & ISubscriberOperationEvent.UPDATE) {
	 *      // the resource is about to be/was updated
	 *   }
	 * </pre>
	 * 
	 * @return the flags that describe the type of operation
	 */
	public abstract int getFlags();

	/**
	 * Return the resource which will be/was affected by this subscriber operation.
	 * 
	 * @return the resource which will be/was affected by this subscriber operation, never null.
	 */
	public abstract IResource getResource();

	/**
	 * Returns the content identifier for this resource prior to this operation.
	 * For an event of type {@link #PRE_OPERATION} this is the current content identifier of the resource.
	 * This value may be null if the resource was not under control of the subscriber prior to this event
	 * (i.e. on a check in). 
	 * 
	 * @return content identifier of this resource prior to this operation, may be null.
	 */
	public String getOldContentIdentifier();

	/**
	 * Returns the content identifier for this resource after this operation.
	 * For an event of type {@link #POST_OPERATION} this is the current content identifier of the resource.
	 *  
	 * @return content identifier of this resource after this operation, never null.
	 */
	public String getNewContentIdentifier();

	/**
	 * Returns the commit message for this subscriber operation, if it is a {@link #COMMIT} or
	 * {@link #CHECK_IN} operation and the event isn't of type {@link #PRE_OPERATION_PRELIMINARY}. 
	 * 
	 * @return commit message if available, may be null.
	 */
	public String getCommitMessage();

	/**
	 * Return the subscriber to which this event applies.
	 * 
	 * @return the subscriber to which this event applies, never null.
	 */
	public abstract Subscriber getSubscriber();

}
