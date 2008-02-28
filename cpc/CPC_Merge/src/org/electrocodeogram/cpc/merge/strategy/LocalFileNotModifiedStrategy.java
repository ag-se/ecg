package org.electrocodeogram.cpc.merge.strategy;


import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.special.IRemoteStoreCloneFile;
import org.electrocodeogram.cpc.core.api.provider.merge.MergeException;
import org.electrocodeogram.cpc.core.utils.CoreClonePositionUtils;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.merge.api.strategy.IMergeContext;
import org.electrocodeogram.cpc.merge.api.strategy.IMergeStrategy;
import org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask;
import org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult;
import org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult.Type;
import org.electrocodeogram.cpc.merge.utils.MergeUtils;


/**
 * A simple strategy which checks if the local source file and clone data was in sync with
 * the base revision and that we can therefore just copy over the remote clone data. 
 * 
 * @author vw
 */
public class LocalFileNotModifiedStrategy implements IMergeStrategy
{
	private static final Log log = LogFactory.getLog(LocalFileNotModifiedStrategy.class);

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
		 * There are two cases which are of interest, either this is a 3-Way-Merge and local
		 * and base data are equal or this is a 2-Way-Merge but the special LocalBaseInSyncHint flag is set.
		 * 
		 * Otherwise there is nothing we can do.
		 */

		if (mergeTask.isThreeWayMerge())
		{
			//first check if the source is equal
			if (mergeTask.getLocalSourceFileContent().equals(mergeTask.getBaseSourceFileContent()))
			{
				log.trace("merge() - local source equals base source.");

				//ok, the source was not modified
				//lets compare all clones
				if (CoreClonePositionUtils.cloneListsEqual(mergeTask.getLocalClones(), mergeTask.getBaseClones()))
				{
					log.trace("merge() - local clone data equals base clone data.");

					//ok, just overwrite the local data :o)
					handleOverwrite(mergeTask, mergeResult, mergeContext.getPendingLocalClones(), mergeContext
							.getPendingRemoteClones());
					return Status.FULL;
				}
				else
				{
					log.trace("merge() - local clone data and base clone data differ.");
				}
			}
			else
			{
				log.trace("merge() - local source and base source differ.");
			}
		}
		else if (mergeTask.isLocalBaseInSyncHint())
		{
			log.trace("merge() - local<->base sync hint flag is present.");

			//easy :o)
			handleOverwrite(mergeTask, mergeResult, mergeContext.getPendingLocalClones(), mergeContext
					.getPendingRemoteClones());
			return Status.FULL;
		}
		else
		{
			log.trace("merge() - 2-way-merge and no local<->base sync hint flag, skipping.");
		}

		//just some sanity check
		//the local source file should always be "remote dirty" if we reach this point.
		if (!((IRemoteStoreCloneFile) mergeTask.getLocalCloneFile()).isRemoteDirty())
		{
			log.warn("merge() - local file is mared as NON-remote-dirty, but this could not be confirmed - mergeTask: "
					+ mergeTask, new Throwable());
		}

		log.trace("merge() - strategy does not apply - SKIPPED.");

		//this strategy does not apply
		return Status.SKIPPED;
	}

	/**
	 * Overwrites the local clone data with the remote data. 
	 */
	private void handleOverwrite(IReadableMergeTask mergeTask, IWriteableMergeResult mergeResult,
			LinkedList<IClone> pendingLocalClones, LinkedList<IClone> pendingRemoteClones)
	{
		log.trace("handleOverwrite() - going to replace the local clone data with the remote data.");

		//integrity check
		//the remote source and the merged source should always be equal here
		if (!mergeTask.getRemoteSourceFileContent().equals(mergeTask.getMergedSourceFileContent()))
		{
			log
					.error(
							"handleOverwrite() - going to use remote clone data, but remote source file does not match merged source file! - mergeTask: "
									+ mergeTask, new Throwable());
			log.info("handleOverwrite() - remote source: "
					+ CoreStringUtils.quoteString(mergeTask.getRemoteSourceFileContent()));
			log.info("handleOverwrite() - merged source: "
					+ CoreStringUtils.quoteString(mergeTask.getMergedSourceFileContent()));
		}

		//All remote clones are remotely unchanged clones
		mergeResult.addClonesRemote(pendingRemoteClones, Type.UNCHANGED);

		if (pendingLocalClones.isEmpty())
		{
			log.trace("handleOverwrite() - no local clones, all remote clones are additions.");

			//there were no local clones, all remote clones are locally added clones
			mergeResult.addClonesLocal(pendingRemoteClones, Type.ADDED);

			pendingRemoteClones.clear();
		}
		else
		{
			//we need to do some more differentiated evaluation

			for (IClone remoteClone : pendingRemoteClones)
			{
				if (log.isTraceEnabled())
					log.trace("handleOverwrite() - evaluate remote clone: " + remoteClone);

				int idx = pendingLocalClones.indexOf(remoteClone);
				if (idx >= 0)
				{
					log.trace("handleOverwrite() - clone exists locally.");

					//the clone was moved, modified or is unchanged
					IClone localClone = pendingLocalClones.get(idx);

					int diffType = MergeUtils.evaluateDifference(remoteClone, localClone);
					if (diffType == MergeUtils.DIFF_TYPE_UNCHANGED)
					{
						log.trace("handleOverwrite() - clone was unchanged.");
						mergeResult.addCloneLocal(remoteClone, Type.UNCHANGED);
					}
					else
					{
						if ((diffType & MergeUtils.DIFF_TYPE_MOVED) != 0)
						{
							log.trace("handleOverwrite() - clone was moved.");
							mergeResult.addCloneLocal(remoteClone, Type.MOVED);
						}
						if ((diffType & MergeUtils.DIFF_TYPE_MODIFIED) != 0)
						{
							log.trace("handleOverwrite() - clone was modified.");
							mergeResult.addCloneLocal(remoteClone, Type.MODFIED);
						}
					}

					//we're done with this local clone
					pendingLocalClones.remove(idx);
				}
				else
				{
					log.trace("handleOverwrite() - clone was remotely added.");

					//the clone was added
					mergeResult.addCloneLocal(remoteClone, Type.ADDED);
				}
			}

			pendingRemoteClones.clear();

			//all remaining local clones were remotely deleted
			if (!pendingLocalClones.isEmpty())
			{
				if (log.isTraceEnabled())
					log.trace("handleOverwrite() - remotely deleted local clones: " + pendingLocalClones);

				mergeResult.addClonesLocal(pendingLocalClones, Type.REMOVED);
				pendingLocalClones.clear();
			}
		}
	}
}
