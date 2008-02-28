package org.electrocodeogram.cpc.similarity.preferences;


import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.electrocodeogram.cpc.similarity.CPCSimilarityPlugin;


public class CPCJavaCodeNormalisingStrategyPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage
{

	public CPCJavaCodeNormalisingStrategyPreferencePage()
	{
		super(GRID);
		setPreferenceStore(CPCSimilarityPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.CPCJavaCodeNormalisingStrategyPreferencePage_title);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	public void createFieldEditors()
	{
		addField(new BooleanFieldEditor(
				CPCPreferenceConstants.PREF_SIMILARITY_STRATEGY_JAVACODENORMALISING_STRIPCOMMENTS,
				Messages.CPCJavaCodeNormalisingStrategyPreferencePage_option_stripComments, getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench)
	{
	}

}
