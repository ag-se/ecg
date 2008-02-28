package org.electrocodeogram.cpc.test.data;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionMultiStatefulObject;


public class SomeMultiStatefulCloneObjectExtension extends SomeStatefulCloneObjectExtension implements
		ICloneObjectExtensionMultiStatefulObject, ISomeMultiStatefulCloneObjectExtension
{
	private static final long serialVersionUID = 1L;

	private TreeSet<SomeDataElement> elements;

	public SomeMultiStatefulCloneObjectExtension()
	{
		elements = new TreeSet<SomeDataElement>();
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.test.data.ISomeMultiStatefulCloneObjectExtension#getDataElements()
	 */
	public List<SomeDataElement> getDataElements()
	{
		List<SomeDataElement> result = new ArrayList<SomeDataElement>(elements.size());

		for (SomeDataElement element : elements)
			if (!element.isDeleted())
				result.add(element);

		return result;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.test.data.ISomeMultiStatefulCloneObjectExtension#addDataElement(org.electrocodeogram.cpc.test.data.SomeDataElement)
	 */
	public void addDataElement(SomeDataElement data)
	{
		assert (data != null);

		//check if this is an update
		if (elements.contains(data))
			//ok, we want to replace this entry
			elements.remove(data);

		elements.add(data);

		setDirty(true);
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.test.data.ISomeMultiStatefulCloneObjectExtension#removeDataElement(org.electrocodeogram.cpc.test.data.SomeDataElement)
	 */
	public void removeDataElement(SomeDataElement data)
	{
		assert (data != null);

		/*
		 * We need to fetch the corresponding element from our internal elements list
		 * and mark it deleted. However, a TreeSet does not offer a get() method.
		 * We're using ceiling() here, it should do the same.
		 * 
		 * javadoc:
		 * 	Returns the least element in this set greater than or equal to the given element,
		 * 	or null if there is no such element. 
		 */
		SomeDataElement ourData = elements.ceiling(data);

		if (ourData == null || !ourData.equals(data))
			//the element data is not part of our elements list, ignore this deletion request
			return;

		//mark our copy as deleted.
		ourData.delete();

		setDirty(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.test.data.SomeStatefulCloneObjectExtension#getExtensionInterfaceClass()
	 */
	@Override
	public Class<? extends ICloneObjectExtension> getExtensionInterfaceClass()
	{
		return ISomeMultiStatefulCloneObjectExtension.class;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.test.data.SomeStatefulCloneObjectExtension#getPersistenceClassIdentifier()
	 */
	@Override
	public String getPersistenceClassIdentifier()
	{
		return ISomeMultiStatefulCloneObjectExtension.PERSISTENCE_CLASS_IDENTIFIER;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionMultiStatefulObject#getMultiPersistenceClassIdentifier()
	 */
	@Override
	public List<String> getMultiPersistenceClassIdentifier()
	{
		return Arrays.asList("elems");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionMultiStatefulObject#getMultiPersistenceObjectIdentifier()
	 */
	@Override
	public List<String> getMultiPersistenceObjectIdentifier()
	{
		return Arrays.asList("id");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionMultiStatefulObject#getMultiState()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<List<Map<String, Comparable<? extends Object>>>> getMultiState()
	{
		List<Map<String, Comparable<? extends Object>>> states = new ArrayList<Map<String, Comparable<? extends Object>>>(
				elements.size());
		String parentUuid = getParentUuid();

		for (SomeDataElement elem : elements)
		{
			states.add(elem.getState(parentUuid));
		}

		return Arrays.asList(states);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionMultiStatefulObject#getMultiStateTypes()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Class<? extends Object>>> getMultiStateTypes()
	{
		return Arrays.asList((new SomeDataElement()).getStateTypes());
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionMultiStatefulObject#purgeDeletedEntries()
	 */
	@Override
	public void purgeDeletedEntries()
	{
		Iterator<SomeDataElement> iter = elements.iterator();
		while (iter.hasNext())
		{
			SomeDataElement elem = iter.next();
			if (elem.isDeleted())
				iter.remove();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionMultiStatefulObject#setMultiState(java.util.List)
	 */
	@Override
	public void setMultiState(List<List<Map<String, Comparable<? extends Object>>>> states)
	{
		assert (states != null && states.size() == 1);

		elements.clear();

		for (Map<String, Comparable<? extends Object>> state : states.get(0))
		{
			SomeDataElement elem = new SomeDataElement();
			elem.setState(state);
			elements.add(elem);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.test.data.SomeStatefulCloneObjectExtension#toString()
	 */
	@Override
	public String toString()
	{
		return getSealStatus() + "SomeMultiStatefulCloneObjectExtension[data: " + getData() + ", parentUuid: "
				+ getParentUuid() + ", elements: " + elements + "]";
	}
}
