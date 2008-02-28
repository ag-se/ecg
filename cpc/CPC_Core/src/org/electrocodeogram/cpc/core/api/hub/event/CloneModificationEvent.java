package org.electrocodeogram.cpc.core.api.hub.event;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.CloneDiff;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubRegistry;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;


/**
 * Modification event object for clone data modifications. Send whenever the clone data for a file is changed
 * for some reason.
 * <p>
 * NOTE:
 * <ul>
 * 	<li>All values MAY BE NULL</li>
 * 	<li>The same clone may be contained in more than one of the lists.</li>
 *  <li>This event represents a collection of all events which happened during one IStoreProvider "transaction".
 *  	As such a clone may appear multiple times in different stages. I.e. it was added, moved and modified
 *  	within a single transaction. If a clone was updated multiple times, it will be listed only once in the
 *  	modified and moved clones lists (the latest version).
 *  <li>A clone that is part of the deleted list, will not appear in any other list.</li>
 *  <li>If all clone data for a large number of files (or all clone data) is removed/updated
 *  	a special format may be used.
 *  	<br>
 *  	{@link #getCloneFile()} is NULL, {@link #isFullModification()} is true
 *  	and all clone lists are NULL.</li>
 *  <li>All objects contained in this event are cloned. Thus instances of the same clone will not match between different
 *  	lists. Use equals() to compare clone objects, not ==.</li>
 *  <li><b>The clone objects must not be modified by a receiver.</b> They are <u>shared</u> between all receivers of this event.</li>
 * </ul>
 * <b>IMPORTANT:</b> Make sure you understand the effects of the {@link IStoreProvider.LockMode} value during
 * 		the acquisition of an exclusive {@link IStoreProvider} lock before you create any events of this
 * 		type yourself.
 * 
 * @author vw
 * 
 * @see IStoreProvider
 * @see IStoreProvider.LockMode
 * @see IStoreProvider.UpdateMode
 * @see IStoreProvider#acquireWriteLock(org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode)
 * @see IStoreProvider#releaseWriteLock()
 */
public class CloneModificationEvent extends CloneEvent
{
	private static final Log log = LogFactory.getLog(CloneModificationEvent.class);

	protected boolean fullModification = false;
	protected List<IClone> addedClones = null;
	protected List<IClone> modifiedClones = null;
	protected List<IClone> movedClones = null;
	protected List<IClone> removedClones = null;

	/**
	 * Creates a new {@link CloneModificationEvent} instance for dispatching via the event hub registry.
	 * <br>
	 * i.e. {@code CPCCorePlugin.getEventHubRegistry().dispatch(newEvent);}
	 * <p>
	 * The <em>cloneFile</em> parameter may be NULL, in the special case that this event is meant to
	 * indicate to all interested parties that <b>all</b> clone data has been potentially removed/updated.
	 * <br>
	 * In this case fullModification has to be true and all lists must be null.
	 * 
	 * @param cloneFile the file for this event, if the event is specific to one file, may be NULL.
	 * @see IEventHubRegistry#dispatch(CPCEvent)
	 * @see CPCCorePlugin#getEventHubRegistry()
	 */
	public CloneModificationEvent(ICloneFile cloneFile)
	{
		super(cloneFile);

		if (log.isTraceEnabled())
			log.trace("CloneModificationEvent() - cloneFile: " + cloneFile);
	}

	/**
	 * Indicates whether the entire clone data for the file was modified.
	 * <br>
	 * In this case all clone lists are NULL and a receiver should retrieve the
	 * latest clone data from the store provider.
	 * <p>
	 * This typically happens when:
	 * <ul>
	 * 	<li>a file is reverted to an earlier state</li>
	 *  <li>...</li>
	 * </ul>
	 * 
	 * @return true if this event represents a full modification of the clone data 
	 */
	public boolean isFullModification()
	{
		return fullModification;
	}

	/**
	 * This may only be set to true if all clone lists of this event are null. 
	 * 
	 * @param fullModification true if this event represents a full modification of the clone data
	 * 
	 * @see #isFullModification()
	 */
	public void setFullModification(boolean fullModification)
	{
		if (log.isTraceEnabled())
			log.trace("setFullModification(): " + fullModification);
		assert (!fullModification || (addedClones == null && modifiedClones == null && movedClones == null && removedClones == null));

		checkSeal();

		this.fullModification = fullModification;
	}

	/**
	 * A list of clones which were added during this event.
	 * <br>
	 * They may also be moved and/or modified by the same event!
	 * 
	 * @return may be NULL
	 */
	public List<IClone> getAddedClones()
	{
		return addedClones;
	}

	/**
	 * Must only be called ONCE.
	 * 
	 * @param addedClones corresponding clone list, never null
	 * 
	 * @see #getAddedClones()
	 */
	public void setAddedClones(List<IClone> addedClones)
	{
		if (log.isTraceEnabled())
			log.trace("setAddedClones(): " + addedClones);
		assert (this.addedClones == null && addedClones != null && getCloneFile() != null);

		checkSeal();

		this.addedClones = addedClones;
	}

