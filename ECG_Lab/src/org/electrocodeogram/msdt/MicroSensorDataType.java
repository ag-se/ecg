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
import org.electrocodeogram.module.Module;
import org.electrocodeogram.msdt.registry.MicroSensorDataTypeRegistrationException;

/**
 * A <em>MicroSensorDataType</em> is a type of an actual
 * <em>MicroActivityEvent</em>. Each <em>MicroActivityEvent</em>
 * is an instance of one <em>MicroSensorDataType</em>. A
 * <em>MicroSensorDataType</em> contains a XML schema object and
 * each <em>MicroActivityEvent</em> accroding to this type is
 * containing an XML document that is an instance of this type's XML
 * schema. In addition a <em>MicroSensorDataType</em> has a uniique
 * name in the ECG, which is the name of its XML schema file.
 */
public class MicroSensorDataType {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(MicroSensorDataType.class.getName());

    /**
     * This is the XML schema that is contained in this
     * <em>MicroSensorDataType</em>.
     */
    private Schema mySchema;

    /**
     * The name of the <em>MicroSensorDataType</em>. In the ECg it
     * is always the name of the XML schema file.
     */
    private String myName;

    /**
     * A static counter.
     */
    private static int count;

    /**
     * The unique int id of this <em>MicroSensorDataType</em>.
     */
    private int id;

    /**
     * This is a reference to the file containing the XML schema.
     */
    private File myDefFile;

    /**
     * A <em>MicroSensorDataType</em> can be provided by multiple
     * modules. Each module is kept as a reference here.
     */
    private ArrayList<Module> providingModules;

    /**
     * This creates a <em>MicroSensorDataType</em> and assigns a
     * unique int id to it.
     * @param name
     *            Is the name for the new <em>MicroSensorDataType</em>.
     *            It is always the name of the XML schema file
     * @param schema
     *            Is the XML schema that actually defines this type
     * @param defFile
     *            Is the filecontaining the XML schema
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

        this.providingModules = new ArrayList<Module>();

        logger.exiting(this.getClass().getName(), "MicroSensorDataType");

    }

    /**
     * This returns the name of the <em>MicroSensorDataType</em>.
     * @return The name of the <em>MicroSensorDataType</em>
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
     * This method returns the XML Schema of this
     * <em>MicroSensorDataType</em>.
     * @return The XML Schema of the <em>MicroSensorDataType</em>
     */
    public final Schema getSchema() {

        logger.entering(this.getClass().getName(), "getSchema");

        logger.exiting(this.getClass().getName(), "getSchema", this.mySchema);

        return this.mySchema;
    }

    /**
     * This method returns the unique integer id of the
     * <em>MicroSensorDataType</em>.
     * @return The unique integer id of the
     *         <em>MicroSensorDataType</em>
     */
    public final int getId() {

        logger.entering(this.getClass().getName(), "getId");

        logger
            .exiting(this.getClass().getName(), "getId", new Integer(this.id));

        return this.id;
    }

    /**
     * Every module that provides this <em>MicroSensorDataType</em>
     * calls this method during its creation and the module is then added to the
     * {@link #providingModules} list. A
     * <em>MicroSensorDataType</em> is unloaded from the {@link org.electrocodeogram.msdt.registry.MsdtRegistry}
     * when there are no more module instances in the ECG Lab that are providing it.
     * @param module
     *            Is the module that provides this <em>MicroSensordataTyp</em>
     * @throws MicroSensorDataTypeRegistrationException
     *             If the module parameter is "null"
     */
    public final void addProvidingModule(final Module module)
        throws MicroSensorDataTypeRegistrationException {

        logger.entering(this.getClass().getName(), "addProvidingModule",
            new Object[] {module});

        if (module == null) {

            logger.exiting(this.getClass().getName(), "addProvidingModule");

            throw new MicroSensorDataTypeRegistrationException(
                "The given module is of value \"null\"");
        }

        if (this.providingModules.contains(module)) {

            logger.exiting(this.getClass().getName(), "addProvidingModule");

            return;
        }

        this.providingModules.add(module);

        logger.log(Level.FINE, "Registered the module " + module.getName()
                               + " with the MSDT " + this.getName() + ".");

        logger.exiting(this.getClass().getName(), "addProvidingModule");
    }

    /**
     * Every module that provides this <em>MicroSensorDataType</em>
     * calls this method during its removal and the module is then removed from the
     * {@link #providingModules} list. A
     * <em>MicroSensorDataType</em> is unloaded from the {@link org.electrocodeogram.msdt.registry.MsdtRegistry}
     * when there are no more module instances in the ECG Lab that are providing it.
     * @param module
     *            Is the module to remove
     * @throws MicroSensorDataTypeRegistrationException
     *             If the given module parameter is "null"
     */
    public final void removeProvidingModule(final Module module)
        throws MicroSensorDataTypeRegistrationException {

        logger.entering(this.getClass().getName(), "removeProvidingModule",
            new Object[] {module});

        if (module == null) {

            logger.exiting(this.getClass().getName(), "removeProvidingModule");

            throw new MicroSensorDataTypeRegistrationException(
                "The given module is of value \"null\"");
        }

        if (!this.providingModules.contains(module)) {

            logger.exiting(this.getClass().getName(), "removeProvidingModule");

            return;
        }

        this.providingModules.remove(module);

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
