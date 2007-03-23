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
import java.util.ArrayList;
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
import org.electrocodeogram.module.UIModule;
import org.electrocodeogram.module.target.TargetModule;
import org.electrocodeogram.module.target.implementation.stats.SDay;
import org.electrocodeogram.module.target.implementation.stats.SGlobal;
import org.electrocodeogram.module.target.implementation.stats.SProject;
import org.electrocodeogram.module.target.implementation.stats.SResource;
import org.electrocodeogram.module.target.implementation.stats.SUser;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.electrocodeogram.system.ModuleSystem;

/**
 * This module makes some statistics of incoming events like the total
 * count of events per day. The statistics are displayed in a GUI
 * window.
 */
public class StatisticsTargetModule extends TargetModule implements UIModule {

    /**
     * This is the logger.
     */
    static Logger logger = LogHelper
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
     * A list of {@link SDay} objects, each representing a day of the
     * timestamp of incoming events.
     */
    private ArrayList < SDay > dayList;
    
    // For further extension of the statistics
    //private ArrayList < SProject > projectList;
    //private ArrayList < SUser > userList;
    //private ArrayList < SResource > resourceList;
    //private SGlobal global;

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
            Integer.valueOf(this.totalEventCount));

        return this.totalEventCount;
    }

    /**
     * Returns the number of different days for which events have been
     * received.
     * @return The number of different days
     */
    protected final int getDayCount() {

        logger.entering(this.getClass().getName(), "getDayCount");

        logger.exiting(this.getClass().getName(), "getDayCount", Integer.valueOf(
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

        this.dayList = new ArrayList < SDay >();

        logger.exiting(this.getClass().getName(), "initialize");

    }

    /**
     * Returns the <em>SDay</em> with the given index from the
     * {@link #dayList}.
     * @param index
     *            Is the index
     * @return IThe <em>SDay</em> with the given index or
     *         <code>null</code> if the index id not found
     */
    protected final SDay getDay(final int index) {

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

        SDay dayOfPacket = new SDay(arg0.getTimeStamp());

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

                SDay sDay = this.dayList.get(index);

                sDay.addEvents();

                sDay.setBegin(arg0.getTimeStamp());

                sDay.setEnd(arg0.getTimeStamp());

                sDay.addEvent(arg0);

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
         * @param sDay Is the day of the column where this button is placed
         */
        public StatsButton(final String title, final StatisticsTargetModule module, final SDay sDay) {

            super(title);

            this.statsModule = module;

            this.setToolTipText("Click to get details");

            this.addMouseListener(new MouseAdapter() {

                public void mouseClicked(MouseEvent e) {

                    String[] files = sDay.getChangedFiles();

                    String[] projects = sDay.getProjects();

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
                        "Codechange details for " + sDay.getDate());

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
            .createLogger(SDay.class.getName());

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
                "getRowCount", Integer.valueOf(this.rowCount));

            return this.rowCount;
        }

        /**
         * @see javax.swing.table.AbstractTableModel#getColumnName(int)
         */
        @Override
        public final String getColumnName(final int columnIndex) {

            statsTableModelLogger.entering(this.getClass().getName(),
                "getColumnName", new Object[] {Integer.valueOf(columnIndex)});

            if (columnIndex == 0) {

                statsTableModelLogger.exiting(this.getClass().getName(),
                    "getColumnName", "");

                return " ";
            }

            SDay sDay = this.statsModule.getDay(columnIndex - 1);

            if (sDay == null) {

                statsTableModelLogger.exiting(this.getClass().getName(),
                    "getColumnName", "");

                return "";
            }

            statsTableModelLogger.exiting(this.getClass().getName(),
                "getColumnName", sDay.getDate());

            return sDay.getDate();

        }

        /**
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        public int getColumnCount() {

            statsTableModelLogger.entering(this.getClass().getName(),
                "getColumnCount");

            statsTableModelLogger.exiting(this.getClass().getName(),
                "getColumnCount", Integer.valueOf(
                    this.statsModule.getDayCount() + 1));

            return this.statsModule.getDayCount() + 1;

        }

        /**
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        public final Object getValueAt(final int rowIndex, final int columnIndex) {

            statsTableModelLogger.entering(this.getClass().getName(),
                "getValueAt", new Object[] {Integer.valueOf(columnIndex)});

            if (columnIndex == 0) {

                statsTableModelLogger.exiting(this.getClass().getName(),
                    "getValueAt", Integer.valueOf(rowIndex));

                return getRowHeadline(rowIndex);
            }

            SDay sDay = this.statsModule.getDay(columnIndex - 1);

            if (sDay == null) {

                statsTableModelLogger.exiting(this.getClass().getName(),
                    "getValueAt", "");

                return "";
            }

            statsTableModelLogger.exiting(this.getClass().getName(),
                "getValueAt", getRowContent(sDay, rowIndex));

            Object o = getRowContent(sDay, rowIndex);

            return o;

        }

        /**
         * Returns the content of a table cell.
         * @param rowIndex
         *            Is the index of the table row
         * @param sDay
         *            Is the day or column index
         * @return The content of the table cell
         */
        @SuppressWarnings("synthetic-access")
        private Object getRowContent(final SDay sDay, final int rowIndex) {

            statsTableModelLogger.entering(this.getClass().getName(),
                "getRowContent", new Object[] {sDay, Integer.valueOf(rowIndex)});

            if (rowIndex == 0) {

                statsTableModelLogger.exiting(this.getClass().getName(),
                    "getRowContent", sDay.getDate());

                return sDay.getDate();
            } else if (rowIndex == 1) {

                statsTableModelLogger.exiting(this.getClass().getName(),
                    "getRowContent", sDay.getBegin());

                return sDay.getBegin();
            } else if (rowIndex == 2) {

                statsTableModelLogger.exiting(this.getClass().getName(),
                    "getRowContent", sDay.getEnd());

                return sDay.getEnd();
            } else if (rowIndex > 2 && rowIndex < this.msdtCount + 3) {
                Integer count = sDay.getMsdtMap().get(ModuleSystem.getInstance()
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
                        this.statsModule, sDay);
                }

                return count.toString();


            } else {

                statsTableModelLogger.exiting(this.getClass().getName(),
                    "getRowContent", Integer.valueOf(sDay.getEventsTotal()));

                return Integer.valueOf(sDay.getEventsTotal());
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
                "getRowHeadline", new Object[] {Integer.valueOf(rowIndex)});

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
