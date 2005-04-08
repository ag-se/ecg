package org.electrocodeogram.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Frank Schlesinger *  * The singleton class ModuleRegistry finds installed ECG modules and makes their module-descriptions * available to the framework. It also keeps track of all currently running modules.
 */

public class ModuleRegistry
{
    // TODO : Register Sensor Source
    
    private Logger logger = Logger.getLogger("ModuleRegistry");

    /**
     * 
     * @uml.property name="theInstance"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private final static ModuleRegistry theInstance = new ModuleRegistry();

    /**
     * 
     * @uml.property name="runningModules"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private RunningModules runningModules = null;

    /**
     * 
     * @uml.property name="installedModules"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private InstalledModules installedModules = null;

    /**
     * 
     * @uml.property name="moduleClassLoader"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private ModuleClassLoader moduleClassLoader = null;

    
    private ModuleRegistry()
    {
        try {
            moduleClassLoader = getModuleClassLoader();
        }
        catch (ClassNotFoundException e2) {
            
            logger.log(Level.SEVERE,"Unable to initialize the module ClassLoader");
            
            e2.printStackTrace();
        }
        
        runningModules = new RunningModules();
        
        installedModules = new InstalledModules();
    }
    
    
    /**
     * This method returns the singleton instance of the ModuleRegistry.
     * @return the singleton instance of the ModuleRegistry
     */
    public static ModuleRegistry getInstance()
    {
        assert (theInstance != null);
        
        return theInstance;
    }

    /**
     * This method instanciates the ModuleClassLoader and passes the curretn ClassLoader
     * as a parameter to be the parent ClassLoader.
     * 
     * @throws ClassNotFoundException This should never occur.
     * 
     * @uml.property name="moduleClassLoader"
     */
    private ModuleClassLoader getModuleClassLoader()
        throws ClassNotFoundException {

        //Class clazz = Class.forName("net.datenfabrik.microstat.core.Module");

        Class clazz = this.getClass();

        ClassLoader currentClassLoader = clazz.getClassLoader();

        return new ModuleClassLoader(currentClassLoader);
    }

    
    /**
     * @author Frank Schlesinger
     *
     * This nested class keeps track of all running modules.
     */
    private class RunningModules extends HashMap
    {

       
        
        /**
         * This method is used to add a module instance to the registry.
         * @param module The reference to the module istance
         */
        public void add(Module module)
        {
            assert (module != null);
            
            put(new Integer(module.getId()),module);
            
        }

        /**
         * This method is used to delete a module instance from the registry.
         * @param module The module instance to delete
         */
        public void delete(Module module)
        {
            assert (module != null);
            
            remove(new Integer(module.getId()));
            
        }

        /**
         * Use this method to get a module instance with the give unique id.
         * @param id The unique id of the module
         */
        public Module getModule(int id)
        {
            assert(id > 0);
            
            assert(containsKey(new Integer(id)));
            
            return (Module) get(new Integer(id));
            
        }

    }
    
    /**
     * @author Frank Schlesinger
     *
     * This class processes the module directory for installed modules.
     */
    private class InstalledModules
    {

        private final String MODULE_PROPERTY_FILE = "module.properties";

        private HashMap availableModules = new HashMap();
        
        
        private InstalledModules()
        {
            this(new File(System.getProperty("user.home") + "/electrocodeogram/modules/"));
        }

