package org.electrocodeogram.cpc.ui.preferences;


import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.electrocodeogram.cpc.ui.CPCUiPlugin;


public class CPCUIPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{

	public CPCUIPreferencePage()
	{
		super(GRID);
		setPreferenceStore(CPCUiPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.CPCUIPreferencePage_title);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench)
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors()
	{
	}

}
