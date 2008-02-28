package org.electrocodeogram.cpc.merge.strategy;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.special.IStatefulObject;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeResultPerspective;
import org.electrocodeogram.cpc.core.api.provider.merge.MergeException;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IReconcilerProvider;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IReconciliationResult;
import org.electrocodeogram.cpc.core.utils.CoreClonePositionUtils;
import org.electrocodeogram.cpc.merge.api.strategy.IMergeContext;
import org.electrocodeogram.cpc.merge.api.strategy.IMergeStrategy;
import org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask;
import org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult;
import org.electrocodeogram.cpc.merge.utils.MergeUtils;


/**
 * An {@link IMergeStrategy} which tries to handle a 3-Way-Merge as good as possible.
 * <p>
 * This strategy uses an {@link IReconcilerProvider} to find the
 * positions of the local and remote clones in the merged source file.<br/>
 * <br/>
 * First locally or remotely added clones are added to the result.<br/>
 * Then clones which were only lost on one side are readded.<br/>
 * After that clones which match in their remote and local versions are added.<br/>
 * And finally the remaining, mismatched clones are dropped.
 * 
 * @author vw
 */
public class FullMergeStrategy implements IMergeStrategy
{
	private static final Log log = LogFactory.getLog(FullMergeStrategy.class);

	private IReconcilerProvider reconcilerProvider;

	public FullMergeStrategy()
	{
		log.trace("FullMergeStrategy()");

		reconcilerProvider = (IReconcilerProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				IReconcilerProvider.class);
		assert (reconcilerProvider != null);

	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.merge.api.strategy.IMergeStrategy#merge(org.electrocodeogram.cpc.merge.api.strategy.IReadableMergeTask, org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult, java.util.LinkedList, java.util.LinkedList, java.util.LinkedList)
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
			log.trace("merge() - no pending base clones, probably only clone additions in this update, skipping.");
			return Status.SKIPPED;
		}

		/*
		 * Get updates positions for old local and remote clones.
		 */
		log.trace("merge() - searching old local clones in merged source.");
		ReconciliationResultWrapper localRecon = new ReconciliationResultWrapper(reconcilerProvider.reconcile(mergeTask
				.getLocalCloneFile(), mergeContext.getPendingLocalClones(), mergeTask.getLocalSourceFileContent(),
				mergeTask.getMergedSourceFileContent(), false), mergeContext.getPendingLocalClones());

		log.trace("merge() - searching old remote clones in merged source.");
		ReconciliationResultWrapper remoteRecon = new ReconciliationResultWrapper(reconcilerProvider.reconcile(
				mergeTask.getLocalCloneFile(), mergeContext.getPendingRemoteClones(), mergeTask
						.getRemoteSourceFileContent(), mergeTask.getMergedSourceFileContent(), false), mergeContext
				.getPendingRemoteClones());

		/*
		 * All locally added or remotely added clones which could be found in the merged source
		 * can be copied over into the final merge result.
		 */

		//locally added
		mergeAdditions(mergeResult.getLocalPerspective(), mergeResult.getRemotePerspective(), mergeContext
				.getPendingLocalClones(), mergeContext.getPendingBaseClones(), localRecon);

		//remotely added
		mergeAdditions(mergeResult.getRemotePerspective(), mergeResult.getLocalPerspective(), mergeContext
				.getPendingRemoteClones(), mergeContext.getPendingBaseClones(), remoteRecon);

		/*
		 * By the time we reach this point there should only be clones left which were neither
		 * added nor removed during this operation (they may still have been lost during the merge though).
		 */

		debugCheckPendingListsMatch(mergeTask, mergeResult, mergeContext.getPendingLocalClones(), mergeContext
				.getPendingRemoteClones(), mergeContext.getPendingBaseClones());

