package org.electrocodeogram.sensor.eclipse.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.electrocodeogram.sensor.eclipse.ECGEclipseSensor;

import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.stdext.sensor.eclipse.EclipseSensorPlugin;

/**
 * Class used to initialize default preference values.
 */
public class ECGPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore wsProperties = EclipseSensorPlugin.getInstance()
				.getPreferenceStore();
        SensorProperties shellProps = ECGEclipseSensor.getInstance().getShellProperties();
        
        wsProperties.setDefault(
                ECGPreferenceConstants.P_ECLIPSE_PROPERTIES, 
                false);
        wsProperties.setDefault(
                ECGPreferenceConstants.P_SERVER_TYPE, 
                shellProps.getECGServerType());
        wsProperties.setDefault(
                ECGPreferenceConstants.P_SERVER_ADDRESS, 
                shellProps.getECGServerAddressAsString());
        wsProperties.setDefault(
                ECGPreferenceConstants.P_SERVER_PORT, 
                shellProps.getECGServerPort());
        wsProperties.setDefault(
                ECGPreferenceConstants.P_SERVER_BATCH, 
                shellProps.getECGServerBatch());
        wsProperties.setDefault(
                ECGPreferenceConstants.P_LOG_FILE, 
                shellProps.getProperty(SensorProperties.ECG_LOG_FILE_KEY));
        wsProperties.setDefault(
                ECGPreferenceConstants.P_LOG_LEVEL, 
                shellProps.getProperty(SensorProperties.ECG_LOG_LEVEL_KEY));

	}

}
