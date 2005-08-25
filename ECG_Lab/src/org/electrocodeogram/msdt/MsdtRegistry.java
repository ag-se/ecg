package org.electrocodeogram.msdt;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.electrocodeogram.module.Module;
import org.electrocodeogram.system.SystemRoot;
import org.xml.sax.SAXException;

/**
 * The MicroSensorDataType registry is a database for MicroSensorDataTypes.
 * Every ECG module is able to bring in its own MicroSensorDataTypes which are
 * the types of events that the actual module is sending. During hte module
 * registration process the module's MicroSensorDataType definitions are
 * registered with this MsdtRegistry. When a module is removed from workspace
 * and dregestering takes place, the module's MicroSensorDataTypes are also
 * deregestered. A core set of MicroSensorDataTypes are provided by the core
 * modules which are built into the ECG.
 */
public class MsdtRegistry
{

    private HashMap<String, MicroSensorDataType> registeredMsdt = null;

    private HashMap<String, MicroSensorDataType> predefinedMsdt = null;

    private Logger logger = null;

    /**
     * This creates the MsdtRegistry object.
     * @throws FileNotFoundException 
     */
    public MsdtRegistry()
    {

        this.logger = Logger.getLogger("MstdManager");

        this.registeredMsdt = new HashMap<String, MicroSensorDataType>();

        this.predefinedMsdt = new HashMap<String, MicroSensorDataType>();

        try {
            loadPredefinedSourceMsdt();
        }
        catch (FileNotFoundException e) {

            this.logger.log(Level.WARNING, e.getMessage());

        }

    }

