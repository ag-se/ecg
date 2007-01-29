package de.fu_berlin.inf.atl.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import de.fu_berlin.inf.atl.ATLPlugin;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. 
 */

public class ATLPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public ATLPreferencePage() {
		super(GRID);
		setPreferenceStore(ATLPlugin.getDefault().getPreferenceStore());
		setDescription("You need to restart the workspace for changes to take effect.");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		addField(
			new BooleanFieldEditor(
				PreferenceConstants.P_ENABLED,
				"&Enable Application Title Logger",
				getFieldEditorParent()));
		addField(
			new IntegerFieldEditor(
                PreferenceConstants.P_INTERVAL,
                "&Update Interval:", 
                getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}