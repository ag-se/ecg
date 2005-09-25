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
import org.electrocodeogram.moduleapi.module.registry.IModuleModuleRegistry;
import org.electrocodeogram.msdt.MicroSensorDataTypeException;
import org.electrocodeogram.system.SystemRoot;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.xml.ClassLoadingException;
import org.electrocodeogram.xml.ECGParser;
import org.electrocodeogram.xml.PropertyException;
import org.xml.sax.SAXException;

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

	Logger logger;

	private RunningModules runningModules;

	private InstalledModules installedModules;

	/**
	 * The constructor creates the ModuleRegistry instance.
	 */
	public ModuleRegistry()
	{
		this.logger = LogHelper.createLogger(this);

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
	public void setModuleDirectory(File file) throws ModuleClassLoaderInitializationException
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

		private File $moduleDirectory;

		private InstalledModules(File moduleDirectory)
		{
			this.availableModuleClassesMap = new HashMap<String, ModuleDescriptor>();

			this.$moduleDirectory = moduleDirectory;

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

			for (int i = 0; i < length; i++)
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

					continue;

				}

				ModuleDescriptor moduleDescriptor = null;
				try
				{
					moduleDescriptor = ECGParser.parseAsModuleDescriptor(modulePropertyFile);
				}
				catch (ClassLoadingException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}
				catch (MicroSensorDataTypeException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}
				catch (PropertyException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}
				catch (SAXException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}
				catch (ModuleSetupLoadException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}

				if (moduleDescriptor == null)
				{
					continue;
				}

				if (this.availableModuleClassesMap.containsKey(moduleDescriptor.getId()))
				{
					getLogger().log(Level.SEVERE, "A module with the id " + moduleDescriptor.getId() + " is allready loaded.");

					continue;
				}

				// put the ModuleDescriptor into the HashMap
				this.availableModuleClassesMap.put(moduleDescriptor.getId(), moduleDescriptor);
											
				getLogger().log(Level.INFO, "Loaded additional module class with id: " + moduleDescriptor.getId() + " " + moduleDescriptor.getClazz().getName());
				
				getLogger().log(Level.FINEST, "Loaded additional module class with id: " + moduleDescriptor.getId() + " " + moduleDescriptor.getClazz().getName());

				notifyOfNewModuleDecriptor(moduleDescriptor);

			}
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
	public int createRunningModule(String id, String name) throws ModuleInstantiationException, ModuleClassException
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

			Object o = constructors[0].newInstance(args);

			if (!(o instanceof Module))
			{
				throw new ModuleInstantiationException("");
			}

			Module module = (Module) o;

			return module.getId();

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

	/**
	 * @see org.electrocodeogram.module.registry.ISystemModuleRegistry#storeModuleSetup(java.io.File)
	 */
	public void storeModuleSetup(File file) throws ModuleSetupStoreException
	{
		this.getLogger().entering(this.getClass().getName(),"storeModuleSetup");
		
		if (file == null)
		{
			this.getLogger().log(Level.SEVERE,"The given file is null");
			
			throw new ModuleSetupStoreException("The given file is null");
		}

		this.getLogger().log(Level.INFO,"Storing module setup in file " + file.getAbsolutePath());
		
		if (this.runningModules.runningModuleMap == null)
		{
			this.logger.log(Level.WARNING, "No modules are currently running.");

			throw new ModuleSetupStoreException(
					"No modules are currently running.");

		}

		PrintWriter writer = null;

		try
		{
			writer = new PrintWriter(new FileOutputStream(file));
		}
		catch (FileNotFoundException e)
		{
			this.logger.log(Level.SEVERE, "The given file " + file.getAbsolutePath() + "could not be found.");

			throw new ModuleSetupStoreException(
					"The given file " + file.getAbsolutePath() + "could not be found.");
		}

		writer.flush();

		Module[] modules = this.runningModules.runningModuleMap.values().toArray(new Module[0]);

		this.logger.log(Level.INFO,"Found " + modules.length + " module(s) to store");
		
		writer.println("<?xml version=\"1.0\"?>");

		writer.println("<modulesetup>");

		for (Module module : modules)
		{
			this.logger.log(Level.INFO, "Storing module " + module.getName());

			writer.println("<module id=\"" + module.getId() + "\" active=\"" + module.isActive() + "\">");

			writer.println("<name>");

			writer.println(module.getName());

			writer.println("</name>");

			writer.println("<fromClassId>");

			writer.println(module.getClassId());

			writer.println("</fromClassId>");

			writer.println("<connectedTo>");

			if (module.getReceivingModuleCount() > 0)
			{
								
				Module[] receivingModules = module.getReceivingModules();

				this.logger.log(Level.INFO,"Module " + module.getName() + " is connected to " + receivingModules.length + " other modules.");
				
				for (Module receivingModule : receivingModules)
				{
					writer.println("<id>");

					writer.println(receivingModule.getId());

					writer.println("</id>");

					this.logger.log(Level.INFO,"Connection to module " + receivingModule.getId() + " stored.");
				}

			}
			else
			{
				this.logger.log(Level.INFO,"Module " + module.getName() + " is not connected to other modules.");
				
			}
			writer.println("</connectedTo>");

			writer.println("<properties>");

			
				ModuleProperty[] moduleProperties = module.getRuntimeProperties();

				if (moduleProperties != null && moduleProperties.length > 0)
				{
					
					this.logger.log(Level.INFO,"Found " + moduleProperties.length + " properties for module " + module.getName());
					
					for (ModuleProperty moduleProperty : moduleProperties)
					{
						String propertyName = moduleProperty.getName();

						this.logger.log(Level.INFO,"Property name " + moduleProperty.getName());
						
						String propertyValue = moduleProperty.getValue();

						this.logger.log(Level.INFO,"Property value " + propertyValue);
						
						String propertyType = moduleProperty.getType().getName();
						
						this.logger.log(Level.INFO,"Property type " + propertyType);
						
						if (propertyValue != null)
						{
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
							
							this.logger.log(Level.INFO,"Property value was stored");
						}
						else
						{
							this.logger.log(Level.INFO,"Property value is null and not stored");
						}

					}

				}
				else
				{
					this.logger.log(Level.INFO,"Did not find any properties for module " + module.getName());
				}
			
			

			writer.println("</properties>");

			writer.println("</module>");

			writer.flush();

		}

		writer.println("</modulesetup>");

		writer.flush();

		writer.close();

	}

	/**
	 * @see org.electrocodeogram.module.registry.ISystemModuleRegistry#loadModuleSetup(java.io.File)
	 */
	public void loadModuleSetup(File file) throws ModuleSetupLoadException
	{
		HashMap<Integer, Integer> moduleIdTransformationMap = new HashMap<Integer, Integer>();

		HashMap<Integer, Integer[]> moduleConnectionMap = new HashMap<Integer, Integer[]>();
		
		ArrayList<Integer> moduleActivationList = new ArrayList<Integer>();

		if (!file.exists())
		{
			this.logger.log(Level.SEVERE, "The file " + file.getAbsolutePath() + " does not exist.");

			throw new ModuleSetupLoadException(
					"The file " + file.getAbsolutePath() + " does not exist.");
		}

		if (file.isDirectory())
		{
			this.logger.log(Level.SEVERE, file.getAbsolutePath() + " is a directory, not a file.");

			throw new ModuleSetupLoadException(
					file.getAbsolutePath() + " is a directory, not a file.");
		}

		if (!file.canRead())
		{
			this.logger.log(Level.SEVERE, "The file " + file.getAbsolutePath() + " can not be read.");

			throw new ModuleSetupLoadException(
					"The file " + file.getAbsolutePath() + " can not be read.");
		}

		clearLab();

		ModuleSetup moduleSetup;

		try
		{
			moduleSetup = ECGParser.parseAsModuleSetup(file);

			ModuleConfiguration[] moduleConfigurations = moduleSetup.getModuleConfigurations();

			for (ModuleConfiguration moduleConfiguration : moduleConfigurations)
			{

				
				
				int assignedModuleId = createRunningModule(moduleConfiguration.getFromClassId(), moduleConfiguration.getModuleName());

				ModuleProperty[] moduleProperties = moduleConfiguration.getModuleProperties();

				if (moduleProperties != null)
				{
					for (ModuleProperty moduleProperty : moduleProperties)
					{
						if(moduleProperty.getType().equals(Class.forName("java.lang.reflect.Method")))
						{
							continue;
						}
						
						getRunningModule(assignedModuleId).setProperty(moduleProperty.getName(), moduleProperty.getValue());

					}
				}

				if(moduleConfiguration.isActive())
				{
					moduleActivationList.add(new Integer(assignedModuleId));
				}
				
				if(moduleIdTransformationMap.containsKey(new Integer(moduleConfiguration.getModuleId())))
				{
					throw new ModuleSetupLoadException("Duplicate module id found " + moduleConfiguration.getModuleId());
				}
				
				moduleIdTransformationMap.put(moduleConfiguration.getModuleId(), new Integer(
						assignedModuleId));

				if (moduleConfiguration.getConnectedTo() != null)
				{
					moduleConnectionMap.put(moduleConfiguration.getModuleId(), moduleConfiguration.getConnectedTo());
				}
			}

			Integer[] storedModuleIds = moduleConnectionMap.keySet().toArray(new Integer[0]);

			for (Integer storedModuleId : storedModuleIds)
			{
				Integer assignedModuleId = moduleIdTransformationMap.get(storedModuleId);

				Integer[] storedReceivingModuleIds = moduleConnectionMap.get(storedModuleId);

				for (Integer storedReceivingModuleId : storedReceivingModuleIds)
				{
					if (storedReceivingModuleId == null)
					{
						continue;
					}

					Integer assignedReceivingModuleId = moduleIdTransformationMap.get(storedReceivingModuleId);

					if(assignedReceivingModuleId == null)
					{
						throw new ModuleSetupLoadException("An unknown connected to id was found for module id: " + storedModuleId);
					}
					Module module = getRunningModule(assignedModuleId.intValue());

					Module receivingModule = getRunningModule(assignedReceivingModuleId.intValue());

					module.connectReceiverModule(receivingModule);

				}
			}

			for(Integer moduleId : moduleActivationList)
			{
				Module module = this.getRunningModule(moduleId.intValue());
				
				if(module.isModuleType(ModuleType.TARGET_MODULE))
				{
					try
					{
						module.activate();
					}
					catch (ModuleActivationException e)
					{
						clearLab();
						
						throw new ModuleSetupLoadException(e.getMessage());
					}
				}
			}
			
			for(Integer moduleId : moduleActivationList)
			{
				Module module = this.getRunningModule(moduleId.intValue());
				
				if(module.isModuleType(ModuleType.INTERMEDIATE_MODULE))
				{
					try
					{
						module.activate();
					}
					catch (ModuleActivationException e)
					{
						clearLab();
						
						throw new ModuleSetupLoadException(e.getMessage());
					}
				}
			}
			
			for(Integer moduleId : moduleActivationList)
			{
				Module module = this.getRunningModule(moduleId.intValue());
				
				if(module.isModuleType(ModuleType.SOURCE_MODULE))
				{
					try
					{
						module.activate();
					}
					catch (ModuleActivationException e)
					{
						clearLab();
						
						throw new ModuleSetupLoadException(e.getMessage());
					}
				}
			}
			
		}
		catch (SAXException e)
		{
			throw new ModuleSetupLoadException(e.getMessage());
		}
		catch (IOException e)
		{
			throw new ModuleSetupLoadException(e.getMessage());
		}
		catch (ModuleSetupLoadException e)
		{
			throw new ModuleSetupLoadException(e.getMessage());
		}
		catch (ModuleInstanceException e)
		{
			throw new ModuleSetupLoadException(e.getMessage());
		}
		catch (ModuleConnectionException e)
		{
			throw new ModuleSetupLoadException(e.getMessage());
		}
		catch (ModuleInstantiationException e)
		{
			throw new ModuleSetupLoadException(e.getMessage());
		}
		catch (ModuleClassException e)
		{
			throw new ModuleSetupLoadException(e.getMessage());
		}
		catch (ModulePropertyException e)
		{
			throw new ModuleSetupLoadException(e.getMessage());
		}
		catch (PropertyException e)
		{
			throw new ModuleSetupLoadException(e.getMessage());
		}
		catch (ClassNotFoundException e)
		{
			throw new ModuleSetupLoadException(e.getMessage());
		}
		catch (ClassLoadingException e)
		{
			throw new ModuleSetupLoadException(e.getMessage());
		}

	}

	/**
	 * 
	 */
	private void clearLab()
	{
		Module[] modules = this.runningModules.runningModuleMap.values().toArray(new Module[0]);

		for (Module module : modules)
		{
			try
			{
				deregisterRunningModule(module.getId());
			}
			catch (ModuleInstanceException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		this.logger.log(Level.INFO, "All running modules are deregistered. ECG Lab is cleared.");
	}
}
