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

public class MsdtManager
{

    private HashMap<String,Schema> msdtSchemas = null;

    private Logger logger = null;

    private SchemaFactory schemaFactory = null;

    public MsdtManager() throws FileNotFoundException
    {

        this.logger = Logger.getLogger("MstdManager");

        this.msdtSchemas = new HashMap<String,Schema>();

        this.schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        loadMDTDSchemas();

    }

    /**
     * 
     */
    private void loadMDTDSchemas() throws FileNotFoundException
    {

        String stdDefsDirPath = "msdt";

        File stdDefsDir = new File(stdDefsDirPath);

        if (!stdDefsDir.exists() || !stdDefsDir.isDirectory()) {
            throw new FileNotFoundException(
                    "Das Verzeichnis \"msdt\" exisitiert nicht oder ist kein Verzeichnis");
        }

        String[] defs = stdDefsDir.list();

        if (defs != null) {
            for (int i = 0; i < defs.length; i++) {
                File defFile = new File(
                        stdDefsDirPath + File.separator + defs[i]);

                Schema schema = null;

                try {

                    schema = this.schemaFactory.newSchema(defFile);

                    this.msdtSchemas.put(defFile.getName(),schema);
                    
                    this.logger.log(Level.INFO, "Registered new MicroSensorDatyType " + defFile.getName());
                }
                catch (SAXException e) {

                    this.logger.log(Level.WARNING, "Error while reading the XML schema file " + defFile.getName());
                    
                    this.logger.log(Level.WARNING, e.getMessage());

                }

            }
        }
        else {
            this.logger.log(Level.INFO, "Es wurden keine mSDT Definitionen gefunden.");
        }

    }
    
    /**
     * This method returns an Array of all currently registered MicroSensorDataType XMLSchema filenames. 
     * @return An Array of all currently registered MicroSensorDataType XMLSchema filenames
     */
    public String[] getMstdSchemaNames()
    {
       return this.msdtSchemas.keySet().toArray(new String[0]);
    }

    /**
     * @param schemaName
     * @return
     */
    public Schema getSchemaForName(String schemaName)
    {
        if (schemaName == null) return null;
        
        if (!this.msdtSchemas.containsKey(schemaName)) return null;
        
        return this.msdtSchemas.get(schemaName);
    }
}