        private InstalledModules(File moduleDirectory)
        {
            assert(moduleDirectory != null);
                        
            if (moduleDirectory.exists() && moduleDirectory.isDirectory()) {
                
                String[] moduleDirectories = moduleDirectory.list();

                assert (moduleDirectories != null);
                
                for (int i = 0; i < moduleDirectories.length; i++) {
                    
                    String modulePropertyFileString = moduleDirectory + File.separator + moduleDirectories[i] + File.separator + MODULE_PROPERTY_FILE;
                    
                    File modulePropertyFile = new File(modulePropertyFileString);
                    
                    if (!modulePropertyFile.exists() || !modulePropertyFile.isFile()) {
                    
                        logger.log(Level.WARNING,"Error inspecting module property file " + modulePropertyFileString);
                        
                        continue;
                        
                    }
                    else {
                        
                        Properties properties = new Properties();
                                                
                        try {
                             
                            properties.load(new FileInputStream(modulePropertyFile));
                        }
                        catch (FileNotFoundException e1) {
                            
                            logger.log(Level.SEVERE,"File not found: " + modulePropertyFile.getName());
                        }
                        catch (IOException e1) {
                            
                            logger.log(Level.SEVERE,"Error while reading: " + modulePropertyFile.getName());
                        }
                        
                        if (properties.size() > 0) {
                            
                            String moduleName = properties.getProperty("MODULE_NAME");
                            
                            // TODO : set module names from file

                            assert(moduleName != null);
                            
                            String moduleClassString =  properties.getProperty("MODULE_CLASS");
                            
                            assert(moduleClassString != null);
                            
                            String fQmoduleClassString = moduleDirectory + File.separator + moduleDirectories[i] + File.separator + moduleClassString + ".class";

                            Class moduleClass = null;
                            
                            try {
                                
                                assert(moduleClassLoader != null);
                                
                                moduleClass = moduleClassLoader.loadClass(fQmoduleClassString);
                                
                                assert(availableModules != null);
                                
                                ModuleDescriptor moduleDescriptor = new ModuleDescriptor(moduleName,moduleClass,properties);
                                                                
                                availableModules.put(moduleName, moduleDescriptor);
                            }
                            catch (ClassNotFoundException e) {
                                
                               logger.log(Level.SEVERE,"Unable to load the module " + moduleName);
                            }
                            
                            

                        }
                    }
                }
            }
            else
            {
                logger.log(Level.WARNING,"The Module directory " + moduleDirectory.getAbsolutePath() + " could not be found.");
            }
        }

        

        
        public Object[] getAvailableModulesNames()
        {
            assert(availableModules != null);
            
            if (availableModules.size() > 0) {
                return availableModules.keySet().toArray();
            }
            else {
                return null;
            }
        }

        public Class getModuleClassForName(String name)
        {
            assert(name != null);
            
            assert(availableModules.containsKey(name));
            
            ModuleDescriptor moduleDescriptor = (ModuleDescriptor) availableModules.get(name);
            
            return moduleDescriptor.getClazz();
        }
        
        public Properties getModulePropertiesForName(String name)
        {
            assert(name != null);
            
            assert(availableModules.containsKey(name));
            
            ModuleDescriptor moduleDescriptor = (ModuleDescriptor) availableModules.get(name);
            
            return moduleDescriptor.getProperties();
        }
       

    }

    /**
     * @param module
     */
    public void addModuleInstance(Module module)
    {
        assert(module != null);
        
        assert(runningModules != null);
        
        runningModules.add(module);
        
    }

    /**
     * @return
     */
    public Object[] getInstalledModulesNames()
    {
         return installedModules.getAvailableModulesNames();
    }

    /**
     * @param moduleName
     * @return
     */
    public Class getModuleClassForName(String moduleName)
    {
        assert(moduleName != null);
        
        assert(installedModules != null);
        
        return installedModules.getModuleClassForName(moduleName);
    }

    
    public Properties getModulePropertiesForName(String moduleName)
    {
        assert(moduleName != null);
        
        assert(installedModules != null);
        
        return installedModules.getModulePropertiesForName(moduleName);
    }
    
    public Properties getModulePropertiesForId(int id)
    {
        assert(id != -1);
        
        if(id == 1)
        {
            return null;
        }
        
        Module module = getModuleInstance(id);
        
        assert(module != null);
        
        assert(installedModules != null);
        
        return installedModules.getModulePropertiesForName(module.getName());
    }
    /**
     * @param id
     * @return
     */
    public Module getModuleInstance(int id)
    {
        // TODO : make this private
        
       assert(id > 0);
       
       assert(runningModules != null);
       
       return runningModules.getModule(id);
    }


    /**
     * @param selectedModuleCellId
     * @param moduleClass
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public void connectNewModuleInstance(int selectedModuleCellId, Class moduleClass) throws ModuleConnectionException
    {
        try {
            getModuleInstance(selectedModuleCellId).connectModule((Module) moduleClass.newInstance());
        }
        catch (InstantiationException e) {
            throw new ModuleConnectionException(e.getMessage());
        }
        catch (IllegalAccessException e) {
            throw new ModuleConnectionException(e.getMessage());
        }
        
    }


    /**
     * @param currentModuleId
     * @param currentPropertyName
     * @param propertyValue
     */
    public void setModuleProperty(int currentModuleId, String currentPropertyName, Object propertyValue)
    {
        
        assert(currentModuleId != 1);
        
        Module module = getModuleInstance(currentModuleId);
        
        assert(module != null);
        
        Properties moduleProperties = getModulePropertiesForId(currentModuleId);
        
        assert(moduleProperties != null);
        
        module.setProperty(currentPropertyName,propertyValue);
        
    }


    /**
     * @param modulId
     */
    public void stopModule(int moduleId)
    {
        Module module = getModuleInstance(moduleId);
        
        module.stop();
        
    }


    /**
     * @param moduleId
     */
    public void startModule(int moduleId)
    {
        Module module = getModuleInstance(moduleId);
        
        module.start();
        
        
    }
    
}