	/**
	 * A list of clones which had their contents changed during this event.
	 * <br>
	 * They may also be added and/or moved by the same event!
	 * <br>
	 * A clone will not appear more than once within this list. It will only contain the latest version.
	 * <p>
	 * Modified clones <u>usually</u> carry an {@link ICloneModificationHistoryExtension} object which
	 * contains {@link CloneDiff} data for all modifications since the last time the
	 * clone was part of an {@link CloneModificationEvent}.
	 * <br>
	 * However, there are some special circumstances under which no modification history data
	 * is available for a clone which might have been modified. I.e. if a file is reverted or if a file
	 * was externally modified and it is not possible to generate an exact {@link CloneDiff} description
	 * of the change.
	 * 
	 * @return may be NULL
	 * 
	 * @see ICloneModificationHistoryExtension
	 * @see CloneDiff
	 */
	public List<IClone> getModifiedClones()
	{
		return modifiedClones;
	}

	/**
	 * Must only be called ONCE.
	 * 
	 * @param modifiedClones corresponding clone list, never null
	 * 
	 * @see #getModifiedClones()
	 */
	public void setModifiedClones(List<IClone> modifiedClones)
	{
		if (log.isTraceEnabled())
			log.trace("setModifiedClones(): " + modifiedClones);
		assert (this.modifiedClones == null && modifiedClones != null && getCloneFile() != null);

		checkSeal();

		this.modifiedClones = modifiedClones;

		/*
		 * Some debug checking. Only enabled if in debug mode.
		 */
		if (CPCCorePlugin.isDebugChecking())
		{
			//modified clones are supposed to contain at least one CloneDiff event.
			if (modifiedClones != null)
			{
				for (IClone clone : modifiedClones)
				{
					ICloneModificationHistoryExtension history = (ICloneModificationHistoryExtension) clone
							.getExtension(ICloneModificationHistoryExtension.class);
					if (history == null || history.getCloneDiffsForTransaction().isEmpty())
					{
						/*
						 * There are some cases were this is allowed. I.e. on a file revert.
						 */
						log.debug(
								"setModifiedClones() - modified clone contains no edit history for this transaction - clone: "
										+ clone + ", history: " + history, new Throwable());
					}
				}
			}
		}
	}

	/**
	 * A list of clones which were moved during this event.
	 * <br>
	 * They may also be added and/or modified by the same event!
	 * <br>
	 * A clone will not appear more than once within this list. It will only contain the latest version.
	 * 
	 * @return may be NULL
	 */
	public List<IClone> getMovedClones()
	{
		return movedClones;
	}

	/**
	 * Must only be called ONCE.
	 * 
	 * @param movedClones corresponding clone list, never null
	 * 
	 * @see #getMovedClones()
	 */
	public void setMovedClones(List<IClone> movedClones)
	{
		if (log.isTraceEnabled())
			log.trace("setMovedClones(): " + movedClones);
		assert (this.movedClones == null && movedClones != null && getCloneFile() != null);

		checkSeal();

		this.movedClones = movedClones;
	}

	/**
	 * A list of clones which were removed during this event.
	 * <br>
	 * Any clone which is part of this list will not appear in any other list.
	 * <br>
	 * A clone which was added and removed during a single transaction will not
	 * be part of any list.
	 * 
	 * @return may be NULL
	 */
	public List<IClone> getRemovedClones()
	{
		return removedClones;
	}

	/**
	 * Must only be called ONCE.
	 * 
	 * @param removedClones corresponding clone list, never null
	 * 
	 * @see #getRemovedClones()
	 */
	public void setRemovedClones(List<IClone> removedClones)
	{
		if (log.isTraceEnabled())
			log.trace("setRemovedClones(): " + removedClones);
		assert (this.removedClones == null && removedClones != null && getCloneFile() != null);

		checkSeal();

		this.removedClones = removedClones;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.hub.event.CPCEvent#isValid()
	 */
	@Override
	public boolean isValid()
	{
		//full modification may only be set if all clone lists are null
		if (fullModification
				&& (addedClones != null || modifiedClones != null || movedClones != null || removedClones != null))
			return false;

		//clone file may only be null if fullModification is true and all clone lists are null
		if (getCloneFile() == null
				&& (!fullModification || addedClones != null || modifiedClones != null || movedClones != null || removedClones != null))
			return false;

		//all list elements should be sealed
		for (List<IClone> cloneList : new List[] { addedClones, modifiedClones, movedClones, removedClones })
			if (cloneList != null && !cloneList.isEmpty())
				for (IClone clone : cloneList)
					if (!clone.isSealed())
						return false;

		return super.isValid();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.hub.event.CPCEvent#toString()
	 */
	@Override
	public String toString()
	{
		return "CloneModificationEvent[" + subToString() + ", fullModification: " + fullModification
				+ ", addedClones: " + addedClones + ", modifiedClones: " + modifiedClones + ", movedClones: "
				+ movedClones + ", removedClones: " + removedClones + "]";
	}
}
