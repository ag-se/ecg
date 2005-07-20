package org.electrocodeogram.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.module.ModuleRegistry;
import org.electrocodeogram.module.source.SocketSourceModule;
import org.electrocodeogram.msdt.MsdtManager;
import org.electrocodeogram.ui.Configurator;
import org.electrocodeogram.ui.messages.GuiEventWriter;

/**
 * This is the root and main class of the ECG Server & Lab component. During its
 * initilisation it creates all other core ECG components. It alos provides
 * a console for logging output and reading input commands.
 * It is implemented as a singleton class that gives centralized access to
 * all components.
 */
public class Core {

	private static Core theInstance = null;

	private ModuleRegistry moduleRegistry = null;

	private SensorShellWrapper sensorShellWrapper = null;

	private Configurator configurator = null;

	private SocketSourceModule sensorSource = null;
	
	private GuiEventWriter guiEventWriter = null;
	
	private MsdtManager mstdManager = null;

	private Logger logger = null;
	
	
	private Core() {
		
		this.logger = Logger.getLogger("Core");
		
		Console gob = new Console();
		
		try {
			
			this.mstdManager = new MsdtManager();
			
		} catch (FileNotFoundException e) {
			
			this.logger.log(Level.SEVERE,e.getMessage());
			
		}
		
		this.configurator = new Configurator();

		this.moduleRegistry = new ModuleRegistry(this);

		this.sensorShellWrapper = new SensorShellWrapper(this);

		this.guiEventWriter = new GuiEventWriter(this);
		
		this.sensorSource = new SocketSourceModule(this);

		gob.start();
		
		theInstance = this;
	}

	private Core(File file) {

		this();

		if (this.moduleRegistry == null) {
			this.moduleRegistry = new ModuleRegistry(this, file);
		} else {
			this.moduleRegistry.setFile(file);
		}
	}

	/**
	 * This method return sthe singleton instance of the Core object,
	 * which is primarily used to get access to other ECG components.
	 * @return The singleton instance of the Core object
	 */
	public static Core getInstance() {
		if (theInstance == null) {
			theInstance = new Core();
		}

		return theInstance;
	}

	// TODO : bring into GUI
	public GuiEventWriter getGuiEventWriter()
	{
		return this.guiEventWriter;
	}
	
	/**
	 * This method returns a reference to the MicroSensorDataTypeManager (MsdtManager) component,
	 * which is a registry for legal types of event data.
	 * @return A reference to the MicroSensorDataTypeManager
	 */
	public MsdtManager getMsdtManager()
	{
		return this.mstdManager;
	}
	
	// TODO : make a real module
	public SocketSourceModule getSensorSource()
	{
		return this.sensorSource;
	}
	
	/**
	 * This method returns a reference to the ModuleRegistry component,
	 * which is managing installed and running modules.
	 * @return A reference to the ModuleRegistry
	 */
	public ModuleRegistry getModuleRegistry() {
		return this.moduleRegistry;
	}

	/**
	 * This method quits the ECG Server & Lab application.
	 */
	public void quit() {
		System.exit(0);
	}

	private class Console extends Thread {

		private BufferedReader bufferedReader = null;

		/**
		 * Creates the console to manage the ECG Server & Lab.
		 *
		 */
		public Console() {
			System.out.println("ElectroCodeoGram Server & Lab is starting...");

			this.bufferedReader = new BufferedReader(new InputStreamReader(
					System.in));

		}

		/**
		 * Here the reading of the console-input is done.
		 */
		@Override
		public void run() {

			while (true) {

				System.out.println(">>");

				String inputString = "" + this.readLine();

				System.out.println("Echo: " + inputString);

				if (inputString.equalsIgnoreCase("quit")) {
					this.quit();
					return;
				}
			}
		}

		private String readLine() {
			try {
				return this.bufferedReader.readLine();
			} catch (IOException e) {
				return "quit";
			}
		}

		private void quit() {
			Core.getInstance().quit();

		}
	}

	/**
	 * This method starts the ECG Server & Lab application. 
	 * @param args If a String parameter is given, it is taken to be the mdoule-directory path.
	 */
	public static void main(String[] args) {

		File file = null;

		if (args != null && args.length > 0) {

			file = new File(args[0]);

			if (file.exists() && file.isDirectory()) {
				new Core(file);
			} else {
				new Core();
			}

		} else {
			new Core();
		}

	}

	/**
	 * This is returning a reference to the SensorShellWrapper, which validates all incoming event data.
	 * @return A reference to the SensorShellWrapper
	 */
	public SensorShellWrapper getSensorShellWrapper() {
		return this.sensorShellWrapper;
	}

	/**
	 * The main GUI component is accessible through this method.
	 * @return The Configurator, beeing the main GUI component
	 */
	public Configurator getConfigurator() {
		return this.configurator;
	}

}
