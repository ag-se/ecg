package org.electrocodeogram.cpc.core.api.data.special;


import java.util.Date;

import org.electrocodeogram.cpc.core.api.data.IClone;


/**
 * Internal sub-interface of {@link IClone} containing internal methods which are related
 * to the creation of new clone objects as well as the modification of a clone contents.
 * <p>
 * This interface may only be used by modules which create or modify clone objects.
 * <p>
 * Users:
 * <ul>
 * 	<li>CPC Track</li>
 * 	<li>CPC Imports</li>
 * </ul> 
 * 
 * @author vw
 * 
 * @see IClone
 */
public interface ICreatorClone extends IClone
{
	/**
	 * Sets the creation date of this clone.
	 * <br>
	 * This method will also set the {@link IClone#getModificationDate()} and
	 * {@link IClone#getCloneStateChangeDate()} values to the given date.
	 * 
	 * @param creationDate the creation date, never null.
	 * 
	 * @see IClone#getModificationDate()
	 * @see IClone#getCloneStateChangeDate()
	 */
	public void setCreationDate(Date creationDate);

	/**
	 * Sets the creator (username) of this clone.
	 * <br>
	 * If the creator can't be determined, the value should be set to NULL.
	 * 
	 * @param creator the creator of this clone, may be NULL.
	 */
	public void setCreator(String creator);

	/**
	 * Sets the UUID for the clone file in which this clone is located.
	 * 
	 * @param fileUuid clone file uuid, never null.
	 */
	public void setFileUuid(String fileUuid);

	/**
	 * Sets the current content for the position range specified by the clone.
	 * <br>
	 * This method is called on creation of a new clone and after each modification
	 * to the content of a clone.
	 * <br>
	 * The first call of this method will also set the {@link IClone#getOriginalContent()} value.
	 * <br>
	 * This method also sets the {@link IClone#getModificationDate()} value to the current time.
	 * 
	 * @param content the new clone content, never null.
	 * 
	 * @see IClone#getModificationDate()
	 */
	public void setContent(String content);

}
