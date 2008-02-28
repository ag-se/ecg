package org.electrocodeogram.cpc.importexport.wizards;


import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;


public class AbstractWelcomeWizardPage extends WizardPage
{
	protected String text = null;

	/**
	 * 
	 * @param title the page title, never null.
	 * @param description the header description, never null.
	 * @param text the body text, never null.
	 */
	public AbstractWelcomeWizardPage(String title, String description, String text)
	{
		super(title);
		assert (title != null && description != null && text != null);

		setTitle(title);
		setDescription(description);

		this.text = text;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent)
	{
		final Text welcomeText = new Text(parent, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.READ_ONLY);

		welcomeText.setText(text);
		welcomeText.setEditable(false);

		setControl(welcomeText);
	}

}
