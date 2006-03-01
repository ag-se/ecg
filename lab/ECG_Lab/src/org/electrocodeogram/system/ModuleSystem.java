/*
 * Classname: ModuleSystem
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.system;

/**
 * This class is providing access to the class {@link Core} for the
 * ECG modules.
 */
public final class ModuleSystem {

    /**
     * The constuctor is hidden for this utility-class.
     */
    private ModuleSystem() {
    // empty block
    }

    /**
     * A call to this method returns the <em>Singleton</em> instance
     * of the <code>IModuleSystem</code> implementation.
     * @return The <em>Singleton</em> instance the <code>IModuleSystem</code>
     *         implementation
     */
    public static IModuleSystem getInstance() {
        Core core = Core.getInstance();

        return core;
    }
}
