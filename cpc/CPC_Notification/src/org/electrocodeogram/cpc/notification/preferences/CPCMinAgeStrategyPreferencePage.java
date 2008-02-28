package org.electrocodeogram.cpc.notification.preferences;


import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.electrocodeogram.cpc.notification.CPCNotificationPlugin;


public class CPCMinAgeStrategyPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{

	public CPCMinAgeStrategyPreferencePage()
	{
		super(GRID);
		setPreferenceStore(CPCNotificationPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.CPCMinAgeStrategyPreferencePage_title);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	public void createFieldEditors()
	{
		addField(new IntegerFieldEditor(CPCPreferenceConstants.PREF_NOTIFICATION_STRATEGY_MINAGE_MINAGEINHOURS,
				Messages.CPCMinAgeStrategyPreferencePage_option_minAgeinhours, getFieldEditorParent()));

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench)
	{
	}

}
