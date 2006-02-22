/*
 * Class: SmallUI
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.electrocodeogram.misc.constants.StringConstants;
import org.electrocodeogram.system.System;

/**
 * When the ECG Lab is started with the command line parameter
 * "-nogui", the graphical user interface (GUI) of the ECG Lab is not
 * displayed. To enable the user to terminate the ECG Lab even in this
 * case, a minimal GUI only containing a quit-button is shown.<br>
 * The primary usecase for the "-nogui" parameter is the
 * <em>Inlineserver</em> mode, where the ECG Lab is started form
 * within the sensor environment automatically. In
 * <em>Inlineserver</em> mode the ECG Lab is started when the sensor
 * is started and it is terminated when the sensor is shut down with
 * his environmental application. But if for any reasons this
 * application crashes, the sensor can not terminate the ECG Lab,
 * which will run until system shutdown.<br>
 * The <em>SmallUI</em> provides a way for the user to terminate the
 * ECG Lab in such a case.
 */
public class SmallUI extends JFrame {

    /**
     * This is the <em>Serialization</em> id.
     */
    private static final long serialVersionUID = 7264907181891834020L;

    /**
     * Creates the UI.
     */
    public SmallUI() {

        super();

        this.setTitle(StringConstants.SMALL_UI_TITLE);

        JButton btnQuit = new JButton(StringConstants.SMALL_UI_QUIT_BUTTON_NAME);

        btnQuit.addActionListener(new ActionListener() {

            public void actionPerformed(@SuppressWarnings("unused")
            final ActionEvent e) {

                int result = JOptionPane
                    .showConfirmDialog(
                        System.getInstance().getMainWindow(),
                        "Are you sure? If you quit the ECG Lab, no event data is recorded anymore.",
                        "Confirm", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                switch (result) {
                    case JOptionPane.YES_OPTION:

                        org.electrocodeogram.system.System.getInstance().quit();

                        break;

                    case JOptionPane.NO_OPTION:

                        break;

                    default:

                        break;

                }

            }

        });

        this.getContentPane().add(btnQuit);

        this.pack();

        this.setVisible(true);
    }

}
