/*
 * Class: EventPacket
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.event;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * This is the most fundamental representation for an recordable
 * events in the <em>ElectroCodeoGram</em>. In case of an
 * <em>eventPacket</em> no assertions are made depending the syntax
 * or the content of the event.<br>
 * Instead events with a legal syntax are stored in
 * {@link org.electrocodeogram.event.WellFormedEventPacket} objetcs
 * and events with validated content are stored in
 * {@link org.electrocodeogram.event.ValidEventPackets}.
 */
public class EventPacket implements Serializable {

    /**
     * This is the <em>Serialization</em> id.
     */
    private static final long serialVersionUID = 2353171166739768704L;

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper.createLogger(EventPacket.class
        .getName());

    /**
     * The unique int id of the module that processed this event at
     * last.
     */
    private int sourceId = -1;

    /**
     * The timestamp telling when thew event was recorded.
     */
    private Date myTimeStamp;

    /**
     * The <em>HackyStat SensorDataType</em> of the event.
     */
    private String mySensorDataType;

    /**
     * The stringlist containing the evtn's data.
     */
    private List myArgList;

    /**
     * This creates a new <em>EventPacket</em>.
     * @param id
     *            The unique int id of the module that processed this
     *            event at last
     * @param timeStamp
     *            The timestamp telling when thew event was recorded
     * @param sensorDataType
     *            The <em>HackyStat SensorDataType</em> of the event
     * @param argList
     *            The stringlist containing the evtn's data
     */
    public EventPacket(final int id, final Date timeStamp,
        final String sensorDataType, final List argList) {

        logger.entering(this.getClass().getName(), "EventPacket", new Object[] {
            new Integer(id), timeStamp, sensorDataType, argList});

        this.sourceId = id;

        this.myTimeStamp = timeStamp;

        this.mySensorDataType = sensorDataType;

        this.myArgList = argList;

        logger.exiting(this.getClass().getName(), "EventPacket");

    }

    /**
     * This method returns the id that identifies module, which was
     * processing this event at last.
     * @return The id of the last processing module
     */
    public int getSourceId() {
        logger.entering(this.getClass().getName(), "getSourceId");

        logger.exiting(this.getClass().getName(), "getSourceId", this.sourceId);

        return this.sourceId;
    }

    /**
     * This method returns the timestamp of the event.
     * @return The timestamp
     */
    public Date getTimeStamp() {
        logger.entering(this.getClass().getName(), "getTimeStamp");

        if (this.myTimeStamp != null) {

            logger.exiting(this.getClass().getName(), "getTimeStamp", new Date(
                this.myTimeStamp.getTime()));

            return new Date(this.myTimeStamp.getTime());
        }

        logger.exiting(this.getClass().getName(), "getTimeStamp", null);

        return null;
    }

    /**
     * This method returns the <em>HackyStat SensorDataType</em> of
     * the event.
     * @return The <em>HackyStat SensorDataType</em>
     */
    public String getSensorDataType() {
        logger.entering(this.getClass().getName(), "getSensorDataType");

        logger.exiting(this.getClass().getName(), "getSensorDataType",
            this.mySensorDataType);

        return this.mySensorDataType;
    }

    /**
     * This method returns the stringlist of the event
     * @return The stringlist
     */
    public List getArgList() {
        logger.entering(this.getClass().getName(), "getArglist");

        logger.exiting(this.getClass().getName(), "getArglist", this.myArgList);

        return this.myArgList;
    }

    /**
     * This method returns a <code>String</code> representation of
     * the event.
     * @return A <code>String</code> representation of the event
     */
    @Override
    public String toString() {
        logger.entering(this.getClass().getName(), "toString");

        String string = "";

        String dateString = "";

        if (this.getTimeStamp() != null) {

            SimpleDateFormat dateFormat = new SimpleDateFormat(
                WellFormedEventPacket.DATE_FORMAT_PATTERN);

            dateString = dateFormat.format(this.getTimeStamp());

        }

        string += dateString + "#";

        string += this.getSensorDataType() + "#";

        StringBuffer stringBuffer = new StringBuffer();

        if (this.getArgList() != null) {
            for (int i = 0; i < this.getArgList().size(); i++) {
                stringBuffer.append(";");

                stringBuffer.append((String) this.getArgList().get(i));
            }
        }

        logger.exiting(this.getClass().getName(), "toString", string
                                                              + stringBuffer
                                                                  .toString());

        return string + stringBuffer.toString();
    }

    /**
     * This method compares a given event with this event.
     * @param packet
     *            Is the event to compare this to
     * @return <code>true</code> if the two events have identical
     *         timestamps, SensorDataTypes and stringlists and
     *         <code>false</code> otherwise
     */
    public boolean isEqual(Object packet) {
        logger.entering(this.getClass().getName(), "isEqual",
            new Object[] {packet});

        boolean equals = false;

        if (!(packet instanceof EventPacket)) {
            logger.log(Level.WARNING,
                "The parameter \"packet\" is not an EventPacket");

            logger.exiting(this.getClass().getName(), "isEqual", new Boolean(
                false));

            return false;
        }

        EventPacket eventPacket = (EventPacket) packet;

        if (this.getTimeStamp().equals(eventPacket.getTimeStamp())
            && this.getSensorDataType().equals(eventPacket.getSensorDataType())) {
            if (this.getArgList().size() == eventPacket.getArgList().size()) {
                int size = eventPacket.getArgList().size();

                for (int i = 0; i < size; i++) {
                    String testString = (String) this.getArgList().get(i);

                    String receivedString = (String) eventPacket.getArgList()
                        .get(i);

                    if (testString.equals(receivedString)) {
                        equals = true;
                    } else {
                        equals = false;
                    }
                }
            }

        }

        logger.exiting(this.getClass().getName(), "isEqual",
            new Boolean(equals));

        return equals;

    }
}
