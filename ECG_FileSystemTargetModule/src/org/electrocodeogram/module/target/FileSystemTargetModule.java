/*
 * Class: FileSystemTargetModule
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.target;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.ModuleProperty;
import org.electrocodeogram.module.ModulePropertyException;

/**
 * This class is an ECG module used to write ECG events into a file in the file
 * system.
 */
public class FileSystemTargetModule extends TargetModule {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(FileSystemTargetModule.class.getName());

    /**
     * A reference to the output file.
     */
    private File outputFile;

    /**
     * The <em>PrintWriter</em> is used to write events into the file.
     */
    private PrintWriter writer;

    /**
     * This is the default output filename prefix.
     */
    private static final String DEFAULT_FILENAME_PREFIX = "out";

    /**
     * This is the default output filename suffix.
     */
    private static final String DEFAULT_FILENAME_SUFFIX = ".log";

    /**
     * This is the default output directory under the user's home directory.
     */
    private static final String LOG_SUBDIR = "ecg_log";

    /**
     * This is the default maximum file size for the rotation of output files.
     */
    private static final int DEFAULT_FILE_SIZE = 1024 * 1024 * 10;

    /**
     * This counter is indexing the output files for the rotation.
     */
    private int count = 0;

    /**
     * The actual maximum file size for the rotation.
     */
    private int fileSize = DEFAULT_FILE_SIZE;

    /**
     * This enables or disables the output file rotation.
     */
    private boolean rotateFiles = false;

    /**
     * A reference to the user's home directory path.
     */
    private String homeDir;

    /**
     * A reference to the log directory.
     */
    private File logDir;

    /**
     * This creates the module instance. It is not to be
     * called by developers, instead it is called from the ECG
     * <em>ModuleRegistry</em> subsystem, when the user requested a new instance of this
     * module.
     * @param id
     *            This is the unique <code>String</code> id of the module
     * @param name
     *            This is the name which is assigned to the module
     *            instance
     */
    public FileSystemTargetModule(final String id, final String name) {
        super(id, name);

        logger.entering(this.getClass().getName(), "FileSystemTargetModule",
            new Object[] {id, name});

        logger.exiting(this.getClass().getName(), "FileSystemTargetModule");

    }

