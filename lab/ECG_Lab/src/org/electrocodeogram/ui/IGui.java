/*
 * Class: IGui
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.ui;

import java.util.Observer;

public interface IGui extends Observer {

    /**
     * Brings up a dialog showing detailed information about the
     * selected module.
     */
    void showModuleDetails();

    /**
     * This method brings the event window to front and creates it if
     * needed. The event window is showing which events are currently
     * passing a module.
     */
    void showMessagesWindow();

    /**
     * When the user has initiated to enter a new module connection,
     * the GUI will go into the <em>ModuleConnectionMode</em>,
     * until the connection has been made or the action is aborted by
     * the user.
     * @param id
     *            Is the unique int id of the currently selected
     *            module.
     */
    void enterModuleConnectionMode(int id);

    /**
     * When the user has initiated to enter a new module connection,
     * the GUI will go into the <em>ModuleConnectionMode</em>,
     * until the connection has been made or the action is aborted by
     * the user.
     */
    void exitModuleConnectionMode();

    /**
     * Tells if the GUI is currently in <em>ModuleConnectionMode</em>.
     * @return <code>true</code> idf the GUi is in
     *         <em>ModuleConnectionMode</em> and <code>false</code>
     *         if not.
     */
    boolean getModuleConnectionMode();

    /**
     * The id of the module that is the source of a conection.
     * @return The id of the module that is the source of a conection
     */
    int getSourceModule();

    /**
     * Brings up a dialog showing detailed information about the
     * selected <em>ModulePackage</em>.
     */
    void showModuleFinderDetails();

    /**
     * When a module is selected by the user, the module menu is
     * accesible. When no module is selcted, the module menu is
     * disabled.
     * @param enable
     *            <code>true</code> to enable the module menu and
     *            <code>false</code> to disable it
     */
    void enableModuleMenu(boolean enable);

}
