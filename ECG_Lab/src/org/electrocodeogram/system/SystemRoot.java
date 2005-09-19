package org.electrocodeogram.system;

import java.io.File;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JFrame;
import org.electrocodeogram.module.classloader.ModuleClassLoaderInitializationException;
import org.electrocodeogram.module.registry.ISystemModuleRegistry;
import org.electrocodeogram.module.registry.ModuleRegistry;
import org.electrocodeogram.module.registry.ModuleSetupLoadException;
import org.electrocodeogram.moduleapi.module.registry.IModuleModuleRegistry;
import org.electrocodeogram.moduleapi.msdt.registry.IModuleMsdtRegistry;
import org.electrocodeogram.moduleapi.system.IModuleSystemRoot;
import org.electrocodeogram.msdt.registry.ISystemMsdtRegistry;
import org.electrocodeogram.msdt.registry.MsdtRegistry;
import org.electrocodeogram.ui.Gui;
import org.electrocodeogram.ui.IGui;

/**
 * This is the singleton root and main class of the ECG Lab application. It
 * implements the interfaces ISystemRoot, which is the way other ECG system
 * compoennts are seeing it and IModuleSystemRoot, which is the way ECG modules
 * are looking at it. During its initialization it creates all ECG system
 * components. It also provides a console for logging output and reading input
 * commands. SystemRoot is implemented as a singleton class that provides global
 * centralized access to all ECG system components and ECG modules.
 */
public class SystemRoot extends Observable implements ISystemRoot, IModuleSystemRoot
{

	private static SystemRoot theInstance;

	private ModuleRegistry moduleRegistry;

	private Gui gui;

	private MsdtRegistry mstdRegistry;

	private SystemRoot()
	{

		this.mstdRegistry = new MsdtRegistry();

		this.moduleRegistry = new ModuleRegistry();

		theInstance = this;

	}

