package org.electrocodeogram.cpc.core.api.data.special;


import java.util.List;
import java.util.Map;

import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;


/**
 * Extension interface for {@link ICloneObjectExtensionStatefulObject} for
 * {@link ICloneObjectExtension} implementations.
 * <br>
 * This interface is meant used for {@link ICloneObjectExtension}s which need to persist a large,
 * fluctuating number of fields.
 * <br>
 * The {@link ICloneObjectExtensionStatefulObject} API requires you to map your internal state to a
 * single <code>Map<String,String></em> object with a predefined, unchanging number of values.
 * This does offer you any good way of persisting lists with complex content of arbitrary length.
 * <p>
 * If you just want to persist a simple object with a fixed number of fields, please refer to the
 * {@link ICloneObjectExtensionStatefulObject} API.
 * <p>
 * An object of this type is persisted/restored in two steps:
 * <ol>
 * 	<li>The regular part of the object is persisted/restored like normal {@link ICloneObjectExtensionStatefulObject} object.</li>
 *  <li>The all list parts of the object are persisted/restored.</li>
 * </ol>
 * There can be an arbitrary but fixed number of different sub-element types. The methods of this interface
 * always return lists or elements or of lists. Each entry in the top most list corresponds to one sub-element type.
 * <br/>
 * The order of sub-element types must be the same for all methods.
 * <p>
 * I.e. if you have an extension X which keeps a couple of fields as well as two lists, one holding elements of type A
 * the other of type B. To persist X you would implement {@link ICloneObjectExtensionMultiStatefulObject} and use the
 * normal {@link ICloneObjectExtensionStatefulObject} API for the persistence of all the fields of X, except the lists.
 * <br>
 * All methods of this API would then return/take a two element top most list, the first element corresponding to persistence
 * data for A and the second element corresponding to persistence data for B.
 * <br>
 * <ul>
 * 	<li>{@link ICloneObjectExtensionMultiStatefulObject#getMultiPersistenceClassIdentifier()} would yield a list, i.e.: {"classid_a", "classid_b"}.</li>
 *  <li>{@link ICloneObjectExtensionMultiStatefulObject#getMultiPersistenceObjectIdentifier()} would yield a list, i.e.: {"creationDate", "somefield"}.</li>
 *  <li>{@link ICloneObjectExtensionMultiStatefulObject#getMultiState()} would yield a list of lists. The top most list contains two lists, the first contains
 *  	states for all sub-elements of type A and the second list states for all sub-elements of type B.</li>
 *  <li>...</li>
 * </ul>
 * If you only have just one sub-element type, just return a list with a single entry, as top most list.
 * <br>
 * If you don't have any sub-element, you should look at {@link ICloneObjectExtensionStatefulObject} instead.
 * 
 * @author vw
 * 
 * @see ICloneObjectExtensionStatefulObject
 * @see IStatefulObject
 * @see ICloneObjectExtension
 */
public interface ICloneObjectExtensionMultiStatefulObject extends ICloneObjectExtensionStatefulObject
{
	/**
	 * The {@link ICloneObjectExtensionMultiStatefulObject#getMultiState()} key to use to indicate whether an
	 * {@link ICloneObjectExtensionMultiStatefulObject} sub-element was deleted.
	 * <br>
	 * The type for this key is Boolean.
	 * <br>
	 * The key is optional, if not present, the default value is <em>false</em>.
	 * <p>
	 * <b>IMPORTANT:</b> this key must not be used for anything else. It must also not be
	 * 		part of the {@link ICloneObjectExtensionMultiStatefulObject#getMultiStateTypes()} maps.
	 */
	public static final String DELETION_MARK_IDENTIFIER = "cpc_deleted";

	/**
	 * A list of <em>PERSISTENCE_CLASS_IDENTIFIER</em> for the sub-objects.
	 * 
	 * @return a list of <em>PERSISTENCE_CLASS_IDENTIFIER</em> for the sub-objects, never null.
	 * 
	 * @see IStatefulObject#getPersistenceClassIdentifier()
	 */
	public List<String> getMultiPersistenceClassIdentifier();

	/**
	 * A list of <em>PERSISTENCE_OBJECT_IDENTIFIER</em> for the sub-objects.
	 * <br>
	 * The corresponding key in the state map has to be a unique value.
	 * 
	 * @return a list of <em>PERSISTENCE_OBJECT_IDENTIFIER</em> for the sub-objects, never null.
	 * 
	 * @see IStatefulObject#getPersistenceObjectIdentifier()
	 */
	public List<String> getMultiPersistenceObjectIdentifier();

	/**
	 * Retrieves a list of lists of {@link IStatefulObject#getState()} mappings for each sub-element of this
	 * extension.
	 * <p>
	 * The elements of the topmost list must be <b>sorted</b> in ascending order according to the key returned by
	 * {@link ICloneObjectExtensionMultiStatefulObject#getMultiPersistenceObjectIdentifier()}.
	 * <p>
	 * The "<em>parent_uuid</em>" key needs to be present in each mapping.
	 * <p>
	 * The list also needs to contain deleted sub-elements. For those the <em>DELETION_MARK_IDENTIFIER</em>
	 * key in the state map should be set to <em>true</em>.
	 * 
	 * @return list of {@link IStatefulObject#getState()} mappings, never null.
	 * 
	 * @see IStatefulObject#getState()
	 */
	public List<List<Map<String, Comparable<? extends Object>>>> getMultiState();

	/**
	 * Sets the internal state of this object and all its sub-objects via the given
	 * list of lists of {@link IStatefulObject#setState(Map)} mappings.
	 * <p>
	 * If the parameter is NULL, all sub-element data for this extension object is cleared.
	 * <br>
	 * This typically happens when all clone data for a file is persisted.
	 * 
	 * @param states list of {@link IStatefulObject#setState(Map)} mappings, may be NULL.
	 * 
	 * @see IStatefulObject#setState(Map)
	 */
	public void setMultiState(List<List<Map<String, Comparable<? extends Object>>>> states);

	/**
	 * Retrieves the {@link IStatefulObject#getStateTypes()} mapping for the different sub-element types.
	 * <br>
	 * The "<em>parent_uuid</em>" key needs to be present in all mappings.
	 * 
	 * @return {@link IStatefulObject#getStateTypes()} mapping for sub-element type.
	 * 
	 * @see IStatefulObject#getStateTypes()
	 */
	public List<Map<String, Class<? extends Object>>> getMultiStateTypes();

	/**
	 * Indicates that all deleted sub-elements can be purged.
	 * <br>
	 * After this method was called, {@link ICloneObjectExtensionMultiStatefulObject#getMultiState()} should
	 * contain only non-deleted sub-entries.
	 * <p>
	 * This method is typically called by the store provider once this {@link ICloneObjectExtensionStatefulObject}
	 * was successfully persisted.
	 */
	public void purgeDeletedEntries();
}
