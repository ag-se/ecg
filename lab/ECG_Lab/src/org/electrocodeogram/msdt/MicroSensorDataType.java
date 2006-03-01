/*
 * Class: MicroSensorDataType
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.msdt;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.validation.Schema;

import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.msdt.registry.MicroSensorDataTypeRegistrationException;

/**
 * A <code>MicroSensorDataType</code> is a type of an actual
 * <em>MicroActivityEvent</em>. These events are carried inside
 * {@link org.electrocodeogram.event.ValidEventPacket} in thh ECG.
 * Each <em>MicroActivityEvent</em> is an instance of one <em>MicroSensorDataType</em>. A
 * <em>MicroSensorDataType</em> contains a XML schema and
 * each <em>MicroActivityEvent</em> accroding to this type is
 * containing an XML document that is an instance of this type's XML
 * schema. In addition a <em>MicroSensorDataType</em> has a unique
 * name in the ECG, which is the name of its XML schema file.
 */
public class MicroSensorDataType {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(MicroSensorDataType.class.getName());

    /**
     * This is the XML schema contained in this
     * <em>MicroSensorDataType</em>.
     */
    private Schema mySchema;

    /**
     * The unique name of the <code>MicroSensorDataType</code>.
     */
    private String myName;

    /**
     * A static counter.
     */
    private static int count;

    /**
     * The unique int id of this <code>MicroSensorDataType</code>.
     */
    private int id;

    /**
     * This is a reference to the file containing the XML schema.
     */
    private File myDefFile;

    /**
     * A <code>MicroSensorDataType</code> can be provided by multiple
     * modules. Each module which provides this MSDT is kept as a reference here.
     */
    private ArrayList<MsdtProvider> providingModules;

    /**
     * This creates a <code>MicroSensorDataType</code> and assigns a
     * unique int id to it.
     * @param name
     *            Is the name for the new <code>MicroSensorDataType</code>.
     * @param schema
     *            Is the XML schema that actually defines this type
     * @param defFile
     *            Is the file containing the XML schema
     * @throws MicroSensorDataTypeException
     *             If the given name is empty or if the XML schema is
     *             null
     */
    public MicroSensorDataType(final String name, final Schema schema,
        final File defFile) throws MicroSensorDataTypeException {

        logger.entering(this.getClass().getName(), "MicroSensorDataType",
            new Object[] {name, schema, defFile});

        this.id = ++count;

        this.myDefFile = defFile;

        if (name == null || name.equals("")) {

            logger.exiting(this.getClass().getName(), "MicroSensorDataType");

            throw new MicroSensorDataTypeException(
                "The given name is \"null\" or empty.");
        }

        this.myName = name;

        if (schema == null) {

            logger.exiting(this.getClass().getName(), "MicroSensorDataType");

            throw new MicroSensorDataTypeException(
                "The given XML schema is \"null\".");
        }

        this.mySchema = schema;

        this.providingModules = new ArrayList<MsdtProvider>();

        logger.exiting(this.getClass().getName(), "MicroSensorDataType");

    }

    /**
     * This returns the name of the <code>MicroSensorDataType</code>.
     * @return The name of the <code>MicroSensorDataType</code>
     */
    public final String getName() {

        logger.entering(this.getClass().getName(), "getName");

        logger.exiting(this.getClass().getName(), "getName", this.myName);

        return this.myName;
    }

    /**
     * Returns a reference to the file containing the XML schema.
     * @return The XMl schema file
     */
    public final File getDefFile() {

        logger.entering(this.getClass().getName(), "getDefFile");

        logger.exiting(this.getClass().getName(), "getDefFile", this.myDefFile);

        return this.myDefFile;
    }

    /**
     * This method returns the XML schema of this
     * <code>MicroSensorDataType</code>.
     * @return The XML schema of the <code>MicroSensorDataType</code>
     */
    public final Schema getSchema() {

        logger.entering(this.getClass().getName(), "getSchema");

        logger.exiting(this.getClass().getName(), "getSchema", this.mySchema);

        return this.mySchema;
    }

    /**
     * This method returns the unique integer id of the
     * <code>MicroSensorDataType</code>.
     * @return The unique integer id of the
     *         <code>MicroSensorDataType</code>
     */
    public final int getId() {

        logger.entering(this.getClass().getName(), "getId");

        logger
            .exiting(this.getClass().getName(), "getId", new Integer(this.id));

        return this.id;
    }

    /**
     * Every module that provides this <code>MicroSensorDataType</code>
     * calls this method during its creation and is then added to this
     * {@link #providingModules} list. A
     * <code>MicroSensorDataType</code> is unloaded from the {@link org.electrocodeogram.msdt.registry.MsdtRegistry}
     * when there are no more module instances in the ECG Lab that are providing it.
     * @param provider
     *            Is the module that provides this <code>MicroSensorDataTyp</code>
     * @throws MicroSensorDataTypeRegistrationException
     *             If the module parameter is <code>null</code>
     */
    public final void addProvidingModule(final MsdtProvider provider)
        throws MicroSensorDataTypeRegistrationException {

        logger.entering(this.getClass().getName(), "addProvidingModule",
            new Object[] {provider});

        if (provider == null) {

            logger.exiting(this.getClass().getName(), "addProvidingModule");

            throw new MicroSensorDataTypeRegistrationException(
                "The given module is of value \"null\"");
        }

        if (this.providingModules.contains(provider)) {

            logger.exiting(this.getClass().getName(), "addProvidingModule");

            return;
        }

        this.providingModules.add(provider);

        logger.log(Level.FINE, "Registered the module " + provider.getName()
                               + " with the MSDT " + this.getName() + ".");

        logger.exiting(this.getClass().getName(), "addProvidingModule");
    }

    /**
     * Every module that provides this <code>MicroSensorDataType</code>
     * calls this method during its removal and is then removed from the
     * {@link #providingModules} list. A
     * <em>MicroSensorDataType</em> is unloaded from the {@link org.electrocodeogram.msdt.registry.MsdtRegistry}
     * when there are no more module instances in the ECG Lab that are providing it.
     * @param provider
     *            Is the module to remove
     * @throws MicroSensorDataTypeRegistrationException
     *             If the given module parameter is <code>null</code>
     */
    public final void removeProvidingModule(final MsdtProvider provider)
        throws MicroSensorDataTypeRegistrationException {

        logger.entering(this.getClass().getName(), "removeProvidingModule",
            new Object[] {provider});

        if (provider == null) {

            logger.exiting(this.getClass().getName(), "removeProvidingModule");

            throw new MicroSensorDataTypeRegistrationException(
                "The given module is of value \"null\"");
        }

        if (!this.providingModules.contains(provider)) {

            logger.exiting(this.getClass().getName(), "removeProvidingModule");

            return;
        }

        this.providingModules.remove(provider);

        if (this.providingModules.size() == 0) {
            try {
                org.electrocodeogram.system.System.getInstance()
                    .getMsdtRegistry().deregisterMsdt(this);
            } catch (MicroSensorDataTypeRegistrationException e) {

                logger
                    .log(Level.SEVERE,
                        "An error occured while deregistering a MicroSensorDataType.");

                logger.log(Level.SEVERE, e.getMessage());

            }
        }

        logger.exiting(this.getClass().getName(), "removeProvidingModule");
    }

}
