/*
 * Class: ValidEventPacket
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.event;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.xml.ECGParser;
import org.hackystat.kernel.sdt.SensorDataTypeException;
import org.hackystat.kernel.sensordata.SensorDataEntryFactory;
import org.w3c.dom.Document;

/**
 * Representation for an event, that has allready passed the
 * validation. Depending on the {@link VALIDATION_LEVEL} a
 * ValideventPacket is either nothing more than a
 * WellformedEventPacket or it can be assured that the
 * ValideventPacket is containing a <em>HackyStat</em> event or it
 * can be assured it is containing an <em>ECG</em> event. A
 * <em>HackyStat</em> event is an instance of an
 * <em>HackyStat</em> <em>SensorDataType</em> and an <em>ECG</em>
 * event is an instance of an <em>ECG</em>
 * {@link org.electrocodeogram.msdt.MicroSensorDataType}.
 */
public class ValidEventPacket extends WellFormedEventPacket {

    /**
     * This is this class' logger.
     */
    private static Logger logger = LogHelper
        .createLogger(ValidEventPacket.class.getName());

    /**
     * This constant integer value is giving the location of the
     * <em>HackyStat ActivityType</em> value in the event's
     * stringlist.
     */
    public static final int ACTIVITY_TYPE_INDEX = 1;

    /**
     * This constant integer value is giving the location of the
     * <em>MicroActivity</em> in the event's stringlist.
     */
    public static final int MICROACTIVITY_INDEX = 2;

    /**
     * The <em>Serialization</em> id.
     */
    private static final long serialVersionUID = -2907957495470756557L;

    /**
     * The <em>MicroSensorDataType</em> of this event, if it is a
     * <em>MicroActivityEvent</em>.
     */
    private transient MicroSensorDataType myMsdt = null;

    /**
     * Represents the three levels of validity for events contained in
     * ValideventPackets.
     */
    public enum VALIDATION_LEVEL {
        /**
         * The event has not been validated. It can be everything that
         * fits into a
         * {@link org.electrocodeogram.event.WellFormedEventPacket}.
         * Of course it can also be a <em>HackyStat</em> event or
         * even an <em>ECG</em> event in this case.
         */
        INVALID,

        /**
         * The event has been accepted as a valid <em>HackyStat</em>
         * event, which means it is an instance of a
         * <em>HackyStat SensorDataType</em>. Of course it can also
         * be an <em>ECG</em> event, in this case, as <em>ECG</em>
         * events are a subset of <em>HackyStat</em> events.
         */
        HACKYSTAT,

        /**
         * The event has been accepted as a valid <em>ECG</em>
         * event, which means it is an instance of a
         * {@link org.electrocodeogram.msdt.MicroSensorDataType}.
         */
        ECG
    }

    /**
     * The default level used during event validation.
     */
    public static final VALIDATION_LEVEL DEFAULT_VALIDITY_LEVEL = VALIDATION_LEVEL.ECG;

    /**
     * The curretn <em>VALIDATION_LEVEL</em>.
     */
    private static VALIDATION_LEVEL validationLevel = DEFAULT_VALIDITY_LEVEL;

    /**
     * Every module is using an ValideventPacket twice. It receives
     * them and creates a new one that is then sended to other
     * modules. The DELIVERY_STATE as gotten by
     * {@link ValidEventPacket#getDeliveryState()} tells if the event
     * is the received one or if it is the sent one. This is actually
     * only used in the GUI to toggle which events to display.
     */
    public enum DELIVERY_STATE {
        /**
         * The event was sent by a module.
         */
        SENT,

        /**
         * The event was received by a module.
         */
        RECEIVED;
    }

    /**
     * THe <em>DELIVERY_STATE</em> of this event.
     */
    private DELIVERY_STATE deliveryState = null;

