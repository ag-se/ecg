/*
 * Class: ModuleDescriptor
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */
package org.electrocodeogram.modulepackage;

import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.msdt.MicroSensorDataType;

/**
 * Collects the information from a <em>module.properties.xml</em> file of a <em>ModulePackage</em>.
 */
public class ModuleDescriptor {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(ModuleDescriptor.class.getName());

    /**
     * Is the unique string id of a
     * <em>ModulePackage</em>.
     */
    private String packageId;

    /**
     * Is the name of the module.
     */
    private String moduleName;

    /**
     * Is the name of the module provider.
     */
    private String moduleProviderName;

    /**
     * Is the version information.
     */
    private String moduleVersion;

    /**
     * Is the module's <code>Class</code>, from which it will be
     * created.
     */
    private Class moduleClazz;

    /**
     * Contains the <em>ModuleProperties</em>.
     */
    private ModuleProperty[] moduleProperties;

    /**
     * Contains the module's <em>MicroSensorDataTypes</em>.
     */
    private MicroSensorDataType[] microSensorDataTypes;

    /**
     * Is a description for the module.
     */
    private String moduleDescription;

    /**
     * Is the <em>MODULE_TYPE</em> of the module.
     */
    private ModuleType moduleType;

    /**
     * This creates a new <em>ModuleDescriptor</em> with the given
     * values.
     * @param id
     *            Is the unique string id of the
     *            <em>ModulePackage</em>
     * @param name
     *            Is the name of the module
     * @param providerName
     *            Is the name of the module provider
     * @param version
     *            Is the version of the module
     * @param clazz
     *            This is the module's <code>Class</code>
     * @param description
     *            Is the module description.
     * @param type
     *            Is the <em>MODULE_TYPE</em> of the module
     * @param properties
     *            Is an array of <em>ModuleProperties</em> for the
     *            module
     * @param msdts
     *            Is an array of <em>MicroSensorDataType</em> for
     *            the module
     */
    public ModuleDescriptor(final String id, final String name,
        final String providerName, final String version, final Class clazz,
        final String description, final ModuleType type,
        final ModuleProperty[] properties, final MicroSensorDataType[] msdts) {

        logger.entering(this.getClass().getName(), "ModuleDescriptor",
            new Object[] {id, name, providerName, version, clazz, description,
                type, properties, msdts});

        this.packageId = id;

        this.moduleName = name;

        this.moduleProviderName = providerName;

        this.moduleVersion = version;

        this.moduleClazz = clazz;

        this.moduleType = type;

        this.moduleDescription = description;

        if (properties != null) {
            int size = properties.length;

            this.moduleProperties = new ModuleProperty[size];

            for (int i = 0; i < size; i++) {
                this.moduleProperties[i] = properties[i];
            }
        }

        if (msdts != null) {
            int size = msdts.length;

            this.microSensorDataTypes = new MicroSensorDataType[size];

            for (int i = 0; i < size; i++) {
                this.microSensorDataTypes[i] = msdts[i];
            }
        }

        logger.exiting(this.getClass().getName(), "ModuleDescriptor");

    }

    /**
     * This returns the module's description.
     * @return The module's description
     */
    public final String getDescription() {

        logger.entering(this.getClass().getName(), "getDescription");

        logger.exiting(this.getClass().getName(), "getDescription",
            this.moduleDescription);

        return this.moduleDescription;
    }

    /**
     * This method returns the module's class.
     * @return The module's class
     */
    public final Class getClazz() {

        logger.entering(this.getClass().getName(), "getClazz");

        logger.exiting(this.getClass().getName(), "getClazz", this.moduleClazz);

        return this.moduleClazz;
    }

    /**
     * This method returns the module's name.
     * @return The module's name
     */
    public final String getName() {

        logger.entering(this.getClass().getName(), "getName");

        logger.exiting(this.getClass().getName(), "getName", this.moduleName);

        return this.moduleName;
    }

    /**
     * This method returns an array of
     * <em>ModuleProperties</em>.
     * @return An array of <em>ModuleProperties</em>
     */
    public final ModuleProperty[] getProperties() {

        logger.entering(this.getClass().getName(), "getProperties");

        if (this.moduleProperties == null) {

            logger.exiting(this.getClass().getName(), "getProperties", null);

            return new ModuleProperty[0];
        }

        int size = this.moduleProperties.length;

        ModuleProperty[] toReturn = new ModuleProperty[size];

        for (int i = 0; i < size; i++) {

            String name = this.moduleProperties[i].getName();

            String value = this.moduleProperties[i].getValue();

            Class clazz = this.moduleProperties[i].getType();

            toReturn[i] = new ModuleProperty(name, value, clazz);
        }

        logger.exiting(this.getClass().getName(), "getProperties", toReturn);

        return toReturn;
    }

    /**
     * This method returns the unique string id of the
     * <em>ModulePackage</em> for this <code>ModuleDescriptor</code>.
     * @return The unique string id of the
     * <em>ModulePackage</em>
     */
    public final String getId() {

        logger.entering(this.getClass().getName(), "getId");

        logger.exiting(this.getClass().getName(), "getId", this.packageId);

        return this.packageId;
    }

    /**
     * This method returns the <em>MicroSensorDataTypes</em> that
     * are provided.
     * @return The <em>MicroSensorDataTypes</em> that are provided.
     */
    public final MicroSensorDataType[] getMicroSensorDataTypes() {

        logger.entering(this.getClass().getName(), "getMicroSensorDataTypes");

        if (this.microSensorDataTypes == null) {

            logger.exiting(this.getClass().getName(),
                "getMicroSensorDataTypes", null);

            return new MicroSensorDataType[0];
        }

        int size = this.microSensorDataTypes.length;

        MicroSensorDataType[] toReturn = new MicroSensorDataType[size];

        for (int i = 0; i < size; i++) {
            toReturn[i] = this.microSensorDataTypes[i];
        }

        logger.exiting(this.getClass().getName(), "getMicroSensorDataTypes",
            toReturn);

        return toReturn;
    }

    /**
     * This method returns the <em>ModulePackage's</em> provider name.
     * @return The <em>ModulePackage's</em> provider name
     */
    public final String getProviderName() {

        logger.entering(this.getClass().getName(), "getProviderName");

        logger.exiting(this.getClass().getName(), "getProviderName",
            this.moduleProviderName);

        return this.moduleProviderName;
    }

    /**
     * This method returns the <em>ModulePackage's</em> version.
     * @return The <em>ModulePackage's</em> version
     */

    public final String getVersion() {

        logger.entering(this.getClass().getName(), "getVersion");

        logger.exiting(this.getClass().getName(), "getVersion",
            this.moduleVersion);

        return this.moduleVersion;
    }

    /**
     * This returns the <em>ModulePackage's</em> <em>MODULE_TYYPE</em>.
     * @return The <em>ModulePackage's</em> <em>MODULE_TYYPE</em>
     */
    public final ModuleType getModuleType() {

        logger.entering(this.getClass().getName(), "getModuleType");

        logger.exiting(this.getClass().getName(), "getModuleType",
            this.moduleType);

        return this.moduleType;
    }
}


