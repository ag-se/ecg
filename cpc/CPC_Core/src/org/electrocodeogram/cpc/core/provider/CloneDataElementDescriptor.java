package org.electrocodeogram.cpc.core.provider;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.electrocodeogram.cpc.core.api.data.ICloneDataElement;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.data.special.IStatefulObject;


/**
 * Data wrapper object for the {@link CloneFactoryProvider}.
 * 
 * @author vw
 * 
 * @see CloneFactoryProvider
 */
class CloneDataElementDescriptor
{
	private String name;
	private String typeName;
	private String className;
	private String parentType;
	private String persistenceClassIdentifier = null;
	private int priority = 0;
	private Class<? extends ICloneDataElement> clazz;
	private ICloneDataElement object;
	private IConfigurationElement configurationElement;

	@SuppressWarnings("unchecked")
	CloneDataElementDescriptor(IConfigurationElement element) throws CoreException
	{
		this.configurationElement = element;
		this.name = element.getAttribute(CloneFactoryProvider.CONFIGURATIONELEMENT_ATTRIBUTE_NAME);
		this.typeName = element.getAttribute(CloneFactoryProvider.CONFIGURATIONELEMENT_ATTRIBUTE_TYPE);
		this.className = element.getAttribute(CloneFactoryProvider.CONFIGURATIONELEMENT_ATTRIBUTE_CLASS);
		if (element.getAttribute(CloneFactoryProvider.CONFIGURATIONELEMENT_ATTRIBUTE_PARENTTYPE) != null)
			this.parentType = element.getAttribute(CloneFactoryProvider.CONFIGURATIONELEMENT_ATTRIBUTE_PARENTTYPE);
		if (element.getAttribute(CloneFactoryProvider.CONFIGURATIONELEMENT_ATTRIBUTE_PRIORITY) != null)
			this.priority = Integer.parseInt(element
					.getAttribute(CloneFactoryProvider.CONFIGURATIONELEMENT_ATTRIBUTE_PRIORITY));

		/*
		 * This is somewhat tricky, we need the implementing class, not an object.
		 * Thus we temporarily create an object in order to then obtain it's class.
		 * 
		 * We now also keep the object for performance reasons.
		 */

		Object tmpObj = element.createExecutableExtension(CloneFactoryProvider.CONFIGURATIONELEMENT_ATTRIBUTE_CLASS);
		if (!(tmpObj instanceof ICloneDataElement))
		{
			throw new IllegalArgumentException("class does not implement ICloneDataElement - class: " + this.className);
		}

		if (tmpObj instanceof IStatefulObject)
			this.persistenceClassIdentifier = ((IStatefulObject) tmpObj).getPersistenceClassIdentifier();

		this.object = (ICloneDataElement) tmpObj;
		this.clazz = this.object.getClass();

		//make sure no one modifies our example object
		this.object.seal();
	}

	String getName()
	{
		return name;
	}

	/**
	 * @return may be NULL if this is an {@link ICloneObjectExtension} descriptor.
	 */
	String getTypeName()
	{
		return typeName;
	}

	String getClassName()
	{
		return className;
	}

	String getParentType()
	{
		return parentType;
	}

	String getPersistenceClassIdentifier()
	{
		return persistenceClassIdentifier;
	}

	int getPriority()
	{
		return priority;
	}

	Class<? extends ICloneDataElement> getClazz()
	{
		return clazz;
	}

	/**
	 * This method does <b>not</b> return a new instance!
	 * 
	 * @return shared object instance, never null.
	 */
	ICloneDataElement getObject()
	{
		return object;
	}

	IConfigurationElement getConfigurationElement()
	{
		return configurationElement;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		return result;
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
		final CloneDataElementDescriptor other = (CloneDataElementDescriptor) obj;
		if (className == null)
		{
			if (other.className != null)
				return false;
		}
		else if (!className.equals(other.className))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "CloneDataElementDescriptor[name: " + name + ", type: " + typeName + ", pri: " + priority + ", class: "
				+ className + ", parentType: " + parentType + "]";
	}

}
