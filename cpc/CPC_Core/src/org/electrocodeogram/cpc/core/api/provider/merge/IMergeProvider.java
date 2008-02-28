package org.electrocodeogram.cpc.core.api.provider.merge;


import org.electrocodeogram.cpc.core.api.provider.IProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;


/**
 * A part of the Remote Store API, a merge provider takes local and remote clone data and tries
 * to reconcile any conflicts by merging the clone data to correctly reflect the new contents
 * of the corresponding source file.
 * <p>
 * Source files are <b>not</b> merged by an {@link IMergeProvider}. This is left to the normal
 * Eclipse procedures. By the time the merge provider is called, the result of the source file
 * merge is already available.
 * <p>
 * A {@link IMergeProvider} must not have any side effects. It must neither access the
 * {@link IStoreProvider} nor the workspace resources in any way.
 * 
 * @author vw
 * 
 * @see IMergeTask
 * @see IMergeResult
 */
public interface IMergeProvider extends IProvider
{
	/**
	 * Merges local and remote clone data to reflect the new contents of the corresponding
	 * source file.
	 * <p>
	 * The concrete merging procedure is left to the implementation. A caller must not make
	 * any assumptions about any properties of the merge process.
	 * <br>
	 * It is up to the implementation to decide whether to attempt a Three-Way merge. The
	 * presence of the required data in the {@link IMergeTask} does not guarantee that a
	 * Three-Way merge will be executed.
	 * 
	 * @param mergeTask the merge task descriptor for this operation, never null.
	 * @return a merge result descriptor, never null.
	 * 
	 * @see IMergeTask
	 * @see IMergeResult
	 * 
	 * @throws IllegalArgumentException if the provided {@link IMergeTask} is not "valid".
	 * @throws MergeException if any errors occur during the merge process.
	 */
	public IMergeResult merge(IMergeTask mergeTask) throws IllegalArgumentException, MergeException;

	/**
	 * Creates a new, empty {@link IMergeTask} instance which can then be filled with
	 * all the required data to descripe the merge task.
	 * <p>
	 * The returned {@link IMergeTask} is not yet "valid". It must not be passed to
	 * {@link IMergeProvider#merge(IMergeTask)} until all required fields have been set.
	 * 
	 * @return new, empty {@link IMergeTask} instance, never null.
	 * 
	 * @see IMergeTask
	 */
	public IMergeTask createTask();
}