    /**
     * If the event is an <em>ECG</em> event, it is containing the
     * <em>Microprocess event</em> as a XML document, which is an
     * instance of a
     * {@link org.electrocodeogram.msdt.MicroSensorDataType}.
     */
    private transient Document document;

    /**
     * Tells which validation has been used to validate this event.
     */
    private VALIDATION_LEVEL validatedWith;

    /**
     * Creates the object.
     * @param sourceId
     *            Is the id of the module that sent this packet at
     *            last
     * @param timeStamp
     *            Is the timestamp of the event
     * @param sensorDataType
     *            Is the <em>HackyStat SensorDataType</em> of the
     *            event
     * @param argList
     *            Is the stringlist of the event
     * @throws IllegalEventParameterException
     *             If the parameters are invalid according to the
     *             current <em>VALIDATION_LEVEL</em>
     */
    public ValidEventPacket(final int sourceId, final Date timeStamp,
        final String sensorDataType, final List argList)
        throws IllegalEventParameterException {
        super(sourceId, timeStamp, sensorDataType, argList);

        logger.entering(this.getClass().getName(), "ValidEventPacket",
            new Object[] {new Integer(sourceId), timeStamp, sensorDataType,
                argList});

        validate();

        logger.exiting(this.getClass().getName(), "ValidEventPacket");
    }

    /**
     * This method is used to get the current
     * <em>VALIDATION_LEVEL</em>. To get the
     * <em>VALIDATION_LEVEL</em> that has been used to validate an
     * actual ValidEventPacket, please use
     * @return The current <em>VALIDATION_LEVEL</em>
     */
    public static VALIDATION_LEVEL getValidationLevel() {

        logger.entering(ValidEventPacket.class.getName(), "getValidationLevel");

        logger.exiting(ValidEventPacket.class.getName(), "getValidationLevel",
            validationLevel);

        return validationLevel;
    }

    /**
     * This method is used to get the <em>VALIDATION_LEVEL</em> that
     * has been used to validate this event.
     * @return The event's <em>VALIDATION_LE'VEL</em>
     */
    public final VALIDATION_LEVEL getValidatedWith() {

        logger.entering(this.getClass().getName(), "getValidatedWith");

        logger.exiting(this.getClass().getName(), "getValidatedWith",
            this.validatedWith);

        return this.validatedWith;
    }

    /**
     * This method is used to set the <em>VALIDATION_LEVEL</em> to
     * be used for the event validation.
     * @param validityLevel
     *            Is the new <em>VALIDATION_LEVEL</em>
     */
    public static void setValidityLevel(final VALIDATION_LEVEL validityLevel) {

        logger.entering(ValidEventPacket.class.getName(), "setValidityLevel",
            new Object[] {validityLevel});

        validationLevel = validityLevel;

        logger.exiting(ValidEventPacket.class.getName(), "setValidityLevel");

    }

    /**
     * This does the actual validation, by checking if the event is
     * valid according to the current <em>VALIDATION_LEVEL</em>.
     * @throws IllegalEventParameterException
     *             If the event is not valid
     */
    private void validate() throws IllegalEventParameterException {

        logger.entering(this.getClass().getName(), "validate");

        logger.log(Level.FINE, "Going to validate the event with level: "
                               + validationLevel.toString());

        if (validationLevel == VALIDATION_LEVEL.INVALID) {

            logger.log(Level.FINE, "Event validation unneccessary in level: "
                                   + VALIDATION_LEVEL.INVALID.toString());
        }

        if (validationLevel == VALIDATION_LEVEL.HACKYSTAT) {
            new WellFormedEventPacket(this.getSourceId(), this.getTimeStamp(),
                this.getSensorDataType(), this.getArglist());

            hackyStatValidate();
        }

        else if (validationLevel == VALIDATION_LEVEL.ECG) {

            new WellFormedEventPacket(this.getSourceId(), this.getTimeStamp(),
                this.getSensorDataType(), this.getArglist());

            hackyStatValidate();

            ecgValidate();

        }

        logger.log(Level.FINE, "The event is valid with respect to level "
                               + validationLevel.toString());

        logger.log(ECGLevel.PACKET, this.toString());

        logger.exiting(this.getClass().getName(), "validate");

    }

