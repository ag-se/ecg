package org.electrocodeogram.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

/**
 * If the ECG Lab is started with the "-nogui" command line parameter,
 * a graphical user interface is not created. This is usefull for
 * the <em>Inlineserver</em> mode, where the ECG Lab is started from
 * inside the sensor environment automatically.<br>
 * To allow the user to terminate the ECG Lab in that case,
 * a tray icon is shown in the system tray. This is only the case
 * if the ECG Lab is running on a Microsoft Windows platform. 
 */
public class TrayUI {

    SystemTray tray = SystemTray.getDefaultSystemTray();

    public TrayUI() {

        JPopupMenu menu;
        JMenuItem menuItem;

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        menu = new JPopupMenu("A Menu");

        menuItem = new JMenuItem("Quit ECG Lab (Inlineserver)");
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                org.electrocodeogram.system.System.getInstance().quit();

            }
        });
        menu.add(menuItem);

        ImageIcon i = new ImageIcon("duke.gif");

        TrayIcon ti = new TrayIcon(i,
            "ElectroCodeGram - ECG Lab (Inlineserver)", menu);

        ti.setIconAutoSize(true);
        ti.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                    "ElectroCodeoGram - www.electrocodeogram.org", "About",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        tray.addTrayIcon(ti);

    }

}
