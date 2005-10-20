package org.electrocodeogram.module;

import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * A ModuleProperty is a property of a module that is changeable by
 * the user during runtime. Module properties are declared in the
 * module's "module.properties.xml" file, that is provided by the
 * module's developer with each module. This file is parsed in the
 * ECG's initialization phase and each declared module property
 * becomes a ModuleProperty object registered along with the other
 * module informations in the ModuleRegistry.
 */
public class ModuleProperty extends Observable {

    /**
     * This is this class' logger.
     */
    private static Logger logger = LogHelper.createLogger(ModuleProperty.class
        .getName());

    /**
     * The property name.
     */
    private String propName;

    /**
     * The current property value.
     */
    private String propValue;

    /**
     * The last property value.
     */
    private String backupValue;

    /**
     * The property type.
     */
    private Class propType;

    /**
     * This creates the ModuleProperty.
     * @param name
     *            Is the name of the property
     * @param value
     *            Is a default value to use before the user sets
     *            antoher value
     * @param type
     *            Is the type of the property. The type must always be
     *            a java class like "java.lang.Integer"
     */
    public ModuleProperty(final String name, final String value,
        final Class type) {

        logger.entering(this.getClass().getName(), "ModuleProperty");

        this.propName = name;

        this.propValue = value;

        this.propType = type;

        logger.exiting(this.getClass().getName(), "ModuleProperty");
    }

    /**
     * This returns the property's name.
     * @return The property's name
     */
    public final String getName() {
        return this.propName;
    }

    /**
     * This returns the property's default value.
     * @return The property's default value
     */
    public final String getValue() {
        return this.propValue;
    }

    /**
     * This returns the property's type.
     * @return The property's type
     */
    public final Class getType() {
        return this.propType;
    }

    /**
     * Sets the value of the property to the given value
     * and stores the old value for backup.
     * @param value Is the new value of the property
     */
    public final void setValue(final String value) {

        logger.entering(this.getClass().getName(), "setValue",
            new Object[] {value});

        this.backupValue = this.propValue;

        this.propValue = value;

        logger.log(Level.INFO, "The value of ModuleProperty " + this.getName()
                               + " has changed to " + this.propValue);

        setChanged();

        notifyObservers();

        clearChanged();

        logger.exiting(this.getClass().getName(), "setValue");
    }

    /**
     * This restores the value of the property in case
     * of a {@link org.electrocodeogram.module.ModulePropertyException}.
     */
    public final void restore() {

        logger.entering(this.getClass().getName(), "restore");

        this.propValue = this.backupValue;

        logger.log(Level.INFO, "The value of ModuleProperty " + this.getName()
                               + " has been reset to " + this.propValue);

        logger.exiting(this.getClass().getName(), "restore");

    }
}
