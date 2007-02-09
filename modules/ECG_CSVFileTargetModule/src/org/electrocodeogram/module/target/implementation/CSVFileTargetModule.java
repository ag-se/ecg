/*
 * Class: CSVFileTargetModule
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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.misc.xml.ECGParser;
import org.electrocodeogram.misc.xml.NodeException;
import org.electrocodeogram.module.target.TargetModule;
import org.electrocodeogram.module.target.TargetModuleException;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.electrocodeogram.modulepackage.ModulePropertyException;
import org.w3c.dom.Document;

/**
 * This class is an ECG module used to write ECG events into a file in
 * the file system.
 */
public class CSVFileTargetModule extends TargetModule {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(CSVFileTargetModule.class.getName());

    /**
     * A reference to the output file.
     */
    private File outputFile;

    /**
     * The <em>PrintWriter</em> is used to write events into the
     * file.
     */
    private PrintWriter writer;

    /**
     * This is the default output filename prefix.
     */
    private static final String DEFAULT_FILENAME_PREFIX = "ecgout";

    /**
     * This is the default output filename suffix.
     */
    private static final String DEFAULT_FILENAME_SUFFIX = ".csv";

    /**
     * This is the default output directory under the user's home
     * directory.
     */
    private static final String LOG_SUBDIR = "ecg_log";

    /**
     * A reference to the user's home directory path.
     */
    private String homeDir;

    /**
     * A reference to the log directory.
     */
    private File logDir;

    /**
     * The delimiter to seperate the columns, defaults to TAB.
     */
    private String delimiter = "\t";

    /**
     * List of columns. The names are used to retrieve values from an
     * Event via ECGParser#getSingleNodeValue()
     */
    private ArrayList<String> columns;
    
    /**
     * Indicates whether column name line must be printed first, true if not.
     */
    private boolean alreadyStarted = false;
    
    /**
     * Name of the pseudo column for the type name of an event
     */
    private static final String TYPE_COLUMN = "type";
    
    /**
     * Name of the pseudo column for the timestamp of an event
     */
    private static final String TIMESTAMP_COLUMN = "timestamp";
    
    /**
     * Generally useful DateFormat
     */
    public static DateFormat dateFormat = DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM, DateFormat.MEDIUM);
    	
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
    public CSVFileTargetModule(final String id, final String name) {
        super(id, name);

        logger.entering(this.getClass().getName(), "CSVFileTargetModule",
            new Object[] {id, name});

        logger.exiting(this.getClass().getName(), "CSVFileTargetModule");

    }

    /**
     * @see org.electrocodeogram.module.target.TargetModule#write(org.electrocodeogram.event.ValidEventPacket)
     */
    @Override
    public final void write(final ValidEventPacket packet) {

        logger.entering(this.getClass().getName(), "write",
            new Object[] {packet});

        boolean first = true;
        
        if (!alreadyStarted) {
            for (String column : columns) {
                this.writer.print((first ?  "" : this.delimiter) + column);
                first = false;
            }
            alreadyStarted = true;
        }
        
        this.writer.println();
        
        first = true;
        for (String column : columns) {
        	this.writer.print( (first ?  "" : this.delimiter) + 
                    getValueOfColumn(packet, column));
                first = false;
        }

        this.writer.flush();

        logger.log(Level.INFO, "An event has been written to the file "
                               + this.outputFile.getAbsolutePath()
                               + " by the module " + this.getName());
        logger.exiting(this.getClass().getName(), "write");
    }

    /**
     * Returns value of column name from packet. The value is taken from
     * the event's document via ECGParser#getSingleNodeValue(). Special
     * columns are: TYPE_COLUMN and TIMESTAMP_COLUMN
     * 
     * @param packet The event which should be read
     * @param column The name of the column
     * @return returns event's value of column 
     */
    private String getValueOfColumn(ValidEventPacket packet, String column) {

        String[] columns = column.split("/");
        for (int i = 0; i < columns.length; i++) {

            String[] concats = columns[i].split("#");
            String results[] = new String[concats.length];
            boolean hasNoValue = false;
            for (int j = 0; !hasNoValue && j < concats.length; j++) {

                if (concats[j].equals(TYPE_COLUMN)) {
                    results[j] = packet.getMicroSensorDataType().getName();
                } else if (concats[j].equals(TIMESTAMP_COLUMN)) {
                    results[j] = dateFormat.format(packet.getTimeStamp());
                } else { 
                    Document document = packet.getDocument();
                    try {
                        results[j] = ECGParser.getSingleNodeValue(concats[j], document);
                        if (results[j] == null)
                            results[j] = "";
                    } catch (NodeException e) {
                        results[j] = null;
                        hasNoValue = true;
                    }
                }                
            }
            
            if (!hasNoValue) {
                String result = "";
                for (int j = 0; j < concats.length; j++) {
                    result += (results[j] != null ? results[j] : "");
                    if (j < concats.length-1)
                        result += "#";
                }
                return result;
            }
            
        }
        return "";
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

            File propertyValueFile = new File(moduleProperty.getValue());

            this.outputFile = propertyValueFile;

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

        } else if (moduleProperty.getName().equals("Columns")) {
            logger.log(Level.INFO, "Request to set the property: "
                                   + moduleProperty.getName());
            this.setColumns(moduleProperty.getValue());

        } else if (moduleProperty.getName().equals("Delimiter")) {
            logger.log(Level.INFO, "Request to set the property: "
                                   + moduleProperty.getName());
            this.setDelimiter(moduleProperty.getValue());

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

    }

    /**
     * Sets the delimiter for printing the table.
     * 
     * @param dl delimiter
     */
    private void setDelimiter(String dl) {
        this.delimiter = dl;
    }

    /**
     * Parses the Columns property of the module.
     * 
     * @param cols List of comma seperated column names
     */
    private void setColumns(final String cols) {

        this.columns = new ArrayList<String>(); 

        StringTokenizer st = new StringTokenizer(cols, ",");
        while (st.hasMoreTokens()) {
            columns.add(st.nextToken());
        }

    }

	/**
     * @see org.electrocodeogram.module.target.TargetModule#startWriter()
     */
    @SuppressWarnings("unused")
    @Override
    public final void startWriter() throws TargetModuleException {

        // currently no support for inline server and default location
        
        /*
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

            this.logDir = new File(this.homeDir + File.separator + LOG_SUBDIR);

            if (!this.logDir.exists()) {
                this.logDir.mkdir();
            }

            String outputFileName = DEFAULT_FILENAME_PREFIX
                                    + DEFAULT_FILENAME_SUFFIX;

            this.outputFile = new File(this.logDir.getAbsolutePath()
                                       + File.separator + outputFileName);

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
        logger.exiting(this.getClass().getName(), "startWriter");
        */
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
