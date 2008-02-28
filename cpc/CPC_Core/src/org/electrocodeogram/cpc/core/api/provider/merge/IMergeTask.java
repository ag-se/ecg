package org.electrocodeogram.cpc.core.api.provider.merge;


import java.util.List;

import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;


/**
 * A task description object for the {@link IMergeProvider}.
 * <p>
 * An instance can be obtained via {@link IMergeProvider#createTask()}.
 * <p>
 * Some values are required, some values are optional.
 * <br>
 * Depending on the provided data a Two-Way or a Three-Way merge may
 * be executed by the merge provider.
 * <p>
 * The contents of a {@link IMergeTask} must not be modified in any way once it
 * has been completely filled with data.
 * <p>
 * <b>Rationale:</b>
 * <blockquote>
 * Depending on the persistence provider used, it may not always be possible for a
 * CPC Sensor and CPC Remote Store provider to obtain all the data needed for a Three-Way
 * merge.
 * <br>
 * To allow maximum flexibility an {@link IMergeProvider} must thus be able to handle
 * Two-Way merges if the data required for a Three-Way merge can not be provided.
 * </blockquote>
 * 
 * @author vw
 * 
 * @see IMergeProvider
 * @see IMergeProvider#createTask()
 */
public interface IMergeTask
{
	/*
	 * Local data.
	 */

	/**
	 * The local {@link ICloneFile} instance (before the merge).
	 * <p>
	 * <b>Required value</b>. 
	 * 
	 * @param cloneFile old local clone file instance, never null.
	 */
	public void setLocalCloneFile(ICloneFile cloneFile);

	/**
	 * The local {@link IClone} instances (before the merge).
	 * <p>
	 * <b>Required value</b>. 
	 * 
	 * @param clones old local clones, may be empty, never null.
	 */
	public void setLocalClones(List<IClone> clones);

	/**
	 * The local source file content before the merge.
	 * <p>
	 * <b>Required value</b>. 
	 * 
	 * @param content old local content of the source file, never null.
	 */
	public void setLocalSourceFileContent(String content);

	/**
	 * Specifies whether the current revision is in sync with the base revision.
	 * <p>
	 * In some cases no information about the base revision might be available.
	 * However, the caller may still be able to determine whether the current
	 * local clone data was potentially modified or whether it can be guaranteed
	 * to be in sync with the base revision.
	 * <p>
	 * The default value is <em>false</em> which indicates that nothing is known
	 * about the synchronisation status between the local and the base revision.
	 * <p>
	 * If the caller can guarantee that the local clone data was not modified and
	 * that thus no merge is needed, this value should be set to <em>true</em>.
	 * <p>
	 * If this value is <em>true</em> a merge provider may simply "overwrite" the
	 * local clone data with the remote clone data without merging.
	 * 
	 * @param localBaseInSyncHint <em>False</em> (default) if local and base revision
	 * 		might differ. <em>True</em> if local and base revision are guaranteed
	 * 		to be in sync.
	 */
	public void setLocalBaseInSyncHint(boolean localBaseInSyncHint);

	/*
	 * Remote data.
	 */

	/**
	 * The remote {@link ICloneFile} instance (before the merge).
	 * <p>
	 * <b>Required value</b>. 
	 * 
	 * @param cloneFile old remote clone file instance, never null.
	 */
	public void setRemoteCloneFile(ICloneFile cloneFile);

	/**
	 * The remote {@link IClone} instances (before the merge).
	 * <p>
	 * <b>Required value</b>. 
	 * 
	 * @param clones old remote clones, may be empty, never null.
	 */
	public void setRemoteClones(List<IClone> clones);

	/**
	 * The remote source file content before the merge.
	 * <p>
	 * <b>Required value</b>.
	 * 
	 * @param content old remote content of the source file, may be NULL.
	 */
	public void setRemoteSourceFileContent(String content);

	/*
	 * Base data. (All optional)
	 */

	/**
	 * The common base {@link ICloneFile} instance.
	 * <p>
	 * <i>Optional value</i>.
	 * 
	 * @param cloneFile common base clone file instance, may be NULL.
	 */
	public void setBaseCloneFile(ICloneFile cloneFile);

	/**
	 * The common base {@link IClone} instances.
	 * <p>
	 * <i>Optional value</i>.
	 * 
	 * @param clones common base clones, may be empty, may be NULL.
	 */
	public void setBaseClones(List<IClone> clones);

	/**
	 * The common base source file content.
	 * <p>
	 * <i>Optional value</i>.
	 * 
	 * @param content common base content of the source file, may be NULL.
	 */
	public void setBaseSourceFileContent(String content);

	/*
	 * Merged data.
	 */

	/**
	 * The result of the merge of the two source files.
	 * <p>
	 * <b>Required value</b>. 
	 * 
	 * @param content the content of the new source file on disk, never null.
	 */
	public void setMergedSourceFileContent(String content);

	/*
	 * Misc
	 */

	/**
	 * Checks whether this task is valid.
	 * 
	 * @return true if all required fields have been set, false otherwise.
	 */
	public boolean isValid();

	/**
	 * All implementations should provide a meaningful toString() method for debugging purposes. 
	 */
	public String toString();
}
