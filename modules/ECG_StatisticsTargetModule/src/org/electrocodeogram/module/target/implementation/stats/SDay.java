package org.electrocodeogram.module.target.implementation.stats;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.misc.xml.ECGParser;
import org.electrocodeogram.misc.xml.NodeException;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.w3c.dom.Document;

/**
 * The <em>StatisticsTargetModule</em> consolidates incoming
 * events on a per day basis. The days are determined from the
 * events timestamp fields. For every day, on which an events have
 * been recorded, they are counted and sorted by their
 * <em>MicroSensorDataType</em>.
 */
public class SDay {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper.createLogger(SDay.class
        .getName());

    /**
     * This is how the event's recording time is written.
     */
    private static final String TIME_FORMAT_PATTERN = "HH:mm:ss";

    /**
     * This is how the event's recording day is written.
     */

    private static final String DATE_FORMAT_PATTERN = "dd.MM.yyyy";

    /**
     * A reference to the date formatter.
     */
    private SimpleDateFormat timeFormat;

    /**
     * A reference to the date formatter.
     */

    private SimpleDateFormat dateFormat;

    /**
     * The <code>Date</code> of the <em>SDay</em>.
     */
    private Date myDate;

    /**
     * The <code>Date</code> of the first recorded event for
     * this <em>SDay</em>.
     */
    private Date begin;

    /**
     * The <code>Date</code> of the last recorded event for this
     * <em>SDay</em>.
     */

    private Date end;

    /**
     * This map is used to count the <em>MicroSensordataTypes</em>
     * of the incoming events.
     */
    HashMap < MicroSensorDataType, Integer > msdtMap;

    /**
     * A map of changed files.
     */
    private HashMap < String, Integer > filenameMap;

    /**
     * A map of changed projects.
     */
    private HashMap < String, Integer > projects;

    /**
     * Used to calculate the date and time values.
     */
    private Calendar calendar;

    /**
     * The total number of evebts for this day.
     */
    int eventsTotal;

    /**
     * Creates a <em>SDay</em> with the given <code>Date</code>.
     * @param date
     *            Is the <code>Date</code> of the <em>SDay</em>
     */
    public SDay(final Date date) {

        logger.entering(this.getClass().getName(), "SDay",
            new Object[] {date});

        this.myDate = date;

        this.calendar = Calendar.getInstance();

        this.calendar.setTime(this.myDate);

        this.timeFormat = new SimpleDateFormat(TIME_FORMAT_PATTERN);

        this.dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);

        //this.changedFiles = new ArrayList < String >();

        this.msdtMap = new HashMap < MicroSensorDataType, Integer >();

        this.filenameMap = new HashMap < String, Integer >();

        this.projects = new HashMap < String, Integer >();

