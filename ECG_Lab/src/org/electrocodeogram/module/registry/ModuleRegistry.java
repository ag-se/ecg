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
import org.electrocodeogram.module.ModuleDescriptor;
import org.electrocodeogram.module.ModuleProperty;
import org.electrocodeogram.module.loader.ModuleClassLoader;
import org.electrocodeogram.module.loader.ModuleClassLoaderInitializationException;
import org.electrocodeogram.moduleapi.module.registry.IModuleModuleRegistry;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.msdt.MicroSensorDataTypeException;
import org.electrocodeogram.system.SystemRoot;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * This is the ModuleRegistry which maintains information about all currently
 * available in the module directory and all running modules.
 * 
 * An available module is a set of a module class that extends the class Module
 * or one of its subclasses, additonal classes and a "module.properties.xml"
 * file that is an instance of the "module.properties.xsd" XML schema and
 * provides the neccessary information about he module.
 * 
 * At runtime multiple running modules can be created from each available
 * module.
 * 
 * The ModuleRegistry is accessible through two interfaces.
 * ISystemModuleRegistry provides methods for other ECG system components and
 * IModuleModuleRegistry provides methods for ECG modules.
 * 
 */
public class ModuleRegistry extends Observable implements ISystemModuleRegistry, IModuleModuleRegistry
{

	private Logger logger;

	private RunningModules runningModules;

	private InstalledModules installedModules;

	/**
	 * The constructor creates the ModuleRegistry instance.
	 */
	public ModuleRegistry()
	{
		this.logger = Logger.getLogger("ModuleRegistry");

		this.runningModules = new RunningModules();

	}

	/**
	 * The constructor creates the ModuleRegistry instance. the given path is
	 * searched for available modules.
	 * 
	 * @param moduleDirectory
	 *            This path is searched for available modules
	 * @throws ModuleClassLoaderInitializationException 
	 */
	public ModuleRegistry(File moduleDirectory) throws ModuleClassLoaderInitializationException
	{
		this();

		this.installedModules = new InstalledModules(moduleDirectory);
	
		this.installedModules.initialize();
		
		setChanged();

		notifyObservers();

		clearChanged();
	}

	/**
	 * If the module directory is not known at ModuleRegistry creation this
	 * method is used by the SystemRoot to set the module directory when known.
	 * This method is not for use by the user.
	 * 
	 * @param file
	 *            This path is searched for available modules
	 * @throws ModuleClassLoaderInitializationException 
	 */
	public void setFile(File file) throws ModuleClassLoaderInitializationException
	{

		
			this.installedModules = new InstalledModules(file);

			this.installedModules.initialize();

			setChanged();

			notifyObservers();

			clearChanged();
	}

	/**
	 * This returns the ModuleRegistry's Logger instance. This method is nor for
	 * use by the user.
	 * 
	 * @return The Logger instance
	 */
	protected Logger getLogger()
	{
		return this.logger;
	}

	/**
	 * @see org.electrocodeogram.module.registry.ISystemModuleRegistry#getAvailableModuleIds()
	 */
	public String[] getAvailableModuleIds()
	{
		if (this.installedModules.availableModuleClassesMap.size() > 0)
		{
			return this.installedModules.availableModuleClassesMap.keySet().toArray(new String[0]);
		}

		return null;
	}

