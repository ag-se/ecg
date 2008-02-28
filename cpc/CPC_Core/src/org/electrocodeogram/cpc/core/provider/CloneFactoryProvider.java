package org.electrocodeogram.cpc.core.provider;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.electrocodeogram.cpc.core.api.data.ICloneDataElement;
import org.electrocodeogram.cpc.core.api.data.ICloneObject;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectSupport;
import org.electrocodeogram.cpc.core.api.data.special.IStatefulObject;
import org.electrocodeogram.cpc.core.api.provider.IManagableProvider;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;


/**
 * Default implementation for the {@link ICloneFactoryProvider} API.
 * 
 * @author vw
 */
public class CloneFactoryProvider implements ICloneFactoryProvider, IManagableProvider
{
	private static Log log = LogFactory.getLog(CloneFactoryProvider.class);

	public static final String EXTENSION_POINT_CLONEDATAELEMENTS = "org.electrocodeogram.cpc.core.cloneDataElements";

	public static final String CONFIGURATIONELEMENT_NAME_CLONEOBJECT = "cloneObject";
	public static final String CONFIGURATIONELEMENT_NAME_CLONEOBJECTSUPPORT = "cloneObjectSupport";
	public static final String CONFIGURATIONELEMENT_NAME_CLONEOBJECTEXTENSION = "cloneObjectExtension";

	public static final String CONFIGURATIONELEMENT_ATTRIBUTE_NAME = "name";
	public static final String CONFIGURATIONELEMENT_ATTRIBUTE_CLASS = "class";
	public static final String CONFIGURATIONELEMENT_ATTRIBUTE_TYPE = "type";
	public static final String CONFIGURATIONELEMENT_ATTRIBUTE_PARENTTYPE = "parentType";
	public static final String CONFIGURATIONELEMENT_ATTRIBUTE_PRIORITY = "priority";

	private static Map<String, CloneDataElementDescriptor> cloneObjectTypeRegistry = null;
	private static Map<String, CloneDataElementDescriptor> cloneObjectClassRegistry = null;

	private static Map<String, CloneDataElementDescriptor> cloneObjectSupportTypeRegistry = null;
	private static Map<String, CloneDataElementDescriptor> cloneObjectSupportClassRegistry = null;

	private static Map<String, CloneDataElementDescriptor> cloneObjectExtensionTypeRegistry = null;
	private static Map<String, CloneDataElementDescriptor> cloneObjectExtensionClassRegistry = null;

	private static Map<String, CloneDataElementDescriptor> persistenceClassIdentifierRegistry = null;

