package org.electrocodeogram.cpc.notification.strategy;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.provider.classification.IClassificationProvider;
import org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult;
import org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult.Action;
import org.electrocodeogram.cpc.notification.api.strategy.INotificationEvaluationStrategy;
import org.electrocodeogram.cpc.notification.api.strategy.INotificationEvaluationStrategyResult;
import org.electrocodeogram.cpc.notification.provider.EvaluationResult;


/**
 * Very simple {@link INotificationEvaluationStrategy} which returns {@link IEvaluationResult.Action#IGNORE}
 * for all clones which have the classification {@link IClassificationProvider#CLASSIFICATION_TEMPLATE}.
 * <p>
 * This strategy will return {@link INotificationEvaluationStrategy.Status#BREAK}, if such a template match is detected.
 * <p 
 * TODO:/FIXME: A better approach might be to mark all clones with classification TEMPLATE as state
 * 		IGNORE during classification. Or to just REJECT them outright?
 * 
 * @author vw
 */
public class SimpleCategoryStrategy implements INotificationEvaluationStrategy
{
	private static final Log log = LogFactory.getLog(SimpleCategoryStrategy.class);

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

		//check whether this clone has been classified as a tempalte method
		if (modifiedClone.hasClassification(IClassificationProvider.CLASSIFICATION_TEMPLATE))
		{
			// Ok, this is a whitespace only change.
			// There is no reason to notify/warn the user about this modification.

			log.trace("evaluateModification() - identified TEMPLATE clone, marking for ignore, BREAK.");

			result.add(new EvaluationResult(Action.IGNORE, IGNORE_RESULT_WEIGHT));
			return Status.BREAK;
		}

		log.trace("evaluateModification() - strategy did not apply, SKIPPED.");

		//we weren't able to identify any whitespace only change
		return Status.SKIPPED;
	}
}
