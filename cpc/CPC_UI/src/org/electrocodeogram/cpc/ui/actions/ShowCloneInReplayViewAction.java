package org.electrocodeogram.cpc.ui.actions;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.ui.views.codereplay.CPCReplay;
import org.electrocodeogram.cpc.ui.views.codereplay.DataProvider;
import org.electrocodeogram.cpc.ui.views.codereplay.ReplayView;


public class ShowCloneInReplayViewAction extends AbstractCPCAction
{
	private static final Log log = LogFactory.getLog(ShowCloneInReplayViewAction.class);

	public ShowCloneInReplayViewAction(StructuredViewer viewer)
	{
		super(viewer);

		if (log.isTraceEnabled())
			log.trace("ShowCloneInReplayViewAction() - viewer: " + viewer);

		setText("Show clone edit history");
		setToolTipText("Opens this clone's edit history.");
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_OBJS_INFO_TSK));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run()
	{
		log.trace("run()");

		/*
		 * First the the selected clone object.
		 */

		ISelection selection = viewer.getSelection();
		if (selection == null)
		{
			log.trace("run() - nothing selected (1), skipping.");
			return;
		}

		Object obj = ((IStructuredSelection) selection).getFirstElement();
		if (obj == null)
		{
			log.trace("run() - nothing selected (2), skipping.");
			return;
		}

		//make sure a clone was selected
		if (!(obj instanceof IClone))
		{
			log.trace("run() - selected element is not a clone, skipping.");
			return;
		}

		IClone clone = (IClone) obj;

		/*
		 * Now make sure the replay view is open.
		 */

		// Get the active page.
		IWorkbenchPage page = getPage();
		if (page == null)
		{
			log.warn("run() - unable to get workbench page, skipping.");
			return;
		}

		//check if the replay view is already open
		if (!isReplayViewVisible())
		{
			log.trace("run() - replay view not yet visible, opening.");

			// nope, open and activate it
			try
			{
				page.showView(ReplayView.VIEW_ID);
			}
			catch (PartInitException e)
			{
				log.error("run() - failed to open SimpleCloneView - " + e, e);
			}
		}

		/*
		 * Finally, load the clone's history data in the replay view.
		 */

		DataProvider.getInstance().setActiveReplay(new CPCReplay(clone));

	}

	private IWorkbenchPage getPage()
	{
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		if (window == null)
		{
			log.warn("getPage() - unable to get workbench window.");
			return null;
		}

		IWorkbenchPage page = window.getActivePage();

		return page;
	}

	private boolean isReplayViewVisible()
	{
		IWorkbenchPage page = getPage();
		if (page == null)
		{
			log.warn("run() - unable to get workbench page.");
			return false;
		}

		IViewPart replayView = page.findView(ReplayView.VIEW_ID);

		return (replayView != null);
	}

}
