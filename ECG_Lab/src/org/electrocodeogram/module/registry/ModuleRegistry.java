package org.electrocodeogram.module.registry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.ModuleActivationException;
import org.electrocodeogram.module.ModuleConnectionException;
import org.electrocodeogram.module.ModuleDescriptor;
import org.electrocodeogram.module.ModuleProperty;
import org.electrocodeogram.module.ModulePropertyException;
import org.electrocodeogram.module.Module.ModuleType;
import org.electrocodeogram.module.classloader.ModuleClassLoaderInitializationException;
import org.electrocodeogram.module.setup.ModuleConfiguration;
import org.electrocodeogram.module.setup.ModuleSetup;
import org.electrocodeogram.msdt.MicroSensorDataTypeException;
import org.electrocodeogram.system.Core;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.xml.ClassLoadingException;
import org.electrocodeogram.xml.ECGParser;
import org.electrocodeogram.xml.PropertyException;
import org.xml.sax.SAXException;

/**
 * This is the ModuleRegistry which maintains information about all
 * currently available in the module directory and all running
 * modules. An available module is a set of a module class that
 * extends the class Module or one of its subclasses, additonal
 * classes and a "module.properties.xml" file that is an instance of
 * the "module.properties.xsd" XML schema and provides the neccessary
 * information about he module. At runtime multiple running modules
 * can be created from each available module. The ModuleRegistry is
 * accessible through two interfaces. ISystemModuleRegistry provides
 * methods for other ECG system components and IModuleModuleRegistry
 * provides methods for ECG modules.
 */
public class ModuleRegistry extends Observable implements IModuleRegistry {

    private static Logger _logger = LogHelper.createLogger(ModuleRegistry.class
        .getName());

    private RunningModules _runningModules;

    private InstalledModules _installedModules;

    /**
     * The constructor creates the ModuleRegistry instance.
     */
    public ModuleRegistry() {

        _logger.entering(this.getClass().getName(), "ModuleRegistry");

        this._runningModules = new RunningModules();

        _logger.exiting(this.getClass().getName(), "ModuleRegistry");

    }

    /**
     * The constructor creates the ModuleRegistry instance. the given
     * path is searched for available modules.
     * @param moduleDirectory
     *            This path is searched for available modules
     * @throws ModuleClassLoaderInitializationException
     */
    public ModuleRegistry(File moduleDirectory)
        throws ModuleClassLoaderInitializationException {
        this();

        _logger.entering(this.getClass().getName(), "ModuleRegistry");

        if (moduleDirectory == null) {
            _logger.log(Level.WARNING, "moduleDirectory is null");

            return;
        }

        this._installedModules = new InstalledModules(this, moduleDirectory);

        this._installedModules.initialize();

        setChanged();

        notifyObservers();

        clearChanged();

        _logger.exiting(this.getClass().getName(), "ModuleRegistry");

    }

    /**
     * If the module directory is not known at ModuleRegistry creation
     * this method is used by the SystemRoot to set the module
     * directory when known. This method is not for use by the user.
     * @param moduleDirectory
     *            This path is searched for available modules
     * @throws ModuleClassLoaderInitializationException
     */
    public void setModuleDirectory(File moduleDirectory)
        throws ModuleClassLoaderInitializationException {
        _logger.entering(this.getClass().getName(), "setModuleDirectory");

        if (moduleDirectory == null) {
            _logger.log(Level.WARNING, "moduleDirectory is null");

            return;
        }

        this._installedModules = new InstalledModules(this, moduleDirectory);

        this._installedModules.initialize();

        setChanged();

        notifyObservers();

        clearChanged();

        _logger.exiting(this.getClass().getName(), "setModuleDirectory");

    }

    /**
     * @see org.electrocodeogram.module.registry.IModuleRegistry#getAvailableModuleIds()
     */
    public String[] getAvailableModuleIds() {
        _logger.entering(this.getClass().getName(), "setModuleDirectory");

        if (this._installedModules.availableModuleClassesMap.size() > 0) {
            return this._installedModules.availableModuleClassesMap.keySet()
                .toArray(
                    new String[this._installedModules.availableModuleClassesMap
                        .size()]);
        }

        _logger.exiting(this.getClass().getName(), "setModuleDirectory");

        return null;
    }

