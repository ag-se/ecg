/*
 * Class: HackyStatTargetModule
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.target;

import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.ModuleProperty;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.system.ModuleSystem;

/**
 * This module makes some statistics of incoming events like the total
 * count of events per day. The statistics are displayed in a GUI
 * window.
 */
public class StatisticsTargetModule extends TargetModule {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(StatisticsTargetModule.class.getName());

    /**
     * The total count of received events since the creation of this
     * module.
     */
    private int totalEventCount;

    /**
     * A reference to the statistics window.
     */
    private StatsFrame frmStats;

    /**
     * A list of {@link Day} objects, each representing a day of the
     * timestamp of incoming events.
     */
    private ArrayList < Day > dayList;

    /**
     * This creates the module instance. It is not to be called by
     * developers, instead it is called from the <em>ECG
     * ModuleRegistry</em>
     * subsystem, when the user requested a new instance of this
     * module.
     * @param id
     *            This is the unique <code>String</code> id of the
     *            module
     * @param name
     *            This is the name which is assigned to the module
     *            instance
     */
    public StatisticsTargetModule(final String id, final String name) {
        super(id, name);

        logger.entering(this.getClass().getName(), "StatisticsTargetModule",
            new Object[] {id, name});

        logger.exiting(this.getClass().getName(), "StatisticsTargetModule");
    }

    /**
     * @see org.electrocodeogram.module.Module#propertyChanged(org.electrocodeogram.module.ModuleProperty)
     */
    @Override
    public final void propertyChanged(final ModuleProperty moduleProperty) {

        logger.entering(this.getClass().getName(), "propertyChanged",
            new Object[] {moduleProperty});

        if (moduleProperty.getName().equals("Show Statistics")) {
            openDialog();
        }

        logger.exiting(this.getClass().getName(), "propertyChanged");
    }

    /**
     * Returns the number of received events since the creation of
     * this module.
     * @return The total number of received events
     */
    protected final int getTotalEventCount() {

        logger.entering(this.getClass().getName(), "getTotalEventCount");

        logger.exiting(this.getClass().getName(), "getTotalEventCount",
            new Integer(this.totalEventCount));

        return this.totalEventCount;
    }

    /**
     * Returns the number of different days for which events have been
     * received.
     * @return The number of different days
     */
    protected final int getDayCount() {

        logger.entering(this.getClass().getName(), "getDayCount");

        logger.exiting(this.getClass().getName(), "getDayCount", new Integer(
            this.dayList.size()));

        return this.dayList.size();
    }

    /**
     * Shows the statistics window.
     */
    private void openDialog() {

        logger.entering(this.getClass().getName(), "openDialog");

        if (this.frmStats == null) {
            this.frmStats = new StatsFrame(this);
        }

        this.frmStats.setVisible(true);

        logger.exiting(this.getClass().getName(), "openDialog");

    }

    /**
     * @see org.electrocodeogram.module.Module#update() This The
     *      method is not implemented in this module.
     */
    @Override
    public void update() {
    // not implemented
    }

    /**
     * @see org.electrocodeogram.module.intermediate.IntermediateModule#initialize()
     */
    @Override
    public final void initialize() {

        logger.entering(this.getClass().getName(), "initialize");

        this.dayList = new ArrayList < Day >();

        logger.exiting(this.getClass().getName(), "initialize");

    }

    /**
     * Returns the <em>Day</em> with the given index from the
     * {@link #dayList}.
     * @param index
     *            Is the index
     * @return IThe <em>Day</em> with the given index or
     *         <code>null</code> if the index id not found
     */
    protected final Day getDay(final int index) {

        logger.entering(this.getClass().getName(), "getDay");

        if (this.dayList.size() == 0) {

            logger.exiting(this.getClass().getName(), "getDay", null);

            return null;
        }

        if (index >= this.dayList.size()) {

            logger.exiting(this.getClass().getName(), "getDay", null);

            return null;
        }

        logger.exiting(this.getClass().getName(), "getDay", this.dayList
            .get(index));

        return this.dayList.get(index);
    }

