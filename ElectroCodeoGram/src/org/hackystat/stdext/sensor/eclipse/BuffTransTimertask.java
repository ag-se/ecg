package org.hackystat.stdext.sensor.eclipse;

import java.util.TimerTask;

/**
 * Implements Eclipse sensor timer task that can be executed by a timer. The timer task checks
 * buffer transition, which represents the event where a developer moves from one buffer
 * (containing a file) to another buffer (containing a different file).
 *
 * @author Takuya Yamashita
 * @version $Id: BuffTransTimertask.java,v 1.3 2004/01/27 02:02:36 takuyay Exp $
 */
public class BuffTransTimertask extends TimerTask {
  /**
   * Processes the state change activity and computes file metrics in a time based interval.
   */
  public void run() {
    EclipseSensor sensor = EclipseSensor.getInstance();
    // process buffer transactions.
    sensor.processBuffTrans();
  }
}
