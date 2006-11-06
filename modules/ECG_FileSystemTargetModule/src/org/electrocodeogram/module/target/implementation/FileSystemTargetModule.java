/*
 * Class: FileSystemTargetModule
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.target.implementation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    private File outputFile = null;

    /**
     * The original name of the output file.
     */
    private String outputFileName = null;

    /**
     * The <em>PrintWriter</em> is used to write events into the
     * file.
     */
    private PrintWriter writer = null;

    /**
     * This is the default output directory under the user's home
     * directory.
     */
    private static final String LOG_SUBDIR = "ecg_log";

    /**
     * This is the default output file name.
     */
    private static final String LOG_FILENAME = "out.log";

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
     * If true, append events to file, if false delete file first
     */
    private boolean append = true;

    /**
     * If true, append date (in form _YYMMDD) to file name
     */
    private boolean stamp = true;

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

        this.getOutputWriter().println(packet.toString());

        logger.log(Level.INFO, "An event has been written to the file "
                               + this.outputFile.getAbsolutePath()
                               + " by the module " + this.getName());

        logger.exiting(this.getClass().getName(), "write");
    }

    /**
     * @see org.electrocodeogram.module.Module#propertyChanged(org.electrocodeogram.modulepackage.ModuleProperty)
     */
    public final void propertyChanged(final ModuleProperty moduleProperty)
        throws ModulePropertyException {

        logger.entering(this.getClass().getName(), "propertyChanged",
            new Object[] {moduleProperty});

        logger.log(Level.INFO, "Request to set the property: "
                + moduleProperty.getName());

        if (moduleProperty.getName().equals("Append Data")) {

            if (moduleProperty.getValue().equalsIgnoreCase("false"))
                this.append = false;
            else
                this.append = true;
            
            logger.log(Level.INFO, "Set the property: "
                    + moduleProperty.getName() + " to "
                    + Boolean.toString(this.append));            
        
        } else if (moduleProperty.getName().equals("Stamp File Name")) {

                if (moduleProperty.getValue().equalsIgnoreCase("false"))
                    this.stamp = false;
                else
                    this.stamp = true;
                
                logger.log(Level.INFO, "Set the property: "
                        + moduleProperty.getName() + " to "
                        + Boolean.toString(this.stamp));            
            
        } else if (moduleProperty.getName().equals("Output File")) {

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
            File tmpFile = new File(this.outputFileName);

            logger.log(Level.INFO, "Set the property: "
                    + moduleProperty.getName() + " to "
                    + tmpFile.getAbsolutePath());

        } else if (moduleProperty.getName().equals("Split Files")) {

            if (moduleProperty.getValue().equalsIgnoreCase("true"))
                this.rotateFiles = true;
            else
                this.rotateFiles = false;

            logger.log(Level.INFO, "Set the property: "
                                   + moduleProperty.getName() +
                                   Boolean.toString(this.rotateFiles));

        } else if (moduleProperty.getName().equals("File Size")) {

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
    public final void startWriter() throws TargetModuleException {

        // not implemented

    }

    /**
     * @see org.electrocodeogram.module.target.TargetModule#stopWriter()
     *      This method is not implemented in this module.
     */
    public void stopWriter() {

    // not implemented

    }
    
    private PrintWriter getOutputWriter() {
        
        if (this.outputFile != null && 
                this.outputFile.length() >= this.fileSize && 
                this.rotateFiles) 
        {
            logger.log(Level.INFO,
                "The log-file has reached the maximum file size of "
                                + this.fileSize);
            this.writer.close();
            this.count++;
            if (this.writer != null)
                this.writer.close();
            this.writer = null;
            this.outputFile = null;
        }

        if (this.outputFile == null || this.writer == null) {
            
            if (this.outputFileName == null) {
                // If property Output File not set, take {user.home}/out.log
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
                
                this.outputFileName = logDirFile.getAbsolutePath() 
                                        + File.separator 
                                        + LOG_FILENAME;
            }
            
            // Seperate prefix (eg "out" in "out.log") from suffix (eg "log")
            int dotPos = this.outputFileName.lastIndexOf('.');
            String fileName = null;
            String fileSuffix = null;
            if (dotPos == -1) {
                fileName = this.outputFileName;
            } else {
                fileName = this.outputFileName.substring(0, dotPos);
                fileSuffix = this.outputFileName.substring(dotPos + 1);
            }
            
            // Build current full file name
            if (this.stamp) {
                SimpleDateFormat sdf = new SimpleDateFormat();
                sdf.applyPattern("yMMdd");
                String yymmdd = sdf.format(new Date());
                fileName += "_" + yymmdd;
            }
            if (this.rotateFiles) {
                fileName += "-" + String.format("%1$02d", this.count);
            }
            fileName += (fileSuffix != null ? ("." + fileSuffix) : "");
            
            try {
                // create file
                this.outputFile = new File(fileName);        
                // create writer based on file
                this.writer = new PrintWriter(new FileWriter(this.outputFile, this.append), true);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error while creating or opening logfile named '"
                        + this.outputFile.getAbsolutePath()
                        + "'.");
            }
        } 
        
        return this.writer;
    }
}
