/*
 * Class: MsdtRegistry
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.msdt.registry;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.msdt.MicroSensorDataTypeException;
import org.electrocodeogram.msdt.MsdtProvider;
import org.xml.sax.SAXException;

/**
 * The database for <em>MicroSensorDataTypes</em>. Every ECG module is able to bring
 * in its own <em>MicroSensorDataTypes</em>, which are the types of
 * events that the module is creating. During the module
 * registration process the module's <em>MicroSensorDataType</em>-definitions
 * are registered with this <code>MsdtRegistry</code>.<br>
 * When a module is removed from ECG Lab and deregistering takes
 * place, the module's <em>MicroSensorDataTypes</em> are also
 * deregistered. A core set of <em>MicroSensorDataTypes</em> is
 * provided by the ECG and is automatically registered when the first <em>SourceModule</em> is created.
 */
public class MsdtRegistry extends Observable implements IMsdtRegistry {

    /**
     * This is a map of all currently registered
     * {@link MicroSensorDataType}.
     */
    private HashMap<String, MicroSensorDataType> registeredMsdt;

    /**
     * This is a map of all predefined {@link MicroSensorDataType}.
     */
    private HashMap<String, MicroSensorDataType> predefinedMsdt;

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper.createLogger(MsdtRegistry.class
        .getName());

    /**
     * This creates the <code>MsdtRegistry<code>.
     */
    public MsdtRegistry() {

        logger.entering(this.getClass().getName(), "MsdtRegistry");

        this.registeredMsdt = new HashMap<String, MicroSensorDataType>();

        this.predefinedMsdt = new HashMap<String, MicroSensorDataType>();

        try {
            loadPredefinedSourceMsdt();
        } catch (FileNotFoundException e) {

            logger
                .log(Level.SEVERE, "The predifined MSDTs could not be found.");

            logger.log(Level.SEVERE, e.getMessage());

        }
        logger.exiting(this.getClass().getName(), "MsdtRegistry");

    }

    /**
     * This method parses the XML schema files in the <em>"msdt"</em>
     * subdirectory and stores each XML schema as a
     * {@link MicroSensorDataType} in the <code>MsdtRegitry</code>.
     * @throws FileNotFoundException
     *             If the "msdt" subdirectory could not be found
     */
    private void loadPredefinedSourceMsdt() throws FileNotFoundException {

        logger.entering(this.getClass().getName(), "loadPredefinedSourceMsdt");

        String msdtSubDirString = "msdt";

        File msdtDir = new File(msdtSubDirString);

        if (!msdtDir.exists() || !msdtDir.isDirectory()) {
            logger
                .log(Level.SEVERE,
                    "The MicroSensorDataType \"msdt\" subdirectory could not be found.");

            logger.exiting(this.getClass().getName(),
                "loadPredefinedSourceMsdt");

            throw new FileNotFoundException(
                "The MicroSensorDataType \"msdt\" subdirectory could not be found.");
        }

        String[] defs = msdtDir.list();

        if (defs != null) {
            for (String def : defs) {
                File defFile = new File(msdtDir.getAbsolutePath()
                                        + File.separator + def);

                if (defFile.getName().equals("msdt.common.xsd")) {
                    continue;
                }

                SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

                Schema schema = null;

                try {

                    schema = schemaFactory.newSchema(defFile);

                    MicroSensorDataType microSensorDataType = new MicroSensorDataType(
                        defFile.getName(), schema, defFile);

                    logger.log(Level.INFO,
                        "Loaded additional MicroSensorDatyType "
                                        + defFile.getName());

                    this.predefinedMsdt.put(microSensorDataType.getName(),
                        microSensorDataType);

                } catch (SAXException e) {

                    logger.log(Level.WARNING,
                        "Error while reading the XML schema file "
                                        + defFile.getName());

                    logger.log(Level.WARNING, e.getMessage());

                } catch (MicroSensorDataTypeException e) {
                    logger.log(Level.WARNING,
                        "Error while crating a MicroSensorDataType from the XML schema file "
                                        + defFile.getName());
                }

            }
        } else {
            logger.log(Level.SEVERE, "No MSDTs are found!");
        }

        logger.exiting(this.getClass().getName(), "loadPredefinedSourceMsdt");

    }

    /**
     * This returns an array of all currently
     * registered {@link MicroSensorDataType}.
     * @return An array of all currently registered
     *         {@link MicroSensorDataType}
     */
    public final MicroSensorDataType[] getMicroSensorDataTypes() {
        logger.entering(this.getClass().getName(), "getMicroSensorDataTypes");

        logger.exiting(this.getClass().getName(), "getMicroSensorDataTypes",
            this.registeredMsdt.values().toArray(
                new MicroSensorDataType[this.registeredMsdt.size()]));

        return this.registeredMsdt.values().toArray(
            new MicroSensorDataType[this.registeredMsdt.size()]);

    }

