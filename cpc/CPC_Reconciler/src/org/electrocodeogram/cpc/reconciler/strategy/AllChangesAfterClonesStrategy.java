package org.electrocodeogram.cpc.reconciler.strategy;


import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IDiffResult;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IReconciliationResult;
import org.electrocodeogram.cpc.reconciler.api.strategy.IReconcilerStrategy;


/**
 * Very simple strategy which only checks whether all modifications might have occured after the last
 * clone in the file. In that case all clone data is up to date and no more modifications are needed.<br/>
 * <br/>
 * If that is not the case it checks whether any of the clones in the file are located before the first
 * modification. Those are then directly marked as valid.
 *  
 * @author vw
 */
public class AllChangesAfterClonesStrategy implements IReconcilerStrategy
{
	private static Log log = LogFactory.getLog(AllChangesAfterClonesStrategy.class);

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.reconciler.strategy.IExternalModificationReconcilerStrategy#reconcile(org.electrocodeogram.cpc.core.api.data.ICloneFile, java.util.List, java.lang.String, java.lang.String, java.util.List, java.util.List, org.electrocodeogram.cpc.core.api.provider.reconciler.ReconciliationResult)
	 */
	@Override
	public Status reconcile(ICloneFile cloneFile, List<IClone> persistedClones, String persistedFileContent,
			String newFileContent, List<IDiffResult> differences, LinkedList<IClone> pendingClones,
			IReconciliationResult result)
	{
		if (log.isTraceEnabled())
			log.trace("reconcile() - ..., pendingClones: " + pendingClones + ", result: " + result);

		/*
		 * This strategy does not re-evaluate any clones which have already been handled by
		 * another strategy. This means that there is nothing to do if pendingClones is empty.
		 */
		if (pendingClones.isEmpty())
		{
			log.trace("reconcile() - pendingClones is empty, SKIPPING");
			return Status.SKIPPED;
		}

		//cache some values
		IClone firstClone = pendingClones.getFirst();
		IClone lastClone = pendingClones.getLast();
		IDiffResult firstDiff = differences.get(0);

		/*
		 * Now check the clone positions in relation to the first diff. 
		 */

		//are all clones located before the first change?
		if (lastClone.getEndOffset() < firstDiff.getOffset())
		{
			//ok, all modifications to the file are located AFTER the last clone in the file.
			//this means that no clone positions where modified.
			log.trace("reconcile() - all changes after clones, clone data unchanged - FULL");

			pendingClones.clear();
			return Status.FULL;
		}
		//is at least one clone located before the first change?
		else if (firstClone.getEndOffset() < firstDiff.getOffset())
		{
			//ok, mark all clones which are located before the first change as reconciled

			//find the index of the first clone which HAS been modified
			int modifiedPos = 0;
			for (IClone clone : pendingClones)
			{
				if (clone.getEndOffset() >= firstDiff.getOffset())
					break;

				++modifiedPos;
			}

			//remove all clones before modifiedPos from the pendingClones list
			for (int i = 0; i < modifiedPos; ++i)
				pendingClones.removeFirst();

			return Status.PARTIAL;
		}
		//otherwise, all clones may be affected
		else
		{
			//there's nothing we can do
			log.trace("reconcile() - all clones are potentially modified - SKIPPED");
			return Status.SKIPPED;
		}
	}
}
