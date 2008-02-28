package org.electrocodeogram.cpc.store.data.extension;


import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionStatefulObject;


public abstract class AbstractStatefulCloneObjectExtension extends AbstractCloneObjectExtension implements
		ICloneObjectExtensionStatefulObject
{
	private static final Log log = LogFactory.getLog(AbstractStatefulCloneObjectExtension.class);

	private boolean dirty = false;

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionStatefulObject#isDirty()
	 */
	@Override
	public boolean isDirty()
	{
		return dirty;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionStatefulObject#setDirty(boolean)
	 */
	@Override
	public void setDirty(boolean dirty)
	{
		if (log.isTraceEnabled())
			log.trace("setDirty() - dirty: " + dirty);

		this.dirty = dirty;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.special.IStatefulObject#getState()
	 */
	@Override
	public Map<String, Comparable<? extends Object>> getState()
	{
		Map<String, Comparable<? extends Object>> state = new HashMap<String, Comparable<? extends Object>>(5);

		state.put(ICloneObjectExtensionStatefulObject.PERSISTENCE_OBJECT_IDENTIFIER, getParentUuid());

		return state;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.special.IStatefulObject#getStateTypes()
	 */
	@Override
	public Map<String, Class<? extends Object>> getStateTypes()
	{
		Map<String, Class<? extends Object>> stateTypes = new HashMap<String, Class<? extends Object>>(5);

		stateTypes.put(ICloneObjectExtensionStatefulObject.PERSISTENCE_OBJECT_IDENTIFIER, String.class);

		return stateTypes;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.special.IStatefulObject#setState(java.util.Map)
	 */
	@Override
	public void setState(Map<String, Comparable<? extends Object>> state)
	{
		assert (state != null);

		try
		{
			setParentUuid((String) state.get(ICloneObjectExtensionStatefulObject.PERSISTENCE_OBJECT_IDENTIFIER));
		}
		catch (Exception e)
		{
			log.error("setState() - unable to restore state: " + state + " - " + e, e);
		}
	}

}
