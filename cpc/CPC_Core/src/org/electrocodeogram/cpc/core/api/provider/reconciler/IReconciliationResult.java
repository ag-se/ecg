package org.electrocodeogram.cpc.core.api.provider.reconciler;


import java.util.List;

import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.hub.event.CloneModificationEvent;


/**
 * Structured return value for the {@link IReconcilerProvider}.
 * <br>
 * The semantics of the clone lists are similar to those of the {@link CloneModificationEvent}.
 * <br>
 * However, they are guaranteed to be non-null at all times.
 * 
 * @author vw
 * 
 * @see IReconcilerProvider
 * @see CloneModificationEvent
 */
public interface IReconciliationResult
{
	/**
	 * The final status/result of the reconciliation effort. 
	 */
	public enum Status
	{
		/**
		 * The reconciler was able to fully reconcile all changes.
		 * No clone data was lost.
		 */
		FULL_RECONCILIATION,

		/**
		 * The reconciler was able to reconcile some of the changes.
		 * Some clone data was lost.
		 */
		PARTIAL_RECONCILIATION,

		/**
		 * The reconciler was unable to reconcile the changes.
		 * All clone data was lost.
		 */
		NO_RECONCILIATION
	}

	/**
	 * The status may only be modified by the {@link IReconcilerProvider}.
	 * 
	 * @return the final status/result of the reconciliation effort, may be NULL during reconciliation.
	 * 		Guaranteed to be non-null once the {@link IReconcilerProvider} returns.
	 */
	public Status getStatus();

	/**
	 * Checks whether this result corresponds to a full reconciliation.
	 * <br>
	 * Convenience method.
	 * 
	 * @return true if {@link IReconciliationResult#getStatus()} is {@link Status#FULL_RECONCILIATION}.
	 */
	public boolean isFullyReconciled();

	/**
	 * A list of clones for which the <b>content</b> was modified due to the reconciliation.
	 * 
	 * @return list of modified clones, never null.
	 * 
	 * @see CloneModificationEvent#getModifiedClones()
	 */
	public List<IClone> getModifiedClones();

	/**
	 * A list of clones which were moved due to the reconciliation.
	 * <br>
	 * This also includes clone instances which had any other values (beside the content)
	 * modified, i.e. extension data.
	 * 
	 * @return list of moved clones, never null.
	 * 
	 * @see CloneModificationEvent#getMovedClones()
	 */
	public List<IClone> getMovedClones();

	/**
	 * Returns the clones which were removed due to the fact that the
	 * reconciled edits removed the clone ranges from the file.
	 * <p>
	 * Clones which were removed because their new positions could not be
	 * determined are <b>not</b> part of this list.
	 * <p>
	 * A clone which is in this list, may not be in any of the other lists.  
	 * 
	 * @return list of removed clones, never null.
	 * 
	 * @see IReconciliationResult#getLostClones()
	 * @see CloneModificationEvent#getRemovedClones()
	 */
	public List<IClone> getRemovedClones();

	/**
	 * Returns a list of clones for which the clone positions could not be reconciled.
	 * <p>
	 * If {@link Status#FULL_RECONCILIATION} is set, this method is guaranteed to return
	 * an empty list.
	 * <br>
	 * A clone which is in this list, may not be in any of the other lists.  
	 * 
	 * @return list of lost clones, never null.
	 */
	public List<IClone> getLostClones();

	/**
	 * Each implementation should provide a meaningful toString() method.
	 */
	public String toString();

}