    private Class getModuleClassForId(String moduleClassId)
        throws ModuleClassException {
        _logger.entering(this.getClass().getName(), "getModuleClassForId");

        if (moduleClassId == null || moduleClassId.equals("")) {
            _logger.log(Level.WARNING, "The module id is null or empty.");

            throw new ModuleClassException("The module id is null orempty.");
        }

        if (!this._installedModules.availableModuleClassesMap
            .containsKey(moduleClassId)) {
            throw new ModuleClassException("The module id " + moduleClassId
                                           + " is unknown.");
        }

        ModuleDescriptor moduleDescriptor = this._installedModules.availableModuleClassesMap
            .get(moduleClassId);

        _logger.exiting(this.getClass().getName(), "getModuleClassForId");

        return moduleDescriptor.getClazz();
    }

    void notifyOfNewModuleDecriptor(ModuleDescriptor moduleDescriptor) {
        _logger.entering(this.getClass().getName(),
            "notifyOfNewModuleDecriptor");

        setChanged();

        notifyObservers(moduleDescriptor);

        clearChanged();

        _logger
            .exiting(this.getClass().getName(), "notifyOfNewModuleDecriptor");
    }

    private static class RunningModules {

        HashMap<Integer, Module> runningModuleMap = new HashMap<Integer, Module>();
    }

    private static class InstalledModules {

        private static Logger _installedModulesLogger = LogHelper
            .createLogger(InstalledModules.class.getName());

        private String _currentModuleDirectoryString;

        private static final String MODULE_PROPERTY_FILE = "module.properties.xml";

        HashMap<String, ModuleDescriptor> availableModuleClassesMap = null;

        private File _moduleDirectory;

        private ModuleRegistry _moduleRegistry;

        private InstalledModules(ModuleRegistry moduleRegistry,
            File moduleDirectory) {
            _installedModulesLogger.entering(this.getClass().getName(),
                "InstalledModules");

            this.availableModuleClassesMap = new HashMap<String, ModuleDescriptor>();

            this._moduleRegistry = moduleRegistry;

            this._moduleDirectory = moduleDirectory;

            _installedModulesLogger.exiting(this.getClass().getName(),
                "InstalledModules");

        }

        void initialize() throws ModuleClassLoaderInitializationException {
            _installedModulesLogger.entering(this.getClass().getName(),
                "initialize");

            // is the parameter not null?
            if (this._moduleDirectory == null) {
                throw new ModuleClassLoaderInitializationException(
                    "The provided module directory path is \"null\".");
            }

            // does the file exist and is it a directory?
            if (!this._moduleDirectory.exists()
                || !this._moduleDirectory.isDirectory()) {
                throw new ModuleClassLoaderInitializationException(
                    "The module directory does not exist or is not a directory.");
            }

            // get all filenames in it
            String[] moduleDirectories = this._moduleDirectory.list();

            // assert no IO-Error has occurred
            if (moduleDirectories == null) {
                throw new ModuleClassLoaderInitializationException(
                    "The module directory does not contain any subdirectories.");
            }

            int length = moduleDirectories.length;

            // are there any files in it?
            if (!(length > 0)) {
                throw new ModuleClassLoaderInitializationException(
                    "The module directory does not contain any subdirectories.");
            }

            for (int i = 0; i < length; i++) {

                this._currentModuleDirectoryString = this._moduleDirectory
                                                     + File.separator
                                                     + moduleDirectories[i];

                File currentModuleDirectory = new File(
                    this._currentModuleDirectoryString);

                // skip all simple files
                if (!currentModuleDirectory.isDirectory()) {
                    _installedModulesLogger.log(Level.WARNING,
                        "Skipping simple file in module directory: "
                                        + currentModuleDirectory
                                            .getAbsolutePath());

                    continue;
                }

                String modulePropertyFileString = this._currentModuleDirectoryString
                                                  + File.separator
                                                  + MODULE_PROPERTY_FILE;

                File modulePropertyFile = new File(modulePropertyFileString);

                // inspect module.property file and skip if neccessary
                if (!modulePropertyFile.exists()
                    || !modulePropertyFile.isFile()) {

                    _installedModulesLogger.log(Level.WARNING,
                        "The module property file does not exist or is not a file: "
                                        + modulePropertyFileString);

                    continue;

                }

                ModuleDescriptor moduleDescriptor = null;
                try {
                    moduleDescriptor = ECGParser
                        .parseAsModuleDescriptor(modulePropertyFile);
                } catch (ClassLoadingException e) {
                    _installedModulesLogger.log(Level.WARNING,
                        "Error while loading the module: "
                                        + modulePropertyFileString);

                    continue;
                } catch (MicroSensorDataTypeException e) {
                    _installedModulesLogger.log(Level.WARNING,
                        "Error while loading the module: "
                                        + modulePropertyFileString);

                    continue;
                } catch (PropertyException e) {
                    _installedModulesLogger.log(Level.WARNING,
                        "Error while loading the module: "
                                        + modulePropertyFileString);

                    continue;
                } catch (SAXException e) {
                    _installedModulesLogger.log(Level.WARNING,
                        "Error while loading the module: "
                                        + modulePropertyFileString);

                    _installedModulesLogger.log(Level.FINEST, e.getMessage());

                    continue;
                } catch (IOException e) {
                    _installedModulesLogger.log(Level.WARNING,
                        "Error while loading the module: "
                                        + modulePropertyFileString);

                    _installedModulesLogger.log(Level.FINEST, e.getMessage());

                    continue;
                } catch (ModuleSetupLoadException e) {
                    _installedModulesLogger.log(Level.WARNING,
                        "Error while loading the module: "
                                        + modulePropertyFileString);

                    continue;
                }

                if (moduleDescriptor == null) {
                    _installedModulesLogger.log(Level.WARNING,
                        "Error while loading the module: "
                                        + modulePropertyFileString);

                    continue;
                }

                if (this.availableModuleClassesMap.containsKey(moduleDescriptor
                    .getId())) {
                    _installedModulesLogger.log(Level.SEVERE,
                        "A module with the id " + moduleDescriptor.getId()
                                        + " is allready loaded.");

                    _installedModulesLogger.log(Level.FINEST,
                        "The ModuleDescriptor was null.");

                    continue;
                }

                // put the ModuleDescriptor into the HashMap
                this.availableModuleClassesMap.put(moduleDescriptor.getId(),
                    moduleDescriptor);

                _installedModulesLogger.log(Level.INFO,
                    "A ModulePaket has been registerd with id: "
                                    + moduleDescriptor.getId());

                this._moduleRegistry
                    .notifyOfNewModuleDecriptor(moduleDescriptor);

            }

            _installedModulesLogger.exiting(this.getClass().getName(),
                "initialize");
        }

    }

