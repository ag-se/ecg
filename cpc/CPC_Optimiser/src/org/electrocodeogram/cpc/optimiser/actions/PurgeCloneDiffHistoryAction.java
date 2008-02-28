package org.electrocodeogram.cpc.optimiser.actions;


import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.CPCConstants;
import org.electrocodeogram.cpc.core.api.data.CloneDiff;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.UpdateMode;
import org.electrocodeogram.cpc.ui.api.AbstractCPCViewActionDelegate;


public class PurgeCloneDiffHistoryAction extends AbstractCPCViewActionDelegate
{
	private static final Log log = LogFactory.getLog(PurgeCloneDiffHistoryAction.class);

	public PurgeCloneDiffHistoryAction()
	{
		log.trace("PurgeCloneDiffHistoryAction()");
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

		/*
		 * Check the selection.
		 */
		IStructuredSelection selection = getSelection();
		if (selection == null || selection.getFirstElement() == null)
		{
			log.debug("run() - no selection, terminating.");
			showError("Purge Clone History", "No selection. You need to select a clone.");
			return;
		}

		Object obj = selection.getFirstElement();

		if (!(obj instanceof IClone))
		{
			log.error("run() - unknown object type: " + obj, new Throwable()); //$NON-NLS-1$
			showError("Purge Clone History", "Unknown object in selection. You need to select a clone.");
			return;
		}

		boolean pressedOk = showConfirm(
				"Purge Clone History",
				"Are you sure that you want to purge the modification history for this clone?\nAll changes will be merged into a single change entry.\n\nThis action can not be undone.");

		if (!pressedOk)
		{
			//the user aborted
			log.trace("run() - user aborted, terminating.");
			return;
		}

		IClone extClone = (IClone) obj;

		//get a store provider instance
		IStoreProvider storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				IStoreProvider.class);

		int diffCount = 0;

		/*
		 * Purge the clone diff data. 
		 */
		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			//get a fresh copy of the clone
			IClone clone = storeProvider.lookupClone(extClone.getUuid());

			ICloneModificationHistoryExtension history = (ICloneModificationHistoryExtension) storeProvider
					.getFullCloneObjectExtension(clone, ICloneModificationHistoryExtension.class);

			if (history == null)
			{
				log.trace("run() - clone has no modification history, nothing to do.");
			}
			else
			{
				if (log.isTraceEnabled())
					log.trace("run() - going to purge history: " + history);

				diffCount = history.getCloneDiffs().size();

				//delete all history diffs
				assert (!history.isPartial());
				history.clearCloneDiffs();

				//add one new diff to "bridge" the gap between original content and current content.
				//TODO: use CompositCloneDiff here?
				history.addCloneDiff(new CloneDiff(CPCConstants.CLONEDIFF_CREATOR_CPC_OPTIMISER, new Date(), false, 0,
						extClone.getOriginalContent().length(), extClone.getContent()));

				clone.addExtension(history);

				storeProvider.updateClone(clone, UpdateMode.MOVED);
			}
		}
		catch (StoreLockingException e)
		{
			log.error("run() - locking error - clone: " + extClone, e); //$NON-NLS-1$
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}

		showMessage("Purge Clone History", "Clone History successfully purged.\n" + diffCount
				+ " history entries removed.");
	}

}
