package org.electrocodeogram.cpc.imports.control;


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
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.ICloneGroup;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.importexport.api.generic.IGenericStatus;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportConfigurationOptionException;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportFailureException;
import org.electrocodeogram.cpc.importexport.generic.AbstractImportExportController;
import org.electrocodeogram.cpc.importexport.generic.GenericStatus;
import org.electrocodeogram.cpc.imports.api.imports.IImportController;
import org.electrocodeogram.cpc.imports.api.imports.IImportTask;
import org.electrocodeogram.cpc.imports.api.imports.adapter.IImportToolAdapter;
import org.electrocodeogram.cpc.imports.api.imports.adapter.IImportToolAdapterDescriptor;
import org.electrocodeogram.cpc.imports.api.imports.strategy.IImportFilterStrategyDescriptor;


/**
 * Default {@link IImportController} implementation.
 * 
 * @author vw
 */
public class ImportController extends AbstractImportExportController implements IImportController
{
	private static Log log = LogFactory.getLog(ImportController.class);
	private static final String EXTENSION_POINT_IMPORT_TOOL_ADAPTERS = "org.electrocodeogram.cpc.imports.importToolAdapters";
	private static final String EXTENSION_POINT_IMPORT_FILTER_STRATEGIES = "org.electrocodeogram.cpc.imports.importFilterStrategies";

	private List<IImportToolAdapterDescriptor> importToolAdapterDescriptors;
	private List<IImportFilterStrategyDescriptor> importFilterStrategyDescriptors;