    /**
     * This is checking if this event is an
     * <em>MicroActivityEvent</em>.
     * @throws IllegalEventParameterException
     *             If this event is not a <em>MicroActivityevent</em>.
     */
    private void ecgValidate() throws IllegalEventParameterException {

        logger.entering(this.getClass().getName(), "ecgValidate");

        logger.log(ECGLevel.PACKET, this.toString());

        if (!this.getSensorDataType().equals(
            WellFormedEventPacket.HACKYSTAT_ACTIVITY_STRING)) {
            logger.log(Level.FINE,
                "The event is not valid in repspect to validity level: "
                                + VALIDATION_LEVEL.ECG);

            logger.log(Level.FINE,
                "The event is not a HackyStat ActivityEvent.");

            logger.exiting(this.getClass().getName(), "ecgValidate");

            throw new IllegalEventParameterException(
                "The event is not a HackyStat ActivityEvent.");

        }

        logger.log(Level.FINE,
            "The event is not valid in repspect to validity level: "
                            + VALIDATION_LEVEL.ECG);

        List argList = this.getArglist();

        String microActivityType = (String) argList.get(1);

        if (microActivityType == null || microActivityType.equals("")) {
            logger.log(Level.FINE,
                "The event is not valid in repspect to validity level: "
                                + VALIDATION_LEVEL.ECG);

            logger.log(Level.FINE,
                "The event does not have a MicroSensorDataType.");

            logger.exiting(this.getClass().getName(), "ecgValidate");

            throw new IllegalEventParameterException(
                "The event does not have a MicroSensorDataType.");

        }

        String microActivityString = (String) argList.get(2);

        if (microActivityString == null || microActivityString.equals("")) {

            logger.log(Level.FINE,
                "The event is not valid in repspect to validity level: "
                                + VALIDATION_LEVEL.ECG);

            logger.log(Level.FINE,
                "The event does not have a MicroSensorDataType.");

            logger.exiting(this.getClass().getName(), "ecgValidate");

            throw new IllegalEventParameterException(
                "The event does not have a MicroSensorDataType.");

        }

        MicroSensorDataType[] microSensorDataTypes = org.electrocodeogram.system.System
            .getInstance().getMsdtRegistry().getMicroSensorDataTypes();

        if (microSensorDataTypes == null || microSensorDataTypes.length == 0) {

            logger.log(Level.FINE,
                "The event is not valid in repspect to validity level: "
                                + VALIDATION_LEVEL.ECG);

            logger
                .log(Level.FINE,
                    "There are currently no MicroSensorDataTypes registered in the ECG.");

            logger.exiting(this.getClass().getName(), "ecgValidate");

            throw new IllegalEventParameterException(
                "There are currently no MicroSensorDataTypes registered in the ECG.");

        }

        Document tmpDocument = null;

        File defFile = null;

        for (MicroSensorDataType microSensorDataType : microSensorDataTypes) {
            if (microSensorDataType.getName().equals(microActivityType)) {
                defFile = microSensorDataType.getDefFile();

                this.myMsdt = microSensorDataType;

                logger.log(Level.FINE,
                    "The event is assumed to have a MicroSensorDataType: "
                                    + microSensorDataType.getName());

                break;
            }
        }

        if (defFile == null) {
            logger.log(Level.FINE,
                "The event is not valid in respect to validity level: "
                                + validationLevel);

            logger.log(Level.FINE,
                "The MicroSensorDataType of the event is unknown.");

            logger.exiting(this.getClass().getName(), "ecgValidate");

            throw new IllegalEventParameterException(
                "The MicroSensorDataType of the event is unknown.");

        }

        try {
            tmpDocument = ECGParser.parseAsMicroActivity(microActivityString,
                defFile.getAbsolutePath());

            this.document = tmpDocument;

            logger.log(Level.FINE,
                "The event is valid in respect to this MicroSensorDataType.");

            logger.exiting(this.getClass().getName(), "ecgValidate");
        } catch (Exception e) {
            logger.log(Level.FINE,
                "The event is not valid in repspect to validity level: "
                                + validationLevel);

            logger
                .log(Level.FINE,
                    "The event is not valid in respect to this MicroSensorDataType.");

        }
    }

