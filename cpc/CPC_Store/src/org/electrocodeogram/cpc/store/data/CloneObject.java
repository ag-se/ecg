package org.electrocodeogram.cpc.store.data;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.Platform;
import org.electrocodeogram.cpc.core.api.data.ICloneObject;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.data.collection.ICloneObjectInterfaces;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionStatefulObject;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


public abstract class CloneObject extends AbstractCloneDataElement implements ICloneObjectInterfaces
{
	private static final Log log = LogFactory.getLog(CloneObject.class);

	private static final long serialVersionUID = 1L;
	private static final List<ICloneObjectExtension> EMPTY_EXTENSIONS_LIST = Collections
			.unmodifiableList(new ArrayList<ICloneObjectExtension>(0));

	protected String uuid;

	//whether this clone was modified and will need to be written to persistent storage
	private boolean dirty = true;

	//whether this clone was already stored in persistent storage at some point (this does not mean that it's not dirty)
	private boolean persisted = false;

	private boolean marked = false;

	//whether there are any extensions registered for this clone object
	private boolean hasExtensions = false;

	//registry for extension objects
	private Map<Class<? extends ICloneObjectExtension>, ICloneObjectExtension> extensions = null;
	private Map<Class<? extends ICloneObjectExtension>, ICloneObjectExtension> deletedExtensions = null;

	public CloneObject()
	{
		this.uuid = CoreUtils.generateUUID();

		if (log.isTraceEnabled())
			log.trace("CloneObject() - generated uuid: " + this.uuid);
	}

