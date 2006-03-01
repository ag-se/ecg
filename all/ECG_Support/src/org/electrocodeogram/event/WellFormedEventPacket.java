/*
 * Class: WellFormedEventPacket
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.event;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.logging.LogHelper.ECGLevel;

/**
 * A <em>WellFormedEventPacket</em> is a subclass of
 * {@link EventPacket}. The data in a <em>WellFormedEventPacket</em>
 * has been checked for compliance with the syntactical rules for
 * event data as there are:
 * <ul>
 * <li>The {@link EventPacket#myTimeStamp} is not null,
 * <li>The {@link EventPacket#mySensorDataType} is not null and
 * <li>The {@link EventPacket#myArgList} is not null and a non empty
 * stringlist
 * </ul>
 */
public class WellFormedEventPacket extends EventPacket {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(WellFormedEventPacket.class.getName());

    /**
     * When an event is a <em>MicroActivityEvent</em> the
     * <em>MicroSensorDataType</em> name is contained in its
     * {@link EventPacket#mySensorDataType} field.
     */
    public static final String MICRO_ACTIVITY_PREFIX = "MicroActivity#";

    /**
     * This is the <code>String</code> which indicates that an event
     * is an <em>ECG MicroActivityEvent</em>.
     */
    public static final String MICRO_ACTIVITY_STRING = "MicroActivity";

    /**
     * This constand holds the <em>HackyStat</em> add-command, which
     * tells the HackyStat server to add this event to its list of
     * events and must be inserted into each event for compatibility
     * reasons.
     */
    public static final String HACKYSTAT_ADD_COMMAND = "add";

    /**
     * This constant holds the
     * <em>HackyStat Activity SensorDataType</em> <code>String</code>,
     * which indicates that an event is in <em>HackyStat Activity</em>
     * event. This is also true for all <em>ECG MicroActivity</em>
     * events.
     */
    public static final String HACKYSTAT_ACTIVITY_STRING = "Activity";

    /**
     * This <code>String</code> constant is used to separate the
     * parts of the string representation of an event.
     */
    public static final String EVENT_SEPARATOR = "#";

    /**
     * This String separates the stringlist entries in the
     * <code>String</code> representation of this event.
     */
    public static final String ARGLIST_SEPARATOR = ";";

    /**
     * This is the pattern used to format the timeStamp values of an
     * event's <code>String</code> representation. The pattern
     * symbols are accroding to the
     * {@link java.text.DateFormatSymbols} class.
     */
    public static final String DATE_FORMAT_PATTERN = "EE dd.MM.yyyy HH:mm:ss z";

    /**
     * The <em>Serialization</em> id.
     */
    private static final long serialVersionUID = 2507406265346291700L;

    public static final String MICRO_ACTIVITY_SUFFIX = "#MicroActivity";

    /**
     * This creates a new <em>WellFormedEventPacket</em> object.
     * @param id
     *            The unique int id of the module that processed this
     *            event at last
     * @param timeStamp
     *            The timestamp telling when thew event was recorded
     * @param sensorDataType
     *            The <em>HackyStat SensorDataType</em> of the event
     * @param argList
     *            The stringlist containing the evtn's data
     * @throws IllegalEventParameterException
     *             If the given parameters are not conforming to the
     *             syntactical rules
     */
    public WellFormedEventPacket(final int id, final Date timeStamp,
        final String sensorDataType, final List argList)
        throws IllegalEventParameterException {
        super(id, timeStamp, sensorDataType, argList);

        logger.entering(this.getClass().getName(), "WellFormedEventPacket",
            new Object[] {new Integer(id), timeStamp, sensorDataType, argList});

        validate();

        logger.exiting(this.getClass().getName(), "ValidEventPacket");

    }

    /**
     * This method checks the syntactical correctness of an event.
     * @throws IllegalEventParameterException
     *             If the given parameters are not conforming to the
     *             syntactical rules
     */
    private void validate() throws IllegalEventParameterException {
        logger.entering(EventPacket.class.getName(), "validate");

        logger.log(ECGLevel.PACKET, this.toString());

        if (this.getTimeStamp() == null) {
            logger.log(Level.FINE,
                "The event is not wellformed. The timestamp is null.");

            logger.exiting(EventPacket.class.getName(), "validate");

            throw new IllegalEventParameterException(
                "The event is not wellformed");
        }

        if (this.getSensorDataType() == null) {
            logger.log(Level.FINE,
                "The event is not wellformed. The SensorDataType is null.");

            logger.exiting(EventPacket.class.getName(), "validate");

            throw new IllegalEventParameterException(
                "The event is not wellformed");
        }

        if (this.getArgList() == null) {

            logger.log(Level.FINE,
                "The event is not wellformed. The argList is null.");

            logger.exiting(EventPacket.class.getName(), "validate");

            throw new IllegalEventParameterException(
                "The event is not wellformed");
        }

        if (this.getArgList().isEmpty()) {

            logger.log(Level.FINE,
                "The event is not wellformed. The argList is empty.");

            logger.exiting(EventPacket.class.getName(), "validate");

            throw new IllegalEventParameterException(
                "The event is not wellformed");
        }

        if (!(this.getArgList().get(0) instanceof String)) {
            logger.log(Level.FINE,
                "The event is not wellformed. The argList is no List<String>.");

            logger.exiting(EventPacket.class.getName(), "validate");

            throw new IllegalEventParameterException(
                "The event is not wellformed");

        }

        logger.log(Level.FINE, "The event is wellformed.");

        logger.exiting(EventPacket.class.getName(), "validate");

    }

}