    /**
     * @see org.electrocodeogram.module.target.TargetModule#write(org.electrocodeogram.event.ValidEventPacket)
     */
    @Override
    public final void write(final ValidEventPacket arg0) {

        logger
            .entering(this.getClass().getName(), "write", new Object[] {arg0});

        this.totalEventCount++;

        Day dayOfPacket = new Day(arg0.getTimeStamp());

        if (this.dayList.size() == 0) {

            dayOfPacket.addEvents();

            dayOfPacket.setBegin(arg0.getTimeStamp());

            dayOfPacket.setEnd(arg0.getTimeStamp());

            dayOfPacket.addEvent(arg0.getMicroSensorDataType());

            this.dayList.add(dayOfPacket);

            this.frmStats.updateTableModel();
        } else {
            if (this.dayList.contains(dayOfPacket)) {
                int index = this.dayList.indexOf(dayOfPacket);

                Day day = this.dayList.get(index);

                day.addEvents();

                day.setBegin(arg0.getTimeStamp());

                day.setEnd(arg0.getTimeStamp());

                day.addEvent(arg0.getMicroSensorDataType());

            } else {
                dayOfPacket.addEvents();

                dayOfPacket.setBegin(arg0.getTimeStamp());

                dayOfPacket.setEnd(arg0.getTimeStamp());

                dayOfPacket.addEvent(arg0.getMicroSensorDataType());

                this.dayList.add(dayOfPacket);

                this.frmStats.updateTableModel();
            }
        }

        this.frmStats.update();

        logger.exiting(this.getClass().getName(), "write");

    }

    /**
     * @see org.electrocodeogram.module.target.TargetModule#startWriter()
     */
    @Override
    public final void startWriter() {

        logger.entering(this.getClass().getName(), "startWriter");

        openDialog();

        logger.exiting(this.getClass().getName(), "startWriter");

    }

    /**
     * @see org.electrocodeogram.module.target.TargetModule#stopWriter()
     */
    @Override
    public final void stopWriter() {

        logger.entering(this.getClass().getName(), "stopWriter");

        closeWindow();

        logger.exiting(this.getClass().getName(), "stopWriter");

    }

    /**
     * Is used to close the statistics window.
     */
    private void closeWindow() {

        logger.entering(this.getClass().getName(), "closeWindow");

        this.frmStats.dispose();

        this.frmStats = null;

        logger.exiting(this.getClass().getName(), "closeWindow");

    }

    /**
     * This is the statistics window.
     */
    private static class StatsFrame extends JFrame {

        /**
         * This is the logger.
         */
        private static Logger statsFrameLogger = LogHelper
            .createLogger(StatsFrame.class.getName());

        /**
         * The <em>Serialization</em> id.
         */
        private static final long serialVersionUID = 1085522698222017915L;

        /**
         * A reference to the module.
         */
        private StatisticsTargetModule module;

        /**
         * The statistics are written into this table.
         */
        private JTable statsTable;

        /**
         * Creates the statistics window and its components.
         * @param statsModule
         *            Is the module
         */
        public StatsFrame(final StatisticsTargetModule statsModule) {

            statsFrameLogger.entering(this.getClass().getName(),
                "statsFramelogger", new Object[] {statsModule});

            this.module = statsModule;

            setTitle("Event Statisctics");

            setLayout(new GridLayout(1, 1));

            setSize(500, 300);

            this.statsTable = new JTable(new StatsTableModel(this.module));

            this.statsTable.setAutoCreateColumnsFromModel(true);

            JScrollPane scrollPane = new JScrollPane(this.statsTable);

            this.getContentPane().add(scrollPane);

            statsFrameLogger.exiting(this.getClass().getName(),
                "statsFramelogger");

        }

        /**
         * Used to repaint the statistics window.
         */
        public void update() {

            statsFrameLogger.entering(this.getClass().getName(), "update");

            this.statsTable.repaint();

            statsFrameLogger.exiting(this.getClass().getName(), "update");
        }

        /**
         * Used to update the table, when new events are received.
         */
        public void updateTableModel() {

            statsFrameLogger.entering(this.getClass().getName(),
                "updateTableModel");

            this.statsTable.setModel(new StatsTableModel(this.module));

            statsFrameLogger.exiting(this.getClass().getName(),
                "updateTableModel");

        }

    }

    /**
     * The <em>StatisticsTargetModule</em> consolidates incoming
     * events on a per day basis. The days are determined from the
     * events timestamp fields. For every day, on which an events have
     * been recorded, they are counted and sorted by their
     * <em>MicroSensorDataType</em>.
     */
    private static class Day {

