package org.electrocodeogram.cpc.imports.api.imports;


import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.importexport.api.generic.IGenericStatus;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportConfigurationOptionException;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportFailureException;
import org.electrocodeogram.cpc.imports.CPCImportsPlugin;
import org.electrocodeogram.cpc.imports.api.imports.adapter.IImportToolAdapter;
import org.electrocodeogram.cpc.imports.api.imports.adapter.IImportToolAdapterDescriptor;
import org.electrocodeogram.cpc.imports.api.imports.strategy.IImportFilterStrategy;
import org.electrocodeogram.cpc.imports.api.imports.strategy.IImportFilterStrategyDescriptor;


/**
 * Main backend controller for the <em>CPC Imports</em> module.
 * <br>
 * Can be used to execute clone data imports via registered {@link IImportToolAdapter}s.
 * <p>
 * An instance can be obtained via {@link CPCImportsPlugin#getImportController()}.
 * 
 * @author vw
 * 
 * @see IImportToolAdapter
 * @see IImportFilterStrategy
 */
public interface IImportController
{
	/**
	 * Retrieves a list of all currently registered {@link IImportToolAdapter} implementations.
	 * 
	 * @return list of all registered {@link IImportToolAdapter} implementations, never null.
	 */
	public List<IImportToolAdapterDescriptor> getRegisteredImportToolAdapters();

	/**
	 * Retrieves a list of all currently registered {@link IImportFilterStrategy} implementations.
	 * 
	 * @return list of all registered {@link IImportFilterStrategy} implementations, never null.
	 */
	public List<IImportFilterStrategyDescriptor> getRegisteredImportFilterStrategies();

	/**
	 * Creates a new empty {@link IImportTask} object which can then be filled with the
	 * task configuration options and data.
	 * <br>
	 * Once filled, the object can be passed to {@link IImportController#executeImport(IProgressMonitor, IImportTask)}
	 * to execute the import task.
	 * 
	 * @return an empty task object, never null.
	 */
	public IImportTask createTask();

	/**
	 * Executes the complete import process using the {@link IImportToolAdapter} which corresponds to the given
	 * {@link IImportToolAdapterDescriptor}.
	 * <p>
	 * Import Process:
	 * <ul>
	 * 	<li>all source files in the given projects are collected</li>
	 * 	<li>the {@link IImportToolAdapter} is called to process these files</li>
	 * 	<li>the returned clone data is passed to the registered {@link IImportFilterStrategy}s</li>
	 *  <li>the resulting clone data is transmitted to the {@link IStoreProvider}</li>
	 * </ul>
	 * 
	 * @param monitor a progress monitor for progress reporting and cancellation, may be NULL.
	 * @param importTask the import task to process, never null.
	 * @return clone import statistics, never null.
	 * 
	 * @throws ImportExportConfigurationOptionException if the given configuration options are illegal.
	 * @throws ImportExportFailureException if any error occurred during the import process.
	 * @throws InterruptedException if the import was cancelled by the user.
	 */
	public IGenericStatus executeImport(IProgressMonitor monitor, IImportTask importTask)
			throws ImportExportConfigurationOptionException, ImportExportFailureException, InterruptedException;

}
