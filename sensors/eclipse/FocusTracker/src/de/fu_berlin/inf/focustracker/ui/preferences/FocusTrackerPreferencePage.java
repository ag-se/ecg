package de.fu_berlin.inf.focustracker.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import de.fu_berlin.inf.focustracker.FocusTrackerPlugin;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class FocusTrackerPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public FocusTrackerPreferencePage() {
		super(GRID);
		setPreferenceStore(FocusTrackerPlugin.getDefault().getPreferenceStore());
		setDescription("FocusTracker Preferences");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		addField(new IntegerFieldEditor(PreferenceConstants.P_ECG_EXPORT_INTERVAL, "ECG Export &Interval (in seconds)", getFieldEditorParent()));
		addField(new ProbabilityFieldEditor(PreferenceConstants.P_ECG_EXPORT_MIN_PROBABILITY_FOR_APPEARANCE, "ECG Export mininum &probability where elements should be considered as visible", getFieldEditorParent()));
		addField(new ProbabilityFieldEditor(PreferenceConstants.P_ECG_EXPORT_MIN_PROBABILITY_FOR_DISAPPEARANCE, "ECG Export &probability where already exported elements should be considered as invisible", getFieldEditorParent()));

		addField(new IntegerFieldEditor(PreferenceConstants.P_USER_INACTIVITY_DETECTION_TIMEOUT, "Inactivity detection timeout (in seconds)", getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.P_ENABLE_JAVA_EDITOR_MOUSE_MOVE_LISTENER, "Enable the JavaEditor MouseMoveListener (experimental)", getFieldEditorParent()));
		
		// link...
		createPreferenceLink((IWorkbenchPreferenceContainer) getContainer(), getFieldEditorParent(), "org.eclipse.ui.preferencePages.Decorators", "See <a>''{0}''</a> to enable or disable decorations."); // 
        
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	public static PreferenceLinkArea createPreferenceLink(IWorkbenchPreferenceContainer container, Composite parent, String pageId, String text) {
        final PreferenceLinkArea area = new PreferenceLinkArea(parent, SWT.NONE, pageId, text, container, null);
        return area;
	}
	
	
}