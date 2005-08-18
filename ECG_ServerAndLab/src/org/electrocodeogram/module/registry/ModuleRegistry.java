package org.electrocodeogram.module.registry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xerces.parsers.DOMParser;
import org.electrocodeogram.module.Module;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * This is the central ModuleRegistry which maintains information about all
 * currently instantiated modules and all module class files in the module
 * directory from which module instances could be created at runtime.
 * 
 * During its creation every module object registers with the ModuleRegistry
 * with its unique id. On deletion of a module the module is deregistered from
 * the ModuleRegistry.
 * 
 */
public class ModuleRegistry extends Observable
{

    private Logger logger = null;

    private RunningModules runningModules = null;

    private InstalledModules installedModules = null;

    /**
     * The constructor creates the ModuleRegistry instance.
     */
    public ModuleRegistry()
    {
        this.logger = Logger.getLogger("ModuleRegistry");

        this.runningModules = new RunningModules();

    }

    /**
     * The constructor creates the ModuleRegistry instance.
     * 
     * @param moduleDirectory
     *            This should be the module directory
     */
    public ModuleRegistry(File moduleDirectory)
    {
        this();

        try {
            this.installedModules = new InstalledModules(moduleDirectory);
        }
        catch (ModuleClassLoaderInitializationException e) {

            this.logger.log(Level.WARNING, "Error while initializing the module class loading.");

            this.logger.log(Level.WARNING, e.getMessage());
        }

    }

    /**
     * If the module directory is not known during ModuleRegistry creation this
     * method can be used to set the module directory later.
     * 
     * @param file
     *            This should be the module directory
     */
    public void setFile(File file)
    {

        try {
            this.installedModules = new InstalledModules(file);

            setChanged();

            notifyObservers();

            clearChanged();
        }
        catch (ModuleClassLoaderInitializationException e) {

            this.logger.log(Level.WARNING, "Error while initializing the module class loading.");

            this.logger.log(Level.WARNING, e.getMessage());
        }

    }

    /**
     * This returns the Logger instance.
     * 
     * @return The Logger instance
     */
    protected Logger getLogger()
    {
        return this.logger;
    }