        /**
         * This is the logger.
         */
        private static Logger dayLogger = LogHelper.createLogger(Day.class
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
         * The <code>Date</code> of the <em>Day</em>.
         */
        private Date date;

        /**
         * The <code>Date</code> of the first recorded event for
         * this <em>Day</em>.
         */
        private Date begin;

        /**
         * The <code>Date</code> of the last recorded event for this
         * <em>Day</em>.
         */

        private Date end;

        /**
         * All files that are changed according to <em>Codechange</em>
         * events are lsited here.
         */
        private ArrayList < String > changedFiles;

        /**
         * This map is used to count the <em>MicroSensordataTypes</em>
         * of the incoming events.
         */
        private HashMap < MicroSensorDataType, Integer > eventTypeCounterMap;

        /**
         * Used to calculate the date and time values.
         */
        private Calendar calendar;

        /**
         * The total number of evebts for this day.
         */
        private int eventsTotal;

        /**
         * Creates a <em>Day</em> with the given <code>Date</code>.
         * @param date
         *            Is the <code>Date</code> of the <em>Day</em>
         */
        public Day(final Date date) {

            dayLogger.entering(this.getClass().getName(), "Day",
                new Object[] {date});

            this.date = date;

            this.calendar = Calendar.getInstance();

            this.calendar.setTime(this.date);

            this.timeFormat = new SimpleDateFormat(TIME_FORMAT_PATTERN);

            this.dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);

            this.changedFiles = new ArrayList < String >();

            this.eventTypeCounterMap = new HashMap < MicroSensorDataType, Integer >();

            dayLogger.exiting(this.getClass().getName(), "Day");
        }

        /**
         * Adds the <em>MicroSensorDataType</em> of an received
         * event to the <em>Day</em>.
         * @param msdt
         *            Is the <em>MicroSensorDataType</em> of the
         *            received event.
         */
        public final void addEvent(final MicroSensorDataType msdt) {

            dayLogger.entering(this.getClass().getName(), "addEvent",
                new Object[] {msdt});

            if (this.eventTypeCounterMap.containsKey(msdt)) {
                Integer count = this.eventTypeCounterMap.get(msdt);

                count = new Integer(count.intValue() + 1);

                this.eventTypeCounterMap.remove(msdt);

                this.eventTypeCounterMap.put(msdt, count);
            } else {
                this.eventTypeCounterMap.put(msdt, new Integer(1));
            }

            dayLogger.exiting(this.getClass().getName(), "addEvent");
        }

        /**
         * Adds a filename of a file changed as reported by an
         * incoming <em>Codechange</em> event, to the <em>Day</em>.
         * @param filename
         *            A filename of a changed file
         */
        public final void addChangedFile(final String filename) {

            dayLogger.entering(this.getClass().getName(), "addChangedFile",
                new Object[] {filename});

            if (filename == null) {

                dayLogger.exiting(this.getClass().getName(), "addChangedFile");

                return;
            }

            if (this.changedFiles.size() == 0) {
                this.changedFiles.add(filename);
            } else if (!this.changedFiles.contains(filename)) {
                this.changedFiles.add(filename);
            }

            dayLogger.exiting(this.getClass().getName(), "addChangedFile");
        }

        /**
         * Returns the number of events for this day.
         * @return The number of events for this day
         */
        public final int getEventsTotal() {

            dayLogger.entering(this.getClass().getName(), "getEventsTotal");

            dayLogger.exiting(this.getClass().getName(), "getEventsTotal",
                new Integer(this.eventsTotal));

            return this.eventsTotal;
        }

        /**
         * Returns the number of this day in the year.
         * @return The number of this day in the year
         */
        public final int getDayOfYear() {

            dayLogger.entering(this.getClass().getName(), "getDayOfYear");

            dayLogger.exiting(this.getClass().getName(), "getDayOfYear",
                new Integer(this.calendar.get(Calendar.DAY_OF_YEAR)));

            return this.calendar.get(Calendar.DAY_OF_YEAR);
        }

        /**
         * Returns the year of this day.
         * @return The year of this day
         */
        public final int getYear() {

            dayLogger.entering(this.getClass().getName(), "getYear");

            dayLogger.exiting(this.getClass().getName(), "getYear",
                new Integer(this.calendar.get(Calendar.YEAR)));

            return this.calendar.get(Calendar.YEAR);
        }

        /**
         * Increases the event counter by one.
         */
        public void addEvents() {

            dayLogger.entering(this.getClass().getName(), "addEvents");

            this.eventsTotal++;

            dayLogger.exiting(this.getClass().getName(), "addEvents");

        }

        /**
         * Returns the time-string of the first recorded event of this
         * day.
         * @return The time-string of the first recorded event
         */
        public String getBegin() {

            dayLogger.entering(this.getClass().getName(), "getBegin");

            dayLogger.exiting(this.getClass().getName(), "getBegin",
                this.timeFormat.format(this.begin));

            return this.timeFormat.format(this.begin);
        }

