/*
 * Classname: ISystem
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.system;

import java.util.Observer;

import javax.swing.JFrame;
import org.electrocodeogram.module.registry.IModuleRegistry;
import org.electrocodeogram.msdt.registry.IMsdtRegistry;
import org.electrocodeogram.ui.IGui;

/**
 * The ISystem interface declares methods to be used by ECG Lab's
 * subsystems to access other subsystems. An instance of ISystem is
 * always accesible by a call to the static method
 * {@link org.electrocodeogram.system.System#getInstance()}.
 */
public interface ISystem extends Observer {

    /**
     * This method returns a reference to the implementation of the
     * {@link org.electrocodeogram.msdt.registry.IMsdtRegistry}
     * interface, which is in fact the system's view on the
     * {@link org.electrocodeogram.msdt.registry.MsdtRegistry}.
     * @return A reference to the <code>MsdtRegistry</code>
     */
    IMsdtRegistry getMsdtRegistry();

    /**
     * This method returns a reference to the implementation of the
     * {@link org.electrocodeogram.msdt.registry.IMsdtRegistry}
     * interface, which is in fact the system's view on the
     * {@link org.electrocodeogram.msdt.registry.MsdtRegistry}.
     * @return A reference to the <em>MicroSensorDataType</em>
     *         registry
     */
    IModuleRegistry getModuleRegistry();

    /**
     * This method returns a reference to the implementation of the
     * {@link org.electrocodeogram.ui.IGui} interface.
     * {@link org.electrocodeogram.msdt.registry.MsdtRegistry}.
     * @return A reference to the GUI object.
     */
    IGui getGui();

    /**
     * This method returns a reference to the main frame of the ECG
     * Lab's graphical user interface.
     * @return A reference to the GUI's main frame object.
     */
    JFrame getMainWindow();

    /**
     * This method is used to quit the ECG Lab application in case of no GUI
     */
    void quit();

    /**
     * When an ECG Lab subsystem has changed its state in a way that
     * is interesting to modules, it calls this method. This will lead
     * to a notification to all modules about the statechange.
     * @param object Is the object that has changed its state
     */
    void fireStateChange(Object object);
}
