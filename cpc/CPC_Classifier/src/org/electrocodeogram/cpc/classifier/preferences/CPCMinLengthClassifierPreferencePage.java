package org.electrocodeogram.cpc.classifier.preferences;


import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.electrocodeogram.cpc.classifier.CPCClassifierPlugin;


public class CPCMinLengthClassifierPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{

	public CPCMinLengthClassifierPreferencePage()
	{
		super(GRID);
		setPreferenceStore(CPCClassifierPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.CPCMinLengthClassifierPreferencePage_title);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	public void createFieldEditors()
	{
		addField(new IntegerFieldEditor(CPCPreferenceConstants.PREF_STRATEGY_MINLENGTH_CHARLEN,
				Messages.CPCMinLengthClassifierPreferencePage_option_charLen, getFieldEditorParent()));

		addField(new IntegerFieldEditor(CPCPreferenceConstants.PREF_STRATEGY_MINLENGTH_TOKENLEN,
				Messages.CPCMinLengthClassifierPreferencePage_option_tokenLen, getFieldEditorParent()));

		addField(new IntegerFieldEditor(CPCPreferenceConstants.PREF_STRATEGY_MINLENGTH_LINECOUNT,
				Messages.CPCMinLengthClassifierPreferencePage_option_lineCount, getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench)
	{
	}

}