	public ImportController()
	{
		log.trace("ImportController()");

		importToolAdapterDescriptors = new LinkedList<IImportToolAdapterDescriptor>();
		importFilterStrategyDescriptors = new LinkedList<IImportFilterStrategyDescriptor>();

		osgiInitialization();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.imports.api.imports.IImportController#getRegisteredImportToolAdapters()
	 */
	@Override
	public List<IImportToolAdapterDescriptor> getRegisteredImportToolAdapters()
	{
		if (log.isTraceEnabled())
			log.trace("getRegisteredImportToolAdapters() - result: " + importToolAdapterDescriptors);

		return importToolAdapterDescriptors;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.imports.api.imports.IImportController#getRegisteredImportFilterStrategies()
	 */
	@Override
	public List<IImportFilterStrategyDescriptor> getRegisteredImportFilterStrategies()
	{
		if (log.isTraceEnabled())
			log.trace("getRegisteredImportFilterStrategies() - result: " + importFilterStrategyDescriptors);

		return importFilterStrategyDescriptors;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.imports.api.imports.IImportController#createTask()
	 */
	@Override
	public IImportTask createTask()
	{
		log.trace("createTask()");

		return new ImportTask();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.imports.api.imports.IImportController#executeImport(org.eclipse.core.runtime.IProgressMonitor, org.electrocodeogram.cpc.imports.api.imports.IImportTask)
	 */
	@Override
	public IGenericStatus executeImport(IProgressMonitor monitor, IImportTask importTask)
			throws ImportExportConfigurationOptionException, ImportExportFailureException, InterruptedException
	{
		if (log.isTraceEnabled())
			log.trace("executeImport() - monitor: " + monitor + ", importTask: " + importTask);

		/*
		 * Make sure the task is valid and that
		 * all the projects in this task exist and are open.
		 */
		checkTask(importTask);

		GenericStatus result = new GenericStatus();

		try
		{
			/*
			 * Get clone data from Import Tool Adapter implementation.
			 */
			log.trace("executeImport() - executing import tool adapter");

			//collect a list of files
			List<IFile> files = CoreFileUtils.getSupportedFilesInProjects(importTask.getProjects());

			//prepare the task
			ImportToolAdapterTask importToolAdapterTask = new ImportToolAdapterTask();
			importToolAdapterTask.setFiles(files);
			importToolAdapterTask.setOptions(importTask.getToolAdapterOptions());

			//prepare result
			ImportToolAdapterResult importToolAdapterResult = new ImportToolAdapterResult();

			//get an instance of the import tool adapter
			IImportToolAdapter importToolAdapter = ((ImportToolAdapterDescriptor) importTask.getToolAdapter())
					.getInstance();

			//execute the import
			IImportToolAdapter.Status status = importToolAdapter.processImport(monitor, importToolAdapterTask,
					importToolAdapterResult);

			if (IImportToolAdapter.Status.NO_RESULTS.equals(status))
			{
				//the import returned no results
				throw new ImportExportFailureException(
						"No clones were found. The configuration settings might be too strict.");
			}

			/*
			 * Filter obtained clone data through registered Import Filter Strategies
			 */

			log.trace("executeImport() - applying import filter strategies");

			//TODO: implement IImportFilterStrategy handling

			/*
			 * Store the obtained clone data.
			 */

			log.trace("executeImport() - persisting imported clone data");

			//get the store provider
			IStoreProvider storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
					IStoreProvider.class);
			assert (storeProvider != null);

			int cloneCount = 0;
			try
			{
				//get an exclusive lock
				storeProvider.acquireWriteLock(LockMode.DEFAULT);

				//purge old clone data, if requested
				if (importTask.isClearExistingClones())
				{
					log.trace("executeImport() - purging old clone data");
					CoreFileUtils.purgeCloneDataForFiles(storeProvider, files, false);
				}

				//add new groups
				for (ICloneGroup cloneGroup : importToolAdapterResult.getCloneGroups())
				{
					storeProvider.addCloneGroup(cloneGroup);
				}

				//All returned cloneFile instances were created (by API contract) via the store providers
				//lookup methods. They are therefore already known to the store provider and need not be
				//submitted to it again.
				for (ICloneFile cloneFile : importToolAdapterResult.getCloneMap().keySet())
				{
					//get clone data
					List<IClone> clones = importToolAdapterResult.getCloneMap().get(cloneFile);

					//add clone data
					storeProvider.addClones(clones);
					cloneCount += clones.size();

					//persist data
					storeProvider.persistData(cloneFile);
				}
			}
			catch (StoreLockingException e)
			{
				//this shouldn't happen
				log.error("executeImport() - locking exception - " + e, e);
			}
			finally
			{
				storeProvider.releaseWriteLock();
			}

			//update counters
			result.setCloneCount(cloneCount);
			result.setCloneGroupCount(importToolAdapterResult.getCloneGroups().size());
			result.setCloneFileCount(importToolAdapterResult.getCloneMap().size());
			result.setProcessedFileCount(files.size());
		}
		catch (CoreException e)
		{
			//for some reason we didn't get an instance of the IImportToolAdapter
			log.error("executeImport() - error initialising Import Tool Adapter - " + e, e);
			throw new ImportExportFailureException(
					"Import failed due to Internal Error: unable to initialise Import Tool Adapter", e);
		}

		if (log.isTraceEnabled())
			log.trace("executeImport() - result: " + result);

		return result;
	}

	/*
	 * Private methods.
	 */

	private void osgiInitialization()
	{
		log.trace("osgiInitialization(): building import tool adapter registry from extension data");

		IConfigurationElement[] extensions = Platform.getExtensionRegistry().getConfigurationElementsFor(
				EXTENSION_POINT_IMPORT_TOOL_ADAPTERS);
		for (IConfigurationElement element : extensions)
		{
			try
			{
				ImportToolAdapterDescriptor descriptor = new ImportToolAdapterDescriptor(element);
				importToolAdapterDescriptors.add(descriptor);
			}
			catch (Exception e)
			{
				log
						.error("registration of import tool adapter failed: " + element.getAttribute("class") + " - "
								+ e, e);
			}
		}

		if (log.isTraceEnabled())
			log.trace("osgiInitialization() - resulting importToolAdapterDescriptors: " + importToolAdapterDescriptors);

		extensions = Platform.getExtensionRegistry().getConfigurationElementsFor(
				EXTENSION_POINT_IMPORT_FILTER_STRATEGIES);
		for (IConfigurationElement element : extensions)
		{
			try
			{
				IImportFilterStrategyDescriptor descriptor = new ImportFilterStrategyDescriptor(element);
				importFilterStrategyDescriptors.add(descriptor);
			}
			catch (Exception e)
			{
				log.error(
						"registration of import filter strategy failed: " + element.getAttribute("class") + " - " + e,
						e);
			}
		}

		if (log.isTraceEnabled())
			log.trace("osgiInitialization() - resulting importFilterStrategyDescriptors: "
					+ importFilterStrategyDescriptors);
	}

}
