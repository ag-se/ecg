package org.electrocodeogram.cpc.core.api.data.extension;


import java.util.List;

import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.utils.CoreClonePositionUtils;


/**
 * A special non-whitespace position object for {@link IClone} objects.
 * <br>
 * Contains start and end offset with whitespaces ( ,\t,\n,\r) ignored.
 * <p>
 * This extension is not persisted.
 * <p>
 * Used by the {@link CoreClonePositionUtils} and the CPC Reconciler module.
 * 
 * @author vw
 * 
 * @see CoreClonePositionUtils#extractPositions(org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider, java.util.List, String)
 */
public interface ICloneNonWsPositionExtension extends ICloneObjectExtension
{
	/**
	 * Retrieves the non-whitespace start offset for the clone.
	 * <p>
	 * <b>IMPORTANT:</b> This value is not persisted and not automatically updated during
	 * 		document modifications.
	 * 		<br>
	 * 		If you need these values, you should update them yourself by calling:
	 * 		<br>
	 * 		{@link CoreClonePositionUtils#extractPositions(ICloneFactoryProvider, List, String)}
	 * 
	 * @return the character offset to the beginning of the file at which the clone begins
	 * 		   (whitespaces not included!). The character at this position is already part of the clone. First char is 0.
	 */
	public int getStartNonWsOffset();

	/**
	 * Sets this extensions non-whitespace start offset.
	 * 
	 * @see #getStartNonWsOffset()
	 */
	public void setStartNonWsOffset(int startNonWsOffset);

	/**
	 * Retrieves the non-whitespace end offset for this clone.
	 * <p>
	 * <b>IMPORTANT:</b> This value is not persisted and not automatically updated during
	 * 		document modifications.
	 * 		<br>
	 * 		If you need these values, you should update them yourself by calling:
	 * 		<br>
	 * 		{@link CoreClonePositionUtils#extractPositions(org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider, java.util.List, String)}
	 * 
	 * @return character offset to the beginning of the file, at which the clone ends
	 * 		(whitespaces not included!). The character at this position is still part of the clone. First char is 0.
	 */
	public int getEndNonWsOffset();

	/**
	 * Sets this extensions non-whitespace end offset.
	 * 
	 * @see #getEndNonWsOffset()
	 */
	public void setEndNonWsOffset(int endNonWsOffset);

}