	private SystemRoot(File moduleDir, File moduleSetup, boolean enableGui) throws ModuleSetupLoadException, ModuleClassLoaderInitializationException
	{

		this();

		if (enableGui)
		{
			this.gui = new Gui(this.moduleRegistry);
		}
		else
		{
			Thread workerThread = new Thread(new Runnable() {

				public void run()
				{
					while(true)
					{
						try
						{
							wait();
						}
						catch (InterruptedException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}});
			
			workerThread.start();
			
		}

		if (this.moduleRegistry == null)
		{
			this.moduleRegistry = new ModuleRegistry(moduleDir);
		}
		else
		{
			this.moduleRegistry.setFile(moduleDir);
		}

		if (moduleSetup != null)
		{
			this.moduleRegistry.loadModuleSetup(moduleSetup);
		}

	}

	/**
	 * Returns the singleton instance of a class that implements thy ISystemRoot
	 * interface. This reference is used to access ECG system components by ECG
	 * system components.
	 * 
	 * @return The singleton instance of an ISystemRoot object
	 */
	public static ISystemRoot getSystemInstance()
	{
		if (theInstance == null)
		{
			theInstance = new SystemRoot();
		}

		return theInstance;
	}

	/**
	 * Returns the singleton instance of a class that implements thy
	 * IModuleSystemRoot interface. This reference is used to access ECG system
	 * components by ECG modules.
	 * 
	 * @return The singleton instance of an ISystemRoot object
	 */
	public static IModuleSystemRoot getModuleInstance()
	{
		if (theInstance == null)
		{
			theInstance = new SystemRoot();
		}

		return theInstance;
	}

	/**
	 * @see org.electrocodeogram.system.ISystemRoot#getSystemMsdtRegistry()
	 */
	public ISystemMsdtRegistry getSystemMsdtRegistry()
	{
		return this.mstdRegistry;
	}

	/**
	 * @see org.electrocodeogram.moduleapi.system.IModuleSystemRoot#getModuleModuleRegistry()
	 */
	public IModuleModuleRegistry getModuleModuleRegistry()
	{
		return this.moduleRegistry;
	}

	/**
	 * This method quits the ECG Server & Lab application.
	 */
	public void quit()
	{
		System.exit(0);
	}

	/**
	 * @see org.electrocodeogram.system.ISystemRoot#getGui()
	 */
	public IGui getGui()
	{
		return this.gui;
	}

	/**
	 * @see org.electrocodeogram.system.ISystemRoot#fireStateChange()
	 */
	public void fireStateChange()
	{
		this.setChanged();

		this.notifyObservers();

		this.clearChanged();

	}

	/**
	 * @see org.electrocodeogram.system.ISystemRoot#addSystemObserver(SystemObserver)
	 */
	public void addSystemObserver(Observer o)
	{
		this.addObserver(o);

	}

	/**
	 * @see org.electrocodeogram.system.ISystemRoot#deleteSystemObserver(Observer)
	 */
	public void deleteSystemObserver(Observer o)
	{
		this.deleteObserver(o);

	}

	/**
	 * This method starts the ECG Server & Lab application.
	 * 
	 * @param args
	 *            If a String parameter is given, it is taken to be the
	 *            mdoule-directory path.
	 */
	public static void main(String[] args)
	{

		String moduleDir = null;

		String moduleSetup = null;

		String nogui = null;
		try
		{
			if (args == null || args.length == 0)
			{

				new SystemRoot(new File("modules"), null, true);
			}

			else if (args.length == 1)
			{
				if (args[0].equals("-nogui"))
				{
					new SystemRoot(new File("modules"), null, false);
				}
				else
				{
					printHelpMessage();
				}
			}
			else
			{
				for (int i = 0; i < args.length; i++)
				{
					String arg = args[i];

					if (arg.equals("-m") && moduleDir == null)
					{
						if(i+1 > args.length-1)
						{
							printHelpMessage();
						}
						
						if (args[i + 1] == null)
						{
							printHelpMessage();

						}

						if (args[i + 1].equals(""))
						{
							printHelpMessage();

						}

						if (args[i + 1].equals("-s"))
						{
							printHelpMessage();

						}

						if (args[i + 1].equals("-nogui"))
						{
							printHelpMessage();

						}

						moduleDir = args[i + 1];

						i++;

					}
					else if (arg.equals("-s") && moduleSetup == null)
					{
						if(i+1 > args.length-1)
						{
							printHelpMessage();
						}
						
						if (args[i + 1] == null)
						{
							printHelpMessage();

						}

						if (args[i + 1].equals(""))
						{
							printHelpMessage();

						}

						if (args[i + 1].equals("-m"))
						{
							printHelpMessage();

						}

						if (args[i + 1].equals("-nogui"))
						{
							printHelpMessage();

						}

						moduleSetup = args[i + 1];

						i++;
					}
					else if (args[i].equals("-nogui"))
					{
						nogui = "nogui";
					}
					else
					{
						printHelpMessage();

					}
				}

				if (moduleDir == null)
				{
					if (moduleSetup == null)
					{
						if (nogui == null)
						{
							new SystemRoot(new File("modules"), null, true);
						}
						{
							new SystemRoot(new File("modules"), null, false);
						}
					}
					else
					{
						if (nogui == null)
						{
							new SystemRoot(new File("modules"), new File(
									moduleSetup), true);
						}
						{
							new SystemRoot(new File("modules"), new File(
									moduleSetup), false);
						}
					}
				}
				else
				{
					if (moduleSetup == null)
					{
						if (nogui == null)
						{
							new SystemRoot(new File(moduleDir), null, true);
						}
						{
							new SystemRoot(new File(moduleDir), null, false);
						}
					}
					else
					{
						if (nogui == null)
						{
							new SystemRoot(new File(moduleDir), new File(
									moduleSetup), true);
						}
						{
							new SystemRoot(new File(moduleDir), new File(
									moduleSetup), false);
						}
					}
				}
			}
		}
		catch (ModuleSetupLoadException e)
		{
			System.out.println("Error while reading the module directory: " + moduleDir);

			System.out.println(e.getMessage());

			printHelpMessage();
		}
		catch (ModuleClassLoaderInitializationException e)
		{
			System.out.println("Error while reading the module setup: " + moduleSetup);

			System.out.println(e.getMessage());

			printHelpMessage();
		}
	}

	private static void printHelpMessage()
	{
		System.out.println("Usage: java -jar ECGLab.jar [-m <moduleDirectory>] | [-s <moduleSetup>] | [-nogui]");

		System.exit(-1);
	}

	/**
	 * @see org.electrocodeogram.system.ISystemRoot#getSystemModuleRegistry()
	 */
	public ISystemModuleRegistry getSystemModuleRegistry()
	{
		return this.moduleRegistry;
	}

	/**
	 * @see org.electrocodeogram.moduleapi.system.IModuleSystemRoot#getRootFrame()
	 */
	public JFrame getRootFrame()
	{
		return this.gui;
	}

	/**
	 * @see org.electrocodeogram.moduleapi.system.IModuleSystemRoot#getModuleMsdtRegistry()
	 */
	public IModuleMsdtRegistry getModuleMsdtRegistry()
	{
		return this.mstdRegistry;
	}
}
