/*
 * Class: ModuleRegistry
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

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

import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.misc.xml.ClassLoadingException;
import org.electrocodeogram.misc.xml.ECGParser;
import org.electrocodeogram.misc.xml.NodeException;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.ModuleActivationException;
import org.electrocodeogram.module.ModuleConnectionException;
import org.electrocodeogram.modulepackage.ModuleType;
import org.electrocodeogram.modulepackage.ModuleDescriptor;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.electrocodeogram.modulepackage.ModulePropertyException;
import org.electrocodeogram.modulepackage.classloader.ModuleClassLoaderInitializationException;
import org.electrocodeogram.modulepackage.modulesetup.ModuleConfiguration;
import org.electrocodeogram.modulepackage.modulesetup.ModuleSetup;
import org.electrocodeogram.msdt.MicroSensorDataTypeException;
import org.xml.sax.SAXException;

/**
 * The <em>ModuleRegistry</em>, maintains information
 * about all currently available <em>ModulePackages</em> in the
 * module directory and all module instances in the ECG Lab. A
 * <em>ModulePackage</em> is a directory containing a module class
 * that extends the class {@link org.electrocodeogram.module.Module}
 * or one of its subclasses. A <em>ModulePackage</em> can also
 * contain additonal classes and must contain a
 * <em>"module.properties.xml"</em> file that is an instance of the
 * <em>"module.properties.xsd"</em> XML schema and provides the
 * neccessary information about the module. At runtime multiple modules
 * instances can be created from each <em>ModulePackage</em>.
 */
public class ModuleRegistry extends Observable implements IModuleRegistry {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper.createLogger(ModuleRegistry.class
        .getName());

    /**
     * A reference to the member class containing the module
     * instances.
     */
    private ModuleInstanceMap moduleInstanceMap;

    /**
     * A reference to the member class containing the
     * <em>ModulePackages</em>.
     */
    private ModulePackagesMap modulePackageMap = new ModulePackagesMap(this);;

    /**
     * Creates the <em>ModuleRegistry</em> without a module
     * directory. The module directory must be provider later by a
     * call to {@link #setModuleDirectory(File)}.
     */
    public ModuleRegistry() {

        logger.entering(this.getClass().getName(), "ModuleRegistry");

        this.moduleInstanceMap = new ModuleInstanceMap();
        this.modulePackageMap = new ModulePackagesMap(this);

        logger.exiting(this.getClass().getName(), "ModuleRegistry");

    }

    /**
     * Creates the <em>ModuleRegistry</em> with a module directory.
     * @param moduleDirectory
     *            This directory is looked for <em>ModulePackages</em>
     * @throws ModuleClassLoaderInitializationException
     *             If an exception occurs while
     *             initialising the
     *             {@link org.electrocodeogram.modulepackage.classloader.ModuleClassLoader}
     */
    public ModuleRegistry(final File modulesDirectory)
        throws ModuleClassLoaderInitializationException {
        this();

        logger.entering(this.getClass().getName(), "ModuleRegistry",
            new Object[] {modulesDirectory});

        setModuleDirectory(modulesDirectory);

        setChanged();
        notifyObservers();
        clearChanged();

        logger.exiting(this.getClass().getName(), "ModuleRegistry");

    }

	/**
     * This method is used to set the directory of modules.
     * @param moduleDirectory
     *            This directory is looked for <em>ModulePackages</em>
     * @throws ModuleClassLoaderInitializationException
     *             If an exception occurs while
     *             inititalising the
     *             {@link org.electrocodeogram.modulepackage.classloader.ModuleClassLoader}
     * TODO rename this to setModulesDirectory()
     */
    public final void setModuleDirectory(final File modulesDirectory)
        throws ModuleClassLoaderInitializationException {
        logger.entering(this.getClass().getName(), "setModuleDirectory",
            new Object[] {modulesDirectory});

        if (modulesDirectory == null) {
            logger.log(Level.WARNING,
                "The parameter \"modulesDirectory\" is null.");

            logger.exiting(this.getClass().getName(), "setModuleDirectory");

            return;
        }

        this.modulePackageMap.initializeModulesDirectory(modulesDirectory);
        
        setChanged();
        notifyObservers();
        clearChanged();

        logger.exiting(this.getClass().getName(), "setModuleDirectory");

    }

