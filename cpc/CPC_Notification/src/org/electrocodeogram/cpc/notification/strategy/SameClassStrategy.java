package org.electrocodeogram.cpc.notification.strategy;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult.Action;
import org.electrocodeogram.cpc.notification.api.strategy.INotificationEvaluationStrategy;
import org.electrocodeogram.cpc.notification.api.strategy.INotificationEvaluationStrategyResult;
import org.electrocodeogram.cpc.notification.provider.EvaluationResult;


/**
 * A simple {@link INotificationEvaluationStrategy} which prevents notifications for clone
 * groups which are entirely located within the same file.
 * <p>
 * Rationale:
 * <blockquote>
 * 	The close proximity of the clone instances makes it likely that the developer will be
 * 	aware of these clones and will take care of the propagation of any modifications,
 * 	if necessary. 
 * </blockquote>
 * 
 * @author vw
 */
//TODO: this strategy should probably be called SameFileStrategy
public class SameClassStrategy implements INotificationEvaluationStrategy
{
	private static final Log log = LogFactory.getLog(SameClassStrategy.class);

	/**
	 * Weight of the {@link Action#MODIFIED} result returned, if some modification is detected.
	 */
	private static final double MODIFIED_RESULT_WEIGHT = 50.0;

	public SameClassStrategy()
	{
		log.trace("SameClassStrategy()");
	}

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

		/*
		 * Check if all group members belong to the same file.
		 */

		boolean allInOneFile = true;
		for (IClone groupClone : groupMembers)
		{
			if (!groupClone.getFileUuid().equals(modifiedClone.getFileUuid()))
			{
				allInOneFile = false;
				break;
			}
		}

		if (allInOneFile)
		{
			log
					.trace("evaluateModification() - all group members of the clone group are located within the same file, preventing notification.");

			/*
			 * Once we reach this point the clone has passed all similarity "filters".
			 * This means that it was modified in some way.
			 */
			result.add(new EvaluationResult(Action.MODIFIED, MODIFIED_RESULT_WEIGHT, ""));
			return Status.BREAK;
		}

		log.trace("evaluateModification() - some group members are located within a different file, strategy SKIPPED.");

		//nothing to do.
		return Status.SKIPPED;
	}

}
