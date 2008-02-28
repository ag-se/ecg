package org.electrocodeogram.cpc.core.preferences;


import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.electrocodeogram.cpc.core.CPCCorePlugin;


/**
 * CPC root preference page. Has no content.
 * 
 * @author vw
 */
public class CPCPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{

	public CPCPreferencePage()
	{
		super(GRID);
		setPreferenceStore(CPCCorePlugin.getDefault().getPreferenceStore());
		setDescription(Messages.CPCPreferencePage_title + "\n\nVersion: "
				+ CPCCorePlugin.getDefault().getBundle().getHeaders().get("Bundle-Version")
				+ "\n\nhttp://cpc.anetwork.de");
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