        logger.exiting(this.getClass().getName(), "SDay");
    }

    /**
     * Adds the <em>MicroSensorDataType</em> of an received
     * event to the <em>SDay</em>.
     * @param event
     *            Is the received event.
     */
    @SuppressWarnings("synthetic-access")
    public final void addEvent(final ValidEventPacket event) {

        logger.entering(this.getClass().getName(), "addEvent",
            new Object[] {event});

        if (this.msdtMap.containsKey(event.getMicroSensorDataType())) {
            Integer count = this.msdtMap
                .get(event.getMicroSensorDataType());

            count = new Integer(count.intValue() + 1);

            this.msdtMap.remove(event.getMicroSensorDataType());

            this.msdtMap.put(event.getMicroSensorDataType(), count);
        } else {
            this.msdtMap
                .put(event.getMicroSensorDataType(), new Integer(1));
        }

        Document document = event.getDocument();

        try {
            String projectName = ECGParser.getSingleNodeValue(
                "projectname", document);

            if (this.projects.containsKey(projectName)) {
                Integer count = this.projects.get(projectName);

                count = new Integer(count.intValue() + 1);

                this.projects.remove(projectName);

                this.projects.put(projectName, count);
            } else {
                this.projects.put(projectName, new Integer(1));
            }

        } catch (NodeException e1) {
            logger.log(Level.INFO, "NodeException: " + e1.getMessage());

        }

        if (event.getMicroSensorDataType().getName().equals(
            "msdt.codechange.xsd")) {

            try {
                String filename = ECGParser.getSingleNodeValue(
                    "documentname", document);

                if (this.filenameMap.containsKey(filename)) {
                    Integer count = this.filenameMap.get(filename);

                    count = new Integer(count.intValue() + 1);

                    this.filenameMap.remove(filename);

                    this.filenameMap.put(filename, count);
                } else {
                    this.filenameMap.put(filename, new Integer(1));
                }

            } catch (NodeException e) {

                logger.log(Level.INFO, "NodeException: " + e.getMessage());
            }
        }

        logger.exiting(this.getClass().getName(), "addEvent");
    }



    /**
     * Returns the number of events for this day.
     * @return The number of events for this day
     */
    public final int getEventsTotal() {

        logger.entering(this.getClass().getName(), "getEventsTotal");

        logger.exiting(this.getClass().getName(), "getEventsTotal",
            new Integer(this.eventsTotal));

        return this.eventsTotal;
    }

    /**
     * Returns the number of this day in the year.
     * @return The number of this day in the year
     */
    public final int getDayOfYear() {

        logger.entering(this.getClass().getName(), "getDayOfYear");

        logger.exiting(this.getClass().getName(), "getDayOfYear",
            new Integer(this.calendar.get(Calendar.DAY_OF_YEAR)));

        return this.calendar.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Returns the year of this day.
     * @return The year of this day
     */
    public final int getYear() {

        logger.entering(this.getClass().getName(), "getYear");

        logger.exiting(this.getClass().getName(), "getYear",
            new Integer(this.calendar.get(Calendar.YEAR)));

        return this.calendar.get(Calendar.YEAR);
    }

    /**
     * Increases the event counter by one.
     */
    public void addEvents() {

        logger.entering(this.getClass().getName(), "addEvents");

        this.eventsTotal++;

        logger.exiting(this.getClass().getName(), "addEvents");

    }

    /**
     * Returns the time-string of the first recorded event of this
     * day.
     * @return The time-string of the first recorded event
     */
    public String getBegin() {

        logger.entering(this.getClass().getName(), "getBegin");

        logger.exiting(this.getClass().getName(), "getBegin",
            this.timeFormat.format(this.begin));

        return this.timeFormat.format(this.begin);
    }

    /**
     * Returns the date-string of this day.
     * @return The date-string of this day
     */
    public String getDate() {

        logger.entering(this.getClass().getName(), "getDate");

        logger.exiting(this.getClass().getName(), "getDate",
            this.dateFormat.format(this.myDate));

        return this.dateFormat.format(this.myDate);
    }

    /**
     * Returns the time-string of the last recorded event of this
     * day.
     * @return The time-string of the last recorded event
     */
    public String getEnd() {

        logger.entering(this.getClass().getName(), "getEnd");

        logger.exiting(this.getClass().getName(), "getEnd",
            this.timeFormat.format(this.end));

        return this.timeFormat.format(this.end);
    }

    /**
     * Sets the time-string of the first recorded event of this
     * day.
     * @param beginDate
     *            The time-string of the first recorded event
     */
    public final void setBegin(final Date beginDate) {

        logger.entering(this.getClass().getName(), "setBegin",
            new Object[] {beginDate});

        if (this.begin == null) {
            this.begin = beginDate;
        } else if (this.begin.compareTo(beginDate) > 0) {
            this.begin = beginDate;
        }

        logger.exiting(this.getClass().getName(), "setBegin");
    }

    /**
     * Sets the time-string of the last recorded event of this
     * day.
     * @param endDate
     *            The time-string of the last recorded event
     */
    public final void setEnd(final Date endDate) {

        logger.entering(this.getClass().getName(), "setEnd",
            new Object[] {endDate});

        if (this.end == null) {
            this.end = endDate;
        } else if (this.end.compareTo(endDate) < 0) {
            this.end = endDate;
        }

        logger.exiting(this.getClass().getName(), "setEnd");
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public final boolean equals(final Object obj) {

        logger.entering(this.getClass().getName(), "equals",
            new Object[] {obj});

        if (obj instanceof SDay) {
            SDay sDay = (SDay) obj;

            if ((sDay.getDayOfYear() == this.getDayOfYear())
                && (sDay.getYear() == this.getYear())) {

                logger.exiting(this.getClass().getName(), "equals",
                    new Boolean(true));

                return true;
            }

        }

        logger.exiting(this.getClass().getName(), "equals", new Boolean(
            false));

        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        String concat = "" + this.getDayOfYear() + this.getYear();

        int code = Integer.parseInt(concat);

        return code;
    }

    /**
     * Returns the names of changed files in one
     * <code>String</code> separated by newlines.
     * @return The names of changed files
     */
    public String[] getChangedFiles() {

        logger.entering(this.getClass().getName(), "getChangedFiles");

        return this.filenameMap.keySet().toArray(
            new String[this.filenameMap.size()]);
    }

    /**
     * Returns the names of changed projects in this day.
     * @return The names of changed projects
     */
    public String[] getProjects() {
        return this.projects.keySet().toArray(
            new String[this.projects.size()]);
    }

    /**
     * @return the msdtMap
     */
    public HashMap<MicroSensorDataType, Integer> getMsdtMap() {
        return msdtMap;
    }

}