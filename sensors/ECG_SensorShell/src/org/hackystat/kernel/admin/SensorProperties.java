package org.hackystat.kernel.admin;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * Provides access to the hackystat properties file for each sensor, and provides reasonable default
 * values when properties file is missing, cannot be read, or lacks property values. <p>
 *
 * The sensor properties file is stored in userhome/.hackystat/sensor.properties. It is a
 * user-maintained file of key=value pairs. All values are trim()'d to prevent whitespace from
 * introducing bugs.
 *
 * @author    Philip M. Johnson
 * @version   $Id: SensorProperties.java,v 1.8 2004/11/28 23:55:59 johnson Exp $
 */
public class SensorProperties {

  /** Hackystat host */
  private static final String HOST_KEY = "HACKYSTAT_HOST";
  /** User's dirKey */
  private static final String USER_KEY = "HACKYSTAT_KEY";
  /** Auto send interval  */
  private static final String AUTOSEND_KEY = "HACKYSTAT_AUTOSEND_INTERVAL";
  /** State change interval */
  private static final String STATECHANGE_KEY = "HACKYSTAT_STATE_CHANGE_INTERVAL";

  /** The standard location of the sensor properties file. */
  private File sensorFile;
  /** The sensor type creating this instance. */
  private String sensorType = "";
  /** Whether the properties file exists and was successfully read. */
  private boolean fileAvailable = false;
  /** The internal properties object. */
  private Properties sensorProps = new Properties();
  /** Indicates whether this sensor is active or not. */
  private boolean sensorEnabled = false;


  /**
   * Initializes access to the Hackystat Sensor settings stored in the user's sensor.properties
   * file.
   *
   * @param sensorType  The type of sensor requesting information. For example, "JUnit", "Emacs",
   *      etc. The string is uppercased internally so that case doesn't matter.
   */
  public SensorProperties(String sensorType) {
    this(sensorType.toUpperCase(), new File(System.getProperty("user.home") +
        "/.hackystat/sensor.properties"));
  }

  /**
   * Creates a "minimal" sensor properties file usable for test case purposes. Needed by SensorShell
   * which must be passed a SensorProperties object containing a host and key.
   *
   * @param host  The hackystat host.
   * @param key   The user's 12 character key.
   */
  public SensorProperties(String host, String key) {
    sensorProps.setProperty(SensorProperties.HOST_KEY, host);
    sensorProps.setProperty(SensorProperties.USER_KEY, key);
    this.fileAvailable = false;
    this.sensorEnabled = false;
  }

  /**
   * Provides access to Hackystat Sensor settings by reading the passed file.
   *
   * @param sensorType  The type of sensor requesting information, such as "JUnit", etc.
   * @param sensorFile  The sensor file to read.
   */
  public SensorProperties(String sensorType, File sensorFile) {
    this.sensorFile = sensorFile;
    this.sensorType = sensorType.toUpperCase();
    try {
      if (sensorFile.exists()) {
        sensorProps.load(new FileInputStream(sensorFile));
        if (sensorProps.size() > 0) {
          this.fileAvailable = true;
          String enableKey = "ENABLE_" + this.sensorType + "_SENSOR";
          this.sensorEnabled = sensorProps.getProperty(enableKey, "false").trim().equals("true");
        }
      }
    }
    catch (Exception e) {
      this.fileAvailable = false;
      this.sensorEnabled = false;
    }
  }

  public InetAddress getECGServerAddress() throws UnknownHostException
  {
      String ECGServerAddressKey = "ECG_SERVER_ADDRESS";
      String str = this.getProperty(ECGServerAddressKey).trim();
      return InetAddress.getByName(str);
    
  }

  public int getECGServerPort()
  {
      String ECGServerPortKey = "ECG_SERVER_PORT";
      String str = this.getProperty(ECGServerPortKey).trim();
      
      return Integer.parseInt(str);
  }

  public String getECGServerType()
  {
      String ECGServerType = "ECG_SERVER_TYPE";
      String str = this.getProperty(ECGServerType).trim();
      
      return str;
  }
  
