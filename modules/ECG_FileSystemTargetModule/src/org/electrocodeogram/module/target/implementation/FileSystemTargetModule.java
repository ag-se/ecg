/*
 * Class: FileSystemTargetModule
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.target.implementation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.electrocodeogram.modulepackage.ModulePropertyException;
import org.electrocodeogram.module.target.TargetModule;
import org.electrocodeogram.module.target.TargetModuleException;

/**
 * This class is an ECG module used to write ECG events into a file in
 * the file system.
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
     * The original name of the output file.
     */
    private String outputFileName;

    /**
     * The <em>PrintWriter</em> is used to write events into the
     * file.
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
     * This is the default output directory under the user's home
     * directory.
     */
    private static final String LOG_SUBDIR = "ecg_log";

    /**
     * This is the default maximum file size for the rotation of
     * output files.
     */
    private static final int DEFAULT_FILE_SIZE = 1024 * 1024 * 10;

    /**
     * This counter is indexing the output files for the rotation.
     */
    private int count = 1;

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
    private String logDir;

    /**
     * This creates the module instance. It is not to be called by
     * developers, instead it is called from the ECG
     * <em>ModuleRegistry</em> subsystem, when the user requested a
     * new instance of this module.
     * @param id
     *            This is the unique <code>String</code> id of the
     *            module
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

                this.outputFile = new File(this.logDir
                                           + File.separator
                                           + ++this.count + "_" 
                                           + this.outputFileName);

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
     * @see org.electrocodeogram.module.Module#propertyChanged(org.electrocodeogram.modulepackage.ModuleProperty)
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
            
            this.outputFileName = moduleProperty.getValue();

            this.outputFile = new File(this.outputFileName);

            this.logDir = this.outputFile.getAbsoluteFile().getParent();
            if (this.logDir == null)
            	this.logDir = ".";

            if (this.writer != null) {
                this.writer.close();
            }

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
     * @see org.electrocodeogram.module.Module#update() This method is
     *      not implemented in this module, as this module does not
     *      need to be informed about ECG Lab subsystem's state
     *      changes.
     */
    @Override
    public void update() {

    // not implemented
    }

    /**
     * @see org.electrocodeogram.module.Module#initialize()
     */
    @Override
    public final void initialize() {

    // not implemented

    }

    /**
     * @see org.electrocodeogram.module.target.TargetModule#startWriter()
     */
    @Override
    public final void startWriter() throws TargetModuleException {

        logger.entering(this.getClass().getName(), "startWriter");

        if (this.outputFile == null) {

            this.homeDir = System.getProperty("user.home");

            if (this.homeDir == null) {

                logger.log(Level.WARNING,
                    "The user's home directory can not be determined.");

                this.homeDir = ".";

                logger.log(Level.WARNING,
                    "Using the current working directory "
                                    + new File(".").getAbsolutePath()
                                    + "instead.");
            }

            File logDirFile = new File(this.homeDir + File.separator + LOG_SUBDIR);

            if (!logDirFile.exists()) {
                logDirFile.mkdir();
            }
            
            this.logDir = logDirFile.getAbsolutePath();

            this.outputFileName = this.logDir + File.separator 
            						+ DEFAULT_FILENAME_PREFIX
                                    + DEFAULT_FILENAME_SUFFIX;

            this.outputFile = new File(this.outputFileName);

            try {
                this.writer = new PrintWriter(new BufferedWriter(
                    new FileWriter(this.outputFile, true)));

            } catch (IOException e) {
                logger.log(Level.SEVERE,
                    "Error while opening the output file: "
                                    + this.outputFile.getAbsolutePath());

                logger.log(Level.FINEST, e.getMessage());
            }
        }

        this.logDir = this.outputFile.getAbsoluteFile().getParent();
        if (this.logDir == null)
        	this.logDir = ".";

        logger.exiting(this.getClass().getName(), "startWriter");

    }

    /**
     * @see org.electrocodeogram.module.target.TargetModule#stopWriter()
     *      This method is not implemented in this module.
     */
    @Override
    public void stopWriter() {

    // not implemented

    }
}
