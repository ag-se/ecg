/*
 * FU Berlin, 2006
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
import org.electrocodeogram.misc.xml.ECGWriter;
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
     * TODO Don't use this, because it simply needs to be the seperator of the file! 
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
     * If true, a special msdt.system event to denote the end of a 
     * file is sent. Other modules may use this event type to
     * invoke finalization code. The lab in nogui mode will
     * terminate after processing this special event 
     */
    private boolean sendEndEvent = false;

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
        final ReadMode readMode, final boolean sendEndEvent) {
        super(sourceModule);

        this.mode = readMode;
        this.sendEndEvent = sendEndEvent;
        this.module = sourceModule;

        this.run = true;

    }

    /**
     * @see org.electrocodeogram.module.source.EventReader#read()
     */
    public final WellFormedEventPacket read() throws EventReaderException {

        logger.entering(this.getClass().getName(), "read");

        WellFormedEventPacket eventPacket = null;

        boolean cdatafragment = false;
        int lineNumber = 0;
        String line = null;
        String code = null;

        try {
            line = this.reader.readLine();

            if (line == null) {

                logger.log(Level.INFO,
                    "The read line is null. Assuming end of file.");
                
                if (this.sendEndEvent) {
                    String eventStr = "<microActivity><system><type>termination</type></system></microActivity>";
                    eventPacket = ECGWriter.createValidEventPacket("msdt.system.xsd", this.dateOfLastEvent, eventStr);
                    this.sendEndEvent = false;
                } else {
                    this.module.deactivate();
                }

                logger.exiting(this.getClass().getName(), "read", null);

                return eventPacket;
            }

            if (line.contains("<![CDATA") && !line.contains("]]>")) {

                cdatafragment = true;

                int beginOfCode = line.indexOf("<![CDATA");

                logger.log(Level.FINE,
                    "Begin of a CDATA section at index: "
                                    + beginOfCode);

                int endOfCode = 0;

                String nextLine;

                while ((nextLine = this.reader.readLine()) != null && this.run) {
                    line += NEW_LINE_CHAR + nextLine;

                    lineNumber++;

                    // There may be many consequtive CDATA sections
                    if (nextLine.contains("]]>") && !nextLine.contains("<![CDATA")) {

                        endOfCode = line.lastIndexOf("]]>");

                        logger.log(Level.FINE,
                            "CDATA section complete at index: " + endOfCode);

                        break;
                    }
                }

                if (endOfCode <= beginOfCode) {
                    logger.log(Level.WARNING, "Error while reading line "
                                              + lineNumber + ":");

                    logger.log(Level.WARNING,
                        "This line does not contain a valid CDATA section.");

                    cdatafragment = false;

                    return null;
                }

                code = line.substring(beginOfCode, endOfCode);

                String preCode = line.substring(0, beginOfCode);

                String postCode = line.substring(endOfCode, line.length());

                line = preCode + CODE_REPLACEMENT + postCode;

            }

            int timeIndex = line.indexOf('#');
            int datatypeIndex = line.indexOf('#', timeIndex+1);
            
            // Get the timestamp String.
            String timeStampString = line.substring(0, timeIndex);
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
            String sensorDataTypeString = line.substring(timeIndex+1, datatypeIndex);
            // Check if the SensorDataType String is well formed.
            if (sensorDataTypeString == null || sensorDataTypeString.equals("")) {
                logger.log(Level.WARNING,
                    "Error while reading SensorDataType in line " + lineNumber
                                    + ":");

                logger.log(Level.WARNING, "The SensorDataType is empty.");

                return null;
            }

            // Get the argList String.
            String argListString = line.substring(datatypeIndex+1);
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

            String[] argListStringArray = new String[3];
            int firstSemi = argListString.indexOf(';');
            int secondSemi = argListString.indexOf(';', firstSemi+1);
            int thirdSemi = argListString.indexOf(';', secondSemi+1);
            // ignore first (argListString.substring(0, firstSemi));
            argListStringArray[0] = argListString.substring(firstSemi+1, secondSemi);
            argListStringArray[1] = argListString.substring(secondSemi+1, thirdSemi);
            argListStringArray[2] = argListString.substring(thirdSemi+1);

            if (cdatafragment) {
                if (code == null || code.equals("")) {
                    logger.log(Level.WARNING, "Error while reading line "
                                              + lineNumber + ":");

                    logger.log(Level.WARNING,
                        "This line does not contain a valid CDATA section replacement.");

                    cdatafragment = false;

                    return null;
                }

                String withCodeReplacement = argListStringArray[argListStringArray.length - 1];

                if (!withCodeReplacement.contains(CODE_REPLACEMENT)) {
                    logger.log(Level.WARNING, "Error while reading line "
                                              + lineNumber + ":");

                    logger.log(Level.WARNING,
                        "This line does not contain a valid CDATA section replacement.");

                    cdatafragment = false;

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

                logger.log(Level.FINER, "Process real time mode.");

                // If this was the first line, then no previous
                // timestamp
                // has been stored.
                if (this.dateOfLastEvent != null) {
                    // Get the time delta of the last event and
                    // the current
                    // event.
                    long eventDelta = eventPacket.getTimeStamp().getTime()
                                      - this.dateOfLastEvent.getTime();

                    logger.log(Level.FINER, "  event delta: " + eventDelta);

                    // Get the current Date
                    Date currentDate = new Date();

                    // Get the delta of the time when the last
                    // event was
                    // parsed and now
                    long realDelta = currentDate.getTime()
                                     - this.relativeDate.getTime();

                    logger.log(Level.FINER, "  real Delta " + realDelta);

                    // Get the delta in real realtime and compare
                    // it to the
                    // event time delta.
                    while (realDelta < eventDelta
                           && this.mode != ReadMode.BURST && this.run) {

                        logger.log(Level.FINER, "  Need to wait");

                        try {
                            // Wait some time and retry until the
                            // time has
                            // ellapsed.
                            Thread.sleep(TIME_SPAN);

                            logger.log(Level.FINER, "  Waiting finished");

                            currentDate = new Date();

                            realDelta = currentDate.getTime()
                                        - this.relativeDate.getTime();

                            logger.log(Level.FINER, "  new real Delta " + realDelta);

                        } catch (InterruptedException e) {
                            // No problem if and interruption
                            // occures...
                        }
                    }

                    logger.log(Level.FINER, "  Do not need to wait");

                }

            } else {
                logger.log(Level.FINER, "Process burst mode.");
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

    /**
     * Set to true if a termination event should be sent
     * @param sendEndEvent
     */
    public void setSendEndEvent(boolean sendEndEvent) {
        this.sendEndEvent = sendEndEvent;
    }
}