    /**
     * @see org.electrocodeogram.module.registry.IModuleRegistry#addModule(java.io.File)
     */
    public void addModule(final File moduleDirectory) 
    	throws ModuleClassLoaderInitializationException {

    	logger.entering(this.getClass().getName(), "addModule",
                new Object[] {moduleDirectory});

        if (moduleDirectory == null) {
            logger.log(Level.WARNING,
                "The parameter \"moduleDirectory\" is null.");

            logger.exiting(this.getClass().getName(), "addModule");

            return;
        }

    	this.modulePackageMap.addModuleDirectory(moduleDirectory);
    	
        setChanged();
        notifyObservers();
        clearChanged();
    	
        logger.entering(this.getClass().getName(), "addModule",
                new Object[] {moduleDirectory});
	}

	/**
     * @see org.electrocodeogram.module.registry.IModuleRegistry#geModulePackageIds()
     * TODO Rename this to getModulePackageIds()
     */
    public final String[] geModulePackageIds() {
        logger.entering(this.getClass().getName(), "getModulePackageIds");

        if (this.modulePackageMap.availableModuleClassesMap.size() > 0) {

            logger
                .exiting(
                    this.getClass().getName(),
                    "setModuleDirectory",
                    this.modulePackageMap.availableModuleClassesMap
                        .keySet()
                        .toArray(
                            new String[this.modulePackageMap.availableModuleClassesMap
                                .size()]));

            return this.modulePackageMap.availableModuleClassesMap.keySet()
                .toArray(
                    new String[this.modulePackageMap.availableModuleClassesMap
                        .size()]);
        }

        logger.exiting(this.getClass().getName(), "getModulePackageIds", new String[0]);

        return new String[0];
    }

    /**
     * Used to get the <em>ModulePackage</em> with the
     * given unique string id.
     * @param id
     *            Is the unique string id
     * @return The <em>ModulePackage</em> having the requested id
     * @throws ModulePackageNotFoundException
     *             If either the id is empty or no
     *             <em>ModulePackage</em> could be found with the
     *             given id
     */
    private Class getModulePackage(final String id)
        throws ModulePackageNotFoundException {
        logger.entering(this.getClass().getName(), "getModulePackage",
            new Object[] {id});

        if (id == null || id.equals("")) {

            logger.log(Level.WARNING, "The module id is null or empty.");

            logger.exiting(this.getClass().getName(), "getModulePackage");

            throw new ModulePackageNotFoundException(
                "The module id is null orempty.", id);
        }

        if (!this.modulePackageMap.availableModuleClassesMap.containsKey(id)) {

            logger.exiting(this.getClass().getName(), "getModulePackage");

            throw new ModulePackageNotFoundException(
                "The module id '" + id + "' is unknown.", id);
        }

        ModuleDescriptor moduleDescriptor = this.modulePackageMap.availableModuleClassesMap
            .get(id);

        logger.exiting(this.getClass().getName(), "getModulePackage",
            moduleDescriptor.getClazz());

        return moduleDescriptor.getClazz();
    }

    /**
     * Fires a notification that a new <em>ModuleDescriptor</em> has been registered,
     * which means that a new <em>ModulePackage</em> has been successfully loaded.
     * The event handling is done in {@link Core#update(Observable, Object)}.
     * @param moduleDescriptor Is the newly registered <em>ModuleDescriptor</em>
     */
    final void fireNewModulePackage(final ModuleDescriptor moduleDescriptor) {
        logger
            .entering(this.getClass().getName(), "fireNewModulePackage");

        setChanged();
        notifyObservers(moduleDescriptor);
        clearChanged();

        logger.exiting(this.getClass().getName(), "fireNewModulePackage");
    }

    /**
     * This member class contains a map with of
     * every created {@link Module} instance as a value and the module's unique
     * int id as the key.
     */
    private static class ModuleInstanceMap {

        HashMap<Integer, Module> runningModuleMap = new HashMap<Integer, Module>();
    }

    /**
     * This member class contains a map with every
     * registered <em>ModuleDescriptor</em> as a value and the
     * <em>ModulePackage's</em> unique string id as
     * the key.
     */
    private static final class ModulePackagesMap {

        /**
         * This is the logger.
         */
        private static Logger modulePackagesMapLogger = LogHelper
            .createLogger(ModulePackagesMap.class.getName());

