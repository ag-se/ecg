package org.electrocodeogram.cpc.notification.preferences;


import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.electrocodeogram.cpc.notification.CPCNotificationPlugin;


public class CPCNotificationPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{

	public CPCNotificationPreferencePage()
	{
		super(GRID);
		setPreferenceStore(CPCNotificationPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.CPCNotificationPreferencePage_title);
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
