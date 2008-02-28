package org.electrocodeogram.cpc.notification.api.strategy;


import org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult;


/**
 * Result wrapper for {@link INotificationEvaluationStrategy} implementations.
 * 
 * @author vw
 */
public interface INotificationEvaluationStrategyResult
{
	/**
	 * Adds the given {@link IEvaluationResult} to this result collection.<br/>
	 * A strategy may call this method more than once.
	 * 
	 * @param result the {@link IEvaluationResult} to add, never null.
	 */
	public void add(IEvaluationResult result);

}