        /**
         * This is the name of the module property file.
         */
        private static final String MODULE_PROPERTY_FILE = "module.properties.xml";

        /**
         * This is the map itself.
         */
        private HashMap<String, ModuleDescriptor> availableModuleClassesMap = null;

        /**
         * A reference to the surrounding <em>ModuleRegistry</em>.
         */
        private ModuleRegistry moduleRegistry;

        /**
         * Creates the <code>ModulePackagesMap</code> and starts looking
         * up for <em>ModulePackages</em> by calling
         * {@link #initialize()}.
         * @param registry
         *            Is the surrounding <em>ModuleRegistry</em>
         * @param directory
         *            Is the module directory
         */
        private ModulePackagesMap(final ModuleRegistry registry) {
            modulePackagesMapLogger.entering(this.getClass().getName(),
                "InstalledModules", new Object[] {registry});

            this.availableModuleClassesMap = new HashMap<String, ModuleDescriptor>();

            this.moduleRegistry = registry;

            modulePackagesMapLogger.exiting(this.getClass().getName(),
                "InstalledModules");

        }

		/**
         * This method is looking for <em>ModulePackages</em> inside
         * the module directory. For every found
         * <em>ModulePackage</em> the module property file is parsed
         * and a {@link ModuleDescriptor} is build and stored inside
         * the map.
         * @throws ModuleClassLoaderInitializationException
         *             If an exception occurs while
         *             initializing the
         *             {@link org.electrocodeogram.modulepackage.classloader.ModuleClassLoader}
         */
        void initializeModulesDirectory(final File modulesDirectory) 
        	throws ModuleClassLoaderInitializationException {

        	modulePackagesMapLogger.entering(this.getClass().getName(),
                "initializeModulesDirectory");

            String[] moduleDirectories = getModulePackages(modulesDirectory);

            int length = moduleDirectories.length;

            String currentModuleDirectoryString;

            for (int i = 0; i < length; i++) {

                currentModuleDirectoryString = modulesDirectory.getAbsolutePath()
                                               + File.separator
                                               + moduleDirectories[i];
                
                File currentModuleDirectory = new File(currentModuleDirectoryString);

                addModuleDirectory(currentModuleDirectory);

            }
            
            modulePackagesMapLogger.exiting(this.getClass().getName(),
                "initializeModulesDirectory");
        }

        /**
         * Adds a module by its directory
         * 
         * @param moduleDirectory the directory which is assumed to contain the module description
         * @param force if true, this module may overlay an already loaded module with the same module id
         */
        public void addModuleDirectory(final File moduleDirectory) {

        	// skip all simple and hidden files/dirs
            if (!moduleDirectory.isDirectory() || moduleDirectory.isHidden()) {
                modulePackagesMapLogger.log(Level.FINE,
                    "Skipping simple or hidden file in module directory: "
                                    + moduleDirectory.getAbsolutePath());
                return;
            }

            String modulePropertyFileString = moduleDirectory.getAbsolutePath()
                                              + File.separator
                                              + MODULE_PROPERTY_FILE;

            File modulePropertyFile = new File(modulePropertyFileString);

            // inspect module.property file and skip if neccessary
            if (!modulePropertyFile.exists() || !modulePropertyFile.isFile()) {

                modulePackagesMapLogger.log(Level.FINE,
                    "The module property file does not exist or is not a file: "
                                    + modulePropertyFileString);
                return;
            }

            ModuleDescriptor moduleDescriptor = null;

            try {

            	moduleDescriptor = ECGParser
                    .parseAsModuleDescriptor(modulePropertyFile);

            } catch (ClassLoadingException e) {
                modulePackagesMapLogger.log(Level.WARNING,
                    "Error while loading the module: "
                                    + modulePropertyFileString);
                return;
            } catch (MicroSensorDataTypeException e) {
                modulePackagesMapLogger.log(Level.WARNING,
                    "Error while loading the module: "
                                    + modulePropertyFileString);
                return;
            } catch (SAXException e) {
                modulePackagesMapLogger.log(Level.WARNING,
                    "Error while loading the module: "
                                    + modulePropertyFileString);
                modulePackagesMapLogger.log(Level.FINEST, e.getMessage());
                return;
            } catch (IOException e) {
                modulePackagesMapLogger.log(Level.WARNING,
                    "Error while loading the module: "
                                    + modulePropertyFileString);
                modulePackagesMapLogger.log(Level.FINEST, e.getMessage());
                return;
            } catch (NodeException e) {
                modulePackagesMapLogger.log(Level.WARNING,
                    "Error while loading the module: "
                                    + modulePropertyFileString);
                modulePackagesMapLogger.log(Level.FINEST, e.getMessage());
                return;
            }

            if (moduleDescriptor == null) {
                modulePackagesMapLogger.log(Level.WARNING,
                    "Error while loading the module: "
                                    + modulePropertyFileString);
                return;
            }

            if (this.availableModuleClassesMap.containsKey(moduleDescriptor
                .getId())) {
            	
            	ModuleDescriptor origMD = this.availableModuleClassesMap.get(moduleDescriptor.getId());
            	
                modulePackagesMapLogger.log(Level.WARNING,
                    "A module with the id " + moduleDescriptor.getId()
                                    + " is already loaded and will not be replaced.");
                modulePackagesMapLogger.log(Level.WARNING,
                        "New module's directory: " + moduleDescriptor.getDirectory());
                modulePackagesMapLogger.log(Level.WARNING,
                        "Original module's directory: " + origMD.getDirectory());
                
                	return;
            }

            // put the ModuleDescriptor into the HashMap
            this.availableModuleClassesMap.put(moduleDescriptor.getId(),
                moduleDescriptor);

            modulePackagesMapLogger.log(Level.INFO,
                "A ModulePaket has been registerd with id: "
                                + moduleDescriptor.getId());

            this.moduleRegistry
                .fireNewModulePackage(moduleDescriptor);

		}

