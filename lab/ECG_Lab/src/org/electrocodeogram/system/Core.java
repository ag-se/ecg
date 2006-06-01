/*
 * Classname: Core
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

/**
 * Contains the core system classes and the interfaces to access them from
 * the subsystems and from the modules.
 */
package org.electrocodeogram.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.registry.IModuleRegistry;
import org.electrocodeogram.module.registry.ModulePackageNotFoundException;
import org.electrocodeogram.module.registry.ModuleRegistry;
import org.electrocodeogram.module.registry.ModuleSetupLoadException;
import org.electrocodeogram.modulepackage.ModuleDescriptor;
import org.electrocodeogram.modulepackage.classloader.ModuleClassLoaderInitializationException;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.msdt.registry.IMsdtRegistry;
import org.electrocodeogram.msdt.registry.MicroSensorDataTypeRegistrationException;
import org.electrocodeogram.msdt.registry.MsdtRegistry;
import org.electrocodeogram.ui.Gui;
import org.electrocodeogram.ui.IGui;
import org.electrocodeogram.ui.SmallUI;

/**
 * The main class of the <em><b>ElectroCodeoGram's</b> ECG Lab</em>
 * software. It is implemented using the <em>Singleton</em> design
 * pattern, to make it globally accessible by all of ECG's subsystems.
 * Access to this class is provided by the implemented Interfaces
 * <ul>
 * <li>{@link org.electrocodeogram.system.ISystem} for the subsystems
 * and
 * <li>{@link org.electrocodeogram.system.IModuleSystem} for modules.
 * </ul>
 * During startup it creates all other vital parts of the system as
 * there are the
 * <ul>
 * <li>{@link org.electrocodeogram.module.registry.ModuleRegistry}
 * which is keeping track of
 * {@link org.electrocodeogram.module.Module} instances currently
 * running in the ECG Lab and available <em>ModulePackages</em> in
 * the <em>ModuleDirectory</em>.
 * <li>{@link org.electrocodeogram.msdt.registry.MsdtRegistry} which
 * stores information about all
 * {@link org.electrocodeogram.msdt.MicroSensorDataType} instances
 * that are currently registered.
 * <li>{@link org.electrocodeogram.ui.Gui} which is the graphical
 * user interface.
 * </ul>
 * The <em>ModuleDirectory</em> is the file system folder, where the
 * ECG Lab is looking for <em>ModulePackages</em> and a
 * <em>ModulePackage</em> is a folder containing a runtime loadable
 * module class and a <em>ModuleDescription</em>. <br>
 * <br>
 * There are severall command line parameters to toggle logging,
 * disable the gui, or load an existing <em>ModuleSetup</em>, which
 * are listed when the ECG Lab is started with
 * <code>java -jar ecglab.jar -help</code>.
 * @see org.electrocodeogram.system.ISystem
 * @see org.electrocodeogram.system.IModuleSystem
 */
