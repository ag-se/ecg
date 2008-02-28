package org.electrocodeogram.cpc.store.data;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.collection.ICloneGroupInterfaces;


public class CloneGroup extends CloneObject implements ICloneGroupInterfaces
{
	private static final Log log = LogFactory.getLog(CloneGroup.class);

	private static final long serialVersionUID = 1L;

	public CloneGroup()
	{
		log.trace("CloneGroup()");
	}

	public CloneGroup(String uuid)
	{
		super(uuid);

		if (log.isTraceEnabled())
			log.trace("CloneGroup() - uuid: " + uuid);
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
	 * NOTE:
	 * 	clone(), equals() and hashCode() are implemented by CloneObject
	 */

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneGroup#toString()
	 */
	@Override
	public String toString()
	{
		return getSealStatus() + "CloneGroup[uuid: " + uuid + "]";
	}
}