        /**
         * Returns all <em>ModulePackage</em> folder-names inside the
         * module directory.
         * @return All <em>ModulePackage</em> folder-names as ana array.
         * @throws ModuleClassLoaderInitializationException
         *             If an exception occurs while
         *             initializing the
         *             {@link org.electrocodeogram.modulepackage.classloader.ModuleClassLoader}
         */
        private String[] getModulePackages(final File modulesDirectory)
            throws ModuleClassLoaderInitializationException {

            modulePackagesMapLogger.entering(this.getClass().getName(),
                "getModulePackages");

            // is the parameter not null?
            if (modulesDirectory == null) {

                modulePackagesMapLogger.exiting(this.getClass().getName(),
                    "initialize");

                throw new ModuleClassLoaderInitializationException(
                    "The provided module directory path is \"null\".");
            }

            // does the file exist and is it a directory?
            if (!modulesDirectory.exists()
                || !modulesDirectory.isDirectory()) {

                modulePackagesMapLogger.exiting(this.getClass().getName(),
                    "initialize");

                throw new ModuleClassLoaderInitializationException(
                    "The module directory does not exist or is not a directory.");
            }

            // get all filenames in it
            String[] modulesDirectories = modulesDirectory.list();

            // assert no IO-Error has occurred
            if (modulesDirectories == null) {

                modulePackagesMapLogger.entering(this.getClass().getName(),
                    "getModulePackages");

                throw new ModuleClassLoaderInitializationException(
                    "The module directory does not contain any subdirectories.");
            }

            // are there any files in it?
            if (!(modulesDirectories.length > 0)) {

                modulePackagesMapLogger.entering(this.getClass().getName(),
                    "getModulePackages");

                throw new ModuleClassLoaderInitializationException(
                    "The module directory does not contain any subdirectories.");
            }

            modulePackagesMapLogger.entering(this.getClass().getName(),
                "getModulePackages", modulesDirectories);

            return modulesDirectories;
        }

    }

    /**
     * Used to register a new module instance. The method is
     * called directly from the constructor of every {@link Module}.
     * @param module
     *            Is the module to register
     */
    public final void registerModule(final Module module) {
        logger.entering(this.getClass().getName(), "registerRunningModule",
            new Object[] {module});

        // check parameter
        if (module == null) {

            logger.log(Level.WARNING, "The parameter \"module\" is null.");

            logger.exiting(this.getClass().getName(), "registerRunningModule");

            return;
        }

        if (this.moduleInstanceMap.runningModuleMap.containsKey(Integer.valueOf(
            module.getId()))) {

            logger.log(Level.WARNING, "This module is allready registered.");

            logger.exiting(this.getClass().getName(), "registerRunningModule");

            return;
        }

        this.moduleInstanceMap.runningModuleMap.put(
            Integer.valueOf(module.getId()), module);

        logger.log(Level.INFO, "A new module instance with name "
                               + module.getName() + " has been registered.");

        fireModuleInstance(module);

        logger.exiting(this.getClass().getName(), "registerRunningModule");

    }

