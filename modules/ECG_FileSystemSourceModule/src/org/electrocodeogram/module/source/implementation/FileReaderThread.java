/*
 * Class: FileReaderThread
 * Version 1.1
 * Date 17.03.2006
 * Fixed Bug #6632
 * 
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.source.implementation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.source.EventReader;
import org.electrocodeogram.module.source.EventReaderException;
import org.electrocodeogram.module.source.SourceModule;
import org.electrocodeogram.module.source.implementation.FileSystemSourceModule.ReadMode;

/**
 * This {@link org.electrocodeogram.module.source.EventReader} is used
 * by the {@link FileSystemSourceModule}. It reads events from a file
 * in the file system. Such a file is created when events are written
 * with the <code>FileSystemTargetModule</code>. The file format is
 * expected to be according to the <code>EventPacket#toString()</code>,
 * with one event per line.
 */
public class FileReaderThread extends EventReader {

	/**
	 * Holds the system dependent representation of a line separator. 
	 */
	private static String NEW_LINE_CHAR = System.getProperty("line.separator");
	
    /**
     * This <em>EventReader</em> makes use of <em>Tokenizers</em>
     * to cut a read line into pieces of event data. Some events like
     * the <em>Codechange</em> event can contain the
     * <em>Tokenizer</em> deliameters in its content. In this case
     * the content is masked with this constant <code>String</code>
     * and unmasked after tokenization.
     */
    private static final String CODE_REPLACEMENT = "CODE";

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper.createLogger(FileReader.class
        .getName());

    /**
     * This is the <em>SourceModule</em> to which read events are
     * passed. It is the {@link FileSystemSourceModule} in this case.
     */
    private SourceModule module;

    /**
     * What was the timestamp of the last event read in?
     */
    private Date dateOfLastEvent;

    /**
     * When was the last event read in?
     */
    private Date relativeDate;

    /**
     * A reference to the input file.
     */
    private File file;

    /**
     * This <em>BufferedReader</em> is used to read the file's
     * content linewise.
     */
    private BufferedReader reader;

    /**
     * This <em>EventReader</em> can either read events as fast as
     * possible, or it can read in the events in the same time as they
     * were written into the file.
     */
    private ReadMode mode;

    /**
     * The state of this <code>Thread</code>.
     */
    private boolean run;

    private static int TIME_SPAN = 100;

    /**
     * This creates the <em>FileReaderThread</em>.
     * @param sourceModule
     *            Is the SourceModule to which the events shall be
     *            passed
     * @param readMode
     *            Tells the <em>FileReaderThread</em> to run in
     *            either "BURST" or "REALTIME" mode
     */
    public FileReaderThread(final SourceModule sourceModule,
        final ReadMode readMode) {
        super(sourceModule);

        this.mode = readMode;

        this.module = sourceModule;

        this.run = true;

    }

    /**
     * This method sets the <em>ReadMode</em> for the
     * <em>FileReaderThread</em>.
     * @param readMode
     *            Is the ReadMode; either "BURST" or "REALTIME".
     */
    public final void setMode(final ReadMode readMode) {

        logger.entering(this.getClass().getName(), "setMode",
            new Object[] {readMode});

        this.mode = readMode;

        logger.exiting(this.getClass().getName(), "setMode");
    }

