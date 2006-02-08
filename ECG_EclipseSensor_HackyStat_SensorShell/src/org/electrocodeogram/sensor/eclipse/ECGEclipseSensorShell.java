/*
 * Class: ECGEclipseSensorShell
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.sensor.eclipse;

import java.util.Date;
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

   public void doCommand(final String command, final List args) {
        if (this.shell != null) {

            this.shell.doCommand(new Date(), command, args);

            this.shell.send();
        }
    }

}