    /**
     * @see org.electrocodeogram.moduleapi.module.registry.IModuleModuleRegistry#registerModule(org.electrocodeogram.module.Module)
     */
    public void registerRunningModule(Module module) {
        _logger.entering(this.getClass().getName(), "registerRunningModule");

        // check parameter
        if (module == null) {
            _logger.log(Level.WARNING, "module is null");

            return;
        }

        if (this._runningModules.runningModuleMap.containsKey(new Integer(
            module.getId()))) {
            _logger.log(Level.WARNING, "module is allready registered");

            return;
        }

        this._runningModules.runningModuleMap.put(new Integer(module.getId()),
            module);

        _logger.log(Level.INFO, "Registered new module " + module.getName());

        setChanged();

        notifyObservers(module);

        clearChanged();

        _logger.exiting(this.getClass().getName(), "registerRunningModule");

    }

    /**
     * @see org.electrocodeogram.module.registry.IModuleRegistry#getRunningModule(int)
     */
    public Module getRunningModule(int moduleId) throws ModuleInstanceException {
        _logger.entering(this.getClass().getName(), "getRunningModule");

        if (!(moduleId > 0)) {
            throw new ModuleInstanceException("The module id is invalid.");
        }

        assert (moduleId > 0);

        if (!(this._runningModules.runningModuleMap.containsKey(new Integer(
            moduleId)))) {
            throw new ModuleInstanceException("The module id is unknown.");
        }

        _logger.exiting(this.getClass().getName(), "getRunningModule");

        return this._runningModules.runningModuleMap.get(new Integer(moduleId));
    }

