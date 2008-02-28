package org.electrocodeogram.cpc.core.api.provider.merge;


import java.util.List;

import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;


/**
 * A result wrapper object for the {@link IMergeProvider}.
 * 
 * @author vw
 * 
 * @see IMergeProvider
 * @see IMergeProvider#merge(IMergeTask)
 */
public interface IMergeResult
{
	/**
	 * Possible result status values for a merge operation. 
	 */
	public enum Status
	{
		/**
		 * All data was successfully merged.
		 */
		FULL_MERGE,

		/**
		 * Some data was successfully merged.
		 * <br>
		 * Some data could not be merged and was dropped.
		 */
		PARTIAL_MERGE,

		/**
		 * It was not possible to merge the data.
		 * <br>
		 * All clone data for the file was lost.
		 */
		NO_MERGE
	}

	/**
	 * Information about the success or failure of this merge operation.
	 * 
	 * @return status of the merge operation, never null.
	 * 
	 * @see IMergeResult.Status
	 */
	public Status getStatus();

	/**
	 * Checks whether this result represents a fully merged state.
	 * <br>
	 * Convenience method.
	 * 
	 * @return true if {@link IMergeResult#getStatus()} is {@link Status#FULL_MERGE}.
	 */
	public boolean isFullyMerged();

	/**
	 * The new {@link ICloneFile} data for the merged file.
	 * 
	 * @return merged {@link ICloneFile}, never null.
	 */
	public ICloneFile getCloneFile();

	/**
	 * Description of the merge implications from the perspective of the local clone data.
	 * <p>
	 * This is the perspective needed to update the local {@link IStoreProvider}.
	 * 
	 * @return local perspective of the merge implications, never null.
	 * 
	 * @see IMergeResultPerspective
	 */
	public IMergeResultPerspective getLocalPerspective();

	/**
	 * Description of the merge implications from the perspective of the remote clone data.
	 * <p>
	 * In most cases a client will probably only need the {@link IMergeResult#getLocalPerspective()}
	 * or {@link IMergeResult#getMergedClones()}.
	 * 
	 * @return remote perspective of the merge implications, never null.
	 * 
	 * @see IMergeResultPerspective
	 */
	public IMergeResultPerspective getRemotePerspective();

	/**
	 * A list of the final {@link IClone} instances for the merged source file.
	 * <br>
	 * The list does not contain duplicates.
	 * <p>
	 * This data might be calculated on demand. A call might therefore be expensive.
	 * <br>
	 * <b>This method is not guaranteed to be thread save.</b>
	 * <p>
	 * This is equivalent to (after removing duplicates):
	 * <br>
	 * <code>getAddedClones()+getMovedClones()+getModifiedClones()+getUnchangedClones()</code>
	 * <p>
	 * A client which only intents to update an {@link IStoreProvider} will not need this information.
	 * <p>
	 * The order of the {@link IClone} instances in this list is not defined.
	 * 
	 * @return a complete list of {@link IClone} instances for the final merged source file, may be empty, never null.
	 */
	public List<IClone> getMergedClones();

	/**
	 * All implementations should provide a meaningful toString() method for debugging purposes. 
	 */
	public String toString();
}