	public CloneObject(String uuid)
	{
		assert (uuid != null);

		this.uuid = uuid;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneObject#getUuid()
	 */
	@Override
	public String getUuid()
	{
		return uuid;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneObject#isMarked()
	 */
	@Override
	public boolean isMarked()
	{
		return marked;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneObject#setMarked(boolean)
	 */
	@Override
	public void setMarked(boolean marked)
	{
		if (log.isTraceEnabled())
			log.trace("setMarked() - marked: " + marked);

		checkSeal();

		this.marked = marked;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStoreCloneObject#isDirty()
	 */
	@Override
	public boolean isDirty()
	{
		//first check our own dirty state
		if (dirty)
			return true;

		//then check if any of our extensions is stateful and dirty
		if (extensions != null)
		{
			for (ICloneObjectExtension extension : extensions.values())
				if (extension instanceof ICloneObjectExtensionStatefulObject)
					if (((ICloneObjectExtensionStatefulObject) extension).isDirty())
						return true;
		}

		//we don't have to check the dirty state for deleted extensions as
		//they are not supposed to be modified anymore.

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStoreCloneObject#setDirty(boolean)
	 */
	@Override
	public void setDirty(boolean dirty)
	{
		if (log.isTraceEnabled())
			log.trace("setDirty() - dirty: " + dirty);

		checkSeal();

		this.dirty = dirty;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStoreCloneObject#isPersisted()
	 */
	@Override
	public boolean isPersisted()
	{
		return persisted;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStoreCloneObject#setPersisted(boolean)
	 */
	@Override
	public void setPersisted(boolean persisted)
	{
		if (log.isTraceEnabled())
			log.trace("setPersisted() - persisted: " + persisted);

		checkSeal();

		this.persisted = persisted;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.data.AbstractCloneDataElement#seal()
	 */
	@Override
	public void seal()
	{
		log.trace("seal()");

		//seal this object via our super class
		super.seal();

		//but also seal all registered extension objects
		if (extensions != null)
		{
			for (ICloneObjectExtension extension : extensions.values())
			{
				extension.seal();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.data.AbstractCloneDataElement#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	/**
	 * Copies over all <em>CloneObject</em> data fields from <em>source</em> to <em>this</em>.<br/>
	 * This is used for manual <em>clone()</em> implementations in child classes.
	 */
	protected void cloneData(CloneObject source)
	{
		this.uuid = source.uuid;
		this.dirty = source.dirty;
		this.persisted = source.persisted;
		this.extensions = null;
		this.hasExtensions = false;

		//check if we need to clone any extensions
		if (source.extensions != null && !source.extensions.isEmpty())
		{
			if (log.isTraceEnabled())
				log.trace("cloneData() - going to clone extensions for source: " + source);

			hasExtensions = true;

			//ok, we do need to clone all the extensions
			this.extensions = new HashMap<Class<? extends ICloneObjectExtension>, ICloneObjectExtension>(
					source.extensions.size());

			for (Map.Entry<Class<? extends ICloneObjectExtension>, ICloneObjectExtension> entry : source.extensions
					.entrySet())
			{
				if (log.isTraceEnabled())
					log.trace("cloneData() - cloning extension - key: " + entry.getKey() + ", value: "
							+ entry.getValue());

				try
				{
					this.extensions.put(entry.getKey(), (ICloneObjectExtension) entry.getValue().clone());
				}
				catch (CloneNotSupportedException e)
				{
					log.error("cloneData(): cloning of ICloneObjectExtension failed - extension: " + entry.getValue()
							+ ", source: " + source + " - " + e, e);
				}
			}
		}

		//check if we need to clone any deleted extensions
		if (source.deletedExtensions != null && !source.deletedExtensions.isEmpty())
		{
			if (log.isTraceEnabled())
				log.trace("cloneData() - going to clone deleted extensions for source: " + source);

			//ok, we do need to clone all the extensions
			this.deletedExtensions = new HashMap<Class<? extends ICloneObjectExtension>, ICloneObjectExtension>(
					source.deletedExtensions.size());

			for (Map.Entry<Class<? extends ICloneObjectExtension>, ICloneObjectExtension> entry : source.deletedExtensions
					.entrySet())
			{
				if (log.isTraceEnabled())
					log.trace("cloneData() - cloning deleted extension - key: " + entry.getKey() + ", value: "
							+ entry.getValue());

				try
				{
					this.deletedExtensions.put(entry.getKey(), (ICloneObjectExtension) entry.getValue().clone());
				}
				catch (CloneNotSupportedException e)
				{
					log.error("cloneData(): cloning of ICloneObjectExtension failed - deleted extension: "
							+ entry.getValue() + ", source: " + source + " - " + e, e);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneObject#equalsAll(org.electrocodeogram.cpc.core.api.data.ICloneObject)
	 */
	@Override
	public boolean equalsAll(ICloneObject otherCloneObject)
	{
		if (log.isTraceEnabled())
			log.trace("equalsAll() - this: " + this + ", otherCloneObject: " + otherCloneObject);

		//first check for null, wrong class, different uuid
		if (!equals(otherCloneObject))
		{
			if (log.isDebugEnabled())
				log.debug("equalsAll() - normal equals differs - this: " + this + ", otherCloneObject: "
						+ otherCloneObject);
			return false;
		}

		CloneObject other = (CloneObject) otherCloneObject;

		//now check additional fields
		if (dirty != other.dirty)
		{
			if (log.isDebugEnabled())
				log.debug("equalsAll() - dirty flag differs - this: " + this.isDirty() + " - " + this
						+ ", otherCloneObject: " + other.isDirty() + " - " + otherCloneObject);
			return false;
		}

		//it is not clear whether we should include the persisted flag in the comparison
		if (persisted != other.persisted)
			log.warn("equalsAll(): persisted state differs, ignoring difference - this: " + this
					+ ", otherCloneObject: " + other);
		//return false;

		//TODO: do we need to check clone extensions here?

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneObject#hasExtensions()
	 */
	@Override
	public boolean hasExtensions()
	{
		return hasExtensions;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneObject#getExtension(java.lang.Class)
	 */
	@Override
	public ICloneObjectExtension getExtension(Class<? extends ICloneObjectExtension> extensionClass)
	{
		assert (extensionClass != null);

		//we only need to do a lookup if the extension registry has been initialised
		if (extensions == null)
			return null;

		return extensions.get(extensionClass);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneObject#getExtensions()
	 */
	@Override
	public List<ICloneObjectExtension> getExtensions()
	{
		/*
		 * NOTE: a missmatch between hasExtensions and extensions is possible
		 * while the clone object is still being restored from persistent storage by a store provider.
		 * 
		 * TODO: make these checks into errors again but detect the special case where
		 * we're currently being restored.
		 */
		//we only need to do a lookup if the extension registry has been initialised
		if ((extensions == null) || (extensions.isEmpty()))
		{
			if (hasExtensions)
				log.trace("getExtensions() - clone has no extensions but hasExtensions is true - clone: " + uuid,
						new Throwable());
			return EMPTY_EXTENSIONS_LIST;
		}

		//ok, return all extensions
		if (!hasExtensions)
			log.trace("getExtensions() - clone has extensions but hasExtensions is false - clone: " + uuid,
					new Throwable());
		return new ArrayList<ICloneObjectExtension>(extensions.values());
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneObject#addExtension(org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension)
	 */
	@Override
	public void addExtension(ICloneObjectExtension extension)
	{
		assert (extension != null);
		if (log.isTraceEnabled())
			log.trace("addExtension() - extension: " + extension + " (interface: "
					+ extension.getExtensionInterfaceClass() + ")");
		assert (extension.getExtensionInterfaceClass() != null);

		checkSeal();

		//initialise registry, if we haven't done that yet
		if (extensions == null)
			extensions = new HashMap<Class<? extends ICloneObjectExtension>, ICloneObjectExtension>(3);

		//double check that this extension may be added for this ICloneObject
		if (extension instanceof ICloneObjectExtensionStatefulObject)
		{
			if (!((ICloneObjectExtensionStatefulObject) extension).getPersistenceParentClassIdentifier().equals(
					this.getPersistenceClassIdentifier()))
			{
				//this extension belongs to a different ICloneObject, it must not be added for our type.
				log.error("addExtension() - trying to add extension of different ICloneObject parent type: "
						+ extension + " - " + this, new Throwable());
				throw new IllegalArgumentException(" trying to add extension of different ICloneObject parent type");
			}
		}

		//set our uuid as parent uuid
		extension.setParentUuid(this.getUuid());

		//add/overwrite value
		extensions.put(extension.getExtensionInterfaceClass(), extension);

		//clear out any deleted extension for this interface class
		if (deletedExtensions != null && !deletedExtensions.isEmpty())
			deletedExtensions.remove(extension.getExtensionInterfaceClass());

		hasExtensions = true;
		setDirty(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneObject#removeExtension(java.lang.Class)
	 */
	@Override
	public void removeExtension(Class<? extends ICloneObjectExtension> extensionClass)
	{
		if (log.isTraceEnabled())
			log.trace("removeExtension() - removeExtension: " + extensionClass);
		assert (extensionClass != null);

		checkSeal();

		if (extensions == null || extensions.isEmpty())
		{
			log.trace("removeExtension() - no extensions present.");
			return;
		}

		ICloneObjectExtension removedExtension = extensions.remove(extensionClass);

		if (removedExtension != null)
		{
			//keep track of removed extensions

			//initialise deleted extensions registry, if we haven't done that yet
			if (deletedExtensions == null)
				deletedExtensions = new HashMap<Class<? extends ICloneObjectExtension>, ICloneObjectExtension>(3);

			deletedExtensions.put(extensionClass, removedExtension);
		}
		else
		{
			log.trace("removeExtension() - extension was not found in this clone object.");
		}

		if (extensions.isEmpty())
		{
			log.trace("removeExtension() - clone object no longer has any extensions.");
			hasExtensions = false;
		}

		setDirty(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneObject#removeExtension(org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension)
	 */
	@Override
	public void removeExtension(ICloneObjectExtension extension)
	{
		if (log.isTraceEnabled())
			log.trace("removeExtension() - extension: " + extension);
		assert (extension != null);

		removeExtension(extension.getExtensionInterfaceClass());
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.special.IStoreCloneObject#getDeletedExtensions()
	 */
	@Override
	public List<ICloneObjectExtension> getDeletedExtensions()
	{
		//we only need to do a lookup if the extension registry has been initialised
		if ((deletedExtensions == null) || (deletedExtensions.isEmpty()))
			return EMPTY_EXTENSIONS_LIST;

		//ok, return all extensions
		return new ArrayList<ICloneObjectExtension>(deletedExtensions.values());
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.special.IStoreCloneObject#purgeDeletedExtensions()
	 */
	@Override
	public void purgeDeletedExtensions()
	{
		if (deletedExtensions == null)
			return;

		deletedExtensions.clear();
		deletedExtensions = null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#getPersistenceObjectIdentifier()
	 */
	@Override
	public String getPersistenceObjectIdentifier()
	{
		return PERSISTENCE_OBJECT_IDENTIFIER;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#getState()
	 */
	@Override
	public Map<String, Comparable<? extends Object>> getState()
	{
		Map<String, Comparable<? extends Object>> result = new HashMap<String, Comparable<? extends Object>>(10);
		//TODO: deciding on the correct size for the HashMap is not easy, as sub classes of
		//		this object will add their own values to this map.

		result.put("uuid", uuid);
		result.put("hasExtensions", hasExtensions);

		/*
		 * The fields dirty & persisted need not be persisted by the store provider.
		 * Any extensions registered for this object will be persisted separately.
		 */

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#setState(java.util.Map)
	 */
	@Override
	public void setState(Map<String, Comparable<? extends Object>> state)
	{
		assert (state != null);

		checkSeal();

		try
		{
			uuid = (String) state.get("uuid");
			hasExtensions = (Boolean) state.get("hasExtensions");

			//dirty, persisted and extensions are not part of the state map.
		}
		catch (Exception e)
		{
			//this should not happen
			log.error("setState() - error while restoring internal state - state: " + state + " - " + e, e);
		}

		//TODO: should we set dirty to true here?
		setDirty(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#getStateTypes()
	 */
	@Override
	public Map<String, Class<? extends Object>> getStateTypes()
	{
		Map<String, Class<? extends Object>> result = new HashMap<String, Class<? extends Object>>(10);
		//TODO: deciding on the correct size for the HashMap is not easy, as sub classes of
		//		this object will add their own values to this map.

		result.put("uuid", String.class);
		result.put("hasExtensions", Boolean.class);

		/*
		 * The fields dirty & persisted need not be persisted by the store provider.
		 * Any extensions registered for this object will be persisted separately.
		 */

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		if (getUuid() == null)
		{
			log.error("hashCode(): uuid is null - " + this, new Throwable());
			return 0;
		}

		return getUuid().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;

		final CloneObject other = (CloneObject) obj;

		if (getUuid() == null || other.getUuid() == null)
		{
			log.error("equals(): uuid is null - " + this + " - " + other, new Throwable());

			if (other.getUuid() != null)
				return false;
		}
		else if (!getUuid().equals(other.getUuid()))
			return false;

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 * 
	 * Basic implementation which only forwards lookups to the AdapterManager.
	 * Subclasses can override this method with something more meaningful.
	 * This was added mainly to ensure adaptability by 3rd party plugins, if needed.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter)
	{
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

}