    /**
     * @see org.electrocodeogram.module.source.EventReader#read()
     */
    @Override
    public final WellFormedEventPacket read() throws EventReaderException {

        logger.entering(this.getClass().getName(), "read");

        WellFormedEventPacket eventPacket = null;

        boolean codechange = false;

        int lineNumber = 0;

        String line = null;

        String code = null;

        StringTokenizer eventTokenizer = null;

        try {
            line = this.reader.readLine();

            if (line == null) {

                logger.log(Level.INFO,
                    "The read line is null. Assuming end of file.");

                this.module.deactivate();

                logger.exiting(this.getClass().getName(), "read", null);

                return null;
            }

            if (line.contains("<codechange>")
                && !line.contains("</codechange>")) {

                codechange = true;

                int beginOfCode = line.indexOf("<![CDATA");

                logger.log(Level.FINE,
                    "Begin of a multiline Codechange event at index: "
                                    + beginOfCode);

                int endOfCode = 0;

                String nextLine;

                while ((nextLine = this.reader.readLine()) != null && this.run) {
                    line += nextLine + NEW_LINE_CHAR;

                    lineNumber++;

                    if (nextLine.contains("</codechange>")) {

                        endOfCode = line.lastIndexOf("</document>");

                        logger.log(Level.FINE,
                            "Codechange event complete at index: " + endOfCode);

                        break;
                    }
                }

                if (endOfCode <= beginOfCode) {
                    logger.log(Level.WARNING, "Error while reading line "
                                              + lineNumber + ":");

                    logger.log(Level.WARNING,
                        "This line does not contain a valid codechange event.");

                    codechange = false;

                    return null;
                }

                code = line.substring(beginOfCode, endOfCode);

                String preCode = line.substring(0, beginOfCode);

                String postCode = line.substring(endOfCode, line.length());

                line = preCode + CODE_REPLACEMENT + postCode;

            }

            // Get a new Tokenizer.
            eventTokenizer = new StringTokenizer(line,
                WellFormedEventPacket.EVENT_SEPARATOR);

            // Check if the line is well formed.

            int tokens = eventTokenizer.countTokens();
            if (tokens != 3) {
                logger.log(Level.WARNING, "Error while reading line "
                                          + lineNumber + ":");

                logger.log(Level.WARNING,
                    "This line does not contain valid event data.");

                return null;
            }

            // Get the timestamp String.
            String timeStampString = eventTokenizer.nextToken();

            // Check if the timestamp String is well formed.
            if (timeStampString == null || timeStampString.equals("")) {
                logger
                    .log(Level.WARNING,
                        "Error while reading timeStamp in line " + lineNumber
                                        + ":");

                logger.log(Level.WARNING, "The timeStamp is empty.");

                return null;
            }

            // Get the SensorDataType String.
            String sensorDataTypeString = eventTokenizer.nextToken();

            // Check if the SensorDataType String is well formed.
            if (sensorDataTypeString == null || sensorDataTypeString.equals("")) {
                logger.log(Level.WARNING,
                    "Error while reading SensorDataType in line " + lineNumber
                                    + ":");

                logger.log(Level.WARNING, "The SensorDataType is empty.");

                return null;
            }

            // Get the argList String.
            String argListString = eventTokenizer.nextToken();

            // Check if the argList String is well formed.
            if (argListString == null || argListString.equals("")) {
                logger.log(Level.WARNING,
                    "Error while reading argList in line " + lineNumber + ":");

                logger.log(Level.WARNING, "The argList is empty.");

                return null;
            }

            // Try to parse the timestamp String into a Date object.
            Date timeStamp = null;

            try {
                timeStamp = new SimpleDateFormat(
                    WellFormedEventPacket.DATE_FORMAT_PATTERN)
                    .parse(timeStampString);
            } catch (ParseException e) {

                logger
                    .log(Level.WARNING,
                        "Error while reading timeStamp in line " + lineNumber
                                        + ":");

                logger.log(Level.WARNING, "The timeStamp is invalid.");

                logger.log(Level.WARNING, e.getMessage());

                return null;
            }

            // This second level Tokenizer is used to dissasemble the
            // argList.
            StringTokenizer argListTokenizer = new StringTokenizer(
                argListString, WellFormedEventPacket.ARGLIST_SEPARATOR);

            // The Array is used to temprarilly store the argList
            // entries.
            String[] argListStringArray = new String[argListTokenizer
                .countTokens()];

            int i = 0;

            while (argListTokenizer.hasMoreTokens()) {
                argListStringArray[i++] = argListTokenizer.nextToken();
            }

            if (codechange) {
                if (code == null || code.equals("")) {
                    logger.log(Level.WARNING, "Error while reading line "
                                              + lineNumber + ":");

                    logger.log(Level.WARNING,
                        "This line does not contain a valid codechange event.");

                    codechange = false;

                    return null;
                }

                String withCodeReplacement = argListStringArray[argListStringArray.length - 1];

                if (!withCodeReplacement.contains(CODE_REPLACEMENT)) {
                    logger.log(Level.WARNING, "Error while reading line "
                                              + lineNumber + ":");

                    logger.log(Level.WARNING,
                        "This line does not contain a valid codechange event.");

                    codechange = false;

                    return null;
                }

                String withCode = withCodeReplacement.replace(CODE_REPLACEMENT, code);
                
                argListStringArray[argListStringArray.length - 1] = withCode;
            }

            // Create a List object from the Array now containing the
            // argList String entries.
            List argList = Arrays.asList(argListStringArray);

            // Try to create an EventPacket object from the line's data.

            try {
                eventPacket = new WellFormedEventPacket(timeStamp,
                    sensorDataTypeString, argList);
            } catch (IllegalEventParameterException e) {

                logger.log(Level.WARNING,
                    "Error while generating event from line " + lineNumber
                                    + ":");

                logger.log(Level.WARNING, e.getMessage());

                return null;
            }

            if (this.mode != ReadMode.BURST) {

                logger.log(Level.FINER, "bin im RT mode.");

                // If this was the first line, then no previous
                // timestamp
                // has been stored.
                if (this.dateOfLastEvent != null) {
                    // Get the time delta of the last event and
                    // the current
                    // event.
                    long eventDelta = eventPacket.getTimeStamp().getTime()
                                      - this.dateOfLastEvent.getTime();

                    logger.log(Level.FINER, "ed " + eventDelta);

                    // Get the current Date
                    Date currentDate = new Date();

                    // Get the delta of the time when the last
                    // event was
                    // parsed and now
                    long realDelta = currentDate.getTime()
                                     - this.relativeDate.getTime();

                    logger.log(Level.FINER, "rd " + realDelta);

                    // Get the delta in real realtime and compare
                    // it to the
                    // event time delta.
                    while (realDelta < eventDelta
                           && this.mode != ReadMode.BURST && this.run) {

                        logger.log(Level.FINER, "Muss warten");

                        try {
                            // Wait some time and retry until the
                            // time has
                            // ellapsed.
                            Thread.sleep(TIME_SPAN);

                            logger.log(Level.FINER, "Hab gewartet");

                            currentDate = new Date();

                            realDelta = currentDate.getTime()
                                        - this.relativeDate.getTime();

                            logger.log(Level.FINER, "neues rd " + realDelta);

                        } catch (InterruptedException e) {
                            // No problem if and interruption
                            // occures...
                        }
                    }

                    logger.log(Level.FINER, "Muss nicht warten");

                }

            } else {
                logger.log(Level.FINER, "Bin im Burst mode.");
            }

            // Store the timestamp of the current event for the
            // next loop.
            this.dateOfLastEvent = eventPacket.getTimeStamp();

            // Store the timestamp for the next loop.
            this.relativeDate = new Date();

        } catch (IOException e) {
            throw new EventReaderException(e.getMessage());
        }
        return eventPacket;

    }

    /**
     * Sets the file to write the events in.
     * @param f Is the new output file
     * @throws IOException If an exception occurs while openeing the new output file
     */
    public final void setInputFile(final File f) throws IOException {

        this.file = f;

        if (this.reader != null) {
            this.reader.close();
        }

        this.reader = new BufferedReader(new FileReader(this.file));

    }
}
