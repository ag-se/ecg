package org.electrocodeogram.stdext.microactivity.sdt;

import java.util.HashMap;

/**
 * Provides a consistent naming strategy for Activity type strings.
 *
 * @author    Philip Johnson
 * @version   $Id: ActivityType.java,v 1.1 2003/05/05 23:59:41 kagawaa Exp $
 */
public class MicroActivityType {
  /** A mapping from strings to their associated ActivityType instances. */
  private static HashMap activityMap = new HashMap();

  /** Tool startup activity. */
  public static final MicroActivityType TOOL_STARTUP = new MicroActivityType("Tool Startup");
  /** Tool shutdown activity. */
  public static final MicroActivityType TOOL_SHUTDOWN = new MicroActivityType("Tool Shutdown");
  /** Open file activity. */
  public static final MicroActivityType OPEN_FILE = new MicroActivityType("Open File");
  /** Save file activity. */
  public static final MicroActivityType SAVE_FILE = new MicroActivityType("Save File");
  /** Compile file activity. */
  public static final MicroActivityType COMPILE_FILE = new MicroActivityType("Compile File");
  /** Open project activity. */
  public static final MicroActivityType OPEN_PROJECT = new MicroActivityType("Open Project");
  /** Close project activity. */
  public static final MicroActivityType CLOSE_PROJECT = new MicroActivityType("Close Project");
  /** Close file activity. */
  public static final MicroActivityType CLOSE_FILE = new MicroActivityType("Close File");
  /** Run program activity. */
  public static final MicroActivityType RUN_PROGRAM = new MicroActivityType("Run Program");
  /** Run test activity. */
  public static final MicroActivityType RUN_TEST = new MicroActivityType("Run Test");
  /** Unknown activity. */
  public static final MicroActivityType UNKNOWN = new MicroActivityType("Unknown");
  /** The active buffer has changed state in a sensor. */
  public static final MicroActivityType STATE_CHANGE = new MicroActivityType("State Change");

  /** Holds the string identifying this ActivityType instance. */
  private String activityString;


  /**
   * Creates a new ActivityType instance of the type specified by
   * ActivityString.
   *
   * @param activityString  The activityType string.
   */
  private MicroActivityType(String activityString) {
    this.activityString = activityString;
    activityMap.put(activityString, this);
  }


  /**
   * Returns the ActivityType instance associated with the passed String.
   * Creates a new ActivityType and caches it if it is so far unknown.
   * Guarantees that only one ActivityType per instance exists.
   *
   * @param activityString  A string naming an activity type.
   * @return                The associated ActivityType instance.
   */
  public static MicroActivityType getInstance(String activityString) {
    MicroActivityType activityType = (MicroActivityType) activityMap.get(activityString);
    if (activityType == null) {
      activityType = new MicroActivityType(activityString);
      activityMap.put(activityString, activityType);
    }
    return activityType;
  }


  /**
   * Returns the string associated with this ActivityType.
   *
   * @return   The activity type string.
   */
  public String toString() {
    return this.activityString;
  }
}

