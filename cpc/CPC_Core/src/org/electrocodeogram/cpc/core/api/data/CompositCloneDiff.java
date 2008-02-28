package org.electrocodeogram.cpc.core.api.data;


import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.special.IStatefulObject;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;


/**
 * A special composite version of the {@link CloneDiff} class.
 * <br>
 * The {@link IStoreProvider} may merge multiple {@link CloneDiff} objects into one to reduce
 * the number of objects and thereby the storage complexity. If multiple {@link CloneDiff} events
 * are merged, the resulting composite diff is of this type.
 * 
 * @author vw
 * 
 * @see CloneDiff
 * @deprecated this class is currently not in use.
 */
@Deprecated
public class CompositCloneDiff extends CloneDiff
{
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(CompositCloneDiff.class);

	/**
	 * {@link IStatefulObject} persistence class identifier, value: "<em>composite_clone_diff</em>"
	 */
	public final String PERSISTENCE_CLASS_IDENTIFIER = "composite_clone_diff";

	private Date startOfRangeDate;
	private Date endOfRangeDate;

	/**
	 * Creates a new {@link CompositCloneDiff}.
	 * <br>
	 * A {@link CompositCloneDiff} does <b>not</b> contain a list of {@link CloneDiff} objects. Instead the
	 * changes made by the included {@link CloneDiff} objects is merged into a single diff object.
	 * <p>
	 * A {@link CompositCloneDiff} object must always contain <b>all</b> {@link CloneDiff} objects which fall
	 * into the given date range described by {@link CompositCloneDiff#getStartOfRangeDate()} and
	 * {@link CompositCloneDiff#getEndOfRangeDate()}. No {@link CloneDiff} object may be left out.
	 * <br>
	 * Once a {@link CompositCloneDiff} is stored, its underlying individual {@link CloneDiff} objects may
	 * <b>not</b> be made accessible to a client in any way.
	 * <br>
	 * I.e. care must be taken that the
	 * {@link IStoreProvider#getFullCloneObjectExtension(ICloneObject, Class)} command does not return any {@link CloneDiff}
	 * objects which are already "contained" within an also returned {@link CompositCloneDiff} object.
	 * <p>
	 * Please refer to the {@link CloneDiff} constructor for more information about the common parameters.
	 * 
	 * @param startOfRangeDate creation date of the oldest {@link CloneDiff} in this composite, never null.
	 * @param endOfRangeDate creation date of the youngest {@link CloneDiff} in this composite, never null.
	 * @param creator creator (username) value of the oldest {@link CloneDiff} object in this composite
	 * 		for which the creator is defined, may be NULL if no {@link CloneDiff} object specified a creator.
	 * 
	 * @see CloneDiff#CloneDiff(String, Date, boolean, int, int, String)
	 */
	public CompositCloneDiff(Date startOfRangeDate, Date endOfRangeDate, String creator, Date creationDate,
			boolean automaticChange, int offset, int length, String text)
	{
		super(creator, creationDate, automaticChange, offset, length, text);

		if (log.isTraceEnabled())
			log.trace("CompositCloneDiff() - ..., startOfRangeDate: " + startOfRangeDate + ", endOfRangeDate: "
					+ endOfRangeDate);
		assert (startOfRangeDate != null && endOfRangeDate != null && !startOfRangeDate.after(endOfRangeDate));

		this.startOfRangeDate = startOfRangeDate;
		this.endOfRangeDate = endOfRangeDate;
	}

	/**
	 * The {@link CloneDiff#getCreationDate()} of the first {@link CloneDiff} object in this composite diff.
	 * 
	 * @return creation date of oldest {@link CloneDiff} object in this composite, never null.
	 */
	public Date getStartOfRangeDate()
	{
		return startOfRangeDate;
	}

	/**
	 * The {@link CloneDiff#getCreationDate()} of the last {@link CloneDiff} object in this composite diff.
	 * 
	 * @return creation date of youngest {@link CloneDiff} object in this composite, never null.
	 */
	public Date getEndOfRangeDate()
	{
		return endOfRangeDate;
	}

	/**
	 * Retrieves the creator (username) of this composite diff.
	 * <br>
	 * If the underlying {@link CloneDiff} objects refer to different creators, the first (oldest) defined
	 * creator value will be used for the composite diff.
	 * <br>
	 * The value may be NULL if the creator was undefined for all underlying {@link CloneDiff} objects.
	 * 
	 * @return creator of this diff, may be NULL.
	 */
	@Override
	public String getCreator()
	{
		return super.getCreator();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.CloneDiff#getPersistenceClassIdentifier()
	 */
	@Override
	public String getPersistenceClassIdentifier()
	{
		return PERSISTENCE_CLASS_IDENTIFIER;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.CloneDiff#getState()
	 */
	@Override
	public Map<String, Comparable<? extends Object>> getState()
	{
		Map<String, Comparable<? extends Object>> result = super.getState();

		result.put("startOfRangeDate", startOfRangeDate);
		result.put("endOfRangeDate", endOfRangeDate);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.CloneDiff#getStateTypes()
	 */
	@Override
	public Map<String, Class<? extends Object>> getStateTypes()
	{
		Map<String, Class<? extends Object>> result = super.getStateTypes();

		result.put("startOfRangeDate", Date.class);
		result.put("endOfRangeDate", Date.class);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.CloneDiff#setState(java.util.Map)
	 */
	@Override
	public void setState(Map<String, Comparable<? extends Object>> state)
	{
		super.setState(state);

		try
		{
			startOfRangeDate = (Date) state.get("startOfRangeDate");
			endOfRangeDate = (Date) state.get("endOfRangeDate");
		}
		catch (Exception e)
		{
			//this should not happen
			log.error("setState() - error while restoring internal state - state: " + state + " - " + e, e);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.CloneDiff#toString()
	 */
	@Override
	public String toString()
	{
		return "CompositCloneDiff[startOfRangeDate: " + startOfRangeDate + ", endOfRangeDate: " + endOfRangeDate
				+ ", content: " + super.toString() + "]";
	}
}
