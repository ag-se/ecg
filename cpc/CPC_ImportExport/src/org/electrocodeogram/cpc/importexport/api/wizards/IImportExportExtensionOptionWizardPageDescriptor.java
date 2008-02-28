package org.electrocodeogram.cpc.importexport.api.wizards;


import org.electrocodeogram.cpc.importexport.wizards.AbstractImportExportClonesWizard;


/**
 * A descriptor which represents an {@link IImportExportExtensionOptionWizardPage} implementation. 
 * 
 * @author vw
 * 
 * @see IImportExportExtensionOptionWizardPage
 * @see AbstractImportExportClonesWizard
 */
public interface IImportExportExtensionOptionWizardPageDescriptor
{
	/**
	 * A unique identifier for the {@link IImportExportExtensionOptionWizardPage} which is represented
	 * by this descriptor.
	 * 
	 * @return id of this option page, never null.
	 */
	public String getId();

	/**
	 * A human readable name for this option page.
	 * 
	 * @return name of option page, never null.
	 */
	public String getName();
}
