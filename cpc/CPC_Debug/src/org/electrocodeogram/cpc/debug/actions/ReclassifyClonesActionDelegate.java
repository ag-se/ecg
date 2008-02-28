package org.electrocodeogram.cpc.debug.actions;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.classification.IClassificationProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.UpdateMode;
import org.electrocodeogram.cpc.core.utils.CoreEditorUtils;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;
import org.electrocodeogram.cpc.ui.api.AbstractCPCViewActionDelegate;


public class ReclassifyClonesActionDelegate extends AbstractCPCViewActionDelegate
{
	private static Log log = LogFactory.getLog(ReclassifyClonesActionDelegate.class);

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action)
	{
		if (log.isTraceEnabled())
			log.trace("run() - action: " + action);

		/*
		 * Make sure the selection is acceptable.
		 * And collect a list of affected files.
		 */
		List<IResource> resources = extractResourcesFromSelection();
		if (resources.isEmpty())
		{
			log.debug("run() - no selection, terminating.");
			showError("CPC Reclassify Clones", "No selection. You need to select projects, folders or files.");
			return;
		}

		/*
		 * Make sure the user is aware of the long running nature of this action.
		 * 
		 * TODO: This should really be a wizard with a progress bar.
		 */

		//TODO: better display a dialog with a table viewer that lists all selected resources?
		boolean pressedOk = showConfirm("CPC Reclassify Clones",
				"Do you want to reclassify all clones\nfor the selected resourced now?\n\nThis operation could take some time.");

		if (!pressedOk)
		{
			//the user aborted
			log.trace("run() - user aborted, terminating.");
			return;
		}

		//get the file entries
		List<IFile> files = CoreFileUtils.getSupportedFilesInResources(resources);

		/*
		 * Now do the work.
		 */

		log.info("run() - reclassifying clones.");

		IStoreProvider storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				IStoreProvider.class);
		assert (storeProvider != null);

		IClassificationProvider classificationProvider = (IClassificationProvider) CPCCorePlugin.getProviderRegistry()
				.lookupProvider(IClassificationProvider.class);
		assert (classificationProvider != null);

		int count = 0;
		/*
		 * TODO: check if it is acceptable to be holding the exclusive lock for the entire operation?
		 */
		try
		{
			//get an exclusive lock
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			for (IFile file : files)
			{
				count += reclassify(storeProvider, classificationProvider, file);
			}
		}
		catch (StoreLockingException e)
		{
			//this shouldn't happen
			log.error("run() - locking error - " + e, e);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}

		log.info("run() - reclassification finished - " + count + " clones in " + files.size() + " files classified.");

		showMessage("CPC Reclassify Clones", "Reclassification finished.\n\n" + count + " clones in " + files.size()
				+ " files classified.");

	}

	/**
	 * Reclassifies all clones in the given file, if it is known to the store provider and contains clones.
	 * If the file isn't currently open in a dirty editor window, the changes made are persisted.
	 */
	private int reclassify(IStoreProvider storeProvider, IClassificationProvider classificationProvider, IFile file)
			throws IllegalArgumentException, StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("reclassify() - file: " + file);

		int count = 0;

		ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(CoreUtils.getProjectnameFromFile(file), CoreUtils
				.getProjectRelativePathFromFile(file), false, true);
		if (cloneFile == null)
		{
			log.trace("reclassify() - file is unknown to store provider, skipping file.");
			return 0;
		}

		List<IClone> clones = storeProvider.getClonesByFile(cloneFile.getUuid());

		if (!clones.isEmpty())
		{
			if (log.isTraceEnabled())
				log.trace("reclassify() - reclassifying " + clones.size() + " clones.");

			for (IClone clone : clones)
			{
				if (log.isTraceEnabled())
					log.trace("reclassify() - reclassifying clone: " + clone + ", old classifications: "
							+ clone.getClassifications());

				IClassificationProvider.Result result = classificationProvider.classify(
						IClassificationProvider.Type.RECLASSIFY, cloneFile, clone, null, null);
				if (IClassificationProvider.Result.ERROR.equals(result))
				{
					log.warn("reclassify() - classification failed.");
				}
				else if (IClassificationProvider.Result.REJECTED.equals(result))
				{
					log
							.debug("reclassify() - classification provider REJECTED clone, but we're NOT deleting anything right now.");
				}
				else if (IClassificationProvider.Result.ACCEPTED.equals(result))
				{
					if (log.isTraceEnabled())
						log.trace("reclassify() - clone was accepted, new classifications: "
								+ clone.getClassifications());
				}
				else
				{
					log.warn("reclassify() - unknown classification provider result: " + result, new Throwable());
				}

				storeProvider.updateClone(clone, UpdateMode.MOVED);
				++count;
			}

			/*
			 * Persist the new clone data.
			 * But only if this isn't a currently open, dirty editor.
			 */
			if (!CoreEditorUtils.isFileOpenInEditorAndDirty(file))
				storeProvider.persistData(cloneFile);
			else
				log.warn("reclassify() - not persisting changes due to presence of dirty editor window - file: " + file
						+ ", cloneFile: " + cloneFile, new Throwable());
		}
		else
		{
			log.trace("reclassify() - file contains no clones.");
		}

		return count;
	}
}
