package org.electrocodeogram.cpc.core.api.data;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.DocumentEvent;
import org.electrocodeogram.cpc.core.api.data.special.IStatefulObject;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.api.provider.track.IPositionUpdateStrategyProvider;
import org.electrocodeogram.cpc.core.utils.CoreHistoryUtils;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;


/**
 * Clone diff objects represent modifications which were made to the content of a clone.
 * <br>
 * Their semantics are the same as those of {@link DocumentEvent}s but all positions are
 * relative to the offset of the affected clone.
 * <p>
 * Due to performance considerations and its inherent simplicity this class is not hidden
 * behind any interface and its implementation is provided directly by the CPC Core and
 * not the {@link ICloneFactoryProvider}.
 * <p>
 * <b>NOTE:</b> a {@link CloneDiff} instance only describes the part of a {@link DocumentEvent}
 * which affected the corresponding clone. I.e. if a modification started before the start offset
 * of the clone than only the part of the modification which was attributed to the clone, should
 * be part of the {@link CloneDiff} event. This may require some translation of offsets.
 * A relative {@link CloneDiff} offset must never be negative.
 * <br>
 * The goal is that {@link CloneDiff} events can be applied to a standalone copy of the corresponding
 * clone, without having to take any modifications outside of the clone into account.
 * 
 * @author vw
 * 
 * @see IPositionUpdateStrategyProvider
 * @see CoreHistoryUtils
 */
public class CloneDiff implements ICloneObjectSupport, IStatefulObject, Comparable<CloneDiff>
{
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(CloneDiff.class);
	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

	/**
	 * {@link IStatefulObject} persistence class identifier, value: "<em>clone_diff</em>"
	 */
	public static final String PERSISTENCE_CLASS_IDENTIFIER = "clone_diff";

	/**
	 * {@link IStatefulObject} persistence object identifier, value: "creationDate"
	 * <br>
	 * {@link #getCreationDate()} is guaranteed to be a unique value per clone.
	 */
	public static final String PERSISTENCE_OBJECT_IDENTIFIER = "creationDate";

	protected String creator;
	protected Date creationDate;
	protected boolean automaticChange = false;

	protected int offset;
	protected int length;
	protected String text;

	/**
	 * This constructor may only be used while restoring {@link CloneDiff} objects.
	 * <br>
	 * The values for this diff can then only be set via {@link CloneDiff#setState(Map)}.
	 */
	public CloneDiff()
	{

	}

	/**
	 * Creates a new, immutable {@link CloneDiff} instance.
	 * <p>
	 * <b>NOTE:</b> The <em>creationDate</em> needs to be a <b>unique</b> value for the corresponding clone.
	 * As java {@link Date}s offer only millisecond precision and many system clocks much less than
	 * that, it would be possible for multiple {@link CloneDiff} instances to be created with the same
	 * date value.
	 * <br>
	 * This needs to be prevented. Any code which creates multiple {@link CloneDiff} instances in quick
	 * succession has to <b>ensure that no two diffs are created with the same <em>creationDate</em> value</b>.
	 * <br>
	 * One possible approach to this requirement would be to remember the time of the last {@link CloneDiff}
	 * instance and to artificially increase the <em>creationDate</em> by one millisecond if it is equal to
	 * the <em>creationDate</em> of the last event. 
	 * 
	 * @param creator the creator (username) of this diff, NULL if unknown.
	 * @param creationDate the creation time of this diff, never null.
	 * 		This value needs to be <b>unique</b> within one clone instance. See above.
	 * @param automaticChange true if it can be guaranteed that this change was not done directly by a human.
	 * 		I.e. refactorings or source code reformats.
	 * @param offset the start offset of this diff, relative to the start offset of the clone, always &gt= 0.
	 * @param length the number of characters replaced by this modification.
	 * 		0 if this modification only added text. Always &gt;=0.
	 * @param text the text which was inserted by this modification, may be NULL.
	 */
	public CloneDiff(String creator, Date creationDate, boolean automaticChange, int offset, int length, String text)
	{
		assert (creationDate != null && offset >= 0 && length >= 0);

		this.creator = creator;
		this.creationDate = creationDate;
		this.automaticChange = automaticChange;

		this.offset = offset;
		this.length = length;
		this.text = text;
	}

	/**
	 * Retrieves the offset of this modification.
	 * 
	 * @return the 0-based offset at which this modification begins. 
	 * 		Offsets are relative to the start offset of the corresponding clone.
	 * 		Always &gt;=0. 
	 */
	public int getOffset()
	{
		return offset;
	}

