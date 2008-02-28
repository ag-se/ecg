package org.electrocodeogram.cpc.core.api.data.extension;


import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import org.electrocodeogram.cpc.core.api.data.CloneDiff;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneObject;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionLazyMultiStatefulObject;
import org.electrocodeogram.cpc.core.api.data.special.IStoreCloneModificationHistoryExtension;
import org.electrocodeogram.cpc.core.api.hub.event.CloneModificationEvent;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.UpdateMode;


/**
 * Extension object which is used by the {@link IStoreProvider} to attach {@link CloneDiff}
 * data of all modifications since the last event on generation of {@link CloneModificationEvent}s and which
 * can also be used to retrieve a list of all modifications made to a clone since its creation.
 * <p>
 * The {@link IStoreProvider} will also process this extension for all calls to
 * {@link IStoreProvider#addClone(IClone)} or
 * {@link IStoreProvider#updateClone(IClone, UpdateMode)}.
 * <br>
 * Any {@link CloneDiff} objects present will be added to the internal modification history of the store provider and can
 * be retrieved by a call to {@link IStoreProvider#getFullCloneObjectExtension(ICloneObject, Class)} at any time.
 * <p>
 * This extension is persisted and lazily restored, see {@link ICloneObjectExtensionLazyMultiStatefulObject}.
 * <p>
 * Used by the {@link IStoreProvider} and the CPC Notification module.
 * <p>
 * <b>Important: All implementations of this interface also need to implement {@link IStoreCloneModificationHistoryExtension}!</b>
 * 
 * @author vw
 * 
 * @see IStoreCloneModificationHistoryExtension
 * @see IStoreProvider#getFullCloneObjectExtension(ICloneObject, Class)
 * @see IStoreProvider#releaseWriteLock()
 * @see IStoreProvider.LockMode
 * @see IStoreProvider#addClone(IClone)
 * @see IStoreProvider#updateClone(IClone, UpdateMode)
 * @see CloneDiff
 * @see CloneModificationEvent
 * @see ICloneObjectExtensionLazyMultiStatefulObject
 */
public interface ICloneModificationHistoryExtension extends ICloneObjectExtension
{
	/**
	 * Yields a list of all modifications ({@link CloneDiff}s) made to this clone object.
	 * <br>
	 * The returned list or its contents may not be modified in any way.
	 * 
	 * @return a list of clone diffs, may not be modified, may be empty, never null.
	 */
	public List<CloneDiff> getCloneDiffs();

	/**
	 * Yields a list of all modifications ({@link CloneDiff}s) made to this clone object
	 * since the last call of {@link IStoreCloneModificationHistoryExtension#endOfTransaction()}.
	 * <br> 
	 * This means the list contains all {@link CloneDiff}s which were added to this
	 * extension since the last {@link CloneModificationEvent} in which this clone was marked as
	 * modified.
	 * <br>
	 * The returned list or its contents may not be modified in any way.
	 * 
	 * @return a list of clone diffs, may not be modified, may be empty, never null.
	 */
	public List<CloneDiff> getCloneDiffsForTransaction();

	/**
	 * Returns the the next valid creation date for use in a {@link CloneDiff} element
	 * for this extension.
	 * <p>
	 * This is either the current time or, if another {@link CloneDiff} element with that
	 * creation date already exists, the current time incremented by a couple of milliseconds.
	 * <p>
	 * This is important in order for a user of this extension to ensure that no two
	 * {@link CloneDiff} elements with the same creation date are added.
	 * 
	 * @return valid creation date for a {@link CloneDiff} element, never null.
	 */
	public Date getValidCreationDate();

	/**
	 * Adds a new {@link CloneDiff} object to this clone modification history extension.
	 * <br>
	 * {@link CloneDiff}s may only be added in correct time order (youngest first).
	 *  
	 * @param cloneDiff the clone diff to add, never null.
	 * 
	 * @throws IllegalArgumentException if <em>cloneDiff</em> is younger than the oldest existing {@link CloneDiff} element.
	 */
	public void addCloneDiff(CloneDiff cloneDiff);

	/**
	 * Adds a list of new {@link CloneDiff} objects to this clone modification history extension.
	 * <br>
	 * {@link CloneDiff}s may only be added in correct time order (youngest first).
	 * 
	 * @param cloneDiffs a list of {@link CloneDiff} objects to add, never null.
	 * 
	 * @throws IllegalArgumentException if an added <em>cloneDiff</em> is younger than the oldest existing {@link CloneDiff} element.
	 */
	public void addCloneDiffs(List<CloneDiff> cloneDiffs);

	/**
	 * Adds a list of new {@link CloneDiff} objects to this clone modification history extension.
	 * <br>
	 * {@link CloneDiff}s may only be added in correct time order (youngest first).
	 * 
	 * @param cloneDiffs a sorted set of {@link CloneDiff} objects to add, never null.
	 * 
	 * @throws IllegalArgumentException if an added <em>cloneDiff</em> is younger than the oldest existing {@link CloneDiff} element.
	 */
	public void addCloneDiffs(SortedSet<CloneDiff> cloneDiffs);

	/**
	 * Removes all {@link CloneDiff} objects from this clone modification history extension.
	 * <br>
	 * This method may only be used if this extension was fully loaded ({@link ICloneObjectExtension#isPartial()} is false).
	 * <p>
	 * This method is typically used when the clone history is either to be completely purged or to be
	 * replaced by some composite diffs.
	 * 
	 * @throws IllegalStateException is {@link ICloneObjectExtension#isPartial()} is true.
	 */
	public void clearCloneDiffs() throws IllegalStateException;

}
