package org.electrocodeogram.cpc.ui.api;


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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;


/**
 * Abstract convenience class which unifies the handling of {@link IViewActionDelegate} and
 * {@link IObjectActionDelegate} contributions to allow a single action implementation to
 * be easily used for view and object action contribution. 
 * 
 * @author vw
 */
public abstract class AbstractCPCViewActionDelegate implements IViewActionDelegate, IObjectActionDelegate
{
	private static final Log log = LogFactory.getLog(AbstractCPCViewActionDelegate.class);

	private IWorkbenchPart targetPart = null;
	private ICPCSelectionSourceViewPart selectionSource = null;
	private IStructuredSelection lastSelection = null;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	@Override
	public void init(IViewPart viewPart)
	{
		if (log.isTraceEnabled())
			log.trace("init() - viewPart: " + viewPart);

		if (!(viewPart instanceof ICPCSelectionSourceViewPart))
		{
			log.error("init() - viewPart is not of type ICPCSelectionSourceViewPart - viewPart: " + viewPart
					+ ", class: " + viewPart.getClass(), new Throwable());
			throw new IllegalArgumentException("Argument must be of type ISelectionSourceViewPart.");
		}

		this.targetPart = viewPart;
		this.selectionSource = (ICPCSelectionSourceViewPart) viewPart;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart)
	{
		if (log.isTraceEnabled())
			log.trace("setActivePart() - targetPart: " + targetPart);

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

		if (selection != null && selection instanceof IStructuredSelection)
			lastSelection = (IStructuredSelection) selection;
		else
			log.debug("selectionChanged() - ignoring unsupported selection.");
	}

	/*
	 * AbstractCPCViewActionDelegate methods.
	 */

	/**
	 * @return current selection or NULL if no selection is currently available.
	 */
	protected IStructuredSelection getSelection()
	{
		if (selectionSource != null)
			return selectionSource.getSelection();

		if (lastSelection != null)
			return lastSelection;

		log.debug("getSelection() - no selection available, returning null.");

		return null;
	}

	/**
	 * Extract a list of selected resources from the current selection.
	 * 
	 * @return list of selected resources, never null.
	 */
	protected List<IResource> extractResourcesFromSelection()
	{
		List<IResource> result = new LinkedList<IResource>();

		IStructuredSelection selection = getSelection();

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

	/**
	 * Extract a list of selected projects from the current selection.
	 * 
	 * @return list of selected projects, never null.
	 */
	protected List<IProject> extractProjectsFromSelection()
	{
		List<IProject> result = new LinkedList<IProject>();

		IStructuredSelection selection = getSelection();

		if (selection == null)
			return result;

		for (Object selectionItem : selection.toList())
		{
			if (selectionItem instanceof IProject)
			{
				if (log.isTraceEnabled())
					log.trace("extractProjectsFromSelection() - project: " + selectionItem);

				result.add((IProject) selectionItem);
			}
		}

		if (log.isTraceEnabled())
			log.trace("extractProjectsFromSelection() - result: " + result);

		return result;
	}

	/**
	 * @return a valid shell, never null.
	 */
	protected Shell getShell()
	{
		assert (targetPart != null);
		return targetPart.getSite().getShell();
	}

	/**
	 * Displays an information {@link MessageDialog} with the given title and message. 
	 */
	protected void showMessage(String title, String message)
	{
		assert (targetPart != null);
		MessageDialog.openInformation(targetPart.getSite().getShell(), title, message);
	}

	/**
	 * Displays an error {@link MessageDialog} with the given title and message. 
	 */
	protected void showError(String title, String message)
	{
		assert (targetPart != null);
		MessageDialog.openError(targetPart.getSite().getShell(), title, message);
	}

	/**
	 * Displays a confirmation {@link MessageDialog} with the given title and message. 
	 */
	protected boolean showConfirm(String title, String message)
	{
		assert (targetPart != null);
		return MessageDialog.openConfirm(targetPart.getSite().getShell(), title, message);
	}

	/**
	 * Displays a question {@link MessageDialog} with the given title and message. 
	 */
	protected boolean showQuestion(String title, String message)
	{
		assert (targetPart != null);
		return MessageDialog.openQuestion(targetPart.getSite().getShell(), title, message);
	}
}
