package org.electrocodeogram.cpc.test.data;


import java.util.Map;

import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionStatefulObject;
import org.electrocodeogram.cpc.store.data.extension.AbstractStatefulCloneObjectExtension;


public class SomeStatefulCloneObjectExtension extends AbstractStatefulCloneObjectExtension implements
		ICloneObjectExtensionStatefulObject, ISomeStatefulCloneObjectExtension
{
	private static final long serialVersionUID = 1L;

	private String data;

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.test.data.ISomeStatefulCloneObjectExtension#getData()
	 */
	public String getData()
	{
		return data;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.test.data.ISomeStatefulCloneObjectExtension#setData(java.lang.String)
	 */
	public void setData(String data)
	{
		this.data = data;

		setDirty(true);
	}

	@Override
	public String getPersistenceParentClassIdentifier()
	{
		return IClone.PERSISTENCE_CLASS_IDENTIFIER;
	}

	@Override
	public String getPersistenceClassIdentifier()
	{
		return PERSISTENCE_CLASS_IDENTIFIER;
	}

	@Override
	public String getPersistenceObjectIdentifier()
	{
		return "parent_uuid";
	}

	@Override
	public Map<String, Comparable<? extends Object>> getState()
	{
		Map<String, Comparable<? extends Object>> state = super.getState();

		state.put("data", data);

		return state;
	}

	@Override
	public Map<String, Class<? extends Object>> getStateTypes()
	{
		Map<String, Class<? extends Object>> stateTypes = super.getStateTypes();

		stateTypes.put("data", String.class);

		return stateTypes;
	}

	@Override
	public void setState(Map<String, Comparable<? extends Object>> state)
	{
		assert (state != null);

		data = (String) state.get("data");

		super.setState(state);
	}

	@Override
	public Class<? extends ICloneObjectExtension> getExtensionInterfaceClass()
	{
		return ISomeStatefulCloneObjectExtension.class;
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	@Override
	public String toString()
	{
		return getSealStatus() + "SomeStatefulCloneObjectExtension[data: " + data + ", parentUuid: " + getParentUuid()
				+ "]";
	}
}
