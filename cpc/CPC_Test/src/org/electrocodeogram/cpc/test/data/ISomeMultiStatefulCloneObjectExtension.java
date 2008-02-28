package org.electrocodeogram.cpc.test.data;


import java.util.List;

import org.electrocodeogram.cpc.core.api.data.collection.ICloneObjectExtensionInterfaces;


public interface ISomeMultiStatefulCloneObjectExtension extends ISomeStatefulCloneObjectExtension,
		ICloneObjectExtensionInterfaces
{

	public static final String PERSISTENCE_CLASS_IDENTIFIER = "some_multistateful_extension";

	public List<SomeDataElement> getDataElements();

	public void addDataElement(SomeDataElement data);

	public void removeDataElement(SomeDataElement data);

}
