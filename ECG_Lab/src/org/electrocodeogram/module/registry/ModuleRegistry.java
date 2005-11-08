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
 * This is the <em>ModuleRegistry</em>, which maintains information
 * about all currently available <em>ModulePackages</em> in the
 * module directory and all module instancec in the ECG Lab. A
 * <em>ModulePackages</em> is a directory containing a module class
 * that extends the class {@link org.electrocodeogram.module.Module}
 * or one of its subclasses. A <em>ModulePackage</em> can also
 * contain additonal classes and must contain a
 * <em>"module.properties.xml"</em> file that is an instance of the
 * <em>"module.properties.xsd"</em> XML schema and provides the
 * neccessary information about he module. At runtime multiple modules
 * instances can be created from each <em>ModulePackages</em>.
 */
public class ModuleRegistry extends Observable implements IModuleRegistry {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper.createLogger(ModuleRegistry.class
        .getName());

    /**
     * A reference to the nested class containing the module
     * instances.
     */
    private ModuleInstanceMap moduleInstanceMap;

    /**
     * A reference to the nested class containing the
     * <em>ModulePackages</em>.
     */
    private ModulePackagesMap modulePackageMap;

    /**
     * Creates the <em>ModuleRegistry</em> without a module
     * directory. The module directory must be provider later by a
     * call to {@link #setModuleDirectory(File)}.
     */
    public ModuleRegistry() {

        logger.entering(this.getClass().getName(), "ModuleRegistry");

        this.moduleInstanceMap = new ModuleInstanceMap();

        logger.exiting(this.getClass().getName(), "ModuleRegistry");

    }

    /**
     * Creates the <em>ModuleRegistry</em> with a module directory.
     * @param moduleDirectory
     *            This directory is looked for <em>ModulePackages</em>
     * @throws ModuleClassLoaderInitializationException
     *             If an <code>Exception</code> occurs while
     *             inititalizing the
     *             {@link org.electrocodeogram.modulepackage.classloader.ModuleClassLoader}
     */
    public ModuleRegistry(final File moduleDirectory)
        throws ModuleClassLoaderInitializationException {
        this();

        logger.entering(this.getClass().getName(), "ModuleRegistry",
            new Object[] {moduleDirectory});

        if (moduleDirectory == null) {
            logger.log(Level.WARNING,
                "The parameter \"moduleDirectory\" is null.");

            logger.exiting(this.getClass().getName(), "ModuleRegistry");

            return;
        }

        this.modulePackageMap = new ModulePackagesMap(this, moduleDirectory);

        this.modulePackageMap.initialize();

        setChanged();

        notifyObservers();

        clearChanged();

        logger.exiting(this.getClass().getName(), "ModuleRegistry");

    }

    /**
     * If the module directory is not known at
     * <em>ModuleRegistry's</em> creation this method is used set
     * the module later.
     * @param moduleDirectory
     *            This directory is looked for <em>ModulePackages</em>
     * @throws ModuleClassLoaderInitializationException
     *             If an <code>Exception</code> occurs while
     *             inititalizing the
     *             {@link org.electrocodeogram.modulepackage.classloader.ModuleClassLoader}
     */
    public final void setModuleDirectory(final File moduleDirectory)
        throws ModuleClassLoaderInitializationException {
        logger.entering(this.getClass().getName(), "setModuleDirectory",
            new Object[] {moduleDirectory});

        if (moduleDirectory == null) {
            logger.log(Level.WARNING,
                "The parameter \"moduleDirectory\" is null.");

            logger.exiting(this.getClass().getName(), "setModuleDirectory");

            return;
        }

        this.modulePackageMap = new ModulePackagesMap(this, moduleDirectory);

        this.modulePackageMap.initialize();

        setChanged();

        notifyObservers();

        clearChanged();

        logger.exiting(this.getClass().getName(), "setModuleDirectory");

    }

