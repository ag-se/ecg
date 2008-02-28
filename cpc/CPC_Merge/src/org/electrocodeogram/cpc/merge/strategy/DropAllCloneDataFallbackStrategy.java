package org.electrocodeogram.cpc.merge.strategy;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.provider.merge.MergeException;
import org.electrocodeogram.cpc.merge.api.strategy.IMergeContext;
import org.electrocodeogram.cpc.merge.api.strategy.IMergeStrategy;
import org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask;
import org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult;
import org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult.Type;


/**
 * A fallback strategy which drops all still pending local and remote clones.<br/>
 * This strategy should always be the last strategy to be called.
 * 
 * @author vw
 */
public class DropAllCloneDataFallbackStrategy implements IMergeStrategy
{
	private static final Log log = LogFactory.getLog(DropAllCloneDataFallbackStrategy.class);

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
		 * Ok, drop all remaining clones.
		 */
		if (!mergeContext.getPendingLocalClones().isEmpty())
		{
			if (log.isDebugEnabled())
				log.debug("merge() - going to drop " + mergeContext.getPendingLocalClones().size() + " local clones: "
						+ mergeContext.getPendingLocalClones());

			mergeResult.addClonesLocal(mergeContext.getPendingLocalClones(), Type.LOST);
			mergeContext.getPendingLocalClones().clear();
		}

		if (!mergeContext.getPendingRemoteClones().isEmpty())
		{
			if (log.isDebugEnabled())
				log.debug("merge() - going to drop " + mergeContext.getPendingRemoteClones().size()
						+ " remote clones: " + mergeContext.getPendingRemoteClones());

			mergeResult.addClonesRemote(mergeContext.getPendingRemoteClones(), Type.LOST);
			mergeContext.getPendingRemoteClones().clear();
		}

		return Status.PARTIAL;
	}

}
