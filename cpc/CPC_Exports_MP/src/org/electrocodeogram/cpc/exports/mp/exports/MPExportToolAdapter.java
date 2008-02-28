package org.electrocodeogram.cpc.exports.mp.exports;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.xml.IMappingProvider;
import org.electrocodeogram.cpc.core.api.provider.xml.MappingException;
import org.electrocodeogram.cpc.core.api.provider.xml.MappingStore;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;
import org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapter;
import org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterResult;
import org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterTask;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportConfigurationOptionException;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportFailureException;


/**
 * A simple {@link IExportToolAdapter} which makes use of an {@link IMappingProvider} to map
 * clone data to files in a specific directory.
 * 
 * @author vw
 */
public class MPExportToolAdapter implements IExportToolAdapter
{
	private static final Log log = LogFactory.getLog(MPExportToolAdapter.class);

	public static final String OPTION_EXPORT_DESTINATION_FOLDER = "exportDestinationFolder";
	public static final String OPTION_EXPORT_EMPTY_CLONE_FILES = "exportEmptyCloneFiles";
	public static final String OPTION_EXPORT_SOURCE_FILES = "exportSourceFiles";

	public MPExportToolAdapter()
	{
		log.trace("MPExportToolAdapter()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapter#processExport(org.eclipse.core.runtime.IProgressMonitor, org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterTask, org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterResult)
	 */
	@Override
	public Status processExport(IProgressMonitor monitor, IExportToolAdapterTask exportTask,
			IExportToolAdapterResult exportResult) throws ImportExportConfigurationOptionException,
			ImportExportFailureException, InterruptedException
	{
		if (log.isTraceEnabled())
			log.trace("processExport() - exportTask: " + exportTask + ", exportResult: " + exportResult + ", monitor: "
					+ monitor);
		assert (exportTask != null && exportTask.isValid() && exportResult != null);

		//begin task
		if (monitor != null)
			monitor.beginTask("exporting clone data", exportTask.getFiles().size());

		/*
		 * Initialise stats counters.
		 */
		int cloneCount = 0;
		//int cloneGroupCount = 0;
		int cloneFileCount = 0;

		/*
		 * Parse configuration settings.
		 */
		String exportDestinationFolder = exportTask.getOptions().get(OPTION_EXPORT_DESTINATION_FOLDER);
		boolean exportEmptyCloneFiles = false;
		boolean exportSourceFiles = false;

		if (exportDestinationFolder == null)
		{
			log.error("processExport() - no export destination folder specified.", new Throwable());
			throw new ImportExportConfigurationOptionException("No export destination folder specified.");
		}

		try
		{
			if (exportTask.getOptions().containsKey(OPTION_EXPORT_EMPTY_CLONE_FILES))
				if (Integer.parseInt(exportTask.getOptions().get(OPTION_EXPORT_EMPTY_CLONE_FILES)) == 1)
					exportEmptyCloneFiles = true;

			if (exportTask.getOptions().containsKey(OPTION_EXPORT_SOURCE_FILES))
				if (Integer.parseInt(exportTask.getOptions().get(OPTION_EXPORT_SOURCE_FILES)) == 1)
					exportSourceFiles = true;
		}
		catch (NumberFormatException e)
		{
			log.error("processExport() - illegal configuration options - " + e, e);
			throw new ImportExportConfigurationOptionException("Illegal number format in configuration options.", e);
		}

		if (log.isTraceEnabled())
			log
					.trace("processExport() - configuration - exportDestinationFolder: " + exportDestinationFolder
							+ ", exportEmptyCloneFiles: " + exportEmptyCloneFiles + ", exportSourceFiles: "
							+ exportSourceFiles);

		/*
		 * Get a mapping provider instance.
		 */
		//TODO: we should include an option to enable the user to select a different mapping provider.
		IMappingProvider mappingProvider = (IMappingProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				IMappingProvider.class);
		if (mappingProvider == null)
		{
			log.error("processExport() - unable to obtain a mapping provider instance.", new Throwable());
			throw new ImportExportFailureException("unable to obtain a mapping provider instance");
		}

		/*
		 * Check the export destination directory.
		 * 
		 * TODO: currently we always write the export data into a directory in our plugin state location.
		 * We should add a dialog window which allows the user to select an arbitrary directory (also outside of the workspace).
		 */
		//FIXME !!!
		//File exportDir = new File("/tmp");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmm");
		String finalExportDestinationFolder = exportDestinationFolder + "/" + sdf.format(new Date());
		IFolder exportDir = ResourcesPlugin.getWorkspace().getRoot().getFolder(new Path(finalExportDestinationFolder));

		//make sure the directory exists

		try
		{
			if (!exportDir.exists())
				CoreFileUtils.createResourceRecursively(exportDir);
		}
		catch (CoreException e)
		{
			log.error("processExport() - unable to create destination directory - exportDir: " + exportDir + " - " + e,
					e);
			throw new ImportExportFailureException("Unable to create destination directory.", e);
		}

		//make sure it is a directory
		//		if (!exportDir.isDirectory())
		//		{
		//			log.error("processExport() - selected export destination is not a directory - exportDir: " + exportDir
		//					+ ", exportTask: " + exportTask, new Throwable());
		//			throw new ImportExportFailureException("selected export destination is not a directory");
		//		}

		/*
		 * We're going to process each file one by one.
		 */
		for (IFile file : exportTask.getFiles())
		{
			if (log.isTraceEnabled())
				log.trace("processExport() - processing file: " + file);

			//make sure the file exists
			if (file == null || !file.exists())
			{
				log.warn("processExport() - can't access file, skipping. - file: " + file + ", exportTask: "
						+ exportTask + ", exportResult: " + exportResult, new Throwable());
				if (monitor != null)
					monitor.worked(1);
				continue;
			}

			//check if the file is known to CPC
			ICloneFile cloneFile = exportTask.getStoreProvider().lookupCloneFileByPath(file.getProject().getName(),
					file.getProjectRelativePath().toString(), false, false);
			if (cloneFile == null)
			{
				//TODO: maybe these files should also be included if exportEmptyCloneFiles is 1?
				log.trace("processExport() - file is not known to cpc, skipping.");
				if (monitor != null)
					monitor.worked(1);
				continue;
			}

			//get the clone data for the file
			//we're interested in the persisted version of the data
			List<IClone> clones = exportTask.getStoreProvider().getPersistedClonesForFile(cloneFile.getUuid());

			if (clones.isEmpty() && !exportEmptyCloneFiles)
			{
				log
						.trace("processExport() - file contains no clone data and exportEmptyCloneFiles is not set, skipping.");
				if (monitor != null)
					monitor.worked(1);
				continue;
			}

			++cloneFileCount;

			/*
			 * Map the clone data into a string.
			 */
			MappingStore mappingStore = new MappingStore(cloneFile, clones);
			String cpcData = null;
			try
			{
				cpcData = mappingProvider.mapToString(mappingStore, true);
			}
			catch (MappingException e)
			{
				log.error("processExport() - error while mapping clone data - file: " + file + ", mappingStore: "
						+ mappingStore + " - " + e, e);
				throw new ImportExportFailureException("error while mapping clone data", e);
			}

			/*
			 * Now write the data to a file.
			 */

			try
			{
				//File cpcExportFile = new File(exportDir, cloneFile.getUuid() + ".cpc-export.dat");
				IFile cpcExportFile = exportDir.getFile(cloneFile.getUuid() + ".cpc-export.dat");

				//				BufferedWriter cpcbw = new BufferedWriter(new FileWriter(cpcExportFile));
				//				cpcbw.append(cpcData);
				//				cpcbw.close();
				CoreUtils.writeFileContent(cpcExportFile, cpcData);

				if (exportSourceFiles)
				{
					//also store a copy of the source file

					//get latest source file content
					IFile sourceFile = ResourcesPlugin.getWorkspace().getRoot().getFile(
							new Path(cloneFile.getProject() + "/" + cloneFile.getPath()));
					if (sourceFile == null || !sourceFile.exists())
					{
						log.error("processExport() - unable to find source file - sourceFile: " + sourceFile
								+ ", exportDir: " + exportDir + ", file: " + file + ", cloneFile: " + cloneFile,
								new Throwable());
						throw new ImportExportFailureException("unable to find source file: " + sourceFile);
					}

					String sourceData = CoreUtils.readFileContent(sourceFile);
					if (sourceData == null)
					{
						log.error("processExport() - unable to get content for source file - sourceFile: " + sourceFile
								+ ", exportDir: " + exportDir + ", file: " + file + ", cloneFile: " + cloneFile,
								new Throwable());
						throw new ImportExportFailureException("unable to get content for source file: " + sourceFile);
					}

					//write output
					//File sourceExportFile = new File(exportDir, cloneFile.getUuid() + ".cpc-export.src");
					IFile sourceExportFile = exportDir.getFile(cloneFile.getUuid() + ".cpc-export.src");

					//					BufferedWriter cpcsrc = new BufferedWriter(new FileWriter(sourceExportFile));
					//					cpcsrc.append(sourceData);
					//					cpcsrc.close();
					CoreUtils.writeFileContent(sourceExportFile, sourceData);
				}

				cloneCount += clones.size();
			}
			catch (CoreException e)
			{
				log.error("processExport() - io error - exportDir: " + exportDir + ", file: " + file + ", cloneFile: "
						+ cloneFile + " - " + e, e);
				throw new ImportExportFailureException("io error while writing export files", e);
			}

			//update progress
			if (monitor != null)
			{
				monitor.worked(1);

				if (monitor.isCanceled())
					throw new InterruptedException("Export was cancelled by user");
			}
		}

		//end task
		if (monitor != null)
		{
			if (monitor.isCanceled())
				throw new InterruptedException("Import was cancelled by user");

			monitor.done();
		}

		exportResult.setExportedCloneCount(cloneCount);
		exportResult.setExportedCloneFileCount(cloneFileCount);
		//exportResult.setExportedCloneGroupCount(cloneGroupCount);
		exportResult.setTotalCloneCount(cloneCount);
		exportResult.setTotalCloneFileCount(cloneFileCount);
		//exportResult.setTotalCloneGroupCount(cloneGroupCount);

		if (cloneCount == 0)
			return Status.NO_EXPORT;
		else
			return Status.FULL_EXPORT;
	}
}
