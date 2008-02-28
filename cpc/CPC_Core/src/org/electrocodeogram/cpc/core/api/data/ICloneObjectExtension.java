package org.electrocodeogram.cpc.core.api.data;


import java.io.Serializable;
import java.util.Map;

import org.electrocodeogram.cpc.core.api.data.collection.ICloneObjectExtensionInterfaces;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionLazyMultiStatefulObject;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;


/**
 * Clone object extensions can be used by 3rd party modules to contribute their own data to any
 * {@link ICloneObject}. Once added, such extensions will be persisted and synchronised by CPC automatically.
 * <p>
 * <b>Any implementation needs to implement {@link ICloneObjectExtensionInterfaces}.
 * Implementing only {@link ICloneObjectExtension} is not enough!</b>
 * <p>
 * This interface lists all methods which are available to all CPC plugins and 3rd party
 * contributions.
 * <p>
 * Additional methods are defined by more specific sub-interfaces which
 * belong to individual CPC plugins and are to be considered private.
 * <br>
 * Any CPC plugin other than the one designated in the sub-interface API must not access
 * such methods.
 * <p>
 * General Considerations valid for all {@link ICloneObjectExtension} implementations:
 * <ul>
 * 	<li>All implementations must be <b>serializable</b> and <b>cloneable</b>.</li>
 *  <li>All implementations must be <b>registered with the {@link ICloneFactoryProvider}</b>'s corresponding extension point.</li>
 *  <li>An {@link ICloneObjectExtension} implementation is only valid for <b>one</b> {@link ICloneObject} type. The type
 *  	needs to be specified during registration with the {@link ICloneFactoryProvider}. The same class may not be
 *  	registered multiple times.</li>
 * </ul>
 * 
 * @author vw
 *
 * @see ICloneObjectExtensionInterfaces
 * @see ICloneFactoryProvider
 * @see ICloneObject
 * @see ICloneDataElement
 * @see Cloneable
 * @see Serializable
 */
public interface ICloneObjectExtension extends ICloneDataElement, Cloneable, Serializable
{
	/*
	 * It is up to the implementation to decide whether overriding equals() and hashCode() is required.
	 * However in most cases this is probably the case.
	 */

	/**
	 * Returns the {@link ICloneObjectExtension} sub-interface which this class is implementing.
	 * <br>
	 * This value has to match the interface for which the implementation was registered with the
	 * {@link ICloneFactoryProvider}.
	 * <p>
	 * I.e. if <em>CloneModificationHistoryExtensionImpl</em> implements {@link ICloneModificationHistoryExtension},
	 * it would return <em>ICloneModificationHistoryExtension.class</em> here.
	 * <p>
	 * This information could be obtained via reflection. However, as the interface class is potentially used
	 * as a {@link Map} key internally, the value needs to match exactly. Using reflection under these conditions
	 * would be error prone.
	 * 
	 * @return the interface class which this extension is implementing, never null.
	 */
	public Class<? extends ICloneObjectExtension> getExtensionInterfaceClass();

	/**
	 * This method will automatically be called, whenever an {@link ICloneObjectExtension} is
	 * added to an {@link ICloneObject} via {@link ICloneObject#addExtension(ICloneObjectExtension)}.
	 * <br>
	 * A {@link ICloneObjectExtension} may only belong to one {@link ICloneObject} instance at a time and may not
	 * be reused.
	 * <br>
	 * Should this method be called multiple times with different <em>parentUuids</em> an {@link IllegalArgumentException}
	 * is thrown.
	 * <p>
	 * <b>IMPORTANT:</b> this method may only be called by the {@link ICloneObject} implementation.
	 * 
	 * @param parentUuid the UUID of the parent {@link ICloneObject} of this extension object, never null.
	 * 
	 * @throws IllegalArgumentException if a client ties to change the <em>parentUuid</em> of this object.
	 */
	public void setParentUuid(String parentUuid);

	/**
	 * Checks whether this {@link ICloneObjectExtension} object was fully restored from persistent storage.
	 * <br>
	 * A true value indicates that there <u>may be</u> additional sub-element data available in persistent storage
	 * which was not loaded when this element was created.
	 * <p>
	 * For all extensions which do not implement {@link ICloneObjectExtensionLazyMultiStatefulObject} this method
	 * always returns <em>false</em>.
	 * <p>
	 * A newly created extensions (which are thus not yet persisted) which implement
	 * {@link ICloneObjectExtensionLazyMultiStatefulObject} should return <em>true</em> until
	 * {@link ICloneObjectExtensionLazyMultiStatefulObject#setPartial(boolean)} is used to
	 * set a new value.
	 * 
	 * @return true if this extension object may not have been fully restored, false otherwise.
	 * 
	 * @see ICloneObjectExtensionLazyMultiStatefulObject#setPartial(boolean)
	 */
	public boolean isPartial();

	/**
	 * All implementations must be cloneable.
	 */
	public Object clone() throws CloneNotSupportedException;

	/**
	 * All implementations should provide a meaningful toString() method for debugging purposes. 
	 */
	public String toString();

}