    /**
     * Fires a notification about a module instance that has either been registered or deregistered with the <code>ModuleRegistry</code>.
     * @param module Is the module instance
     */
    private void fireModuleInstance(final Module module) {
        setChanged();

        notifyObservers(module);

        clearChanged();
    }

    /**
     * @see org.electrocodeogram.module.registry.IModuleRegistry#getModule(int)
     */
    public final Module getModule(final int id)
        throws ModuleInstanceNotFoundException {
        logger.entering(this.getClass().getName(), "getModule",
            new Object[] {Integer.valueOf(id)});

        if (!(id > 0)) {

            logger.exiting(this.getClass().getName(), "getModule");

            throw new ModuleInstanceNotFoundException(
                "The module id is invalid.", id);
        }

        assert (id > 0);

        if (!(this.moduleInstanceMap.runningModuleMap.containsKey(Integer.valueOf(
            id)))) {

            logger.exiting(this.getClass().getName(), "getModule");

            throw new ModuleInstanceNotFoundException(
                "The module id is unknown.", id);
        }

        logger.exiting(this.getClass().getName(), "getModule",
            this.moduleInstanceMap.runningModuleMap.get(Integer.valueOf(id)));

        return this.moduleInstanceMap.runningModuleMap.get(Integer.valueOf(id));
    }

    /**
     * @see org.electrocodeogram.module.registry.IModuleRegistry#createModule(java.lang.String,
     *      java.lang.String)
     */
    public final int createModule(final String id, final String name)
        throws ModuleInstantiationException, ModulePackageNotFoundException {
        logger.entering(this.getClass().getName(), "createModule",
            new Object[] {id, name});

        if (id == null || id.equals("")) {

            logger.log(Level.WARNING, "The module id is null or empty.");
            logger.exiting(this.getClass().getName(), "createModule");
            throw new ModulePackageNotFoundException(
                "The module id is null or empty.", id);
        }

        if (name == null || name.equals("")) {
            logger.log(Level.WARNING, "The module name is null or empty.");
            logger.exiting(this.getClass().getName(), "createModule");
            throw new ModulePackageNotFoundException(
                "The module name is null or empty.", id);
        }

        Class moduleClass = getModulePackage(id);

        try {

            Constructor[] constructors = moduleClass.getConstructors();

            Object[] args = new Object[] {id, name};

            Object o = constructors[0].newInstance(args);

            Module module = (Module) o;

            logger.exiting(this.getClass().getName(), "createModule");

            return module.getId();

        } catch (InstantiationException e) {
            logger.log(Level.SEVERE, "Error while creating a new Module.");
            logger.exiting(this.getClass().getName(), "createModule");
            throw new ModuleInstantiationException(e.getMessage(), id);

        } catch (IllegalAccessException e) {

            logger.log(Level.SEVERE, "Error while creating a new Module.");
            logger.exiting(this.getClass().getName(), "createModule");
            throw new ModuleInstantiationException(e.getMessage(), id);
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Error while creating a new Module.");
            logger.exiting(this.getClass().getName(), "createModule");
            throw new ModuleInstantiationException(e.getMessage(), id);
        } catch (InvocationTargetException e) {
            logger.log(Level.SEVERE, "Error while creating a new Module.");
            logger.exiting(this.getClass().getName(), "createModule");
            throw new ModuleInstantiationException(e.getMessage(), id);
        }

    }

    /**
     * This is used to deregister a module instance.
     * @param module
     *            Is the module to deregister
     */
    public final void deregisterModule(final Module module) {
        logger.entering(this.getClass().getName(), "deregisterModule",
            new Object[] {module});

        this.moduleInstanceMap.runningModuleMap.remove(Integer.valueOf(module
            .getId()));

        logger.log(Level.INFO, "The module " + module.getName()
                               + " has been deregistered.");

        fireModuleInstance(module);

        logger.exiting(this.getClass().getName(), "deregisterModule");

    }