  public String getECGServerPath()
  {
      String ECGServerPath = "ECG_SERVER_PATH";
      String str = this.getProperty(ECGServerPath).trim();
      
      return str;
  }

  
  /**
   * Returns the directory in which the sensor.properties file is located (if it exists). This is
   * normally the .hackystat directory. If this SensorProperties instance was created without a
   * sensor.properties file, or if for some other reason the sensor.properties file cannot be found,
   * then this method returns null.
   *
   * @return   A File instance indicating a directory, or null.
   */
  public File getSensorPropertiesDir() {
    if ((sensorFile != null) && (sensorFile.exists())) {
      return sensorFile.getParentFile();
    }
    else {
      return null;
    }
  }


  /**
   * Returns true if the supplied property is set to true in this sensor properties.
   *
   * @param sensorType  A string such as "Eclipse" or "Eclipse_UnitTest".
   * @return            True if this property is enabled.
   */
  public boolean isSensorTypeEnabled(String sensorType) {
    String enableKey = "ENABLE_" + sensorType.toUpperCase() + "_SENSOR";
    return sensorProps.getProperty(enableKey, "false").trim().equals("true");
  }
  
  /**
   * Returns the property value associated with the property key in this sensor.properties file.
   * @param key The parameter key.
   * @return The property value associated with this property key, or null if not found.
   */
  public String getProperty(String key) {
    return sensorProps.getProperty(key);
  }


  /**
   * Returns true if the sensor corresponding to these properties should be used.
   *
   * @return   True if the sensor is enabled.
   */
  public boolean isSensorEnabled() {
    return this.sensorEnabled;
  }


  /**
   * Returns the hackystat host. Defaults to http://localhost/.
   *
   * @return   The hackystat host.
   */
  public String getHackystatHost() {
    String host = sensorProps.getProperty(HOST_KEY, "http://localhost/").trim(); 
    if (!host.endsWith("/")) {
      host = host + "/";
    }
    return host;
  }


  /**
   * Returns the absolute path to the properties file, or the empty string if the file is not
   * available.
   *
   * @return   The absolutePath value or the empty string.
   */
  public String getAbsolutePath() {
    return (this.sensorFile == null) ? "" : this.sensorFile.getAbsolutePath();
  }


  /**
   * Returns the 12 character key for this user. Defaults to "ChangeThis"
   *
   * @return   The 12 character key
   */
  public String getKey() {
    return sensorProps.getProperty(USER_KEY, "ChangeThis").trim();
  }

  /**
   * Returns the AutoSend interval for use by the SensorShell, or 0 if it was not specified. This is
   * returned as a string since it is typically sent off to the SensorShell as a String argument.
   *
   * @return   The autosend interval.
   */
  public String getAutoSendInterval() {
    String intervalString = sensorProps.getProperty(AUTOSEND_KEY, "0").trim();
    try {
      // make sure it's an integer.
      Integer.parseInt(intervalString);
      return intervalString;
    }
    catch (Exception e) {
      return "0";
    }
  }

  /**
   * Returns the StateChange interval for use by sensors, or 30 if it was not specified. The
   * stateChange interval must be greater than 0.
   *
   * @return   The state change interval in seconds.
   */
  public int getStateChangeInterval() {
    String intervalString = sensorProps.getProperty(STATECHANGE_KEY, "60").trim();
    try {
      int interval = Integer.parseInt(intervalString);
      return (interval > 0) ? interval : 30;
    }
    catch (Exception e) {
      return 30;
    }
  }


  /**
   * Returns true if the sensor properties file was found and read successfully.
   *
   * @return   True if file was found and readable.
   */
  public boolean isFileAvailable() {
    return this.fileAvailable;
  }


  /**
   * Returns true if the hackystat server can be found and is responding to Hackystat SOAP requests,
   * false otherwise.
   *
   * @return   True if the hackystat server could be contacted, false otherwise.
   */
//  public boolean isHostAlive() {
//    boolean success = true;
//    try {
//      Notification.send(getHackystatHost(), getKey(), Notification.PING, "");
//    }
//    catch (Exception e) {
//      success = false;
//      //redundant, but clear.
//    }
//    return success;
//  }
}

