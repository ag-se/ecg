package org.electrocodeogram.cpc.store.data.extension;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.CloneDiff;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionLazyMultiStatefulObject;
import org.electrocodeogram.cpc.core.api.data.special.IStoreCloneModificationHistoryExtension;


/**
 * Default implementation of {@link ICloneModificationHistoryExtension}.
 * 
 * @author vw
 */
public class CloneModificationHistoryExtension extends AbstractStatefulCloneObjectExtension implements
		IStoreCloneModificationHistoryExtension, ICloneObjectExtensionLazyMultiStatefulObject
{
	private static Log log = LogFactory.getLog(CloneModificationHistoryExtension.class);

	public static final String PERSISTENCE_CLASS_IDENTIFIER = "clone_mod_history";
	public static final List<String> PERSISTENCE_MULTI_CLASS_IDENTIFIER = Arrays.asList("clone_diff");
	public static final List<String> PERSISTENCE_MULTI_OBJECT_IDENTIFIER = Arrays
			.asList(CloneDiff.PERSISTENCE_OBJECT_IDENTIFIER);

	private static final long serialVersionUID = 1L;
	private static final List<CloneDiff> EMPTY_CLONEDIFF_LIST = new ArrayList<CloneDiff>(0);

	private LinkedList<CloneDiff> cloneDiffs = null;
	private List<CloneDiff> deletedCloneDiffs = null;
	private Date endOfTransactionCloneDiffCreationDate = null;
	private boolean partial = false;
	private boolean cleared = false;

	public CloneModificationHistoryExtension()
	{
		log.trace("CloneModificationHistoryExtension()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension#getCloneDiffs()
	 */
	@Override
	public List<CloneDiff> getCloneDiffs()
	{
		if (cloneDiffs == null || cloneDiffs.isEmpty())
			return EMPTY_CLONEDIFF_LIST;
		else
			return Collections.unmodifiableList(cloneDiffs);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension#getCloneDiffsForTransaction()
	 */
	@Override
	public List<CloneDiff> getCloneDiffsForTransaction()
	{
		//if we don't have an end of transaction creation date, we behave like getCloneDiffs()
		if (endOfTransactionCloneDiffCreationDate == null)
			return getCloneDiffs();

		//there is nothing to do, if there are no CloneDiff elements
		if (cloneDiffs == null || cloneDiffs.isEmpty())
		{
			return EMPTY_CLONEDIFF_LIST;
		}
		else
		{
			/*
			 * Build a list of all diffs which are older than endOfTransactionCloneDiffCreationDate.
			 */
			List<CloneDiff> result = new LinkedList<CloneDiff>();

			for (CloneDiff diff : cloneDiffs)
			{
				if (diff.getCreationDate().after(endOfTransactionCloneDiffCreationDate))
					result.add(diff);
				else
					//diffs should always be in ascending order according to creation date
					assert (result.isEmpty());
			}

			return result;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension#getValidCreationDate()
	 */
	@Override
	public Date getValidCreationDate()
	{
		//if there are no other CloneDiffs, the current date is valid
		if (cloneDiffs == null || cloneDiffs.isEmpty())
			return new Date();

		long curTime = System.currentTimeMillis();
		CloneDiff lastDiff = cloneDiffs.getLast();
		assert (lastDiff != null && lastDiff.getCreationDate() != null);

		if (lastDiff.getCreationDate().getTime() >= curTime)
		{
			//this time is already in use, increment by one
			curTime = lastDiff.getCreationDate().getTime() + 1;
		}

		return new Date(curTime);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension#addCloneDiff(org.electrocodeogram.cpc.core.api.data.CloneDiff)
	 */
	@Override
	public void addCloneDiff(CloneDiff cloneDiff)
	{
		if (log.isTraceEnabled())
			log.trace("addCloneDiff(): " + cloneDiff);
		assert (cloneDiff != null);

		checkSeal();

		if (cloneDiffs == null)
			cloneDiffs = new LinkedList<CloneDiff>();

		//do some additional integrity checking, if we're in debug checking mode
		if (CPCCorePlugin.isDebugChecking())
		{
			//double check that there is no other CloneDiff entry with the same creation date
			for (CloneDiff otherDiff : cloneDiffs)
			{
				if (cloneDiff.getCreationDate().equals(otherDiff.getCreationDate()))
				{
					log.error("addCloneDiff() - trying to add CloneDiff with exisitng creation date - existing entry: "
							+ otherDiff + ", new entry: " + cloneDiff, new Throwable());
				}
			}
		}

		cloneDiffs.add(cloneDiff);

		setDirty(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension#addCloneDiffs(java.util.List)
	 */
	@Override
	public void addCloneDiffs(List<CloneDiff> cloneDiffs)
	{
		if (log.isTraceEnabled())
			log.trace("addCloneDiffs(): " + cloneDiffs);
		assert (cloneDiffs != null);

		checkSeal();

		if (this.cloneDiffs == null)
			this.cloneDiffs = new LinkedList<CloneDiff>();

		//do some additional integrity checking, if we're in debug checking mode
		if (CPCCorePlugin.isDebugChecking())
		{
			//double check that there is no other CloneDiff entry with the same creation date
			for (CloneDiff cloneDiff : cloneDiffs)
			{
				for (CloneDiff otherDiff : cloneDiffs)
				{
					if (cloneDiff.getCreationDate().equals(otherDiff.getCreationDate()))
					{
						log.error(
								"addCloneDiff() - trying to add CloneDiff with exisitng creation date - existing entry: "
										+ otherDiff + ", new entry: " + cloneDiff, new Throwable());
					}
				}
			}
		}

		this.cloneDiffs.addAll(cloneDiffs);

		setDirty(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension#addCloneDiffs(java.util.SortedSet)
	 */
	@Override
	public void addCloneDiffs(SortedSet<CloneDiff> cloneDiffs)
	{
		if (log.isTraceEnabled())
			log.trace("addCloneDiffs(): " + cloneDiffs);
		assert (cloneDiffs != null);

		checkSeal();

		if (this.cloneDiffs == null)
			this.cloneDiffs = new LinkedList<CloneDiff>();

		this.cloneDiffs.addAll(cloneDiffs);

		setDirty(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension#clearCloneDiffs()
	 */
	@Override
	public void clearCloneDiffs() throws IllegalStateException
	{
		log.trace("clearCloneDiffs()");

		if (isPartial())
			throw new IllegalStateException("method call is only allowed if extension is not partial");

		checkSeal();

		if (cloneDiffs != null)
		{
			deletedCloneDiffs = cloneDiffs;
			cloneDiffs = new LinkedList<CloneDiff>();
		}

		endOfTransactionCloneDiffCreationDate = null;
		cleared = true;

		setDirty(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension#wasCleared()
	 */
	@Override
	public boolean wasCleared()
	{
		return cleared;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension#endOfTransaction()
	 */
	@Override
	public void endOfTransaction()
	{
		log.trace("endOfTransaction()");

		checkSeal();

		cleared = false;

		if (cloneDiffs == null || cloneDiffs.isEmpty())
		{
			//nothing to do, no clone diffs yet
			log.trace("endOfTransaction() - no clone diffs available yet, skipped.");
			return;
		}

		//remember the creation date of the oldest (last) clone diff
		endOfTransactionCloneDiffCreationDate = cloneDiffs.getLast().getCreationDate();

		if (log.isTraceEnabled())
			log.trace("endOfTransaction() - new end of transaction diff creation date: "
					+ endOfTransactionCloneDiffCreationDate);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension#getEndOfTransactionCloneDiffCreationDate()
	 */
	@Override
	public Date getEndOfTransactionCloneDiffCreationDate()
	{
		return endOfTransactionCloneDiffCreationDate;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension#setEndOfTransactionCloneDiffCreationDate(java.util.Date)
	 */
	@Override
	public void setEndOfTransactionCloneDiffCreationDate(Date endOfTransactionCloneDiffCreationDate)
	{
		if (log.isTraceEnabled())
			log.trace("setEndOfTransactionCloneDiffCreationDate(): " + endOfTransactionCloneDiffCreationDate);

		checkSeal();

		this.endOfTransactionCloneDiffCreationDate = endOfTransactionCloneDiffCreationDate;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionLazyMultiStatefulObject#isPartial()
	 */
	@Override
	public boolean isPartial()
	{
		return partial;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionLazyMultiStatefulObject#setPartial(boolean)
	 */
	@Override
	public void setPartial(boolean partial)
	{
		if (log.isTraceEnabled())
			log.trace("setPartial(): " + partial);

		this.partial = partial;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension#getExtensionInterfaceClass()
	 */
	@Override
	public Class<? extends ICloneObjectExtension> getExtensionInterfaceClass()
	{
		return ICloneModificationHistoryExtension.class;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.ICloneObjectExtensionMultiStatefulObject#getMultiPersistenceClassIdentifier()
	 */
	@Override
	public List<String> getMultiPersistenceClassIdentifier()
	{
		return PERSISTENCE_MULTI_CLASS_IDENTIFIER;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.ICloneObjectExtensionMultiStatefulObject#getMultiPersistenceObjectIdentifier()
	 */
	@Override
	public List<String> getMultiPersistenceObjectIdentifier()
	{
		return PERSISTENCE_MULTI_OBJECT_IDENTIFIER;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.ICloneObjectExtensionMultiStatefulObject#getMultiState()
	 */
	@Override
	public List<List<Map<String, Comparable<? extends Object>>>> getMultiState()
	{
		int resultSize = (cloneDiffs != null ? cloneDiffs.size() : 0);
		List<Map<String, Comparable<? extends Object>>> states = new ArrayList<Map<String, Comparable<? extends Object>>>(
				resultSize);

		if (cloneDiffs != null)
		{
			for (CloneDiff diff : cloneDiffs)
			{
				Map<String, Comparable<? extends Object>> state = diff.getState();
				//parent_uuid
				state.put(PERSISTENCE_OBJECT_IDENTIFIER, getParentUuid());
				states.add(state);
			}
		}

		if (deletedCloneDiffs != null)
		{
			for (CloneDiff diff : deletedCloneDiffs)
			{
				Map<String, Comparable<? extends Object>> state = diff.getState();
				//parent_uuid
				state.put(PERSISTENCE_OBJECT_IDENTIFIER, getParentUuid());
				//deletion marker
				state.put(DELETION_MARK_IDENTIFIER, new Boolean(true));
				states.add(state);
			}
		}

		List<List<Map<String, Comparable<?>>>> result = new ArrayList<List<Map<String, Comparable<?>>>>(1);
		result.add(states);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.ICloneObjectExtensionMultiStatefulObject#getMultiStateTypes()
	 */
	@Override
	public List<Map<String, Class<? extends Object>>> getMultiStateTypes()
	{
		//create a temporary instance
		CloneDiff diff = new CloneDiff();

		Map<String, Class<? extends Object>> stateTypes = diff.getStateTypes();
		//parent_uuid
		stateTypes.put(PERSISTENCE_OBJECT_IDENTIFIER, String.class);

		List<Map<String, Class<? extends Object>>> result = new ArrayList<Map<String, Class<? extends Object>>>(1);
		result.add(stateTypes);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.ICloneObjectExtensionMultiStatefulObject#setMultiState(java.util.List)
	 */
	@Override
	public void setMultiState(List<List<Map<String, Comparable<? extends Object>>>> states)
	{
		if (log.isTraceEnabled())
			log.trace("setMultiState(): " + states);

		checkSeal();

		if (states == null)
		{
			//clear all clone diff data

			log.trace("setMultiState() - clearing all clone diff data.");

			if (cloneDiffs != null)
				cloneDiffs.clear();

			return;
		}

		assert (states.size() == 1);

		if (cloneDiffs == null)
			cloneDiffs = new LinkedList<CloneDiff>();
		else
			cloneDiffs.clear();

		for (Map<String, Comparable<? extends Object>> state : states.get(0))
		{
			CloneDiff diff = new CloneDiff();
			diff.setState(state);
			cloneDiffs.add(diff);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionMultiStatefulObject#purgeDeletedEntries()
	 */
	@Override
	public void purgeDeletedEntries()
	{
		log.trace("purgeDeletedEntries()");

		if (deletedCloneDiffs != null)
		{
			deletedCloneDiffs.clear();
			deletedCloneDiffs = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#getPersistenceClassIdentifier()
	 */
	@Override
	public String getPersistenceClassIdentifier()
	{
		return PERSISTENCE_CLASS_IDENTIFIER;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#getPersistenceObjectIdentifier()
	 */
	@Override
	public String getPersistenceObjectIdentifier()
	{
		//parent_uuid
		return PERSISTENCE_OBJECT_IDENTIFIER;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.ICloneObjectExtensionStatefulObject#getPersistenceParentClassIdentifier()
	 */
	@Override
	public String getPersistenceParentClassIdentifier()
	{
		return IClone.PERSISTENCE_CLASS_IDENTIFIER;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#getState()
	 */
	@Override
	public Map<String, Comparable<? extends Object>> getState()
	{
		Map<String, Comparable<? extends Object>> state = super.getState();

		//nothing to persist atm

		return state;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#getStateTypes()
	 */
	@Override
	public Map<String, Class<? extends Object>> getStateTypes()
	{
		Map<String, Class<? extends Object>> stateTypes = super.getStateTypes();

		//nothing to persist atm

		return stateTypes;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#setState(java.util.Map)
	 */
	@Override
	public void setState(Map<String, Comparable<? extends Object>> state)
	{
		if (log.isTraceEnabled())
			log.trace("setState(): " + state);
		assert (state != null);

		checkSeal();

		//nothing to restore atm

		super.setState(state);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		CloneModificationHistoryExtension result = (CloneModificationHistoryExtension) super.clone();

		//we need a deep copy of the clone diff list
		if (this.cloneDiffs != null)
		{
			result.cloneDiffs = new LinkedList<CloneDiff>();
			for (CloneDiff diff : this.cloneDiffs)
				result.cloneDiffs.add((CloneDiff) diff.clone());
		}

		//TODO: are these "copy actions" needed? These fields should already be copied by Object.clone(), right?
		result.endOfTransactionCloneDiffCreationDate = this.endOfTransactionCloneDiffCreationDate;

		result.partial = this.partial;

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return getSealStatus() + "CloneModificationHistoryExtension[endOfTrans: "
				+ endOfTransactionCloneDiffCreationDate + ", " + getCloneDiffs().size() + " total, "
				+ getCloneDiffsForTransaction().size() + " new, dirty: " + isDirty() + ", partial: " + isPartial()
				+ ", cloneDiffs: " + cloneDiffs + "]";
	}

}