        /**
         * Returns the date-string of this day.
         * @return The date-string of this day
         */
        public String getDate() {

            dayLogger.entering(this.getClass().getName(), "getDate");

            dayLogger.exiting(this.getClass().getName(), "getDate",
                this.dateFormat.format(this.date));

            return this.dateFormat.format(this.date);
        }

        /**
         * Returns the time-string of the last recorded event of this
         * day.
         * @return The time-string of the last recorded event
         */
        public String getEnd() {

            dayLogger.entering(this.getClass().getName(), "getEnd");

            dayLogger.exiting(this.getClass().getName(), "getEnd",
                this.timeFormat.format(this.end));

            return this.timeFormat.format(this.end);
        }

        /**
         * Sets the time-string of the first recorded event of this
         * day.
         * @param begin
         *            The time-string of the first recorded event
         */
        public final void setBegin(final Date begin) {

            dayLogger.entering(this.getClass().getName(), "setBegin",
                new Object[] {begin});

            if (this.begin == null) {
                this.begin = begin;
            } else if (this.begin.compareTo(begin) > 0) {
                this.begin = begin;
            }

            dayLogger.exiting(this.getClass().getName(), "setBegin");
        }

        /**
         * Sets the time-string of the last recorded event of this
         * day.
         * @param end
         *            The time-string of the last recorded event
         */
        public final void setEnd(final Date end) {

            dayLogger.entering(this.getClass().getName(), "setEnd",
                new Object[] {end});

            if (this.end == null) {
                this.end = end;
            } else if (this.end.compareTo(end) < 0) {
                this.end = end;
            }

            dayLogger.exiting(this.getClass().getName(), "setEnd");
        }

        public final boolean isEqual(final Object obj) {

            dayLogger.entering(this.getClass().getName(), "equals",
                new Object[] {obj});

            if (obj instanceof Day) {
                Day day = (Day) obj;

                if ((day.getDayOfYear() == this.getDayOfYear())
                    && (day.getYear() == this.getYear())) {

                    dayLogger.exiting(this.getClass().getName(), "equals",
                        new Boolean(true));

                    return true;
                }

            }

            dayLogger.exiting(this.getClass().getName(), "equals", new Boolean(
                false));

            return false;
        }

        /**
         * Returns the names of changed files in one
         * <code>String</code> separated by newlines.
         * @return The names of changed files
         */
        public String getChangedFiles() {

            dayLogger.entering(this.getClass().getName(), "getChangedFiles");

            String toReturn = "";

            for (String filename : this.changedFiles) {
                toReturn += filename + "\n";
            }

            dayLogger.exiting(this.getClass().getName(), "getChangedFiles",
                toReturn);

            return toReturn;
        }

    }

    /**
     * This is the <code>TableModel</code> for the statistics table.
     */
    private static class StatsTableModel extends AbstractTableModel {

        /**
         * This is the logger.
         */
        private static Logger statsTableModelLogger = LogHelper
            .createLogger(Day.class.getName());

        /**
         * The <em>serialization</em> id.
         */
        private static final long serialVersionUID = -1852628212540288731L;

        /**
         * The number of rows in the table.
         */
        private int rowCount;

        /**
         * The number of <em>MicroSensorDataTypes</em> in the table.
         */
        private int msdtCount = ModuleSystem.getInstance()
            .getMicroSensorDataTypes().length;

        /**
         * A reference to the module.
         */
        private StatisticsTargetModule statsModule;

        /**
         * This creates the <code>TableModel</code>.
         * @param statsModule
         *            Is the module
         */
        public StatsTableModel(final StatisticsTargetModule statsModule) {

            statsTableModelLogger.entering(this.getClass().getName(),
                "StatsTableModel", new Object[] {statsModule});

            this.statsModule = statsModule;

            this.rowCount = 4 + this.msdtCount;

            statsTableModelLogger.exiting(this.getClass().getName(),
                "StatsTableModel");
        }

        /**
         * Returns the number of rows in the table.
         * @return The number of rows in the table
         */
        public int getRowCount() {

            statsTableModelLogger.entering(this.getClass().getName(),
                "getRowCount");

            statsTableModelLogger.exiting(this.getClass().getName(),
                "getRowCount", new Integer(this.rowCount));

            return this.rowCount;
        }

