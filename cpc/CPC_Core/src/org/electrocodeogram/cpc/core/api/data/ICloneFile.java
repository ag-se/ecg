package org.electrocodeogram.cpc.core.api.data;


import org.electrocodeogram.cpc.core.api.data.collection.ICloneFileInterfaces;
import org.electrocodeogram.cpc.core.api.data.special.IStatefulObject;


/**
 * Public interface for all clone file data objects.
 * <p>
 * <b>Any implementation needs to implement {@link ICloneFileInterfaces}.
 * Implementing only {@link ICloneFile} is not enough!</b>
 * <p>
 * This interface lists all methods which are available to all CPC plugins and 3rd party
 * contributions.
 * <p>
 * Additional methods are defined by more specific sub-interfaces which
 * belong to individual CPC plugins and are to be considered private.<br/>
 * Any CPC plugin other than the one designated in the sub-interface API must not access
 * such methods.
 * 
 * @author vw
 * 
 * @see ICloneFileInterfaces
 */
public interface ICloneFile extends ICloneObject
{
	/**
	 * {@link IStatefulObject} persistence class identifier, value: "<em>clone_file</em>"
	 */
	public final String PERSISTENCE_CLASS_IDENTIFIER = "clone_file";

	/**
	 * Retrieves the project name of this file.
	 * 
	 * @return project which contains the resource, never null.
	 */
	public String getProject();

	/**
	 * Retrieves the project relative path of this file.
	 * 
	 * @return path of resource, relative to project directory. Contains the file name, but not the project name. Never null.
	 */
	public String getPath();

	/**
	 * Retrieves the file size at the point in time of the last save operation.
	 * 
	 * @return the file size of the corresponding file on disk in bytes.
	 */
	public long getSize();

	/**
	 * Retrieves the file modification timestamp at the point in time of the last save operation.
	 * <br>
	 * This value may not always be available.
	 * 
	 * @return the file modification timestamp of the corresponding file on disk in bytes.
	 */
	public long getModificationDate();

}
