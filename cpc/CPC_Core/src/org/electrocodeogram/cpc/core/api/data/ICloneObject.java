package org.electrocodeogram.cpc.core.api.data;


import java.io.Serializable;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.electrocodeogram.cpc.core.api.data.collection.ICloneObjectInterfaces;
import org.electrocodeogram.cpc.core.api.data.special.IStatefulObject;
import org.electrocodeogram.cpc.core.api.data.special.IStoreCloneObject;


/**
 * Base interface for all CPC Data Objects.
 * <p>
 * <b>Any implementation needs to implement {@link ICloneObjectInterfaces}.
 * Implementing only {@link ICloneObject} is not enough!</b>
 * <p>
 * This interface lists all methods which are available to all CPC plugins and 3rd party
 * contributions.
 * <p>
 * Additional methods are defined by more specific sub-interfaces
 * (in the <em>*.api.data.special</em> package) which belong to individual CPC plugins
 * and are to be considered private.
 * <br>
 * Any CPC plugin other than the one designated in the sub-interface API must not access
 * such methods.
 * <br>
 * This does not apply to the main sub-interfaces in the <em>*.api.data</em> package.
 * <p>
 * General Considerations valid for all types of CPC Data Objects:
 * <ul>
 * 	<li>All implementations must be <b>serializable</b>, <b>cloneable</b> and <b>adaptable</b>.</li>
 *  <li>All implementations must provide a zero argument constructor which auto generates a <em>uuid</em>
 *  as well as a constructor which takes one <em>String uuid</em> argument.</li>
 * </ul>
 * 
 * @author vw
 * 
 * @see ICloneObjectInterfaces
 * @see IAdaptable
 * @see Serializable
 * @see Cloneable
 */
public interface ICloneObject extends ICloneDataElement, IAdaptable, Serializable, Cloneable
{
	/**
	 * {@link IStatefulObject} persistence object identifier, value: "<em>uuid</em>"
	 */
	public final String PERSISTENCE_OBJECT_IDENTIFIER = "uuid";

	/**
	 * Retrieves the <em>uuid</em> which uniquely identifies this object.
	 * <p>
	 * This value has to be initialised during the construction of an object.
	 * 
	 * @return unique identifier of this object, never null.
	 */
	public String getUuid();

	/**
	 * Checks whether this clone object has been marked.
	 * <p>
	 * A typical use for marks is to pass info on some kind of selection of clone objects from
	 * a list between method calls without having to create an additional list or having to
	 * add an {@link ICloneObjectExtension}.
	 * <p>
	 * Marks are not persisted.
	 * <p>
	 * <b>NOTE:</b> Marks have no meaning outside of the module which set them. They exist
	 * 		only due to performance considerations. If you need to store some data for
	 * 		a clone object which will be handled by other modules, you should use
	 * 		{@link ICloneObject#getExtension(Class)} instead.
	 * 		<br>
	 * 		Whenever a clone object leaves your module, you should consider all marks to be lost/corrupted.
	 * 
	 * @return true if this instance has been marked.
	 */
	public boolean isMarked();

	/**
	 * Marks or unmarks a clone object.
	 * <p>
	 * Marks are not persisted.
	 * <p>
	 * Be sure to read the limitations for the usage of marks, see: {@link #isMarked()} 
	 * 
	 * @param marked whether this instance should be marked or not.
	 * 
	 * @see #isMarked()
	 */
	public void setMarked(boolean marked);

	/**
	 * Cached boolean value which indicates whether there is currently any {@link ICloneObjectExtension}
	 * added to this clone object.
	 * <br>
	 * The return value will be false if there is no
	 * non-deleted extension present. Deleted extensions are not taken into account.
	 * <br>
	 * This values is persisted as part of the {@link ICloneObject}.
	 * 
	 * @return true if there is at least one extension for this object.
	 */
	public boolean hasExtensions();

	/**
	 * Retrieves an {@link ICloneObjectExtension} which has been added to this clone object.
	 * If no extension of this type has been added, null is returned.
	 * <br>
	 * A deleted extension will not be returned.
	 * 
	 * @param extensionClass the extension type to retrieve, never null
	 * @return the extension in question or NULL if no such extension has been added
	 * 
	 * @see ICloneObjectExtension
	 */
	public ICloneObjectExtension getExtension(Class<? extends ICloneObjectExtension> extensionClass);

	/**
	 * Retrieves a list of all currently added {@link ICloneObjectExtension}s for this clone object
	 * which have not been deleted.
	 * 
	 * @return list of non-deleted <em>ICloneObjectExtension</em>s, never null.
	 * 
	 * @see ICloneObjectExtension
	 */
	public List<ICloneObjectExtension> getExtensions();

	/**
	 * Adds an {@link ICloneObjectExtension} to this clone object.
	 * <br>
	 * The {@link ICloneObjectExtension} will be registered under the class returned by its
	 * {@link ICloneObjectExtension#getExtensionInterfaceClass()} method.
	 * <p>
	 * There can <u>only be one extension of a given type at any time</u>. Adding an extension
	 * while another extension of the same type is already registered, will <b>replace</b> the
	 * existing extension.
	 * <p>
	 * <b>NOTE:</b> in the current implementation stateful extensions will only be persisted for {@link IClone} objects.
	 * 
	 * @param extension the extension to add, will replace any existing extension of the same type, never null.
	 * 
	 * @see ICloneObjectExtension
	 */
	public void addExtension(ICloneObjectExtension extension);

	/**
	 * Removes any {@link ICloneObjectExtension} of the given type from this {@link ICloneObject}.
	 * <br>
	 * The extension is added to the internal deletion registry.
	 *  
	 * @param extensionClass the interface type for which a registered extension should be removed, never null.
	 * 
	 * @see IStoreCloneObject#getDeletedExtensions()
	 * @see IStoreCloneObject#purgeDeletedExtensions()
	 */
	public void removeExtension(Class<? extends ICloneObjectExtension> extensionClass);

	/**
	 * Removes <b>any</b> {@link ICloneObjectExtension} which matches the {@link ICloneObjectExtension#getExtensionInterfaceClass()}
	 * value of the given extension from this {@link ICloneObject}.
	 * <br>
	 * Convenience method.
	 * <br>
	 * This is a short hand for {@link #removeExtension(Class)}. 
	 * 
	 * @param extension the {@link ICloneObjectExtension} for which any extension of equal type should be removed, never null.
	 * 
	 * @see #removeExtension(Class)
	 */
	public void removeExtension(ICloneObjectExtension extension);

	/**
	 * Equality based on uuid.
	 */
	public boolean equals(Object obj);

	/**
	 * HashCode based on uuid.
	 */
	public int hashCode();

	/**
	 * Checks not only the uuid but ALL data fields for equality.
	 * 
	 * @param otherCloneObject clone object to compare to, may be null, may be same instance
	 * @return true if all fields are equal, false otherwise or if otherCloneObject is null.
	 */
	public boolean equalsAll(ICloneObject otherCloneObject);

	/**
	 * All implementations must be cloneable.
	 */
	public Object clone() throws CloneNotSupportedException;

	/**
	 * All implementations should provide a meaningful toString() method for debugging purposes. 
	 */
	public String toString();

}