    /**
     * @see org.electrocodeogram.module.registry.IModuleRegistry#createRunningModule(java.lang.String,
     *      java.lang.String)
     */
    public int createRunningModule(String id, String name)
        throws ModuleInstantiationException, ModuleClassException {
        _logger.entering(this.getClass().getName(), "createRunningModule");

        if (id == null || id.equals("")) {
            _logger.log(Level.WARNING, "The module id is null or empty.");

            throw new ModuleClassException("The module id is null or empty.");
        }

        if (name == null || name.equals("")) {
            _logger.log(Level.WARNING, "The module name is null or empty.");

            throw new ModuleClassException("The module name is null or empty.");
        }

        Class moduleClass = getModuleClassForId(id);

        try {

            Constructor[] constructors = moduleClass.getConstructors();

            Object[] args = new Object[] {id, name};

            Object o = constructors[0].newInstance(args);

            if (!(o instanceof Module)) {
                throw new ModuleInstantiationException("");
            }

            Module module = (Module) o;

            _logger.exiting(this.getClass().getName(), "createRunningModule");

            return module.getId();

        } catch (InstantiationException e) {
            _logger.log(Level.SEVERE, "Error while creating a new Module.");

            throw new ModuleInstantiationException(e.getMessage());

        } catch (IllegalAccessException e) {

            _logger.log(Level.SEVERE, "Error while creating a new Module.");

            throw new ModuleInstantiationException(e.getMessage());
        } catch (IllegalArgumentException e) {
            _logger.log(Level.SEVERE, "Error while creating a new Module.");

            throw new ModuleInstantiationException(e.getMessage());
        } catch (InvocationTargetException e) {
            _logger.log(Level.SEVERE, "Error while creating a new Module.");

            throw new ModuleInstantiationException(e.getMessage());
        }

    }

    /**
     * @see org.electrocodeogram.moduleapi.module.registry.IModuleModuleRegistry#deregisterModule(Module)
     */
    public void deregisterRunningModule(Module module) {
        _logger.entering(this.getClass().getName(), "deregisterRunningModule");

        this._runningModules.runningModuleMap
            .remove(new Integer(module.getId()));

        _logger.log(Level.INFO, "Deregestered module " + module.getName());

        setChanged();

        notifyObservers(module);

        clearChanged();

        _logger.exiting(this.getClass().getName(), "deregisterRunningModule");

    }

    /**
     * @see org.electrocodeogram.module.registry.IModuleRegistry#getModuleDescriptor(java.lang.String)
     */
    public ModuleDescriptor getModuleDescriptor(String moduleClassId)
        throws ModuleClassException {
        _logger.entering(this.getClass().getName(), "getModuleDescriptor");

        if (moduleClassId == null || moduleClassId.equals("")) {

            throw new ModuleClassException("The module id is null or empty.");
        }

        if (this._installedModules == null) {
            throw new ModuleClassException("The module id is unknown.");
        }

        if (!this._installedModules.availableModuleClassesMap
            .containsKey(moduleClassId)) {
            throw new ModuleClassException("The module id is unknown.");
        }

        _logger.exiting(this.getClass().getName(), "getModuleDescriptor");

        return this._installedModules.availableModuleClassesMap
            .get(moduleClassId);

    }

