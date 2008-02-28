package org.electrocodeogram.cpc.ui.actions;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredViewer;
import org.electrocodeogram.cpc.ui.views.Messages;


public abstract class AbstractCPCAction extends Action
{
	protected StructuredViewer viewer;

	public AbstractCPCAction(StructuredViewer viewer)
	{
		this.viewer = viewer;
	}

	protected void showMessage(String message)
	{
		MessageDialog.openInformation(viewer.getControl().getShell(),
				Messages.SimpleCloneView_MessageDialogTitle_SimpleCloneView, message);
	}
}