    /**
     * This is checking if this event is a <em>HackyStat</em> event.
     * @throws IllegalEventParameterException
     *             If the event is not a <em>HackyStat</em>
     */
    private void hackyStatValidate() throws IllegalEventParameterException {
        logger.entering(this.getClass().getName(), "hackyStatValidate");

        logger.log(ECGLevel.PACKET, this.toString());

        List<String> entryList = new ArrayList<String>();

        List argList = this.getArglist();

        entryList.add(new Long(this.getTimeStamp().getTime()).toString());

        for (Object elem : argList) {
            if (elem instanceof String) {
                String str = (String) elem;

                entryList.add(str);
            } else {
                logger.log(Level.FINE,
                    "The event is not valid in repspect to validity level: "
                                    + VALIDATION_LEVEL.HACKYSTAT);

                logger.log(Level.FINE,
                    "The argList contains non-String elements.");

                logger.exiting(this.getClass().getName(), "hackyStatValidate");

                throw new IllegalEventParameterException(
                    "The argList contains non-String elements.");
            }

        }

        try {
            SensorDataEntryFactory
                .getEntry(this.getSensorDataType(), entryList);

        } catch (SensorDataTypeException e) {
            logger.log(Level.FINE,
                "The event is not valid in respect to validity level: "
                                + VALIDATION_LEVEL.HACKYSTAT);

            logger.log(Level.FINE, e.getMessage());

            logger.exiting(this.getClass().getName(), "hackyStatValidate");

            throw new IllegalEventParameterException(e.getMessage());
        }
    }

    /**
     * This method returns the MicroSensorDataType of the
     * MicroActivity event that is packet in this
     * TypedValidEventPacket.
     * @return The MicroSensorDataType of the MicroActivity
     */
    public final MicroSensorDataType getMicroSensorDataType() {
        logger.entering(this.getClass().getName(), "getMicroSensorDataType");

        logger.exiting(this.getClass().getName(), "getMicroSensorDataType",
            this.myMsdt);

        return this.myMsdt;
    }

    /**
     * This returns the delivery state of this event.
     * @return the delivery state of this event
     */
    public final DELIVERY_STATE getDeliveryState() {
        logger.entering(this.getClass().getName(), "getDeliveryState");

        logger.exiting(this.getClass().getName(), "getDeliveryState",
            this.deliveryState);

        return this.deliveryState;
    }

    /**
     * This sets the delivery state of this event.
     * @param state
     *            Is the delivery state
     */

    public final void setDeliveryState(final DELIVERY_STATE state) {
        logger.entering(this.getClass().getName(), "setDeliveryState",
            new Object[] {state});

        if (state == null) {
            logger.log(Level.WARNING, "The parameter \"state\" is null");

            logger.exiting(this.getClass().getName(), "setDeliveryState");

            return;
        }

        this.deliveryState = state;

        logger.exiting(this.getClass().getName(), "setDeliveryState");
    }

    /**
     * This returns the XML document of the event, if it is a
     * <em>MicroActivityEvent</em>.
     * @return The XML document of the event, if it is a
     *         <em>MicroActivityEvent</em> and <code>null</code>
     *         if not.
     */
    public final Document getDocument() {

        logger.entering(this.getClass().getName(), "getDocument");

        logger.exiting(this.getClass().getName(), "getDocument", this.document);

        return this.document;
    }
}