    /**
     * This method parses the XML schema files and strores each XML schema in the
     * MsdtRegitry's HashMap.
     */
    private void loadPredefinedSourceMsdt() throws FileNotFoundException
    {

        String msdtSubDirString = "msdt";

        File msdtDir = new File(msdtSubDirString);

        if (!msdtDir.exists() || !msdtDir.isDirectory()) {
            throw new FileNotFoundException(
                    "The MicroSensorDataType \"msdt\" subdirectory can not be found.");
        }

        String[] defs = msdtDir.list();

        if (defs != null) {
            for (int i = 0; i < defs.length; i++) {
                File defFile = new File(
                        msdtDir.getAbsolutePath() + File.separator + defs[i]);

                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

                Schema schema = null;

                try {

                    schema = schemaFactory.newSchema(defFile);

                    MicroSensorDataType microSensorDataType = new MicroSensorDataType(
                            defFile.getName(), schema);

                    this.logger.log(Level.INFO, "Loaded additional MicroSensorDatyType " + defFile.getName());

                    this.predefinedMsdt.put(microSensorDataType.getName(), microSensorDataType);

                }
                catch (SAXException e) {

                    this.logger.log(Level.WARNING, "Error while reading the XML schema file " + defFile.getName());

                    this.logger.log(Level.WARNING, e.getMessage());

                }
                catch (IllegalMicroSensorDataTypeNameException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (IllegalMicroSensorDataTypeSchemaException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        else {
            this.logger.log(Level.INFO, "No msdt data is found.");
        }

    }

    /**
     * This method returns an Array of all currently registered
     * MicroSensorDataType XMLSchemas.
     * 
     * @return An Array of all currently registered MicroSensorDataType
     *         XMLSchemas
     */
    public MicroSensorDataType[] getMicroSensorDataTypes()
    {
        return this.registeredMsdt.values().toArray(new MicroSensorDataType[0]);
    }

    /**
     * This method is used by modules to register their MicroSensorDataTypes (MSDTs)
     * with this MsdtRegistry. If the MSDT has allready been registered it is not
     * registered again but the providingModule parameter is passed to the MSDT's list
     * of modules that are providing this MSDT.
     * The actual MSDT is returned so that the providingModule can add it to its
     * list of provided MSDT.
     * 
     * @param msdt
     *            Is the MSDT the providingModule wants to register
     * @param providingModule Is the Module object that provides the MSDT
     * @return A reference to the registered MSDT
     * @throws MSDTIsNullException
     *             Is thrown by this method, if the provided MSDT
     *             is "null". 
     * @throws ModuleIsNullException If the module parameter is "null"
     */
    public MicroSensorDataType requestMsdtRegistration(MicroSensorDataType msdt, Module providingModule) throws MSDTIsNullException, ModuleIsNullException
    {
        if (msdt == null) {
            throw new MSDTIsNullException(
                    "The given MicroSensorDataType is \"null\".");
        }

        if (providingModule == null) {
            throw new ModuleIsNullException(
                    "the given Module is of value \"null\"");
        }

        if (this.registeredMsdt.containsKey(msdt.getName())) {
            MicroSensorDataType knownMsdt = this.registeredMsdt.get(msdt.getName());

            knownMsdt.addProvidingModule(providingModule);

            this.logger.log(Level.INFO, "Registered additonal Module with a known MicroSensorDatyType " + knownMsdt.getName());

            SystemRoot.getSystemInstance().fireStateChange();

            return knownMsdt;

        }

        msdt.addProvidingModule(providingModule);

        this.registeredMsdt.put(msdt.getName(), msdt);

        this.logger.log(Level.INFO, "Registered a new MicroSensorDatyType " + msdt.getName());

        SystemRoot.getSystemInstance().fireStateChange();

        return msdt;

    }

    /**
     * This method is used by modules to deregister their MicroSensorDataTypes.
     * 
     * @param msdt
     *            Is the actual MicroSensorDataType to deregister
     * @throws MSDTIsNullException
     *             Is thrown by this method, if the provided MicroSensorDataType
     *             is "null" or if the given MicroSensorDataType is not
     *             registered with the registry.
     */
    public void deregisterMsdt(MicroSensorDataType msdt) throws MSDTIsNullException
    {
        if (msdt == null) {
            throw new MSDTIsNullException(
                    "The given MicroSensorDataType is \"null\".");
        }

        if (!this.registeredMsdt.containsKey(msdt.getName())) {
            throw new MSDTIsNullException(
                    "A MicroSensorDataType with the name " + msdt.getName() + " is not registered.");
        }

        this.registeredMsdt.remove(msdt.getName());

        this.logger.log(Level.INFO, "Deregistered MicroSensorDatyType " + msdt.getName());

        SystemRoot.getSystemInstance().fireStateChange();
    }

    /**
     * @return
     */
    public MicroSensorDataType[] getPredefinedMicroSensorDataTypes()
    {
        return this.predefinedMsdt.values().toArray(new MicroSensorDataType[0]);
    }

    /**
     * @param file
     * @return
     * @throws MicroSensorDataTypeException 
     */
    public MicroSensorDataType parseMicroSensorDataType(File defFile) throws MicroSensorDataTypeException
    {
        if(!defFile.exists())
        {
            throw new MicroSensorDataTypeException("Error while loading MSDT:\nThe schema file " + defFile.getAbsolutePath() + " does not exist.");
        }
        
        if(!defFile.isFile())
        {
            throw new MicroSensorDataTypeException("Error while loading MSDT:\nThe schema file " + defFile.getAbsolutePath() + " is not a plain file.");
        }
        
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        Schema schema = null;

        try {

            schema = schemaFactory.newSchema(defFile);

            MicroSensorDataType microSensorDataType = new MicroSensorDataType(
                    defFile.getName(), schema);

            this.logger.log(Level.INFO, "Loaded additional MicroSensorDatyType " + defFile.getName());

            return microSensorDataType;

        }
        catch (SAXException e) {

            throw new MicroSensorDataTypeException("Error while reading the XML schema file " + defFile.getName() +"\n" + e.getMessage());
        }
        catch (IllegalMicroSensorDataTypeNameException e) {
            
            throw new MicroSensorDataTypeException("Error while reading the XML schema file " + defFile.getName() +"\n" + e.getMessage());
            
        }
        catch (IllegalMicroSensorDataTypeSchemaException e) {
            
            throw new MicroSensorDataTypeException("Error while reading the XML schema file " + defFile.getName() +"\n" + e.getMessage());
            
        }
    }
}