	private Class getModuleClassForId(String moduleClassId) throws ModuleClassException
	{
		if (moduleClassId == null || moduleClassId.equals(""))
		{
			throw new ModuleClassException("The module id is empty.");
		}

		if (!this.installedModules.availableModuleClassesMap.containsKey(moduleClassId))
		{
			throw new ModuleClassException(
					"The module id " + moduleClassId + " is unknown.");
		}

		ModuleDescriptor moduleDescriptor = this.installedModules.availableModuleClassesMap.get(moduleClassId);

		return moduleDescriptor.getClazz();
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
		private String currentModuleDirectoryString;

		private static final String MODULE_PROPERTY_FILE = "module.properties.xml";

		HashMap<String, ModuleDescriptor> availableModuleClassesMap = null;

		private ModuleClassLoader moduleClassLoader = null;

		private File $moduleDirectory;

		private InstalledModules(File moduleDirectory)
		{
			this.availableModuleClassesMap = new HashMap<String, ModuleDescriptor>();

			this.moduleClassLoader = getModuleClassLoader();

			this.$moduleDirectory = moduleDirectory;

		}

		private ModuleClassLoader getModuleClassLoader()
		{

			Class clazz = this.getClass();

			ClassLoader currentClassLoader = clazz.getClassLoader();

			return new ModuleClassLoader(currentClassLoader);
		}

		void initialize() throws ModuleClassLoaderInitializationException
		{
			// is the parameter not null?
			if (this.$moduleDirectory == null)
			{
				throw new ModuleClassLoaderInitializationException(
						"The provided module directory path is \"null\".");
			}
			
			// does the file exist and is it a directory?
			if (!this.$moduleDirectory.exists() || !this.$moduleDirectory.isDirectory())
			{
				throw new ModuleClassLoaderInitializationException(
						"The module directory does not exist or is not a directory.");
			}

			// get all filenames in it
			String[] moduleDirectories = this.$moduleDirectory.list();

			// assert no IO-Error has occurred
			if (moduleDirectories == null)
			{
				throw new ModuleClassLoaderInitializationException(
						"The module directory does not contain any subdirectories.");
			}

			int length = moduleDirectories.length;

			// are there any files in it?
			if (!(length > 0))
			{
				throw new ModuleClassLoaderInitializationException(
						"The module directory does not contain any subdirectories.");
			}

			nextmodule: for (int i = 0; i < length; i++)
			{

				this.currentModuleDirectoryString = this.$moduleDirectory + File.separator + moduleDirectories[i];

				File currentModuleDirectory = new File(
						this.currentModuleDirectoryString);

				// skip all simple files
				if (!currentModuleDirectory.isDirectory())
				{
					getLogger().log(Level.WARNING, "Skipping simple file in module directory: " + currentModuleDirectory.getAbsolutePath());

					continue;
				}

				String modulePropertyFileString = this.currentModuleDirectoryString + File.separator + MODULE_PROPERTY_FILE;

				File modulePropertyFile = new File(modulePropertyFileString);

				// inspect module.property file and skip if neccessary
				if (!modulePropertyFile.exists() || !modulePropertyFile.isFile())
				{

					getLogger().log(Level.WARNING, "The module property file does not exist or is not a file: " + modulePropertyFileString);

					continue nextmodule;

				}

				Document document = null;

				try
				{
					document = parseDocument(modulePropertyFile);
				}
				catch (Exception e)
				{

					getLogger().log(Level.WARNING, "Error parsing module property file " + modulePropertyFileString);

					getLogger().log(Level.WARNING, e.getMessage());

					continue nextmodule;
				}
			
				
				ModuleDescriptor moduleDescriptor = null;
				
				try
				{
					MicroSensorDataType[] microSensorDataTypes = getMicroSensorDataTypes(document);

					ModuleProperty[] moduleProperties = getModuleProperties(document);

					moduleDescriptor = new ModuleDescriptor(
							getNodeValue("id", document),
							getNodeValue("name", document),
							getNodeValue("provider-name", document),
							getNodeValue("version", document),
							getClass(this.currentModuleDirectoryString, getNodeValue("class", document)),
							getNodeValue("description", document),
							moduleProperties, microSensorDataTypes);
				}
				catch (NodeValueException e)
				{
					getLogger().log(Level.SEVERE, "Unable to load the module.");

					continue nextmodule;
				}
				catch (ClassLoadingException e)
				{
					getLogger().log(Level.SEVERE, "Unable to load the module.");

					continue nextmodule;
				}
				catch (PropertyException e)
				{
					getLogger().log(Level.SEVERE, "Unable to load the module.");

					continue nextmodule;
				}
				catch (MicroSensorDataTypeException e)
				{
					getLogger().log(Level.SEVERE, "Unable to load the module.");

					continue nextmodule;
				}

				if (this.availableModuleClassesMap.containsKey(moduleDescriptor.getId()))
				{
					getLogger().log(Level.SEVERE, "A module with the id " + moduleDescriptor.getId() + " is allready loaded.");

					continue nextmodule;
				}

				// put the ModuleDescriptor into the HashMap
				this.availableModuleClassesMap.put(moduleDescriptor.getId(), moduleDescriptor);

				getLogger().log(Level.INFO, "Loaded additional module class with id: " + moduleDescriptor.getId() + " " + moduleDescriptor.getClazz().getName());

				notifyOfNewModuleDecriptor(moduleDescriptor);

			}
		}

		private MicroSensorDataType[] getMicroSensorDataTypes(Document document) throws MicroSensorDataTypeException
		{
			Node microSensorDataTypesNode = document.getElementsByTagName("microsensordatatypes").item(0);

			MicroSensorDataType[] microSensorDataTypes = null;

			if (microSensorDataTypesNode != null)
			{

				NodeList msdtList = document.getElementsByTagName("microsensordatatype");

				if (msdtList != null)
				{
					microSensorDataTypes = new MicroSensorDataType[msdtList.getLength()];

					for (int j = 0; j < msdtList.getLength(); j++)
					{
						Node msdtNode = msdtList.item(j);

						NodeList msdtNodeChildNodes = msdtNode.getChildNodes();

						Node msdtNameNode = msdtNodeChildNodes.item(1);

						if (msdtNameNode == null || !msdtNameNode.getNodeName().equals("msdtName"))
						{
							throw new MicroSensorDataTypeException("");
						}

						Node msdtFileNode = msdtNodeChildNodes.item(3);

						if (msdtFileNode != null && !msdtFileNode.getNodeName().equals("msdtFile"))
						{
							throw new MicroSensorDataTypeException("");
						}

						String msdtName = msdtNameNode.getFirstChild().getNodeValue();

						if (msdtName == null || msdtName.equals(""))
						{
							throw new MicroSensorDataTypeException("");
						}

						String msdtFileString = msdtFileNode.getFirstChild().getNodeValue();

						if (msdtFileString == null || msdtFileString.equals(""))
						{
							throw new MicroSensorDataTypeException("");
						}

						File msdtFile = new File(
								this.currentModuleDirectoryString + File.separator + msdtFileString);

						try
						{
							microSensorDataTypes[j] = SystemRoot.getSystemInstance().getSystemMsdtRegistry().parseMicroSensorDataType(msdtFile);
						}
						catch (MicroSensorDataTypeException e)
						{

							throw new MicroSensorDataTypeException("");

						}

					}
				}

			}
			return microSensorDataTypes;
		}

		private ModuleProperty[] getModuleProperties(Document document) throws PropertyException
		{
			Node properties = document.getElementsByTagName("properties").item(0);

			ModuleProperty[] moduleProperties = null;

			if (properties != null)
			{

				NodeList propertyList = document.getElementsByTagName("property");

				if (propertyList != null)
				{
					moduleProperties = new ModuleProperty[propertyList.getLength()];

					for (int j = 0; j < propertyList.getLength(); j++)
					{
						Node propertyNode = propertyList.item(j);

						NodeList propertyNodeChildNodes = propertyNode.getChildNodes();

						Node modulePropertyNameNode = propertyNodeChildNodes.item(1);

						if (modulePropertyNameNode == null || !modulePropertyNameNode.getNodeName().equals("propertyName"))
						{
							throw new PropertyException();
						}

						Node modulePropertyTypeNode = propertyNodeChildNodes.item(3);

						if (modulePropertyTypeNode == null || !modulePropertyTypeNode.getNodeName().equals("propertyType"))
						{
							throw new PropertyException();
						}

						Node modulePropertyValueNode = propertyNodeChildNodes.item(5);

						if (modulePropertyValueNode == null || !modulePropertyValueNode.getNodeName().equals("propertyValue"))
						{
							throw new PropertyException();
						}

						String modulePropertyName = modulePropertyNameNode.getFirstChild().getNodeValue();

						if (modulePropertyName == null || modulePropertyName.equals(""))
						{
							throw new PropertyException();
						}

						String modulePropertyType = modulePropertyTypeNode.getFirstChild().getNodeValue();

						if (modulePropertyType == null || modulePropertyType.equals(""))
						{
							throw new PropertyException();
						}

						Class type = null;
						try
						{
							type = Class.forName(modulePropertyType);
						}
						catch (ClassNotFoundException e)
						{

							throw new PropertyException();
						}

						String modulePropertyValue = null;

						if (modulePropertyValueNode != null)
						{
							modulePropertyValue = modulePropertyValueNode.getFirstChild().getNodeValue();

							if (modulePropertyValue == null || modulePropertyValue.equals(""))
							{
								throw new PropertyException();
							}
						}

						moduleProperties[j] = new ModuleProperty(
								modulePropertyName, modulePropertyValue, type);

					}
				}

			}
			return moduleProperties;

		}

		private Class getClass(String classPath, String className) throws ClassLoadingException
		{
			Class moduleClass;

			this.moduleClassLoader.addModuleClassPath(classPath);

			try
			{
				moduleClass = this.moduleClassLoader.loadClass(className);
			}
			catch (ClassNotFoundException e)
			{
				throw new ClassLoadingException();
			}

			if (moduleClass == null)
			{
				throw new ClassLoadingException();
			}

			return moduleClass;
		}

		private String getNodeValue(String nodeName, Document document) throws NodeValueException
		{
			Node node = document.getElementsByTagName(nodeName).item(0);

			if (node == null)
			{
				throw new NodeValueException();
			}

			String string = node.getFirstChild().getNodeValue();

			if (string == null)
			{
				throw new NodeValueException();
			}

			return string;
		}

		private Document parseDocument(File modulePropertyFile) throws SAXException, IOException
		{
			InputSource inputSource = null;

			// read the property file
			try
			{
				inputSource = new InputSource(
						new FileReader(modulePropertyFile));
			}
			catch (FileNotFoundException e2)
			{

				// is checked before and should never happen

				// TODO : message

			}

			// create the XML parser
			DOMParser parser = new DOMParser();

			// set the parsing properties
			try
			{
				parser.setFeature("http://xml.org/sax/features/validation", true);
				parser.setFeature("http://apache.org/xml/features/validation/schema", true);
				parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
				parser.setFeature("http://apache.org/xml/features/validation/dynamic", true);

				// parse the property file against the "module.properties.xsd
				// XML schema
				parser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", "lib/ecg/module.properties.xsd");

			}
			catch (SAXNotRecognizedException e1)
			{
				// is checked and schould never occure

				// TODO : message

			}
			catch (SAXNotSupportedException e1)
			{
				// is checked and schould never occure

				// TODO : message
			}

			// parse

			parser.parse(inputSource);

			// get the XML document
			return parser.getDocument();

		}

		private class NodeValueException extends Exception
		{
			private static final long serialVersionUID = -5525286840545881387L;
		}

		private class ClassLoadingException extends Exception
		{
			private static final long serialVersionUID = 2292155480118662068L;
		}

		private class PropertyException extends Exception
		{
			private static final long serialVersionUID = 4841697530729167223L;
		}
	}

