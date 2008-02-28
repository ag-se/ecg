package org.electrocodeogram.cpc.test.data;


import java.util.HashMap;
import java.util.Map;

import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionMultiStatefulObject;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionStatefulObject;


public class SomeDataElement implements Comparable<SomeDataElement>
{
	private int id;
	private String data;
	private boolean deleted = false;

	public SomeDataElement()
	{

	}

	public SomeDataElement(int id, String data)
	{
		assert (id >= 0 && data != null);

		this.id = id;
		this.data = data;
	}

	public int getId()
	{
		return id;
	}

	public String getData()
	{
		return data;
	}

	public void setData(String data)
	{
		this.data = data;
	}

	boolean isDeleted()
	{
		return deleted;
	}

	void delete()
	{
		deleted = true;
	}

	public Map<String, Comparable<? extends Object>> getState(String parentUuid)
	{
		Map<String, Comparable<? extends Object>> state = new HashMap<String, Comparable<? extends Object>>(4);

		state.put("id", id);
		state.put("data", data);
		state.put(ICloneObjectExtensionStatefulObject.PERSISTENCE_OBJECT_IDENTIFIER, parentUuid);

		if (deleted)
			state.put(ICloneObjectExtensionMultiStatefulObject.DELETION_MARK_IDENTIFIER, true);

		return state;
	}

	public Map<String, Class<? extends Object>> getStateTypes()
	{
		Map<String, Class<? extends Object>> stateTypes = new HashMap<String, Class<? extends Object>>(3);

		stateTypes.put("id", Integer.class);
		stateTypes.put("data", String.class);

		stateTypes.put(ICloneObjectExtensionStatefulObject.PERSISTENCE_OBJECT_IDENTIFIER, String.class);

		return stateTypes;
	}

	public void setState(Map<String, Comparable<? extends Object>> state)
	{
		assert (state != null);
		assert (!state.containsKey(ICloneObjectExtensionMultiStatefulObject.DELETION_MARK_IDENTIFIER));

		id = (Integer) state.get("id");
		data = (String) state.get("data");
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
		result = prime * result + id;
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
		final SomeDataElement other = (SomeDataElement) obj;
		if (id != other.id)
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SomeDataElement o)
	{
		return this.id - o.id;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "SomeDataElement[id: " + id + ", data: " + data + ", deleted: " + deleted + "]";
	}
}
