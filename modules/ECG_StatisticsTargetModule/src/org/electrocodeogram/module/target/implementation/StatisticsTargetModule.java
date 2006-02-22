/*
 * Class: HackyStatTargetModule
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.target.implementation;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.misc.constants.UIConstants;
import org.electrocodeogram.misc.xml.ECGParser;
import org.electrocodeogram.misc.xml.NodeException;
import org.electrocodeogram.module.UIModule;
import org.electrocodeogram.module.target.TargetModule;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.system.ModuleSystem;
import org.w3c.dom.Document;

/**
 * This module makes some statistics of incoming events like the total
 * count of events per day. The statistics are displayed in a GUI
 * window.
 */
public class StatisticsTargetModule extends TargetModule implements UIModule {

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
    private StatsFrame pnlStats;

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
     * @see org.electrocodeogram.module.Module#propertyChanged(org.electrocodeogram.modulepackage.ModuleProperty)
     *      This method is not implemented in this module.
     */
    @Override
    public final void propertyChanged(@SuppressWarnings("unused")
    final ModuleProperty moduleProperty) {
    // not implemented
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
     * @return The panel showing the statistics table
     */
    private JPanel openDialog() {

        logger.entering(this.getClass().getName(), "openDialog");

        if (this.pnlStats == null) {
            this.pnlStats = new StatsFrame(this);
        }

        logger.exiting(this.getClass().getName(), "openDialog", this.pnlStats);

        return this.pnlStats;

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

            dayOfPacket.addEvent(arg0);

            this.dayList.add(dayOfPacket);

            this.pnlStats.updateTableModel();
        } else {
            if (this.dayList.contains(dayOfPacket)) {
                int index = this.dayList.indexOf(dayOfPacket);

                Day day = this.dayList.get(index);

                day.addEvents();

                day.setBegin(arg0.getTimeStamp());

                day.setEnd(arg0.getTimeStamp());

                day.addEvent(arg0);

            } else {
                dayOfPacket.addEvents();

                dayOfPacket.setBegin(arg0.getTimeStamp());

                dayOfPacket.setEnd(arg0.getTimeStamp());

                dayOfPacket.addEvent(arg0);

                this.dayList.add(dayOfPacket);

                this.pnlStats.updateTableModel();
            }
        }

        this.pnlStats.update();

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
     * This is the statistics window.
     */
    private static class StatsFrame extends JPanel {

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

            setLayout(new GridLayout(1, 1));

            this.statsTable = new JTable(new StatsTableModel(this.module));

            TableCellRenderer defaultRenderer = this.statsTable
                .getDefaultRenderer(Object.class);

            this.statsTable.setDefaultRenderer(Object.class, null);

            this.statsTable.setDefaultRenderer(Object.class,
                new JTableButtonRenderer(defaultRenderer));

            this.statsTable.setAutoCreateColumnsFromModel(true);

            this.statsTable.setPreferredScrollableViewportSize(new Dimension(
                300, 180));

            JScrollPane scrollPane = new JScrollPane(this.statsTable);

            this.add(scrollPane);

            this.statsTable.addMouseListener(new JTableButtonMouseListener(
                this.statsTable));

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
        private Date myDate;

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
         * This map is used to count the <em>MicroSensordataTypes</em>
         * of the incoming events.
         */
        private HashMap < MicroSensorDataType, Integer > msdtMap;

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
        private int eventsTotal;

        /**
         * Creates a <em>Day</em> with the given <code>Date</code>.
         * @param date
         *            Is the <code>Date</code> of the <em>Day</em>
         */
        public Day(final Date date) {

            dayLogger.entering(this.getClass().getName(), "Day",
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

            dayLogger.exiting(this.getClass().getName(), "Day");
        }

        /**
         * Adds the <em>MicroSensorDataType</em> of an received
         * event to the <em>Day</em>.
         * @param event
         *            Is the received event.
         */
        @SuppressWarnings("synthetic-access")
        public final void addEvent(final ValidEventPacket event) {

            dayLogger.entering(this.getClass().getName(), "addEvent",
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

            dayLogger.exiting(this.getClass().getName(), "addEvent");
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
                this.dateFormat.format(this.myDate));

            return this.dateFormat.format(this.myDate);
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
         * @param beginDate
         *            The time-string of the first recorded event
         */
        public final void setBegin(final Date beginDate) {

            dayLogger.entering(this.getClass().getName(), "setBegin",
                new Object[] {beginDate});

            if (this.begin == null) {
                this.begin = beginDate;
            } else if (this.begin.compareTo(beginDate) > 0) {
                this.begin = beginDate;
            }

            dayLogger.exiting(this.getClass().getName(), "setBegin");
        }

        /**
         * Sets the time-string of the last recorded event of this
         * day.
         * @param endDate
         *            The time-string of the last recorded event
         */
        public final void setEnd(final Date endDate) {

            dayLogger.entering(this.getClass().getName(), "setEnd",
                new Object[] {endDate});

            if (this.end == null) {
                this.end = endDate;
            } else if (this.end.compareTo(endDate) < 0) {
                this.end = endDate;
            }

            dayLogger.exiting(this.getClass().getName(), "setEnd");
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public final boolean equals(final Object obj) {

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

            dayLogger.entering(this.getClass().getName(), "getChangedFiles");

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

    }

    /**
     * Used to react on button clicks in the table.
     *
     */
    private static class JTableButtonMouseListener implements MouseListener {

        /**
         * A reference to the table.
         */
        private JTable tbl;

        /**
         * Forwards table-mouse-events to the right button.
         * @param e The event
         */
        private void forwardEventToButton(final MouseEvent e) {
            TableColumnModel columnModel = this.tbl.getColumnModel();
            int column = columnModel.getColumnIndexAtX(e.getX());
            int row = e.getY() / this.tbl.getRowHeight();
            Object value;
            JButton button;
            MouseEvent buttonEvent;

            if (row >= this.tbl.getRowCount() || row < 0
                || column >= this.tbl.getColumnCount() || column < 0) {
                    return;
            }

            value = this.tbl.getValueAt(row, column);

            if (!(value instanceof JButton)) {
                return;
            }

            button = (JButton) value;

            buttonEvent = SwingUtilities.convertMouseEvent(
                this.tbl, e, button);
            button.dispatchEvent(buttonEvent);

            this.tbl.repaint();
        }

        /**
         * Creates the listener.
         * @param table A reference to the table
         */
        public JTableButtonMouseListener(final JTable table) {
            this.tbl = table;
        }

        /**
         * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
         */
        public void mouseClicked(final MouseEvent e) {
            forwardEventToButton(e);
        }

        /**
         * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
         */
        public void mouseEntered(final MouseEvent e) {
            forwardEventToButton(e);
        }

        /**
         * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
         */
        public void mouseExited(final MouseEvent e) {
            forwardEventToButton(e);
        }

        /**
         * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
         */
        public void mousePressed(final MouseEvent e) {
            forwardEventToButton(e);
        }

        /**
         * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
         */
        public void mouseReleased(final MouseEvent e) {
            forwardEventToButton(e);
        }
    }

    /**
     * Used to render the buttons for the "codechange"-events.
     *
     */
    private static class JTableButtonRenderer implements TableCellRenderer {

        /**
         * The parent renderer.
         */
        private TableCellRenderer renderer;

        /**
         * Creates this renderer and assings the parent renderer.
         * @param parent Is the parent renderer
         */
        public JTableButtonRenderer(final TableCellRenderer parent) {
            this.renderer = parent;
        }

        /**
         * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
         */
        public Component getTableCellRendererComponent(final JTable table,
            final Object value, final boolean isSelected,
            final boolean hasFocus, final int row, final int column) {
            if (value instanceof Component) {
                return (Component) value;
            }
            return this.renderer.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, column);
        }
    }

    /**
     * This is a button to be displayed in the table.
     *
     */
    private static class StatsButton extends JButton {

        /**
         * The <em>Serialization</em> id.
         */
        private static final long serialVersionUID = -279871491903771677L;

        /**
         * A reference to the module.
         */
        private StatisticsTargetModule statsModule;

        /**
         * Creates the button.
         * @param title The name of the button
         * @param module A reference to the module
         * @param day Is the day of the column where this button is placed
         */
        public StatsButton(final String title, final StatisticsTargetModule module, final Day day) {

            super(title);

            this.statsModule = module;

            this.setToolTipText("Click to get details");

            this.addMouseListener(new MouseAdapter() {

                @SuppressWarnings("synthetic-access")
                @Override
                public void mouseClicked(@SuppressWarnings("unused") MouseEvent e) {

                    String[] files = day.getChangedFiles();

                    String[] projects = day.getProjects();

                    String fileString = "";

                    String projectString = "";

                    if (files == null || files.length == 0) {
                        fileString = "No changed files recorded.";
                    } else {
                        for (String str : files) {
                            fileString += str + "\n";
                        }
                    }

                    if (projects == null || projects.length == 0) {
                        projectString = "No projects recorded.";
                    } else {
                        for (String str : projects) {
                            projectString += str + "\n";
                        }

                    }

                    JDialog dlg = new JDialog(
                        (JFrame) StatsButton.this.statsModule.pnlStats
                            .getRootPane().getParent(),
                        "Codechange details for " + day.getDate());

                    dlg.getContentPane().setLayout(new GridLayout(1, 2));

                    dlg.setBackground(UIConstants.ECG_LIGHT_GREEN);

                    JPanel pnlLeft = new JPanel();

                    pnlLeft.setBackground(UIConstants.ECG_LIGHT_GREEN);

                    JPanel pnlRight = new JPanel();

                    pnlRight.setBackground(UIConstants.ECG_LIGHT_GREEN);

                    pnlLeft.setBorder(new TitledBorder(new LineBorder(
                        UIConstants.FRM_EVENT_WINDOW_BORDER_COLOR,
                        UIConstants.FRM_EVENT_WINDOW_BORDER_WIDTH,
                        UIConstants.BORDER_ROUNDED), "List of changed files"));

                    pnlRight
                        .setBorder(new TitledBorder(new LineBorder(
                            UIConstants.FRM_EVENT_WINDOW_BORDER_COLOR,
                            UIConstants.FRM_EVENT_WINDOW_BORDER_WIDTH,
                            UIConstants.BORDER_ROUNDED),
                            "List of changed projects"));

                    JTextArea txaFiles = new JTextArea();

                    txaFiles.setBackground(UIConstants.ECG_LIGHT_GREEN);

                    txaFiles.append(fileString);

                    JTextArea txaProjects = new JTextArea();

                    txaProjects.setBackground(UIConstants.ECG_LIGHT_GREEN);

                    txaProjects.append(projectString);

                    pnlLeft.add(txaFiles);

                    pnlRight.add(txaProjects);

                    dlg.getContentPane().add(pnlLeft, 0);

                    dlg.getContentPane().add(pnlRight, 1);

                    dlg.pack();

                    dlg.setVisible(true);

                 }
            });

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
         * @param module
         *            Is the module
         */
        public StatsTableModel(final StatisticsTargetModule module) {

            statsTableModelLogger.entering(this.getClass().getName(),
                "StatsTableModel", new Object[] {module});

            this.statsModule = module;

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

                return " ";
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

            Object o = getRowContent(day, rowIndex);

            return o;

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
                Integer count = day.msdtMap.get(ModuleSystem.getInstance()
                    .getMicroSensorDataTypes()[rowIndex - 3]);

                if (count == null) {

                    statsTableModelLogger.exiting(this.getClass().getName(),
                        "getRowContent", "");

                    return "";
                }

                statsTableModelLogger.exiting(this.getClass().getName(),
                    "getRowContent", count.toString());

                if (getRowHeadline(rowIndex).equals("msdt.codechange.xsd")) {
                    return new StatsButton(count.toString(),
                        this.statsModule, day);
                }

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

    /**
     * @see org.electrocodeogram.module.UIModule#getPanelName()
     */
    public final String getPanelName() {

        return "Event Statistics";
    }

    /**
     * @see org.electrocodeogram.module.UIModule#getPanel()
     */
    public final JPanel getPanel() {

        return openDialog();
    }

    /**
     * @see org.electrocodeogram.module.target.TargetModule#stopWriter()
     *      This method is not implemented fr this module.
     */
    @Override
    public void stopWriter() {
    // not implemented
    }
}
