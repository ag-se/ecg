package org.electrocodeogram.cpc.importexport.wizards;


import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.electrocodeogram.cpc.importexport.api.wizards.IImportExportExtensionOptionWizardPage;


public abstract class AbstractImportExportExtensionOptionWizardPage extends WizardPage implements
		IImportExportExtensionOptionWizardPage
{
	protected Map<String, String> optionMap = null;

	public AbstractImportExportExtensionOptionWizardPage(String title, String description)
	{
		super(title);
		setTitle(title);
		setDescription(description);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.api.wizards.IImportExportExtensionOptionWizardPage#init(java.util.Map)
	 */
	@Override
	public void init(Map<String, String> optionMap)
	{
		assert (optionMap != null);

		this.optionMap = optionMap;
	}

}
