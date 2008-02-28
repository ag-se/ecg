package org.electrocodeogram.cpc.similarity.provider;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.electrocodeogram.cpc.core.common.GenericStrategyDescriptor;
import org.electrocodeogram.cpc.similarity.CPCSimilarityPlugin;
import org.electrocodeogram.cpc.similarity.api.strategy.ISimilarityStrategy;
import org.electrocodeogram.cpc.similarity.preferences.CPCPreferenceConstants;


/**
 * Descriptor object for {@link ISimilarityStrategy}s.
 * 
 * The ordering of this class is by descending priority and is not consistent with equals/hashcode.
 * 
 * @author vw
 * 
 * @see GenericStrategyDescriptor
 * @see ISimilarityStrategy
 * @see SimilarityProvider
 */
public class SimilarityStrategyDescriptor extends GenericStrategyDescriptor
{
	private static final Log log = LogFactory.getLog(SimilarityStrategyDescriptor.class);

	private double weight = 1.0f;

	/**
	 * 
	 * @param element never null
	 * @param weight must be &gt;= 0.
	 */
	public SimilarityStrategyDescriptor(IConfigurationElement element, double weight)
	{
		super(CPCSimilarityPlugin.getDefault().getPluginPreferences(),
				CPCPreferenceConstants.PREF_SIMILARITY_STRATEGIES_PREFIX, element);
		assert (weight >= 0);

		log.trace("NotificationEvaluationStrategyDescriptor() ...");
	}

	/**
	 * Retrieves the weight of this strategy.<br/>
	 * Weight 1 is the default value for a strategy.<br/>
	 * A value of 0 marks the strategy as being disabled.<br/>
	 * <br/>
	 * The higher the value, the higher the influence of this strategy.<br/>
	 * A strategy with weight 2 has twice as much impact on the final result
	 * as a strategy with weight 1.
	 * 
	 * @return weight of the strategy, value is always &gt;= 0.
	 */
	public double getWeight()
	{
		assert (weight >= 0);
		return weight;
	}

	/**
	 * Retrieves an instance of this singleton strategy.
	 * 
	 * @return cached instance, never null.
	 * @throws CoreException thrown if underlying strategy can not be instantiated
	 */
	public synchronized ISimilarityStrategy getInstance() throws CoreException
	{
		return (ISimilarityStrategy) getGenericInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "SimilarityStrategyDescriptor[super: " + super.toString() + ", weight: " + weight + "]";
	}
}
