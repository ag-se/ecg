package org.electrocodeogram.cpc.core.api.data.special;


import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;


/**
 * Extension of the {@link ICloneObjectExtensionMultiStatefulObject} interface.
 * <br>
 * By implementing this interface, an {@link ICloneObjectExtension} indicates that the additional
 * sub-elements should not be automatically restored from the database during a normal lookup.
 * <p>
 * This is especially useful for {@link ICloneObjectExtension}s with a large number of sub-elements
 * which would use up considerable amounts of memory, if they were always loaded into memory by default.
 * <br>
 * One example is the {@link ICloneModificationHistoryExtension}.
 * <p>
 * The {@link IStoreProvider#getFullCloneObjectExtension(org.electrocodeogram.cpc.core.api.data.ICloneObject, Class)}
 * method can be used to retrieve all sub-element data for a lazy loaded extension object.
 * 
 * @author vw
 * 
 * @see ICloneObjectExtensionMultiStatefulObject
 * @see ICloneObjectExtensionStatefulObject
 * @see IStatefulObject
 * @see ICloneObjectExtension
 * @see ICloneModificationHistoryExtension
 * @see IStoreProvider#getFullCloneObjectExtension(org.electrocodeogram.cpc.core.api.data.ICloneObject, Class)
 */
public interface ICloneObjectExtensionLazyMultiStatefulObject extends ICloneObjectExtensionMultiStatefulObject
{
	/**
	 * Sets the partial state of this extension.
	 * <p>
	 * <b>IMPORTANT:</b> This method must only be used by the persistence provider.
	 * 
	 * @param partial true if there <b>might</b> be additional sub-element data in persistent storage, false otherwise.
	 * 
	 * @see ICloneObjectExtensionLazyMultiStatefulObject#isPartial()
	 */
	public void setPartial(boolean partial);
}
