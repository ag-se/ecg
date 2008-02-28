package org.electrocodeogram.cpc.notification.strategy;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult.Action;
import org.electrocodeogram.cpc.core.api.provider.similarity.ISimilarityProvider;
import org.electrocodeogram.cpc.notification.api.strategy.INotificationEvaluationStrategy;
import org.electrocodeogram.cpc.notification.api.strategy.INotificationEvaluationStrategyResult;
import org.electrocodeogram.cpc.notification.provider.EvaluationResult;


/**
 * A simple {@link INotificationEvaluationStrategy} which checks whether the similarity of the modified
 * clone and its origin or all other clone group member is still 100%.
 * <p>
 * The {@link ISimilarityProvider} API specification guarantees that a similarity of 100% is only returned
 * if the code fragments are semantically equal. Thus, there is no reason to notify the user about a
 * clone modification if it is still semantically equal to the rest of the group.
 * <p>
 * This strategy will return {@link INotificationEvaluationStrategy.Status#BREAK}, if a semantic equivalence is detected.
 * <p>
 * A typical modification which could be "filtered out" by this strategy is a change inside of a comment
 * block or a local rename refactoring (once the similarity provider supports that).
 * 
 * @author vw
 */
public class FullSimilarityStrategy implements INotificationEvaluationStrategy
{
	private static final Log log = LogFactory.getLog(FullSimilarityStrategy.class);

	/**
	 * Weight of the {@link Action#INSYNC} result returned, if semantic equivalence is detected.
	 */
	private static final double INSYNC_RESULT_WEIGHT = 100.0;

	/**
	 * Weight of the {@link Action#MODIFIED} result returned, if some modification is detected.
	 */
	private static final double MODIFIED_RESULT_WEIGHT = 50.0;

	private ISimilarityProvider similarityProvider;

	public FullSimilarityStrategy()
	{
		log.trace("FullSimilarityStrategy()");

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
		 * We define full similarity as this clone having 100% similarity (according to the current similarity provider)
		 * to all its clone group members.
		 */

		//Compute the similarity of this clone against each other group member.
		boolean allInSync = true;
		int similarityToOrigin = -1;
		int similaritySum = 0;
		for (IClone groupMember : groupMembers)
		{
			if (modifiedClone.equals(groupMember))
				//there is no need to compare us with ourself
				continue;

			int similarity = similarityProvider.calculateSimilarity(ISimilarityProvider.LANGUAGE_JAVA, modifiedClone,
					groupMember, false);
			similaritySum += similarity;

			//remember the similarity to our origin
			if (modifiedClone.getOriginUuid() != null && modifiedClone.getOriginUuid().equals(groupMember.getUuid()))
				similarityToOrigin = similarity;

			if (similarity < 100)
			{
				//there is at least one group member with which we do not have 100% similarity.

				if (log.isTraceEnabled())
					log
							.trace("evaluateModification() - difference between clone and a clone group member detected - clone: "
									+ modifiedClone + ", groupMember: " + groupMember);

				allInSync = false;

				//We need to continue for now, even though we already know that there won't be
				//a total similarity of 100%. However, we're also interested in the value of
				//the total similarity and the similarity to our origin.
				//TODO: if this becomes a performance issue, we could calculate the similarity to our origin separately
				//		if needed and just stop using the total similarity value.
			}
		}

		//calculate overall similarity
		double overallSimilarity = ((double) similaritySum) / ((double) groupMembers.size() - 1);

		if (log.isTraceEnabled())
			log.trace("evaluateModification() - overall similarity: " + overallSimilarity + ", similarity to origin: "
					+ similarityToOrigin);

		//check if we found any non-100% similarity
		if (allInSync)
		{
			//all is in sync, we're happy :o)
			log.trace("evaluateModification() - all group members are in sync, INSYNC");

			result.add(new EvaluationResult(Action.INSYNC, INSYNC_RESULT_WEIGHT));
			return Status.BREAK;
		}
		/*
		 * Once we reach this point we know that there is at least one group member to which our similarity
		 * is less than 100%. However, we might still have a similarity of 100% to our origin, which would
		 * indicate that it is not a modification of this clone which caused the non-similarity but on older
		 * modification to one of the other clones.
		 */
		else if (similarityToOrigin == 100)
		{
			//we have 100% similarity to our origin.
			log.trace("evaluateModification() - 100% similarity to origin, but non-100% total similarity, MODIFIED");

			result.add(new EvaluationResult(Action.MODIFIED, MODIFIED_RESULT_WEIGHT,
					"clone modified, similarity to origin: " + similarityToOrigin + "%, overall similarity: "
							+ Math.round(overallSimilarity) + "%"));
			return Status.MODIFIED;
		}
		/*
		 * Ok, so we don't have 100% similarity to our origin (and thus obviously also not to all our group members).
		 * This means that this modification is a likely candidate for a cpc notification. However, we leave that
		 * decision up to any potential followup strategy and simply skip this clone.
		 */
		else
		{
			return Status.SKIPPED;
		}
	}
}