    /**
     * @see org.electrocodeogram.module.target.TargetModule#write(org.electrocodeogram.event.ValidEventPacket)
     */
    @Override
    public final void write(final ValidEventPacket packet) {

        logger.entering(this.getClass().getName(), "write",
            new Object[] {packet});

        try {
            this.writer.println(packet.toString());

            this.writer.flush();

            logger.log(Level.INFO, "An event has been written to the file "
                                   + this.outputFile.getAbsolutePath()
                                   + " by the module " + this.getName());

            if (this.outputFile.length() >= this.fileSize && this.rotateFiles) {
                logger.log(Level.INFO,
                    "The log-file has reached the maximum file size of "
                                    + this.fileSize);

                this.writer.close();

                this.outputFile = new File(this.logDir.getAbsoluteFile()
                                           + File.separator + ++this.count
                                           + "_"
                                           + this.outputFile.getAbsolutePath());

                this.writer = new PrintWriter(new FileWriter(this.outputFile));

                logger.log(Level.INFO, "A new log-file has been created: "
                                       + this.outputFile.getAbsolutePath());
            }

            logger.exiting(this.getClass().getName(), "write");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while writing to logfile: "
                                     + this.outputFile.getAbsolutePath()
                                     + "\nThe disk might be full.");
        }
    }

    /**
     * @see org.electrocodeogram.module.Module#propertyChanged(org.electrocodeogram.module.ModuleProperty)
     */
    @Override
    public final void propertyChanged(final ModuleProperty moduleProperty)
        throws ModulePropertyException {

        logger.entering(this.getClass().getName(), "propertyChanged",
            new Object[] {moduleProperty});

        if (moduleProperty.getName().equals("Output File")) {

            logger.log(Level.INFO, "Request to set the property: "
                                   + moduleProperty.getName());

            if (moduleProperty.getValue() == null) {
                logger.log(Level.WARNING, "The property value is null for: "
                                          + moduleProperty.getName());

                logger.exiting(this.getClass().getName(), "propertyChanged");

                throw new ModulePropertyException(
                    "The property value is null.", this.getName(),
                    this.getId(), moduleProperty.getName(), moduleProperty
                        .getValue());
            }

            File propertyValueFile = new File(moduleProperty.getValue());

            this.outputFile = propertyValueFile;

            this.writer.close();

            try {
                this.writer = new PrintWriter(new FileWriter(this.outputFile));

                logger.log(Level.INFO, "Set the property: "
                                       + moduleProperty.getName() + " to "
                                       + this.outputFile.getAbsolutePath());
            } catch (IOException e) {

                logger.log(Level.SEVERE,
                    "The file could not be opened for writing: "
                                    + moduleProperty.getValue());

                logger.exiting(this.getClass().getName(), "propertyChanged");

                throw new ModulePropertyException(
                    "The file could not be opened for writing.",
                    this.getName(), this.getId(), moduleProperty.getName(),
                    moduleProperty.getValue());
            }

        } else if (moduleProperty.getName().equals("Split Files")) {
            logger.log(Level.INFO, "Request to set the property: "
                                   + moduleProperty.getName());

            if (moduleProperty.getValue().equals("true")) {
                this.rotateFiles = true;

                logger.log(Level.INFO, "Set the property: "
                                       + moduleProperty.getName() + " to true");
            } else if (moduleProperty.getValue().equals("false")) {
                this.rotateFiles = false;

                logger
                    .log(Level.INFO, "Set the property: "
                                     + moduleProperty.getName() + " to false");
            } else {
                logger.log(Level.WARNING,
                    "The module does not support a property value of "
                                    + moduleProperty.getValue()
                                    + " with the given name: "
                                    + moduleProperty.getName());

                logger.exiting(this.getClass().getName(), "propertyChanged");

                throw new ModulePropertyException(
                    "The module does not support this property.", this
                        .getName(), this.getId(), moduleProperty.getName(),
                    moduleProperty.getValue());
            }

        } else if (moduleProperty.getName().equals("File Size")) {
            logger.log(Level.INFO, "Request to set the property: "
                                   + moduleProperty.getName());

            try {
                this.fileSize = Integer.parseInt(moduleProperty.getValue());

                logger.log(Level.INFO, "Set the property: "
                                       + moduleProperty.getName() + " to "
                                       + this.fileSize);

            } catch (NumberFormatException e) {
                logger.log(Level.WARNING,
                    "The module does not support a property value of "
                                    + moduleProperty.getValue()
                                    + " with the given name: "
                                    + moduleProperty.getName());

                logger.exiting(this.getClass().getName(), "propertyChanged");

                throw new ModulePropertyException(
                    "The module does not support this property.", this
                        .getName(), this.getId(), moduleProperty.getName(),
                    moduleProperty.getValue());
            }

        } else {
            logger.log(Level.WARNING,
                "The module does not support a property with the given name: "
                                + moduleProperty.getName());

            logger.exiting(this.getClass().getName(), "propertyChanged");

            throw new ModulePropertyException(
                "The module does not support this property.", this.getName(),
                this.getId(), moduleProperty.getName(), moduleProperty
                    .getValue());

        }

        logger.exiting(this.getClass().getName(), "propertyChanged");
    }

    /**
     * @see org.electrocodeogram.module.Module#update()
     *      This method is not implemented in this module, as
     *      this module does not need to be informed about
     *      ECG Lab subsystem's state changes.
     */
    @Override
    public void update() {

    // not implemented
    }

    /**
     * @see org.electrocodeogram.module.Module#initialize()
     * The method creates the default output file and a <em>PrintWriter</em> to write to it.
     */
    @Override
    public final void initialize() {
        logger.entering(this.getClass().getName(), "initialize");

        this.homeDir = System.getProperty("user.home");

        if (this.homeDir == null) {

            logger.log(Level.WARNING,
                "The user's home directory can not be determined.");

            this.homeDir = ".";

            logger.log(Level.WARNING, "Using the current working directory "
                                      + new File(".").getAbsolutePath()
                                      + "instead.");
        }

        this.logDir = new File(this.homeDir + File.separator + LOG_SUBDIR);

        if (!this.logDir.exists()) {
            this.logDir.mkdir();
        }

        String outputFileName = DEFAULT_FILENAME_PREFIX
                                + DEFAULT_FILENAME_SUFFIX;

        this.outputFile = new File(this.logDir.getAbsolutePath()
                                   + File.separator + outputFileName);

        try {
            this.writer = new PrintWriter(new BufferedWriter(new FileWriter(
                this.outputFile, true)));

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while opening the output file: "
                                     + this.outputFile.getAbsolutePath());

            logger.log(Level.FINEST, e.getMessage());
        }

        logger.exiting(this.getClass().getName(), "initialize");
    }

    /**
     * @see org.electrocodeogram.module.target.TargetModule#startWriter()
     *  This method is not implemented in this module.
     */
    @SuppressWarnings("unused")
    @Override
    public void startWriter() throws TargetModuleException {

    // not implemented

    }

    /**
     * @see org.electrocodeogram.module.target.TargetModule#stopWriter()
     *  This method is not implemented in this module.
     */
    @Override
    public void stopWriter() {

    // not implemented

    }
}
