package org.electrocodeogram.cpc.exports.api.exports.adapter;


import org.eclipse.core.runtime.IProgressMonitor;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportConfigurationOptionException;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportFailureException;


/**
 * A <em>CPC Exports</em> interface which can be used to contribute export implementations.
 * <br>
 * An implementation of this interface is likely to be an adapter/wrapper around some kind of mapping tool.
 * <p>
 * Implementations need to be registered with the <em>CPC Exports</em> extension point
 * <em>org.electrocodeogram.cpc.exports.exportToolAdapters</em>.
 * 
 * @author vw
 */
public interface IExportToolAdapter
{
	/**
	 * Return value for {@link IExportToolAdapter#processExport(IProgressMonitor, IExportToolAdapterTask, IExportToolAdapterResult)}.
	 */
	public enum Status
	{
		/**
		 * The export process finished successfully.
		 * <br>
		 * All clone data was exported.
		 */
		FULL_EXPORT,

		/**
		 * No error occurred but the export did not export all clone data.
		 * <br>
		 * This may happen in cases where special filters are applied during export.
		 */
		PARTIAL_EXPORT,

		/**
		 * No error occurred but the export did not export any clone data.
		 * <br>
		 * This typically happens if the exported files/projects currently don't contain any cpc clone data. 
		 */
		NO_EXPORT
	}

	/**
	 * Executes the clone data export according to the given export task description.
	 * 
	 * @param monitor optional progress monitor, may be null.
	 * @param exportTask a description of the export task at hand, never null.
	 * @param exportResult an empty result object which will be filled with some statistics, never null.
	 * @return the status of this export, never null.
	 * 
	 * @throws ImportExportConfigurationOptionException
	 * @throws ImportExportFailureException
	 * @throws InterruptedException
	 */
	public Status processExport(IProgressMonitor monitor, IExportToolAdapterTask exportTask,
			IExportToolAdapterResult exportResult) throws ImportExportConfigurationOptionException,
			ImportExportFailureException, InterruptedException;
}