		/*
		 * The local and remote clones can differ in content, in position and in extra data though.
		 * Furthermore their positions are likely to have changed due to the merge.
		 * 
		 * Some crude heuristics:
		 * 
		 * 1) if a clone was lost on one side and has a position on the other side, we take the other side.
		 *
		 * 2) if a clone was found in the same position in the merged result by both sides, we're lucky :o)
		 * 	  (same position in merged file implies that the content matches)
		 * 
		 * 3) if a clone was found in different positions or has different content, we log a warning and
		 * 	  drop the clone.
		 */

		/*
		 * 1) lost/removed on only one side
		 */

		//locally lost/removed
		mergeLost(mergeResult.getLocalPerspective(), mergeResult.getRemotePerspective(), mergeContext
				.getPendingLocalClones(), mergeContext.getPendingRemoteClones(), mergeContext.getPendingBaseClones(),
				localRecon, remoteRecon);

		//remotely lost/removed
		mergeLost(mergeResult.getRemotePerspective(), mergeResult.getLocalPerspective(), mergeContext
				.getPendingRemoteClones(), mergeContext.getPendingLocalClones(), mergeContext.getPendingBaseClones(),
				remoteRecon, localRecon);

		debugCheckPendingListsMatch(mergeTask, mergeResult, mergeContext.getPendingLocalClones(), mergeContext
				.getPendingRemoteClones(), mergeContext.getPendingBaseClones());

		/*
		 * 2) matching positions
		 */

		//locally & remotely
		mergeMatching(mergeTask, mergeResult, mergeContext, mergeContext.getPendingLocalClones(), mergeContext
				.getPendingRemoteClones(), mergeContext.getPendingBaseClones(), localRecon, remoteRecon);

		debugCheckPendingListsMatch(mergeTask, mergeResult, mergeContext.getPendingLocalClones(), mergeContext
				.getPendingRemoteClones(), mergeContext.getPendingBaseClones());

		/*
		 * 3) position mismatch, we don't do anything with those for now.
		 * They will be dropped by the fallback strategy.
		 */

		//		boolean dataLost = mergeMissmatch(mergeResult, mergeContext.getPendingLocalClones(), mergeContext
		//				.getPendingRemoteClones(), mergeContext.getPendingBaseClones());
		/*
		 * All done :o)
		 */

