package de.fu_berlin.inf.focustracker.report;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.focustracker.FocusTrackerPlugin;

public class OpenReportEditorAction implements IViewActionDelegate {

	public void init(IViewPart aView) {
		// TODO Auto-generated method stub

	}

public void run(IAction action) {

		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
		
				IWorkbenchPage page = FocusTrackerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
				if (page == null) {
					return;
				}
				IEditorInput input = new ReportEditorInput(); //(files, generator);
				try {
					page.openEditor(input, ReportEditorPart.ID);
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}});
	}	
		public void selectionChanged(IAction action, ISelection aSelection) {
		// TODO Auto-generated method stub

	}

}
