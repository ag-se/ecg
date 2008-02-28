package org.electrocodeogram.cpc.ui.actions;


import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.ui.views.CloneDetailsDialog;
import org.electrocodeogram.cpc.ui.views.Messages;


public class ShowCloneDetailsAction extends AbstractCPCAction
{
	public ShowCloneDetailsAction(StructuredViewer viewer)
	{
		super(viewer);

		setText(Messages.SimpleCloneView_ActionName_CloneDetails);
		setToolTipText(Messages.SimpleCloneView_ActionToolTip_CloneDetails);
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
		ISelection selection = viewer.getSelection();
		if (selection == null)
			return;

		Object obj = ((IStructuredSelection) selection).getFirstElement();
		if (obj == null)
			return;

		if (obj instanceof IClone)
		{
			CloneDetailsDialog dialog = new CloneDetailsDialog(viewer.getControl().getShell(), 0, (IClone) obj);
			dialog.open();
		}
		else
		{
			showMessage("Object Details:\n\n" + obj.toString());
		}
	}

}
