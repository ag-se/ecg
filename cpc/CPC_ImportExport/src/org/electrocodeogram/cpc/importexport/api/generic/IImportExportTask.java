package org.electrocodeogram.cpc.importexport.api.generic;


import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;


/**
 * Generic parts of the import/export task description.
 * 
 * @author vw
 */
public interface IImportExportTask
{
	/**
	 * Retrieves the {@link IGenericImportExportDescriptor} for adapter implementation to be used for this import/export.
	 *  
	 * @return {@link IGenericImportExportDescriptor} of the implementation to be used, never null.
	 */
	public IGenericImportExportDescriptor getToolAdapter();

	public void setToolAdapter(IGenericImportExportDescriptor toolAdapterDescriptor);

	/**
	 * Retrieves a map with configuration options for the {@link IGenericImportExportDescriptor} implementation.<br/>
	 * The keys correspond to the {@link IGenericExtensionOption#getId()} values specified
	 * in the {@link IGenericImportExportDescriptor} the implementation. 
	 * 
	 * @return a map with configuration options, never null.
	 */
	public Map<String, String> getToolAdapterOptions();

	public void setToolAdapterOptions(Map<String, String> toolAdapterOptions);

	/**
	 * Retrieves a list of projects which should be processed during this import/export.<br/>
	 * An empty list is not allowed.
	 * 
	 * @return list of projects to process, needs to contain at least one entry, never null.
	 */
	public List<IProject> getProjects();

	public void setProjects(List<IProject> projects);

	/**
	 * Checks whether all mandatory fields have been correctly filled.
	 * 
	 * @return <em>true</em> if this task is valid, <em>false</em> otherwise.
	 */
	public boolean isValid();

}