        /**
         * @see javax.swing.table.AbstractTableModel#getColumnName(int)
         */
        @Override
        public final String getColumnName(final int columnIndex) {

            statsTableModelLogger.entering(this.getClass().getName(),
                "getColumnName", new Object[] {new Integer(columnIndex)});

            if (columnIndex == 0) {

                statsTableModelLogger.exiting(this.getClass().getName(),
                    "getColumnName", "");

                return "";
            }

            Day day = this.statsModule.getDay(columnIndex - 1);

            if (day == null) {

                statsTableModelLogger.exiting(this.getClass().getName(),
                    "getColumnName", "");

                return "";
            }

            statsTableModelLogger.exiting(this.getClass().getName(),
                "getColumnName", day.getDate());

            return day.getDate();

        }

        /**
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        public int getColumnCount() {

            statsTableModelLogger.entering(this.getClass().getName(),
                "getColumnCount");

            statsTableModelLogger.exiting(this.getClass().getName(),
                "getColumnCount", new Integer(
                    this.statsModule.getDayCount() + 1));

            return this.statsModule.getDayCount() + 1;

        }

        /**
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        public final Object getValueAt(final int rowIndex, final int columnIndex) {

            statsTableModelLogger.entering(this.getClass().getName(),
                "getValueAt", new Object[] {new Integer(columnIndex)});

            if (columnIndex == 0) {

                statsTableModelLogger.exiting(this.getClass().getName(),
                    "getValueAt", new Integer(rowIndex));

                return getRowHeadline(rowIndex);
            }

            Day day = this.statsModule.getDay(columnIndex - 1);

            if (day == null) {

                statsTableModelLogger.exiting(this.getClass().getName(),
                    "getValueAt", "");

                return "";
            }

            statsTableModelLogger.exiting(this.getClass().getName(),
                "getValueAt", getRowContent(day, rowIndex));

            return getRowContent(day, rowIndex);

        }

        /**
         * Returns the content of a table cell.
         * @param rowIndex
         *            Is the index of the table row
         * @param day
         *            Is the day or column index
         * @return The content of the table cell
         */
        @SuppressWarnings("synthetic-access")
        private Object getRowContent(final Day day, final int rowIndex) {

            statsTableModelLogger.entering(this.getClass().getName(),
                "getRowContent", new Object[] {day, new Integer(rowIndex)});

            if (rowIndex == 0) {

                statsTableModelLogger.exiting(this.getClass().getName(),
                    "getRowContent", day.getDate());

                return day.getDate();
            } else if (rowIndex == 1) {

                statsTableModelLogger.exiting(this.getClass().getName(),
                    "getRowContent", day.getBegin());

                return day.getBegin();
            } else if (rowIndex == 2) {

                statsTableModelLogger.exiting(this.getClass().getName(),
                    "getRowContent", day.getEnd());

                return day.getEnd();
            } else if (rowIndex > 2 && rowIndex < this.msdtCount + 3) {
                Integer count = day.eventTypeCounterMap.get(ModuleSystem
                    .getInstance().getMicroSensorDataTypes()[rowIndex - 3]);

                if (count == null) {

                    statsTableModelLogger.exiting(this.getClass().getName(),
                        "getRowContent", "");

                    return "";
                }

                statsTableModelLogger.exiting(this.getClass().getName(),
                    "getRowContent", count.toString());

                return count.toString();

            } else {

                statsTableModelLogger.exiting(this.getClass().getName(),
                    "getRowContent", new Integer(day.eventsTotal));

                return new Integer(day.eventsTotal);
            }

        }

        /**
         * Returns the headlines for the given row.
         * @param rowIndex
         *            Is the index of the table row
         * @return The headline of the given row
         */
        private String getRowHeadline(final int rowIndex) {

            statsTableModelLogger.entering(this.getClass().getName(),
                "getRowHeadline", new Object[] {new Integer(rowIndex)});

            if (rowIndex == 0) {

                statsTableModelLogger.exiting(this.getClass().getName(),
                    "getRowHeadline", "Date");

                return "Date";
            } else if (rowIndex == 1) {

                statsTableModelLogger.exiting(this.getClass().getName(),
                    "getRowHeadline", "Begin");

                return "Begin";
            } else if (rowIndex == 2) {

                statsTableModelLogger.exiting(this.getClass().getName(),
                    "getRowHeadline", "End");

                return "End";
            } else if (rowIndex > 2 && rowIndex < this.msdtCount + 3) {

                statsTableModelLogger.exiting(this.getClass().getName(),
                    "getRowHeadline", ModuleSystem.getInstance()
                        .getMicroSensorDataTypes()[rowIndex - 3].getName());

                return ModuleSystem.getInstance().getMicroSensorDataTypes()[rowIndex - 3]
                    .getName();
            } else {

                statsTableModelLogger.exiting(this.getClass().getName(),
                    "getRowHeadline", "Events total");

                return "Events total";
            }

        }

    }
}
