package org.electrocodeogram.cpc.exports.ui.actions;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.electrocodeogram.cpc.exports.ui.wizards.ExportClonesWizard;


public class ExportClonesAction implements IObjectActionDelegate
{
	private static Log log = LogFactory.getLog(ExportClonesAction.class);

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
		IWorkbenchWindow window = site.getWorkbenchWindow();

		ExportClonesWizard wizard = new ExportClonesWizard();
		wizard.init(window.getWorkbench(), selection);
		WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
		dialog.open();
	}

}
