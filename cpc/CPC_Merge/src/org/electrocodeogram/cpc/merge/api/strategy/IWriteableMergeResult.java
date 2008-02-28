package org.electrocodeogram.cpc.merge.api.strategy;


import java.util.Collection;

import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeResult;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeResultPerspective;


/**
 * Extension interface of {@link IMergeResult} which allows {@link IMergeStrategy}s to
 * incrementally build and modify the final {@link IMergeResult} of a merge operation.
 * <p>
 * All {@link IClone} instance lists which are part of the {@link IMergeResultPerspective}s 
 * and the {@link ICloneFile} instance which can be obtained from an {@link IWriteableMergeResult}
 * (via the getters defined in {@link IMergeResult}) may be freely modified by an {@link IMergeStrategy}.
 * <p>
 * <b>NOTE:</b> The {@link IMergeResult#getMergedClones()} list <b>must not</b> be modified by a strategy.
 * 	It is a cached, read-only list which is generated on demand. 
 * 
 * @author vw
 */
public interface IWriteableMergeResult extends IMergeResult
{
	/**
	 * Specifies how a clone was affected by the merge operation.
	 */
	public enum Type
	{
		/**
		 * The clone was newly added.
		 */
		ADDED,

		/**
		 * The clone's position or other attributes were changed.
		 */
		MOVED,

		/**
		 * The content of the clone was modified.
		 */
		MODFIED,

		/**
		 * Combination of {@link Type#MOVED} and {@link Type#MODFIED}.
		 */
		MOVED_MODIFIED,

		/**
		 * The clone was removed due to a user modification.
		 */
		REMOVED,

		/**
		 * The clone was lost during the merge because its new position could not
		 * be determined.
		 */
		LOST,

		/**
		 * The clone was not affected by the merge.
		 */
		UNCHANGED
	}

	/**
	 * Sets the new status for this merge.
	 * <br>
	 * The default status is {@link IMergeResult.Status#NO_MERGE}.
	 * 
	 * @param status the new status for the merge operation, never null.
	 */
	public void setStatus(Status status);

	/**
	 * Adds the given clone to the clone lists of the local and remote perspective
	 * which correspond to the given types.
	 * <br> 
	 * Convenience method.
	 */
	public void addClone(IClone clone, Type localType, Type remoteType);

	/**
	 * Adds the given clones to the clone lists of the local and remote perspective
	 * which correspond to the given types.
	 * <br> 
	 * Convenience method.
	 */
	public void addClones(Collection<IClone> clones, Type localType, Type remoteType);

	/**
	 * Adds the given clone to the clone lists of the local perspective
	 * which correspond to the given type. 
	 * <br> 
	 * Convenience method.
	 */
	public void addCloneLocal(IClone clone, Type localType);

	/**
	 * Adds the given clones to the clone lists of the local perspective
	 * which correspond to the given type. 
	 * <br> 
	 * Convenience method.
	 */
	public void addClonesLocal(Collection<IClone> clones, Type localType);

	/**
	 * Adds the given clone to the clone lists of the remote perspective
	 * which correspond to the given type. 
	 * <br> 
	 * Convenience method.
	 */
	public void addCloneRemote(IClone clone, Type remoteType);

	/**
	 * Adds the given clones to the clone lists of the remote perspective
	 * which correspond to the given type. 
	 * <br> 
	 * Convenience method.
	 */
	public void addClonesRemote(Collection<IClone> clones, Type remoteType);
}
