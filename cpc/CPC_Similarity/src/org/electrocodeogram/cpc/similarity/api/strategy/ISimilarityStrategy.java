package org.electrocodeogram.cpc.similarity.api.strategy;


import org.electrocodeogram.cpc.core.api.provider.similarity.ISimilarityProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.similarity.provider.SimilarityProvider;


/**
 * Strategy extension interface for the default {@link ISimilarityProvider} implementation.
 * 
 * @author vw
 * 
 * @see ISimilarityProvider
 * @see SimilarityProvider
 * @see ISimilarityStrategyTask
 */
public interface ISimilarityStrategy
{
	/**
	 * Return value for {@link ISimilarityStrategy#calculateSimilarity(IStoreProvider, ISimilarityStrategyTask)}. 
	 */
	public enum Status
	{
		/**
		 * The strategy was not applicable. No modifications were made.
		 * <br>
		 * Processing should continue with the next strategy.
		 */
		SKIPPED,

		/**
		 * The strategy was applied. Some modifications were made.
		 * <br> 
		 * Processing should continue with the next strategy.
		 */
		CONTINUE,

		/**
		 * Processing of further similarity strategies should be aborted.
		 */
		BREAK
	}

	/**
	 * Applies this strategy to the given similarity evaluation task.
	 * 
	 * @param storeProvider an optional store provider reference, NULL if the client requested a transient handling of the evaluation.
	 * @param task the similarity evaluation task, never null. A strategy directly modifies the task object in order to store its results. 
	 * @return the status of this operation, never null.
	 * 
	 * @see ISimilarityStrategyTask
	 */
	public Status calculateSimilarity(IStoreProvider storeProvider, ISimilarityStrategyTask task);
}
