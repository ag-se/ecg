package org.electrocodeogram.cpc.store.local.xml.provider;


import java.util.LinkedList;

import org.electrocodeogram.cpc.core.api.data.special.IStatefulObject;
import org.electrocodeogram.cpc.core.api.provider.xml.MappingStore;


/**
 * Special version of {@link MappingStore} which can be incrementally filled with data.
 * 
 * @author vw
 */
class IncrementalXMLMappingStore extends MappingStore
{
	IncrementalXMLMappingStore()
	{
		statefulChildObjects = new LinkedList<IStatefulObject>();
	}

	void setStatefulParentObject(IStatefulObject statefulParentObject)
	{
		this.statefulParentObject = statefulParentObject;
	}

	void addStatefulChildObject(IStatefulObject statefulChildObject)
	{
		this.statefulChildObjects.add(statefulChildObject);
	}
}
