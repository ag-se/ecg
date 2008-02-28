package org.electrocodeogram.cpc.core.api.data;


/**
 * Root interface of all CPC data objects.
 * <p>
 * Each CPC data object is either one of these types:
 * <ul>
 * 	<li>{@link ICloneObject}</li>
 *  <li>{@link ICloneObjectSupport}</li>
 *  <li>{@link ICloneObjectExtension}</li>
 * </ul>
 * 
 * @author vw
 * 
 * @see ICloneObject
 * @see ICloneObjectSupport
 * @see ICloneObjectExtension
 */
public interface ICloneDataElement
{
	/**
	 * Seals this {@link ICloneDataElement} instance.
	 * <br>
	 * A sealed object may not be modified in any way. Otherwise an IllegalStateException is thrown.
	 * <br>
	 * Sealing an already sealed object has no effect.
	 * <p>
	 * In order to "unseal" an {@link ICloneDataElement}, it needs to be cloned. The seal state will not
	 * be propagated to the cloned instance.
	 */
	public void seal();

	/**
	 * Checks whether this {@link ICloneDataElement} instance has been sealed.
	 * 
	 * @return true if this instance has been sealed.
	 */
	public boolean isSealed();
}
