package org.electrocodeogram.cpc.debug.actions;


import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.core.utils.CoreHistoryUtils;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;
import org.electrocodeogram.cpc.ui.api.AbstractCPCViewActionDelegate;


public class IntegrityCheckActionDelegate extends AbstractCPCViewActionDelegate
{
	private static Log log = LogFactory.getLog(IntegrityCheckActionDelegate.class);

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
			showError("CPC Integrity Check", "No selection. You need to select projects, folders or files.");
			return;
		}

		/*
		 * Make sure the user is aware of the long running nature of this action.
		 * 
		 * TODO: This should really be a wizard with a progress bar.
		 */

		//TODO: better display a dialog with a table viewer that lists all selected resources?
		boolean pressedOk = showConfirm(
				"CPC Integrity Check",
				"Do you want to start an integrity check of the clone data\nfor the selected resourced now?\n\nThis operation could take some time.");

		if (!pressedOk)
		{
			//the user aborted
			log.trace("run() - user aborted, terminating.");
			return;
		}

		//get the file entries
		List<IFile> files = CoreFileUtils.getSupportedFilesInResources(resources);

		/*
		 * Now do the checking.
		 */

		log.info("run() - integrity check started");

		if (log.isTraceEnabled())
			log.trace("run() - checking integrity for files: " + files);

		IStoreProvider storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				IStoreProvider.class);
		assert (storeProvider != null);

		List<String> errors = new LinkedList<String>();
		List<String> warnings = new LinkedList<String>();

		//temporary map to check for non-unique file uuids
		Map<String, IFile> uuidToFileMap = new HashMap<String, IFile>(files.size());

		/*
		 * TODO: check if it is acceptable to be holding the exclusive lock for the entire operation?
		 */
		try
		{
			//get an exclusive lock
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			for (IFile file : files)
			{
				if (log.isTraceEnabled())
					log.trace("run() - checking file: " + file);

				checkFile(storeProvider, file, uuidToFileMap, errors, warnings);
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

		//TODO: display errors and warnings to the user
		for (String error : errors)
			log.warn("ERROR: " + error);
		for (String warning : warnings)
			log.warn("WARN : " + warning);

		log.info("run() - integrity check finished - " + errors.size() + " errors, " + warnings.size() + " warnings");

		showMessage("CPC Integrity Check", "Integrity check finished.\n\n" + "Result: " + errors.size() + " errors, "
				+ warnings.size() + " warnings\n\n" + "Please refer to the CPC log file for more details.");
	}

	/**
	 * Does multiple integrity checks for the given file and all clones it contains.
	 */
	private void checkFile(IStoreProvider storeProvider, IFile file, Map<String, IFile> uuidToFileMap,
			List<String> errors, List<String> warnings) throws StoreLockingException
	{
		/*
		 * Identify file.
		 */
		ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(file.getProject().getName(), file
				.getProjectRelativePath().toString(), false, false);
		if (cloneFile == null)
		{
			if (log.isTraceEnabled())
				log.trace("checkFile() - file is not known to CPC, ignoring - " + file);
			return;
		}

		/*
		 * Check persistent uuid property.
		 */
		String uuidProperty = CoreFileUtils.getFileUuidProperty(file);
		if (uuidProperty == null)
		{
			log.info("checkFile() - WARN - file has no persistent uuid property - cloneFile: " + cloneFile);

			warnings.add("file has no persistent uuid property - " + file.getLocation());
		}
		else if (!uuidProperty.equals(cloneFile.getUuid()))
		{
			log.info("checkFile() - ERROR - file has incorrect persistent uuid property - cloneFile: " + cloneFile
					+ ", uuidProperty: " + uuidProperty);

			errors.add("file has incorrect persistent uuid property - " + file.getLocation());
		}

		/*
		 * Check for non-unique uuid
		 */
		if (uuidToFileMap.containsKey(cloneFile.getUuid()))
		{
			log.info("checkFile() - ERROR - file has non-unique uuid property - cloneFile: " + cloneFile + ", file: "
					+ file + ", otherFile: " + uuidToFileMap.get(cloneFile.getUuid()));

			errors.add("file has non-unique uuid property - " + file.getLocation() + " - "
					+ uuidToFileMap.get(cloneFile.getUuid()).getLocation());
		}
		else
		{
			uuidToFileMap.put(cloneFile.getUuid(), file);
		}

		/*
		 * Check file sync status.
		 */
		long modDate = CoreFileUtils.getFileModificationDate(file);
		long size = CoreFileUtils.getFileSize(file);
		if (cloneFile.getModificationDate() != modDate || cloneFile.getSize() != size)
		{
			if (log.isTraceEnabled())
				log.trace("checkFile() - INFO - modification dates or sizes don't match - cloneFile: " + cloneFile
						+ ", modDate DB: " + cloneFile.getModificationDate() + ", modDate FS: " + modDate
						+ ", size DB: " + cloneFile.getSize() + ", size FS: " + size);

			//now compare the file content
			String fsContent = CoreUtils.readFileContent(file);
			String dbContent = storeProvider.getPersistedCloneFileContent(cloneFile);
			if (fsContent == null || dbContent == null)
			{
				log.info("checkFile() - ERROR - unable to get fs file contents - cloneFile: " + cloneFile
						+ ", content DB: " + CoreStringUtils.truncateString(dbContent) + ", content FS: "
						+ CoreStringUtils.truncateString(fsContent));

				errors.add("unable to get file contents - " + file.getLocation());
			}
			else if (fsContent.length() != dbContent.length() || !fsContent.equals(dbContent))
			{
				log.info("checkFile() - WARN - file contents are not in sync - cloneFile: " + cloneFile
						+ ", content DB: " + CoreStringUtils.truncateString(dbContent) + ", content FS: "
						+ CoreStringUtils.truncateString(fsContent));

				warnings.add("file contents are not in sync - " + file.getLocation());
			}
			else
			{
				log.trace("checkFile() - files content matches.");
			}
		}

		/*
		 * Check integrity of clone data for this file.
		 */
		List<IClone> clones = storeProvider.getClonesByFile(cloneFile.getUuid());
		String curContent = CoreUtils.getFileContentFromEditorOrFile(file);
		if (curContent == null)
		{
			log.info("checkFile() - ERROR - unable to get current file content - cloneFile: " + cloneFile);

			errors.add("unable to get current file content - " + file.getLocation());
		}
		else
		{
			//ok, check each clone one by one
			for (IClone clone : clones)
			{
				checkClone(storeProvider, file, cloneFile, clone, curContent, warnings, errors);
			}
		}
	}

	/**
	 * Does multiple integrity checks for the given clone.
	 */
	private void checkClone(IStoreProvider storeProvider, IFile file, ICloneFile cloneFile, IClone clone,
			String curContent, List<String> warnings, List<String> errors)
	{
		if (log.isTraceEnabled())
			log.trace("checkClone() - checking clone: " + clone);

		/*
		 * Check that the content matches.
		 */
		if (!cloneContentMatch(clone, curContent))
		{
			log.info("checkClone() - ERROR - clone content mismatch - cloneFile: " + cloneFile + ", clone: " + clone);

			errors.add("clone content mismatch - " + file.getLocation() + " - " + clone.getUuid() + " ["
					+ clone.getOffset() + ":" + clone.getLength() + "]");
		}

		/*
		 * Check that the clone history is valid.
		 */
		if (!cloneDiffHistoryMatch(storeProvider, clone))
		{
			log.info("checkClone() - ERROR - clone diff history replay mismatch - cloneFile: " + cloneFile
					+ ", clone: " + clone);

			errors.add("clone diff history replay mismatch - " + file.getLocation() + " - " + clone.getUuid() + " ["
					+ clone.getOffset() + ":" + clone.getLength() + "]");
		}

	}

	/**
	 * Checks whether a complete replay of the clone's diff history will end up matching the current content.
	 */
	private boolean cloneDiffHistoryMatch(IStoreProvider storeProvider, IClone clone)
	{
		String content = CoreHistoryUtils.getCloneContentForDate(storeProvider, clone, new Date(), false);

		if (content == null)
		{
			log.info("cloneDiffHistoryMatch() - unable to replay history for: " + clone);
			return false;
		}
		else if (!content.equals(clone.getContent()))
		{
			log.info("cloneDiffHistoryMatch() - content mismatch - got: " + CoreStringUtils.truncateString(content)
					+ ", expected: " + CoreStringUtils.truncateString(clone.getContent()));
			return false;
		}

		return true;
	}

	/**
	 * Checks whether the persisted content of this clone matches the current content.  
	 */
	private boolean cloneContentMatch(IClone clone, String curDocContent)
	{
		if (clone.getEndOffset() + 1 > curDocContent.length())
		{
			log.info("cloneContentMatch() - offset out of range - off: " + clone.getEndOffset() + ", document len: "
					+ curDocContent);
			return false;
		}

		String content = curDocContent.substring(clone.getOffset(), clone.getEndOffset() + 1);
		if (!content.equals(clone.getContent()))
		{
			log.info("cloneContentMatch() - content mismatch - got: " + CoreStringUtils.truncateString(content)
					+ ", expected: " + CoreStringUtils.truncateString(clone.getContent()));
			return false;
		}

		return true;
	}

}
