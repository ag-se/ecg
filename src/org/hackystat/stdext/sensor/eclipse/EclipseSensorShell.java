package org.hackystat.stdext.sensor.eclipse;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.kernel.shell.SensorShell;

/**
 * Provides the sensor shell wrapper class, which provides eclipse specific invocation. * doCommand method provides the process status line dislay as well as invoking doCommand of the  * SensorShell instance. * @author Takuya Yamashita * @version $Id: EclipseSensorShell.java,v 1.4 2004/10/15 22:48:38 takuyay Exp $
 */

public class EclipseSensorShell {

    /**
     * The sensor shell instance.
     * 
     * @uml.property name="shell"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private SensorShell shell;

  /** The flag for enabling Eclipse sensor. */
  private boolean isEnabled;
  /** The flag for enabling status line monitor sensor. */
  private boolean isMonitorEnabled;
  
  /**
   * Instantiates the eclipse specific sensor shell.
   * @param shell the SensorShell instance.
   */
  public EclipseSensorShell(SensorShell shell) {
    this.shell = shell;
    final String MONITOR = EclipseSensor.ECLIPSE_MONITOR;
    this.isEnabled = shell.getSensorProperties().isSensorTypeEnabled(EclipseSensor.ECLIPSE);
    this.isMonitorEnabled = shell.getSensorProperties().isSensorTypeEnabled(MONITOR);
  }
  
  /**
   * Invokes the shell associated with this sensor with command and its args. 
   * Provides the time stamp automatically. Do nothing if sensor shell instance is
   * null. Displays sensor data information on the status line at the bottom of the Eclipse IDE if
   * <code>ENABLE_ECLIPSE_MONITOR_SENSOR=true</code>.
   *
   * @param command A legal SensorShell command name (e.g. "Activity", "FileMetric", etc.
   * @param args A list of string arguments to the shell.
   */
  public void doCommand(final String command, final List args) {
    if (this.shell != null) {
      if (this.isEnabled && this.isMonitorEnabled) {
        // create process status line string.
        StringBuffer buffer = new StringBuffer();
        for (Iterator i = args.iterator(); i.hasNext();) {
          buffer.append((String) i.next() + " : ");
        }
        buffer.delete(buffer.length() - 3, buffer.length());
        processStatusLine("Hackystat Sensor : " + command + " : "
                                                      + buffer.toString());
      }
      this.shell.doCommand(new Date(), command, args);
    }
  }
  
  /**
   * Sends all Hackystat data to the server. Do nothing if sensor shell instance is null.
   */
  public void send() {
    if (this.shell != null) {
      this.shell.send();
    }
  }
  
  /**
   * Prints out the line plus newline if in interactive mode, and always logs the line.
   * Provided to clients to support logging of error messages. 
   * 
   * @param message  The line to be printed.
   */
  public void println(String message) {
    this.shell.println(message);
  }

    /**
     * Sets monitor enabled status.
     * @param isMonitorEnabled true if monitor is enabled.
     * 
     * @uml.property name="isMonitorEnabled"
     */
    public void setMonitorEnabled(boolean isMonitorEnabled) {
        this.isMonitorEnabled = isMonitorEnabled;
    }

  
  /**
   * Gets the SensorProperties instance.
   * @return the SensorProperties instance.
   */
  public SensorProperties getSensorProperties() {
    return this.shell.getSensorProperties();
  }
  
  /**
   * Processes to display the message in the status line. Since EclipseSensor is executed
   * from a non-UI thread, such the application that wishs to call UI code from the non-UI thread
   * must provide a Runnable (anonymous class in this case) that calls the UI code. 
   * For more detail, try to search "Threading issues" in the "Help | Help Contents" of
   * the Eclipse IDE.
   * 
   * @param message the message to be displayed.
   */
  public void processStatusLine(final String message) {
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        // Retrieves status line manager instance and sets the message into the instance.
        IWorkbench workbench = EclipseSensorPlugin.getInstance().getWorkbench();
        IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
        if (activeWindow != null) {
          IWorkbenchPage activePage = activeWindow.getActivePage();
          if (activePage != null) {
            IViewReference[] viewReferences = activePage.getViewReferences();
            for (int i = 0; i < viewReferences.length; i++) {
              IViewPart viewPart = viewReferences[i].getView(true);
              if (viewPart != null) {
                IActionBars viewActionbars = viewPart.getViewSite().getActionBars();
                IStatusLineManager viewStatusManager = viewActionbars.getStatusLineManager();
                // Set status line associated with view part such as Package Explore, and etc.
                viewStatusManager.setMessage(message);
              }
            }
            IEditorPart editorPart = activePage.getActiveEditor();
            if (editorPart != null) {
              IActionBars partActionBars = editorPart.getEditorSite().getActionBars();
              IStatusLineManager partStatusManager = partActionBars.getStatusLineManager();
              // Set status line associated with editor part such as text editor.
              partStatusManager.setMessage(message);
            }
          }
        }
      }
    });
  }
}
