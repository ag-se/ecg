package de.fu_berlin.inf.atl.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.fu_berlin.inf.atl.ATLPlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = ATLPlugin.getDefault()
				.getPreferenceStore();
		store.setDefault(PreferenceConstants.P_ENABLED, true);
		store.setDefault(PreferenceConstants.P_INTERVAL, 200);
	}

}
