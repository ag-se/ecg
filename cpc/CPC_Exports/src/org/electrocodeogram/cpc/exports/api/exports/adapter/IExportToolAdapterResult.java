package org.electrocodeogram.cpc.exports.api.exports.adapter;


/**
 * Export result wrapper for the {@link IExportToolAdapter}.
 * <p>
 * An {@link IExportToolAdapter} implementation is not required to fill provide
 * all of these statistics.
 * <br>
 * By default all values are -1.
 * 
 * @author vw
 */
public interface IExportToolAdapterResult
{
	/**
	 * Retrieves the total number of clones found in the given files.
	 * 
	 * @return total number of clones found in the given files.
	 */
	public int getTotalCloneCount();

	/**
	 * Sets the total number of clones found in the given files.
	 * 
	 * @param totalCloneCount total number of clones found in the given files.
	 */
	public void setTotalCloneCount(int totalCloneCount);

	/**
	 * Retrieves the number of clones exported.
	 * 
	 * @return number of clones exported.
	 */
	public int getExportedCloneCount();

	/**
	 * Sets the number of clones exported.
	 * 
	 * @param exportedCloneCount number of clones exported.
	 */
	public void setExportedCloneCount(int exportedCloneCount);

	/**
	 * Retrieves the total number of clone groups found in the given files.
	 * 
	 * @return total number of clone groups found in the given files.
	 */
	public int getTotalCloneGroupCount();

	/**
	 * Sets the total number of clone groups found in the given files.
	 * 
	 * @param totalCloneGroupCount total number of clone groups found in the given files.
	 */
	public void setTotalCloneGroupCount(int totalCloneGroupCount);

	/**
	 * Retrieves the number of clone groups exported.
	 * 
	 * @return number of clone groups exported. 
	 */
	public int getExportedCloneGroupCount();

	/**
	 * Sets the number of clone groups exported.
	 * 
	 * @param exportedCloneGroupCount number of clone groups exported.
	 */
	public void setExportedCloneGroupCount(int exportedCloneGroupCount);

	/**
	 * Retrieves the number of processed files which contained at least one clone.
	 * 
	 * @return number of processed files which contained at least one clone.
	 */
	public int getTotalCloneFileCount();

	/**
	 * Sets the number of processed files which contained at least one clone.
	 * 
	 * @param totalCloneFileCount number of processed files which contained at least one clone.
	 */
	public void setTotalCloneFileCount(int totalCloneFileCount);

	/**
	 * Retrieves the number of exported files which contained at least one clone.
	 * 
	 * @return number of exported files which contained at least one clone.
	 */
	public int getExportedCloneFileCount();

	/**
	 * Sets the number of exported files which contained at least one clone.
	 * 
	 * @param exportedCloneFileCount number of exported files which contained at least one clone.
	 */
	public void setExportedCloneFileCount(int exportedCloneFileCount);
}
