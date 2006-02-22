/*
 * Class: UIModule
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module;

import javax.swing.JPanel;


/**
 * This interface must be implemented by modules, which want to contribute to the GUI of the ECG Lab.
 * Such modules can provide a panel to display.
 */
public interface UIModule {

    /**
     * Is getting the name of the panel.
     * @return The panel's name
     */
    String getPanelName();

    /**
     * Is getting the panel itself.
     * @return The panel
     */
    JPanel getPanel();

}

