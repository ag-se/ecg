package org.electrocodeogram.cpc.notification.provider;


import org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult;


/**
 * Default implementation of {@link IEvaluationResult}.
 * 
 * @author vw
 */
public class EvaluationResult implements IEvaluationResult
{
	private Action action;
	private double weight;
	private String message;

	public EvaluationResult(Action action)
	{
		this(action, 1.0, null);
	}

	public EvaluationResult(Action action, double weight)
	{
		this(action, weight, null);
	}

	public EvaluationResult(Action action, String message)
	{
		this(action, 1.0, message);
	}

	public EvaluationResult(Action action, double weight, String message)
	{
		assert (action != null && weight >= 0);

		this.action = action;
		this.weight = weight;
		this.message = message;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult#getAction()
	 */
	@Override
	public Action getAction()
	{
		return action;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult#getWeight()
	 */
	@Override
	public double getWeight()
	{
		return weight;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult#getMessage()
	 */
	@Override
	public String getMessage()
	{
		return message;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "EvaluationResult[action: " + action + ", weight: " + weight + ", message: " + message + "]";
	}
}
