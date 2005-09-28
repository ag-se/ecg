package org.electrocodeogram.test.msdt;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.msdt.MicroSensorDataTypeException;
import org.electrocodeogram.msdt.registry.MicroSensorDataTypeRegistrationException;
import org.electrocodeogram.msdt.registry.MsdtRegistry;
import org.electrocodeogram.test.module.TestModule;
import org.xml.sax.SAXException;

/**
 * This Mock is used for testing the ECG's event validating mechanism.
 * Instead of loading MicroSensorDataTypes (MSDTs) with a module, like the
 * original MsdtRegistry does, this Mock loads the predefined MSDTs at startup.
 */
public class MockMsdtRegistry extends MsdtRegistry
{

    private TestModule testModule = null;
    
    private Logger logger = null;
    
    /**
     * This creates the Mock and loads the predefines MSDTs.
     * @throws FileNotFoundException If no predefined MSDTs where founds
     */
    public MockMsdtRegistry() throws FileNotFoundException
    {
        super();
        
        this.logger = Logger.getLogger("MockMsdtRegistry"); 
        
        this.testModule = new TestModule();
        
        this.loadPredefinedSourceMsdt();
    }
    private MicroSensorDataType requestMsdtRegistration(MicroSensorDataType msdt) throws MicroSensorDataTypeRegistrationException
    {
        return super.requestMsdtRegistration(msdt,this.testModule);
    }
    
    /**
     * This method parses the XML schema files and strores each XML schema in the
     * MockMsdtRegitry's HashMap.
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
                    
                    MicroSensorDataType microSensorDataType = new MicroSensorDataType(defFile.getName(),schema); 
                    
                    this.logger.log(Level.INFO,"Loaded additional MicroSensorDatyType " + defFile.getName());
                    
                    
                    try {
                        this.requestMsdtRegistration(microSensorDataType);
                    }
                    catch (MicroSensorDataTypeRegistrationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                }
                catch (SAXException e) {

                    this.logger.log(Level.WARNING, "Error while reading the XML schema file " + defFile.getName());
                    
                    this.logger.log(Level.WARNING, e.getMessage());

                }
				catch (MicroSensorDataTypeException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
               
               
            }
        }
        else {
            this.logger.log(Level.INFO, "No msdt data is found.");
        }

    }
}
