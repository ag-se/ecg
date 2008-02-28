package org.electrocodeogram.cpc.core.api.provider.notification;


import java.util.List;

import org.electrocodeogram.cpc.core.api.data.IClone;


/**
 * Return value for {@link INotificationEvaluationProvider#evaluateModification(IClone, List, boolean)}.
 * 
 * @author vw
 * 
 * @see INotificationEvaluationProvider
 */
public interface IEvaluationResult
{
	/**
	 * The type of action which should be taken as a result of an evaluation.
	 */
	public enum Action
	{
		/**
		 * Completely ignore this modification event.
		 * <br>
		 * The state of the clone and its clone group members will not be modified.
		 * <p>
		 * This action is typically chosen, if the nature of the change guarantees that
		 * an evaluation of the modification of this clone and its group members would
		 * not yield any different results than before the change.
		 * <br>
		 * I.e. a white space only change or a change which only affected a comment. 
		 */
		IGNORE,

		/**
		 * The clone is in sync with all its clone group members. This means that they
		 * are all semantically equivalent.
		 * <p>
		 * Notifications and modified states for the clone and all its clone group
		 * members should be cleared.
		 * <br>
		 * The new state for all of them would be {@link IClone.State#DEFAULT}.
		 */
		INSYNC,

		/**
		 * The clone is in sync with all its clone group members, if one considers
		 * all modifications made shortly after the creation of each group member
		 * to be of no consequence.
		 * <p>
		 * This state therefore describes parametrised clones which have not been
		 * modified in any significant way since their initial parametrisation.
		 * <p>
		 * Notifications and modified states for the clone and all its clone group
		 * members should be cleared.
		 * <br>
		 * The new state for all of them would be {@link IClone.State#CUSTOMISED}.
		 */
		INSYNC_CUSTOMISED,

		/**
		 * The clone modification is minor but does represent a possible change in
		 * semantics which might be of interest to the user.
		 * <br>
		 * At the same time the modification is not deemed important enough to warrant
		 * an action of type {@link Action#NOTIFY} or {@link Action#WARN}.
		 * <p>
		 * This clone and all other members of this clone group should be set to
		 * {@link IClone.State#MODIFIED}. Other group members are not updated to this
		 * state if they already have a higher state set ({@link IClone.State#NOTIFY}
		 * or {@link IClone.State#WARN}).
		 */
		MODIFIED,

		/**
		 * The user should be notified about this modification. It might have
		 * introduced some update anomalies.
		 * <br>
		 * Notifications are typically displayed in some non intrusive manner.
		 * <p>
		 * Indicates that this clone's state should be set to {@link IClone.State#NOTIFY} and
		 * the state of all its group members to {@link IClone.State#MODIFIED}, unless
		 * they already have a higher state set.
		 */
		NOTIFY,

		/**
		 * The user should be warned about this modification. There is a very
		 * high likelihood that it has introduced some update anomalies.
		 * <br>
		 * Warnings are typically displayed in a more prominent manner. They
		 * might be displayed in the same way as java warnings or errors.
		 * <br>
		 * This action type should be used very sparingly.
		 * <p>
		 * Indicates that this clone's state should be set to {@link IClone.State#WARN} and
		 * the state of all its group members to {@link IClone.State#MODIFIED}, unless
		 * they already have a higher state set.
		 */
		WARN,

		/**
		 * Similar to {@link Action#NOTIFY}.<br>But indicates to the client of the
		 * {@link INotificationEvaluationProvider} that this
		 * notification should be made visible to the user instantly.
		 * <br>
		 * This should be used only in cases were it is obvious that user should be
		 * notified right away. The default behaviour of {@link Action#NOTIFY} is to allow
		 * the client to delay the notification until the user has finished modifying the
		 * clone and its surroundings. The client will then typically delegate the clone
		 * back to the {@link INotificationEvaluationProvider} for reevaluation once the
		 * "delay" has passed.
		 * 
		 * @see Action#NOTIFY
		 */
		INSTANT_NOTIFY,

		/**
		 * Similar to {@link Action#WARN}.
		 * 
		 * @see Action#WARN
		 * @see Action#INSTANT_NOTIFY
		 */
		INSTANT_WARN,

		/**
		 * The clone modification has changed the clone to an extend which
		 * makes it very likely that the clone does no longer belong to its
		 * original clone group. It should therefore be removed from the
		 * group and be treated as a stand alone instance.
		 */
		LEAVE_GROUP
	}

	/**
	 * What should be done with this clone? Does the user need to be notified?
	 * 
	 * @return {@link Action} which should be taken, never null.
	 */
	public Action getAction();

	/**
	 * The importance of this notification/warning in relation to other events.
	 * <br>
	 * The default weight is <em>1.0</em>.
	 * <br>
	 * Only applies to action types {@link Action#NOTIFY} and {@link Action#WARN}.
	 * 
	 * @return weight of this event, &gt;= 0.
	 */
	public double getWeight();

	/**
	 * Optional notification/warning message which should be displayed to the user.
	 * <br>
	 * Only applies to action types {@link Action#NOTIFY} and {@link Action#WARN}.
	 * 
	 * @return a human readable message, NULL if no specific message should be shown.
	 */
	public String getMessage();
}
