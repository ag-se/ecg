package org.electrocodeogram.cpc.core.api.provider.reconciler;


import java.util.List;

import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.IProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;


/**
 * Interface for external modification reconciliation providers.
 * <br>
 * Only one reconciler can be active at any point in time. However, a reconciler
 * will typically allow other modules to contribute their own reconciliation sub-strategies.
 * 
 * @author vw
 */
public interface IReconcilerProvider extends IProvider
{
	/**
	 * Tries to reconcile an external modification of a clone file and the internal clone data.
	 * <br>
	 * Takes the existing clone data and tries to identify the new positions and sizes in the new file content.
	 * <br>
	 * Clones may be moved, modified or deleted. No new clones may be added by the reconciler.
	 * <p>
	 * A reconciler <b>does not</b> access or modify the corresponding clone file via the file system and <b>does not</b>
	 * modify the clone data directly, i.e. via the {@link IStoreProvider}.
	 * <br>
	 * A reconciler may not have any side effects.
	 * <p>
	 * After the reconciler returns, the non-removed clone data remaining in the {@link IReconciliationResult} must
	 * be valid. All clones for which the new position could not be calculated must be listed in the
	 * {@link IReconciliationResult}'s removedClones list.
	 * <p>
	 * If no reconciliation is needed, the reconciler returns a {@link IReconciliationResult} where all clone lists
	 * are null.
	 * 
	 * @param cloneFile the clone file which was modified, never null.
	 * @param persistedClones the currently persisted clone data for the file, never null.
	 * 		Clone list is <b>sorted by start offset</b>.
	 * @param persistedFileContent the currently persisted content for the file, may be NULL.
	 * @param newFileContent the new content which was produced by some external modification to the file, may be NULL.
	 * @param notifyUser true if the user should be notified about this reconciliation (or rather the fact that an
	 * 		external modification has taken place). The user should at least be offered two choices: try to reconcile changes
	 * 		or drop all clone data for file. It is up to the provider implementation to decide on
	 * 		a suitable way of displaying this information to the user (i.e. a simple dialog or an entire wizard).
	 * @return a valid reconciliation result, never null.
	 */
	public IReconciliationResult reconcile(ICloneFile cloneFile, List<IClone> persistedClones,
			String persistedFileContent, String newFileContent, boolean notifyUser);

}
