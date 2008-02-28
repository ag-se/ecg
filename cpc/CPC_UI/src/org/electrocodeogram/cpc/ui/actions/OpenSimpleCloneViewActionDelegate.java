package org.electrocodeogram.cpc.ui.actions;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.electrocodeogram.cpc.ui.views.SimpleCloneView;


public class OpenSimpleCloneViewActionDelegate implements IWorkbenchWindowActionDelegate
{
	private static Log log = LogFactory.getLog(OpenSimpleCloneViewActionDelegate.class);

	private IWorkbenchWindow window;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void init(IWorkbenchWindow window)
	{
		this.window = window;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action)
	{
		// Get the active page.
		IWorkbenchPage page = getPage();
		if (page == null)
			return;

		if (isSimpleCloneViewVisible())
		{
			//the simple clone view is already open, ignore this call
			action.setEnabled(false);
			return;
		}

		// Open and activate the Favorites view.
		try
		{
			page.showView(SimpleCloneView.VIEW_ID);
		}
		catch (PartInitException e)
		{
			log.error("run() - failed to open SimpleCloneView - " + e, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection)
	{
		/*
		 * Disable the option if the Simple Clone View is currently being displayed
		 */

		if (isSimpleCloneViewVisible())
			//the simple clone view is already open, disable action
			action.setEnabled(false);
		else
			//enable action
			action.setEnabled(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	@Override
	public void dispose()
	{
		//not in use
	}

	private IWorkbenchPage getPage()
	{
		if (window == null)
			return null;

		IWorkbenchPage page = window.getActivePage();

		return page;
	}

	private boolean isSimpleCloneViewVisible()
	{
		IWorkbenchPage page = getPage();
		if (page == null)
			return false;

		IViewPart simpleCloneView = page.findView(SimpleCloneView.VIEW_ID);

		return (simpleCloneView != null);
	}
}
