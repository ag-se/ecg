package org.electrocodeogram.cpc.imports.sdd.imports;


import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.soc.sdd.comparer.Chain;
import org.eclipse.soc.sdd.comparer.Comparer;
import org.eclipse.soc.sdd.indexer.Document;
import org.eclipse.soc.sdd.indexer.Indexer;
import org.eclipse.soc.sdd.indexer.InvertedIndexer;
import org.eclipse.soc.sdd.indexer.JavaIndexer;
import org.eclipse.soc.sdd.indexer.JavaInvertedIndexer;
import org.eclipse.soc.sdd.postprocessor.IPostProcessor;
import org.eclipse.soc.sdd.postprocessor.RemoveRedundantParts;
import org.eclipse.soc.sdd.preprocessor.AddOneSpaceInFrontOfNonAlphaNumericPreProcessor;
import org.eclipse.soc.sdd.preprocessor.RemoveImportPreProcessor;
import org.eclipse.soc.sdd.preprocessor.RemoveJavaCommentsPreProcessor;
import org.eclipse.soc.sdd.preprocessor.RemoveRedundantCharactersAndMakeLowerCasePreProcessor;
import org.eclipse.soc.sdd.supplier.ChunkSupplier;
import org.eclipse.soc.sdd.supplier.HashedBreakpointChunkSupplier;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.CPCConstants;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.ICloneGroup;
import org.electrocodeogram.cpc.core.api.data.special.ICreatorClone;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportConfigurationOptionException;
import org.electrocodeogram.cpc.importexport.api.generic.ImportExportFailureException;
import org.electrocodeogram.cpc.imports.api.imports.adapter.IImportToolAdapter;
import org.electrocodeogram.cpc.imports.api.imports.adapter.IImportToolAdapterResult;
import org.electrocodeogram.cpc.imports.api.imports.adapter.IImportToolAdapterTask;


/**
 * Implementation of the <em>CPC Imports</em> {@link IImportToolAdapter} Interface for the <em>SDD Eclipse Plugin</em>.<br/>
 * <br/>
 * This implementation requires the presence of the "Duplicated code detection tool (SDD)" by Iryoung Jeong.<br/>
 * http://wiki.eclipse.org/index.php/Duplicated_code_detection_tool_(SDD)<br/>
 * http://sourceforge.net/projects/sddforeclipse/<br/>
 * Plugin ID: org.eclipse.soc.sdd, Version: 1.0.0, Obtained on: 2007-09-14<br/>
 * A backup copy of the used CVS source can be found in the backup directory of this plugin.
 * 
 * @author vw
 */
public class SDDImportToolAdapter implements IImportToolAdapter
{
	private static Log log = LogFactory.getLog(SDDImportToolAdapter.class);

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.imports.api.imports.IImportToolAdapter#processImport(java.util.List, java.util.Map, java.util.Map)
	 */
	@Override
	public Status processImport(IProgressMonitor monitor, IImportToolAdapterTask importToolAdapterTask,
			IImportToolAdapterResult importToolAdapterResult) throws ImportExportConfigurationOptionException,
			ImportExportFailureException, InterruptedException
	{
		if (log.isTraceEnabled())
			log.trace("processImport() - monitor: " + monitor + ", importToolAdapterTask: " + importToolAdapterTask
					+ ", importToolAdapterResult: " + importToolAdapterResult);
		assert (importToolAdapterTask != null && importToolAdapterTask.isValid() && importToolAdapterResult != null);

		if (monitor != null)
			monitor.beginTask("searching for clones", importToolAdapterTask.getFiles().size());

		/*
		 * Process options.
		 */
		//default values
		int minChainSize = 15;
		int nNeighborLength = 2;
		try
		{
			if (importToolAdapterTask.getOptions().containsKey("minChainSize"))
				minChainSize = Integer.parseInt(importToolAdapterTask.getOptions().get("minChainSize"));

			if (importToolAdapterTask.getOptions().containsKey("nNeighborLength"))
				nNeighborLength = Integer.parseInt(importToolAdapterTask.getOptions().get("nNeighborLength"));
		}
		catch (NumberFormatException e)
		{
			log.error("processImport() - illegal configuration options - " + e, e);
			throw new ImportExportConfigurationOptionException("Illegal number format in configuration options.", e);
		}

		int cloneCount;
		try
		{
			SDDCompareResult importResult = doImport(monitor, importToolAdapterTask.getFiles(), minChainSize,
					nNeighborLength);
			//TODO: do we need to update the monitor in parseImport() too?
			cloneCount = parseImport(importToolAdapterTask.getFiles(), importToolAdapterResult.getCloneMap(),
					importToolAdapterResult.getCloneGroups(), importResult);
		}
		catch (Exception e)
		{
			if (e instanceof ImportExportFailureException)
			{
				//just rethrow without taking any action
				throw (ImportExportFailureException) e;
			}
			else if (e instanceof InterruptedException)
			{
				//just rethrow without taking any action
				throw (InterruptedException) e;
			}
			else
			{
				log.error("processImport() - import failed - " + e, e);
				throw new ImportExportFailureException("Import failed due to internal error - " + e, e);
			}
		}

		if (monitor != null)
		{
			if (monitor.isCanceled())
				throw new InterruptedException("Import was cancelled by user");

			monitor.done();
		}

		if (cloneCount > 0)
			return Status.SUCCESS;
		else
			return Status.NO_RESULTS;
	}