    /**
     * @see org.electrocodeogram.module.registry.IModuleRegistry#storeModuleSetup(java.io.File)
     */
    public void storeModuleSetup(File file) throws ModuleSetupStoreException {
        _logger.entering(this.getClass().getName(), "storeModuleSetup");

        if (file == null) {
            throw new ModuleSetupStoreException("The given file is null");
        }

        _logger.log(Level.INFO, "Storing module setup in file "
                                + file.getAbsolutePath());

        if (this._runningModules.runningModuleMap == null) {
            throw new ModuleSetupStoreException(
                "No modules are currently running.");

        }

        PrintWriter writer = null;

        try {
            writer = new PrintWriter(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            throw new ModuleSetupStoreException("The given file "
                                                + file.getAbsolutePath()
                                                + "could not be found.");
        }

        writer.flush();

        Module[] modules = this._runningModules.runningModuleMap.values()
            .toArray(new Module[this._runningModules.runningModuleMap.size()]);

        _logger.log(Level.INFO, "Found " + modules.length
                                + " module(s) to store");

        writer.println("<?xml version=\"1.0\"?>");

        writer.println("<modulesetup>");

        for (Module module : modules) {
            _logger.log(Level.INFO, "Storing module " + module.getName());

            writer.println("<module id=\"" + module.getId() + "\" active=\""
                           + (module.getState() == true) + "\">");

            writer.println("<name>");

            writer.println(module.getName());

            writer.println("</name>");

            writer.println("<fromClassId>");

            writer.println(module.getClassId());

            writer.println("</fromClassId>");

            writer.println("<connectedTo>");

            if (module.getReceivingModuleCount() > 0) {

                Module[] receivingModules = module.getReceivingModules();

                _logger.log(Level.FINEST, "Module " + module.getName()
                                          + " is connected to "
                                          + receivingModules.length
                                          + " other modules.");

                for (Module receivingModule : receivingModules) {
                    writer.println("<id>");

                    writer.println(receivingModule.getId());

                    writer.println("</id>");

                    _logger.log(Level.INFO, "Connection to module "
                                            + receivingModule.getId()
                                            + " stored.");
                }

            } else {
                _logger.log(Level.FINEST,
                    "Module " + module.getName()
                                    + " is not connected to other modules.");

            }
            writer.println("</connectedTo>");

            writer.println("<properties>");

            ModuleProperty[] moduleProperties = module.getRuntimeProperties();

            if (moduleProperties != null && moduleProperties.length > 0) {

                _logger.log(Level.FINEST, "Found " + moduleProperties.length
                                          + " properties for module "
                                          + module.getName());

                for (ModuleProperty moduleProperty : moduleProperties) {
                    String propertyName = moduleProperty.getName();

                    _logger.log(Level.FINEST, "Property name "
                                              + moduleProperty.getName());

                    String propertyValue = moduleProperty.getValue();

                    _logger
                        .log(Level.FINEST, "Property value " + propertyValue);

                    String propertyType = moduleProperty.getType().getName();

                    _logger.log(Level.FINEST, "Property type " + propertyType);

                    if (propertyValue != null) {
                        writer.println("<property>");

                        writer.println("<name>");

                        writer.println(propertyName);

                        writer.println("</name>");

                        writer.println("<value>");

                        writer.println(propertyValue);

                        writer.println("</value>");

                        writer.println("<type>");

                        writer.println(propertyType);

                        writer.println("</type>");

                        writer.println("</property>");

                        _logger.log(Level.FINEST, "Property value was stored");
                    } else {
                        _logger.log(Level.FINEST,
                            "Property value is null and not stored");
                    }

                }

            } else {
                _logger.log(Level.FINEST,
                    "Did not find any properties for module "
                                    + module.getName());
            }

            writer.println("</properties>");

            writer.println("</module>");

            writer.flush();

        }

        writer.println("</modulesetup>");

        writer.flush();

        writer.close();

        _logger.exiting(this.getClass().getName(), "storeModuleSetup");

    }

    /**
     * @see org.electrocodeogram.module.registry.IModuleRegistry#loadModuleSetup(java.io.File)
     */
    public void loadModuleSetup(File file) throws ModuleSetupLoadException {
        _logger.entering(this.getClass().getName(), "loadModuleSetup");

        if (file == null) {
            throw new ModuleSetupLoadException("The given File is null");
        }

        HashMap<Integer, Integer> moduleIdTransformationMap = new HashMap<Integer, Integer>();

        HashMap<Integer, Integer[]> moduleConnectionMap = new HashMap<Integer, Integer[]>();

        ArrayList<Integer> moduleActivationList = new ArrayList<Integer>();

        if (!file.exists()) {
            throw new ModuleSetupLoadException("The file "
                                               + file.getAbsolutePath()
                                               + " does not exist.");
        }

        if (file.isDirectory()) {
            throw new ModuleSetupLoadException(file.getAbsolutePath()
                                               + " is a directory, not a file.");
        }

        if (!file.canRead()) {
            throw new ModuleSetupLoadException("The file "
                                               + file.getAbsolutePath()
                                               + " can not be read.");
        }

        clearLab();

        ModuleSetup moduleSetup;

        try {
            moduleSetup = ECGParser.parseAsModuleSetup(file);

            ModuleConfiguration[] moduleConfigurations = moduleSetup
                .getModuleConfigurations();

            for (ModuleConfiguration moduleConfiguration : moduleConfigurations) {

                int assignedModuleId = createRunningModule(moduleConfiguration
                    .getFromClassId(), moduleConfiguration.getModuleName());

                ModuleProperty[] moduleProperties = moduleConfiguration
                    .getModuleProperties();

                if (moduleProperties != null) {
                    for (ModuleProperty moduleProperty : moduleProperties) {
                        
                        if(moduleProperty.getType().equals(Class.forName("java.lang.reflect.Method")) || moduleProperty.getValue() == null)
                        {
                            continue;
                        }
                        
                        getRunningModule(assignedModuleId).getModuleProperty(moduleProperty.getName()).setValue(moduleProperty.getValue());
                    }
                }

                if (moduleConfiguration.isActive()) {
                    moduleActivationList.add(new Integer(assignedModuleId));
                }

                if (moduleIdTransformationMap.containsKey(new Integer(
                    moduleConfiguration.getModuleId()))) {
                    throw new ModuleSetupLoadException(
                        "Duplicate module id found "
                                        + moduleConfiguration.getModuleId());
                }

                moduleIdTransformationMap.put(new Integer(moduleConfiguration
                    .getModuleId()), new Integer(assignedModuleId));

                if (moduleConfiguration.getConnectedTo() != null) {
                    moduleConnectionMap.put(new Integer(moduleConfiguration
                        .getModuleId()), moduleConfiguration.getConnectedTo());
                }
            }

            Integer[] storedModuleIds = moduleConnectionMap.keySet().toArray(
                new Integer[moduleConnectionMap.size()]);

            for (Integer storedModuleId : storedModuleIds) {
                Integer assignedModuleId = moduleIdTransformationMap
                    .get(storedModuleId);

                Integer[] storedReceivingModuleIds = moduleConnectionMap
                    .get(storedModuleId);

                for (Integer storedReceivingModuleId : storedReceivingModuleIds) {
                    if (storedReceivingModuleId == null) {
                        continue;
                    }

                    Integer assignedReceivingModuleId = moduleIdTransformationMap
                        .get(storedReceivingModuleId);

                    if (assignedReceivingModuleId == null) {
                        throw new ModuleSetupLoadException(
                            "An unknown connected to id was found for module id: "
                                            + storedModuleId);
                    }
                    Module module = getRunningModule(assignedModuleId
                        .intValue());

                    Module receivingModule = getRunningModule(assignedReceivingModuleId
                        .intValue());

                    module.connectReceiverModule(receivingModule);

                }
            }

            for (Integer moduleId : moduleActivationList) {
                Module module = this.getRunningModule(moduleId.intValue());

                if (module.isModuleType(ModuleType.TARGET_MODULE)) {
                    try {
                        module.activate();
                    } catch (ModuleActivationException e) {
                        clearLab();

                        throw new ModuleSetupLoadException(e.getMessage());
                    }
                }
            }

            for (Integer moduleId : moduleActivationList) {
                Module module = this.getRunningModule(moduleId.intValue());

                if (module.isModuleType(ModuleType.INTERMEDIATE_MODULE)) {
                    try {
                        module.activate();
                    } catch (ModuleActivationException e) {
                        clearLab();

                        throw new ModuleSetupLoadException(e.getMessage());
                    }
                }
            }

            for (Integer moduleId : moduleActivationList) {
                Module module = this.getRunningModule(moduleId.intValue());

                if (module.isModuleType(ModuleType.SOURCE_MODULE)) {
                    try {
                        module.activate();
                    } catch (ModuleActivationException e) {
                        clearLab();

                        throw new ModuleSetupLoadException(e.getMessage());
                    }
                }
            }

            _logger.exiting(this.getClass().getName(), "loadModuleSetup");
        } catch (SAXException e) {
            throw new ModuleSetupLoadException(e.getMessage());
        } catch (IOException e) {
            throw new ModuleSetupLoadException(e.getMessage());
        } catch (ModuleSetupLoadException e) {
            throw new ModuleSetupLoadException(e.getMessage());
        } catch (ModuleInstanceException e) {
            throw new ModuleSetupLoadException(e.getMessage());
        } catch (ModuleConnectionException e) {
            throw new ModuleSetupLoadException(e.getMessage());
        } catch (ModuleInstantiationException e) {
            throw new ModuleSetupLoadException(e.getMessage());
        } catch (ModuleClassException e) {
            throw new ModuleSetupLoadException(e.getMessage());
       
        } catch (PropertyException e) {
            throw new ModuleSetupLoadException(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new ModuleSetupLoadException(e.getMessage());
        } catch (ClassLoadingException e) {
            throw new ModuleSetupLoadException(e.getMessage());
        }

    }

    /**
     * 
     */
    private void clearLab() {
        _logger.entering(this.getClass().getName(), "clearLab");

        Module[] modules = this._runningModules.runningModuleMap.values()
            .toArray(new Module[this._runningModules.runningModuleMap.size()]);

        for (Module module : modules) {

            deregisterRunningModule(module);

        }

        _logger.log(Level.INFO,
            "All running modules are deregistered. ECG Lab is cleared.");

        _logger.exiting(this.getClass().getName(), "clearLab");
    }
}
