package org.electrocodeogram.cpc.merge.api.strategy;


import java.util.List;

import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeProvider;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeTask;
import org.electrocodeogram.cpc.merge.provider.MergeTask;


/**
 * A special read-only interface which corresponds to the {@link IMergeTask} interface.
 * <p>
 * This interface is meant to underline the fact that a {@link MergeTask} must not be modified
 * by an {@link IMergeStrategy}. It does thus <b>not</b> extend {@link IMergeTask}.
 * 
 * @author vw
 * 
 * @see IMergeTask
 * @see MergeTask
 * @see IMergeProvider
 * @see IMergeStrategy
 */
public interface IReadableMergeTask
{
	/**
	 * Retrieves the local {@link ICloneFile} underlying this merge task.
	 * 
	 * @return the local file, never null.
	 */
	public ICloneFile getLocalCloneFile();

	/**
	 * Retrieves the list of clone data for the local revision of the source file.
	 * 
	 * @return list of local clones, never null.
	 */
	public List<IClone> getLocalClones();

	/**
	 * Retrieves the content for the local revision of the source file.
	 * 
	 * @return local source file content, never null.
	 */
	public String getLocalSourceFileContent();

	/**
	 * Specifies whether the local revision of the source file is guaranteed
	 * to be in sync with the base revision.
	 * 
	 * @return <em>true</em> if local and base revisions are in sync, <em>false</em> otherwise.
	 */
	public boolean isLocalBaseInSyncHint();

	/**
	 * Retrieves the remote {@link ICloneFile} underlying this merge task.
	 * 
	 * @return remote file, never null.
	 */
	public ICloneFile getRemoteCloneFile();

	/**
	 * Retrieves the list of clone data for the remote revision of the source file.
	 * 
	 * @return list of remote clones, never null.
	 */
	public List<IClone> getRemoteClones();

	/**
	 * Retrieves the content for the remote revision of the source file.
	 * 
	 * @return remote content of source file, never null.
	 */
	public String getRemoteSourceFileContent();

	/**
	 * Retrieves the base revision of the {@link ICloneFile} underlying this merge task.
	 * 
	 * @return base revision of file, may be NULL.
	 */
	public ICloneFile getBaseCloneFile();

	/**
	 * Retrieves the list of clone data for the base revision of the source file.
	 * 
	 * @return list of base revision clones, may be NULL.
	 */
	public List<IClone> getBaseClones();

	/**
	 * Retrieves the content for the base revision of the source file.
	 * 
	 * @return base resivison source file content, may be NULL.
	 */
	public String getBaseSourceFileContent();

	/**
	 * Retrieves the merged content of the source file.
	 * 
	 * @return merged file content, never null.
	 */
	public String getMergedSourceFileContent();

	/**
	 * Returns <em>true</em> if all base revision data is available.
	 * <br>
	 * Otherwise <em>false</em> is returned. <em>False</em> is also returned if
	 * this merge task is not valid.
	 * <br>
	 * Convenience method.
	 */
	public boolean isThreeWayMerge();
}
