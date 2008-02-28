package org.electrocodeogram.cpc.importexport.generic;


import org.eclipse.core.resources.IProject;
import org.electrocodeogram.cpc.importexport.api.generic.IImportExportTask;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportConfigurationOptionException;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportFailureException;


/**
 * Some generic methods which are needed by the import and export controllers in <em>CPC Imports</em>
 * and <em>CPC Exports</em>.
 * 
 * @author vw
 */
public abstract class AbstractImportExportController
{
	/**
	 * Ensures that the given task object is non-null and valid.
	 * Also checks that all selected projects exist and are open.
	 * 
	 * @param task the task to check, never null.
	 * @throws ImportExportFailureException thrown if the task is invalid or any selected project isn't accessible.
	 */
	protected void checkTask(IImportExportTask task) throws ImportExportConfigurationOptionException
	{
		if (task == null)
			throw new ImportExportConfigurationOptionException("Operation failed: task object may not be null.");

		if (!task.isValid())
			throw new ImportExportConfigurationOptionException("Operation failed: task object is invalid.");

		for (IProject project : task.getProjects())
		{
			if (!project.exists())
				throw new ImportExportConfigurationOptionException("Operation failed: project does not exist: "
						+ project.getName());

			if (!project.isOpen())
				throw new ImportExportConfigurationOptionException("Operation failed: project is not open: "
						+ project.getName());
		}
	}
}
