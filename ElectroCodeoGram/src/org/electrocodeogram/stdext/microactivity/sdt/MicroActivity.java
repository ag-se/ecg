package org.electrocodeogram.stdext.microactivity.sdt;

import java.util.ArrayList;
import java.util.List;

import org.hackystat.kernel.sdt.SensorDataType;
import org.hackystat.kernel.sensordata.SensorDataEntry;

/**
 * Provides the wrapper class for Activity sensor data.
 * <ul>
 *   <li> entryArray[0] is the activity sdt name.
 *   <li> entryArray[1] is the timestamp string.
 *   <li> entryArray[2] is the tool string.
 *   <li> entryArray[3] is the activity type name string.
 *   <li> entryArray[4] is the data string.
 *   <li> entryArray[5] is the timestamp Date instance.
 *   <li> entryArray[6] is the ActivityType instance.
 * </ul>
 *
 *
 * @author    Philip Johnson
 * @version   $Id: Activity.java,v 1.2 2004/05/17 20:29:49 hongbing Exp $
 */
public class MicroActivity extends SensorDataEntry {

  /**
   * Invoked implicitly by the SensorData mechanism to create a new Activity
   * instance.
   *
   * @param entryArray  The string array of Activity field data.
   * @param sdt         The SensorDataType instance associated with this
   *      activity
   */
  public MicroActivity(Object[] entryArray, SensorDataType sdt) {
    this.entryArray = entryArray;
    this.sdt = sdt;
  }


  /**
   * Returns the data (typically a single file name or a comma-delimited list of
   * files) associated with this Activity.
   *
   * @return   A string containing data associated with this activity.
   */
  public final String getData() {
    return (String) this.entryArray[4];
  }

  /**
   * Makes list of files associated this data entry.
   * 
   * @return List of files associated with this sensor data entry. 
   */
  public List getFilePaths() {
    if (getData() == null || getData().length() == 0) {
      return new ArrayList();
    }
    else {
      List filePaths = new ArrayList();      
      filePaths.add(getData());
      return filePaths;
    }
  }

  /**
   * Returns the type of activity associated with this Activity.
   *
   * @return   The activity type.
   */
  public final MicroActivityType getActivityType() {
    return (MicroActivityType) this.entryArray[6];
  }


  /**
   * Returns a non-empty string listing errors found, or else null if no errors.
   *
   * @return   A string if errors, or null if everything's cool.
   */
  public String getErrors() {
    String errorMessage = "";
    if (getTimestamp() == null) {
      errorMessage += "Invalid timestamp; ";
    }
    if (getTool() == null) {
      errorMessage += "Invalid tool; ";
    }
    if (getActivityType() == null) {
      errorMessage += "Invalid activity type; ";
    }
    if (getData() == null) {
      errorMessage += "Invalid data; ";
    }
    return (errorMessage == "") ? null : errorMessage;
  }
}




