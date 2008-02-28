package org.electrocodeogram.cpc.importexport.api.generic;


/**
 * Import/Export statistics wrapper.<br/>
 * Provides details about the number of files, clone groups and clones.
 * 
 * @author vw
 */
public interface IGenericStatus
{
	/**
	 * @return the number of imported/exported clones.
	 */
	public int getCloneCount();

	/**
	 * @return the number of imported/exported clone groups.
	 */
	public int getCloneGroupCount();

	/**
	 * @return the number of processed files which contained clone data.
	 */
	public int getCloneFileCount();

	/**
	 * @return the total number of processed files.
	 */
	public int getProcessedFileCount();
}
