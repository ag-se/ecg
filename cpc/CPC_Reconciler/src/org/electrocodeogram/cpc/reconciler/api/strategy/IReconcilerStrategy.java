package org.electrocodeogram.cpc.reconciler.api.strategy;


import java.util.LinkedList;
import java.util.List;

import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IDiffResult;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IReconcilerProvider;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IReconciliationResult;


/**
 * Interface for {@link IReconcilerProvider} reconciliation strategies.
 * <p>
 * Every implementation needs to provide a <b>no-argument constructor</b>.
 * <p>
 * Implementations of this interface are treated as Singletons. An instance of each strategy will be generated
 * at startup and will then be reused whenever needed.
 * 
 * @author vw
 * 
 * @see IReconcilerProvider
 * @see IReconciliationResult
 */
//TODO: add example
public interface IReconcilerStrategy
{
	/**
	 * Return status indicator for {@link IReconcilerStrategy#reconcile(ICloneFile, List, String, String, List, LinkedList, IReconciliationResult)}.
	 */
	public enum Status
	{
		/**
		 * Indicates that the strategy did not make any modifications to the {@link IReconciliationResult}.
		 */
		SKIPPED,

		/**
		 * Indicates that the strategy made some modifications to the {@link IReconciliationResult}.
		 * <br>
		 * However, not all clone positions could be fully reconciled.
		 */
		PARTIAL,

		/**
		 * Indicates that the strategy successfully reconciled all (remaining) clone positions.
		 * <br>
		 * The {@link IReconciliationResult} is potentially in it's final state.
		 * <p>
		 * Further strategies will still be executed in order to have a chance to reject the result.
		 */
		FULL,

		/**
		 * Indicates that this event should not be passed on to any more strategies and that
		 * the {@link IReconciliationResult} is in it's final stage.
		 * <p>
		 * A strategy will typically return this value if it detected a special situation which may
		 * confuse other strategies or if it needs to make sure that no other strategy will
		 * override its decision. 
		 */
		BREAK
	}

	/**
	 * This method is called for each registered reconciliation strategy in turn (in order of their priority)
	 * until one method returns {@link Status#BREAK} or all strategies have been called.
	 * <p>
	 * Only the <em>pendingClones</em> and <em>result</em> parameters may be modified.
	 * <br>
	 * All other parameters are to be considered <b>read only</b>.
	 * <p>
	 * A clone to be added to the <em>result</em> may <b>only</b> be taken from the <em>pendingClones</em>
	 * list or from within the <em>result</em> object.
	 * As only that list and the <em>result</em> object are guaranteed to contain cloned instances.
	 * <br>
	 * The elements of the <em>persistedClones</em> list are not cloned and may not be modified.
	 * <br>
	 * A strategy <b>must not clone any objects itself</b>.
	 * <p> 
	 * A strategy should ignore any clone which it can't find. Only clones which were definitely removed should
	 * be added to the {@link IReconciliationResult} removed clones list.
	 * <br>
	 * Once all strategies have been run or one strategy returned {@link Status#BREAK}, all remaining clones
	 * in the <em>pendingClones</em> list, will be added to the lost clones list automatically.
	 * 
	 * @param cloneFile the clone file which was modified, never null.
	 * @param persistedClones the currently persisted clone data for the file, never null.
	 * 		Clone list is <b>sorted by start offset</b>.
	 * @param persistedFileContent the currently persisted content for the file, may be NULL.
	 * @param newFileContent the new content which was produced by some external modification to the file, may be NULL.
	 * @param differences the differences between the persisted and the new file content, never null.
	 * 		Differences are sorted by start offset.
	 * @param pendingClones a list of clones which have not yet been reconciled, this is the "todo list" for each strategy.
	 * 		A strategy should remove any clones which it adds to the <em>result</em> from this list.
	 * 		Clone list is <b>sorted by start offset</b>. 	
	 * 		This is the only parameter besides <em>result</em> which may be modified.
	 * @param result the current, incrementally filled {@link IReconciliationResult}.
	 * 		This is the only parameter besides <em>pendingClones</em> which may be modified.
	 * @return a status flag indicating the type of modifications done by this strategy. 
	 */
	public Status reconcile(ICloneFile cloneFile, List<IClone> persistedClones, String persistedFileContent,
			String newFileContent, List<IDiffResult> differences, LinkedList<IClone> pendingClones,
			IReconciliationResult result);
}
