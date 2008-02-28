package org.electrocodeogram.cpc.imports.api.imports.adapter;


import org.eclipse.core.runtime.IProgressMonitor;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportConfigurationOptionException;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportFailureException;
import org.electrocodeogram.cpc.imports.api.data.IImportCloneObjectExtension;
import org.electrocodeogram.cpc.imports.api.imports.IImportController;


/**
 * A <em>CPC Imports</em> interface which can be used to contribute import implementations.
 * <br>
 * An implementation of this interface is likely to be an adapter/wrapper around an existing clone
 * detection tool.
 * <p>
 * Implementations need to be registered with the <em>CPC Imports</em> extension point
 * <em>org.electrocodeogram.cpc.imports.importToolAdapters</em>.
 * 
 * @author vw
 * 
 * @see IImportController
 * @see IImportToolAdapterTask
 * @see IImportToolAdapterResult
 */
public interface IImportToolAdapter
{
	/**
	 * Return value for {@link IImportToolAdapter#processImport(IProgressMonitor, IImportToolAdapterTask, IImportToolAdapterResult)}. 
	 */
	public enum Status
	{
		/**
		 * The import process finished successfully.
		 * <br>
		 * Some clone data was imported.
		 */
		SUCCESS,

		/**
		 * No error occurred but the import failed to detect
		 * any clone data fit for import.
		 */
		NO_RESULTS
	}

	/**
	 * Takes an {@link IImportToolAdapterTask} description which contains a list of files to import clone data for and
	 * an options map. Furthermore an empty import result wrapper object if provided for the resulting clone data.
	 * <br>
	 * The options map contains user selected options. Possible options are specified using the corresponding
	 * <em>CPC Imports</em> extension point. 
	 * <p>
	 * Files for which the import did not produce any clones should not be added to the result map.
	 * <p>
	 * The import implementation <b>may</b> attach additional confidence data to each imported clone
	 * by attaching {@link IImportCloneObjectExtension} instances to the clone objects.
	 * <p>
	 * All exceptions which are not declared in the signature need to be caught and re-thrown as an
	 * {@link ImportExportFailureException} by all implementations.
	 * <p>
	 * An implementation may not create its own {@link ICloneFile} instances. All returned instances must
	 * be created via {@link IStoreProvider#lookupCloneFileByPath(String, String, boolean, boolean)}.
	 * An implementation may not submit any data to the {@link IStoreProvider}, it should have
	 * <b>no side effects</b> (aside of those caused by the {@link IStoreProvider}'s lookup methods). 
	 * 
	 * @param monitor progress monitor for progress reporting and cancellation, may be NULL.
	 * 		An implementation should start a new task and continuously update the amount of work done
	 * 		if this is not null. The task should be marked as finished, once the implementation
	 * 		is about to return control to the caller. The monitor should regularly be checked for cancellation.
	 * @param importTask the import task which contains the task description, never null.
	 * @param importResult the import result object to which detected clones and their groups should be added, never null.
	 * @return the status of the import process, never null.
	 * @throws ImportExportConfigurationOptionException if the option map contains illegal data or is missing
	 * 		required options. The exception message should be meaningful for the end user.
	 * @throws ImportExportFailureException if the import failed for some reason. The exception message should be meaningful
	 * 		for the end user.
	 * @throws InterruptedException of the import was cancelled by the user.
	 */
	public Status processImport(IProgressMonitor monitor, IImportToolAdapterTask importTask,
			IImportToolAdapterResult importResult) throws ImportExportConfigurationOptionException,
			ImportExportFailureException, InterruptedException;

}
