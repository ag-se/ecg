package org.electrocodeogram.cpc.importexport.wizards;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.electrocodeogram.cpc.importexport.api.wizards.IImportExportExtensionOptionWizardPage;
import org.electrocodeogram.cpc.importexport.api.wizards.IImportExportExtensionOptionWizardPageDescriptor;


/**
 * Default implementation for {@link IImportExportExtensionOptionWizardPageDescriptor}.
 * 
 * @author vw
 */
public class ImportExportExtensionOptionWizardPageDescriptor implements
		IImportExportExtensionOptionWizardPageDescriptor
{
	private static final Log log = LogFactory.getLog(ImportExportExtensionOptionWizardPageDescriptor.class);

	public static final String ELEMENT_NAME = "optionPage";

	protected String name;
	protected String clazz;
	protected IConfigurationElement element;

	public ImportExportExtensionOptionWizardPageDescriptor(IConfigurationElement element)
			throws IllegalArgumentException
	{
		if (log.isTraceEnabled())
			log.trace("ImportExportExtensionOptionWizardPageDescriptor() - element: " + element);

		if (element == null || !element.getName().equals(ELEMENT_NAME))
		{
			log.error("ImportExportExtensionOptionWizardPageDescriptor() - illegal element - element: " + element,
					new Throwable());
			throw new IllegalArgumentException("illegal element");
		}

		this.name = element.getAttribute("name");
		this.clazz = element.getAttribute("class");
		this.element = element;

		if (name == null || clazz == null)
		{
			log.error("ImportExportExtensionOptionWizardPageDescriptor() - illegal name or class - name: " + name
					+ ", class: " + clazz + ", element: " + element, new Throwable());
			throw new IllegalArgumentException("illegal name or class");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.api.wizards.IImportExportExtensionOptionWizardPageDescriptor#getId()
	 */
	@Override
	public String getId()
	{
		return clazz;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.api.wizards.IImportExportExtensionOptionWizardPageDescriptor#getName()
	 */
	@Override
	public String getName()
	{
		return name;
	}

	/**
	 * Creates and returns an instance of the {@link IImportExportExtensionOptionWizardPage} implementation
	 * which is represented by this descriptor.
	 */
	public IImportExportExtensionOptionWizardPage getInstance() throws CoreException
	{
		log.trace("getInstance()");

		return (IImportExportExtensionOptionWizardPage) element.createExecutableExtension("class");
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "ImportExportExtensionOptionWizardPageDescriptor[id: " + clazz + ", name: " + name + ", element: "
				+ element + "]";
	}
}
