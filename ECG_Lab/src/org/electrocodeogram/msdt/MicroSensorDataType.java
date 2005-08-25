package org.electrocodeogram.msdt;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.xml.validation.Schema;

import org.electrocodeogram.module.Module;
import org.electrocodeogram.system.SystemRoot;

/**
 * A MicroSensorDataType is a type for an actual MicroActivity event.
 * Each MicroActivity is belonging to exactly one MicroSensorDataType.
 * Each MicroSensorDataType contains a XML schema object and each
 * MicroActivity accroding to this type is written in an XML document
 * string that is according to the type's XML schema.
 * In addition a MicroSensorDataType has a name which is set to be the name
 * of the XML schema file during MicroSensorDataType creation.
 * Additionally a unique integer id is given to each MicroSensorDataType during creation. 
 */
public class MicroSensorDataType
{
    private Logger logger = null;
    
    private Schema $schema = null;
    
    private String $name = null;
    
    private static int count = 0;
    
    private int id = 0;
    
    private ArrayList<Module> providingModules = null;
   
    /**
     * This creates a MicroSensorDataType and assigns a unique integer id ti it.
     * @param name Is the name for the new type. It is provided by the module object and is the filename of the XMl schema file.
     * @param schema Is the XML schema that actually defines the type
     * @throws IllegalMicroSensorDataTypeNameException If the name value is "null"
     * @throws IllegalMicroSensorDataTypeSchemaException If the schema value is "null"
     */
    public MicroSensorDataType(String name, Schema schema) throws IllegalMicroSensorDataTypeNameException, IllegalMicroSensorDataTypeSchemaException
    {
        this.logger = Logger.getLogger("MicroSensorDataType");
        
        this.id = ++count;
        
        if(name == null || name.equals(""))
        {
            throw new IllegalMicroSensorDataTypeNameException("The given name is \"null\" or empty.");
        }
        
        this.$name = name;
        
        if(schema == null)
        {
            throw new IllegalMicroSensorDataTypeSchemaException("The given XML schema is \"null\".");
        }
        
        this.$schema = schema;
        
        this.providingModules = new ArrayList<Module>();

    }

    /**
     * This method returns the name of the MicroSensorDataType.
     * @return The name of the MicroSensorDataType
     */
    public String getName()
    {
        return this.$name;
    }

    /**
     * This method returns the XML Schema of the MicroSensorDataType.
     * @return The XML Schema of the MicroSensorDataType
     */
    public Schema getSchema()
    {
        return this.$schema;
    }

    /**
     * This method returns the unique integer id of the MicroSensorDataType.
     * @return The unique integer id of the MicroSensorDataType
     */
    public int getId()
    {
        return this.id;
    }
    
    /**
     * This method is used during the load of MicroSensorDataTypes (MSTDs). Every module
     * that defines this MSDT registers with the MSDT using this method.
     * An MSDT is unloaded when there are no more modules registered with it.
     * If the module has allready registered with the MSDT, this method simply returns.
     * @param module Is the module that defines this MSDT
     * @throws ModuleIsNullException If the module parameter is "null"
     */
    public void addProvidingModule(Module module) throws ModuleIsNullException
    {
        if(module == null)
        {
            throw new ModuleIsNullException("The given module is of value \"null\"");
        }
        
        if(this.providingModules.contains(module))
        {
            return;
        }
        
        this.providingModules.add(module);
        
        this.logger.log(Level.INFO,"Registered module " + module.getName() + " for the MSDT " + this.getName() + ".");
    }
    
    /**
     * This method is used during the deregistration of modules. If a module is deregistered (unloaded) that
     * has defined this MicroSensorDataType (MSDT), then this method is called by the module to tell
     * this MSDT that it no longer needs it. If no module is registered with the MSDT the MSDT can be unloaded too.
     * If the given module was never registered with the MSDt, this method simply returns.
     * @param module Is the module to deregister with the MSDT
     * @throws ModuleIsNullException If the given module parameter is "null"
     */
    public void removeProvidingModule(Module module) throws ModuleIsNullException
    {
        if(module == null)
        {
            throw new ModuleIsNullException("The given module is of value \"null\"");
        }
        
        if(!this.providingModules.contains(module))
        {
            return;
        }
        
        this.providingModules.remove(module);
        
        if(this.providingModules.size() == 0)
        {
            try {
                SystemRoot.getSystemInstance().getMsdtRegistry().deregisterMsdt(this);
            }
            catch (MSDTIsNullException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
}
