package org.electrocodeogram.cpc.importexport.api.generic;


import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;


/**
 * Generic import/export tool adapter task description.
 * 
 * @author vw
 */
public interface IImportExportToolAdapterTask
{
	/**
	 * Retrieves the list of files which should be processed for this import/export task.
	 * 
	 * @return the list of files to be included in this import/export, never null.
	 */
	public List<IFile> getFiles();

	public void setFiles(List<IFile> files);

	/**
	 * Retries a map with configuration data for this import/export task.
	 * 
	 * @return a map containing user selected options for this import/export, never null.
	 */
	public Map<String, String> getOptions();

	public void setOptions(Map<String, String> options);

	/**
	 * Checks whether all mandatory fields have been correctly filled.
	 * 
	 * @return true if this task is valid
	 */
	public boolean isValid();

}
