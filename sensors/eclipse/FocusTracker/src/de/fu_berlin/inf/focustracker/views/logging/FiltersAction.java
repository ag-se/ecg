package de.fu_berlin.inf.focustracker.views.logging;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;

class FiltersAction extends Action {

	ChartView view;
    /**
     * Creates the action.
     * @param tasklist the task list
     * @param id the id
     */
    public FiltersAction(ChartView aView, String id) {
//        super(tasklist, id);
//        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
//				ITaskListHelpContextIds.FILTERS_ACTION);
    	view = aView;
    }

    /**
     * Performs this action.
     */
    public void run() {
        FiltersDialog dialog = new FiltersDialog(view.getSite().getShell());
        dialog.setFilter(view.getFilter());
        int result = dialog.open();
        if (result == Window.OK) {
            view.filterChanged();
        }
    }
}