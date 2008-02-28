package org.electrocodeogram.cpc.merge.api.strategy;


import org.electrocodeogram.cpc.core.api.provider.merge.IMergeProvider;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeResult;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeTask;
import org.electrocodeogram.cpc.core.api.provider.merge.MergeException;


/**
 * Interface for {@link IMergeProvider} merge strategies.
 * <p>
 * Every implementation needs to provide a <b>no-argument constructor</b>.
 * <p>
 * Implementations of this interface are treated as Singletons. An instance of each strategy will be generated
 * at startup and will then be reused whenever needed.
 * 
 * @author vw
 * 
 * @see IMergeProvider
 * @see IMergeTask
 * @see IMergeResult
 */
//TODO: add example
public interface IMergeStrategy
{
	/**
	 * Return status indicator for {@link IMergeStrategy#merge(IReadableMergeTask, IWriteableMergeResult, IMergeContext)}.
	 */
	public enum Status
	{
		/**
		 * Indicates that the strategy did not make any modifications to the {@link IMergeResult}.
		 */
		SKIPPED,

		/**
		 * Indicates that the strategy made some modifications to the {@link IMergeResult}.
		 * <br>
		 * However, not all clone positions could be fully merged.
		 */
		PARTIAL,

		/**
		 * Indicates that the strategy successfully merged all (remaining) clone positions.
		 * <br>
		 * The {@link IMergeResult} is potentially in it's final state.
		 * <p>
		 * Further strategies will still be executed in order to have a chance to reject the result.
		 */
		FULL,

		/**
		 * Indicates that this event should not be passed on to any more strategies and that
		 * the {@link IMergeResult} is in it's final stage.
		 * <p>
		 * A strategy will typically return this value if it detected a special situation which may
		 * confuse other strategies or if it needs to make sure that no other strategy will
		 * override its decision. 
		 */
		BREAK
	}

	/**
	 * Tries to merge the <em>pendingLocalClones</em> and <em>pendingRemoteClones</em> according to the
	 * data given in the <em>mergeTask</em>.
	 * <p>
	 * May additionally processes <em>pendingBaseClones</em> in case of 3-way-merges.<br/>
	 * See also: {@link IReadableMergeTask#isThreeWayMerge()}.
	 * <p>
	 * The pending base clones list does not need to be completely processed. The pending local and remote
	 * clone lists should be empty once <b>all</b> strategies have been executed.
	 * <br>
	 * Usually the last ("fallback") strategy will mark all remaining, pending local and remote clones as
	 * lost clones.
	 * <p>
	 * The <em>pendingLocalClones</em>, <em>pendingRemoteClones</em> and <em>pendingBaseClones</em> as well
	 * as some support functions like a {@link ICloneObjectExtensionMerger} are available via the provided
	 * {@link IMergeContext}.
	 * 
	 * @param mergeTask read-only description of the merge task at hand, the contents must not be modified in any way, never null.
	 * @param mergeResult a writable result wrapper which is used to incrementally build up the final merge result, never null.
	 * 		An {@link IMergeStrategy} may freely modify this object. However, care must be taken that a clone is not added
	 * 		multiple times to the same list. All clones which are added to the result need to be removed from <b>both</b>
	 * 		pending clones lists.
	 * @param mergeContext a collection of progress/status information about the current merge as well as some utility functions,
	 * 		never null.
	 * 		The context can be used to get access to lists of the still pending local/remote/base clones and to a merger for
	 * 		clone extensions.
	 * @return the {@link IMergeResult.Status} result status for this strategy, never null.
	 * 
	 * @throws MergeException to be thrown when a serious error is detected. An exception should not be thrown if it can be expected
	 * 		that some other strategy might be able to handle this situation. All in all an exception should only be thrown in very extreme cases.
	 */
	public Status merge(IReadableMergeTask mergeTask, IWriteableMergeResult mergeResult, IMergeContext mergeContext)
			throws MergeException;
}
