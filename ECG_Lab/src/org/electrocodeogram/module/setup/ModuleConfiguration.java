/*
 * Class: ModuleConfiguration
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.setup;

import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.ModuleProperty;

/**
 * A <em>ModuleConfiguration</em> is a collection of information
 * about a module instance in the ECG Lab.
 * <em>ModuleConfigurations</em> are defined per module and are
 * telling for example to which other modules a module is connected,
 * which <em>ModuleProperties</em> and if the module is active or
 * inactive.<br>
 * The set of <em>ModuleConfigurations</em> for each module instance
 * is a <em>ModuleSetup</em> and is represented by
 * {@link ModuleSetup}. <em>ModuleSetups</em> can be stored into
 * files and loaded from files into the ECG Lab.
 */
public class ModuleConfiguration {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(ModuleConfiguration.class.getName());

    /**
     * The name of the module.
     */
    private String moduleName;

    /**
     * The unique int id of the module.
     */
    private int moduleId;

    /**
     * The state of the module.
     */
    private boolean isActive;

    /**
     * This is an <code>Array</code> containing all
     * <em>ModuleProperties</em> with their runtime values.
     */
    private ModuleProperty[] moduleProperties;

    /**
     * An <code>Array</code> of unique module ids from modules this
     * module is connected to.
     */
    private Integer[] connectedTo;

    /**
     * The unique <code>String</code> id of the
     * <em>ModulePackage</em>.
     */
    private String modulePackageId;

    /**
     * This returns the id of the <em>ModulePackage</em>.
     * @return The id of the <em>ModulePackage</em>
     */
    public final String getModulePackageId() {

        logger.entering(this.getClass().getName(), "getModulePackageId");

        logger.exiting(this.getClass().getName(), "getModulePackageId",
            this.modulePackageId);

        return this.modulePackageId;
    }

    /**
     * This creates a new <em>ModuleConfiguration</em> for a module
     * instance.
     * @param to
     *            Is an <code>Arrray</code> of nique int ids from
     *            modules that the module is connected to
     * @param id
     *            Is the unique int id of the module
     * @param name
     *            Is the name of the module
     * @param properties
     *            Is an <code>Array</code> of the
     *            <em>ModuleProperties</em> with their runtime
     *            values
     * @param packageId
     *            Is the unique <code>String</code> id of the
     *            <em>ModulePackage</em>
     * @param active
     *            Is the state of the module
     */
    public ModuleConfiguration(final Integer[] to, final int id,
        final String name, final boolean active,
        final ModuleProperty[] properties, final String packageId) {

        logger.entering(this.getClass().getName(), "ModuleConfiguration",
            new Object[] {to, new Integer(id), name, new Boolean(active),
                properties, packageId});

        this.moduleId = id;

        this.moduleName = name;

        this.modulePackageId = packageId;

        this.isActive = active;

        if (properties != null) {

            int size = properties.length;

            this.moduleProperties = new ModuleProperty[size];

            for (int i = 0; i < size; i++) {
                this.moduleProperties[i] = properties[i];
            }

        }

        if (to != null) {

            int size = to.length;

            this.connectedTo = new Integer[size];

            for (int i = 0; i < size; i++) {
                this.connectedTo[i] = to[i];
            }

        }

        logger.exiting(this.getClass().getName(), "ModuleConfiguration");

    }

    /**
     * Returns the unique int ids of modules the module is connected
     * to.
     * @return The unique int ids of modules the module is connected
     *         to
     */
    public final Integer[] getConnectedTo() {

        logger.entering(this.getClass().getName(), "getConnectedTo");

        if (this.connectedTo == null) {

            logger.exiting(this.getClass().getName(), "getConnectedTo", null);

            return null;
        }

        int size = this.connectedTo.length;

        Integer[] toReturn = new Integer[size];

        for (int i = 0; i < size; i++) {
            toReturn[i] = this.connectedTo[i];
        }

        logger.exiting(this.getClass().getName(), "getConnectedTo", toReturn);

        return toReturn;
    }

    /**
     * Return the unique int id of the module.
     * @return The unique int id of the module
     */
    public final int getModuleId() {

        logger.entering(this.getClass().getName(), "getModuleId");

        logger.exiting(this.getClass().getName(), "getModuleId", new Integer(
            this.moduleId));

        return this.moduleId;
    }

    /**
     * Returns the name of the module.
     * @return The name of the module
     */
    public final String getModuleName() {

        logger.entering(this.getClass().getName(), "getModuleId");

        logger.exiting(this.getClass().getName(), "getModuleId",
            this.moduleName);

        return this.moduleName;
    }

    /**
     * Returns the <em>ModuleProperties</em> with their runtime
     * values.
     * @return The <em>ModuleProperties</em> with their runtime
     *         values
     */
    public final ModuleProperty[] getModuleProperties() {

        logger.entering(this.getClass().getName(), "getModuleProperties");

        if (this.moduleProperties == null) {

            logger.exiting(this.getClass().getName(), "getModuleProperties",
                null);

            return null;
        }

        int size = this.moduleProperties.length;

        ModuleProperty[] toReturn = new ModuleProperty[size];

        for (int i = 0; i < size; i++) {
            toReturn[i] = this.moduleProperties[i];
        }

        logger.exiting(this.getClass().getName(), "getModuleProperties",
            toReturn);

        return toReturn;
    }

    /**
     * Retuns the state of the module.
     * @return The state of the module
     */
    public final boolean isActive() {

        logger.entering(this.getClass().getName(), "isActive");

        logger.exiting(this.getClass().getName(), "isActive", new Boolean(this.isActive));

        return this.isActive;
    }
}