    /**
     * @see org.electrocodeogram.module.registry.IModuleRegistry#getAvailableModuleIds()
     */
    @SuppressWarnings("synthetic-access")
    public final String[] getAvailableModuleIds() {
        logger.entering(this.getClass().getName(), "setModuleDirectory");

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

        logger.exiting(this.getClass().getName(), "setModuleDirectory", new String[0]);

        return new String[0];
    }

    /**
     * This methos is used to get the <em>ModulePackage</em> for a
     * given unique <code>String</code> id.
     * @param id
     *            Is the unique <code>String</code> id
     * @return The <em>ModulePackage</em> having the requested id
     * @throws ModulePackageNotFoundException
     *             If either the id is illegal (id < 0) or no
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
                "The module id is unknown.", id);
        }

        ModuleDescriptor moduleDescriptor = this.modulePackageMap.availableModuleClassesMap
            .get(id);

        logger.exiting(this.getClass().getName(), "getModulePackage",
            moduleDescriptor.getClazz());

        return moduleDescriptor.getClazz();
    }

    /**
     * Fires a notification that a new <em>ModuleDescriptor</em> has been registered.
     * The event handling is done in {@link Core#update(Observable, Object)}.
     * @param moduleDescriptor Is the newly registered <em>ModuleDescriptor</em>
     */
    void fireNewModulePackage(ModuleDescriptor moduleDescriptor) {
        logger
            .entering(this.getClass().getName(), "fireNewModulePackage");

        setChanged();

        notifyObservers(moduleDescriptor);

        clearChanged();

        logger.exiting(this.getClass().getName(), "fireNewModulePackage");
    }

    /**
     * This nested class contains a <code>Map</code> with every
     * created {@link Module} in it as a value and the module's unique
     * int id as the key.
     */
    private static class ModuleInstanceMap {

        HashMap<Integer, Module> runningModuleMap = new HashMap<Integer, Module>();
    }

    /**
     * This nested class contains a <code>Map</code> with every
     * found <em>ModulePackage</em> in it as a value and the
     * <em>ModulePackage's</em> unique <code>String</code> id as
     * the key.
     */
    private static final class ModulePackagesMap {

        /**
         * This is the logger.
         */
        private static Logger modulePackagesMapLogger = LogHelper
            .createLogger(ModulePackagesMap.class.getName());

        /**
         * A reference to the module directory path that is currently
         * looked up for <em>ModulePackages</em>.
         */
        private String currentModuleDirectoryString;

        /**
         * This is the name of the module property file.
         */
        private static final String MODULE_PROPERTY_FILE = "module.properties.xml";

        /**
         * This is the <code>Map</code> itself.
         */
        private HashMap<String, ModuleDescriptor> availableModuleClassesMap = null;

        /**
         * The module directory.
         */
        private File moduleDirectory;

        /**
         * A reference to the sorrounding <em>ModuleRegistry</em>.
         */
        private ModuleRegistry moduleRegistry;

        /**
         * Creates the <em>ModulePackagesMap</em> and starts looking
         * up for <em>ModulePackages</em> by calling
         * {@link #initialize()}.
         * @param registry
         *            Is the sorrounding <em>ModuleRegistry</em>
         * @param directory
         *            Is the module directory
         */
        private ModulePackagesMap(final ModuleRegistry registry,
            final File directory) {
            modulePackagesMapLogger.entering(this.getClass().getName(),
                "InstalledModules", new Object[] {registry, directory});

            this.availableModuleClassesMap = new HashMap<String, ModuleDescriptor>();

            this.moduleRegistry = registry;

            this.moduleDirectory = directory;

            modulePackagesMapLogger.exiting(this.getClass().getName(),
                "InstalledModules");

        }

