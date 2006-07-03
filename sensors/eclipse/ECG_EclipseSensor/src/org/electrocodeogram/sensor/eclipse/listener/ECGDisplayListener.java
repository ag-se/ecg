package org.electrocodeogram.sensor.eclipse.listener;

import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.sensor.eclipse.ECGEclipseSensor;

/**
 * Listens for events on the display. It's used for cathing windows others than
 * the main window, i.e. dialogs
 */
public class ECGDisplayListener implements Listener {

    /**
     * 
     */
    private final ECGEclipseSensor sensor;

    /**
     * @param sensor
     */
    public ECGDisplayListener(ECGEclipseSensor sensor) {
        this.sensor = sensor;
    }

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {
        ECGEclipseSensor.logger.entering(this.getClass().getName(), "handleEvent", new Object[] {event});

        if (event == null || event.widget == null) {
            // could happen, don't know why
            ECGEclipseSensor.logger.exiting(this.getClass().getName(), "handleEvent");
            return;                
        }
        if (event.widget.getClass() == org.eclipse.swt.widgets.Shell.class) {
            Shell shell = (Shell)event.widget;
            // is it a dialog below the main window or another dialog
            if (shell.getParent() == null) {
                // seems to be main window which is handeled by ShellListener
                ECGEclipseSensor.logger.exiting(this.getClass().getName(), "handleEvent");
                return;
            }
            if (event.type == SWT.Activate) {
                // in case of Activate, it may be opened the first time. Check if shell is known
                Shell foundOpenedShell = null;
                for (Iterator it = this.sensor.openDialogs.iterator(); it.hasNext() && foundOpenedShell == null; ) {
                    Shell s = (Shell)it.next();
                    if (s.equals(shell))
                        foundOpenedShell = s;
                }
                if (foundOpenedShell == null) {
                    // new shell, send opened event
                    this.sensor.openDialogs.add(shell);
                    // send window open as well
                    ECGEclipseSensor.logger.log(ECGLevel.PACKET,
                        "A dialogOpened event has been recorded.");
                    this.sensor.processActivity(
                        "msdt.dialog.xsd",
                        "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                            + this.sensor.username
                            + "</username><id>"
                            + shell.hashCode()
                            + "</id></commonData><dialog><activity>opened</activity><dialogname>"
                            + shell.getText()
                            + "</dialogname></dialog></microActivity>");
                }
                // finally send activate event
                ECGEclipseSensor.logger.log(ECGLevel.PACKET,
                    "A dialogActivated event has been recorded.");
                this.sensor.processActivity(
                    "msdt.dialog.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + this.sensor.username
                        + "</username><id>"
                        + shell.hashCode()
                        + "</id></commonData><dialog><activity>activated</activity><dialogname>"
                        + shell.getText()
                        + "</dialogname></dialog></microActivity>");
            }
            else if (event.type == SWT.Deactivate) {
                ECGEclipseSensor.logger.log(ECGLevel.PACKET,
                    "A dialogDeactivated event has been recorded.");
                this.sensor.processActivity(
                    "msdt.dialog.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + this.sensor.username
                        + "</username><id>"
                        + shell.hashCode()
                        + "</id></commonData><dialog><activity>deactivated</activity><dialogname>"
                        + shell.getText()
                        + "</dialogname></dialog></microActivity>");
            }
            else if (event.type == SWT.Dispose) {
                // in case of Dispose, remove dialog from opened dialogs
                if (this.sensor.openDialogs.remove(shell)) {
                    // if known opened dialog send deactivate first
                    ECGEclipseSensor.logger.log(ECGLevel.PACKET,
                        "A dialogDeactivated event has been recorded.");
                    this.sensor.processActivity(
                        "msdt.dialog.xsd",
                        "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                            + this.sensor.username
                            + "</username><id>"
                            + shell.hashCode()
                            + "</id></commonData><dialog><activity>deactivated</activity><dialogname>"
                            + shell.getText()
                            + "</dialogname></dialog></microActivity>");
                    // ...and closed afterwards
                    ECGEclipseSensor.logger.log(ECGLevel.PACKET,
                        "A dialogClosed event has been recorded.");
                    this.sensor.processActivity(
                        "msdt.dialog.xsd",
                        "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                            + this.sensor.username
                            + "</username><id>"
                            + shell.hashCode()
                            + "</id></commonData><dialog><activity>closed</activity><dialogname>"
                            + shell.getText()
                            + "</dialogname></dialog></microActivity>");
                }
            }
        }

        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "handleEvent");
    }

}