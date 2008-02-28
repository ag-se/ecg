package org.electrocodeogram.cpc.importexport.generic;


import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.electrocodeogram.cpc.importexport.api.generic.IGenericExtensionOption;
import org.electrocodeogram.cpc.importexport.api.generic.IGenericImportExportDescriptor;
import org.electrocodeogram.cpc.importexport.api.wizards.IImportExportExtensionOptionWizardPageDescriptor;
import org.electrocodeogram.cpc.importexport.wizards.ImportExportExtensionOptionWizardPageDescriptor;


/**
 * Default implementation for {@link IGenericImportExportDescriptor}.<br/>
 * <br/>
 * The <em>CPC Imports</em> and <em>CPC Exports</em> modules define their own sub-classes.
 * 
 * @author vw
 */
public class GenericImportExportDescriptor implements IGenericImportExportDescriptor
{
	private static Log log = LogFactory.getLog(GenericImportExportDescriptor.class);

	protected IConfigurationElement element;
	protected String clazz;
	protected String name;
	protected List<IGenericExtensionOption> options;
	protected List<IImportExportExtensionOptionWizardPageDescriptor> optionPages;

	public GenericImportExportDescriptor(IConfigurationElement element) throws IllegalArgumentException
	{
		if (log.isTraceEnabled())
			log.trace("GenericImportExportDescriptor() - element: " + element);

		this.element = element;
		this.name = element.getAttribute("name");
		this.clazz = element.getAttribute("class");

		if (this.name == null || this.clazz == null)
			throw new IllegalArgumentException("invalid name or class attribute");

		/*
		 * parse option and option wizard page data, if present
		 */
		options = new LinkedList<IGenericExtensionOption>();
		optionPages = new LinkedList<IImportExportExtensionOptionWizardPageDescriptor>();

		for (IConfigurationElement subElement : element.getChildren())
		{
			try
			{
				if (subElement.getName().equals(GenericExtensionOption.ELEMENT_NAME))
				{
					options.add(new GenericExtensionOption(subElement));
				}
				else if (subElement.getName().equals(ImportExportExtensionOptionWizardPageDescriptor.ELEMENT_NAME))
				{
					optionPages.add(new ImportExportExtensionOptionWizardPageDescriptor(subElement));
				}
			}
			catch (IllegalArgumentException e)
			{
				log.error("GenericImportExportDescriptor() - illegal sub-element - " + subElement + " - " + e, e);
			}
		}

	}

	/*
	 * IGenericImportExportDescriptor methods.
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.api.generic.IGenericImportExportDescriptor#getId()
	 */
	@Override
	public String getId()
	{
		return clazz;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.api.generic.IGenericImportExportDescriptor#getName()
	 */
	@Override
	public String getName()
	{
		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.api.generic.IGenericImportExportDescriptor#getOptionDefinitions()
	 */
	@Override
	public List<IGenericExtensionOption> getOptionDefinitions()
	{
		return options;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.api.generic.IGenericImportExportDescriptor#getOptionPages()
	 */
	@Override
	public List<IImportExportExtensionOptionWizardPageDescriptor> getOptionPages()
	{
		return optionPages;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "GenericImportExportDescriptor[name: " + name + ", class: " + clazz + ", options: " + options
				+ ", optionPages: " + optionPages + "]";
	}

}
