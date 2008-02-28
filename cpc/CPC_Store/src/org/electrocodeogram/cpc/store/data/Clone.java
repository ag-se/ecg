package org.electrocodeogram.cpc.store.data;


import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneObject;
import org.electrocodeogram.cpc.core.api.data.collection.ICloneInterfaces;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


/**
 * POJO which hold all data about a specific clone.<br/>
 * <br/> 
 * CompareTo orders by start offset.
 * 
 * @author vw
 */
public class Clone extends CloneObject implements ICloneInterfaces
{
	private static Log log = LogFactory.getLog(Clone.class);

	private static final long serialVersionUID = 1L;
	private static final Set<String> EMPTY_STRING_SET = Collections.unmodifiableSet(new HashSet<String>(0));

	/**
	 * Whether to append extension data to toString() results.<br/>
	 * Mainly interesting during debugging of extensions.
	 */
	private static final boolean logExtensionData = true;

	/*
	 * Identity
	 */
	private Date creationDate;
	private String creator;

	/*
	 * File
	 */
	private String fileUuid;

	/*
	 * Position
	 */
	private int offset = -1;
	private int length = -1;

	/*
	 * Relationships
	 */
	private String groupUuid;
	private String originUuid;

	/*
	 * Classifications
	 */
	private Set<String> classifications;

	/*
	 * Content
	 */
	private String content;
	private String originalContent;
	private Date modificationDate;

	/*
	 * State
	 */

	private State cloneState = State.DEFAULT;
	private Date cloneStateChangeDate;
	private Date cloneStateDismissalDate = null;
	private double cloneStateWeight = 0;
	private String cloneStateMessage = null;

	//whether this clone should be persisted or not
	private boolean _transient = false;

	/**
	 * Instantiates a new <em>Clone</em> object. Automatically assigns a unique UUID.
	 */
	public Clone()
	{
		super();

		log.trace("Clone()");
	}

