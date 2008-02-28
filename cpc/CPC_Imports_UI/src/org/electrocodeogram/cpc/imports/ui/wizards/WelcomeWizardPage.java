package org.electrocodeogram.cpc.imports.ui.wizards;


import org.electrocodeogram.cpc.importexport.wizards.AbstractWelcomeWizardPage;


/**
 * Simple welcome page which displays a short intro text which explains important
 * points which the user should consider before importing static clone data.
 * 
 * @author vw
 */
public class WelcomeWizardPage extends AbstractWelcomeWizardPage
{
	public WelcomeWizardPage()
	{
		super("Intro", "Welcome to the CPC Static Clone Data Import Wizard.",
				"The CPC Static Clone Data Import Wizard allows you to use one of the installed\n"
						+ "static clone detection plugins to initialise the clone database for a project.\n"
						+ "This is a good idea if you're introducing CPC to an existing project.\n"
						+ "If the project is still in a very early development phase importing clone data\n"
						+ "is not recommended.");
	}
}
