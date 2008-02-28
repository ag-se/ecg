package org.electrocodeogram.cpc.ui.preferences;


import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.electrocodeogram.cpc.ui.CPCUiPlugin;


/**
 * Allow users to customise the appearance of the CPC UI ruler.
 * 
 * @author vw
 */
public class CPCUIRulerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{

	public CPCUIRulerPreferencePage()
	{
		super(GRID);
		setPreferenceStore(CPCUiPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.CPCUIRulerPreferencePage_title);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	public void createFieldEditors()
	{
		addField(new ScaleFieldEditor(CPCPreferenceConstants.PREF_UI_RULER_WIDTH, Messages.CPCUIRulerPreferencePage_rulerWidth,
				getFieldEditorParent(), 1, 15, 1, 1));

		addField(new ColorFieldEditor(CPCPreferenceConstants.PREF_UI_RULER_COLOUR_BACKGROUND, Messages.CPCUIRulerPreferencePage_backgroundColour,
				getFieldEditorParent()));
		addField(new ColorFieldEditor(CPCPreferenceConstants.PREF_UI_RULER_COLOUR_MIXED, Messages.CPCUIRulerPreferencePage_mixedStateColour,
				getFieldEditorParent()));
		addField(new ColorFieldEditor(CPCPreferenceConstants.PREF_UI_RULER_COLOUR_SYNC, Messages.CPCUIRulerPreferencePage_inSyncColour,
				getFieldEditorParent()));
		addField(new ColorFieldEditor(CPCPreferenceConstants.PREF_UI_RULER_COLOUR_MODIFIED, Messages.CPCUIRulerPreferencePage_modifiedColour,
				getFieldEditorParent()));
		addField(new ColorFieldEditor(CPCPreferenceConstants.PREF_UI_RULER_COLOUR_NOTIFY, Messages.CPCUIRulerPreferencePage_notifyColour,
				getFieldEditorParent()));
		addField(new ColorFieldEditor(CPCPreferenceConstants.PREF_UI_RULER_COLOUR_WARN, Messages.CPCUIRulerPreferencePage_warnColour,
				getFieldEditorParent()));
		addField(new ColorFieldEditor(CPCPreferenceConstants.PREF_UI_RULER_COLOUR_IGNORE, Messages.CPCUIRulerPreferencePage_ignoredColour,
				getFieldEditorParent()));
		addField(new ColorFieldEditor(CPCPreferenceConstants.PREF_UI_RULER_COLOUR_ORPHAN, Messages.CPCUIRulerPreferencePage_orphanColour,
				getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench)
	{
	}

}
