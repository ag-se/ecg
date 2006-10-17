package org.electrocodeogram.sensor.eclipse.preferences;

import org.electrocodeogram.logging.LogHelper;
import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.kernel.shell.SensorShell;

/**
 * Constant definitions for plug-in preferences
 */
public class ECGPreferenceConstants {

    /**
     * This <code>String</code> is the key for the property
     * which decides whether the plugin's properties or
     * the sensor.properties should be taken for the configuration
     * If the value behind the key is "true" than the plugin specific
     * configuration will be taken 
     */
    public static final String P_ECLIPSE_PROPERTIES = "ECG_ECLIPSE_PROPERTIES";

    // The following keys correspond to the ones of the sensor.properties
    public static final String P_SERVER_TYPE = SensorProperties.ECG_SERVER_TYPE_KEY;
    public static final String P_SERVER_TYPE_NULL = SensorShell.ServerMode.NULL.toString();
    public static final String P_SERVER_TYPE_INLINE = SensorShell.ServerMode.INLINE.toString();
    public static final String P_SERVER_TYPE_REMOTE = SensorShell.ServerMode.REMOTE.toString();
    public static final String P_SERVER_ADDRESS = SensorProperties.ECG_SERVER_ADDRESS_KEY;
    public static final String P_SERVER_PORT = SensorProperties.ECG_SERVER_PORT_KEY;
    public static final String P_SERVER_BATCH = SensorProperties.ECG_SERVER_BATCH_KEY;
    public static final String P_LOG_LEVEL = SensorProperties.ECG_LOG_LEVEL_KEY;
    public static final String P_LOG_LEVEL_DEBUG = LogHelper.LEVEL_DEBUG;
    public static final String P_LOG_LEVEL_ERROR = LogHelper.LEVEL_ERROR;
    public static final String P_LOG_LEVEL_INFO = LogHelper.LEVEL_INFO;
    public static final String P_LOG_LEVEL_OFF = LogHelper.LEVEL_OFF;
    public static final String P_LOG_LEVEL_PACKET = LogHelper.LEVEL_PACKET;
    public static final String P_LOG_LEVEL_VERBOSE = LogHelper.LEVEL_VERBOSE;
    public static final String P_LOG_LEVEL_WARNING = LogHelper.LEVEL_WARNING;
    public static final String P_LOG_FILE = SensorProperties.ECG_LOG_FILE_KEY;

	
}