public final class Core extends Observable implements ISystem, IModuleSystem,
    Observer {

    /**
     * This is the default <em>ModuleDirectory</em> path.
     */
    private static final String DEFAULT_MODULE_DIRECTORY = "modules";

    /**
     * This is this class' logger.
     */
    private static Logger logger = LogHelper.createLogger(Core.class.getName());

    /**
     * This is the <em>Singleton</em> instance of this class.
     */
    private static Core theInstance;

    /**
     * This is a reference to the <em>ModuleRegistry</em> subsystem.
     */
    private ModuleRegistry moduleRegistry;

    /**
     * This is a reference to the <em>GUI</em> subsystem.
     */
    private Gui gui;

    /**
     * This is a reference to the <em>MsdtRegistry</em> subsystem.
     */
    private MsdtRegistry mstdRegistry;

    /**
     * GuiKind denotes the kind of Gui for the Lab: GUI is full module
     * gui, SMALLUI is simple termination possibility, and NOGUI
     * is batch mode
     */
	private enum GuiKind {GUI, SMALLUI, NOGUI}
	
	/**
     * The private constructor is only used internally to create the
     * <em>Singleton</em> instance. Here the
     * {@link org.electrocodeogram.module.registry.ModuleRegistry} and
     * the {@link ore.electrocodeogram.msdt.registry.MsdtRegistry} are
     * created and stored into fields for later access through getter
     * methods.
     */
    private Core() {

        logger.entering(this.getClass().getName(), "SystemRoot");

        this.mstdRegistry = new MsdtRegistry();

        this.moduleRegistry = new ModuleRegistry();

        theInstance = this;

        this.mstdRegistry.addObserver(this);

        this.moduleRegistry.addObserver(this);

        logger.exiting(this.getClass().getName(), "SystemRoot");

    }

    /**
     * This is a private constructor that is only to be called from
     * the {@link #main(String[])} method. It first calls the
     * {@link #Core()} constructor and then starts to initialize the
     * subsystems as defined by the command line parameters.
     * @param moduleDir
     *            The path to the <em>ModuleDirectory</em> given as
     *            a command line parameter
     * @param moduleSetup
     *            The path to the <em>ModuleSetup</em> given as a
     *            command line parameter
     * @param guiKind
     *            Tells the ECG Lab to either start with or without a
     *            graphical user interface
     * @throws ModuleSetupLoadException
     *             If the <em>ModuleSetup</em> could not be loaded
     * @throws ModuleClassLoaderInitializationException
     *             If the
     *             {@link org.electrocodeogram.module.registry.ModuleClassLoader},
     *             which is needed to load modules into the ECG Lab,
     *             could not be created
     */
    @SuppressWarnings("boxing")
    private Core(final String modulesDir, final String addModuleDir, 
    	final String moduleSetup, final GuiKind guiKind) 
    		throws ModuleSetupLoadException, ModuleClassLoaderInitializationException {

        this();

        logger.entering(this.getClass().getName(), "SystemRoot", new Object[] {
            modulesDir, moduleSetup, guiKind});

        Thread console = new Console();

        if (guiKind == GuiKind.GUI) {
            logger.log(Level.INFO,
                "Going to start ECG Lab with user interface.");

            this.gui = new Gui();

            this.moduleRegistry.addObserver(this.gui);

            this.mstdRegistry.addObserver(this.gui);

        } else if (guiKind == GuiKind.NOGUI) {
			
            logger.log(Level.INFO,
            "Going to start ECG Lab without user interface.");			
		
		} else {

            logger.log(Level.INFO,
                "Going to start ECG Lab with only basic user interface.");

            new SmallUI();

            // String osName =
            // java.lang.System.getProperty("os.name");
            //
            // if (osName == null || osName.equals("")) {
            // logger
            // .log(Level.WARNING,
            // "The operating system name could not be read from the
            // system environment.");
            //
            // new SmallUI();
            //
            // } else {
            //
            // logger.log(Level.FINE, "The operating system name is: "
            // + osName);
            //
            // if (osName.startsWith("Windows")) {
            // logger.log(Level.INFO,
            // "The operating system is a windows system.");
            //
            // new TrayUI();
            //
            // } else if (osName.startsWith("Linux")) {
            // logger.log(Level.INFO,
            // "The operating system is a linux system.");
            //
            // new SmallUI();
            //
            // } else {
            // logger
            // .log(Level.SEVERE,
            // "The operating system is an unknown system. Aborting
            // Inlineserver startup...");
            //
            // new SmallUI();
            //
            // }
            // }
            //
        }

        if (this.moduleRegistry == null)
            this.moduleRegistry = new ModuleRegistry();

        if (addModuleDir != null)
        {
            logger.log(Level.INFO, "Using given additional module directory: " + addModuleDir);
            this.moduleRegistry.addModule(new File(addModuleDir));
        }
        
        if (modulesDir == null) {
            logger.log(Level.INFO, "Using default modules directory: "
                                   + DEFAULT_MODULE_DIRECTORY);

            this.moduleRegistry.setModuleDirectory(new File(
                    DEFAULT_MODULE_DIRECTORY));
        } else {
            logger
                .log(Level.INFO, "Using given modules directory: " + modulesDir);

            this.moduleRegistry.setModuleDirectory(new File(modulesDir));
        }
        
        if (moduleSetup != null) {
            logger.log(Level.INFO, "Loading module setup: " + moduleSetup);

            this.moduleRegistry.loadModuleSetup(new File(moduleSetup));
        }

        console.start();

        LogHelper.disableForeignLogger();

        logger.exiting(this.getClass().getName(), "SystemRoot");

    }

    /**
     * Returns the <em>Singleton</em> instance of this class.
     * @return The <em>Singleton</em> instance of this class
     */
    protected static Core getInstance() {
        logger.entering(Core.class.getName(), "getInstance");

        if (theInstance == null) {
            logger.log(Level.FINE,
                "There is no instance by now. Going to create one.");

            theInstance = new Core();
        }

        logger.exiting(Core.class.getName(), "getInstance");

        return theInstance;
    }

    /**
     * @see org.electrocodeogram.system.ISystem#getMsdtRegistry()
     */
    public IMsdtRegistry getMsdtRegistry() {
        logger.entering(this.getClass().getName(), "getMsdtRegistry");

        logger.exiting(this.getClass().getName(), "getMsdtRegistry",
            this.mstdRegistry);

        return this.mstdRegistry;
    }

    /**
     * @see org.electrocodeogram.system.ISystem#getModuleRegistry()
     */
    public IModuleRegistry getModuleRegistry() {
        logger.entering(Core.class.getName(), "getModuleRegistry");

        logger.exiting(Core.class.getName(), "getModuleRegistry");

        return this.moduleRegistry;
    }

    /**
     * @see org.electrocodeogram.system.ISystem#quit()
     */
    public void quit() {
        logger.entering(this.getClass().getName(), "quit");

        logger.log(Level.INFO, "Exiting ECG Lab...");

        java.lang.System.exit(0);

        logger.exiting(this.getClass().getName(), "quit");
    }

    /**
     * @see org.electrocodeogram.system.ISystem#getGui()
     */
    public IGui getGui() {
        logger.entering(this.getClass().getName(), "getGui");

        logger.exiting(this.getClass().getName(), "getGui", this.gui);

        return this.gui;
    }

    /**
     * @see org.electrocodeogram.system.ISystem#getMainWindow()
     */
    public JFrame getMainWindow() {
        logger.entering(this.getClass().getName(), "getFrame");

        logger.exiting(this.getClass().getName(), "getFrames", this.gui);

        return this.gui;

    }

    /**
     * @see org.electrocodeogram.system.IModuleSystem#registerModule(org.electrocodeogram.module.Module)
     */
    public void registerModule(final Module module) {

        if (module == null) {

            logger.log(Level.WARNING, "Parameter module is null.");

            return;
        }

        this.addObserver(module.getSystemObserver());

        this.moduleRegistry.registerModule(module);

    }

    /**
     * @see org.electrocodeogram.system.IModuleSystem#deregisterModule(Module)
     */
    public void deregisterModule(final Module module) {

        if (module == null) {

            logger.log(Level.WARNING, "Parameter module is null.");

            return;
        }

        this.deleteObserver(module.getSystemObserver());

        this.moduleRegistry.deregisterModule(module);

    }

    /**
     * @see org.electrocodeogram.system.IModuleSystem#getModuleDescriptor(java.lang.String)
     */
    public ModuleDescriptor getModuleDescriptor(final String id)
        throws ModulePackageNotFoundException {
        return this.moduleRegistry.getModuleDescriptor(id);
    }

    /**
     * @see org.electrocodeogram.system.IModuleSystem#getMicroSensorDataTypes()
     */
    public MicroSensorDataType[] getMicroSensorDataTypes() {
        return this.mstdRegistry.getMicroSensorDataTypes();
    }

    /**
     * @see org.electrocodeogram.system.IModuleSystem#getPredefinedMicroSensorDataTypes()
     */
    public MicroSensorDataType[] getPredefinedMicroSensorDataTypes() {
        return this.mstdRegistry.getPredefinedMicroSensorDataTypes();
    }

    /**
     * @see org.electrocodeogram.system.IModuleSystem#requestMsdtRegistration(org.electrocodeogram.msdt.MicroSensorDataType,
     *      org.electrocodeogram.module.Module)
     */
    public MicroSensorDataType requestMsdtRegistration(
        final MicroSensorDataType msdt, final Module module)
        throws MicroSensorDataTypeRegistrationException {
        return this.mstdRegistry.requestMsdtRegistration(msdt, module);
    }

    /**
     * The main method starts the ECG Lab. <br>
     * After it has set up a <code>DefaultExceptionHandler</code> the command line
     * parameters are parsed and at last the ECG Lab is started.
     * @param args
     *            The command line parameters as listet when called
     *            with <code>java -jar ECGLab.jar -help</code>
     */
    public static void main(final String[] args) {

        logger.entering(Core.class.getName(), "main", args);

        Thread
            .setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());

        logger.log(Level.FINE,
            "The DefaultExceptionHandler has been registered.");

        boolean help = isHelp(args);

        String addModuleDir = getAddModuleDir(args);

        String modulesDir = getModulesDir(args);

        String moduleSetup = getModuleSetup(args);

        Level logLevel = getLogLevel(args);

        String logFile = getLogFile(args);

        GuiKind guiKind = guiKind(args);

        if (help) {
            printHelpMessage();
        }

        try {
            startSystem(modulesDir, addModuleDir, moduleSetup, guiKind, logLevel, logFile);
        } catch (ModuleSetupLoadException e) {
            java.lang.System.err
                .println("The following error occured while loading the given ModuleSetup: "
                         + moduleSetup);

            java.lang.System.err.println(e.getMessage());
        } catch (ModuleClassLoaderInitializationException e) {
            java.lang.System.err
                .println("The following error occured while starting the module management:");

            java.lang.System.err.println(e.getMessage());
        }

        logger.exiting(Core.class.getName(), "main");
    }

    /**
     * The {@link #main} method calls this as the final step after all
     * command line parameters have been parsed. The method set the
     * logging as defined and calls the
     * {@link #SystemRoot(String, String, boolean)} constructor
     * passing the command line parameters to it.
     * @param moduleDir
     *            The path to the <em>ModuleDirectory</em> given as
     *            a command line parameter
     * @param moduleSetup
     *            The path to the <em>ModuleSetup</em> given as a
     *            command line parameter
     * @param guiKind
     *            Tells the ECG Lab to either start with or without a
     *            graphical user interface
     * @param logLevel
     *            Is the loglevel to use for logging
     * @param logFile
     *            Is the logfile to use for logging
     * @throws ModuleSetupLoadException
     *             If the <em>ModuleSetup</em> could not be loaded
     * @throws ModuleClassLoaderInitializationException
     *             If the
     *             {@link org.electrocodeogram.module.registry.ModuleClassLoader},
     *             which is needed to load modules into the ECG Lab,
     *             could not be created
     */
    private static void startSystem(final String modulesDir, final String addModuleDir, 
        final String moduleSetup, final GuiKind guiKind, final Level logLevel,
        final String logFile) throws ModuleSetupLoadException,
        ModuleClassLoaderInitializationException {

        logger.entering(Core.class.getName(), "startSystem");

        LogHelper.setLogLevel(logLevel);

        try {
            LogHelper.setLogFile(logFile);
        } catch (SecurityException e) {
            java.lang.System.err.println("Unable to set the logfile to: "
                                         + logFile);
        } catch (IOException e) {
            java.lang.System.err.println("Unable to set the logfile to: "
                                         + logFile);
        }

        new Core(modulesDir, addModuleDir, moduleSetup, guiKind);

        logger.exiting(Core.class.getName(), "startSystem");

    }

    /**
     * Parses the command line parameters for the
     * <em>ModulesDirectory</em>.
     * @param args
     *            The command line parameters
     * @return The path to the <em>ModuleDirectory</em> as given as
     *         a command line parameter
     */
    private static String getModulesDir(final String[] args) {
        logger.entering(Core.class.getName(), "getModulesDir", args);

        if (args == null || args.length == 0) {
            logger.exiting(Core.class.getName(), "getModulesDir");

            return null;
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-m")) {
                if (args.length - 1 < i + 1) {
                    printHelpMessage();
                }
                if (args[i + 1] == null || args[i + 1].equals("")
                    || args[i + 1].startsWith("-")) {
                    printHelpMessage();
                } else {
                    logger.exiting(Core.class.getName(), "getModulesDir",
                        args[i + 1]);

                    return args[i + 1];
                }
            }
        }

        logger.exiting(Core.class.getName(), "getModuleDir", null);

        return null;
    }

    /**
     * Parses the command line parameters for the
     * <em>AdditionalModuleDirectory</em>.
     * @param args
     *            The command line parameters
     * @return The path to the <em>ModuleDirectory</em> as given as
     *         a command line parameter
     */
    private static String getAddModuleDir(final String[] args) {
        logger.entering(Core.class.getName(), "getAddModuleDir", args);

        if (args == null || args.length == 0) {
            logger.exiting(Core.class.getName(), "getAddModuleDir");

            return null;
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-madd")) {
                if (args.length - 1 < i + 1) {
                    printHelpMessage();
                }
                if (args[i + 1] == null || args[i + 1].equals("")
                    || args[i + 1].startsWith("-")) {
                    printHelpMessage();
                } else {
                    logger.exiting(Core.class.getName(), "getAddModuleDir",
                        args[i + 1]);

                    return args[i + 1];
                }
            }
        }

        logger.exiting(Core.class.getName(), "getModuleDir", null);

        return null;
    }

    /**
     * Parses the command line parameters for the <code>-nogui</code>
     * and <code>-smallui</code> parameter.
     * @param args
     *            The command line parameters
     * @return If <code>-nogui</code> is given it returns
     *         <code>NOGUI</code>, <code>SMALLUI</code> for
     *         <code>-smallui</code>, otherwise <code>GUI</code>
     */
    private static GuiKind guiKind(final String[] args) {
        logger.entering(Core.class.getName(), "guiKind");

        if (args == null || args.length == 0) {
            logger.exiting(Core.class.getName(), "guiKind");
            return GuiKind.GUI;
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-nogui")) {
                logger.exiting(Core.class.getName(), "guiKind");
                return GuiKind.NOGUI;
            }
            if (args[i].equals("-smallui")) {
                logger.exiting(Core.class.getName(), "guiKind");
                return GuiKind.SMALLUI;
            }
        }

        logger.exiting(Core.class.getName(), "guiKind");

        return GuiKind.GUI;
    }

    /**
     * Parses the command line parameters for the <code>-help</code>
     * parameter.
     * @param args
     *            The command line parameters
     * @return If <code>-help</code> is given it returns
     *         <code>true</code> otherwise <code>false</code>
     */
    private static boolean isHelp(final String[] args) {
        logger.entering(Core.class.getName(), "isHelp");

        if (args == null || args.length == 0) {
            logger.exiting(Core.class.getName(), "isHelp");

            return false;
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-help")) {
                logger.exiting(Core.class.getName(), "isHelp");

                return true;
            }
        }

        logger.exiting(Core.class.getName(), "isHelp");

        return false;
    }

    /**
     * Parses the command line parameters for the <em>ModuleSetup</em>.
     * @param args
     *            The command line parameters
     * @return The path to the <em>ModuleSetup</em> as given as a
     *         command line parameter
     */
    private static String getModuleSetup(final String[] args) {
        logger.entering(Core.class.getName(), "getModuleSetup");

        if (args == null || args.length == 0) {
            logger.exiting(Core.class.getName(), "getModuleSetup");

            return null;
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-s")) {
                if (args.length - 1 < i + 1) {
                    printHelpMessage();
                }
                if (args[i + 1] == null || args[i + 1].equals("")
                    || args[i + 1].equals("-m") || args[i + 1].equals("-l")
                    || args[i + 1].equals("--log-file")
                    || args[i + 1].equals("-nogui")) {
                    printHelpMessage();
                } else {

                    logger.exiting(Core.class.getName(), "getModuleSetup");

                    return args[i + 1];
                }
            }
        }

        logger.exiting(Core.class.getName(), "getModuleSetup");

        return null;
    }

    /**
     * Parses the command line parameters for the logfile.
     * @param args
     *            The command line parameters
     * @return The path to the logfile as given as a command line
     *         parameter
     */
    private static String getLogFile(final String[] args) {
        logger.entering(Core.class.getName(), "getLogFile");

        if (args == null || args.length == 0) {
            logger.exiting(Core.class.getName(), "getLogFile");

            return null;
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--log-file")) {
                if (args.length - 1 < i + 1) {
                    printHelpMessage();
                }
                if (args[i + 1] == null || args[i + 1].equals("")
                    || args[i + 1].equals("-m") || args[i + 1].equals("-l")
                    || args[i + 1].equals("-s") || args[i + 1].equals("-nogui")) {
                    printHelpMessage();
                } else {
                    logger.exiting(Core.class.getName(), "getLogFile");

                    return args[i + 1];
                }
            }
        }
        logger.exiting(Core.class.getName(), "getLogFile");

        return null;
    }

    /**
     * Parses the command line parameters for the loglevel.
     * @param args
     *            The command line parameters
     * @return The loglevel as given as a command line parameter
     */
    private static Level getLogLevel(final String[] args) {
        logger.entering(Core.class.getName(), "getLogLevel");

        if (args == null || args.length == 0) {
            logger.exiting(Core.class.getName(), "getLogLevel");

            return null;
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--log-level")) {
                if (args.length - 1 < i + 1) {
                    printHelpMessage();
                }
                if (args[i + 1] == null || args[i + 1].equals("")
                    || args[i + 1].equals("-m") || args[i + 1].equals("-l")
                    || args[i + 1].equals("--log-file")
                    || args[i + 1].equals("-s") || args[i + 1].equals("-nogui")) {
                    printHelpMessage();
                } else {
                    if (args[i + 1].equalsIgnoreCase(LogHelper.LEVEL_OFF)) {
                        logger.exiting(Core.class.getName(), "getLogLevel");

                        return Level.OFF;
                    } else if (args[i + 1]
                        .equalsIgnoreCase(LogHelper.LEVEL_ERROR)) {
                        logger.exiting(Core.class.getName(), "getLogLevel");

                        return Level.SEVERE;
                    } else if (args[i + 1]
                        .equalsIgnoreCase(LogHelper.LEVEL_WARNING)) {
                        logger.exiting(Core.class.getName(), "getLogLevel");

                        return Level.WARNING;
                    } else if (args[i + 1]
                        .equalsIgnoreCase(LogHelper.LEVEL_INFO)) {
                        logger.exiting(Core.class.getName(), "getLogLevel");

                        return Level.INFO;
                    } else if (args[i + 1]
                        .equalsIgnoreCase(LogHelper.LEVEL_VERBOSE)) {
                        logger.exiting(Core.class.getName(), "getLogLevel");

                        return Level.FINE;
                    } else if (args[i + 1]
                        .equalsIgnoreCase(LogHelper.LEVEL_PACKET)) {
                        logger.exiting(Core.class.getName(), "getLogLevel");

                        return ECGLevel.PACKET;
                    } else if (args[i + 1]
                        .equalsIgnoreCase(LogHelper.LEVEL_DEBUG)) {
                        logger.exiting(Core.class.getName(), "getLogLevel");

                        return Level.FINEST;
                    } else {
                        logger.exiting(Core.class.getName(), "getLogLevel");

                        return null;
                    }
                }
            }
        }
        logger.exiting(Core.class.getName(), "getLogLevel");

        return null;
    }

    /**
     * This method prints out the list of command line parameters.
     */
    private static void printHelpMessage() {

        logger.entering(Core.class.getName(), "printHelpMessage");

        java.lang.System.out.println("Usage: java -jar ECGLab.jar <options>\n");

        java.lang.System.out.println("Where options are:\n");

        java.lang.System.out
            .println("-m <moduleDir>\tSets the module directory to moduleDir.\n");

        java.lang.System.out
            .println("-s <moduleSetupFile>\tIs the file containing the module setup to load.\n");

        java.lang.System.out
            .println("--log-level [off | error | warning | info | verbose | packet | debug ]\tSets the log level.\n");

        java.lang.System.out
            .println("--log-file <logFile>\tIs the logfile to use. If no logfile is given, logging goes to standard out.\n");

        java.lang.System.out
            .println("-nogui\tTells the ECG Lab to start without graphical user interface (for inline server mode).\n");

        java.lang.System.out
        .println("-smallui\tTells the ECG Lab to start with only a task killer button.\n");

        java.lang.System.out.println("-help\tPrints out this list.\n");

        java.lang.System.exit(-1);

        logger.exiting(Core.class.getName(), "printHelpMessage");
    }

    /**
     * The DefaultExceptionHandler implementation to react on all
     * unforeseen Exceptions.
     */
    private static class DefaultExceptionHandler implements
        UncaughtExceptionHandler {

        /**
         * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread,
         *      java.lang.Throwable)
         */
        @SuppressWarnings("synthetic-access")
        public void uncaughtException(final Thread t, final Throwable e) {
            logger.entering(Core.class.getName(), "uncaughtException");

            java.lang.System.out.println("An uncaught Exception had occured:");

            java.lang.System.out.println("Thread:" + t.getName());

            java.lang.System.out.println("Class: " + t.getClass());

            java.lang.System.out.println("State: " + t.getState());

            java.lang.System.out.println("Message: " + e.getMessage());

            java.lang.System.out.println("StackTrace: ");

            e.printStackTrace();

            logger.exiting(Core.class.getName(), "uncaughtException");

        }

    }

    /**
     * The <code>Console</code> is reading commands from <em>SDTIN</em>.
     * It is currently only used to quit the ECG Lab, but could easily
     * be extended to be an alternative to the GUI.
     */
    private static class Console extends Thread {

        /**
         * This is the logger.
         */
        private Logger consoleLogger = LogHelper.createLogger(Console.class
            .getName());

        /**
         * To read from <em>SDTIN</em>.
         */
        private BufferedReader reader;

        /**
         * The <code>Thread</code> is running untl this is
         * <code>false</code>.
         */
        private boolean run;

        /**
         * Creates the <em>Console</em>.
         */
        public Console() {

            this.consoleLogger.entering(this.getClass().getName(), "Console");

            this.reader = new BufferedReader(new InputStreamReader(
                java.lang.System.in));

            this.run = true;

            this.consoleLogger.exiting(this.getClass().getName(), "Console");
        }

        /**
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {

            this.consoleLogger.entering(this.getClass().getName(), "run");

            String line;

            while (this.run) {

                try {
                    line = this.reader.readLine();

                    if (line != null) {

                        java.lang.System.out.println("Echo: " + line);

                        if (line.equals("quit")) {
                            Core.getInstance().quit();
                        } else {
                            java.lang.System.out.println("Unknown Commmand: "
                                                         + line);
                        }
                    }

                } catch (IOException e) {

                    this.consoleLogger.log(Level.SEVERE,
                        "An error occured while reading from SDTIN.");

                    this.consoleLogger.log(Level.SEVERE, e.getMessage());

                    this.run = false;
                }

            }

            this.consoleLogger.exiting(this.getClass().getName(), "run");

        }
    }

    /**
     * @see java.util.Observer#update(java.util.Observable,
     *      java.lang.Object)
     */
    public void update(final Observable o, final Object arg) {

        logger.entering(this.getClass().getName(), "update", new Object[] {o,
            arg});

        if (o instanceof Module.GuiNotificator) {

            Module.GuiNotificator sn = (Module.GuiNotificator) o;

            fireStateChange(sn.getModule());
        } else if (o instanceof ModuleRegistry) {
            fireStateChange(arg);
        }

        logger.exiting(this.getClass().getName(), "update");

    }

    /**
     * @see org.electrocodeogram.system.ISystem#fireStateChange(Object)
     */
    public void fireStateChange(final Object object) {
        logger.entering(this.getClass().getName(), "fireStateChange",
            new Object[] {object});

        this.setChanged();

        this.notifyObservers(object);

        this.clearChanged();

        logger.log(Level.FINE,
            "A statechange has occured. Notification has been done.");

        logger.exiting(this.getClass().getName(), "fireStateChange");

    }
}
