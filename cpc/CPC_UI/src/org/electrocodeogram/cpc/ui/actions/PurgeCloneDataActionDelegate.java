package org.electrocodeogram.cpc.ui.actions;


import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;


public class PurgeCloneDataActionDelegate implements IObjectActionDelegate
{
	private static Log log = LogFactory.getLog(PurgeCloneDataActionDelegate.class);

	private IWorkbenchPart targetPart = null;
	private IStructuredSelection selection = null;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart)
	{
		if (log.isTraceEnabled())
			log.trace("setActivePart() - action: " + action + ", targetPart: " + targetPart);

		this.targetPart = targetPart;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection)
	{
		if (log.isTraceEnabled())
			log.trace("selectionChanged() - action: " + action + ", selection: " + selection);

		if (selection == null)
			return;

		this.selection = selection instanceof IStructuredSelection ? (IStructuredSelection) selection : null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action)
	{
		if (log.isTraceEnabled())
			log.trace("run() - action: " + action);

		IWorkbenchPartSite site = targetPart.getSite();
		//IWorkbenchWindow window = site.getWorkbenchWindow();

		/*
		 * Make sure the selection is acceptable.
		 */
		if (selection == null)
		{
			log.debug("run() - no selection, terminating.");
			MessageDialog.openError(site.getShell(), "Purge Clone Data",
					"No selection. You need to select projects, folders or files.");
			return;
		}

		/*
		 * Make sure the user really want to purge the clone data.
		 */

		//TODO: better display a dialog with a table viewer that lists all selected resources?
		boolean pressedOk = MessageDialog
				.openConfirm(
						site.getShell(),
						"Purge Clone Data",
						"Are you sure you want to purge all clone data for the selected resources?\nThe clone data will be permanently deleted. This action can not be undone.");

		if (!pressedOk)
		{
			//the user aborted
			log.trace("run() - user aborted, terminating.");
			return;
		}

		/*
		 * Collect a list of affected files.
		 */
		List<IResource> resources = extractResourcesFromSelection();
		if (resources.isEmpty())
		{
			log.trace("run() - selection yielded no resources, terminating.");
			return;
		}

		//get the file entries
		List<IFile> files = CoreFileUtils.getSupportedFilesInResources(resources);

		/*
		 * Purge clone data.
		 */

		if (log.isTraceEnabled())
			log.trace("run() - puring clone data for files: " + files);

		IStoreProvider storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				IStoreProvider.class);
		assert (storeProvider != null);

		try
		{
			//get an exclusive lock
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			//purge all clone data
			CoreFileUtils.purgeCloneDataForFiles(storeProvider, files, false);
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

		MessageDialog.openInformation(site.getShell(), "Purge Clone Data", "Clone data was successfully purged.");
	}

	/**
	 * Extract a list of selected resources from the current selection.
	 * 
	 * @return list of selected resources, never null.
	 */
	private List<IResource> extractResourcesFromSelection()
	{
		List<IResource> result = new LinkedList<IResource>();

		if (selection == null)
			return result;

		for (Object selectionItem : selection.toList())
		{
			if (selectionItem instanceof IProject)
			{
				if (log.isTraceEnabled())
					log.trace("extractResourcesFromSelection() - project: " + selectionItem);

				result.add((IProject) selectionItem);
			}
			else if (selectionItem instanceof IFolder)
			{
				if (log.isTraceEnabled())
					log.trace("extractResourcesFromSelection() - folder: " + selectionItem);

				result.add((IFolder) selectionItem);
			}
			else if (selectionItem instanceof IFile)
			{
				if (log.isTraceEnabled())
					log.trace("extractResourcesFromSelection() - file: " + selectionItem);

				result.add((IFile) selectionItem);
			}
			else
			{
				log.warn("extractResourcesFromSelection() - unexpected selection item, ignored -" + selectionItem);
			}
		}

		if (log.isTraceEnabled())
			log.trace("extractResourcesFromSelection() - result: " + result);

		return result;
	}

}
