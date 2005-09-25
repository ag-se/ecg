package org.electrocodeogram.system;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;

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
import org.electrocodeogram.logging.LogHelper;
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

	private static SystemRoot _theInstance;

	private ModuleRegistry _moduleRegistry;

	private Gui _gui;

	private MsdtRegistry _mstdRegistry;

	private SystemRoot()
	{

		this._mstdRegistry = new MsdtRegistry();

		this._moduleRegistry = new ModuleRegistry();

		_theInstance = this;

	}

	private SystemRoot(String moduleDir, String moduleSetup, boolean nogui) throws ModuleSetupLoadException, ModuleClassLoaderInitializationException
	{

		this();

		if (!nogui)
		{
			this._gui = new Gui(this._moduleRegistry);
		}
		else
		{
			Thread workerThread = new Thread(new Runnable()
			{

				public void run()
				{
					while (true)
					{
						try
						{
							synchronized (this)
							{
								wait();
							}

						}
						catch (InterruptedException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}
			});

			workerThread.start();
		}

		if(moduleDir == null)
		{
			if (this._moduleRegistry == null)
			{
				this._moduleRegistry = new ModuleRegistry(new File("modules"));
			}
			else
			{
				this._moduleRegistry.setModuleDirectory(new File("modules"));
			}
		}
		else 
		{
			if (this._moduleRegistry == null)
			{
				this._moduleRegistry = new ModuleRegistry(new File(moduleDir));
			}
			else
			{
				this._moduleRegistry.setModuleDirectory(new File(moduleDir));
			}
		}
		if (moduleSetup != null)
		{
			this._moduleRegistry.loadModuleSetup(new File(moduleSetup));
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
		if (_theInstance == null)
		{
			_theInstance = new SystemRoot();
		}

		return _theInstance;
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
		if (_theInstance == null)
		{
			_theInstance = new SystemRoot();
		}

		return _theInstance;
	}

	/**
	 * @see org.electrocodeogram.system.ISystemRoot#getSystemMsdtRegistry()
	 */
	public ISystemMsdtRegistry getSystemMsdtRegistry()
	{
		return this._mstdRegistry;
	}

	/**
	 * @see org.electrocodeogram.moduleapi.system.IModuleSystemRoot#getModuleModuleRegistry()
	 */
	public IModuleModuleRegistry getModuleModuleRegistry()
	{
		return this._moduleRegistry;
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
		return this._gui;
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
	 * @see org.electrocodeogram.system.ISystemRoot#addSystemObserver(java.util.Observer)
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

		Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());
		
		String moduleDir = getModuleDir(args);

		String moduleSetup = getModuleSetup(args);

		Level logLevel = getLogLevel(args);
		
		String logFile = getLogFile(args);
		
		boolean nogui = isNogui(args);

		try
		{
			startSystem(moduleDir,moduleSetup,nogui,logLevel,logFile);
		}
		catch (ModuleSetupLoadException e)
		{
			System.out.println("A module could not be loaded");
			
			System.out.println(e.getMessage());
		}
		catch (ModuleClassLoaderInitializationException e)
		{
			System.out.println("The module loader could not be initialized");
			
			System.out.println(e.getMessage());
		}
	}

	private static void startSystem(String moduleDir, String moduleSetup, boolean nogui, Level logLevel, String logFile) throws ModuleSetupLoadException, ModuleClassLoaderInitializationException
	{
		
		LogHelper.setLogLevel(logLevel);
		
		try
		{
			LogHelper.setLogFile(logFile);
		}
		catch (SecurityException e)
		{
			System.err.println("Unable to set the lof file " + logFile);
		}
		catch (IOException e)
		{
			System.err.println("Unable to set the lof file " + logFile);
		}
		
		new SystemRoot(moduleDir,moduleSetup,nogui);
			
	}
	
	private static String getModuleDir(String[] args)
	{
		if(args == null || args.length == 0)
		{
			return null;
		}
		for(int i=0;i<args.length;i++)
		{
			if(args[i].equals("-m"))
			{
				if(args.length-1 < i+1)
				{
					printHelpMessage();
				}
				if(args[i+1] == null || args[i+1].equals("") || args[i+1].equals("-s") || args[i+1].equals("-l") || args[i+1].equals("--log-file") || args[i+1].equals("-nogui"))
				{
					printHelpMessage();
				}
				else
				{
					return args[i+1];
				}
			}
		}
		return null;
	}

	private static boolean isNogui(String[] args)
	{
		if(args == null || args.length == 0)
		{
			return false;
		}
		for(int i=0;i<args.length;i++)
		{
			if(args[i].equals("-nogui"))
			{
				return true;
			}
		}
		return false;
	}
	
	private static String getModuleSetup(String[] args)
	{
		
		if(args == null || args.length == 0)
		{
			return null;
		}
		for(int i=0;i<args.length;i++)
		{
			if(args[i].equals("-s"))
			{
				if(args.length-1 < i+1)
				{
					printHelpMessage();
				}
				if(args[i+1] == null || args[i+1].equals("") || args[i+1].equals("-m") || args[i+1].equals("-l") || args[i+1].equals("--log-file") || args[i+1].equals("-nogui"))
				{
					printHelpMessage();
				}
				else
				{
					return args[i+1];
				}
			}
		}
		return null;
	}
	
	private static String getLogFile(String[] args)
	{
		if(args == null || args.length == 0)
		{
			return null;
		}
		for(int i=0;i<args.length;i++)
		{
			if(args[i].equals("--log-file"))
			{
				if(args.length-1 < i+1)
				{
					printHelpMessage();
				}
				if(args[i+1] == null || args[i+1].equals("") || args[i+1].equals("-m") || args[i+1].equals("-l") || args[i+1].equals("-s") || args[i+1].equals("-nogui"))
				{
					printHelpMessage();
				}
				else
				{
					return args[i+1];
				}
			}
		}
		return null;
	}
	
	private static Level getLogLevel(String[] args)
	{
		if(args == null || args.length == 0)
		{
			return null;
		}
		for(int i=0;i<args.length;i++)
		{
			if(args[i].equals("--log-level"))
			{
				if(args.length-1 < i+1)
				{
					printHelpMessage();
				}
				if(args[i+1] == null || args[i+1].equals("") || args[i+1].equals("-m") || args[i+1].equals("-l") || args[i+1].equals("--log-file") || args[i+1].equals("-s") || args[i+1].equals("-nogui"))
				{
					printHelpMessage();
				}
				else
				{
					if(args[i+1].equalsIgnoreCase("off"))
					{
						return Level.OFF;
					}
					else if(args[i+1].equalsIgnoreCase("error"))
					{
						return Level.SEVERE;
					}
					else if(args[i+1].equalsIgnoreCase("warning"))
					{
						return Level.WARNING;
					}
					else if(args[i+1].equalsIgnoreCase("info"))
					{
						return Level.INFO;
					}
					else if(args[i+1].equalsIgnoreCase("debug"))
					{
						return Level.FINEST;
					}
					else
					{
						return null;
					}
				}
			}
		}
		return null;
	}
	
	private static void printHelpMessage()
	{
		System.out.println("Usage: java -jar ECGLab.jar <options>\n");

		System.out.println("Where options are:\n");

		System.out.println("-m <moduleDir>\t\t\t\t\tSets the module directory to moduleDir.\n");

		System.out.println("-s <moduleSetupFile>\t\t\t\tIs the file containing the module setup to load.\n");

		System.out.println("--log-level [off | error | warning | info | debug ]\tSets the log level.\n");

		System.out.println("--log-file <logFile>\t\t\t\tIs the logfile to use. If no logfile is given, logging goes to standard out.\n");

		System.out.println("-nogui\t\t\t\t\t\tTells the ECG Lab to start without graphical user interface (for inline server mode).\n");

		System.exit(-1);
	}

	/**
	 * @see org.electrocodeogram.system.ISystemRoot#getSystemModuleRegistry()
	 */
	public ISystemModuleRegistry getSystemModuleRegistry()
	{
		return this._moduleRegistry;
	}

	/**
	 * @see org.electrocodeogram.moduleapi.system.IModuleSystemRoot#getRootFrame()
	 */
	public JFrame getRootFrame()
	{
		return this._gui;
	}

	/**
	 * @see org.electrocodeogram.moduleapi.system.IModuleSystemRoot#getModuleMsdtRegistry()
	 */
	public IModuleMsdtRegistry getModuleMsdtRegistry()
	{
		return this._mstdRegistry;
	}

	private static class DefaultExceptionHandler implements UncaughtExceptionHandler
	{
		public void uncaughtException(Thread t, Throwable e)
		{
			System.out.println("An uncaught Exception had occured:");
			
			System.out.println("Thread:" + t.getName());
			
			System.out.println("Class: " + t.getClass());
			
			System.out.println("State: " + t.getState());
			
			System.out.println("Message: " + e.getMessage());
			
			System.out.println("StackTrace: ");
			
			e.printStackTrace();
					
		}

	}
}
