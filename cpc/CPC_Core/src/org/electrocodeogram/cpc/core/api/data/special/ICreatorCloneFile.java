package org.electrocodeogram.cpc.core.api.data.special;


import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;


/**
 * Internal sub-interface of {@link ICloneFile} containing internal methods which are related
 * to the creation of new clone file objects as well as any kind of modification.
 * <p>
 * This interface may only be used by modules which create or modify clone file objects.
 * <p>
 * Users:
 * <ul>
 * 	<li>CPC Store</li>
 * 	<li>CPC Store Remote?</li>
 * </ul> 
 * 
 * @author vw
 */
public interface ICreatorCloneFile extends ICloneFile
{
	/**
	 * Set by {@link IStoreProvider} at {@link ICloneFile} creation time and on file moves. 
	 * 
	 * @param project the project name, never null.
	 */
	public void setProject(String project);

	/**
	 * Set by {@link IStoreProvider} at {@link ICloneFile} creation time and on file moves.
	 * 
	 * @param path the project relative path of the file, never null. 
	 */
	public void setPath(String path);

	/**
	 * Set by {@link IStoreProvider} during a call to {@link IStoreProvider#persistData(ICloneFile)}.
	 * 
	 * @param size the file size in bytes.
	 */
	public void setSize(long size);

	/**
	 * Set by {@link IStoreProvider} during a call to {@link IStoreProvider#persistData(ICloneFile)}.
	 * 
	 * @param modificationDate the last modification date of the file.
	 */
	public void setModificationDate(long modificationDate);
}
