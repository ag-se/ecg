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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.msdt.MicroSensorDataTypeException;
import org.xml.sax.SAXException;

/**
 * The <em>MicroSensorDataType</em> registry is a database for
 * <em>MicroSensorDataTypes</em>. Every ECG module is able to bring
 * in its own <em>MicroSensorDataTypes</em>, which are the types of
 * events that the actual module is sending. During the module
 * registration process the module's <em>MicroSensorDataType</em>
 * definitions are registered with this <em>MsdtRegistry</em>.<br>
 * When a module is removed from ECG Lab and deregistering takes
 * place, the module's <em>MicroSensorDataTypes</em> are also
 * deregistered. A core set of <em>MicroSensorDataTypes</em> is
 * provided by the ECG.
 */
public class MsdtRegistry implements IMsdtRegistry {

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
     * This creates the MsdtRegistry.
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
     * This method parses the XML schema files in the "msdt"
     * subdirectory and stores each XML schema as a
     * {@link MicroSensorDataType} in the <em>MsdtRegitry</em>.
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

                    logger.log(Level.FINEST, e.getMessage());

                } catch (MicroSensorDataTypeException e) {
                    logger.log(Level.WARNING,
                        "Error while reading the XML schema file "
                                        + defFile.getName());
                }

            }
        } else {
            logger.log(Level.SEVERE, "No MSDTs are found!");
        }

        logger.exiting(this.getClass().getName(), "loadPredefinedSourceMsdt");

    }

    /**
     * This returns an <code>Array</code> of all currently
     * registered {@link MicroSensorDataType}.
     * @return An <code>Array</code> of all currently registered
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
     * registered with the <em>MsdtRegistry</em> and available in
     * the ECG Lab.
     * @param msdt
     *            Is the <em>MicroSensorDataType</em> provided by a
     *            module
     * @param module
     *            Is the module that provides the
     *            <em>MicroSensorDataType</em>
     * @return The <em>MicroSensorDataType</em> object
     * @throws MicroSensorDataTypeRegistrationException
     *             If the parameters are null
     */
    public final MicroSensorDataType requestMsdtRegistration(
        final MicroSensorDataType msdt, final Module module)
        throws MicroSensorDataTypeRegistrationException {
        logger.entering(this.getClass().getName(), "requestMsdtRegistration",
            new Object[] {msdt, module});

        if (msdt == null) {

            logger
                .exiting(this.getClass().getName(), "requestMsdtRegistration");

            throw new MicroSensorDataTypeRegistrationException(
                "The MicroSensorDataType is \"null\".");
        }

        if (module == null) {

            logger
                .exiting(this.getClass().getName(), "requestMsdtRegistration");

            throw new MicroSensorDataTypeRegistrationException(
                "The Module is \"null\"");
        }

        if (this.registeredMsdt.containsKey(msdt.getName())) {
            MicroSensorDataType knownMsdt = this.registeredMsdt.get(msdt
                .getName());

            knownMsdt.addProvidingModule(module);

            logger.log(Level.INFO,
                "Registered additonal Module with a known MicroSensorDatyType "
                                + knownMsdt.getName());

            org.electrocodeogram.system.System.getInstance().fireStateChange();

            return knownMsdt;

        }

        msdt.addProvidingModule(module);

        this.registeredMsdt.put(msdt.getName(), msdt);

        logger.log(Level.INFO, "Registered a new MicroSensorDatyType "
                               + msdt.getName());

        org.electrocodeogram.system.System.getInstance().fireStateChange();

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

        org.electrocodeogram.system.System.getInstance().fireStateChange();

        logger.exiting(this.getClass().getName(), "deregisterMsdt");
    }

    /**
     * Returns an <code>Array</code> of {@link MicroSensorDataType} entries, that
     * are predefined in by the ECG.
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
     * @see org.electrocodeogram.msdt.registry.IMsdtRegistry#parseMicroSensorDataType(java.io.File)
     */
    public final MicroSensorDataType parseMicroSensorDataType(final File defFile)
        throws MicroSensorDataTypeException {
        logger.entering(this.getClass().getName(), "parseMicroSensorDataType",
            new Object[] {defFile});

        if (!defFile.exists()) {

            logger.exiting(this.getClass().getName(),
                "parseMicroSensorDataType");

            throw new MicroSensorDataTypeException(
                "Error while loading MSDT:\nThe schema file "
                                + defFile.getAbsolutePath()
                                + " does not exist.");
        }

        if (!defFile.isFile()) {

            logger.exiting(this.getClass().getName(),
                "parseMicroSensorDataType");

            throw new MicroSensorDataTypeException(
                "Error while loading MSDT:\nThe schema file "
                                + defFile.getAbsolutePath()
                                + " is not a plain file.");
        }

        SchemaFactory schemaFactory = SchemaFactory
            .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        Schema schema = null;

        try {

            schema = schemaFactory.newSchema(defFile);

            MicroSensorDataType microSensorDataType = new MicroSensorDataType(
                defFile.getName(), schema, defFile);

            logger.log(Level.INFO, "Loaded additional MicroSensorDatyType "
                                   + defFile.getName());

            logger.exiting(this.getClass().getName(),
                "parseMicroSensorDataType");

            return microSensorDataType;

        } catch (SAXException e) {

            logger.exiting(this.getClass().getName(),
                "parseMicroSensorDataType");

            throw new MicroSensorDataTypeException(
                "Error while reading the XML schema file " + defFile.getName()
                                + "\n" + e.getMessage());
        }

    }
}
