package org.electrocodeogram.cpc.optimiser.actions;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.electrocodeogram.cpc.optimiser.task.OrphanDetectionTask;
import org.electrocodeogram.cpc.ui.api.AbstractCPCViewActionDelegate;


public class OrphanDetectionActionDelegate extends AbstractCPCViewActionDelegate
{
	private static Log log = LogFactory.getLog(OrphanDetectionActionDelegate.class);

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
		 */
		if (getSelection() == null)
		{
			log.debug("run() - no selection, terminating.");
			showError("CPC Optimiser", "No selection. You need to select projects.");
			return;
		}

		/*
		 * Collect a list of affected files.
		 */
		List<IProject> projects = extractProjectsFromSelection();
		if (projects.isEmpty())
		{
			log.trace("run() - no projects selected, terminating.");
			showError("CPC Optimiser", "Invalid selection. You need to select projects.");
			return;
		}

		OrphanDetectionTask task = new OrphanDetectionTask();
		task.init(projects, null);
		task.schedule();

		showMessage("CPC Optimiser", "Orphan clone detection was started as a background job.");
	}

}
