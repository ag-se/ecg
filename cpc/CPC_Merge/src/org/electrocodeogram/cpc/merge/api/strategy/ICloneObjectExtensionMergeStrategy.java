package org.electrocodeogram.cpc.merge.api.strategy;


import java.util.LinkedList;

import org.electrocodeogram.cpc.core.api.data.ICloneObject;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeProvider;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeResult;
import org.electrocodeogram.cpc.merge.provider.MergeProvider;


/**
 * A special support API which allows {@link ICloneObjectExtension}s and other modules to contribute
 * special handling code for merging of {@link ICloneObjectExtension} data.
 * <p>
 * Every implementation needs to provide a <b>no-argument constructor</b>.
 * <p>
 * Implementations of this interface are treated as Singletons. An instance of each strategy will be generated
 * at startup and will then be reused whenever needed.
 * 
 * @author vw
 * 
 * @see IMergeProvider
 * @see MergeProvider
 * @see ICloneObjectExtension
 */
public interface ICloneObjectExtensionMergeStrategy
{
	/**
	 * Return status indicator for {@link ICloneObjectExtensionMergeStrategy#merge(IReadableMergeTask, IMergeResult, ICloneObject, ICloneObject, ICloneObject, ICloneObject, LinkedList, LinkedList, LinkedList)}.
	 */
	public enum Status
	{
		/**
		 * Indicates that the strategy did not make any modifications to the merged clone object.
		 */
		SKIPPED,

		/**
		 * Indicates that the strategy made some modifications to the merged clone object.
		 * <br>
		 * However, not all clone object extensions could be fully merged.
		 */
		PARTIAL,

		/**
		 * Indicates that the strategy successfully merged all (remaining) clone object extensions.<br/>
		 * The merged clone object is potentially in it's final state.
		 * <p>
		 * Further strategies will still be executed in order to have a chance to reject the result.
		 */
		FULL,

		/**
		 * Indicates that this event should not be passed on to any more strategies and that
		 * the merged clone object is in it's final stage.
		 * <p>
		 * A strategy will typically return this value if it detected a special situation which may
		 * confuse other strategies or if it needs to make sure that no other strategy will
		 * override its decision. 
		 */
		BREAK
	}

	/**
	 * Takes a local and remote version of an {@link ICloneObject} instance with extensions and an optional
	 * base version and merges the data of supported extensions.
	 * <p>
	 * The result is directly written to the given merged version of the clone object.
	 * <p>
	 * The pending base clone object list does not need to be completely processed. The pending local and remote
	 * clone object lists should be empty once <b>all</b> strategies have been executed.
	 * <br>
	 * Usually the last ("fallback") strategy will take care of all remaining, pending local and remote clone objects.
	 * 
	 * @param mergeTask the current merge task for which this merging is taking place, must not be
	 * 		modified in any way, never null. 
	 * @param mergeResult the current merge result, this may not yet be the final merge result,
	 * 		must not be modified in any way, never null.
	 * @param localCloneObject the former local version of the clone object, must not be modified, never null.
	 * @param remoteCloneObject the former remote version of the clone object, must not be modified, never null.
	 * @param baseCloneObject an <u>optional</u> base version of the clone object, must not be modified, may be NULL.
	 * @param mergedCloneObject the new, merged version of the clone object, <u>may be modified</u>, never null.
	 * @param pendingLocalExtensions a list of so far unhandled former local extension objects, may be empty, <u>may be modified</u>, never null.
	 * @param pendingRemoteExtensions a list of so far unhandled former remote extension objects, may be empty, <u>may be modified</u>, never null.
	 * @param pendingBaseExtensions a list of so far unhandled base extension objects, may be empty, <u>may be modified</u>, never null.
	 * @return the status of this merge operations, see: {@link Status}
	 */
	public Status merge(IReadableMergeTask mergeTask, IMergeResult mergeResult, ICloneObject localCloneObject,
			ICloneObject remoteCloneObject, ICloneObject baseCloneObject, ICloneObject mergedCloneObject,
			LinkedList<ICloneObjectExtension> pendingLocalExtensions,
			LinkedList<ICloneObjectExtension> pendingRemoteExtensions,
			LinkedList<ICloneObjectExtension> pendingBaseExtensions);
}
