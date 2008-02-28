package org.electrocodeogram.cpc.importexport.generic;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.electrocodeogram.cpc.importexport.api.generic.IGenericExtensionOption;


/**
 * Default {@link IGenericExtensionOption} implementation.
 * 
 * @author vw
 */
public class GenericExtensionOption implements IGenericExtensionOption
{
	private static Log log = LogFactory.getLog(GenericExtensionOption.class);

	public static final String ELEMENT_NAME = "option";

	private String id;
	private String name;
	private String defaultValue;
	private String value;

	public GenericExtensionOption(IConfigurationElement element) throws IllegalArgumentException
	{
		if (log.isTraceEnabled())
			log.trace("GenericExtensionOption() - element: " + element);

		if (element == null || !element.getName().equals(ELEMENT_NAME))
		{
			log.error("GenericExtensionOption() - illegal element - element: " + element, new Throwable());
			throw new IllegalArgumentException("illegal element");
		}

		this.id = element.getAttribute("key");
		this.name = element.getAttribute("name");
		this.defaultValue = element.getAttribute("defaultValue");
		this.value = null;

		if (this.id == null || this.name == null)
		{
			log.error("GenericExtensionOption() - invalid id or name - id: " + id + ", name: " + name + ", element: "
					+ element, new Throwable());
			throw new IllegalArgumentException("invalid id or name in option element");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.imports.api.imports.IImportToolAdapterOption#getId()
	 */
	@Override
	public String getId()
	{
		return id;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.imports.api.imports.IImportToolAdapterOption#getName()
	 */
	@Override
	public String getName()
	{
		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.imports.api.imports.IImportToolAdapterOption#getDefaultValue()
	 */
	@Override
	public String getDefaultValue()
	{
		return defaultValue;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.imports.api.imports.IImportToolAdapterOption#getValue()
	 */
	@Override
	public String getValue()
	{
		if (value != null)
			return value;
		else
			return defaultValue;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.imports.api.imports.IImportToolAdapterOption#setValue(java.lang.String)
	 */
	@Override
	public void setValue(String value)
	{
		if (log.isTraceEnabled())
			log.trace("setValue(): " + value);

		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "GenericExtensionOption[id: " + id + ", name: " + name + ", defaultValue: " + defaultValue + ", value: "
				+ value + "]";
	}
}
