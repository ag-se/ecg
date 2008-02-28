package org.electrocodeogram.cpc.core.api.data.special;


import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneObject;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;


/**
 * A special version of the {@link IStatefulObject} interface which needs to be implemented by
 * all {@link ICloneObjectExtension} objects which require persistence.
 * <p>
 * If you need to persist extension objects which contain lists of arbitrary length or complex content, please refer
 * to the {@link ICloneObjectExtensionMultiStatefulObject} API.
 * <p>
 * There are a number of important differences between the normal {@link IStatefulObject} handling
 * for {@link ICloneObject}s & co and the handling for {@link ICloneObjectExtension}s.
 * <br>
 * <ul>
 * 	<li>The {@link IStatefulObject#getPersistenceObjectIdentifier()} <b>must</b> be "<em>parent_uuid</em>".</li>
 * 	<li>The value for the key "<em>parent_uuid</em>" as returned by {@link IStatefulObject#getState()}
 * 		<b>must</b> correspond to the <em>parent UUID</em> as set via {@link ICloneObjectExtension#setParentUuid(String)}.</li>
 * 	<li>There can only be one {@link ICloneObjectExtensionStatefulObject} object per {@link ICloneObject}, the
 * 		<em>parent UUID</em> is thus a unique identifier for any extension instance of a given type.</li>
 *  <li>An {@link ICloneObjectExtension} is limited to exactly one, pre-specified {@link ICloneObject} type.
 *  	The <em>PERSISTENCE_CLASS_IDENTIFIER</em> of that type needs to be returned by
 *  	{@link ICloneObjectExtensionStatefulObject#getPersistenceParentClassIdentifier()} method.</li>
 * </ul>
 * <br>
 * <b>NOTE:</b> In the current implementation stateful extensions are only meaningful for {@link IClone} objects.
 * 
 * @author vw
 * 
 * @see ICloneObjectExtensionMultiStatefulObject
 * @see IStatefulObject
 * @see ICloneObjectExtension
 */
public interface ICloneObjectExtensionStatefulObject extends IStatefulObject, ICloneObjectExtension
{
	/**
	 * @see IStatefulObject#getPersistenceClassIdentifier()
	 */
	public static final String PERSISTENCE_OBJECT_IDENTIFIER = "parent_uuid";

	/**
	 * Each {@link ICloneObjectExtensionStatefulObject} implementation has to be linked to one specific
	 * {@link ICloneObject} type.
	 * <br>
	 * This method must return the <em>PERSISTENCE_CLASS_IDENTIFIER</em> value of that class.
	 * <br>
	 * I.e. {@link IClone#PERSISTENCE_CLASS_IDENTIFIER}.
	 * 
	 * @return the {@link IStatefulObject#getPersistenceClassIdentifier()} of the parent entity type, never null.
	 */
	public String getPersistenceParentClassIdentifier();

	/**
	 * Checks whether this {@link ICloneObjectExtensionStatefulObject} instance was modified in a way
	 * which affected the persistent part of its data.
	 * <br>
	 * This should be true if and only if a modification has taken place which might have resulted in
	 * a change of the {@link IStatefulObject#getState()} return value.
	 * <p>
	 * For {@link ICloneObjectExtensionMultiStatefulObject} implementations, true should also be returned
	 * if a modification might have changed the return value of
	 * {@link ICloneObjectExtensionMultiStatefulObject#getMultiState()}.
	 * 
	 * @return true if this instance was modified in any way which needs to be persisted, false otherwise.
	 */
	public boolean isDirty();

	/**
	 * Called by the {@link IStoreProvider} (with value <em>false</em>) after this extension was successfully persisted.
	 * <p>
	 * <b>IMPORTANT:</b> this method may only be called internally or by the {@link IStoreProvider}.
	 *
	 * @param dirty true if this entry is out of sync and needs to be persisted, false otherwise.
	 */
	public void setDirty(boolean dirty);
}
