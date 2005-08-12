package org.electrocodeogram.msdt;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

/**
 * The MicroSensorDataType registry is a database for MicroSensorDataTypes.
 * At the first creation of this object the application's "msdt" subdirectory
 * is looked up for XML schema files. Each XML schema file is then parsed
 * and and defines a MicroSensorDataType for the ECG. 
 */
public class MsdtRegistry
{

    private HashMap<String,MicroSensorDataType> registeredMsdt = null;

    private Logger logger = null;

    //private SchemaFactory schemaFactory = null;

    /**
     * This creates the MsdtRegistry object. 
     * @throws FileNotFoundException If the "msdt" subdirectory of the ECG application is not found.
     */
    public MsdtRegistry() throws FileNotFoundException
    {

        this.logger = Logger.getLogger("MstdManager");

        this.registeredMsdt = new HashMap<String,MicroSensorDataType>();

        //this.schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        //loadMDTDSchemas();

    }

//    /**
//     * This method parses the XML schema files and strores each XML schema in the
//     * MsdtRegitry's HashMap.
//     */
//    private void loadMDTDSchemas() throws FileNotFoundException
//    {
//
//        String stdDefsDirPath = "msdt";
//
//        File stdDefsDir = new File(stdDefsDirPath);
//
//        if (!stdDefsDir.exists() || !stdDefsDir.isDirectory()) {
//            throw new FileNotFoundException(
//                    "Das Verzeichnis \"msdt\" exisitiert nicht oder ist kein Verzeichnis");
//        }
//
//        String[] defs = stdDefsDir.list();
//
//        if (defs != null) {
//            for (int i = 0; i < defs.length; i++) {
//                File defFile = new File(
//                        stdDefsDirPath + File.separator + defs[i]);
//
//                Schema schema = null;
//
//                try {
//
//                    schema = this.schemaFactory.newSchema(defFile);
//
//                    this.msdtSchemas.put(defFile.getName(),schema);
//                    
//                    this.logger.log(Level.INFO, "Registered new MicroSensorDatyType " + defFile.getName());
//                }
//                catch (SAXException e) {
//
//                    this.logger.log(Level.WARNING, "Error while reading the XML schema file " + defFile.getName());
//                    
//                    this.logger.log(Level.WARNING, e.getMessage());
//
//                }
//
//            }
//        }
//        else {
//            this.logger.log(Level.INFO, "Es wurden keine mSDT Definitionen gefunden.");
//        }
//
//    }
    
    
    
    /**
     * This method returns an Array of all currently registered MicroSensorDataType XMLSchemas. 
     * @return An Array of all currently registered MicroSensorDataType XMLSchemas
     */
    public MicroSensorDataType[] getMicroSensorDataTypes()
    {
       return this.registeredMsdt.values().toArray(new MicroSensorDataType[0]);
    }
   
    public void registerMsdt(MicroSensorDataType msdt) throws MicroSensorDataTypeRegisterException
    {
        if(msdt == null)
        {
            throw new MicroSensorDataTypeRegisterException("The given MicroSensorDataType is \"null\".");
        }
        
        this.registeredMsdt.put(msdt.getName(),msdt);
        
        this.logger.log(Level.INFO, "Registered new MicroSensorDatyType " + msdt.getName());
    }
    
    
}
