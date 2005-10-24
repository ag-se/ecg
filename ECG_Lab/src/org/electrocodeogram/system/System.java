/*
 * Classname: System
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.system;

/**
 * This class is providing access to the class {@link Core} for the
 * subsystems.
 */
public final class System {

    /**
     * The constuctor is hidden for this utility-class.
     */
    private System() {
    // empty block

    }

    /**
     * A call to this method returns the <em>Singleton</em> instance
     * the ISystem implementation.
     * @return The <em>Singleton</em> instance the ISystem
     *         implementation
     */
    public static ISystem getInstance() {
        Core core = Core.getInstance();

        return core;
    }

}