	public CloneFactoryProvider()
	{
		log.trace("CloneFactoryProvider()");

		initialiseRegistries();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider#getInstance(java.lang.Class, java.lang.String)
	 */
	@Override
	public ICloneObject getInstance(Class<? extends ICloneObject> type, String uuid)
	{
		if (log.isTraceEnabled())
			log.trace("getInstance() - type: " + type + ", uuid: " + uuid);
		assert (type != null && uuid != null);

		//first lookup by type/interface (i.e. IClone)
		CloneDataElementDescriptor descriptor = cloneObjectTypeRegistry.get(type.getName());
		if (descriptor == null)
			//if that fails, try to lookup by implementing class (i.e. Clone)
			descriptor = cloneObjectClassRegistry.get(type.getName());

		if (descriptor != null)
		{
			try
			{
				//return a new instance with the uuid parameter
				//according to the ICloneObject API contract all implementations need to provide a one
				//String argument contructor which takes an uuid parameter.
				//this should therefore always work.
				/*
				 * TODO: / FIXME: we could get into some troubes with class loaders here by
				 * not using the configuration element. However, we want to pass an argument to
				 * the constructor, which is not possible via the configuration element.
				 */
				return (ICloneObject) descriptor.getClazz().getConstructor(String.class).newInstance(uuid);

			}
			catch (Exception e)
			{
				//this shouldn't happen
				log.error("getInstance() - exception while creating instance of clone object - type: " + type
						+ ", class: " + descriptor.getClassName() + ", uuid: " + uuid, e);
				return null;
			}
		}
		else
		{
			//unknown clone object type
			log.warn("getInstance() - unknown clone object instance requested: " + type);
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider#getInstance(java.lang.Class)
	 */
	@Override
	public ICloneDataElement getInstance(Class<? extends ICloneDataElement> type)
	{
		if (log.isTraceEnabled())
			log.trace("getInstance() - type: " + type);
		assert (type != null);

		//first lookup by type/interface (i.e. IClone)
		CloneDataElementDescriptor descriptor = cloneObjectTypeRegistry.get(type.getName());
		if (descriptor == null)
			//if that fails, try to lookup by implementing class (i.e. Clone)
			descriptor = cloneObjectClassRegistry.get(type.getName());

		//also check the clone object support registry
		if (descriptor == null)
			//by type/interface (i.e. ICloneFileContent)
			descriptor = cloneObjectSupportTypeRegistry.get(type.getName());
		if (descriptor == null)
			//by implementing class (i.e. CloneFileContent)
			descriptor = cloneObjectSupportClassRegistry.get(type.getName());

		//and the clone object extension registry
		if (descriptor == null)
			//by type/interface (i.e. ICloneNonWsPositionExtension)
			descriptor = cloneObjectExtensionTypeRegistry.get(type.getName());
		if (descriptor == null)
			//by implementing class (i.e. CloneNonWsPositionExtension)
			descriptor = cloneObjectExtensionClassRegistry.get(type.getName());

		if (descriptor != null)
		{
			try
			{
				//return a new instance
				return (ICloneDataElement) descriptor.getConfigurationElement().createExecutableExtension(
						CONFIGURATIONELEMENT_ATTRIBUTE_CLASS);
				//return (ICloneDataElement) descriptor.getClazz().newInstance();
			}
			catch (Exception e)
			{
				//this shouldn't happen
				log.error("getInstance() - exception while creating instance of class - type: " + type + ", class: "
						+ descriptor.getClassName() + " - " + e, e);
				return null;
			}
		}
		else
		{
			//unknown clone object type
			log.warn("getInstance() - unknown clone object instance requested: " + type);
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider#getInstanceByPersistenceClassIdentifier(java.lang.String)
	 */
	public IStatefulObject getInstanceByPersistenceClassIdentifier(String persistenceClassIdentifier)
	{
		if (log.isTraceEnabled())
			log.trace("getInstanceByPersistenceClassIdentifier() - persistenceClassIdentifier: "
					+ persistenceClassIdentifier);
		assert (persistenceClassIdentifier != null);

		CloneDataElementDescriptor descriptor = persistenceClassIdentifierRegistry.get(persistenceClassIdentifier);
		if (descriptor != null)
		{
			try
			{
				//return a new instance
				return (IStatefulObject) descriptor.getConfigurationElement().createExecutableExtension(
						CONFIGURATIONELEMENT_ATTRIBUTE_CLASS);
			}
			catch (Exception e)
			{
				//this shouldn't happen
				log.error(
						"getInstanceByPersistenceClassIdentifier() - exception while creating instance of class - persistenceClassIdentifier: "
								+ persistenceClassIdentifier + ", class: " + descriptor.getClassName() + " - " + e, e);
				return null;
			}
		}
		else
		{
			//unknown persistenceClassIdentifier
			log
					.warn("getInstanceByPersistenceClassIdentifier() - unknown persistenceClassIdentifier instance requested: "
							+ persistenceClassIdentifier);
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider#getRegisteredCloneObjectExtensions()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Class<? extends ICloneObjectExtension>> getRegisteredCloneObjectExtensions()
	{
		log.trace("getRegisteredCloneObjectExtensions()");

		List<Class<? extends ICloneObjectExtension>> result = new LinkedList<Class<? extends ICloneObjectExtension>>();

		for (CloneDataElementDescriptor descriptor : cloneObjectExtensionTypeRegistry.values())
			result.add((Class<? extends ICloneObjectExtension>) descriptor.getClazz());

		if (log.isTraceEnabled())
			log.trace("getRegisteredCloneObjectExtensions() - result: " + result);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider#getRegisteredCloneObjectExtensions(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Class<? extends ICloneObjectExtension>> getRegisteredCloneObjectExtensions(
			Class<? extends ICloneObject> parentType)
	{
		if (log.isTraceEnabled())
			log.trace("getRegisteredCloneObjectExtensions() - parentType: " + parentType);
		assert (parentType != null);

		List<Class<? extends ICloneObjectExtension>> result = new LinkedList<Class<? extends ICloneObjectExtension>>();

		for (CloneDataElementDescriptor descriptor : cloneObjectExtensionTypeRegistry.values())
		{
			//only add those extensions which match the given parent type
			//TODO: we might want to add a special HashMap as lookup structure for this if
			//we expect the  number of registered extensions to be high.
			if (parentType.toString().equals(descriptor.getParentType()))
				result.add((Class<? extends ICloneObjectExtension>) descriptor.getClazz());
		}

		if (log.isTraceEnabled())
			log.trace("getRegisteredCloneObjectExtensions() - result: " + result);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider#getRegisteredCloneObjectExtensionObjects(java.lang.Class)
	 */
	@Override
	public List<ICloneObjectExtension> getRegisteredCloneObjectExtensionObjects(Class<? extends ICloneObject> parentType)
	{
		if (log.isTraceEnabled())
			log.trace("getRegisteredCloneObjectExtensionObjects() - parentType: " + parentType);
		assert (parentType != null);

		List<ICloneObjectExtension> result = new LinkedList<ICloneObjectExtension>();

		for (CloneDataElementDescriptor descriptor : cloneObjectExtensionTypeRegistry.values())
		{
			//only add those extensions which match the given parent type

			//			if (log.isTraceEnabled())
			//				log.trace("getRegisteredCloneObjectExtensionObjects() - check: " + descriptor + " - "
			//						+ parentType.getCanonicalName() + " == " + descriptor.getParentType() + " ?");

			//TODO: we might want to add a special HashMap as lookup structure for this if
			//we expect the  number of registered extensions to be high.
			if (parentType.getCanonicalName().equals(descriptor.getParentType()))
				result.add((ICloneObjectExtension) descriptor.getObject());
		}

		if (log.isTraceEnabled())
			log.trace("getRegisteredCloneObjectExtensionObjects() - result: " + result);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider#getRegisteredCloneObjects()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Class<? extends ICloneObject>> getRegisteredCloneObjects()
	{
		log.trace("getRegisteredCloneObjects()");

		List<Class<? extends ICloneObject>> result = new LinkedList<Class<? extends ICloneObject>>();

		for (CloneDataElementDescriptor descriptor : cloneObjectTypeRegistry.values())
			result.add((Class<? extends ICloneObject>) descriptor.getClazz());

		if (log.isTraceEnabled())
			log.trace("getRegisteredCloneObjects() - result: " + result);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider#getRegisteredCloneObjectSupports()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Class<? extends ICloneObjectSupport>> getRegisteredCloneObjectSupports()
	{
		log.trace("getRegisteredCloneObjectSupports()");

		List<Class<? extends ICloneObjectSupport>> result = new LinkedList<Class<? extends ICloneObjectSupport>>();

		for (CloneDataElementDescriptor descriptor : cloneObjectSupportTypeRegistry.values())
			result.add((Class<? extends ICloneObjectSupport>) descriptor.getClazz());

		if (log.isTraceEnabled())
			log.trace("getRegisteredCloneObjectSupports() - result: " + result);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.IProvider#getProviderName()
	 */
	@Override
	public String getProviderName()
	{
		return "CPC Core: org.electrocodeogram.cpc.core.provider.CloneFactoryProvider";
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IManagableProvider#onLoad()
	 */
	@Override
	public void onLoad()
	{
		log.trace("onLoad()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IManagableProvider#onUnload()
	 */
	@Override
	public void onUnload()
	{
		log.trace("onUnload()");
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		//TODO: add something more meaningful here
		return super.toString();
	}

	/**
	 * Initialises the clone object registry with custom and default implementations.<br/>
	 * Retrieves data from the corresponding extension point (TODO).
	 */
	private void initialiseRegistries()
	{
		log.trace("initialiseCloneObjectRegistry()");

		/*
		 * ICloneObject
		 */

		cloneObjectTypeRegistry = new HashMap<String, CloneDataElementDescriptor>(10);
		cloneObjectClassRegistry = new HashMap<String, CloneDataElementDescriptor>(10);

		/*
		 * ICloneObjectSupport
		 */

		cloneObjectSupportTypeRegistry = new HashMap<String, CloneDataElementDescriptor>(5);
		cloneObjectSupportClassRegistry = new HashMap<String, CloneDataElementDescriptor>(5);

		/*
		 * ICloneObjectExtension
		 */

		cloneObjectExtensionTypeRegistry = new HashMap<String, CloneDataElementDescriptor>(5);
		cloneObjectExtensionClassRegistry = new HashMap<String, CloneDataElementDescriptor>(5);

		/*
		 * For lookups of IStatefulObjects
		 */

		persistenceClassIdentifierRegistry = new HashMap<String, CloneDataElementDescriptor>(15);

		/*
		 * Load registered objects from extension point.
		 */

		osgiCloneDataElementsInitialization();
	}

	private void osgiCloneDataElementsInitialization()
	{
		log.trace("osgiCloneObjectInitialization(): building clone data elements registry from extension data");

		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IConfigurationElement[] extensions = reg.getConfigurationElementsFor(EXTENSION_POINT_CLONEDATAELEMENTS);
		for (IConfigurationElement element : extensions)
		{
			try
			{
				//check the type of the registered element
				if (element.getName().equals(CONFIGURATIONELEMENT_NAME_CLONEOBJECT))
				{
					//it's an ICloneObject
					CloneDataElementDescriptor descriptor = new CloneDataElementDescriptor(element);

					cloneObjectClassRegistry
							.put(element.getAttribute(CONFIGURATIONELEMENT_ATTRIBUTE_CLASS), descriptor);

					//only set for "type" if none is registered yet or our priority is higher
					CloneDataElementDescriptor oldDescriptor = cloneObjectTypeRegistry.get(element
							.getAttribute(CONFIGURATIONELEMENT_ATTRIBUTE_TYPE));
					if (oldDescriptor == null || oldDescriptor.getPriority() < descriptor.getPriority())
						cloneObjectTypeRegistry.put(element.getAttribute(CONFIGURATIONELEMENT_ATTRIBUTE_TYPE),
								descriptor);

					if (descriptor.getPersistenceClassIdentifier() != null)
						persistenceClassIdentifierRegistry.put(descriptor.getPersistenceClassIdentifier(), descriptor);
				}
				else if (element.getName().equals(CONFIGURATIONELEMENT_NAME_CLONEOBJECTSUPPORT))
				{
					//it's an ICloneObjectSupport
					CloneDataElementDescriptor descriptor = new CloneDataElementDescriptor(element);

					cloneObjectSupportClassRegistry.put(element.getAttribute(CONFIGURATIONELEMENT_ATTRIBUTE_CLASS),
							descriptor);

					//only set for "type" if none is registered yet or our priority is higher
					CloneDataElementDescriptor oldDescriptor = cloneObjectSupportTypeRegistry.get(element
							.getAttribute(CONFIGURATIONELEMENT_ATTRIBUTE_TYPE));
					if (oldDescriptor == null || oldDescriptor.getPriority() < descriptor.getPriority())
						cloneObjectSupportTypeRegistry.put(element.getAttribute(CONFIGURATIONELEMENT_ATTRIBUTE_TYPE),
								descriptor);

					if (descriptor.getPersistenceClassIdentifier() != null)
						persistenceClassIdentifierRegistry.put(descriptor.getPersistenceClassIdentifier(), descriptor);
				}
				else if (element.getName().equals(CONFIGURATIONELEMENT_NAME_CLONEOBJECTEXTENSION))
				{
					//it's an ICloneObjectExtension
					CloneDataElementDescriptor descriptor = new CloneDataElementDescriptor(element);

					cloneObjectExtensionClassRegistry.put(element.getAttribute(CONFIGURATIONELEMENT_ATTRIBUTE_CLASS),
							descriptor);

					//only set for "type" if none is registered yet or our priority is higher
					CloneDataElementDescriptor oldDescriptor = cloneObjectExtensionTypeRegistry.get(element
							.getAttribute(CONFIGURATIONELEMENT_ATTRIBUTE_TYPE));
					if (oldDescriptor == null || oldDescriptor.getPriority() < descriptor.getPriority())
						cloneObjectExtensionTypeRegistry.put(element.getAttribute(CONFIGURATIONELEMENT_ATTRIBUTE_TYPE),
								descriptor);

					if (descriptor.getPersistenceClassIdentifier() != null)
						persistenceClassIdentifierRegistry.put(descriptor.getPersistenceClassIdentifier(), descriptor);
				}
				else
				{
					//unsupported element (this shouldn't happen as it would violate the schema)
					log.error("osgiCloneDataElementsInitialization() - unsupported element - " + element.getName()
							+ " - " + element, new Throwable());
				}
			}
			catch (Exception e)
			{
				log.error("registration of clone data element failed: "
						+ element.getAttribute(CONFIGURATIONELEMENT_ATTRIBUTE_CLASS) + " - " + e, e);
			}
		}
	}

}
