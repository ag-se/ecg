package org.electrocodeogram.cpc.similarity.preferences;


import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.electrocodeogram.cpc.similarity.CPCSimilarityPlugin;


public class CPCSimilarityPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{

	public CPCSimilarityPreferencePage()
	{
		super(GRID);
		setPreferenceStore(CPCSimilarityPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.CPCSimilarityPreferencePage_title);
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
