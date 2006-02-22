/*
 * Class: EventWindow
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.ui.event;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.electrocodeogram.misc.constants.UIConstants;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.event.ValidEventPacket.DELIVERY_STATE;
import org.electrocodeogram.misc.constants.StringConstants;
import org.electrocodeogram.module.registry.ModuleInstanceNotFoundException;

/**
 * This is a window to display the events that are passing a module.
 */
public class EventWindow extends JFrame {

    /**
     * This is the <em>Serialization</em> id.
     */
    private static final long serialVersionUID = -8375101165326048034L;

    /**
     * Contains the {@link #textArea} to display the events.
     */
    private JPanel pnlMessages;

    /**
     * A scrollpane.
     */
    private JScrollPane scrollPane;

    /**
     * Here the events are displayed.
     */
    private JTextArea textArea;

    /**
     * This toggles which events are to be shown. Only those sent by
     * the according module, or only those received.
     */
    private DELIVERY_STATE deliveryState;

    /**
     * Used to automatically scroll the scollpane as new events are
     * appended.
     */
    private boolean autoscroll = false;

    /**
     * A reference to the border of this window.
     */
    private TitledBorder titledBorder = null;

    /**
     * The menu entry to allow only sent events to be displayed.
     */
    private JRadioButtonMenuItem menuSent;

    /**
     * The menu entry to allow only received events to be displayed.
     */
    private JRadioButtonMenuItem menuReceived;

