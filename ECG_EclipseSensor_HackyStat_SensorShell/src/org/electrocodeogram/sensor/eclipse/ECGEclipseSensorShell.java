/**
 * 
 */
package org.electrocodeogram.sensor.eclipse;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hackystat.kernel.shell.SensorShell;
import org.hackystat.stdext.sensor.eclipse.EclipseSensorShell;

/**
 *
 */
public class ECGEclipseSensorShell extends EclipseSensorShell {

    private SensorShell shell;

    /**
     * @param shell
     */
    public ECGEclipseSensorShell(SensorShell shell) {
        super(shell);

        this.shell = shell;

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

            this.shell.doCommand(new Date(), command, args);

            this.shell.send();
        }
    }

}
