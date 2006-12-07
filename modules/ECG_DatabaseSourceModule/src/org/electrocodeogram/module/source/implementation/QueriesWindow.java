package org.electrocodeogram.module.source.implementation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Logger;
import javax.sql.rowset.CachedRowSet;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.source.SourceModule;
import org.electrocodeogram.module.target.implementation.DBCommunicator;

/**
 * @author jule
 * @version 1.0 This class is JPanel to integrate in the Lab-GUI for
 *          selecting/entering and execute queries to the database
 */
public class QueriesWindow extends JPanel {
    private final String name = "Queries Window";

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper.createLogger(QueriesWindow.class
            .getName());

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * the vector which holds the queries for the pull-down Menue (JComboBox) in
     * the Queries Window
     */
    private final Vector<String> queries = new Vector<String>();

    /**
     * the pull-down menue from which to select the Queries
     */
    private JComboBox combo;

    /**
     * for displaying messages to the User
     */
    private JLabel msgLabel;

    /**
     * JPanel on which the Pull-Down menue with the queries is placed and which
     * itself is placed on the container
     */
    private JPanel comboPanel;

    /**
     * the panel holding all buttons, the panel is placed on the container
     */
    private JPanel buttonPanel;

    /**
     * this Panel holds, if necessary, the components in which the user enters
     * data for the selected query
     */
    private JPanel queryPanel;

    /**
     * the button which has to be pressed to execute the selected Query in the
     * pull-dowm menue
     */
    private JButton goButton;

    /**
     * the button to close the QueriesWindow
     */
    private JButton closeButton;

    /**
     * the textfield in that the user enters the desired username for his query
     */
    private JTextField usernameTextField = new JTextField("username");

    /**
     * the label for the username textfield
     */
    private JLabel usernameLabel = new JLabel("username");

    /**
     * the field into which the user can enter an id of an event, this used for
     * the query to get all events between two run events. so the linkid of the
     * forst run event is entered into this textfield
     */
    private JTextField firstIDTextField = new JTextField("ID1", 10);

    /**
     * the field into which the user can enter an id of an event, this used for
     * the query to get all events between two run events. so the linkid of the
     * second run event is entered into this textfield
     */
    private JTextField secondIDTextField = new JTextField("ID2", 10);

    /**
     * here the user can enter his own query
     */
    private JTextArea queryField;

    /**
     * a box for selecting a date from a given calendar
     */
    // private DateComboBox dateBox;
    /**
     * the textfield in that the user enters the desired username for his query
     */
    private JTextField dateBox = new JTextField("JJJJ-MM-DD");

    /**
     * the number of the selected item in the pull-down menue
     */
    private int selectedComboIndex;

    /**
     * the DBCommunicator for access to the database
     */
    private DBCommunicator dbCommunicator;

    private SourceModule sourceModule;

    /**
     * constructor
     */
    public QueriesWindow(DBCommunicator dbCom, SourceModule mySourceModule) {
        this.dbCommunicator = dbCom;
        this.sourceModule = mySourceModule;
        // set the size for the this (Queries Window) JPanel
        this.setSize(600, 800);
        setVisible(true);
        /**
         * add the String tho the Vector for the Pull-Down Menue
         */
        addQueriesToPullDownMenue();
        init();
        repaint();
    }

    /**
     * this method adds a given numer of Strings to the Vector which holds the
     * Strings for the selection of the pull-down menue
     * 
     */
    private void addQueriesToPullDownMenue() {
        queries.add(0, "1 - Please select your query here");
        queries.add(1,
                "2 - Alle Ereignisse des Programmierers X am tt.MM.JJJJ ");
        queries.add(2,
                "3 - Alle Ereignisse zwischen Ereignis Run1 und Ereignis Run2");
        queries
                .add(
                        3,
                        "4 - Alle Ereignisse die zwischen zwei weniger als 30 Sekunden auseinander liegenden Run Ereignissen liegen");
        queries.add(4, "5 - eigene Abfrage eingeben");
    }

