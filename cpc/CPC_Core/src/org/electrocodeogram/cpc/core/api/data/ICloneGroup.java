package org.electrocodeogram.cpc.core.api.data;


import org.electrocodeogram.cpc.core.api.data.collection.ICloneGroupInterfaces;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionStatefulObject;
import org.electrocodeogram.cpc.core.api.data.special.IStatefulObject;


/**
 * Public interface for all clone group data objects.
 * <p>
 * <b>Any implementation needs to implement {@link ICloneGroupInterfaces}.
 * Implementing only {@link ICloneGroup} is not enough!</b>
 * <p>
 * This interface lists all methods which are available to all CPC plugins and 3rd party
 * contributions.
 * <p>
 * Additional methods are defined by more specific sub-interfaces which
 * belong to individual CPC plugins and are to be considered private.<br/>
 * Any CPC plugin other than the one designated in the sub-interface API must not access
 * such methods.
 * <p>
 * <b>IMPORTANT NOTE</b>
 * <blockquote>
 * In order to ease the implementation of file based local storage providers as well as
 * repository based remote store providers clone group objects are <b>not persisted individually</b>
 * (clone group uuids are stored together with each clone member, nothing else is persisted).<br/>
 * An clone group object only contains it's uuid as a unique identifier and cached/derived counters
 * and statistics.
 * <br>
 * A custom clone group implementation <b>must not</b> expect any data fields to be persisted.<br/>
 * This also means that {@link ICloneObjectExtensionStatefulObject}s will <b>not</b> be persisted.
 * </blockquote>
 * 
 * @author vw
 * 
 * @see ICloneGroupInterfaces
 */
public interface ICloneGroup extends ICloneObject
{
	/**
	 * {@link IStatefulObject} persistence class identifier, value: "<em>clone_group</em>"
	 */
	public final String PERSISTENCE_CLASS_IDENTIFIER = "clone_group";

	/*
	 * An ICloneGroup object does not provide any additional data elements.
	 */
}