	/**
	 * Retrieves the number of characters replaced by this modification.
	 * 
	 * @return the number of characters replaced by this modification.
	 * 		0 if this modification only added text. Always &gt;=0.
	 */
	public int getLength()
	{
		return length;
	}

	/**
	 * Retrieves the text which was inserted by this modification.
	 * 
	 * @return the text which was inserted by this modification, may be NULL.
	 */
	public String getText()
	{
		return text;
	}

	/**
	 * Retrieves the length of the text which was inserted by this modification.
	 * <br>
	 * Convenience Method.
	 * 
	 * @return length of {@link CloneDiff#getText()} or 0 if it is null. Always &gt;=0.
	 */
	public int getTextLength()
	{
		return (text == null ? 0 : text.length());
	}

	/**
	 * Retrieves the creator (username) of this diff.
	 * <br>
	 * The value may be NULL if the creator could not be identified.
	 * 
	 * @return creator of this diff, may be NULL.
	 */
	public String getCreator()
	{
		return creator;
	}

	/**
	 * Retrieves the creation time of this diff.
	 * <br>
	 * It is up to the creator of the {@link CloneDiff} object to ensure uniqueness of
	 * creation times within the diff events of one clone object.
	 * <br>
	 * See: {@link #CloneDiff(String, Date, boolean, int, int, String)}
	 * 
	 * @return creation time of this diff, never null.
	 */
	public Date getCreationDate()
	{
		return creationDate;
	}

	/**
	 * Checks whether this {@link CloneDiff} was created by some automated action, i.e.
	 * a refactoring or a source code reformat.
	 * <br>
	 * This is a best effort value. It is guaranteed that the change was not made directly
	 * by a human if this value is true. However, if the value is false nothing can be guaranteed.
	 * 
	 * @return <em>true</em> if this change was definitely not done by a human. <em>False</em> if a human
	 * 	made the change or if unsure.
	 */
	public boolean isAutomaticChange()
	{
		return automaticChange;
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
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#getState()
	 */
	@Override
	public Map<String, Comparable<? extends Object>> getState()
	{
		Map<String, Comparable<? extends Object>> result = new HashMap<String, Comparable<? extends Object>>(10);

		result.put("creator", creator);
		result.put("creationDate", creationDate);
		result.put("automaticChange", automaticChange);

		result.put("offset", offset);
		result.put("length", length);
		result.put("text", text);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#getStateTypes()
	 */
	@Override
	public Map<String, Class<? extends Object>> getStateTypes()
	{
		Map<String, Class<? extends Object>> result = new HashMap<String, Class<? extends Object>>(10);

		result.put("creator", String.class);
		result.put("creationDate", Date.class);
		result.put("automaticChange", Boolean.class);

		result.put("offset", Integer.class);
		result.put("length", Integer.class);
		result.put("text", String.class);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#setState(java.util.Map)
	 */
	@Override
	public void setState(Map<String, Comparable<? extends Object>> state)
	{
		if (log.isTraceEnabled())
			log.trace("setState() - state: " + state);
		assert (state != null);

		try
		{
			creator = (String) state.get("creator");
			creationDate = (Date) state.get("creationDate");
			automaticChange = (Boolean) state.get("automaticChange");

			offset = (Integer) state.get("offset");
			length = (Integer) state.get("length");
			text = (String) state.get("text");
		}
		catch (Exception e)
		{
			//this should not happen
			log.error("setState() - error while restoring internal state - state: " + state + " - " + e, e);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneDataElement#isSealed()
	 */
	@Override
	public boolean isSealed()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneDataElement#seal()
	 */
	@Override
	public void seal()
	{
		//Nothing to do, a CloneDiff object is immutable.
	}

	/**
	 * {@link CloneDiff} objects are immutable.
	 * <br>
	 * <b>WARNING:</b> For performance reasons this method does <b>not</b> create a clone
	 * of the object, but instead it simply returns the object itself.
	 * <br>
	 * For normal use inside of CPC this does not make a difference due to the immutable state
	 * of this object.
	 * <br>
	 * However, in violation of the {@link Object#clone()} contract,
	 * <code>cloneDiff.clone() == cloneDiff</code> will be <b>true</b>.
	 */
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final CloneDiff other = (CloneDiff) obj;
		if (creationDate == null)
		{
			if (other.creationDate != null)
				return false;
		}
		else if (!creationDate.equals(other.creationDate))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CloneDiff o)
	{
		return (int) (this.creationDate.getTime() - o.creationDate.getTime());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "CloneDiff[o: " + offset + ", l: " + length + ", t: " + CoreStringUtils.truncateString(text) + ", a: "
				+ automaticChange + ", c: " + creator + ", d: " + simpleDateFormat.format(creationDate) + "]";
	}
}
