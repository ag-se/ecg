package org.electrocodeogram.cpc.notification.strategy;


import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.CloneDiff;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension;
import org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult.Action;
import org.electrocodeogram.cpc.core.api.provider.similarity.ISimilarityProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.utils.CoreHistoryUtils;
import org.electrocodeogram.cpc.notification.api.strategy.INotificationEvaluationStrategy;
import org.electrocodeogram.cpc.notification.api.strategy.INotificationEvaluationStrategyResult;
import org.electrocodeogram.cpc.notification.provider.EvaluationResult;


/**
 * A simple {@link INotificationEvaluationStrategy} which compares the current content of the clone against
 * the content prior to the modification. If the similarity is 100%, the modification is ignored.
 * <p>
 * This typically happens on whitespace only or comment changes.
 * 
 * @author vw
 */
public class SelfSimilarityStrategy implements INotificationEvaluationStrategy
{
	private static final Log log = LogFactory.getLog(SelfSimilarityStrategy.class);

	/**
	 * Weight of the {@link Action#IGNORE} result returned, if a full self similarity is detected.
	 */
	private static final double IGNORE_RESULT_WEIGHT = 100.0;

	private static IStoreProvider storeProvider;
	private ISimilarityProvider similarityProvider;

	public SelfSimilarityStrategy()
	{
		log.trace("SelfSimilarityStrategy()");

		storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(IStoreProvider.class);
		assert (storeProvider != null);

		similarityProvider = (ISimilarityProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				ISimilarityProvider.class);
		assert (similarityProvider != null);
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
		 * Get content before this modification from history and calculate similarity to the
		 * current content.
		 * 
		 * On 100% similarity return INGORE & BREAK
		 * 
		 * TODO:
		 * 		Otherwise check for a user discarded notification for this clone. If one exists, use the timestamp
		 * 		of that discarded notification to acquire the clone content from that time.
		 * 		Then calculate the similarity of the current content against that content and return INGORE & BREAK
		 * 		if it is 100%.
		 * 
		 * 	PROBLEM:
		 * 		Is this likely to happen? As the first check above failed, it would mean that we can only
		 * 		get 100% similarity if some changes were undone somewhere in between.
		 * 		Is the likely hood for that high enough to warrant the extra overhead?
		 * 			It is probably not worth the effort.
		 */

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

		//get the oldest (first) diff of the transaction
		CloneDiff oldestDiff = history.getCloneDiffsForTransaction().get(0);

		//get the clone content prior to the oldest diff and thus prior to this transaction
		String oldContent = CoreHistoryUtils.getCloneContentForDate(storeProvider, modifiedClone, new Date(oldestDiff
				.getCreationDate().getTime() - 1), true);
		if (oldContent == null)
		{
			log.warn("evaluateModification() - unable to obtain old content for clone via history - clone: "
					+ modifiedClone + ", oldestDiff: " + oldestDiff);
			return Status.SKIPPED;
		}

		//now calculate the similarity between the old content and the current content
		int similarity = similarityProvider.calculateSimilarity(ISimilarityProvider.LANGUAGE_JAVA, oldContent,
				modifiedClone.getContent());

		if (log.isTraceEnabled())
			log.trace("evaluateModification() - similarity: " + similarity);

		//now check if there were any real changes during this transaction
		if (similarity == 100)
		{
			log.trace("evaluateModification() - identified full self similarity, marking for ignore, BREAK.");

			result.add(new EvaluationResult(Action.IGNORE, IGNORE_RESULT_WEIGHT));
			return Status.BREAK;
		}

		log.trace("evaluateModification() - strategy did not apply, SKIPPED.");

		return Status.SKIPPED;
	}
}
