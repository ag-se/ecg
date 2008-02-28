package org.electrocodeogram.cpc.merge.strategy;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.provider.merge.MergeException;
import org.electrocodeogram.cpc.merge.api.strategy.IMergeContext;
import org.electrocodeogram.cpc.merge.api.strategy.IMergeStrategy;
import org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask;
import org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult;
import org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult.Type;


/**
 * This strategy checks the given data for locally or remotely deleted clones and removes them
 * from the pending clone lists.<br/>
 * They will thus be removed from the final merged clone data.<br/>
 * <br/>
 * This strategy only applies to 3-way-merges.
 * 
 * @author vw
 */
public class LocalOrRemoteCloneDeletionsStrategy implements IMergeStrategy
{
	private static final Log log = LogFactory.getLog(LocalOrRemoteCloneDeletionsStrategy.class);

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IMergeStrategy#merge(org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask, org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult, java.util.LinkedList, java.util.LinkedList)
	 */
	@Override
	public Status merge(IReadableMergeTask mergeTask, IWriteableMergeResult mergeResult, IMergeContext mergeContext)
			throws MergeException
	{
		if (log.isTraceEnabled())
			log.trace("merge() - mergeTask: " + mergeTask + ", mergeResult: " + mergeResult + ", mergeContext: "
					+ mergeContext);
		assert (mergeTask != null && mergeResult != null && mergeContext != null);

		/*
		 * Check if there is anything for us to do.
		 */
		if (!mergeContext.isLocalOrRemoteClonePending())
		{
			log.trace("merge() - no pending clones left, nothing to do, skipping.");
			return Status.SKIPPED;
		}

		/*
		 * We only support 3-way-merges
		 */
		if (!mergeTask.isThreeWayMerge())
		{
			log.trace("merge() - 2-way-merge not supported, skipping.");
			return Status.SKIPPED;
		}

		if (mergeContext.getPendingBaseClones().isEmpty())
		{
			log.trace("merge() - no pending base clones, probably no clone deletions in this update, skipping.");
			return Status.SKIPPED;
		}

		/*
		 * Ok, now identify locally and remotely deleted clones by comparing the
		 * local and remote clone data with the base clone data.
		 */

		/*
		 * Get locally and remotely deleted clones.
		 */
		Set<IClone> locallyDeletedClones = extractDeletedClones(mergeTask.getLocalClones(), mergeContext
				.getPendingBaseClones());
		Set<IClone> remotelyDeletedClones = extractDeletedClones(mergeTask.getRemoteClones(), mergeContext
				.getPendingBaseClones());

		/*
		 * Remove all clones which were deleted on both sides.
		 */
		Set<IClone> commonDeletions = new HashSet<IClone>(locallyDeletedClones);
		commonDeletions.retainAll(remotelyDeletedClones);
		if (!commonDeletions.isEmpty())
		{
			if (log.isTraceEnabled())
				log.trace("merge() - locally and remotely deleted clones: " + commonDeletions);

			mergeContext.getPendingBaseClones().removeAll(commonDeletions);

			//remove common deletions from local and remote deletions
			locallyDeletedClones.removeAll(commonDeletions);
			remotelyDeletedClones.removeAll(commonDeletions);
		}

		/*
		 * Handle locally deleted clones.
		 */
		if (!locallyDeletedClones.isEmpty())
		{
			if (log.isTraceEnabled())
				log.trace("merge() - locally deleted clones: " + locallyDeletedClones);

			/*
			 * TODO: check here if the remote clone was moved or modified.
			 * That might result in a type of conflict. Though probably a lesser one.
			 */

			mergeResult.addClonesRemote(locallyDeletedClones, Type.REMOVED);

			mergeContext.getPendingBaseClones().removeAll(locallyDeletedClones);
			mergeContext.getPendingRemoteClones().removeAll(locallyDeletedClones);
		}

		/*
		 * Handle remotely deleted clones.
		 */
		if (!remotelyDeletedClones.isEmpty())
		{
			if (log.isTraceEnabled())
				log.trace("merge() - remotely deleted clones: " + remotelyDeletedClones);

			/*
			 * TODO: check here if the local clone was moved or modified.
			 * That might result in a type of conflict. Though probably a lesser one.
			 */

			mergeResult.addClonesLocal(remotelyDeletedClones, Type.REMOVED);

			mergeContext.getPendingBaseClones().removeAll(remotelyDeletedClones);
			mergeContext.getPendingLocalClones().removeAll(remotelyDeletedClones);
		}

		//did we do anything?
		if (!commonDeletions.isEmpty() || !locallyDeletedClones.isEmpty() || !remotelyDeletedClones.isEmpty())
			return Status.PARTIAL;
		else
			return Status.SKIPPED;
	}

	private Set<IClone> extractDeletedClones(List<IClone> revision, List<IClone> base)
	{
		Set<IClone> result = new HashSet<IClone>(base.size());

		for (IClone baseClone : base)
		{
			if (!revision.contains(baseClone))
			{
				//ok, this clone was deleted in revision
				result.add(baseClone);
			}
		}

		return result;
	}
}
