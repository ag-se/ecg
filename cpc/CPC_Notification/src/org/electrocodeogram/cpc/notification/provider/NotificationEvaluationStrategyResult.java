package org.electrocodeogram.cpc.notification.provider;


import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult;
import org.electrocodeogram.cpc.notification.api.strategy.INotificationEvaluationStrategyResult;


public class NotificationEvaluationStrategyResult implements INotificationEvaluationStrategyResult
{
	private static final Log log = LogFactory.getLog(NotificationEvaluationStrategyResult.class);

	private List<IEvaluationResult> results;

	public NotificationEvaluationStrategyResult()
	{
		log.trace("NotificationEvaluationStrategyResult()");

		results = new LinkedList<IEvaluationResult>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.notification.api.strategy.INotificationEvaluationStrategyResult#add(org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult)
	 */
	@Override
	public void add(IEvaluationResult result)
	{
		if (log.isTraceEnabled())
			log.trace("add() - result: " + result);
		assert (result != null);

		results.add(result);
	}

	public List<IEvaluationResult> getResults()
	{
		return results;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "NotificationEvaluationStrategyResult[results: " + results + "]";
	}
}