	/**
	 * @see org.electrocodeogram.moduleapi.module.registry.IModuleModuleRegistry#registerRunningModule(org.electrocodeogram.module.Module)
	 */
	public void registerRunningModule(Module module)
	{
		// check parameter
		if (module == null)
		{
			return;
		}

		if (this.runningModules.runningModuleMap.containsKey(new Integer(
				module.getId())))
		{
			return;
		}

		this.runningModules.runningModuleMap.put(new Integer(module.getId()), module);

		this.logger.log(Level.INFO, "Registered module " + module.getName());

		SystemRoot.getSystemInstance().addSystemObserver(module.getSystemObserver());

		setChanged();

		notifyObservers(module);

		clearChanged();

	}

	/**
	 * @see org.electrocodeogram.module.registry.ISystemModuleRegistry#getRunningModule(int)
	 */
	public Module getRunningModule(int moduleId) throws ModuleInstanceException
	{
		if (!(moduleId > 0))
		{
			throw new ModuleInstanceException("The module id is invalid.");
		}

		assert (moduleId > 0);

		if (!(this.runningModules.runningModuleMap.containsKey(new Integer(
				moduleId))))
		{
			throw new ModuleInstanceException("The module id is unknown.");
		}

		return this.runningModules.runningModuleMap.get(new Integer(moduleId));
	}