    /**
     * This method returns the IDs of all currently known module class files
     * that are ready to be instantiated.
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
        if (!(moduleClassId > 0)) {
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
     *         of the module as an Array of ModuleProperty objects
     * @throws IllegalModuleIDException
     *             If the given moduleClassId has a value of < 1
     * @throws UnknownModuleIDException
     *             If a module class with the given id could not be found
     */
    public ModuleProperty[] getModuleClassProperties(int moduleClassId) throws IllegalModuleIDException, UnknownModuleIDException
    {
        if (!(moduleClassId > 0)) {
            throw new IllegalModuleIDException();
        }

        if (this.installedModules == null) {
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

    void notifyOfNewModuleDecriptor(ModuleDescriptor moduleDescriptor)
    {
        setChanged();

        notifyObservers(moduleDescriptor);

        clearChanged();
    }

    private static class RunningModules
    {
        HashMap<Integer, Module> runningModuleMap = new HashMap<Integer, Module>();

    }

    private class InstalledModules
    {

        private static final String MODULE_PROPERTY_FILE = "module.properties.xml";

        HashMap<Integer, ModuleDescriptor> availableModuleClassesMap = null;

        private int id = 1;

        private ModuleClassLoader moduleClassLoader = null;

        private InstalledModules(File moduleDirectory) throws ModuleClassLoaderInitializationException
        {
            this.availableModuleClassesMap = new HashMap<Integer, ModuleDescriptor>();

            this.moduleClassLoader = getModuleClassLoader();

            initialize(moduleDirectory);
        }

        private ModuleClassLoader getModuleClassLoader()
        {

            Class clazz = this.getClass();

            ClassLoader currentClassLoader = clazz.getClassLoader();

            return new ModuleClassLoader(currentClassLoader);
        }

        private void initialize(File moduleDirectory) throws ModuleClassLoaderInitializationException
        {
            // is the parameter not null?
            if (moduleDirectory == null) {
                throw new ModuleClassLoaderInitializationException(
                        "The provided module directory path is \"null\".");
            }

            // does the file exist and is it a directory?
            if (!moduleDirectory.exists() || !moduleDirectory.isDirectory()) {
                throw new ModuleClassLoaderInitializationException(
                        "The module directory does not exist or is not a directory.");
            }

            // get all filenames in it
            String[] moduleDirectories = moduleDirectory.list();

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

            assert (length > 0);

            for (int i = 0; i < length; i++) {

                String currentModuleDirectoryPath = moduleDirectory + File.separator + moduleDirectories[i];

                File currentModuleDirectory = new File(
                        currentModuleDirectoryPath);

                // skip all simple files
                if (!currentModuleDirectory.isDirectory()) {

                    getLogger().log(Level.WARNING, "Skipping simple file in module directory: " + currentModuleDirectory.getAbsolutePath());

                    continue;
                }

                String modulePropertyFileString = currentModuleDirectoryPath + File.separator + MODULE_PROPERTY_FILE;

                File modulePropertyFile = new File(modulePropertyFileString);

                // inspect module.property file and skip if neccessary
                if (!modulePropertyFile.exists() || !modulePropertyFile.isFile()) {

                    getLogger().log(Level.WARNING, "The module property file does not exist or is not a file: " + modulePropertyFileString);

                    continue;

                }

                Document document = null;

                InputSource inputSource = null;

                // read the property file
                try {
                    inputSource = new InputSource(new FileReader(
                            modulePropertyFile));
                }
                catch (FileNotFoundException e2) {

                    // is checked before and should never happen

                    // TODO : message

                }

                // create the XML parser
                DOMParser parser = new DOMParser();

                // set the parsing properties
                try {
                    parser.setFeature("http://xml.org/sax/features/validation", true);
                    parser.setFeature("http://apache.org/xml/features/validation/schema", true);
                    parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
                    parser.setFeature("http://apache.org/xml/features/validation/dynamic", true);

                    // parse the property file against the "module.properties.xsd XML schema
                    parser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", "module.properties.xsd");

                }
                catch (SAXNotRecognizedException e1) {
                    // is checked and schould never occure

                    // TODO : message

                }
                catch (SAXNotSupportedException e1) {
                    // is checked and schould never occure

                    // TODO : message
                }

                // parse    
                try {
                    parser.parse(inputSource);

                    // get the XML document
                    document = parser.getDocument();
                }
                catch (SAXException e2) {

                    getLogger().log(Level.WARNING, "Error parsing module property file " + modulePropertyFileString);

                    getLogger().log(Level.WARNING, e2.getMessage());

                    continue;
                }
                catch (IOException e2) {

                    getLogger().log(Level.WARNING, "Error parsing module property file " + modulePropertyFileString);

                    getLogger().log(Level.WARNING, e2.getMessage());

                    continue;
                }

                // get the node values
                Node moduleNameNode = document.getElementsByTagName("name").item(0);

                String moduleName = moduleNameNode.getFirstChild().getNodeValue();

                Node moduleClassNode = document.getElementsByTagName("class").item(0);

                String moduleClassName = moduleClassNode.getFirstChild().getNodeValue();

                Node moduleDescriptionNode = document.getElementsByTagName("description").item(0);

                String moduleDescription = moduleDescriptionNode.getFirstChild().getNodeValue();

                Node properties = document.getElementsByTagName("properties").item(0);

                ModuleProperty[] moduleProperties = null;
                
                if (properties != null) {

                    NodeList propertyList = document.getElementsByTagName("property");

                    moduleProperties = new ModuleProperty[propertyList.getLength()];
                    
                    if (propertyList != null)
                    {
                        for (int j = 0; j < propertyList.getLength(); j++)
                        {
                            Node propertyNode = propertyList.item(j);
                            
                            NodeList propertyNodeChildNodes = propertyNode.getChildNodes();
                            
                            Node modulePropertyNameNode = propertyNodeChildNodes.item(1);
                            
                            if(!modulePropertyNameNode.getNodeName().equals("propertyName"))
                            {
                                getLogger().log(Level.WARNING, "Error parsing module property file " + modulePropertyFileString);

                                continue;
                            }
                            
                            Node modulePropertyTypeNode = propertyNodeChildNodes.item(3);
                            
                            if(!modulePropertyTypeNode.getNodeName().equals("propertyType"))
                            {
                                getLogger().log(Level.WARNING, "Error parsing module property file " + modulePropertyFileString);

                                continue;
                            }
                            
                            Node modulePropertyValueNode = propertyNodeChildNodes.item(5);
                            
                            if(!modulePropertyValueNode.getNodeName().equals("propertyValue"))
                            {
                                getLogger().log(Level.WARNING, "Error parsing module property file " + modulePropertyFileString);

                                continue;
                            }
                            
                            String modulePropertyName = modulePropertyNameNode.getFirstChild().getNodeValue();

                            if (modulePropertyName == null || modulePropertyName.equals("")) {
                                getLogger().log(Level.WARNING, "Error parsing module property file " + modulePropertyFileString);

                                continue;
                            }

                            
                            String modulePropertyType = modulePropertyTypeNode.getFirstChild().getNodeValue();

                            if (modulePropertyType == null || modulePropertyType.equals("")) {
                                getLogger().log(Level.WARNING, "Error parsing module property file " + modulePropertyFileString);

                                continue;
                            }
                            
                            Class type = null;
                            try {
                                type = Class.forName(modulePropertyType);
                            }
                            catch (ClassNotFoundException e) {
                                
                                getLogger().log(Level.WARNING, "The property type is not a full qualified Java class name.");
                                
                                getLogger().log(Level.WARNING, "Property type: " + modulePropertyType + " in module property file: " + modulePropertyFileString);
                                
                                continue;
                            }
                            
                            String modulePropertyValue = null;
                            
                            if(modulePropertyValueNode != null)
                            {
                                modulePropertyValue = modulePropertyValueNode.getFirstChild().getNodeValue();
    
                                if (modulePropertyValue == null || modulePropertyValue.equals("")) {
                                    getLogger().log(Level.WARNING, "Error parsing module property file " + modulePropertyFileString);
    
                                    continue;
                                }
                            }
                            
                            moduleProperties[j] = new ModuleProperty(modulePropertyName,modulePropertyValue,type);
                            
                            
                        }
                    }

                }

                String fQmoduleClassString = currentModuleDirectoryPath + File.separator + moduleClassName + ".class";

                Class moduleClass = null;

                try {

                    moduleClass = this.moduleClassLoader.loadClass(fQmoduleClassString);

                    int moduleClassId = this.id++;

                    // make a new ModuleDescriptor for this module class nad
                    //assign it the unique id
                    ModuleDescriptor moduleDescriptor = new ModuleDescriptor(
                            moduleClassId, moduleName, moduleClass,
                            moduleDescription, moduleProperties);

                    // put the ModuleDescriptor into the HashMap
                    this.availableModuleClassesMap.put(new Integer(
                            moduleClassId), moduleDescriptor);

                    getLogger().log(Level.INFO, "Loaded additional module class with id: " + moduleClassId + " " + moduleDescriptor.getClazz().getName());

                    notifyOfNewModuleDecriptor(moduleDescriptor);

                }
                catch (ClassNotFoundException e) {

                    getLogger().log(Level.SEVERE, "Unable to load the module " + moduleName + " because the class " + fQmoduleClassString + " is not found.\nProceeding with next module if any.");
                }

            }
        }
    }

    /**
     * This method registers a module instance with the ModuleRegistry. If the
     * module instance is already registered with the ModuleRegistry nothing
     * happens. This method is automatically called whenever a new object of
     * class Module is created.
     * 
     * @param module
     *            Is the module instance to register
     */
    public void registerModuleInstance(Module module)
    {
        // check parameter
        if (module == null) {
            return;
        }

        if (this.runningModules.runningModuleMap.containsKey(new Integer(
                module.getId()))) {
            return;
        }

        this.runningModules.runningModuleMap.put(new Integer(module.getId()), module);

        this.logger.log(Level.INFO, "Regestered module " + module.getName());

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
     *            Is the id of the module class to be instantiated
     * @param moduleName
     *            Is the name that should be given to the new module object
     * @throws ModuleInstantiationException
     *             If an exception occurs during the instantiation of the module
     * @throws IllegalModuleIDException
     *             If the given moduleClassId has a value of < 1
     * @throws UnknownModuleIDException
     *             If a module class with the given id could not be found
     */
    public void createModuleInstance(int moduleClassId, String moduleName) throws ModuleInstantiationException, IllegalModuleIDException, UnknownModuleIDException
    {
        if (!(moduleClassId > 0)) {
            throw new IllegalModuleIDException();
        }

        assert (moduleClassId > 0);

        Class moduleClass = getModuleClassForId(moduleClassId);

        try {

            Constructor[] constructors = moduleClass.getConstructors();

            Object[] args = new Object[] { new Integer(moduleClassId), moduleName };

            constructors[0].newInstance(args);

        }
        catch (InstantiationException e) {

            throw new ModuleInstantiationException(e.getMessage());

        }
        catch (IllegalAccessException e) {

            throw new ModuleInstantiationException(e.getMessage());
        }
        catch (IllegalArgumentException e) {
            throw new ModuleInstantiationException(e.getMessage());
        }
        catch (InvocationTargetException e) {
            throw new ModuleInstantiationException(e.getMessage());
        }
    }

    /**
     * This method removes a module instance from the ModuleRegistry. It is
     * called from the Module.remove() method.
     * 
     * @param moduleId
     *            Is the id of the module to deregister.
     * @throws IllegalModuleIDException
     *             If the given moduleClassId has a value of < 1
     * @throws UnknownModuleIDException
     *             If a module class with the given id could not be found
     * 
     */
    public void deregisterModuleInstance(int moduleId) throws UnknownModuleIDException, IllegalModuleIDException
    {
        if (!(moduleId > 0)) {
            throw new IllegalModuleIDException();
        }

        assert (moduleId > 0);

        Module module = getModuleInstance(moduleId);

        this.runningModules.runningModuleMap.remove(new Integer(moduleId));

        this.logger.log(Level.INFO, "Deregestered module " + module.getName());

        setChanged();

        notifyObservers(module);

        clearChanged();

    }

    /**
     * This method returns the description of the module class with the given
     * id. The description is given by the module developer in the module's
     * "module.property" file.
     * 
     * @param moduleClassId
     *            Is the id of the module class to get the description of
     * @return The description about the module class
     * @throws IllegalModuleIDException
     *             If the given moduleClassId has a value of < 1
     * @throws UnknownModuleIDException
     *             If a module class with the given id could not be found
     */
    public String getModuleClassDescription(int moduleClassId) throws IllegalModuleIDException, UnknownModuleIDException
    {

        if(!(moduleClassId > 0))
        {
            throw new IllegalModuleIDException();
        }
        
        if(!this.installedModules.availableModuleClassesMap.containsKey(new Integer(moduleClassId)))
        {
            throw new UnknownModuleIDException();
        }

        ModuleDescriptor moduleDescriptor = this.installedModules.availableModuleClassesMap.get(new Integer(moduleClassId));
        
        return moduleDescriptor.getDescription();
    }

}