    /**
     * This is called by modules that are providing their own
     * {@link MicroSensorDataType}. The module's MSDT is then
     * registered with the <code>MsdtRegistry</code> and available in
     * the ECG Lab.
     * @param msdt
     *            Is the <em>MicroSensorDataType</em> provided by a
     *            module
     * @param provider
     *            Is the module that provides the
     *            <em>MicroSensorDataType</em>
     * @return The <code>MicroSensorDataType</code>-object
     * @throws MicroSensorDataTypeRegistrationException
     *             If the parameters are null
     */
    public final MicroSensorDataType requestMsdtRegistration(
        final MicroSensorDataType msdt, final MsdtProvider provider)
        throws MicroSensorDataTypeRegistrationException {
        logger.entering(this.getClass().getName(), "requestMsdtRegistration",
            new Object[] {msdt, provider});

        if (msdt == null) {

            logger
                .exiting(this.getClass().getName(), "requestMsdtRegistration");

            throw new MicroSensorDataTypeRegistrationException(
                "The MicroSensorDataType is \"null\".");
        }

        if (provider == null) {

            logger
                .exiting(this.getClass().getName(), "requestMsdtRegistration");

            throw new MicroSensorDataTypeRegistrationException(
                "The Module is \"null\"");
        }

        if (this.registeredMsdt.containsKey(msdt.getName())) {
            MicroSensorDataType knownMsdt = this.registeredMsdt.get(msdt
                .getName());

            knownMsdt.addProvidingModule(provider);

            logger.log(Level.INFO,
                "Registered additonal Module with a known MicroSensorDatyType "
                                + knownMsdt.getName());

            fireStatechangeNotification();

            return knownMsdt;

        }

        msdt.addProvidingModule(provider);

        this.registeredMsdt.put(msdt.getName(), msdt);

        logger.log(Level.INFO, "Registered a new MicroSensorDatyType "
                               + msdt.getName());

        fireStatechangeNotification();

        logger.exiting(this.getClass().getName(), "requestMsdtRegistration",
            msdt);

        return msdt;

    }

    /**
     * @see org.electrocodeogram.msdt.registry.IMsdtRegistry#deregisterMsdt(org.electrocodeogram.msdt.MicroSensorDataType)
     */
    public final void deregisterMsdt(final MicroSensorDataType msdt)
        throws MicroSensorDataTypeRegistrationException {
        logger.entering(this.getClass().getName(), "deregisterMsdt",
            new Object[] {msdt});

        if (msdt == null) {

            logger.exiting(this.getClass().getName(), "deregisterMsdt");

            throw new MicroSensorDataTypeRegistrationException(
                "The given MicroSensorDataType is \"null\".");
        }

        if (!this.registeredMsdt.containsKey(msdt.getName())) {

            logger.exiting(this.getClass().getName(), "deregisterMsdt");

            throw new MicroSensorDataTypeRegistrationException(
                "A MicroSensorDataType with the name " + msdt.getName()
                                + " is not registered.");
        }

        this.registeredMsdt.remove(msdt.getName());

        logger.log(Level.INFO, "Deregistered MicroSensorDatyType "
                               + msdt.getName());

        fireStatechangeNotification();

        logger.exiting(this.getClass().getName(), "deregisterMsdt");
    }

    /**
     * Returns an array of {@link MicroSensorDataType} entries, that
     * are predefined and loacted in the <em>"msdt"</em> folder.
     * @return The predefined <em>MicroSensorDatatTypes</em>
     */
    public final MicroSensorDataType[] getPredefinedMicroSensorDataTypes() {
        logger.entering(this.getClass().getName(),
            "getPredefinedMicroSensorDataTypes");

        logger.exiting(this.getClass().getName(),
            "getPredefinedMicroSensorDataTypes", this.predefinedMsdt.values()
                .toArray(new MicroSensorDataType[this.predefinedMsdt.size()]));

        return this.predefinedMsdt.values().toArray(
            new MicroSensorDataType[this.predefinedMsdt.size()]);
    }


    /**
     * The method is called to notify the ECG system of state
     * changes in this registry.
     */
    private void fireStatechangeNotification() {
        logger.entering(this.getClass().getName(),
            "fireStatechangeNotification");

        setChanged();

        notifyObservers(this);

        clearChanged();

        logger.exiting(this.getClass().getName(),
            "fireStatechangeNotification");
    }
}
