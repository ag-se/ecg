package org.electrocodeogram.cpc.core.api.data.special;


import java.util.List;

import org.electrocodeogram.cpc.core.api.data.ICloneObject;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;


/**
 * Extension interface for {@link ICloneObject} which contains additional internal
 * methods for use only by an {@link IStoreProvider}.
 * <p>
 * All {@link ICloneObject} implementations have to implement this interface.
 * <p>
 * Rationale:
 * <blockquote>
 * These methods should not be accessed by other plugins besides the {@link IStoreProvider}.
 * They are therefore "hidden" by this extra interface. The fact that an {@link ICloneObject} object
 * will need to be cast to this interface before any of the methods can be accessed is meant
 * to work as a deterrent for accidental access to these methods.
 * <br>
 * The {@link ICloneObjectExtension} mechanism is not used by most CPC plugins for performance reasons.
 * </blockquote>
 * 
 * @author vw
 * 
 * @see IStoreProvider
 * @see ICloneObject
 */
public interface IStoreCloneObject extends ICloneObject, IStatefulObject
{
	/**
	 * Whether this clone was modified and will need to be written to persistent storage.
	 * 
	 * @return true if this instance was modified
	 */
	public boolean isDirty();

	/**
	 * Sets this clone objects dirty flag.
	 * 
	 * @see #isDirty()
	 */
	public void setDirty(boolean dirty);

	/**
	 * Whether this clone was already stored in persistent storage at some point.
	 * <br>
	 * This does not mean that it can't be dirty dirty.
	 * <br>
	 * This is interesting for storage provider implementations which require different actions for
	 * addition and update of data (i.e. SQL INSERT and UPDATE).
	 * 
	 * @return true if this instance was persisted in the past
	 */
	public boolean isPersisted();

	/**
	 * Sets this clone objects persisted flag.
	 * 
	 * @see #isPersisted()
	 */
	public void setPersisted(boolean persisted);

	/**
	 * Retrieves a list of deleted {@link ICloneObjectExtension}s for this clone object.
	 * 
	 * @return list of deleted <em>ICloneObjectExtension</em>s, never null.
	 * 
	 * @see #purgeDeletedExtensions()
	 * @see ICloneObject#removeExtension(Class)
	 * @see ICloneObject#removeExtension(ICloneObjectExtension)
	 */
	public List<ICloneObjectExtension> getDeletedExtensions();

	/**
	 * Purges all currently deleted extensions from the {@link ICloneObject}.
	 * <br>
	 * After this method was called, the return value of {@link #getDeletedExtensions()}
	 * will be an empty list.
	 * <p>
	 * A store provider will typically call this method each time an {@link ICloneObject} was persisted
	 * successfully (and there is thus no need to retain any information about deleted extensions any longer).
	 * 
	 * @see #getDeletedExtensions()
	 */
	public void purgeDeletedExtensions();
}
