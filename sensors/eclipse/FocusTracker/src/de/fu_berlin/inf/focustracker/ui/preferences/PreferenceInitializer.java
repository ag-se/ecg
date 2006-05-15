package de.fu_berlin.inf.focustracker.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.fu_berlin.inf.focustracker.FocusTrackerPlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/**
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = FocusTrackerPlugin.getDefault()
				.getPreferenceStore();
		store.setDefault(PreferenceConstants.P_ENABLE_JAVA_EDITOR_MOUSE_MOVE_LISTENER, false);
		store.setDefault(PreferenceConstants.P_USER_INACTIVITY_DETECTION_TIMEOUT, 60);
		
		store.setDefault(PreferenceConstants.P_ECG_EXPORT_INTERVAL, 10);
		store.setDefault(PreferenceConstants.P_ECG_EXPORT_MIN_PROBABILITY_FOR_APPEARANCE, 0.25d);
		store.setDefault(PreferenceConstants.P_ECG_EXPORT_MIN_PROBABILITY_FOR_DISAPPEARANCE, 0.1d);
	}

}