	public Clone(String uuid)
	{
		super(uuid);

		if (log.isTraceEnabled())
			log.trace("Clone() - uuid: " + uuid);
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#getCreationDate()
	 */
	@Override
	public Date getCreationDate()
	{
		return creationDate;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.ICreatorClone#setCreationDate(java.util.Date)
	 */
	@Override
	public void setCreationDate(Date creationDate)
	{
		if (log.isTraceEnabled())
			log.trace("setCreationDate(): " + creationDate);
		assert (creationDate != null);

		checkSeal();

		this.creationDate = creationDate;

		//also update modification and state change date
		this.modificationDate = creationDate;
		this.cloneStateChangeDate = creationDate;

		setDirty(true);
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#getCreator()
	 */
	@Override
	public String getCreator()
	{
		return creator;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.ICreatorClone#setCreator(java.lang.String)
	 */
	@Override
	public void setCreator(String creator)
	{
		if (log.isTraceEnabled())
			log.trace("setCreator(): " + creator);
		assert (creator != null);

		checkSeal();

		this.creator = creator;
		setDirty(true);
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#getFileUuid()
	 */
	@Override
	public String getFileUuid()
	{
		return fileUuid;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.ICreatorClone#setFileUuid(java.lang.String)
	 */
	@Override
	public void setFileUuid(String fileUuid)
	{
		if (log.isTraceEnabled())
			log.trace("setFileUuid(): " + fileUuid);
		assert (fileUuid != null);

		checkSeal();

		this.fileUuid = fileUuid;
		setDirty(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#getOffset()
	 */
	@Override
	public int getOffset()
	{
		return offset;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#setOffset(int)
	 */
	@Override
	public void setOffset(int offset)
	{
		if (log.isTraceEnabled())
			log.trace("setOffset(): " + offset);
		assert (offset >= 0);

		checkSeal();

		this.offset = offset;
		setDirty(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#getLength()
	 */
	@Override
	public int getLength()
	{
		return length;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#setLength(int)
	 */
	@Override
	public void setLength(int length)
	{
		if (log.isTraceEnabled())
			log.trace("setLength(): " + length);
		assert (length >= 0);

		checkSeal();

		this.length = length;
		setDirty(true);
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#getGroupUuid()
	 */
	@Override
	public String getGroupUuid()
	{
		return groupUuid;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#setGroupUuid(java.lang.String)
	 */
	@Override
	public void setGroupUuid(String groupUuid)
	{
		if (log.isTraceEnabled())
			log.trace("setGroupUuid(): " + groupUuid);

		checkSeal();

		this.groupUuid = groupUuid;
		setDirty(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#getOriginUuid()
	 */
	@Override
	public String getOriginUuid()
	{
		return originUuid;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#setOriginUuid(java.lang.String)
	 */
	@Override
	public void setOriginUuid(String originUuid)
	{
		if (log.isTraceEnabled())
			log.trace("setOriginUuid(): " + originUuid);

		checkSeal();

		this.originUuid = originUuid;
		setDirty(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#getClassifications()
	 */
	@Override
	public Set<String> getClassifications()
	{
		if (classifications != null)
			return Collections.unmodifiableSet(classifications);
		else
			return EMPTY_STRING_SET;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#hasClassification(java.lang.String)
	 */
	@Override
	public boolean hasClassification(String classification)
	{
		assert (classification != null);

		if (classifications == null)
			return false;

		return classifications.contains(classification);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#addClassification(java.lang.String)
	 */
	@Override
	public void addClassification(String classification)
	{
		if (log.isTraceEnabled())
			log.trace("addClassification(): " + classification);
		assert (classification != null);

		checkSeal();

		if (classifications == null)
			classifications = new HashSet<String>(3);

		classifications.add(classification);
		setDirty(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#removeClassification(java.lang.String)
	 */
	@Override
	public void removeClassification(String classification)
	{
		if (log.isTraceEnabled())
			log.trace("removeClassification(): " + classification);
		assert (classification != null);

		checkSeal();

		if (classifications == null)
			return;

		classifications.remove(classification);
		setDirty(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#getOriginalContent()
	 */
	@Override
	public String getOriginalContent()
	{
		return originalContent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#getContent()
	 */
	@Override
	public String getContent()
	{
		return content;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.ICreatorClone#setContent(java.lang.String)
	 */
	@Override
	public void setContent(String content)
	{
		if (log.isTraceEnabled())
			log.trace("setContent(): " + content);
		assert (content != null);

		checkSeal();

		this.content = content;

		//the very first call to setContent() should also set the original content value.
		if (this.originalContent == null)
			this.originalContent = content;

		//also update modification date
		this.modificationDate = new Date();

		setDirty(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#getModificationDate()
	 */
	@Override
	public Date getModificationDate()
	{
		return modificationDate;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#getCloneState()
	 */
	@Override
	public State getCloneState()
	{
		return cloneState;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#getCloneStateChangeDate()
	 */
	@Override
	public Date getCloneStateChangeDate()
	{
		return cloneStateChangeDate;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#getCloneStateDismissalDate()
	 */
	@Override
	public Date getCloneStateDismissalDate()
	{
		return cloneStateDismissalDate;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#getCloneStateMessage()
	 */
	@Override
	public String getCloneStateMessage()
	{
		return cloneStateMessage;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#getCloneStateWeight()
	 */
	@Override
	public double getCloneStateWeight()
	{
		return cloneStateWeight;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#setCloneState(org.electrocodeogram.cpc.core.api.data.IClone.State, double, java.lang.String)
	 */
	@Override
	public void setCloneState(State cloneState, double weight, String message)
	{
		if (log.isTraceEnabled())
			log.trace("setCloneState(): " + cloneState + ", weight: " + weight + ", message: " + message);
		assert (cloneState != null);

		checkSeal();

		this.cloneState = cloneState;
		this.cloneStateWeight = weight;
		this.cloneStateMessage = message;

		//also update clone state change date
		this.cloneStateChangeDate = new Date();

		setDirty(true);
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#isTransient()
	 */
	@Override
	public boolean isTransient()
	{
		return _transient;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#setTransient(boolean)
	 */
	@Override
	public void setTransient(boolean _transient)
	{
		if (log.isTraceEnabled())
			log.trace("setTransient(): " + _transient);

		checkSeal();

		this._transient = _transient;
		setDirty(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#getPersistenceIdentifier()
	 */
	@Override
	public String getPersistenceClassIdentifier()
	{
		return PERSISTENCE_CLASS_IDENTIFIER;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#getState()
	 */
	@Override
	public Map<String, Comparable<? extends Object>> getState()
	{
		log.trace("getState()");

		//first get the internal state of our super class
		Map<String, Comparable<?>> result = super.getState();

		//now add our data
		result.put("creationDate", creationDate);
		result.put("creator", creator);
		result.put("fileUuid", fileUuid);
		result.put("groupUuid", groupUuid);
		result.put("originUuid", originUuid);
		result.put("offset", offset);
		result.put("length", length);
		result.put("classifications", (classifications == null ? "" : CoreUtils.collectionToString(classifications)));

		result.put("cloneState", cloneState.toString());
		result.put("cloneStateChangeDate", cloneStateChangeDate);
		result.put("cloneStateDismissalDate", cloneStateDismissalDate);
		result.put("cloneStateWeight", cloneStateWeight);
		result.put("cloneStateMessage", cloneStateMessage);
		//_transient is not persisted

		result.put("content", content);
		result.put("originalContent", originalContent);
		result.put("modificationDate", modificationDate);

		if (log.isTraceEnabled())
			log.trace("getState() - result: " + result);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.data.CloneObject#getStateTypes()
	 */
	@Override
	public Map<String, Class<? extends Object>> getStateTypes()
	{
		log.trace("getStateTypes()");

		//first get the internal state of our super class
		Map<String, Class<? extends Object>> result = super.getStateTypes();

		//now add our data
		result.put("creationDate", Date.class);
		result.put("creator", String.class);
		result.put("fileUuid", String.class);
		result.put("groupUuid", String.class);
		result.put("originUuid", String.class);
		result.put("offset", Integer.class);
		result.put("length", Integer.class);
		result.put("classifications", String.class);

		result.put("cloneState", String.class);
		result.put("cloneStateChangeDate", Date.class);
		result.put("cloneStateDismissalDate", Date.class);
		result.put("cloneStateWeight", Double.class);
		result.put("cloneStateMessage", String.class);
		//_transient is not persisted

		result.put("content", String.class);
		result.put("originalContent", String.class);
		result.put("modificationDate", Date.class);

		if (log.isTraceEnabled())
			log.trace("getStateTypes() - result: " + result);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.data.CloneObject#setState(java.util.Map)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setState(Map<String, Comparable<? extends Object>> state)
	{
		if (log.isTraceEnabled())
			log.trace("setState() - state: " + state);
		assert (state != null);

		checkSeal();

		//first set the internal state of our super class
		super.setState(state);

		//now set our data
		creationDate = (Date) state.get("creationDate");
		creator = (String) state.get("creator");
		fileUuid = (String) state.get("fileUuid");
		groupUuid = (String) state.get("groupUuid");
		originUuid = (String) state.get("originUuid");
		offset = (Integer) state.get("offset");
		length = (Integer) state.get("length");
		if ("".equals(state.get("classifications")))
			classifications = null;
		else
			classifications = new HashSet<String>(CoreUtils.collectionFromString((String) state.get("classifications")));

		cloneState = State.valueOf((String) state.get("cloneState"));
		cloneStateChangeDate = (Date) state.get("cloneStateChangeDate");
		cloneStateDismissalDate = (Date) state.get("cloneStateDismissalDate");
		cloneStateWeight = (Double) state.get("cloneStateWeight");
		cloneStateMessage = (String) state.get("cloneStateMessage");
		//_transient is not persisted

		content = (String) state.get("content");
		originalContent = (String) state.get("originalContent");
		modificationDate = (Date) state.get("modificationDate");
	}

	/*
	 * Convenience methods.
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#getEndOffset()
	 */
	@Override
	public int getEndOffset()
	{
		return offset + length - 1;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#intersects(org.electrocodeogram.cpc.core.api.data.IClone)
	 */
	@Override
	public boolean intersects(IClone clone)
	{
		if (log.isTraceEnabled())
			log.trace("intersects() - otherClone: " + clone + ", this: " + this);
		assert (clone != null);

		return intersects(clone.getOffset(), clone.getLength());
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#intersects(int, int)
	 */
	@Override
	public boolean intersects(int otherOffset, int otherLength)
	{
		if (log.isTraceEnabled())
			log.trace("intersects() - offset: " + otherOffset + ", length: " + otherLength);
		assert (otherOffset >= 0 && otherLength >= 0);

		int otherEndOffset = (otherLength > 0 ? otherOffset + otherLength - 1 : otherOffset);

		if ((getOffset() <= otherOffset) && (otherOffset <= getEndOffset()))
			return true;

		if ((getOffset() <= otherEndOffset) && (otherEndOffset <= getEndOffset()))
			return true;

		if ((otherOffset <= getOffset()) && (getOffset() <= otherEndOffset))
			return true;

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		Clone result = new Clone(uuid);

		result.creationDate = creationDate;
		result.creator = creator;
		result.fileUuid = fileUuid;
		result.groupUuid = groupUuid;
		result.originUuid = originUuid;
		result.offset = offset;
		result.length = length;
		if (classifications != null)
			result.classifications = new HashSet<String>(classifications);

		result.cloneState = cloneState;
		result.cloneStateChangeDate = cloneStateChangeDate;
		result.cloneStateDismissalDate = cloneStateDismissalDate;
		result.cloneStateWeight = cloneStateWeight;
		result.cloneStateMessage = cloneStateMessage;
		result._transient = _transient;

		result.content = content;
		result.originalContent = originalContent;
		result.modificationDate = modificationDate;

		//copy over any data fields of our super class too
		result.cloneData(this);

		return result;
	}

	/*
	 * NOTE:
	 * 	equals() and hashCode() are implemented by CPCDataObject
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#equalsAll(org.electrocodeogram.cpc.core.api.data.IClone)
	 */
	@Override
	public boolean equalsAll(ICloneObject otherClone)
	{
		if (log.isTraceEnabled())
			log.trace("equalsAll() - this: " + this + ", otherClone: " + otherClone);

		if (otherClone == null)
		{
			log.debug("equalsAll() - otherclone is null");
			return false;
		}

		if (getClass() != otherClone.getClass())
		{
			//this shouldn't happen during normal use
			log.warn("equalsAll() - class missmatch - this.class: " + this.getClass() + ", otherClone.class: "
					+ otherClone.getClass() + ", this: " + this + ", otherClone: " + otherClone);
			return false;
		}

		Clone other = (Clone) otherClone;

		//first check via superclass, this will also do an uuid check
		if (!super.equalsAll(other))
		{
			if (log.isDebugEnabled())
				log.debug("equalsAll() - super missmatch - this: " + this + ", otherClone: " + otherClone);
			return false;
		}

		//now check all our fields
		if ((creator != null) && (!creator.equals(other.creator)))
		{
			if (log.isDebugEnabled())
				log.debug("equalsAll() - creator missmatch - this: " + this + ", otherClone: " + otherClone);
			return false;
		}

		if ((fileUuid != null) && (!fileUuid.equals(other.fileUuid)))
		{
			if (log.isDebugEnabled())
				log.debug("equalsAll() - fileUuid missmatch - this: " + this + ", otherClone: " + otherClone);
			return false;
		}

		if (offset != other.getOffset() || length != other.getLength())
		{
			if (log.isDebugEnabled())
				log.debug("equalsAll() - position missmatch - this: " + this + ", otherClone: " + otherClone);
			return false;
		}

		if ((groupUuid != null) && (!groupUuid.equals(other.groupUuid)))
		{
			if (log.isDebugEnabled())
				log.debug("equalsAll() - groupUuid missmatch - this: " + this + ", otherClone: " + otherClone);
			return false;
		}

		if ((originUuid != null) && (!originUuid.equals(other.originUuid)))
		{
			if (log.isDebugEnabled())
				log.debug("equalsAll() - originUuid missmatch - this: " + this + ", otherClone: " + otherClone);
			return false;
		}

		if ((classifications != null) && (!classifications.equals(other.classifications)))
		{
			if (log.isDebugEnabled())
				log.debug("equalsAll() - classifications missmatch - this: " + this + ", otherClone: " + otherClone);
			return false;
		}

		if (_transient != other._transient)
		{
			if (log.isDebugEnabled())
				log.debug("equalsAll() - transient missmatch - this: " + this + ", otherClone: " + otherClone);

			return false;
		}

		if (!cloneState.equals(other.cloneState))
		{
			if (log.isDebugEnabled())
				log.debug("equalsAll() - cloneState missmatch - this: " + this + ", otherClone: " + otherClone);

			return false;
		}

		log.trace("equalsAll() - result: true");

		return true;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#compareTo(org.electrocodeogram.cpc.core.api.data.Clone)
	 * 
	 * We're ordering by start offset here
	 */
	@Override
	public int compareTo(IClone o)
	{
		if (this.equals(o))
		{
			//ok, perfect match we're done
			return 0;
		}
		//this is the only check which is meaningful if one of the clone objects is an "empty" delimiter object
		//used in a SortedSet operation
		else if (offset != o.getOffset())
		{
			//ok, the offsets differ, this is easy
			return offset - o.getOffset();
		}
		//note: for an empty delimiter object all absolute position values are -1
		else
		{
			//we're in some trouble now. The two clones are not equal but start at the same position.
			//we'll fall back to the end position in that case
			if (getEndOffset() != o.getEndOffset())
			{
				//ok, we can use the end offset
				return getEndOffset() - o.getEndOffset();
			}
			else
			{
				/*
				 * Both clones cover exactly the same code, the might thus be equal, but
				 * for some reason they got different uuids.
				 * 
				 * This can happen temporarily within a collection of changes caused by one
				 * event. I.e. if one clone is deleted and another one takes it's place both
				 * positions will be equal but the "offending" clone will be removed soon.
				 * 
				 * This can also happen if two clones are moved in a way which positions one
				 * clone exactly on the old location of the other clone. If the first clone is
				 * moved first, the cache will temporarily contain two clones on the same position.
				 * However, the other clone will be moved away soon.
				 * 
				 * Another situation in which this can happen are two clones which are overlapping
				 * each other and which are both reduced in size due to document changes.
				 * I.e.  ("." clone A, "," clone B)
				 * 	initial state:	....;;;;;,,,,
				 *  1st edit:		....;;;XXXXX -> ....;;;
				 *  2nd	edit:		XXXXX;; -> ;;
				 *  result:			both clones have the same start and end offsets
				 *
				 * It is no clear how such a case should be best handled. Simply merging the two clones
				 * might cause some problems, as they both have other clone group members and the similarity
				 * between the members of the different groups may be quite low.
				 * A sensible approach seems to be:
				 * 	- detect this situation and execute some special merging evaluation code
				 *  - if the similarity between the two groups is high, merge the groups
				 *  - if the similarity is low, either ignore the situation or drop one or both of the modified clones.
				 * 
				 * @see org.electrocodeogram.cpc.store.provider.AbstractStoreProvider#_checkIntegrityAfterChange(List<IClone>)
				 */
				if (log.isDebugEnabled())
					log.debug("compareTo(): found two clones with equal position but different uuids: " + this + ", "
							+ o);

				/*
				 * It's tricky to decide what to return here, the clones should clearly be equal,
				 * however, equals() returned false and by contract we may not return 0 if equals is false.
				 * We fallback to the objects hashcode for now. 
				 */
				return this.hashCode() - o.hashCode();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.IClone#toString()
	 */
	@Override
	public String toString()
	{
		return getSealStatus() + "Clone[uuid: " + uuid + ", off: " + offset + ", len: " + length + ", state: "
				+ cloneState + ", trans: " + _transient + ", hasExt: " + hasExtensions() + ", dirty: " + isDirty()
				+ ", date: " + creationDate + ", creator: " + creator + ", file: " + fileUuid + ", group: " + groupUuid
				+ ", originUuid: " + originUuid + " -- extensions: " + getExtensions().size()
				+ (logExtensionData ? " - " + getExtensions() : "") + "]";
	}
}