    /**
     * Creates an <em>EventWindow</em> for events passing the module
     * with the given id.
     * @param id
     *            Is the unique int id of the module
     * @throws ModuleInstanceNotFoundException
     *             If the given module id is not known
     */
    public EventWindow(final int id) throws ModuleInstanceNotFoundException {

        this.setTitle(StringConstants.EVENT_WINDOW_TITLE);

        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        this.setBounds(0, 0, UIConstants.DEFAULT_EVENT_WINDOW_WIDTH,
            UIConstants.DEFAULT_EVENT_WINDOW_HEIGHT);

        JMenuBar menuBar = new JMenuBar();

        JMenu menuView = new JMenu(StringConstants.EVENT_WINDOW_MENU_VIEW_TITLE);

        this.menuSent = new JRadioButtonMenuItem(
            StringConstants.EVENT_WINDOW_SENT_MENU_ENTRY_TITLE);

        this.menuSent.setSelected(true);

        this.menuSent.addActionListener(new ActionListener() {

            @SuppressWarnings("synthetic-access")
            public void actionPerformed(@SuppressWarnings("unused")
            final ActionEvent e) {
                EventWindow.this.deliveryState = DELIVERY_STATE.SENT;

                try {
                    EventWindow.this.titledBorder = new TitledBorder(
                        new LineBorder(
                            UIConstants.FRM_EVENT_WINDOW_BORDER_COLOR,
                            UIConstants.FRM_EVENT_WINDOW_BORDER_WIDTH,
                            UIConstants.BORDER_ROUNDED),
                        StringConstants.EVENT_WINDOW_MODULE_SELECTED_AND_SENT
                                        + org.electrocodeogram.system.System
                                            .getInstance().getModuleRegistry()
                                            .getModule(id).getName() + ", "
                                        + id);
                } catch (ModuleInstanceNotFoundException e1) {

                    JOptionPane.showMessageDialog(
                        org.electrocodeogram.system.System.getInstance()
                            .getMainWindow(), e1.getMessage(), e1.getClass()
                            .getName(), JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        this.menuReceived = new JRadioButtonMenuItem(
            StringConstants.EVENT_WINDOW_RECEIVED_MENU_ENTRY_TITLE);

        this.menuReceived.addActionListener(new ActionListener() {

            @SuppressWarnings("synthetic-access")
            public void actionPerformed(@SuppressWarnings("unused")
            final ActionEvent e) {
                EventWindow.this.deliveryState = DELIVERY_STATE.RECEIVED;

                try {
                    EventWindow.this.titledBorder = new TitledBorder(
                        new LineBorder(
                            UIConstants.FRM_EVENT_WINDOW_BORDER_COLOR,
                            UIConstants.FRM_EVENT_WINDOW_BORDER_WIDTH,
                            UIConstants.BORDER_ROUNDED),
                        StringConstants.EVENT_WINDOW_MODULE_SELECTED_AND_RECEIVED
                                        + org.electrocodeogram.system.System
                                            .getInstance().getModuleRegistry()
                                            .getModule(id).getName()
                                        + ", "
                                        + id);
                } catch (ModuleInstanceNotFoundException e1) {
                    JOptionPane.showMessageDialog(
                        org.electrocodeogram.system.System.getInstance()
                            .getMainWindow(), e1.getMessage(), e1.getClass()
                            .getName(), JOptionPane.ERROR_MESSAGE);
                }

            }
        });

        ButtonGroup group = new ButtonGroup();

        group.add(this.menuSent);

        group.add(this.menuReceived);

        menuView.add(this.menuSent);

        menuView.add(this.menuReceived);

        // menuView.add(this.menuClear);

        menuBar.add(menuView);

        this.setJMenuBar(menuBar);

        this.textArea = new JTextArea();
        this.textArea.setEditable(false);
        this.textArea.setLineWrap(true);

        this.scrollPane = new JScrollPane(this.textArea);

        this.scrollPane
            .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.scrollPane
            .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.scrollPane.getVerticalScrollBar().getModel().addChangeListener(
            new ChangeListener() {

                @SuppressWarnings("synthetic-access")
                public void stateChanged(@SuppressWarnings("unused")
                final ChangeEvent e) {
                    if (EventWindow.this.autoscroll) {
                        JScrollBar vertBar = EventWindow.this.scrollPane
                            .getVerticalScrollBar();
                        vertBar.setValue(vertBar.getMaximum());
                        EventWindow.this.autoscroll = false;
                    }

                }
            });

        this.titledBorder = new TitledBorder(new LineBorder(
            UIConstants.FRM_EVENT_WINDOW_BORDER_COLOR,
            UIConstants.FRM_EVENT_WINDOW_BORDER_WIDTH,
            UIConstants.BORDER_ROUNDED),
            StringConstants.EVENT_WINDOW_MODULE_SELECTED_AND_SENT
                            + org.electrocodeogram.system.System.getInstance()
                                .getModuleRegistry().getModule(id).getName()
                            + ", " + id);

        this.pnlMessages = new JPanel(new GridLayout(1, 1));
        this.pnlMessages.setBorder(this.titledBorder);
        this.pnlMessages
            .setBackground(UIConstants.FRM_EVENT_WINDOW_BACKGROUND_COLOR);
        this.pnlMessages.add(this.scrollPane);

        this.getContentPane().add(this.pnlMessages);

        this.deliveryState = DELIVERY_STATE.SENT;

    }

    /**
     * This method is used to append a line of text to be displayed.
     * @param text
     *            Is the text to be apppended
     */
    private void append(final String text) {
        this.textArea.append(text);

        JScrollBar vertBar = this.scrollPane.getVerticalScrollBar();

        if (vertBar.getValue() == vertBar.getMaximum()
                                  - vertBar.getVisibleAmount()) {
            this.autoscroll = true;
        }
    }

    /**
     * Adds a new event to this window.
     * @param event
     *            Is thenew event that is to be displayed
     */
    public final void appendEvent(final ValidEventPacket event) {
        if (event.getDeliveryState() != null) {
            if (event.getDeliveryState().equals(this.deliveryState)) {
                this.textArea.append(event.getTimeStamp().toString() + ","
                                     + event.getSensorDataType());

                List argList = event.getArgList();

                if (argList != null) {

                    Object[] args = event.getArgList().toArray();

                    int count = args.length;

                    for (int i = 0; i < count; i++) {
                        String str = (String) args[i];

                        if (str.equals("")) {
                            continue;
                        }
                        this.append("," + str);

                    }

                }
                this.append("\n");
            }
        }

    }
}