	/**
	 * Converts imported SDD date into clone objects.
	 * 
	 * @return the number of clones
	 */
	private int parseImport(List<IFile> files, Map<ICloneFile, List<IClone>> cloneResults,
			List<ICloneGroup> groupResults, SDDCompareResult importResult) throws ImportExportFailureException
	{
		if (log.isTraceEnabled())
			log.trace("parseImport() - files: " + files + ", cloneResults: " + cloneResults + ", importResult: "
					+ importResult);

		int cloneCount = 0;

		//get a some providers
		IStoreProvider storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				IStoreProvider.class);
		ICloneFactoryProvider cloneFactoryProvider = (ICloneFactoryProvider) CPCCorePlugin.getProviderRegistry()
				.lookupProvider(ICloneFactoryProvider.class);

		//for all chains (a clone group)
		for (Chain[] chains : importResult.getSimilarParts())
		{
			//create a new clone group
			ICloneGroup group = (ICloneGroup) cloneFactoryProvider.getInstance(ICloneGroup.class);
			groupResults.add(group);
			if (log.isTraceEnabled())
				log.trace("New Clone Group: " + group);

			//for all matched file parts (a clone)
			for (int i = 0; i < chains.length; i++)
			{
				//the current file
				IFile file = importResult.getOriginalFile(chains[i].getDocid());

				//lookup the clone file
				ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(CoreUtils.getProjectnameFromFile(file),
						CoreUtils.getProjectRelativePathFromFile(file), true, false);
				if (cloneFile == null)
				{
					//this shouldn't happen
					log.error("parseImport() - unable to obtain clone file for file - file: " + file, new Throwable());
					throw new ImportExportFailureException(
							"Internal Error - Unable to obtain clone file object for file: " + file.getFullPath());
				}
				if (log.isTraceEnabled())
					log.trace("  cloneFile: " + cloneFile);

				//create a new clone
				IClone clone = (IClone) cloneFactoryProvider.getInstance(IClone.class);

				//initialise clone with import data
				((ICreatorClone) clone).setFileUuid(cloneFile.getUuid());
				((ICreatorClone) clone).setCreationDate(new Date());
				((ICreatorClone) clone).setCreator(CPCConstants.CLONE_CREATOR_AUTOMATED_IMPORT);
				clone.setGroupUuid(group.getUuid());
				clone.setTransient(false);

				//update the position
				clone.setOffset(importResult.getStartOffset(chains[i]));
				clone.setLength(importResult.getEndOffset(chains[i]) - clone.getOffset() + 1);

				/*
				 * We'd need to set the clone content too.
				 * For that we need to get the file contents.
				 * But if we do it here we risk reading the same file multiple times.
				 * We therefore delay this until later.
				 */

				if (log.isTraceEnabled())
					log.trace("  New Clone: " + clone);

				List<IClone> clones = cloneResults.get(cloneFile);
				//initialise the list if this is the first time we store a clone for this clone file
				if (clones == null)
				{
					clones = new LinkedList<IClone>();
					cloneResults.put(cloneFile, clones);
				}

				clones.add(clone);
				++cloneCount;
			}
		}

		/*
		 * Now update the nonWs offsets and contents.
		 */
		for (ICloneFile cloneFile : cloneResults.keySet())
		{
			//get the file content
			IFile file = CoreFileUtils.getFileForCloneFile(cloneFile);
			String content = CoreUtils.readFileContent(file);
			if (content == null)
			{
				//this should never happen
				log.error("parseImport() - unable to read file: " + file.getLocation(), new Throwable());
				throw new ImportExportFailureException("Unable to read file: " + file.getLocation());
			}

			List<IClone> clones = cloneResults.get(cloneFile);
			assert (clones != null);

			//OLD - non-whitespace positions are no longer updated on the fly
			//			//now set the nonWs position for every clone
			//			CoreClonePositionUtils.extractPositions(cloneFactoryProvider, clones, content);

			//set content for all clones
			for (IClone clone : clones)
			{
				try
				{
					((ICreatorClone) clone).setContent(content.substring(clone.getOffset(), clone.getEndOffset() + 1));
				}
				catch (Exception e)
				{
					//this should never happen
					log.error("parseImport() - unable to extract clone content - file: " + file + ", clone: " + clone,
							new Throwable());
					throw new ImportExportFailureException("Unable to extract clone content - file: " + file
							+ ", clone: " + clone);
				}
			}
		}

