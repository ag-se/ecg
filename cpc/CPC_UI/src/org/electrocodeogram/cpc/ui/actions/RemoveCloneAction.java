package org.electrocodeogram.cpc.ui.actions;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.ui.views.Messages;


public class RemoveCloneAction extends AbstractCPCAction
{
	private static final Log log = LogFactory.getLog(RemoveCloneAction.class);

	public RemoveCloneAction(StructuredViewer viewer)
	{
		super(viewer);

		setText(Messages.SimpleCloneView_ActionName_RemoveClone);
		setToolTipText(Messages.SimpleCloneView_ActionToolTip_RemoveClone);
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

		if (!(obj instanceof IClone))
		{
			log.error("removeCloneAction.run() - unknown object type: " + obj, new Throwable()); //$NON-NLS-1$
			return;
		}

		IClone clone = (IClone) obj;

		//now delete the clone
		IStoreProvider storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				IStoreProvider.class);

		//get the corresponding file
		ICloneFile cloneFile = storeProvider.lookupCloneFile(clone.getFileUuid());
		if (cloneFile == null)
		{
			log.error("removeCloneAction.run() - unable to retrieve clone file - clone: " + clone, new Throwable()); //$NON-NLS-1$
			return;
		}

		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);
			storeProvider.removeClone(clone);
		}
		catch (StoreLockingException e)
		{
			log.error("removeCloneAction.run() - locking error - clone: " + clone, e); //$NON-NLS-1$
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}

		showMessage(Messages.SimpleCloneView_SuccessMessage_CloneRemoved + "\n" + clone); //$NON-NLS-2$
	}

}
