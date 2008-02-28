package org.electrocodeogram.cpc.notification.api.strategy;


import java.util.List;

import org.electrocodeogram.cpc.core.api.data.CloneDiff;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension;
import org.electrocodeogram.cpc.core.api.hub.event.CloneModificationEvent;
import org.electrocodeogram.cpc.core.api.hub.event.CloneNotificationEvent;
import org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult;
import org.electrocodeogram.cpc.notification.provider.NotificationEvaluationProvider;


/**
 * Interface for strategies which support the {@link NotificationEvaluationProvider} in reaching its
 * decision about how to handle a given clone content modification.
 * <p>
 * Implementations of this interface can be registered with the {@link NotificationEvaluationProvider}
 * by extending the corresponding <em>CPC Notification</em> extension point "<em>notificationEvaluationStategies</em>". 
 * <p>
 * All strategies are treated as singletons. An implementation should expect concurrent calls to all methods
 * of this interface.
 * 
 * @author vw
 * 
 * @see NotificationEvaluationProvider
 */
public interface INotificationEvaluationStrategy
{
	/**
	 * Return status indicator for {@link INotificationEvaluationStrategy#evaluateModification(IClone, List, boolean, INotificationEvaluationStrategyResult)}.
	 */
	public enum Status
	{
		/**
		 * Indicates that the strategy does not apply to the given clone and did not make any modifications.
		 */
		SKIPPED,

		/**
		 * Indicates that the strategy made some modifications to the result object.
		 */
		MODIFIED,

		/**
		 * Indicates that this event should not be passed on to any more strategies and that
		 * the clone's evaluation is in it's final stage.<br/>
		 * <br/>
		 * A strategy will typically return this value if it detected a special situation which may
		 * confuse other strategies or if it needs to make sure that no other strategy will
		 * override its decision. 
		 */
		BREAK
	}

	/**
	 * Takes a modified clone and a list of its clone group members and evaluates whether what kind of
	 * action should be taken.
	 * 
	 * @param modifiedClone the clone which was modified, never null.
	 * @param groupMembers a list of all group members of the clone's clone group,
	 * 		the modified clone itself is also part of this list, size always &gt;=2, never null.
	 * 		The {@link IClone} instance will contain a description of the latest modifications
	 * 		as {@link CloneDiff}s inside of an {@link ICloneModificationHistoryExtension} object.
	 * 		However, no {@link CloneDiff}s will be available during reevaluation of an event.
	 * @param initialEvaluation true if this is the first time this modification is evaluated.
	 * 		Typically this is set to true when the modification is first seen as an
	 * 		{@link CloneModificationEvent} and set to false for later reevaluations due to
	 * 		(delayed) {@link CloneNotificationEvent}s.
	 * @param result an initially empty wrapper of {@link IEvaluationResult}s. The strategy should add
	 * 		its own results as a new {@link IEvaluationResult} to this wrapper. A strategy may add multiple
	 * 		{@link IEvaluationResult}s. Never null.
	 * @return the {@link Status} of this evaluation, never null.
	 */
	public Status evaluateModification(IClone modifiedClone, List<IClone> groupMembers, boolean initialEvaluation,
			INotificationEvaluationStrategyResult result);
}
