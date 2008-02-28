package org.electrocodeogram.cpc.exports.control;


import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.exports.api.exports.IExportController;
import org.electrocodeogram.cpc.exports.api.exports.IExportTask;
import org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapter;
import org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterDescriptor;
import org.electrocodeogram.cpc.importexport.api.generic.IGenericStatus;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportConfigurationOptionException;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportFailureException;
import org.electrocodeogram.cpc.importexport.generic.AbstractImportExportController;
import org.electrocodeogram.cpc.importexport.generic.GenericStatus;


public class ExportController extends AbstractImportExportController implements IExportController
{
	private static Log log = LogFactory.getLog(ExportController.class);
	private static final String EXTENSION_POINT_EXPORT_TOOL_ADAPTERS = "org.electrocodeogram.cpc.exports.exportToolAdapters";

	private List<IExportToolAdapterDescriptor> exportToolAdapterDescriptors;

	public ExportController()
	{
		log.trace("ExportController()");

		exportToolAdapterDescriptors = new LinkedList<IExportToolAdapterDescriptor>();

		osgiInitialization();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.exports.api.exports.IExportController#createTask()
	 */
	@Override
	public IExportTask createTask()
	{
		log.trace("createTask()");

		return new ExportTask();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.exports.api.exports.IExportController#getRegisteredExportToolAdapters()
	 */
	@Override
	public List<IExportToolAdapterDescriptor> getRegisteredExportToolAdapters()
	{
		if (log.isTraceEnabled())
			log.trace("getRegisteredExportToolAdapters() - result: " + exportToolAdapterDescriptors);

		return exportToolAdapterDescriptors;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.exports.api.exports.IExportController#executeExport(org.eclipse.core.runtime.IProgressMonitor, org.electrocodeogram.cpc.exports.api.exports.IExportTask)
	 */
	@Override
	public IGenericStatus executeExport(IProgressMonitor monitor, IExportTask exportTask)
			throws ImportExportConfigurationOptionException, ImportExportFailureException, InterruptedException
	{
		if (log.isTraceEnabled())
			log.trace("executeExport() - monitor: " + monitor + ", exportTask: " + exportTask);

		/*
		 * Make sure the task is valid and that
		 * all the projects in this task exist and are open.
		 */
		checkTask(exportTask);

		GenericStatus result = new GenericStatus();

		try
		{
			/*
			 * Get clone data from Export Tool Adapter implementation.
			 */
			log.trace("executeExport() - executing export tool adapter");

			//collect a list of files
			List<IFile> files = CoreFileUtils.getSupportedFilesInProjects(exportTask.getProjects());

			//get the store provider
			IStoreProvider storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
					IStoreProvider.class);
			assert (storeProvider != null);

			//prepare the task
			ExportToolAdapterTask exportToolAdapterTask = new ExportToolAdapterTask();
			exportToolAdapterTask.setFiles(files);
			exportToolAdapterTask.setOptions(exportTask.getToolAdapterOptions());
			exportToolAdapterTask.setStoreProvider(storeProvider);

			//prepare result
			ExportToolAdapterResult exportToolAdapterResult = new ExportToolAdapterResult();

			//get an instance of the import tool adapter
			IExportToolAdapter exportToolAdapter = ((ExportToolAdapterDescriptor) exportTask.getToolAdapter())
					.getInstance();

			//execute the import
			IExportToolAdapter.Status status = exportToolAdapter.processExport(monitor, exportToolAdapterTask,
					exportToolAdapterResult);

			if (IExportToolAdapter.Status.NO_EXPORT.equals(status))
			{
				//the export exported no clones?
				throw new ImportExportFailureException(
						"No clones were exported. There might be no clones in the selected projects or the configuration settings might be too strict.");
			}

			//update counters
			result.setCloneCount(exportToolAdapterResult.getExportedCloneCount());
			result.setCloneGroupCount(exportToolAdapterResult.getExportedCloneGroupCount());
			result.setCloneFileCount(exportToolAdapterResult.getExportedCloneFileCount());
			result.setProcessedFileCount(files.size());
		}
		catch (CoreException e)
		{
			//for some reason we didn't get an instance of the IExportToolAdapter
			log.error("executeExport() - error initialising Export Tool Adapter - " + e, e);
			throw new ImportExportFailureException(
					"Export failed due to Internal Error: unable to initialise Export Tool Adapter", e);
		}

		if (log.isTraceEnabled())
			log.trace("executeExport() - result: " + result);

		return result;
	}

	/*
	 * Private methods.
	 */

	private void osgiInitialization()
	{
		log.trace("osgiInitialization(): building export tool adapter registry from extension data");

		IConfigurationElement[] extensions = Platform.getExtensionRegistry().getConfigurationElementsFor(
				EXTENSION_POINT_EXPORT_TOOL_ADAPTERS);
		for (IConfigurationElement element : extensions)
		{
			try
			{
				ExportToolAdapterDescriptor descriptor = new ExportToolAdapterDescriptor(element);
				exportToolAdapterDescriptors.add(descriptor);
			}
			catch (Exception e)
			{
				log
						.error("registration of export tool adapter failed: " + element.getAttribute("class") + " - "
								+ e, e);
			}
		}

		if (log.isTraceEnabled())
			log.trace("osgiInitialization() - resulting exportToolAdapterDescriptors: " + exportToolAdapterDescriptors);
	}
}
