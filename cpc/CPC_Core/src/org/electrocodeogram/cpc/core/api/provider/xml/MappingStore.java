package org.electrocodeogram.cpc.core.api.provider.xml;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.special.IStatefulObject;


/**
 * Simple input and output wrapper for {@link IMappingProvider}s.
 * 
 * @author vw
 * 
 * @see IMappingProvider
 */
public class MappingStore
{
	private static final Log log = LogFactory.getLog(MappingStore.class);

	protected IStatefulObject statefulParentObject = null;
	protected List<IStatefulObject> statefulChildObjects = null;

	/**
	 * For use by sub classes only.
	 */
	protected MappingStore()
	{

	}

	/**
	 * Creates a new {@link MappingStore} instance.
	 * 
	 * @param statefulObject a {@link IStatefulObject}, never null.
	 */
	public MappingStore(IStatefulObject statefulObject)
	{
		assert (statefulObject != null);

		this.statefulChildObjects = Arrays.asList(statefulObject);
	}

	/**
	 * Creates a new {@link MappingStore} instance.
	 * 
	 * @param statefulObjects a list of {@link IStatefulObject}s, may be empty, never null.
	 */
	public MappingStore(List<IStatefulObject> statefulObjects)
	{
		assert (statefulObjects != null);

		this.statefulChildObjects = statefulObjects;
	}

	/**
	 * Creates a new {@link MappingStore} instance.
	 * 
	 * @param statefulParentObject the parent {@link IStatefulObject} which all given child objects should
	 * 		be located under, never null. This is typically an {@link ICloneFile} instance.
	 * @param statefulChildObjects a list of {@link IStatefulObject}s which should be located within the
	 * 		given parent object, may be empty, never null. This is typically a list of {@link IClone} instances.
	 */
	public MappingStore(IStatefulObject statefulParentObject, List<IStatefulObject> statefulChildObjects)
	{
		assert (statefulParentObject != null && statefulChildObjects != null);

		this.statefulParentObject = statefulParentObject;
		this.statefulChildObjects = statefulChildObjects;
	}

	/**
	 * Creates a new {@link MappingStore} instance.
	 * <br>
	 * Convenience method.
	 * 
	 * @param cloneFile
	 * @param clones
	 */
	@SuppressWarnings("unchecked")
	public MappingStore(ICloneFile cloneFile, List<IClone> clones)
	{
		this((IStatefulObject) cloneFile, (List) clones);
	}

	/**
	 * An additional parent {@link IStatefulObject} for the given child objects.<br/>
	 * This value may be NULL.
	 * 
	 * @return the parent {@link IStatefulObject}, may be NULL.
	 */
	public IStatefulObject getStatefulParentObject()
	{
		return statefulParentObject;
	}

	/**
	 * Retrieves the {@link ICloneFile} which was stored as parent object in this
	 * mapping store. This may fail if this mapping store has no designated parent object
	 * or if it has a different type.
	 * <br>
	 * Convenience method.
	 * 
	 * @return an {@link ICloneFile} instance if the parent object exists, NULL otherwise.
	 * 
	 * @throws MappingException if the parent object does not have type {@link ICloneFile}.
	 * 
	 * @see MappingStore#getStatefulParentObject()
	 */
	public ICloneFile getCloneFile() throws MappingException
	{
		if (statefulParentObject == null)
			return null;

		if (statefulParentObject instanceof ICloneFile)
			return (ICloneFile) statefulParentObject;

		//ok, this is bad, we don't have a clone file object!
		log.warn(
				"getCloneFile() - parent object of parsed cpc data does is not an ICloneFile instance - statefulParentObject: "
						+ statefulParentObject, new Throwable());

		throw new MappingException(
				"parent object of parsed cpc data does is not an ICloneFile instance - statefulParentObject: "
						+ statefulParentObject);
	}

	/**
	 * A list of {@link IStatefulObject}s which should be mapped to/where mapped from a string representation.
	 * <br>
	 * The list and its elements may not be modified.
	 * 
	 * @return a list of {@link IStatefulObject}s, may be empty, never null.
	 */
	public List<IStatefulObject> getStatefulChildObjects()
	{
		assert (statefulChildObjects != null);
		return statefulChildObjects;
	}

	/**
	 * Retrieves the child objects as a list of {@link IClone} instances. This may fail
	 * if this store does other types of classes.
	 * <br>
	 * Convenience method.
	 * 
	 * @return a list of {@link IClone}s, may be empty, never null. 
	 * 
	 * @throws MappingException if any of the {@link MappingStore#getStatefulChildObjects()} is not of type
	 * 		{@link IClone}.
	 * 
	 * @see MappingStore#getStatefulChildObjects()
	 */
	public List<IClone> getClones() throws MappingException
	{
		assert (statefulChildObjects != null);

		List<IClone> clones = new ArrayList<IClone>(statefulChildObjects.size());

		for (IStatefulObject statefulObject : statefulChildObjects)
		{
			if (statefulObject == null || !(statefulObject instanceof IClone))
			{
				log.warn("getClones() - parsed cpc data contains a non-IClone child object - statefulObject: "
						+ statefulObject, new Throwable());
				throw new MappingException("parsed cpc data contains a non-IClone child object - statefulObject: "
						+ statefulObject);
			}

			clones.add((IClone) statefulObject);
		}

		return clones;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "MappingStore[statefulParentObject: " + statefulParentObject + ", statefulChildObjects: "
				+ statefulChildObjects + "]";
	}
}
