package org.electrocodeogram.system;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public final class SystemRoot extends Observable implements ISystemRoot, IModuleSystemRoot
{

	/**
	 * 
	 */
	private static final String DEFAULT_MODULE_DIRECTORY = "modules";

	static Logger _logger = LogHelper.createLogger(SystemRoot.class.getName());

	private static SystemRoot _theInstance;

	private ModuleRegistry _moduleRegistry;

	private Gui _gui;

	private MsdtRegistry _mstdRegistry;

	private SystemRoot()
	{

		_logger.entering(this.getClass().getName(), "SystemRoot");

		this._mstdRegistry = new MsdtRegistry();

		this._moduleRegistry = new ModuleRegistry();

		_theInstance = this;

		_logger.exiting(this.getClass().getName(), "SystemRoot");

	}

	private SystemRoot(String moduleDir, String moduleSetup, boolean nogui) throws ModuleSetupLoadException, ModuleClassLoaderInitializationException
	{

		this();

		_logger.entering(this.getClass().getName(), "SystemRoot");

		if (!nogui)
		{
			_logger.log(Level.INFO, "Starting ECG Lab with GUI.");

			this._gui = new Gui(this._moduleRegistry);
		}
		else
		{
			_logger.log(Level.INFO, "Starting ECG Lab without GUI.");

			Thread workerThread = new WorkerThread();

			workerThread.start();
		}

		if (moduleDir == null)
		{
			_logger.log(Level.INFO, "Starting with default module directory: " + DEFAULT_MODULE_DIRECTORY);

			if (this._moduleRegistry == null)
			{
				this._moduleRegistry = new ModuleRegistry(new File(
						DEFAULT_MODULE_DIRECTORY));
			}
			else
			{
				this._moduleRegistry.setModuleDirectory(new File(
						DEFAULT_MODULE_DIRECTORY));
			}
		}
		else
		{
			_logger.log(Level.INFO, "Starting with module directory: " + moduleDir);

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
			_logger.log(Level.INFO, "Starting with module setup: " + moduleSetup);

			this._moduleRegistry.loadModuleSetup(new File(moduleSetup));
		}

		_logger.exiting(this.getClass().getName(), "SystemRoot");

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
		_logger.entering(SystemRoot.class.getName(), "getSystemInstance");

		if (_theInstance == null)
		{
			_theInstance = new SystemRoot();
		}

		_logger.exiting(SystemRoot.class.getName(), "getSystemInstance");

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
		_logger.entering(SystemRoot.class.getName(), "getModuleInstance");

		if (_theInstance == null)
		{
			_theInstance = new SystemRoot();
		}

		_logger.exiting(SystemRoot.class.getName(), "getModuleInstance");

		return _theInstance;
	}

	/**
	 * @see org.electrocodeogram.system.ISystemRoot#getSystemMsdtRegistry()
	 */
	public ISystemMsdtRegistry getSystemMsdtRegistry()
	{
		_logger.entering(this.getClass().getName(), "getSystemMsdtRegistry");

		_logger.exiting(this.getClass().getName(), "getSystemMsdtRegistry");

		return this._mstdRegistry;
	}

	/**
	 * @see org.electrocodeogram.moduleapi.system.IModuleSystemRoot#getModuleModuleRegistry()
	 */
	public IModuleModuleRegistry getModuleModuleRegistry()
	{
		_logger.entering(this.getClass().getName(), "getModuleModuleRegistry");

		_logger.exiting(this.getClass().getName(), "getModuleModuleRegistry");

		return this._moduleRegistry;
	}

	/**
	 * This method quits the ECG Server & Lab application.
	 */
	public void quit()
	{
		_logger.entering(this.getClass().getName(), "quit");

		System.exit(0);

		_logger.exiting(this.getClass().getName(), "quit");
	}

	/**
	 * @see org.electrocodeogram.system.ISystemRoot#getGui()
	 */
	public IGui getGui()
	{
		_logger.entering(this.getClass().getName(), "getGui");

		_logger.exiting(this.getClass().getName(), "getGui");

		return this._gui;
	}

	public JFrame getFrame()
	{
		_logger.entering(this.getClass().getName(), "getFrame");

		_logger.exiting(this.getClass().getName(), "getFrames");

		return this._gui;
		
	}
	
	/**
	 * @see org.electrocodeogram.system.ISystemRoot#fireStateChange()
	 */
	public void fireStateChange()
	{
		_logger.entering(this.getClass().getName(), "fireStateChange");

		this.setChanged();

		this.notifyObservers();

		this.clearChanged();

		_logger.exiting(this.getClass().getName(), "fireStateChange");

	}

	/**
	 * @see org.electrocodeogram.system.ISystemRoot#addSystemObserver(java.util.Observer)
	 */
	public void addSystemObserver(Observer o)
	{
		_logger.entering(this.getClass().getName(), "addSystemObserver");

		if (o == null)
		{
			_logger.log(Level.WARNING, "Observer is null");

			return;
		}

		this.addObserver(o);

		_logger.exiting(this.getClass().getName(), "addSystemObserver");

	}

	/**
	 * @see org.electrocodeogram.system.ISystemRoot#deleteSystemObserver(Observer)
	 */
	public void deleteSystemObserver(Observer o)
	{
		_logger.entering(this.getClass().getName(), "deleteSystemObserver");

		if (o == null)
		{
			_logger.log(Level.WARNING, "Observer is null");

			return;
		}

		this.deleteObserver(o);

		_logger.exiting(this.getClass().getName(), "deleteSystemObserver");

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

		_logger.entering(SystemRoot.class.getName(), "main");

		Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());

		_logger.log(Level.INFO, "Registered DefaultExceptionHandler");

		String moduleDir = getModuleDir(args);

		String moduleSetup = getModuleSetup(args);

		Level logLevel = getLogLevel(args);

		String logFile = getLogFile(args);

		boolean nogui = isNogui(args);

		try
		{
			startSystem(moduleDir, moduleSetup, nogui, logLevel, logFile);

			_logger.log(Level.INFO, "ECG Lab is starting...");
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

		_logger.exiting(SystemRoot.class.getName(), "main");
	}

	private static void startSystem(String moduleDir, String moduleSetup, boolean nogui, Level logLevel, String logFile) throws ModuleSetupLoadException, ModuleClassLoaderInitializationException
	{

		_logger.entering(SystemRoot.class.getName(), "startSystem");

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

		new SystemRoot(moduleDir, moduleSetup, nogui);

		_logger.exiting(SystemRoot.class.getName(), "startSystem");

	}

	private static String getModuleDir(String[] args)
	{
		_logger.entering(SystemRoot.class.getName(), "getModuleDir");

		if (args == null || args.length == 0)
		{
			_logger.exiting(SystemRoot.class.getName(), "getModuleDir");

			return null;
		}
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-m"))
			{
				if (args.length - 1 < i + 1)
				{
					printHelpMessage();
				}
				if (args[i + 1] == null || args[i + 1].equals("") || args[i + 1].equals("-s") || args[i + 1].equals("-l") || args[i + 1].equals("--log-file") || args[i + 1].equals("-nogui"))
				{
					printHelpMessage();
				}
				else
				{
					_logger.exiting(SystemRoot.class.getName(), "getModuleDir");

					return args[i + 1];
				}
			}
		}

		_logger.exiting(SystemRoot.class.getName(), "getModuleDir");

		return null;
	}

	private static boolean isNogui(String[] args)
	{
		_logger.entering(SystemRoot.class.getName(), "isNogui");

		if (args == null || args.length == 0)
		{
			_logger.exiting(SystemRoot.class.getName(), "isNogui");

			return false;
		}
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-nogui"))
			{
				_logger.exiting(SystemRoot.class.getName(), "isNogui");

				return true;
			}
		}

		_logger.exiting(SystemRoot.class.getName(), "isNogui");

		return false;
	}

	private static String getModuleSetup(String[] args)
	{
		_logger.entering(SystemRoot.class.getName(), "getModuleSetup");

		if (args == null || args.length == 0)
		{
			_logger.exiting(SystemRoot.class.getName(), "getModuleSetup");

			return null;
		}
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-s"))
			{
				if (args.length - 1 < i + 1)
				{
					printHelpMessage();
				}
				if (args[i + 1] == null || args[i + 1].equals("") || args[i + 1].equals("-m") || args[i + 1].equals("-l") || args[i + 1].equals("--log-file") || args[i + 1].equals("-nogui"))
				{
					printHelpMessage();
				}
				else
				{

					_logger.exiting(SystemRoot.class.getName(), "getModuleSetup");

					return args[i + 1];
				}
			}
		}

		_logger.exiting(SystemRoot.class.getName(), "getModuleSetup");

		return null;
	}

	private static String getLogFile(String[] args)
	{
		_logger.entering(SystemRoot.class.getName(), "getLogFile");

		if (args == null || args.length == 0)
		{
			_logger.exiting(SystemRoot.class.getName(), "getLogFile");

			return null;
		}
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("--log-file"))
			{
				if (args.length - 1 < i + 1)
				{
					printHelpMessage();
				}
				if (args[i + 1] == null || args[i + 1].equals("") || args[i + 1].equals("-m") || args[i + 1].equals("-l") || args[i + 1].equals("-s") || args[i + 1].equals("-nogui"))
				{
					printHelpMessage();
				}
				else
				{
					_logger.exiting(SystemRoot.class.getName(), "getLogFile");

					return args[i + 1];
				}
			}
		}
		_logger.exiting(SystemRoot.class.getName(), "getLogFile");

		return null;
	}

	private static Level getLogLevel(String[] args)
	{
		_logger.entering(SystemRoot.class.getName(), "getLogLevel");

		if (args == null || args.length == 0)
		{
			_logger.exiting(SystemRoot.class.getName(), "getLogLevel");

			return null;
		}
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("--log-level"))
			{
				if (args.length - 1 < i + 1)
				{
					printHelpMessage();
				}
				if (args[i + 1] == null || args[i + 1].equals("") || args[i + 1].equals("-m") || args[i + 1].equals("-l") || args[i + 1].equals("--log-file") || args[i + 1].equals("-s") || args[i + 1].equals("-nogui"))
				{
					printHelpMessage();
				}
				else
				{
					if (args[i + 1].equalsIgnoreCase("off"))
					{
						_logger.exiting(SystemRoot.class.getName(), "getLogLevel");

						return Level.OFF;
					}
					else if (args[i + 1].equalsIgnoreCase("error"))
					{
						_logger.exiting(SystemRoot.class.getName(), "getLogLevel");

						return Level.SEVERE;
					}
					else if (args[i + 1].equalsIgnoreCase("warning"))
					{
						_logger.exiting(SystemRoot.class.getName(), "getLogLevel");

						return Level.WARNING;
					}
					else if (args[i + 1].equalsIgnoreCase("info"))
					{
						_logger.exiting(SystemRoot.class.getName(), "getLogLevel");

						return Level.INFO;
					}
					else if (args[i + 1].equalsIgnoreCase("debug"))
					{
						_logger.exiting(SystemRoot.class.getName(), "getLogLevel");

						return Level.FINEST;
					}
					else
					{
						_logger.exiting(SystemRoot.class.getName(), "getLogLevel");

						return null;
					}
				}
			}
		}
		_logger.exiting(SystemRoot.class.getName(), "getLogLevel");

		return null;
	}

	private static void printHelpMessage()
	{

		_logger.entering(SystemRoot.class.getName(), "printHelpMessage");

		System.out.println("Usage: java -jar ECGLab.jar <options>\n");

		System.out.println("Where options are:\n");

		System.out.println("-m <moduleDir>\t\t\t\t\tSets the module directory to moduleDir.\n");

		System.out.println("-s <moduleSetupFile>\t\t\t\tIs the file containing the module setup to load.\n");

		System.out.println("--log-level [off | error | warning | info | debug ]\tSets the log level.\n");

		System.out.println("--log-file <logFile>\t\t\t\tIs the logfile to use. If no logfile is given, logging goes to standard out.\n");

		System.out.println("-nogui\t\t\t\t\t\tTells the ECG Lab to start without graphical user interface (for inline server mode).\n");

		System.exit(-1);

		_logger.exiting(SystemRoot.class.getName(), "printHelpMessage");
	}

	/**
	 * @see org.electrocodeogram.system.ISystemRoot#getSystemModuleRegistry()
	 */
	public ISystemModuleRegistry getSystemModuleRegistry()
	{
		_logger.entering(SystemRoot.class.getName(), "getSystemModuleRegistry");

		_logger.exiting(SystemRoot.class.getName(), "getSystemModuleRegistry");

		return this._moduleRegistry;
	}

	/**
	 * @see org.electrocodeogram.moduleapi.system.IModuleSystemRoot#getRootFrame()
	 */
	public JFrame getRootFrame()
	{
		_logger.entering(SystemRoot.class.getName(), "getRootFrame");

		_logger.exiting(SystemRoot.class.getName(), "getRootFrame");

		return this._gui;
	}

	/**
	 * @see org.electrocodeogram.moduleapi.system.IModuleSystemRoot#getModuleMsdtRegistry()
	 */
	public IModuleMsdtRegistry getModuleMsdtRegistry()
	{
		_logger.entering(SystemRoot.class.getName(), "getModuleMsdtRegistry");

		_logger.exiting(SystemRoot.class.getName(), "getModuleMsdtRegistry");

		return this._mstdRegistry;
	}

	private static class DefaultExceptionHandler implements UncaughtExceptionHandler
	{
		public void uncaughtException(Thread t, Throwable e)
		{
			_logger.entering(SystemRoot.class.getName(), "uncaughtException");

			System.out.println("An uncaught Exception had occured:");

			System.out.println("Thread:" + t.getName());

			System.out.println("Class: " + t.getClass());

			System.out.println("State: " + t.getState());

			System.out.println("Message: " + e.getMessage());

			System.out.println("StackTrace: ");

			e.printStackTrace();

			_logger.exiting(SystemRoot.class.getName(), "uncaughtException");

		}

	}
	
	private static class WorkerThread extends Thread
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
					_logger.log(Level.WARNING, "The SystemRoot's WorkerThread has been interrupted.");
				}
			}

		}
	}
}
