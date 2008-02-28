package org.electrocodeogram.cpc.notification.strategy;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.CloneDiff;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension;
import org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult.Action;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.notification.api.strategy.INotificationEvaluationStrategy;
import org.electrocodeogram.cpc.notification.api.strategy.INotificationEvaluationStrategyResult;
import org.electrocodeogram.cpc.notification.provider.EvaluationResult;


/**
 * A very simple {@link INotificationEvaluationStrategy} which only checks whether the modifications
 * made on the modified clones content are whitespace only changes.
 * <p>
 * Does not detect whitespace only changes which removed data (performance reasons).
 * <p>
 * This strategy will return {@link INotificationEvaluationStrategy.Status#BREAK}, if a whitespace only change is detected.
 * 
 * @author vw
 */
public class WhitespaceOnlyChangeStrategy implements INotificationEvaluationStrategy
{
	private static final Log log = LogFactory.getLog(WhitespaceOnlyChangeStrategy.class);

	/**
	 * Weight of the {@link Action#IGNORE} result returned, if a whitespace only change is detected.
	 */
	private static final double IGNORE_RESULT_WEIGHT = 100.0;

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.notification.api.strategy.INotificationEvaluationStrategy#evaluateModification(org.electrocodeogram.cpc.core.api.data.IClone, java.util.List, boolean, org.electrocodeogram.cpc.notification.api.strategy.INotificationEvaluationStrategyResult)
	 */
	@Override
	public Status evaluateModification(IClone modifiedClone, List<IClone> groupMembers, boolean initialEvaluation,
			INotificationEvaluationStrategyResult result)
	{
		if (log.isTraceEnabled())
			log.trace("evaluateModification() - modifiedClone: " + modifiedClone + ", groupMembers: " + groupMembers
					+ ", initialEvaluation: " + initialEvaluation + ", result: " + result);
		assert (modifiedClone != null && groupMembers != null && groupMembers.size() >= 2 && result != null);

		//get the list of modifications made to this clone
		ICloneModificationHistoryExtension history = (ICloneModificationHistoryExtension) modifiedClone
				.getExtension(ICloneModificationHistoryExtension.class);
		if (history == null || history.getCloneDiffsForTransaction().isEmpty())
		{
			//this happens for delayed notification events which are re-evaluated at a later time
			if (log.isTraceEnabled())
				log.trace("evaluateModification() - clone contains no modification history data, skipping: "
						+ modifiedClone);
			return Status.SKIPPED;
		}

		if (log.isTraceEnabled())
			log.trace("evaluateModification() - modification history: " + history);

		//now check each modification to see whether they are all whitespace only changes
		boolean wsOnly = true;
		for (CloneDiff diff : history.getCloneDiffsForTransaction())
		{
			if (log.isTraceEnabled())
				log.trace("evaluateModification() - evaluate diff: " + diff);

			if (diff.getLength() > 0)
			{
				log
						.trace("evaluateModification() - diff removed characters, can't guarantee whitespace-only change, aborting.");
				/*
				 * We can't guarantee that the removed characters where only whitespaces.
				 * 
				 * To do a real check for removal of non-whitespace characters here,
				 * we'd need to access the content of the clone prior to the current diff.
				 * To allow that we need the full clone diff data and the original content of the
				 * clone.
				 * 
				 * This is possible via CoreHistoryUtils.getCloneContentForDate(), however,
				 * it does not seem worth the effort. This strategy is mainly meant to speed things
				 * up by weeding out whitespace only changes with as little overhead as possible.
				 * 
				 * So doing a full blown history check here would go against the main
				 * reason why this strategy exists in the first place.
				 * 
				 * A whitespace only change which was not detected here (because of this shortcoming),
				 * will be detected by the SelfSimilarityStrategy in the next step.
				 * So we're not loosing anything by not implementing a better check here.
				 */
				wsOnly = false;
				break;
			}

			if (diff.getText() != null && CoreStringUtils.containsNonWhitespace(diff.getText()))
			{
				log.trace("evaluateModification() - diff added non-whitespace characters, aborting.");
				wsOnly = false;
				break;
			}
		}

		if (wsOnly)
		{
			// Ok, this is a whitespace only change.
			// There is no reason to notify/warn the user about this modification.

			log.trace("evaluateModification() - identified whitespace only change, marking for ignore, BREAK.");

			result.add(new EvaluationResult(Action.IGNORE, IGNORE_RESULT_WEIGHT));
			return Status.BREAK;
		}

		log.trace("evaluateModification() - strategy did not apply, SKIPPED.");

		//we weren't able to identify any whitespace only change
		return Status.SKIPPED;
	}
}
