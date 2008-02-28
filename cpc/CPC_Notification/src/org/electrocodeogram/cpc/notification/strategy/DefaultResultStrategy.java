package org.electrocodeogram.cpc.notification.strategy;


import java.util.List;

import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult;
import org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult.Action;
import org.electrocodeogram.cpc.notification.api.strategy.INotificationEvaluationStrategy;
import org.electrocodeogram.cpc.notification.api.strategy.INotificationEvaluationStrategyResult;
import org.electrocodeogram.cpc.notification.provider.EvaluationResult;
import org.electrocodeogram.cpc.notification.provider.NotificationEvaluationProvider;


/**
 * By default the {@link NotificationEvaluationProvider} will mark any clone modification as
 * {@link IEvaluationResult.Action#IGNORE}. We don't want to be generating incorrect
 * notifications if some reason no strategies were registered.
 * <p>
 * This strategy adds a default result with action {@link IEvaluationResult.Action#NOTIFY}
 * and weight <em>0.0</em>.
 * <br>
 * That means that once this strategy is registered, the default reply of the
 * {@link NotificationEvaluationProvider} will be {@link IEvaluationResult.Action#NOTIFY}.
 * 
 * @author vw
 */
public class DefaultResultStrategy implements INotificationEvaluationStrategy
{

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.notification.api.strategy.INotificationEvaluationStrategy#evaluateModification(org.electrocodeogram.cpc.core.api.data.IClone, java.util.List, boolean, org.electrocodeogram.cpc.notification.api.strategy.INotificationEvaluationStrategyResult)
	 */
	@Override
	public Status evaluateModification(IClone modifiedClone, List<IClone> groupMembers, boolean initialEvaluation,
			INotificationEvaluationStrategyResult result)
	{
		result.add(new EvaluationResult(Action.NOTIFY, 0.0));
		return Status.MODIFIED;
	}

}