		if (log.isTraceEnabled())
			log.trace("parseImport() - result (cloneCount): " + cloneCount);

		return cloneCount;
	}

	/**
	 * Does the actual import.
	 */
	private SDDCompareResult doImport(IProgressMonitor monitor, List<IFile> files, int minChainSize, int nNeighborLength)
			throws ImportExportFailureException, InterruptedException
	{
		if (log.isTraceEnabled())
			log.trace("doImport() - files: " + files + ", minChainSize: " + minChainSize + ", nNeighborLength: "
					+ nNeighborLength);

		long startTime = System.currentTimeMillis();

		/*
		 * Initialisation
		 */
		InvertedIndexer II;
		Indexer indexer;
		Comparer comparer;

		// init supplier & preprocessing & init & indexing
		II = new JavaInvertedIndexer();
		ChunkSupplier supplier = new HashedBreakpointChunkSupplier(3);
		supplier.addPreprocessor(new RemoveJavaCommentsPreProcessor());
		supplier.addPreprocessor(new RemoveImportPreProcessor());
		supplier.addPreprocessor(new AddOneSpaceInFrontOfNonAlphaNumericPreProcessor());
		supplier.addPreprocessor(new RemoveRedundantCharactersAndMakeLowerCasePreProcessor());
		II.init(supplier);
		indexer = new JavaIndexer();
		indexer.init(supplier);

		/*
		 * Process files
		 */
		for (int i = 0; i < files.size(); ++i)
		{
			IFile file = files.get(i);

			if (log.isTraceEnabled())
				log.trace("doImport() - indexing file = " + file);

			try
			{
				indexer.index(i, new InputStreamReader(file.getContents()));
				II.index(i, new InputStreamReader(file.getContents()));
			}
			catch (CoreException e)
			{
				log.error("doImport() - error while reading file: " + file.getLocation() + " - " + e, e);
				throw new ImportExportFailureException("Error while reading file: " + file.getLocation() + " - " + e, e);
			}

			if (monitor != null)
			{
				monitor.worked(1);
				//Thread.sleep(250);
				if (monitor.isCanceled())
					throw new InterruptedException("Import was cancelled by user");
			}
		}

		II.optimizing();
		indexer.optimizing();
		if (log.isTraceEnabled())
			log.trace("doImport() - initializing finished - " + (System.currentTimeMillis() - startTime) + " ms");
		List<Document> docs = indexer.getDocs();

		int chainLength = 0;
		for (Document doc : docs)
		{
			chainLength += doc.getLength();
		}
		if (log.isTraceEnabled())
			log.trace("doImport() - total Chain Length=" + chainLength);
		comparer = new Comparer(minChainSize, nNeighborLength);
		List<Chain[]> results = new ArrayList<Chain[]>();
		// comparing..
		int totalComparingTime = 0;
		for (Document doc : docs)
		{
			for (int i = 0; i < doc.getLength(); i++)
			{
				totalComparingTime++;
				if (log.isTraceEnabled())
				{
					if ((totalComparingTime % 100) == 0)
					{
						log.trace("doImport() - comparing... " + totalComparingTime + " ms");
					}
				}
				Chain[] matchedChains = comparer.compareOneChunk(indexer, II, doc, i);
				if (matchedChains != null)
				{
					results.add(matchedChains);
				}
			}
		}
		if (log.isTraceEnabled())
			log.trace("doImport() - total comparing... " + totalComparingTime + " ms");
		if (log.isDebugEnabled())
			log.debug("doImport() - comparing finished - " + (System.currentTimeMillis() - startTime) + " ms");

		// post processing
		IPostProcessor processor = new RemoveRedundantParts();
		results = processor.process(results);

		SDDCompareResult result = new SDDCompareResult();
		result.setInvertedIndexer(II);
		result.setIndexer(indexer);
		result.setFiles(files);
		result.setSimilarParts(results);

		if (log.isDebugEnabled())
			log.debug("doImport() - postprocessing finished - " + (System.currentTimeMillis() - startTime) + " ms");

		return result;
	}
}
