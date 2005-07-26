package org.electrocodeogram.event;

import java.util.Date;
import java.util.List;

/**
 * A ValidEventPacket is a subclass of EventPacket. The data
 * in a ValidEventPacket has been checked for compliance
 * with the syntactical rules for event data.
 * So a ValidEventPacket can be trusted to be syntactically valid.
 */
public class ValidEventPacket extends EventPacket
{

    private static final long serialVersionUID = 2507406265346291700L;

    /**
     * This creates a new EventPacket object
     * @param id The module source ID identifies where the EventPacket comes from
     * @param timeStamp The timeStamp tells when the event was recorded
     * @param sensorDataType The HackyStat SensorDataType of the event
     * @param argList The argList of parameters containing all the relevant event data
     * @throws IllegalEventParameterException If the given parameters are not conforming to the syntactical MPE rules
     */
    public ValidEventPacket(int id, Date timeStamp, String sensorDataType, List argList) throws IllegalEventParameterException
    {
        super(id, timeStamp, sensorDataType, argList);

        if (!isSyntacticallyCorrect(timeStamp, sensorDataType, argList)) {
            throw new IllegalEventParameterException();
        }

        if (id < 0) {
            throw new IllegalEventParameterException();
        }

    }

}
