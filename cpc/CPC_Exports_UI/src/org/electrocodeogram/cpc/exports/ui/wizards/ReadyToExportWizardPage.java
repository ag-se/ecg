package org.electrocodeogram.cpc.exports.ui.wizards;


import org.eclipse.core.resources.IProject;
import org.electrocodeogram.cpc.importexport.api.generic.IGenericExtensionOption;
import org.electrocodeogram.cpc.importexport.wizards.AbstractReadyToImportExportWizardPage;


public class ReadyToExportWizardPage extends AbstractReadyToImportExportWizardPage
{
	public ReadyToExportWizardPage()
	{
		super("Ready to Export", "All required data has been collected, the export process may now be started.");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.importexport.wizards.AbstractReadyToImportExportWizardPage#getBodyText()
	 */
	@Override
	public String getBodyText()
	{
		ExportClonesWizard wizard = (ExportClonesWizard) getWizard();

		StringBuilder cfgStr = new StringBuilder();
		cfgStr.append("The following export configuration will be used.\n\n");

		cfgStr.append("Projects to export:\n");
		for (IProject project : wizard.getSelectedProjects())
		{
			cfgStr.append("  * ");
			cfgStr.append(project.getName());
			cfgStr.append("\n");
		}

		cfgStr.append("\nExport Tool Adapter Implementation:\n  * ");
		cfgStr.append(wizard.getSelectedToolAdapter().getName());

		cfgStr.append("\n\nExport Tool Adapter Options:\n");
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
