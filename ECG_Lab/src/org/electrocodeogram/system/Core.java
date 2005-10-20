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

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.ModuleDescriptor;
import org.electrocodeogram.module.classloader.ModuleClassLoaderInitializationException;
import org.electrocodeogram.module.registry.IModuleRegistry;
import org.electrocodeogram.module.registry.ModuleClassException;
import org.electrocodeogram.module.registry.ModuleRegistry;
import org.electrocodeogram.module.registry.ModuleSetupLoadException;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.msdt.registry.IMsdtRegistry;
import org.electrocodeogram.msdt.registry.MicroSensorDataTypeRegistrationException;
import org.electrocodeogram.msdt.registry.MsdtRegistry;
import org.electrocodeogram.ui.Gui;
import org.electrocodeogram.ui.IGui;

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
 * <code>java -jar ECGLab-jar -help</code>.
 * @see org.electrocodeogram.system.ISystem
 * @see org.electrocodeogram.system.IModuleSystem
 */
public final class Core extends Observable implements ISystem, IModuleSystem {

    /**
     * This is the default <em>ModuleDirectoryPath</em>.
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
     * @param nogui
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
    private Core(final String moduleDir, final String moduleSetup,
        final boolean nogui) throws ModuleSetupLoadException,
        ModuleClassLoaderInitializationException {

        this();

        logger.entering(this.getClass().getName(), "SystemRoot", new Object[] {
            moduleDir, moduleSetup, nogui});

        if (!nogui) {
            logger.log(Level.INFO,
                "Going to start ECG Lab with user interface.");

            this.gui = new Gui(this.moduleRegistry);
        } else {
            logger.log(Level.INFO,
                "Going to start ECG Lab without user interface.");

            Thread workerThread = new WorkerThread();

            workerThread.start();
        }

        if (moduleDir == null) {
            logger.log(Level.INFO, "Using default module directory: "
                                   + DEFAULT_MODULE_DIRECTORY);

            if (this.moduleRegistry == null) {
                this.moduleRegistry = new ModuleRegistry(new File(
                    DEFAULT_MODULE_DIRECTORY));
            } else {
                this.moduleRegistry.setModuleDirectory(new File(
                    DEFAULT_MODULE_DIRECTORY));
            }
        } else {
            logger
                .log(Level.INFO, "Using given module directory: " + moduleDir);

            if (this.moduleRegistry == null) {
                this.moduleRegistry = new ModuleRegistry(new File(moduleDir));
            } else {
                this.moduleRegistry.setModuleDirectory(new File(moduleDir));
            }
        }
        if (moduleSetup != null) {
            logger.log(Level.INFO, "Loading module setup: " + moduleSetup);

            this.moduleRegistry.loadModuleSetup(new File(moduleSetup));
        }

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
     * @see org.electrocodeogram.system.ISystem#fireStateChange()
     */
    public void fireStateChange() {
        logger.entering(this.getClass().getName(), "fireStateChange");

        this.setChanged();

        this.notifyObservers();

        this.clearChanged();

        logger.log(Level.FINE,
            "A statechange has occured. Notification has been done.");

        logger.exiting(this.getClass().getName(), "fireStateChange");

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

        this.moduleRegistry.registerRunningModule(module);

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

        this.moduleRegistry.deregisterRunningModule(module);

    }

    /**
     * @see org.electrocodeogram.system.IModuleSystem#getModuleDescriptor(java.lang.String)
     */
    public ModuleDescriptor getModuleDescriptor(final String id)
        throws ModuleClassException {
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
     * After it has set up a DefaultExceptionHandler the command line
     * parameters are parsed ans at last the ECG Lab is started.
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

        String moduleDir = getModuleDir(args);

        String moduleSetup = getModuleSetup(args);

        Level logLevel = getLogLevel(args);

        String logFile = getLogFile(args);

        boolean nogui = isNogui(args);

        if (help) {
            printHelpMessage();
        }

        try {
            startSystem(moduleDir, moduleSetup, nogui, logLevel, logFile);
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
     * @param nogui
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
    private static void startSystem(final String moduleDir,
        final String moduleSetup, final boolean nogui, final Level logLevel,
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

        new Core(moduleDir, moduleSetup, nogui);

        logger.exiting(Core.class.getName(), "startSystem");

    }

    /**
     * Parses the command line parameters for the
     * <em>ModuleDirectory</em>.
     * @param args
     *            The command line parameters
     * @return The path to the <em>ModuleDirectory</em> as given as
     *         a command line parameter
     */
    private static String getModuleDir(final String[] args) {
        logger.entering(Core.class.getName(), "getModuleDir", args);

        if (args == null || args.length == 0) {
            logger.exiting(Core.class.getName(), "getModuleDir");

            return null;
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-m")) {
                if (args.length - 1 < i + 1) {
                    printHelpMessage();
                }
                if (args[i + 1] == null || args[i + 1].equals("")
                    || args[i + 1].equals("-s") || args[i + 1].equals("-l")
                    || args[i + 1].equals("--log-file")
                    || args[i + 1].equals("-nogui")) {
                    printHelpMessage();
                } else {
                    logger.exiting(Core.class.getName(), "getModuleDir",
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
     * parameter.
     * @param args
     *            The command line parameters
     * @return If <code>-nogui</code> is given it returns
     *         <code>true</code> otherwise <code>false</code>
     */
    private static boolean isNogui(final String[] args) {
        logger.entering(Core.class.getName(), "isNogui");

        if (args == null || args.length == 0) {
            logger.exiting(Core.class.getName(), "isNogui");

            return false;
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-nogui")) {
                logger.exiting(Core.class.getName(), "isNogui");

                return true;
            }
        }

        logger.exiting(Core.class.getName(), "isNogui");

        return false;
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

        java.lang.System.out.println("-help\tPrints out this list.\n");

        java.lang.System.exit(-1);

        logger.exiting(Core.class.getName(), "printHelpMessage");
    }

    /**
     * @see org.electrocodeogram.system.IModuleSystem#getRootFrame()
     */
    public JFrame getRootFrame() {
        logger.entering(Core.class.getName(), "getRootFrame");

        logger.exiting(Core.class.getName(), "getRootFrame");

        return this.gui;
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
     * If the ECG Lab is started without a graphical user interface,
     * this Thread is used to prevent it from simply exiting after
     * leaving the
     * {@link org.electrocodeogram.system.Core#main(String[])} method.
     */
    private static class WorkerThread extends Thread {

        /**
         * @see java.lang.Thread#run()
         */
        @SuppressWarnings("synthetic-access")
        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (this) {
                        wait();
                    }

                } catch (InterruptedException e) {
                    logger.log(Level.WARNING,
                        "The SystemRoot's WorkerThread has been interrupted.");
                }
            }

        }
    }
}
