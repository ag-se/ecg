package org.electrocodeogram.cpc.classifier.preferences;


import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.electrocodeogram.cpc.classifier.CPCClassifierPlugin;


public class CPCClassifierPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{

	public CPCClassifierPreferencePage()
	{
		super(GRID);
		setPreferenceStore(CPCClassifierPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.ClassifierPreferencePage_title);
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
