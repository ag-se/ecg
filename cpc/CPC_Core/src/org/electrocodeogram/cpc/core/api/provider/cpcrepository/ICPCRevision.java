package org.electrocodeogram.cpc.core.api.provider.cpcrepository;


import java.util.List;

import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;


/**
 * A {@link ICPCRevision} is a simple wrapper object for remotely stored clone data packages.
 * <br> 
 * It contains:
 * <ul>
 * 	<li>a revision id string</li>
 *  <li>an {@link ICloneFile} instance</li>
 *  <li>a list of {@link IClone} instances for the file</li>
 * </ul>
 * {@link ICPCRevision} instances are used in combination with {@link ICPCRepositoryProvider}
 * operations.
 * <br>
 * A new instance for this interface can be obtained via:
 * {@link ICPCRepositoryProvider#createRevision()}
 * 
 * @author vw
 * 
 * @see ICPCRepositoryProvider
 */
public interface ICPCRevision
{

	/**
	 * Retrieves the revision identifier string for this cpc revision.
	 * <br>
	 * I.e. a revision identifier as it is assigned to source files from the main
	 * source repository provider.
	 * 
	 * @return revision identifier, never null.
	 */
	public String getRevisionId();

	/**
	 * Sets the revision identifier string for this cpc revision.
	 * 
	 * @param revisionId revision identifier, never null.
	 * 
	 * @see ICPCRevision#getRevisionId()
	 */
	public void setRevisionId(String revisionId);

	/**
	 * Retrieves the {@link ICloneFile} instance for this cpc revision.
	 * 
	 * @return an {@link ICloneFile} instance, never null.
	 */
	public ICloneFile getCloneFile();

	/**
	 * Sets the {@link ICloneFile} instance for this cpc revision.
	 * 
	 * @param cloneFile an {@link ICloneFile} instance, never null.
	 * 
	 * @see ICPCRevision#getCloneFile()
	 */
	public void setCloneFile(ICloneFile cloneFile);

	/**
	 * Retries a list of {@link IClone} instances which are part of this revision.
	 * <br>
	 * They are all located with the {@link ICPCRevision#getCloneFile()} file.
	 * 
	 * @return a list of {@link IClone} for this file, never null.
	 */
	public List<IClone> getClones();

	/**
	 * Specifies a list of {@link IClone} instances which are part of this revision.
	 * 
	 * @param clones a list of {@link IClone} for this file, never null.
	 * 
	 * @see ICPCRevision#getClones()
	 */
	public void setClones(List<IClone> clones);

	/*
	 * Misc
	 */

	/**
	 * Checks whether all required fields for this element have been set.
	 * 
	 * @return true if all required fields have been set, false otherwise.
	 */
	public boolean isValid();

	/**
	 * All implementations should provide a meaningful toString() method for debugging purposes. 
	 */
	public String toString();

}
