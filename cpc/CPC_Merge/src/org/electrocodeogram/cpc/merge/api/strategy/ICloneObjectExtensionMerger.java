package org.electrocodeogram.cpc.merge.api.strategy;


import org.electrocodeogram.cpc.core.api.data.ICloneObject;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeResult;
import org.electrocodeogram.cpc.core.api.provider.merge.MergeException;


/**
 * A special merge handler for {@link ICloneObjectExtension}s.
 * <br>
 * The merge is internally handled by the registered {@link ICloneObjectExtensionMergeStrategy}s.
 * 
 * @author vw
 */
public interface ICloneObjectExtensionMerger
{
	/**
	 * Merges the {@link ICloneObjectExtension} data of the given local, remote and base {@link ICloneObject}s.
	 * <br>
	 * The given merged clone object is updated in place.
	 * 
	 * @param mergeTask current merge task, may not be modified, never null.
	 * @param mergeResult current merge result, may not be in its final state, may not be modified, never null.
	 * @param localCloneObject former local clone object, may not be modified, never null.
	 * @param remoteCloneObject former remote clone object, may not be modified, never null.
	 * @param baseCloneObject optional base clone object, may not be modified, may be NULL.
	 * @param mergedCloneObject new merged clone object, may be modified, never null.
	 */
	public void merge(IReadableMergeTask mergeTask, IMergeResult mergeResult, ICloneObject localCloneObject,
			ICloneObject remoteCloneObject, ICloneObject baseCloneObject, ICloneObject mergedCloneObject)
			throws MergeException;

}
