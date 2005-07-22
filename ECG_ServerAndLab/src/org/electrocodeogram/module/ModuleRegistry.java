package org.electrocodeogram.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.core.ICore;
import org.electrocodeogram.ui.Configurator;

/**
 * This is the central ModuleRegistry which maintains information about all
 * currently instanciated modules and all module class files in the module
 * directory from which module instances could be created at runtime.
 * 
 * During its creation
 * every module object registers with the ModuleRegistry with its unique id. On
 * deletion of a module the module is deregistered from the ModuleRegistry.
 * 
 */
public class ModuleRegistry extends Observable
{

    private Logger logger = null;

    private RunningModules runningModules = null;

    private InstalledModules installedModules = null;

    private File moduleDirectory = null;

    /**
     * The constructor creates the ModuleRegistry instance.
     * @param corePar A reference to the Core object
     */
    public ModuleRegistry(ICore corePar)
    {
        this.logger = Logger.getLogger("ModuleRegistry");

        addObserver(corePar.getConfigurator());

        this.runningModules = new RunningModules();

    }

    /**
     * The constructor creates the ModuleRegistry instance.
     * @param corePar A reference to the Core object
     * @param filePar This should be the module directory
     */
    public ModuleRegistry(ICore corePar, File filePar)
    {
        this(corePar);

        this.installedModules = new InstalledModules(filePar);
        
        setChanged();

        notifyObservers();

        clearChanged();
    }

    /**
     * If the module directory is not knoen during ModuleRegistry creation
     * this method can be used to set the mofule directory later.
     * @param filePar This should be the module directory
     */
    public void setFile(File filePar) {
    	
    	this.installedModules = new InstalledModules(filePar);
    	
    	setChanged();

        notifyObservers();

        clearChanged();
		
	}
    

    private Logger getLogger()
    {
        return this.logger;
    }

    /**
     * This method returns the IDs of all currently known module class files
     * that are ready to be instanciated.
     * 
     * @return The IDs of all currently known module class files
     */
    public Integer[] getAvailableModuleClassIds()
    {
        if (this.installedModules.availableModuleClassesMap.size() > 0) {
            return this.installedModules.availableModuleClassesMap.keySet().toArray(new Integer[0]);
        }

        return null;
    }

    /**
     * This method returns the module class object with the given moduleClassId.
     * 
     * @param moduleClassId
     * @return The class object
     * @throws IllegalModuleIDException
     *             If the given moduleClassId has a value of < 1
     * @throws UnknownModuleIDException
     *             If a module class with the given id could not be found
     */
    public Class getModuleClassForId(int moduleClassId) throws IllegalModuleIDException, UnknownModuleIDException
    {
        if (!(moduleClassId > 1)) {
            throw new IllegalModuleIDException();
        }

        if (!this.installedModules.availableModuleClassesMap.containsKey(new Integer(
                moduleClassId))) {
            throw new UnknownModuleIDException();
        }

        ModuleDescriptor moduleDescriptor = this.installedModules.availableModuleClassesMap.get(new Integer(
                moduleClassId));

        return moduleDescriptor.getClazz();
    }

    /**
     * This method returns the module properties of the module class with the
     * given moduleClassId.
     * 
     * @param moduleClassId
     * @return The module properties as declared in the "module.properties" file
     *         of the module
     * @throws IllegalModuleIDException
     *             If the given moduleClassId has a value of < 1
     * @throws UnknownModuleIDException
     *             If a module class with the given id could not be found
     */
    public Properties getModulePropertiesForId(int moduleClassId) throws IllegalModuleIDException, UnknownModuleIDException
    {
        if (!(moduleClassId > 0)) {
            throw new IllegalModuleIDException();
        }
        
        if(this.installedModules == null)
        {
        	return null;
        }

        if (!this.installedModules.availableModuleClassesMap.containsKey(new Integer(
                moduleClassId))) {
            throw new UnknownModuleIDException();
        }

       
            ModuleDescriptor moduleDescriptor = this.installedModules.availableModuleClassesMap.get(new Integer(
                    moduleClassId));

            return moduleDescriptor.getProperties();
        

    }

    private class RunningModules
    {
        HashMap<Integer, Module> runningModuleMap = new HashMap<Integer, Module>();
    }

    
    private class InstalledModules
    {

