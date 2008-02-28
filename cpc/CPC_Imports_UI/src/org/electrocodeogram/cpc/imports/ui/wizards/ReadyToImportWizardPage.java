package org.electrocodeogram.cpc.imports.ui.wizards;


import org.eclipse.core.resources.IProject;
import org.electrocodeogram.cpc.importexport.api.generic.IGenericExtensionOption;
import org.electrocodeogram.cpc.importexport.wizards.AbstractReadyToImportExportWizardPage;


public class ReadyToImportWizardPage extends AbstractReadyToImportExportWizardPage
{
	public ReadyToImportWizardPage()
	{
		super("Ready to Import", "All required data has been collected, the import process may now be started.");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.wizards.AbstractReadyToImportExportWizardPage#getBodyText()
	 */
	@Override
	public String getBodyText()
	{
		ImportClonesWizard wizard = (ImportClonesWizard) getWizard();

		StringBuilder cfgStr = new StringBuilder();
		cfgStr.append("The following import configuration will be used.\n\n");

		cfgStr.append("Projects to import:\n");
		for (IProject project : wizard.getSelectedProjects())
		{
			cfgStr.append("  * ");
			cfgStr.append(project.getName());
			cfgStr.append("\n");
		}

		cfgStr.append("\nImport Tool Adapter Implementation:\n  * ");
		cfgStr.append(wizard.getSelectedToolAdapter().getName());

		cfgStr.append("\n\nDelete existing clone data on import: ");
		cfgStr.append(wizard.isClearExistingClonesEnabled() ? "Yes" : "No");

		cfgStr.append("\n\nImport Tool Adapter Options:\n");
		for (IGenericExtensionOption option : wizard.getOptionList())
		{
			cfgStr.append("  * ");
			cfgStr.append(option.getName());
			cfgStr.append(": ");
			cfgStr.append(option.getValue());
			cfgStr.append("\n");
		}

		return cfgStr.toString();
	}
}
