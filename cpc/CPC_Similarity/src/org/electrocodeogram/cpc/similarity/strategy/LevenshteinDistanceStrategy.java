package org.electrocodeogram.cpc.similarity.strategy;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.similarity.api.strategy.ISimilarityStrategy;
import org.electrocodeogram.cpc.similarity.api.strategy.ISimilarityStrategyTask;
import org.electrocodeogram.cpc.similarity.utils.StringDistanceUtils;


/**
 * Very basic {@link ISimilarityStrategy} which uses the Levenshtein Distance in relation
 * to the length of the longer of the two clones to obtain a percentage value for their similarity.
 * the
 * 
 * @author vw
 */
public class LevenshteinDistanceStrategy implements ISimilarityStrategy
{
	private static final Log log = LogFactory.getLog(LevenshteinDistanceStrategy.class);

	public LevenshteinDistanceStrategy()
	{
		log.trace("LevenshteinDistanceStrategy()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.similarity.api.strategy.ISimilarityStrategy#calculateSimilarity(org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider, org.electrocodeogram.cpc.core.api.data.IClone, org.electrocodeogram.cpc.core.api.data.IClone)
	 */
	@Override
	public Status calculateSimilarity(IStoreProvider storeProvider, ISimilarityStrategyTask task)
	{
		if (log.isTraceEnabled())
			log.trace("calculateSimilarity() - task: " + task);
		assert (task != null);

		int maxLength = Math.max(task.getProcessedContent1().length(), task.getProcessedContent2().length());
		int distance = StringDistanceUtils
				.levenshteinDistance(task.getProcessedContent1(), task.getProcessedContent2());
		/*
		 * The levenshtein distance has an upper bound which corresponds to the length of the longest string.
		 * We need to convert the distance into a percentage value between 0 (no similarity) and 100 (total similarity).
		 */
		double result = 100;
		if (distance > 0)
			result = 100 - ((((double) distance) / ((double) maxLength)) * 100);
		task.addScore(result, 1.0);

		if (log.isTraceEnabled())
			log.trace("calculateSimilarity() - distance: " + distance + ", maxLen: " + maxLength + ", result: "
					+ result);

		return Status.CONTINUE;
	}
}
