package org.electrocodeogram.stdext.microactivity.sdt;

import junit.framework.TestCase;

import java.util.Date;
import java.util.Arrays;

import org.hackystat.kernel.sensordata.SensorDataEntryFactory;
import org.hackystat.kernel.admin.ServerProperties;
import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.kernel.shell.SensorShell;
import org.hackystat.kernel.util.DateInfo;

/**
 * Tests that this SDT is defined correctly.
 *
 * @author    Philip Johnson
 * @version   $Id: TestActivity.java,v 1.4 2004/04/09 23:09:32 johnson Exp $
 */
public class TestMicroActivity extends TestCase {

  /**
   * Tests the SDT definition by creating an instance and checking each field and accessor.
   * @exception Exception  if an error occurs
   */
  public void testActivitySDT() throws Exception {
    String sensorType = "Activity";
    Date timestamp = new Date();
    String timestampString = String.valueOf(timestamp.getTime());
    String tool = "Emacs";
    MicroActivityType activityType = MicroActivityType.OPEN_FILE;
    String data = "foo.txt";
    String[] attributes = {timestampString, tool, activityType.toString(), data};
    MicroActivity activity = (MicroActivity) SensorDataEntryFactory.getEntry(sensorType,
        Arrays.asList(attributes));
    assertEquals("Checking timestamp", timestamp, activity.getTimestamp());
    assertEquals("Checking tool", tool, activity.getTool());
    assertEquals("Checking activitytype", activityType, activity.getActivityType());
    assertEquals("Checking data", data, activity.getData());
  }


  /**
   * Tests the activity shell command by invoking each command type.
   * @exception Exception  if an error occurs
   */
  public void testActivityShellCommand() throws Exception {
    // Create the sensor properties file for this test case.
    ServerProperties serverProperties = ServerProperties.getInstance();
    String testUser = "testshellcommand" + serverProperties.getTestDomain();
    SensorProperties sensorProps = new SensorProperties(serverProperties.getHackystatHost(), 
      testUser);

    // Create the shell instance for this test case.
    boolean isInteractive = false;
    boolean enableOfflineData = false;
    long dateLong = DateInfo.getTestDate().getTime();
    SensorShell shell = new SensorShell(sensorProps, isInteractive, "test", enableOfflineData);

    // Now exercise the shell with Activity commands.
    String[] addArgs = {"add", "Open File", "foo.txt,bar.txt"};
    assertTrue("Checking add",
        shell.doCommand(new Date(dateLong), "Activity", Arrays.asList(addArgs)));
    String[] toolArgs = {"setTool", "Emacs"};
    assertTrue("Checking setTool",
        shell.doCommand(new Date(dateLong + 1000), "Activity", Arrays.asList(toolArgs)));
    String[] changeArgs = {"statechange", "foo.txt", "20"};
    assertTrue("Checking first statechange",
        shell.doCommand(new Date(dateLong + 2000), "Activity", Arrays.asList(changeArgs)));
    changeArgs[2] = "21";
    assertTrue("Checking second statechange",
        shell.doCommand(new Date(dateLong + 3000), "Activity", Arrays.asList(changeArgs)));
    assertTrue("Checking send", shell.send());
  }
}