        /**
         * This methos is looking for <em>ModulePackages</em> inside
         * the module directory. For every found
         * <em>ModulePackage</em> the module property file is parsed
         * and a {@link ModuleDescriptor} is build and stored inside
         * the <code>Map</code>.
         * @throws ModuleClassLoaderInitializationException
         *             If an <code>Exception</code> occures while
         *             initializing the
         *             {@link org.electrocodeogram.modulepackage.classloader.ModuleClassLoader}
         */
        void initialize() throws ModuleClassLoaderInitializationException {
            modulePackagesMapLogger.entering(this.getClass().getName(),
                "initialize");

            String[] moduleDirectories = getModulePackages();

            int length = moduleDirectories.length;

            for (int i = 0; i < length; i++) {

                this.currentModuleDirectoryString = this.moduleDirectory
                                                    + File.separator
                                                    + moduleDirectories[i];

                File currentModuleDirectory = new File(
                    this.currentModuleDirectoryString);

                // skip all simple files
                if (!currentModuleDirectory.isDirectory()) {
                    modulePackagesMapLogger.log(Level.FINE,
                        "Skipping simple file in module directory: "
                                        + currentModuleDirectory
                                            .getAbsolutePath());

                    continue;
                }

                String modulePropertyFileString = this.currentModuleDirectoryString
                                                  + File.separator
                                                  + MODULE_PROPERTY_FILE;

                File modulePropertyFile = new File(modulePropertyFileString);

                // inspect module.property file and skip if neccessary
                if (!modulePropertyFile.exists()
                    || !modulePropertyFile.isFile()) {

                    modulePackagesMapLogger.log(Level.FINE,
                        "The module property file does not exist or is not a file: "
                                        + modulePropertyFileString);

                    continue;

                }

                ModuleDescriptor moduleDescriptor = null;
                try {
                    moduleDescriptor = ECGParser
                        .parseAsModuleDescriptor(modulePropertyFile);
                } catch (ClassLoadingException e) {
                    modulePackagesMapLogger.log(Level.WARNING,
                        "Error while loading the module: "
                                        + modulePropertyFileString);

                    continue;
                } catch (MicroSensorDataTypeException e) {
                    modulePackagesMapLogger.log(Level.WARNING,
                        "Error while loading the module: "
                                        + modulePropertyFileString);

                    continue;
                } catch (SAXException e) {
                    modulePackagesMapLogger.log(Level.WARNING,
                        "Error while loading the module: "
                                        + modulePropertyFileString);

                    modulePackagesMapLogger.log(Level.FINEST, e.getMessage());

                    continue;
                } catch (IOException e) {
                    modulePackagesMapLogger.log(Level.WARNING,
                        "Error while loading the module: "
                                        + modulePropertyFileString);

                    modulePackagesMapLogger.log(Level.FINEST, e.getMessage());

                    continue;
                } catch (NodeException e) {
                    modulePackagesMapLogger.log(Level.WARNING,
                        "Error while loading the module: "
                                        + modulePropertyFileString);

                    modulePackagesMapLogger.log(Level.FINEST, e.getMessage());

                    continue;

                } catch (ModulePropertyException e) {
                    modulePackagesMapLogger.log(Level.WARNING,
                        "Error while loading the module: "
                                        + modulePropertyFileString);

                    modulePackagesMapLogger.log(Level.FINEST, e.getMessage());

                    continue;
                }
                if (moduleDescriptor == null) {
                    modulePackagesMapLogger.log(Level.WARNING,
                        "Error while loading the module: "
                                        + modulePropertyFileString);

                    continue;
                }

                if (this.availableModuleClassesMap.containsKey(moduleDescriptor
                    .getId())) {
                    modulePackagesMapLogger.log(Level.SEVERE,
                        "A module with the id " + moduleDescriptor.getId()
                                        + " is allready loaded.");

                    modulePackagesMapLogger.log(Level.FINEST,
                        "The ModuleDescriptor was null.");

                    continue;
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

            modulePackagesMapLogger.exiting(this.getClass().getName(),
                "initialize");
        }

        /**
         * Returns all <em>ModulePackage</em> directories inside the
         * module directory.
         * @return All <em>ModulePackage</em> directories
         * @throws ModuleClassLoaderInitializationException
         *             If an <code>Exception</code> occures while
         *             initializing the
         *             {@link org.electrocodeogram.modulepackage.classloader.ModuleClassLoader}
         */
        private String[] getModulePackages()
            throws ModuleClassLoaderInitializationException {

            modulePackagesMapLogger.entering(this.getClass().getName(),
                "getModulePackages");

            // is the parameter not null?
            if (this.moduleDirectory == null) {

                modulePackagesMapLogger.exiting(this.getClass().getName(),
                    "initialize");

                throw new ModuleClassLoaderInitializationException(
                    "The provided module directory path is \"null\".");
            }

            // does the file exist and is it a directory?
            if (!this.moduleDirectory.exists()
                || !this.moduleDirectory.isDirectory()) {

                modulePackagesMapLogger.exiting(this.getClass().getName(),
                    "initialize");

                throw new ModuleClassLoaderInitializationException(
                    "The module directory does not exist or is not a directory.");
            }

            // get all filenames in it
            String[] moduleDirectories = this.moduleDirectory.list();

            // assert no IO-Error has occurred
            if (moduleDirectories == null) {

                modulePackagesMapLogger.entering(this.getClass().getName(),
                    "getModulePackages");

                throw new ModuleClassLoaderInitializationException(
                    "The module directory does not contain any subdirectories.");
            }

            // are there any files in it?
            if (!(moduleDirectories.length > 0)) {

                modulePackagesMapLogger.entering(this.getClass().getName(),
                    "getModulePackages");

                throw new ModuleClassLoaderInitializationException(
                    "The module directory does not contain any subdirectories.");
            }

            modulePackagesMapLogger.entering(this.getClass().getName(),
                "getModulePackages", moduleDirectories);

            return moduleDirectories;
        }

    }

    /**
     * This is used to register a new module instance. The method is
     * called from the constructor of every {@link Module}.
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

        if (this.moduleInstanceMap.runningModuleMap.containsKey(new Integer(
            module.getId()))) {

            logger.log(Level.WARNING, "This module is allready registered.");

            logger.exiting(this.getClass().getName(), "registerRunningModule");

            return;
        }

        this.moduleInstanceMap.runningModuleMap.put(
            new Integer(module.getId()), module);

        logger.log(Level.INFO, "A new module instance with name "
                               + module.getName() + " has been registered.");

        fireModuleInstance(module);

        logger.exiting(this.getClass().getName(), "registerRunningModule");

    }

    /**
     * Fires a notification about a module instance that has either been registered or deregistered with the <em>ModuleRegistry</em>.
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
            new Object[] {new Integer(id)});

        if (!(id > 0)) {

            logger.exiting(this.getClass().getName(), "getModule");

            throw new ModuleInstanceNotFoundException(
                "The module id is invalid.", id);
        }

        assert (id > 0);

        if (!(this.moduleInstanceMap.runningModuleMap.containsKey(new Integer(
            id)))) {

            logger.exiting(this.getClass().getName(), "getModule");

            throw new ModuleInstanceNotFoundException(
                "The module id is unknown.", id);
        }

        logger.exiting(this.getClass().getName(), "getModule",
            this.moduleInstanceMap.runningModuleMap.get(new Integer(id)));

        return this.moduleInstanceMap.runningModuleMap.get(new Integer(id));
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

        this.moduleInstanceMap.runningModuleMap.remove(new Integer(module
            .getId()));

        logger.log(Level.INFO, "The module " + module.getName()
                               + " has been deregistered.");

        fireModuleInstance(module);

        logger.exiting(this.getClass().getName(), "deregisterModule");

    }

    /**
     * @see org.electrocodeogram.module.registry.IModuleRegistry#getModuleDescriptor(java.lang.String)
     */
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
                           + (module.getState()) + "\">");

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
                    moduleActivationList.add(new Integer(assignedModuleId));
                }

                if (moduleIdTransformationMap.containsKey(new Integer(
                    moduleConfiguration.getModuleId()))) {
                    throw new ModuleSetupLoadException(
                        "Duplicate module id found "
                                        + moduleConfiguration.getModuleId(),
                        file.getAbsolutePath());
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
                                            + storedModuleId, file
                                .getAbsolutePath());
                    }
                    Module module = getModule(assignedModuleId.intValue());

                    Module receivingModule = getModule(assignedReceivingModuleId
                        .intValue());

                    module.connectReceiverModule(receivingModule);

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