        private final String MODULE_PROPERTY_FILE = "module.properties";

        HashMap<Integer, ModuleDescriptor> availableModuleClassesMap = null;

        private int id = 0;

        private ModuleClassLoader moduleClassLoader = null;

        private InstalledModules(File moduleDirectoryPar)
        {
            this.availableModuleClassesMap = new HashMap<Integer, ModuleDescriptor>();

            this.moduleClassLoader = getModuleClassLoader();

            initialize(moduleDirectoryPar);
        }

        private ModuleClassLoader getModuleClassLoader()
        {

            Class clazz = this.getClass();

            ClassLoader currentClassLoader = clazz.getClassLoader();

            return new ModuleClassLoader(currentClassLoader);
        }

        private void initialize(File moduleDirectoryPar)
        {
            // is the parameter not null?
            if (moduleDirectoryPar == null) {
                return;
            }

            assert (moduleDirectoryPar != null);

            // does the file exist and is it a directory?
            if (!moduleDirectoryPar.exists() || !moduleDirectoryPar.isDirectory()) {
                return;
            }

            assert (moduleDirectoryPar.exists() && moduleDirectoryPar.isDirectory());

            // get all filenames in it
            String[] moduleDirectories = moduleDirectoryPar.list();

            // assert no IO-Error has occured
            if (moduleDirectories == null) {
                return;
            }

            assert (moduleDirectories != null);

            int length = moduleDirectories.length;

            // are there any files in it?
            if (!(length > 0)) {
                return;
            }

            assert (length > 0);

            for (int i = 0; i < length; i++) {

                String actModuleDirectoryPath = moduleDirectoryPar + File.separator + moduleDirectories[i];

                File actModuleDirectory = new File(actModuleDirectoryPath);

                // skip all simple files
                if (!actModuleDirectory.isDirectory()) {
                    continue;
                }

                String modulePropertyFileString = actModuleDirectoryPath + File.separator + this.MODULE_PROPERTY_FILE;

                File modulePropertyFile = new File(modulePropertyFileString);

                // inspect module.property file and skip if neccessary
                if (!modulePropertyFile.exists() || !modulePropertyFile.isFile()) {

                    getLogger().log(Level.WARNING, "Error inspecting module property file " + modulePropertyFileString);

                    continue;

                }

                Properties properties = new Properties();

                try {

                    properties.load(new FileInputStream(modulePropertyFile));
                }
                catch (FileNotFoundException e1) {

                    getLogger().log(Level.SEVERE, "File not found: " + modulePropertyFile.getName());
                }
                catch (IOException e1) {

                    getLogger().log(Level.SEVERE, "Error while reading: " + modulePropertyFile.getName());
                }

                if (properties.size() > 0) {

                    String moduleName = properties.getProperty("MODULE_NAME");

                    // skip this module if its name is not given
                    if (moduleName == null || moduleName.length() == 0) {
                        continue;
                    }

                    assert (moduleName != null && moduleName.length() > 0);

                    String moduleClassString = properties.getProperty("MODULE_CLASS");

                    // skip this module if its class name is not given
                    if (moduleClassString == null || moduleClassString.length() == 0) {
                        continue;
                    }

                    assert (moduleClassString != null && moduleClassString.length() > 0);

                    String fQmoduleClassString = moduleDirectoryPar + File.separator + moduleDirectories[i] + File.separator + moduleClassString + ".class";

                    Class moduleClass = null;

                    try {

                        moduleClass = this.moduleClassLoader.loadClass(fQmoduleClassString);

                        int moduleClassId = this.id++;

                        ModuleDescriptor moduleDescriptor = new ModuleDescriptor(
                                moduleClassId, moduleName, moduleClass,
                                properties);

                        this.availableModuleClassesMap.put(new Integer(
                                moduleClassId), moduleDescriptor);
                       
                    }
                    catch (ClassNotFoundException e) {

                        getLogger().log(Level.SEVERE, "Unable to load the module " + moduleName + " because the class " + fQmoduleClassString + " is not found.\nProceeding with next module if any.");
                    }
                }

            }
        }
    }

