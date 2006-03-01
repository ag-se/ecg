/*
 * Class: MockSensor
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.test.validation;

import org.electrocodeogram.event.EventPacket;
import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.kernel.shell.SensorShell;

/**
 * This simulates a sensor. Where a real sensor is running inside another
 * appplication and is recording events there, this <code>MockSensor</code>
 * is getting its recoded events from a call to {@link #sendEvent(EventPacket)}.
 * Like the real sensor this <code>MockSensor</code> then passes the event to
 * his instance of the {@link org.hackystat.kernel.shell.SensorShell}.
 * This <code>MockSensor</code> is used in {@link org.electrocodeogram.test.validation.WellformedEventTests}
 * to check how the <code>SensorShell</code> deals with wellformed and malformed events.
 */
public class MockSensor {

    /**
     * A reference to the <code>SensorShelll</code>.
     */
    private SensorShell shell = null;

    /**
     * Used to create the {@link #shell}.
     */
    private SensorProperties properties = null;

    /**
     * Creates the <code>MockSensor</code>.
     */
    public MockSensor() {

        this.properties = new SensorProperties("MockSensor");

        this.shell = new SensorShell(this.properties, false, "");

    }

    /**
     * This method passes the given event to the {@link #shell}.
     * @param eventPacket
     *            The "recorded" event
     * @return The result as given by the ECG SensorShell. <code>true</code>
     *         means the event has been accepted and <code>false</code>
     *         means it has been revoked by the {@link SensorShell}
     */
    public final boolean sendEvent(final EventPacket eventPacket) {
        if (eventPacket == null) {

            return false;

        }

        boolean result = this.shell.doCommand(eventPacket.getTimeStamp(),
            eventPacket.getSensorDataType(), eventPacket.getArgList());

        return result;
    }

}