		if (mergeContext.isLocalOrRemoteClonePending())
			return Status.PARTIAL;
		else
			return Status.FULL;
	}

	//	/**
	//	 * 
	//	 * @return true if data was lost, false otherwise.
	//	 */
	//	private boolean mergeMissmatch(IWriteableMergeResult mergeResult, LinkedList<IClone> pendingLocalClones,
	//			LinkedList<IClone> pendingRemoteClones, LinkedList<IClone> pendingBaseClones)
	//	{
	//		log.trace("mergeMissmatch()");
	//
	//		boolean dataLost = false;
	//
	//		if (!pendingLocalClones.isEmpty())
	//		{
	//			if (log.isTraceEnabled())
	//				log.trace("mergeMissmatch() - position mismatch for local clones, dropping - pendingLocalClones: "
	//						+ pendingLocalClones);
	//
	//			mergeResult.addClonesLocal(pendingLocalClones, Type.LOST);
	//
	//			pendingBaseClones.removeAll(pendingLocalClones);
	//			pendingLocalClones.clear();
	//
	//			dataLost = true;
	//		}
	//
	//		if (!pendingRemoteClones.isEmpty())
	//		{
	//			if (log.isTraceEnabled())
	//				log.trace("mergeMissmatch() - position mismatch for remote clones, dropping - pendingRemoteClones: "
	//						+ pendingRemoteClones);
	//
	//			mergeResult.addClonesRemote(pendingRemoteClones, Type.LOST);
	//
	//			pendingBaseClones.removeAll(pendingRemoteClones);
	//			pendingRemoteClones.clear();
	//
	//			dataLost = true;
	//		}
	//
	//		return dataLost;
	//	}

	/**
	 * Copies over all clones which are located at the same positions in the resulting merged file.<br/>
	 * The clones are added to the correct MOVED/MODIFIED/UNCHANGED lists of both sides.
	 */
	private void mergeMatching(IReadableMergeTask mergeTask, IWriteableMergeResult mergeResult,
			IMergeContext mergeContext, LinkedList<IClone> pendingLocalClones, LinkedList<IClone> pendingRemoteClones,
			LinkedList<IClone> pendingBaseClones, ReconciliationResultWrapper localReconciliation,
			ReconciliationResultWrapper remoteReconciliation) throws MergeException
	{
		log.trace("mergeMatching() - for both perspectives.");

		/*
		 * By this point in time we will have taken care of:
		 * - all locally, remotely and locally&remotely removed clones
		 * - all locally or remotely added clones
		 * - all locally or remotely lost/removed clones during merge which still existed on the other side
		 * 
		 * This means that all clones should exist on both sides and should only be moved/modified.
		 */

		Iterator<IClone> iter = pendingLocalClones.iterator();
		//for (IClone localClone : pendingLocalClones)
		while (iter.hasNext())
		{
			IClone localClone = iter.next();

			IClone remoteClone = remoteReconciliation.getSourceMap().get(localClone.getUuid());
			IClone newLocalClone = localReconciliation.getResultMap().get(localClone.getUuid());
			IClone newRemoteClone = remoteReconciliation.getResultMap().get(localClone.getUuid());

			if (remoteClone == null || newLocalClone == null || newRemoteClone == null)
			{
				log.error("mergeMatching() - new local or remote or new remote clone not found - newLocalClone: "
						+ newLocalClone + ", remoteClone: " + remoteClone + ", newRemoteClone: " + newRemoteClone
						+ ", localClone: " + localClone + ", mergeResult: " + mergeResult, new Throwable());
				continue;
			}

			if (newLocalClone.getOffset() == newRemoteClone.getOffset()
					&& newLocalClone.getLength() == newRemoteClone.getLength())
			{
				if (log.isTraceEnabled())
					log.trace("mergeMatching() - clone positions match - newLocalClone: " + newLocalClone
							+ ", newRemoteClone: " + newRemoteClone);

				/*
				 * Even though the clone position matches, the remaining clone data (i.e. extensions)
				 * might still differ. However, in this simple merge provider we do not take this into
				 * account.
				 * We do one check here and print a warning if this might be the case.
				 * 
				 * We will always prefer the remote clone data over the local one.
				 */
				if (!CoreClonePositionUtils.statefulObjectsEqual((IStatefulObject) newLocalClone,
						(IStatefulObject) newRemoteClone, true))
				{
					log.warn("mergeMatching() - even though the position data matches, some other data differs, "
							+ "sub clone merging not implemented, falling back to remote clone data - newLocalClone: "
							+ newLocalClone + ", newRemoteClone: " + newRemoteClone, new Throwable());
				}

				//the remote clone becomes our new merged clone
				IClone mergedClone;
				try
				{
					mergedClone = (IClone) newRemoteClone.clone();
				}
				catch (CloneNotSupportedException e)
				{
					log.error(
							"mergeMatching() - unable to clone clone instance - clone: " + newRemoteClone + " - " + e,
							e);
					throw new MergeException("unable to clone clone instance - clone: " + newRemoteClone + " - " + e, e);
				}

				//merge potential extension data
				IClone baseCloneObject = getCloneFromList(mergeTask.getBaseClones(), mergedClone);
				mergeContext.getCloneObjectExtensionMerger().merge(mergeTask, mergeResult, newLocalClone,
						newRemoteClone, baseCloneObject, mergedClone);

				//check how the new data differs from the old local data
				//and add the clone to the corresponding lists of the perspective
				evaluateCloneDifferenceAndAddToPerspective(localClone, mergedClone, mergeResult.getLocalPerspective());

				//check how the new data differs from the old remote data
				//and add the clone to the corresponding lists of the perspective
				evaluateCloneDifferenceAndAddToPerspective(remoteClone, mergedClone, mergeResult.getRemotePerspective());

				//this clone has now been handled
				//pendingLocalClones.remove(localClone);
				iter.remove();
				pendingRemoteClones.remove(localClone);
				pendingBaseClones.remove(localClone);
			}
		}
	}

	/**
	 * Looks at the removed and lost clones of this side and checks if they still exist on the other side.<br/>
	 * If they do, the values from the other side are used.<br/>
	 * The clones are added to the correct MOVED/MODIFIED/UNCHANGED lists of this and the other side.
	 */
	private void mergeLost(IMergeResultPerspective thisPerspective, IMergeResultPerspective otherPerspective,
			LinkedList<IClone> thisPendingClones, LinkedList<IClone> otherPendingClones,
			LinkedList<IClone> pendingBaseClones, ReconciliationResultWrapper thisReconciliation,
			ReconciliationResultWrapper otherReconciliation)
	{
		if (log.isTraceEnabled())
			log.trace("mergeLost() - for perspective: " + thisPerspective.getName());

		//build a list with all removed or lost clones on this side
		List<IClone> deletedClones = new ArrayList<IClone>(thisReconciliation.getReconciliationResult().getLostClones()
				.size()
				+ thisReconciliation.getReconciliationResult().getRemovedClones().size());
		deletedClones.addAll(thisReconciliation.getReconciliationResult().getLostClones());
		deletedClones.addAll(thisReconciliation.getReconciliationResult().getRemovedClones());

		//build a list with all remaining clones on the other side (not yet updated version!)
		//		Set<IClone> otherRemainingClones = new HashSet<IClone>(otherPendingClones);
		//		otherRemainingClones.removeAll(otherReconciliation.getReconciliationResult().getLostClones());
		//		otherRemainingClones.removeAll(otherReconciliation.getReconciliationResult().getRemovedClones());

		//for each clone which was removed or lost on this side
		for (IClone deletedClone : deletedClones)
		{
			//check if it still exists on the other side
			//			if (otherRemainingClones.contains(deletedClone))
			if (otherReconciliation.getResultMap().containsKey(deletedClone.getUuid())
					&& otherPendingClones.contains(deletedClone))
			{
				if (log.isTraceEnabled())
					log
							.trace("mergeLost() - clone was removed/lost on this side but still exists on other side - clone: "
									+ deletedClone);

				//get the new clone data
				//this also adds the clone to the correct lists for the other perspective
				IClone newClone = findNewClone(otherPerspective, otherReconciliation, deletedClone);

				//now we need to decide about the correct lists for this perspective
				int diff = MergeUtils.evaluateDifference(deletedClone, newClone);
				if (diff == MergeUtils.DIFF_TYPE_UNCHANGED)
				{
					log.trace("mergeLost() - clone was unchanged (UNCHANGED @ " + thisPerspective.getName() + ").");
					thisPerspective.getUnchangedClones().add(newClone);
				}
				else
				{
					if ((diff & MergeUtils.DIFF_TYPE_MODIFIED) != 0)
					{
						log.trace("mergeLost() - clone was modified (MODDIFIED @ " + thisPerspective.getName() + ").");
						thisPerspective.getModifiedClones().add(newClone);
					}
					if ((diff & MergeUtils.DIFF_TYPE_MOVED) != 0)
					{
						log.trace("mergeLost() - clone was moved (MOVED @ " + thisPerspective.getName() + ").");
						thisPerspective.getMovedClones().add(newClone);
					}
				}

				//this clone has been handled, remove it from the pending lists
				thisPendingClones.remove(deletedClone);
				otherPendingClones.remove(deletedClone);
				pendingBaseClones.remove(deletedClone);
			}
		}
	}

	/**
	 * Adds clones which were added on this side to this sides MOVED/MODIFIED/UNCHANGED lists and the other
	 * sides ADDED list. The added clones are removed from the pending clone lists. 
	 */
	private void mergeAdditions(IMergeResultPerspective thisPerspective, IMergeResultPerspective otherPerspective,
			LinkedList<IClone> thisPendingClones, LinkedList<IClone> pendingBaseClones,
			ReconciliationResultWrapper reconciliation)
	{
		if (log.isTraceEnabled())
			log.trace("mergeAdditions() - for perspective: " + thisPerspective.getName());

		Set<IClone> remainingClones = new HashSet<IClone>(thisPendingClones);
		remainingClones.removeAll(reconciliation.getReconciliationResult().getLostClones());
		remainingClones.removeAll(reconciliation.getReconciliationResult().getRemovedClones());

		Iterator<IClone> iter = remainingClones.iterator();
		while (iter.hasNext())
		{
			IClone clone = iter.next();

			if (!pendingBaseClones.contains(clone))
			{
				//clone was added on this side
				if (log.isTraceEnabled())
					log.trace("mergeAdditions() - clone was added on this side (" + thisPerspective.getName()
							+ ") (other side: ADD)- clone: " + clone);

				//get the new clone data
				IClone newClone = findNewClone(thisPerspective, reconciliation, clone);

				//add it to the other perspective as ADDED
				otherPerspective.getAddedClones().add(newClone);

				//remote the clone entry, it has been handled
				thisPendingClones.remove(clone);
				pendingBaseClones.remove(clone);
				iter.remove();
			}
		}
	}

	/**
	 * Extracts the new version of the given clone from the reconciliation result if the clone was
	 * moved or modified. Otherwise the given clone is returned.<br/>
	 * Also adds the clone to the MODIFIED/MOVED/UNCHANGED lists of the given perspective. 
	 */
	private IClone findNewClone(IMergeResultPerspective perspective, ReconciliationResultWrapper reconciliation,
			IClone clone)
	{
		IClone newClone = null;

		//decide on the type of change for this perspective
		int newIdx = reconciliation.getReconciliationResult().getModifiedClones().indexOf(clone);
		if (newIdx >= 0)
		{
			//the clone was modified
			newClone = reconciliation.getReconciliationResult().getModifiedClones().get(newIdx);

			if (log.isTraceEnabled())
				log.trace("findNewClone() - clone was modified (MODDIFIED @ " + perspective.getName() + ") - clone: "
						+ newClone);

			//add it to this perspective as MODIFIED
			perspective.getModifiedClones().add(newClone);
		}

		newIdx = reconciliation.getReconciliationResult().getMovedClones().indexOf(clone);
		if (newIdx >= 0)
		{
			//the clone was moved
			newClone = reconciliation.getReconciliationResult().getMovedClones().get(newIdx);

			if (log.isTraceEnabled())
				log.trace("findNewClone() - clone was moved (MOVED @ " + perspective.getName() + ") - clone: "
						+ newClone);

			//add it to this perspective as MOVED
			perspective.getMovedClones().add(newClone);
		}

		if (newClone == null)
		{
			//the clone was unchanged
			newClone = clone;

			if (log.isTraceEnabled())
				log.trace("findNewClone() - clone was unchanged (UNCHANGED @ " + perspective.getName() + ") - clone: "
						+ newClone);

			perspective.getUnchangedClones().add(newClone);
		}

		return newClone;
	}

	/**
	 * Takes the old and new version of a clone and a perspective and evaluates the type
	 * of modification done to the clone. The new version of the clone will then be
	 * added to the correct UNCHANGED/MODIFIED/MOVED lists of the given perspective.
	 */
	private void evaluateCloneDifferenceAndAddToPerspective(IClone oldClone, IClone newClone,
			IMergeResultPerspective perspective)
	{
		int diff = MergeUtils.evaluateDifference(oldClone, newClone);

		if (diff == MergeUtils.DIFF_TYPE_UNCHANGED)
		{
			log.trace("evaluateCloneDifferenceAndAddToPerspective() - clone was unchanged (UNCHANGED @ "
					+ perspective.getName() + ").");
			perspective.getUnchangedClones().add(newClone);
		}
		else
		{
			if ((diff & MergeUtils.DIFF_TYPE_MODIFIED) != 0)
			{
				log.trace("evaluateCloneDifferenceAndAddToPerspective() - clone was modified (MODDIFIED @ "
						+ perspective.getName() + ").");
				perspective.getModifiedClones().add(newClone);
			}
			if ((diff & MergeUtils.DIFF_TYPE_MOVED) != 0)
			{
				log.trace("evaluateCloneDifferenceAndAddToPerspective() - clone was moved (MOVED @ "
						+ perspective.getName() + ").");
				perspective.getMovedClones().add(newClone);
			}
		}
	}

	/**
	 * For a given clone object this method retrieves the corresponding clone object from a list. 
	 * 
	 * @param baseClones may be NULL.
	 * @param mergedClone may be NULL.
	 * @return may be NULL.
	 */
	private IClone getCloneFromList(List<IClone> clones, IClone clone)
	{
		if (clones == null || clone == null)
			return null;

		int idx = clones.indexOf(clone);
		return clones.get(idx);
	}

	/**
	 * Checks whether the local and remote pending lists are of equal size and contain the same
	 * clones (uuid match).
	 */
	private void debugCheckPendingListsMatch(IReadableMergeTask mergeTask, IWriteableMergeResult mergeResult,
			LinkedList<IClone> pendingLocalClones, LinkedList<IClone> pendingRemoteClones,
			LinkedList<IClone> pendingBaseClones)
	{
		//some integrity checking, pending local and pending remote clone lists should contain the same clones
		if (pendingLocalClones.size() != pendingRemoteClones.size() || !pendingLocalClones.equals(pendingRemoteClones))
		{
			log.warn("merge() - local and remote clone lists don't match - pending local: " + pendingLocalClones
					+ ", pending remote: " + pendingRemoteClones + ", pending base: " + pendingBaseClones
					+ ", mergeTask: " + mergeTask + ", mergeResult: " + mergeResult, new Throwable());
		}
	}

	/**
	 * Simple {@link IReconciliationResult} wrapper for convenience and performance reasons.<br/>
	 * Allows easy access to the resulting clone instances in the final reconciled result by UUID.
	 */
	private class ReconciliationResultWrapper
	{
		private IReconciliationResult reconciliationResult;
		private Map<String, IClone> resultMap;
		private Map<String, IClone> sourceMap;

		ReconciliationResultWrapper(IReconciliationResult reconciliationResult, List<IClone> originalClones)
		{
			this.reconciliationResult = reconciliationResult;

			sourceMap = new HashMap<String, IClone>(originalClones.size());
			resultMap = new HashMap<String, IClone>(originalClones.size());
			for (IClone clone : reconciliationResult.getModifiedClones())
				resultMap.put(clone.getUuid(), clone);
			for (IClone clone : reconciliationResult.getMovedClones())
				resultMap.put(clone.getUuid(), clone);
			for (IClone clone : originalClones)
			{
				sourceMap.put(clone.getUuid(), clone);

				if (!reconciliationResult.getLostClones().contains(clone)
						&& !reconciliationResult.getRemovedClones().contains(clone))
					resultMap.put(clone.getUuid(), clone);
			}
		}

		IReconciliationResult getReconciliationResult()
		{
			return reconciliationResult;
		}

		/**
		 * A map which contains clone UUID to clone mappings for all clones which still
		 * exist in the final reconciliation result (lost and removed clones are not part of the map). 
		 */
		Map<String, IClone> getResultMap()
		{
			return resultMap;
		}

		/**
		 * A map which contains clone UUID to clone mappings for all clones which are part
		 * of the original clone list (the returned clone instances are the <b>pre-reconciliation</b> versions).  
		 */
		Map<String, IClone> getSourceMap()
		{
			return sourceMap;
		}
	}
}