	/**
	 * @see org.electrocodeogram.module.registry.ISystemModuleRegistry#createRunningModule(java.lang.String,
	 *      java.lang.String)
	 */
	public void createRunningModule(String id, String name) throws ModuleInstantiationException, ModuleClassException
	{
		if (id == null || id.equals(""))
		{
			throw new ModuleClassException("The module id is empty.");
		}

		Class moduleClass = getModuleClassForId(id);

		try
		{

			Constructor[] constructors = moduleClass.getConstructors();

			Object[] args = new Object[] { id, name };

			constructors[0].newInstance(args);

		}
		catch (InstantiationException e)
		{

			throw new ModuleInstantiationException(e.getMessage());

		}
		catch (IllegalAccessException e)
		{

			throw new ModuleInstantiationException(e.getMessage());
		}
		catch (IllegalArgumentException e)
		{
			throw new ModuleInstantiationException(e.getMessage());
		}
		catch (InvocationTargetException e)
		{
			throw new ModuleInstantiationException(e.getMessage());
		}
	}

	/**
	 * @see org.electrocodeogram.moduleapi.module.registry.IModuleModuleRegistry#deregisterRunningModule(int)
	 */
	public void deregisterRunningModule(int id) throws ModuleInstanceException
	{
		if (!(id > 0))
		{
			throw new ModuleInstanceException("The module id is invalid.");
		}

		assert (id > 0);

		Module module = getRunningModule(id);

		this.runningModules.runningModuleMap.remove(new Integer(id));

		this.logger.log(Level.INFO, "Deregestered module " + module.getName());

		SystemRoot.getSystemInstance().deleteSystemObserver(module.getSystemObserver());

		setChanged();

		notifyObservers(module);

		clearChanged();

	}

	/**
	 * @see org.electrocodeogram.module.registry.ISystemModuleRegistry#getModuleDescriptor(java.lang.String)
	 */
	public ModuleDescriptor getModuleDescriptor(String moduleClassId) throws ModuleClassException
	{
		if (moduleClassId == null || moduleClassId.equals(""))
		{
			throw new ModuleClassException("The module id is empty.");
		}

		if (this.installedModules == null)
		{
			throw new ModuleClassException("The module id is unknown.");
		}

		if (!this.installedModules.availableModuleClassesMap.containsKey(moduleClassId))
		{
			throw new ModuleClassException("The module id is unknown.");
		}

		return this.installedModules.availableModuleClassesMap.get(moduleClassId);

	}

}