    /**
     * This method registers a module instance with the ModuleRegistry. If the module
     * instance is allready registered with the ModuleRegistry nothing happens.
     * This method is automatically called whenever a new object of class Module
     * is created.
     * 
     * @param module
     *            Is the module instance to register
     */
    protected void registerModuleInstance(Module module)
    {
        // check parameter
        if (module == null) {
            return;
        }

        assert (module != null);

        if (this.runningModules.runningModuleMap.containsKey(new Integer(
                module.getId()))) {
            return;
        }

        this.runningModules.runningModuleMap.put(new Integer(module.getId()), module);

        setChanged();

        notifyObservers(module);

        clearChanged();

    }

    /**
     * This method returns the module instance with the given moduleId.
     * 
     * @param moduleId
     *            Is the id of the module instance to return
     * @return The desired module instance
     * @throws IllegalModuleIDException
     *             If the given moduleClassId has a value of < 1
     * @throws UnknownModuleIDException
     *             If a module class with the given id could not be found
     */
    public Module getModuleInstance(int moduleId) throws IllegalModuleIDException, UnknownModuleIDException
    {
        if (!(moduleId > 0)) {
            throw new IllegalModuleIDException();
        }

        assert (moduleId > 0);

        if (!(this.runningModules.runningModuleMap.containsKey(new Integer(
                moduleId)))) {
            throw new UnknownModuleIDException();
        }

        return this.runningModules.runningModuleMap.get(new Integer(moduleId));
    }

    /**
     * This method takes the id of a module class and returns the module
     * instance of it. It also gives the module instance the given name.
     * 
     * @param moduleClassId
     *            Is the id of the module class to be instanciated
     * @param moduleName
     *            Is the name that should be given to the new module object
     * @throws ModuleInstantiationException
     *             If an exception occurs during the instanciation of the module
     * @throws IllegalModuleIDException
     *             If the given moduleClassId has a value of < 1
     * @throws UnknownModuleIDException
     *             If a module class with the given id could not be found
     */
    public void createModuleInstanceFromModuleClassId(int moduleClassId, String moduleName) throws ModuleInstantiationException, IllegalModuleIDException, UnknownModuleIDException
    {
        if (!(moduleClassId > 0)) {
            throw new IllegalModuleIDException();
        }

        assert (moduleClassId > 0);

        Class moduleClass = getModuleClassForId(moduleClassId);

        Module module = null;
        try {
            module = (Module) moduleClass.newInstance();
        }
        catch (InstantiationException e) {

            throw new ModuleInstantiationException(e.getMessage());

        }
        catch (IllegalAccessException e) {

            throw new ModuleInstantiationException(e.getMessage());
        }

        module.setName(moduleName);
        
        String description = getModulePropertiesForId(moduleClassId).getProperty("MODULE_DESCRIPTION");
        
        module.setDescription(description);
    }
  
    /**
     * This method removes a module instance from the ModuleRegistry.
     * It is called from the Module.remove() method.
     * @param moduleId Is the id of the module to deregister.
     * @throws IllegalModuleIDException
     *             If the given moduleClassId has a value of < 1
     * @throws UnknownModuleIDException
     *             If a module class with the given id could not be found
  
     */
    protected void deregisterModuleInstance(int moduleId) throws UnknownModuleIDException, IllegalModuleIDException
    {
        if (!(moduleId > 0)) {
            throw new IllegalModuleIDException();
        }

        assert (moduleId > 0);
        
        Module module = getModuleInstance(moduleId);

        this.runningModules.runningModuleMap.remove(new Integer(moduleId));
        
        setChanged();

        notifyObservers(module);

        clearChanged();

    }

    /**
     * This method returns the description of the module class with the given id.
     * The description is given by the module developer in the module's "module.property" file.
     * @param moduleClassId Is the id of the module class to get the description of
     * @return The description about the module class
    * @throws IllegalModuleIDException
     *             If the given moduleClassId has a value of < 1
     * @throws UnknownModuleIDException
     *             If a module class with the given id could not be found
     */
    public String getModuleDescription(int moduleClassId) throws IllegalModuleIDException, UnknownModuleIDException
    {

        Properties properties = getModulePropertiesForId(moduleClassId);
       
        String moduleDescription = properties.getProperty("MODULE_DESCRIPTION");

        return moduleDescription;
    }

	

}