    /**
     * the Action Listener for the close Button
     */
    ActionListener closeListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    };

    /**
     * in this
     * 
     */
    public void init() {
        // initialize the Components
        msgLabel = new JLabel();
        comboPanel = new JPanel();
        buttonPanel = new JPanel();
        goButton = new JButton("GO");
        goButton.setToolTipText("get the result from the selected query");
        closeButton = new JButton("close");
        closeButton.setToolTipText("close this Window");
        queryPanel = new JPanel();
        queryPanel.setLayout(new BorderLayout(40, 40));
        queryPanel.setSize(700, 400);
        // add the vector with the strings for the selection to the pull-down
        // menue
        combo = new JComboBox(queries);
        combo.setSize(600, 40);
        combo
                .setToolTipText("you can select your query with the corresponding number key");
        // this keySelectionmanager gives the ability to select an item in the
        // Pull-down menue via a number key
        combo.setKeySelectionManager(new JComboBox.KeySelectionManager() {
            public int selectionForKey(char aKey, ComboBoxModel aModel) {
                int pos = Math.abs(aKey - 1 - '0');
                return pos >= aModel.getSize() ? aModel.getSize() - 1 : pos;
            }
        });
        // add the combobox the the combo Panel
        comboPanel.setLayout(new FlowLayout());
        comboPanel.add(combo);
        // add the buttons to the button panel
        buttonPanel.add(msgLabel);
        buttonPanel.add(goButton);
        buttonPanel.add(closeButton);
        // --------------- fields for the first selection -----------
        final JPanel firstChoicePanel = new JPanel();
        firstChoicePanel.setLayout(new FlowLayout());
        usernameLabel = new JLabel("username");
        usernameTextField = new JTextField("testUserName");
        // dateBox = new DateComboBox(new Date(), "yyyy-MM-dd");
        // add components to the Panel for choice 1
        firstChoicePanel.add(usernameLabel);
        firstChoicePanel.add(usernameTextField);
        firstChoicePanel.add(dateBox);
        // ------------------------------------------------------------
        // --------------- fields for the second selection -----------
        final JPanel secondChoicePanel = new JPanel();
        secondChoicePanel.setLayout(new FlowLayout());
        final JLabel firstIDLabel = new JLabel("id of the first event");
        firstIDTextField = new JTextField("ID1", 10);
        final JLabel secondIDLabel = new JLabel("id of the second event");
        secondIDTextField = new JTextField("ID2", 10);
        // --- add components for the second selection ----------------
        secondChoicePanel.add(firstIDLabel);
        secondChoicePanel.add(firstIDTextField);
        secondChoicePanel.add(secondIDLabel);
        secondChoicePanel.add(secondIDTextField);
        // -----------------------------------------------------
        // --- fields for the third selection ---------------
        final JPanel thirdChoicePanel = new JPanel();
        thirdChoicePanel.setLayout(new FlowLayout());
        // --------------------------------------------------------
        // --- fields for the fourth choice ---------------
        final JPanel fourthChoicePanel = new JPanel();
        fourthChoicePanel.setLayout(new FlowLayout());
        queryField = new JTextArea("", 10, 40);
        queryField.setBackground(Color.WHITE);
        queryField.setSize(new Dimension(150, 300));
        // --- add components for the fourth choice ----------------
        fourthChoicePanel.add(queryField);
        // ------------------------------------------------------------------
        /**
         * the item listener receives an Event when the seleced item in the
         * pull-down menue changed
         */
        combo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                // get the source of the Event (the pull-down menue)
                JComboBox selectedChoice = (JComboBox) e.getSource();
                // the index of the selected item in the pull-down menue
                selectedComboIndex = selectedChoice.getSelectedIndex();
                switch (selectedComboIndex) {
                    case 0:
                        // first remove components corresponding to other
                        // selections
                        queryPanel.removeAll();
                        msgLabel.setText(" ");
                        break;
                    case 1:
                        // first remove components corresponding to other
                        // selections
                        queryPanel.removeAll();
                        // add the panel whioch hold the fields for the selected
                        // choice element
                        queryPanel.add(firstChoicePanel, BorderLayout.CENTER);
                        msgLabel
                                .setText("please enter the username and select the desired date");
                        break;
                    case 2:
                        // first remove components corresponding to other
                        // selections
                        queryPanel.removeAll();
                        // add the panel whioch hold the fields for the selected
                        // choice element
                        queryPanel.add(secondChoicePanel, BorderLayout.CENTER);
                        msgLabel
                                .setText("please enter the ids of the two events");
                        break;
                    case 3:
                        // first remove components corresponding to other
                        // selections
                        queryPanel.removeAll();
                        msgLabel.setText("runs with timediff < 30 sec");
                        break;
                    case 4:
                        // first remove components corresponding to other
                        // selections
                        queryPanel.removeAll();
                        // add the panel whioch hold the fields for the selected
                        // choice element
                        queryPanel.add(fourthChoicePanel, BorderLayout.CENTER);
                        msgLabel.setText("Please enter your own query here");
                        break;
                    default:
                        break;
                }
            }
        });
        closeButton.addActionListener(this.closeListener);
        // add the panels with their components to the container of this
        // ResultWindow JFrame
        this.add(comboPanel, BorderLayout.NORTH);
        this.add(queryPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
        // this Listenerreceives an Event if the go button was pressed, which
        // means tat a query has to be executed and the result has to be
        // displayed for the user
        ActionListener goButtonListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showResults();
            }
        };
        goButton.addActionListener(goButtonListener);
    }// END init()

    public String getName() {
        return this.name;
    }

    private void showResults() {
        String username = "";
        String date = "";
        switch (selectedComboIndex) {
            case 0:
                msgLabel.setText("Please select your query first");
                break;
            case 1:
                try {
                    username = usernameTextField.getText();
                }
                catch (NullPointerException npe) {
                    JOptionPane.showMessageDialog(QueriesWindow.this,
                            "please enter a username");
                }
                try {
                    date = dateBox.getText();
                }
                catch (NullPointerException npe) {
                    JOptionPane.showMessageDialog(QueriesWindow.this,
                            "please enter a valid date");
                }
                /**
                 * get the ResultSet containing the comomndata part of the
                 * events which satisfy the query
                 */
                CachedRowSet e1 = DBQueries.getEventsWithUsernameAndDate(
                        username, date, dbCommunicator);
                /**
                 * display the result as a table in a new Window
                 */
                displayQueryResults(e1, 0);
                break;
            case 2:
                String firstID = "";
                String secondID = "";
                try {
                    firstID = firstIDTextField.getText();
                }
                catch (NullPointerException npe) {
                    JOptionPane.showMessageDialog(QueriesWindow.this,
                            "please enter the linkid of the first event");
                }
                try {
                    secondID = secondIDTextField.getText();
                }
                catch (NullPointerException npe) {
                    JOptionPane.showMessageDialog(QueriesWindow.this,
                            "please enter the linkid of the second event");
                }
                CachedRowSet e2 = DBQueries.getEventsBetweenTwoEvents(firstID,
                        secondID, dbCommunicator);
                displayQueryResults(e2, 0);
                break;
            case 3:
                CachedRowSet e3 = DBQueries.getRunsWithTimediff(dbCommunicator);
                displayQueryResults(e3, 1);
                break;
            case 4:
                String userStmt = queryField.getText();
                CachedRowSet userRS = DBQueries.executeUserQuery(userStmt,
                        dbCommunicator);
                displayQueryResults(userRS, 0);
                break;
            default:
                break;
        }
    }

    public void displayQueryResults(CachedRowSet resultSet, final int window) {
        logger.info("display query Results");
        final JTable table = new JTable();
        final CachedRowSet event = resultSet;
        // It may take a while to get the results, so give the user some
        // immediate feedback that their query was accepted.
        msgLabel.setText("Contacting database...");
        // In order to allow the feedback message to be displayed, we don't
        // run the query directly, but instead place it on the event queue
        // to be run after all pending events and redisplays are done.
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    // This is the crux of it all. Use the factory object
                    // to obtain a TableModel object for the query results
                    // and display that model in the JTable component.
                    table.setModel(new TableModelFromResultSet(event));
                    new ResultWindow(table, window, dbCommunicator,
                            sourceModule);
                    // We're done, so clear the feedback message
                    msgLabel.setText(" ");
                }
                catch (SQLException ex) {
                    // If something goes wrong, clear the message line
                    msgLabel.setText(" ");
                    // Then display the error in a dialog box
                    JOptionPane.showMessageDialog(QueriesWindow.this,
                            new String[] { // Display a 2-line message
                            ex.getClass().getName() + ": ", ex.getMessage() });
                }
            }
        });
    }
}
