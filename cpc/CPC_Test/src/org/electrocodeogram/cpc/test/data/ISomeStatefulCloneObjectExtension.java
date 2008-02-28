package org.electrocodeogram.cpc.test.data;


import org.electrocodeogram.cpc.core.api.data.collection.ICloneObjectExtensionInterfaces;


public interface ISomeStatefulCloneObjectExtension extends ICloneObjectExtensionInterfaces
{

	public static final String PERSISTENCE_CLASS_IDENTIFIER = "some_stateful_extension";

	public String getData();

	public void setData(String data);

}