    /**
     * @see org.electrocodeogram.module.registry.IModuleRegistry#getModuleDescriptor(java.lang.String)
     */
    @SuppressWarnings({"synthetic-access","synthetic-access", "synthetic-access"})
    public final ModuleDescriptor getModuleDescriptor(final String id)
        throws ModulePackageNotFoundException {
        logger.entering(this.getClass().getName(), "getModuleDescriptor",
            new Object[] {id});

        if (id == null || id.equals("")) {

            logger.exiting(this.getClass().getName(), "getModuleDescriptor");

            throw new ModulePackageNotFoundException(
                "The module id is null or empty.", id);
        }

        if (this.modulePackageMap == null) {

            logger.exiting(this.getClass().getName(), "getModuleDescriptor");

            throw new ModulePackageNotFoundException(
                "The module id is unknown.", id);
        }

        if (!this.modulePackageMap.availableModuleClassesMap.containsKey(id)) {

            logger.exiting(this.getClass().getName(), "getModuleDescriptor");

            throw new ModulePackageNotFoundException(
                "The module id is unknown.", id);
        }

        logger.exiting(this.getClass().getName(), "getModuleDescriptor",
            this.modulePackageMap.availableModuleClassesMap.get(id));

        return this.modulePackageMap.availableModuleClassesMap.get(id);

    }

