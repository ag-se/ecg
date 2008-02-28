package org.electrocodeogram.cpc.core.api.data.special;


import java.util.Date;

import org.electrocodeogram.cpc.core.api.data.CloneDiff;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;


/**
 * Special extension interface for {@link ICloneModificationHistoryExtension} which contains methods which may
 * only be called by the current {@link IStoreProvider}.
 * 
 * @author vw
 * 
 * @see ICloneModificationHistoryExtension
 * @see IStoreProvider
 */
public interface IStoreCloneModificationHistoryExtension extends ICloneModificationHistoryExtension
{
	/**
	 * Indicates the end of the current {@link IStoreProvider} transaction.
	 * <p>
	 * <b>IMPORTANT:</b> This method must only be used from within the {@link IStoreProvider}!
	 * 
	 * @see ICloneModificationHistoryExtension#getCloneDiffsForTransaction()
	 * @see IStoreProvider
	 */
	//TODO: should we move this method to a separate interface?
	public void endOfTransaction();

	/**
	 * Retrieves the creation date of the last (oldest) {@link CloneDiff} of this extension at the time of the
	 * last call of {@link #endOfTransaction()}.
	 * <br>
	 * If {@link #endOfTransaction()} was never called or if no {@link CloneDiff}
	 * elements were added at that point in time, this method will return NULL.
	 * <p>
	 * <b>IMPORTANT:</b> This method must only be used from within the {@link IStoreProvider}!
	 * 
	 * @return creation date of oldest diff during last call of {@link #endOfTransaction()}, may be NULL.
	 */
	//TODO: should we move this method to a separate interface?
	public Date getEndOfTransactionCloneDiffCreationDate();

	/**
	 * Sets the {@link #getEndOfTransactionCloneDiffCreationDate()} value.
	 * <br>
	 * This method is used by the {@link IStoreProvider} to "merge" multiple {@link ICloneModificationHistoryExtension}s,
	 * if needed.
	 * <p>
	 * <b>IMPORTANT:</b> This method must only be used from within the {@link IStoreProvider}!
	 * 
	 * @param endOfTransactionCloneDiffCreationDate the new end of transaction creation date, may be NULL.
	 */
	//TODO: should we move this method to a separate interface?
	public void setEndOfTransactionCloneDiffCreationDate(Date endOfTransactionCloneDiffCreationDate);

	/**
	 * Checks whether this extension was cleared ({@link ICloneModificationHistoryExtension#clearCloneDiffs()}) since
	 * the end of the last transaction.
	 * <br>
	 * This value is set to <em>true</em> when {@link ICloneModificationHistoryExtension#clearCloneDiffs()} is called
	 * and is reset to <em>false</em> when {@link #endOfTransaction()} is called.
	 * <br>
	 * The default value is <em>false</em>.
	 * 
	 * @return <em>true</em> if this extension was cleared recently, <em>false</em> otherwise.
	 */
	public boolean wasCleared();

}
