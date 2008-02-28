package org.electrocodeogram.cpc.ui.data;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySheetEntry;
import org.eclipse.ui.views.properties.IPropertySource2;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.data.special.IStoreCloneObject;


public class ClonePropertySource implements IPropertySource2
{
	private static final Log log = LogFactory.getLog(ClonePropertySource.class);

	private IClone clone;
	private List<PropertyDescriptor> properties;

	public ClonePropertySource(IClone clone)
	{
		if (log.isTraceEnabled())
			log.trace("ClonePropertySource() - clone: " + clone);
		assert (clone != null);

		this.clone = clone;

		properties = new ArrayList<PropertyDescriptor>(25);
		properties.add(new MyPropertyDescriptor("uuid", "Clone UUID", "UUIDs"));
		properties.add(new MyPropertyDescriptor("offset", "Offset", "Position"));
		properties.add(new MyPropertyDescriptor("length", "Length", "Position"));
		properties.add(new MyPropertyDescriptor("fileUuid", "File UUID", "UUIDs"));
		properties.add(new MyPropertyDescriptor("groupUuid", "Group UUID", "UUIDs"));
		properties.add(new MyPropertyDescriptor("originUuid", "Origin UUID", "UUIDs"));
		properties.add(new MyPropertyDescriptor("classifications", "Classifications", "Misc"));
		properties.add(new MyPropertyDescriptor("creationDate", "Creation Date", "Dates"));
		properties.add(new MyPropertyDescriptor("modDate", "Modification Date", "Dates"));
		properties.add(new MyPropertyDescriptor("cloneState", "State", "Clone State"));
		properties.add(new MyPropertyDescriptor("cloneStateChangeDate", "Change Date", "Clone State"));
		properties.add(new MyPropertyDescriptor("cloneStateDismissalDate", "Dismissal Date", "Clone State"));
		properties.add(new MyPropertyDescriptor("cloneStateWeight", "Weight", "Clone State"));
		properties.add(new MyPropertyDescriptor("cloneStateMsg", "Message", "Clone State"));
		properties.add(new MyPropertyDescriptor("transient", "Is Transient", "Flags"));
		properties.add(new MyPropertyDescriptor("hasExtensions", "Has Extensions", "Flags"));
		properties.add(new MyPropertyDescriptor("persisted", "Is Persisted", "Flags"));
		properties.add(new MyPropertyDescriptor("dirty", "Is Dirty", "Flags"));
		properties.add(new MyPropertyDescriptor("sealed", "Is Sealed", "Flags"));

		//add some extension information
		if (clone.hasExtensions())
		{
			List<ICloneObjectExtension> extensions = clone.getExtensions();
			for (ICloneObjectExtension extension : extensions)
			{
				properties.add(new MyPropertyDescriptor("ext:" + extension.getExtensionInterfaceClass().toString(),
						extension.getExtensionInterfaceClass().getSimpleName(), "Extensions", true));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
	 */
	@Override
	public IPropertyDescriptor[] getPropertyDescriptors()
	{
		return properties.toArray(new IPropertyDescriptor[properties.size()]);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(java.lang.Object)
	 */
	@Override
	public Object getPropertyValue(Object id)
	{
		if (id.equals("uuid"))
			return clone.getUuid();
		else if (id.equals("offset"))
			return clone.getOffset();
		else if (id.equals("length"))
			return clone.getLength();
		else if (id.equals("fileUuid"))
			return clone.getFileUuid();
		else if (id.equals("groupUuid"))
			return clone.getGroupUuid();
		else if (id.equals("originUuid"))
			return clone.getOriginUuid();
		else if (id.equals("classifications"))
			return clone.getClassifications();
		else if (id.equals("creationDate"))
			return clone.getCreationDate();
		else if (id.equals("modDate"))
			return clone.getModificationDate();
		else if (id.equals("cloneState"))
			return clone.getCloneState();
		else if (id.equals("cloneStateChangeDate"))
			return clone.getCloneStateChangeDate();
		else if (id.equals("cloneStateDismissalDate"))
			return clone.getCloneStateDismissalDate();
		else if (id.equals("cloneStateWeight"))
			return clone.getCloneStateWeight();
		else if (id.equals("cloneStateMsg"))
			return clone.getCloneStateMessage();
		else if (id.equals("transient"))
			return clone.isTransient();
		else if (id.equals("hasExtensions"))
			return clone.hasExtensions();
		else if (id.equals("persisted"))
			return ((IStoreCloneObject) clone).isPersisted();
		else if (id.equals("dirty"))
			return ((IStoreCloneObject) clone).isDirty();
		else if (id.equals("sealed"))
			return clone.isSealed();
		else if (id instanceof String && ((String) id).startsWith("ext:"))
		{
			//special extension handling
			String clazz = ((String) id).substring(4);
			List<ICloneObjectExtension> extensions = clone.getExtensions();
			for (ICloneObjectExtension extension : extensions)
			{
				if (clazz.equals(extension.getExtensionInterfaceClass().toString()))
				{
					return extension;
				}
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource2#isPropertyResettable(java.lang.Object)
	 */
	@Override
	public boolean isPropertyResettable(Object id)
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource2#isPropertySet(java.lang.Object)
	 */
	@Override
	public boolean isPropertySet(Object id)
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource#getEditableValue()
	 */
	@Override
	public Object getEditableValue()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource#resetPropertyValue(java.lang.Object)
	 */
	@Override
	public void resetPropertyValue(Object id)
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setPropertyValue(Object id, Object value)
	{
	}

	private class MyPropertyDescriptor extends PropertyDescriptor
	{
		public MyPropertyDescriptor(String id, String displayName, String category)
		{
			super(id, displayName);
			setCategory(category);
		}

		public MyPropertyDescriptor(String id, String displayName, String category, boolean expertProperty)
		{
			super(id, displayName);
			setCategory(category);
			if (expertProperty)
				setFilterFlags(new String[] { IPropertySheetEntry.FILTER_ID_EXPERT });
		}
	}
}
