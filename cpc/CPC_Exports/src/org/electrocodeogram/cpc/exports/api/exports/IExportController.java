package org.electrocodeogram.cpc.exports.api.exports;


import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.electrocodeogram.cpc.exports.CPCExportsPlugin;
import org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapter;
import org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterDescriptor;
import org.electrocodeogram.cpc.importexport.api.generic.IGenericStatus;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportConfigurationOptionException;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportFailureException;


/**
 * Main backend controller for the <em>CPC Exports</em> module.
 * <br>
 * Can be used to execute clone data exports via registered {@link IExportToolAdapter}s.
 * <p>
 * An instance can be obtained via {@link CPCExportsPlugin#getExportController()}.
 * 
 * @author vw
 * 
 * @see IExportToolAdapter
 */
public interface IExportController
{
	/**
	 * Retrieves a list of all currently registered {@link IExportToolAdapter} implementations.
	 * 
	 * @return list of all registered {@link IExportToolAdapter} implementations, never null.
	 */
	public List<IExportToolAdapterDescriptor> getRegisteredExportToolAdapters();

	/**
	 * Creates a new empty {@link IExportTask} object which can then be filled with the
	 * task configuration options and data.
	 * <br>
	 * Once filled, the object can be passed to {@link IExportController#executeExport(IProgressMonitor, IExportTask)}
	 * to execute the export task.
	 * 
	 * @return an empty task object, never null.
	 */
	public IExportTask createTask();

	/**
	 * Executes the complete export process using the {@link IExportToolAdapter} which corresponds to the given
	 * {@link IExportToolAdapterDescriptor}.
	 * <p>
	 * Export Process:
	 * <ul>
	 * 	<li>all source files in the given projects are collected</li>
	 * 	<li>the {@link IExportToolAdapter} is called to process/export these files</li>
	 * </ul>
	 * 
	 * @param monitor a progress monitor for progress reporting and cancellation, may be NULL.
	 * @param exportTask the export task to process, never null.
	 * @return clone export statistics, never null.
	 * 
	 * @throws ImportExportConfigurationOptionException if the given configuration options are illegal.
	 * @throws ImportExportFailureException if any error occurred during the export process.
	 * @throws InterruptedException if the import was cancelled by the user.
	 */
	public IGenericStatus executeExport(IProgressMonitor monitor, IExportTask exportTask)
			throws ImportExportConfigurationOptionException, ImportExportFailureException, InterruptedException;

}
