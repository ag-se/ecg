package org.electrocodeogram.cpc.exports.ui.wizards;


import org.electrocodeogram.cpc.importexport.wizards.AbstractWelcomeWizardPage;


public class WelcomeWizardPage extends AbstractWelcomeWizardPage
{
	public WelcomeWizardPage()
	{
		super("Intro", "Welcome to the CPC Clone Data Export Wizard.",
				"The CPC Clone Data Export Wizard allows you to use one of the installed\n"
						+ "clone export plugins to export all clone data for a given set of projects.");
	}

}
