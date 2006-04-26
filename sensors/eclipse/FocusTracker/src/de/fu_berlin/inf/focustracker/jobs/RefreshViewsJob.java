package de.fu_berlin.inf.focustracker.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.focustracker.ui.FocusTrackerDecorator;
import de.fu_berlin.inf.focustracker.views.BeanView;


public class RefreshViewsJob extends Job {

	private static int DELAY = 2000;
	private BeanView beanView;
	
	public RefreshViewsJob(BeanView aBeanView) {
		super("Refresh views Job");
		beanView = aBeanView;
		schedule(DELAY);
	}
	
	@Override
	protected IStatus run(IProgressMonitor aMonitor) {
		try {
//			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(FocussedElementsView.ID);
//			FocussedElementsView visitedElementsView = (FocussedElementsView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(FocussedElementsView.ID);
//			if(visitedElementsView != null) {
//				visitedElementsView.getViewer().refresh(true);
//				System.err.println("Refreshing!");
//			}
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if(!beanView.isDisposed()) {
						beanView.refresh(beanView.isLabelUpdate());
						if(!beanView.isPinnedOutput()) {
							TableItem[] tableItems = beanView.getViewer().getTable().getItems();
							if(tableItems.length>0) {
								beanView.getViewer().getTable().showItem(tableItems[tableItems.length-1]);
							}
						}
//						reveal(beanView.getObjects()[beanView.getObjects().length]);
					}
				}
			});
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		
		schedule(DELAY);
		return Status.OK_STATUS;
	}

}
