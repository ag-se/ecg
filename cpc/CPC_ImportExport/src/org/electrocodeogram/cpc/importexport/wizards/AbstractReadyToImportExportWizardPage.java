package org.electrocodeogram.cpc.importexport.wizards;


import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;


public abstract class AbstractReadyToImportExportWizardPage extends WizardPage
{
	protected Text confirmConfigText;

	public AbstractReadyToImportExportWizardPage(String title, String description)
	{
		super(title);
		setTitle(title);
		setDescription(description);
		setPageComplete(false);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent)
	{
		confirmConfigText = new Text(parent, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL);
		confirmConfigText.setText("...");
		confirmConfigText.setEditable(false);

		setControl(confirmConfigText);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible)
	{
		confirmConfigText.setText(getBodyText());

		setPageComplete(true);
		super.setVisible(visible);
	}

	/**
	 * @return the body text to display in the main text field, never null.
	 */
	protected abstract String getBodyText();
}