    /**
     * @see org.electrocodeogram.module.registry.IModuleRegistry#storeModuleSetup(java.io.File)
     */
    public final void storeModuleSetup(final File file)
        throws ModuleSetupStoreException {
        logger.entering(this.getClass().getName(), "storeModuleSetup",
            new Object[] {file});

        if (file == null) {

            logger.exiting(this.getClass().getName(), "storeModuleSetup");

            throw new ModuleSetupStoreException("The given file is null.", "");
        }

        logger.log(Level.INFO, "Storing module setup in file "
                               + file.getAbsolutePath());

        if (this.moduleInstanceMap.runningModuleMap == null) {
            logger.exiting(this.getClass().getName(), "storeModuleSetup");
            throw new ModuleSetupStoreException(
                "No modules are currently running.", file.getAbsolutePath());

        }

        PrintWriter writer = null;

        try {
            writer = new PrintWriter(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            logger.exiting(this.getClass().getName(), "storeModuleSetup");
            throw new ModuleSetupStoreException("The given file "
                                                + file.getAbsolutePath()
                                                + "could not be found.", file
                .getAbsolutePath());
        }

        writer.flush();

        Module[] modules = this.moduleInstanceMap.runningModuleMap
            .values()
            .toArray(new Module[this.moduleInstanceMap.runningModuleMap.size()]);

        logger.log(Level.INFO, "Found " + modules.length
                               + " module(s) to store");

        writer.println("<?xml version=\"1.0\"?>");

        writer.println("<modulesetup>");

        for (Module module : modules) {
            logger.log(Level.INFO, "Storing module " + module.getName());

            writer.println("<module id=\"" + module.getId() + "\" active=\""
                           + (module.isActive()) + "\">");

            writer.println("<name>");

            writer.println(module.getName());

            writer.println("</name>");

            writer.println("<fromClassId>");

            writer.println(module.getModulePacketId());

            writer.println("</fromClassId>");

            writer.println("<connectedTo>");

            if (module.getReceivingModuleCount() > 0) {

                Module[] receivingModules = module.getReceivingModules();

                logger.log(Level.FINEST, "Module " + module.getName()
                                         + " is connected to "
                                         + receivingModules.length
                                         + " other modules.");

                for (Module receivingModule : receivingModules) {
                    writer.println("<id>");

                    writer.println(receivingModule.getId());

                    writer.println("</id>");

                    logger.log(Level.INFO, "Connection to module "
                                           + receivingModule.getId()
                                           + " stored.");
                }

            } else {
                logger.log(Level.FINEST,
                    "Module " + module.getName()
                                    + " is not connected to other modules.");

            }
            writer.println("</connectedTo>");

            writer.println("<properties>");

            ModuleProperty[] moduleProperties = module.getRuntimeProperties();

            if (moduleProperties != null && moduleProperties.length > 0) {

                logger.log(Level.FINEST, "Found " + moduleProperties.length
                                         + " properties for module "
                                         + module.getName());

                for (ModuleProperty moduleProperty : moduleProperties) {
                    String propertyName = moduleProperty.getName();

                    logger.log(Level.FINEST, "Property name "
                                             + moduleProperty.getName());

                    String propertyValue = moduleProperty.getValue();

                    logger.log(Level.FINEST, "Property value " + propertyValue);

                    String propertyType = moduleProperty.getType().getName();

                    logger.log(Level.FINEST, "Property type " + propertyType);

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

                        logger.log(Level.FINEST, "Property value was stored");
                    } else {
                        logger.log(Level.FINEST,
                            "Property value is null and not stored");
                    }

                }

            } else {
                logger.log(Level.FINEST,
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

        logger.exiting(this.getClass().getName(), "storeModuleSetup");

    }

    /**
     * @see org.electrocodeogram.module.registry.IModuleRegistry#loadModuleSetup(java.io.File)
     */
    public final void loadModuleSetup(final File file)
        throws ModuleSetupLoadException {
        logger.entering(this.getClass().getName(), "loadModuleSetup",
            new Object[] {file});

        if (file == null) {

            logger.exiting(this.getClass().getName(), "loadModuleSetup");

            throw new ModuleSetupLoadException("The given File is null", "");
        }

        HashMap<Integer, Integer> moduleIdTransformationMap = new HashMap<Integer, Integer>();

        HashMap<Integer, Integer[]> moduleConnectionMap = new HashMap<Integer, Integer[]>();

        ArrayList<Integer> moduleActivationList = new ArrayList<Integer>();

        if (!file.exists()) {
            logger.exiting(this.getClass().getName(), "loadModuleSetup");
            throw new ModuleSetupLoadException("The file "
                                               + file.getAbsolutePath()
                                               + " does not exist.", file
                .getAbsolutePath());
        }

        if (file.isDirectory()) {
            throw new ModuleSetupLoadException(
                file.getAbsolutePath() + " is a directory, not a file.", file
                    .getAbsolutePath());
        }

        if (!file.canRead()) {
            logger.exiting(this.getClass().getName(), "loadModuleSetup");
            throw new ModuleSetupLoadException("The file can not be read.",
                file.getAbsolutePath());
        }

        clearLab();

        ModuleSetup moduleSetup;

        try {
            moduleSetup = ECGParser.parseAsModuleSetup(file);

            ModuleConfiguration[] moduleConfigurations = moduleSetup
                .getModuleConfigurations();

            for (ModuleConfiguration moduleConfiguration : moduleConfigurations) {

                int assignedModuleId = createModule(moduleConfiguration
                    .getModulePackageId(), moduleConfiguration.getModuleName());

                ModuleProperty[] moduleProperties = moduleConfiguration
                    .getModuleProperties();

                if (moduleProperties != null) {
                    for (ModuleProperty moduleProperty : moduleProperties) {

                        if (moduleProperty.getType().equals(
                            Class.forName("java.lang.reflect.Method"))
                            || moduleProperty.getValue() == null) {
                            continue;
                        }

                        getModule(assignedModuleId).getModuleProperty(
                            moduleProperty.getName()).setValue(
                            moduleProperty.getValue());
                    }
                }

                if (moduleConfiguration.isActive()) {
                    moduleActivationList.add(Integer.valueOf(assignedModuleId));
                }

                if (moduleIdTransformationMap.containsKey(Integer.valueOf(
                    moduleConfiguration.getModuleId()))) {
                    throw new ModuleSetupLoadException(
                        "Duplicate module id found "
                                        + moduleConfiguration.getModuleId(),
                        file.getAbsolutePath());
                }

                moduleIdTransformationMap.put(Integer.valueOf(moduleConfiguration
                    .getModuleId()), Integer.valueOf(assignedModuleId));

                if (moduleConfiguration.getConnectedTo() != null) {
                    moduleConnectionMap.put(Integer.valueOf(moduleConfiguration
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
                                            + storedModuleId, file
                                .getAbsolutePath());
                    }
                    Module module = getModule(assignedModuleId.intValue());

                    Module receivingModule = getModule(assignedReceivingModuleId
                        .intValue());

                    module.connectModule(receivingModule);

                }
            }

            for (Integer moduleId : moduleActivationList) {
                Module module = this.getModule(moduleId.intValue());

                if (module.isModuleType(ModuleType.TARGET_MODULE)) {
                    try {
                        module.activate();
                    } catch (ModuleActivationException e) {
                        clearLab();
                        logger.exiting(this.getClass().getName(),
                            "loadModuleSetup");
                        throw new ModuleSetupLoadException(e.getMessage(), file
                            .getAbsolutePath());
                    }
                }
            }

            for (Integer moduleId : moduleActivationList) {
                Module module = this.getModule(moduleId.intValue());

                if (module.isModuleType(ModuleType.INTERMEDIATE_MODULE)) {
                    try {
                        module.activate();
                    } catch (ModuleActivationException e) {
                        clearLab();
                        logger.exiting(this.getClass().getName(),
                            "loadModuleSetup");
                        throw new ModuleSetupLoadException(e.getMessage(), file
                            .getAbsolutePath());
                    }
                }
            }

            for (Integer moduleId : moduleActivationList) {
                Module module = this.getModule(moduleId.intValue());

                if (module.isModuleType(ModuleType.SOURCE_MODULE)) {
                    try {
                        module.activate();
                    } catch (ModuleActivationException e) {
                        clearLab();
                        logger.exiting(this.getClass().getName(),
                            "loadModuleSetup");
                        throw new ModuleSetupLoadException(e.getMessage(), file
                            .getAbsolutePath());
                    }
                }
            }

            logger.exiting(this.getClass().getName(), "loadModuleSetup");
        } catch (SAXException e) {
            logger.exiting(this.getClass().getName(), "loadModuleSetup");
            throw new ModuleSetupLoadException(e.getMessage(), file
                .getAbsolutePath());
        } catch (IOException e) {
            logger.exiting(this.getClass().getName(), "loadModuleSetup");
            throw new ModuleSetupLoadException(e.getMessage(), file
                .getAbsolutePath());
        } catch (ModuleSetupLoadException e) {
            logger.exiting(this.getClass().getName(), "loadModuleSetup");
            throw new ModuleSetupLoadException(e.getMessage(), file
                .getAbsolutePath());
        } catch (ModuleInstanceNotFoundException e) {
            logger.exiting(this.getClass().getName(), "loadModuleSetup");
            throw new ModuleSetupLoadException(e.getMessage(), file
                .getAbsolutePath());
        } catch (ModuleConnectionException e) {
            logger.exiting(this.getClass().getName(), "loadModuleSetup");
            throw new ModuleSetupLoadException(e.getMessage(), file
                .getAbsolutePath());
        } catch (ModuleInstantiationException e) {
            logger.exiting(this.getClass().getName(), "loadModuleSetup");
            throw new ModuleSetupLoadException(e.getMessage(), file
                .getAbsolutePath());
        } catch (ModulePackageNotFoundException e) {
            logger.exiting(this.getClass().getName(), "loadModuleSetup");
            throw new ModuleSetupLoadException(e.getMessage(), file
                .getAbsolutePath());
        } catch (ModulePropertyException e) {
            logger.exiting(this.getClass().getName(), "loadModuleSetup");
            throw new ModuleSetupLoadException(e.getMessage(), file
                .getAbsolutePath());
        } catch (ClassNotFoundException e) {
            logger.exiting(this.getClass().getName(), "loadModuleSetup");
            throw new ModuleSetupLoadException(e.getMessage(), file
                .getAbsolutePath());
        } catch (ClassLoadingException e) {
            logger.exiting(this.getClass().getName(), "loadModuleSetup");
            throw new ModuleSetupLoadException(e.getMessage(), file
                .getAbsolutePath());
        } catch (NodeException e) {
            logger.exiting(this.getClass().getName(), "loadModuleSetup");
            throw new ModuleSetupLoadException(e.getMessage(), file
                .getAbsolutePath());
        }

    }

    /**
     * This method removes all modules from the ECG Lab.
     */
    public final void clearLab() {
        logger.entering(this.getClass().getName(), "clearLab");

        Module[] modules = this.moduleInstanceMap.runningModuleMap
            .values()
            .toArray(new Module[this.moduleInstanceMap.runningModuleMap.size()]);

        for (Module module : modules) {

            deregisterModule(module);

        }

        logger.log(Level.INFO,
            "All running modules are deregistered. ECG Lab is cleared.");

        logger.exiting(this.getClass().getName(), "clearLab");
    }
}
