package org.electrocodeogram.cpc.store.local.sql.data;


import org.electrocodeogram.cpc.core.api.data.ICloneObjectSupport;
import org.electrocodeogram.cpc.core.api.data.special.IStatefulObject;


public interface ICloneFileContent extends IStatefulObject, ICloneObjectSupport
{
	public static final String PERSISTENCE_CLASS_IDENTIFIER = "_clone_file_content";
	public static final String PERSISTENCE_OBJECT_IDENTIFIER = "fileUuid";

	public String getFileUuid();

	public void setFileUuid(String fileUuid);

	public String getContent();

	public void setContent(String content);

}
