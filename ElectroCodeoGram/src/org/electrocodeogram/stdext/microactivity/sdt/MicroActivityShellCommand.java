package org.electrocodeogram.stdext.microactivity.sdt;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.hackystat.kernel.sensordata.SensorDataEntryFactory;
import org.hackystat.kernel.shell.OfflineManager;
import org.hackystat.kernel.shell.SensorShell;
import org.hackystat.kernel.shell.command.ShellCommandAdapter;

/**
 * Implements the "Activity" related commands for SensorShell.
 *
 * @author    Philip M. Johnson
 * @version   $Id: ActivityShellCommand.java,v 1.4 2003/01/21 18:16:22 johnson
 *      Exp $
 */
public class MicroActivityShellCommand extends ShellCommandAdapter {
  /** Last file name */
  private String lastFileName = null;
  /** Last buffer size */
  private String lastBufferSize = null;


  /**
   * Returns a string describing the commands available in this command shell.
   *
   * @return   The help string.
   */
  public String getHelpString() {
    return
        "Activity#add#<ActivityType>#<Data>" + cr
        + " Adds the specified activity type and corresponding data." + cr
        + "Activity#setTool#<tool>" + cr
        + "  Specifies the tool that generated the activities in this session." + cr
        + "Activity#statechange#<filename>#<buffersize>" + cr
        + "  Adds an activity if the <buffersize> associated with <filename> has changed." + cr;
  }


  /**
   * Processes the statechange command. Creates a new Activity instance if the
   * lastFileName is the same as the current one but the buffer size has
   * changed. Sets resultMessage to an Error string if the two arguments are not
   * supplied. Note that the first statechange event just initializes the state
   * and never creates an activity instance.
   *
   * @param timeStamp  The time at which this command occurred.
   * @param argList    The SensorShell arguments.
   * @param shell      The sensorshell (ignored).
   * @return           True if the statechange command was understood
   *      successfully. Will return true even if no actual state change took
   *      place as long as it was understood.
   */
  public boolean statechange(Date timeStamp, List argList, SensorShell shell) {
    try {
      // Start by making sure fileName and bufferSize are non-empty strings.
      String fileName = (String) argList.get(1);
      String bufferSize = (String) argList.get(2);
      if ((fileName.equals("")) || (bufferSize.equals(""))) {
        throw new IllegalArgumentException("Empty file name or buffer size.");
      }

      // If the first time, just initialize the state change data.
      if (this.lastFileName == null) {
        this.lastFileName = fileName;
        this.lastBufferSize = bufferSize;
        this.resultMessage = "Activity statechange OK (initial file name and buffer size)";
      }
      // Otherwise, check for a change to the file we saw during the last statechange.
      else {
        if ((fileName.equals(this.lastFileName)) && (!bufferSize.equals(this.lastBufferSize))) {
          // Generate the State Change activity and add it to the list.
          String[] entryAttributes = {String.valueOf(timeStamp.getTime()),
              this.tool,
              MicroActivityType.STATE_CHANGE.toString(),
              fileName};
          MicroActivity activity = (MicroActivity) SensorDataEntryFactory.getEntry(
              "Activity", Arrays.asList(entryAttributes));
          entryList.add(activity);
          // Only record real state changes as Activity add commands for offline storage.
          String[] changeArgs = {"add", "State Change", fileName};
          OfflineManager.getInstance().add(timeStamp, "Activity", Arrays.asList(changeArgs));
          this.resultMessage = "Activity statechange OK (added activity)";
        }
        else {
          this.resultMessage = "Activity statechange OK (no change)";
        }
        // Update the lastFileName and lastBufferSize
        this.lastFileName = fileName;
        this.lastBufferSize = bufferSize;
      }
    }
    catch (Exception e) {
      this.resultMessage = "Activity statechange error (invalid argument(s))" + cr + e;
      return false;
    }
    return true;
  }


  /**
   * Processes the add command. Creates a new Activity instance and sets the
   * result message to OK if the new activity instance was created successfully
   * and Error if the arguments do not allow successful creation of an Activity.
   *
   * @param timeStamp  The time at which this command occurred.
   * @param argList    The SensorShell arguments.
   * @param shell      The sensorshell (ignored).
   * @return           True if the add command was processed successfully.
   */
  public boolean add(Date timeStamp, List argList, SensorShell shell) {
    try {
      MicroActivityType activityType = MicroActivityType.getInstance((String) argList.get(1));
      // Data is optional for an add command.
      String data = (argList.size() == 3) ? (String) argList.get(2) : "";
      if (argList.size() > 3) {
        throw new IllegalArgumentException("Too many arguments passed to add command.");
      }
      // Activity SDT attributes are: <timestamp>, <tool>, <type>, <data>
      String[] entryAttributes = {String.valueOf(timeStamp.getTime()),
          this.tool,
          activityType.toString(),
          data};
      MicroActivity activity = (MicroActivity) SensorDataEntryFactory.getEntry("MicroActivity",
          Arrays.asList(entryAttributes));
      entryList.add(activity);
      // Record this for potential offline data storage.
      OfflineManager.getInstance().add(timeStamp, "Activity", argList);
      this.resultMessage = "Activity add OK (" + entryList.size() + " total)";
    }
    catch (Exception e) {
      this.resultMessage = "Activity add error (Invalid element(s)." + cr + argList + cr + e;
      return false;
    }
    return true;
  }

}



