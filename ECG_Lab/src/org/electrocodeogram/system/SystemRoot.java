package org.electrocodeogram.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Observable;

import javax.swing.JFrame;

import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.registry.ISystemModuleRegistry;
import org.electrocodeogram.module.registry.ModuleRegistry;
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

		Console console = new Console();

		this.mstdRegistry = new MsdtRegistry();

		this.moduleRegistry = new ModuleRegistry();

		this.gui = new Gui(this.moduleRegistry);

		theInstance = this;

		console.start();
	}

	private SystemRoot(File file)
	{

		this();

		if (this.moduleRegistry == null)
		{
			this.moduleRegistry = new ModuleRegistry(file);
		}
		else
		{
			this.moduleRegistry.setFile(file);
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

	private static class Console extends Thread
	{

		private BufferedReader bufferedReader = null;

		/**
		 * Creates the console to manage the ECG Server & Lab.
		 * 
		 */
		public Console()
		{
			System.out.println("ElectroCodeoGram Server & Lab is starting...");

			this.bufferedReader = new BufferedReader(new InputStreamReader(
					System.in));
		}

		/**
		 * Here the reading of the console-input is done.
		 */
		@Override
		public void run()
		{

			while (true)
			{

				System.out.println(">>");

				String inputString = "" + this.readLine();

				System.out.println("Echo: " + inputString);

				if (inputString.equalsIgnoreCase("quit"))
				{
					this.quit();
					return;
				}
			}
		}

		private String readLine()
		{
			try
			{
				return this.bufferedReader.readLine();
			}
			catch (IOException e)
			{
				return "quit";
			}
		}

		private void quit()
		{
			SystemRoot.getSystemInstance().quit();

		}
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
	 * @see org.electrocodeogram.system.ISystemRoot#addModule(org.electrocodeogram.module.Module)
	 */
	public void addModule(Module module)
	{
		this.addObserver(module);

	}

	/**
	 * @see org.electrocodeogram.system.ISystemRoot#deleteModule(org.electrocodeogram.module.Module)
	 */
	public void deleteModule(Module module)
	{
		this.deleteObserver(module);

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

		File file = null;

		if (args != null && args.length > 0)
		{

			file = new File(args[0]);

			if (file.exists() && file.isDirectory())
			{
				new SystemRoot(file);
			}
			else
			{
				new SystemRoot();
			}

		}
		else
		{
			new SystemRoot();
		}

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
