/*
 * Class: SensorShell
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.hackystat.kernel.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.client.EventPacketQueueOverflowException;
import org.electrocodeogram.client.IllegalHostOrPortException;
import org.electrocodeogram.client.EventSender;
import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.hackystat.kernel.admin.SensorProperties;

/**
 * This is the <em>ECG SensorShell</em>. Its fq-name is
 * <em>org.hackystat.kernel.shell.SensorShell</em> just like the
 * class provided by the <em>HackyStat Project</em> (please visit <a
 * href="http://www.hackystat.org">www.hackystat.org</a> for more
 * information).<br>
 * Its shall be used by sensors from the ECG and the
 * <em>Hackystat</em> system and so this class has the same
 * signature as the <em>Hackystat
 * SensorShell</em>. The
 * <em>SensorShell's</em> purpose is to send recorded events ECG
 * Lab, where they are analysed and stored. A sensor developer does
 * not have to implement the functionality of sending data to the ECG
 * Lab by himself. Because the ECG shall directly support the use of
 * original HackyStat sensors, this class acts like the original
 * <em>Hackystat SensorShell</em> class. That means that every
 * original <em>Hackystat</em> sensor is able to collect data for
 * the ECG by simply replacing the <em>Hackystat's</em>
 * sensorshell.jar library with the ECG sensorshell.jar. Sensors are
 * able to use this class in to different ways depending on the sensor
 * programming language. If the sensor is written in Java, this class
 * can be referenced as a <em>SensorShell</em>-Object. On the
 * SensorShell object the method
 * {@link #doCommand(Date, String, List)} is called to send recorded
 * data to the ECG Lab. To support even sensors that are not written
 * in Java, this class provides a main method that makes it a process
 * that communicates via standard-input and -output. The process is
 * created via a "java -jar sensorshell.jar" command at the operating
 * system level. Every string that is passed to the standard-input of
 * the SensorShell process, is taken for collected data.
 */
public class SensorShell {

    /**
     * The default toolname.
     */
    private static final String ECG_TOOLNAME = "ElectroCodeoGram";

    /**
     * Key to pass the server configuration (path, port, type, ip) through
     * doCommand()
     */
    public static final String Initialization = "Initialization command";

    /**
     * The SensorShell can either run in <em>Inlinesever</em> mode,
     * where the ECG Lab is started automatically by the SensorShell,
     * or in <em>Remoteserver</em> mode, where the ECG Lab is
     * started manually and can be located on a different machine.
     */
    public enum ServerMode {
        // Never change the constant names since their toString() is used!
        /**
         * In the <em>Nullserver</em> mode the SensorShell doesn't
         * start the ECG Lab, i.e. no events are stored.
         */
        NULL,

        /**
         * In the <em>Inlineserver</em> mode the SensorShell starts
         * the ECG Lab automatically, by calling the
         * <code>ecg.bat</code> or <code>ecg.sh</code> scripts.
         * These scripts can be used to configure how to start the ECG
         * Lab.
         */
        INLINE,

        /**
         * In the <em>Remoteserver</em> mode the Sensoorshell
         * expects the ECG Lab to be started and waiting for incoming
         * connections. The IP-Address/TCP-Port of the ECG Lab are
         * read from the <em>sensor.properties</em> file.
         */
        REMOTE
    }

    /**
     * The actual server mode.
     */
    private ServerMode serverMode;

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper.createLogger(SensorShell.class
        .getName());

    /**
     * This is a reference to the <em>SensorProperties</em> object,
     * that contains the information of the <em>sensor.properties</em>
     * file. The file is used to configure all sensors and the
     * SensorShell in the system.
     */
    private SensorProperties sensorProperties;

    /**
     * Used to read from <em>STDIN</em>, when started via
     * {@link #main(String[])}.
     */
    private BufferedReader bufferedReader;

    /**
     * The line separator.
     */
    private String cr = System.getProperty("line.separator");

    /**
     * The prompt for <em>STDOUT</em>.
     */
    private String prompt = "SensorShell >> ";

    /**
     * Used to separate event string-segments.
     */
    private String delimiter = "#";

    /**
     * A flag indicating if the <em>SensorShell</em> is started via
     * {@link #main(String[])} or created as an object-instance.
     */
    private boolean isInteractive;

    /**
     * This is a reference to the <code>Thread</code> that is
     * sending the recorded events to the ECG Lab.
     */
    private EventSender eventSender = null;

    /**
     * This is the absolute path to the ECG EclipseSensor plugin.
     */
    private String sensorPath;

    /**
     * This is the IP address of the ECG lab server with the SocketSourceModule.
     */
    private String ecgLabIp;

    /**
     * This is the port number of the ECG lab server with the SocketSourceModule.
     */
    private String ecgLabPort;

    /**
     * A flag that indicates if the <em>SensorShell</em> is able to
     * receive recorded events.
     */
    private boolean acceptEvents;

    /**
     * A reference to the <code>Thread</code> starting the ECG Lab
     * in <em>Inlineserver</em> mode.
     */
    private static InlineServer inlineServer;

    /**
     * This creates the <em>ECG SensorShell</em>. Note that for full ECG sensor shell
     * functionality, it is necessary to call initializeCommuniaction() (which decides on
     * null, inline or remote mode) and initializeLogging() afterwards.
     * 
     * @param properties
     *            The <em>SensorProperties</em> configuring the ECG
     *            SensorShell
     * @param interactive
     *            Is <code>true</code> if the SensorShell is run as
     *            a process and <code>false</code> if it is created
     *            as an object-instance
     * @param toolName
     *            Is the name of the environment the sensor runs in
     */
    public SensorShell(final SensorProperties properties,
        final boolean interactive,
        final String toolName) {
        logger.entering(this.getClass().getName(), "SensorShell", new Object[] {
            properties, new Boolean(interactive), toolName});

        this.acceptEvents = true;

        if (properties == null) {
            logger.log(Level.SEVERE,
                "The parameter \"SensorProperties\" is null. Aborting...");

            return;
        }

        this.sensorProperties = properties;

        this.isInteractive = interactive;
        
        // For this special ECG Shell, in case of an Eclipse Sensor
        //  do not do the usual initialization now. It will be called
        //  via doCommand later.
        // TODO This is a dirty trick (especially because of the
        //  "eclipse" literal which is defined by Hackystat but
        //  not available here
        if (!toolName.equals("eclipse") && 
            !toolName.equals(SensorShell.ECG_TOOLNAME)) {
            initializeCommunication();
            initializeLogging();
        }

        logger.exiting(this.getClass().getName(), "SensorShell");
    }

    /**
     * This creates the <em>ECG SensorShell</em>.
     * @param properties
     *            The <em>SensorProperties</em> configuring the ECG
     *            SensorShell
     * @param interactive
     *            Is <code>true</code> if the SensorShell is run as
     *            a process and <code>false</code> if it is created
     *            as an object-instance
     */
    public SensorShell(final SensorProperties properties,
        final boolean interactive) {
        this(properties, interactive, ECG_TOOLNAME);
    }

    /**
     * This creates the <em>ECG SensorShell</em>.
     * @param properties
     *            The <em>SensorProperties</em> configuring the ECG
     *            SensorShell
     * @param interactive
     *            Is <code>true</code> if the SensorShell is run as
     *            a process and <code>false</code> if it is created
     *            as an object-instance
     * @param toolName
     *            Is the name of the environment the sensor runs in
     * @param enableOfflineData
     *            Is not used in this implementation
     * @param commandFile
     *            Is not used in this implementation
     */
    public SensorShell(final SensorProperties properties,
        final boolean interactive, final String toolName,
        final boolean enableOfflineData, 
        final File commandFile) {
        this(properties, interactive, toolName);
    }

    /**
     * This creates the <em>ECG SensorShell</em>.
     * @param properties
     *            The <em>SensorProperties</em> configuring the ECG
     *            SensorShell
     * @param interactive
     *            Is <code>true</code> if the SensorShell is run as
     *            a process and <code>false</code> if it is created
     *            as an object-instance
     * @param toolName
     *            Is the name of the environment the sensor runs in
     * @param enableOfflineData
     *            Is not used in this implementation
     */
    public SensorShell(final SensorProperties properties,
        final boolean interactive, final String toolName,
        final boolean enableOfflineData) {
        this(properties, interactive, toolName);
    }

    /**
     * Reads the <em>sensor.properties</em> file and sets up the
     * <em>EventSender</em>. If the SensorShell is in
     * <em>Inlineserver</em> mode, this method also creates the
     * {@link SensorShell.InlineServer}.
     */
    private void initializeCommunication() {
        logger.entering(this.getClass().getName(), "initializeCommunication");

        if (this.sensorProperties.getECGServerType() == null) {
            logger.log(Level.WARNING,
                "ECG_SEVER_TYPE is not specified.");

            this.serverMode = ServerMode.REMOTE;

            logger.log(Level.WARNING, "Going Remoteserver per default...");

            initializeEventSender();
        } else if (this.sensorProperties.getECGServerType()
            .equals(ServerMode.INLINE.toString())) {
            logger.log(Level.FINE,
                "The ECG_SEVER_TYPE is INLINE in sensor.properties.");

            this.serverMode = ServerMode.INLINE;

            logger.log(Level.FINE, "Going Inlineserver...");

            startInlineServer(); 
            initializeEventSender();
            
        } else if (this.sensorProperties.getECGServerType()
            .equals(ServerMode.NULL.toString())) {
            logger.log(Level.FINE, "ECG_SEVER_TYPE is NULL");

            this.serverMode = ServerMode.NULL;

            // dont do anything
                
        } else if (this.sensorProperties.getECGServerType()
            .equals(ServerMode.REMOTE.toString())) {
            logger.log(Level.FINE, "ECG_SEVER_TYPE is REMOTE Remoteserver...");
            
            initializeEventSender();
            
        } else {
            logger.log(Level.WARNING,
                "ECG_SEVER_TYPE is not specified.");

            logger.log(Level.WARNING, "Going Remoteserver per default...");

            this.serverMode = ServerMode.REMOTE;

            initializeEventSender();
        }

        logger.exiting(this.getClass().getName(), "initializeCommunication");
    }

    /**
     * Creates the <em>EventSender</em>, which is a
     * <code>Thread</code> that sends recorded events
     * asynchroneously to the ECG Lab.
     */
    private boolean initializeEventSender() {
        logger.entering(this.getClass().getName(), "initializeEventSender");

        try {
            // Try to create the SendingThread with the given ECG
            // Lab address
            
            if (this.eventSender != null) {
                // appempt to initializr Event Sender again, kill the first one
                this.eventSender.disconnect();
            }
            if (this.serverMode == ServerMode.NULL) {
                // dont start eventSender
            }
            if (this.serverMode == ServerMode.INLINE) {
                this.eventSender = new EventSender(InetAddress
                    .getByName("127.0.0.1"), this.sensorProperties
                    .getECGServerPort());
            } else {
                this.eventSender = new EventSender(this.sensorProperties
                    .getECGServerAddress(), this.sensorProperties
                    .getECGServerPort());
            }
        } catch (IllegalHostOrPortException e) {
            logger
                .log(
                    Level.SEVERE,
                    "The ECG Lab's address is invalid.\nPlease check the ECG_SERVER_ADDRESS and ECG_SERVER_PORT values in the file \".hackystat/sensor.properties\" in your home directory.");

            logger.exiting(this.getClass().getName(), "initializeEventSender");

            return false;

        } catch (UnknownHostException e) {
            logger
                .log(
                    Level.SEVERE,
                    "The ECG Server's address is invalid.\nPlease check the ECG_SERVER_ADDRESS and ECG_SERVER_PORT values in the file \".hackystat/sensor.properties\" in your home directory.");

            logger.exiting(this.getClass().getName(), "initializeEventSender");

            return false;
        }

        logger.exiting(this.getClass().getName(), "initializeEventSender");
        
        return true;
    }

    /**
     * This creates the <code>Thread</code>, which starts the ECG
     * Lab process.
     */
    private void startInlineServer() {
        logger.entering(this.getClass().getName(), "startInlineServer");

        if (inlineServer == null) {
            inlineServer = new InlineServer(this);
            inlineServer.start();            
        }

        logger.exiting(this.getClass().getName(), "startInlineServer");
    }

    /**
     * This stops the ECG SensorShell and the ECG Lab process, if the
     * SensorShell is running in <em>Inlineserver</em> mode.
     */
    public static void stop() {

        logger.entering(SensorShell.class.getName(), "stop");

        logger.log(Level.FINE, "The ECG SensorShell is stopping...");

        if (inlineServer != null) {
            logger.log(Level.FINE, "An ECG Lab process is found.");

            inlineServer.stopECGServer();

            logger.log(Level.FINE,
                "The stop command has been sent to the ECG Lab process.");
        }

        logger.log(Level.FINE, "NO ECG Lab process is found.");

        logger.exiting(SensorShell.class.getName(), "stop");
    }

    /**
     * This sets up the logging with parameters read from the
     * <em>sensor.properties</em> file.
     */
    private void initializeLogging() {

        logger.entering(this.getClass().getName(), "initializeLogging");

        String logFile = this.sensorProperties.getProperty("ECG_LOG_FILE");

        if (logFile != null) {
            try {
                LogHelper.setLogFile(logFile);
            } catch (SecurityException e) {
                logger.log(Level.SEVERE,
                    "An error occured while creating the logfile." + logFile);

                logger.log(Level.SEVERE, e.getMessage());

                logger.exiting(this.getClass().getName(), "initializeLogging");

            } catch (IOException e) {
                logger.log(Level.SEVERE,
                    "An error occured while creating the logfile." + logFile);

                logger.log(Level.SEVERE, e.getMessage());

                logger.exiting(this.getClass().getName(), "initializeLogging");
            }
        }

        Level logLevel = LogHelper.getLogLevel(this.sensorProperties
            .getProperty("ECG_LOG_LEVEL"));

        LogHelper.setLogLevel(logLevel);

        logger.exiting(this.getClass().getName(), "initializeLogging");

    }

    /**
     * This method is called by the ECG sensors that are having a
     * reference to a SensorShell object. Whenever they record an
     * event they call the this method and pass the parameters
     * according to the <em>HackyStat SensorDataType</em> and
     * <em>ECG
     * MicroSensorDataType</em> definitions.
     * @param timeStamp
     *            The timeStamp of the event
     * @param sensorDataType
     *            The <em>HackyStat SensorDataType</em> name of the
     *            event
     * @param argList
     *            The stringlist of the event's data
     * @return <code>true</code> if the event's data is
     *         syntactically correct and <code>false</code>
     *         otherwise
     */
    public final boolean doCommand(final Date timeStamp,
        final String sensorDataType, final List argList) {
        logger.entering(this.getClass().getName(), "doCommand", new Object[] {
            timeStamp, sensorDataType, argList});

        boolean result;

        if (!this.acceptEvents) {

            logger.log(Level.FINE,
                "The SensorShell is currently not accepting new events.");

            logger.exiting(this.getClass().getName(), "doCommand", new Boolean(
                "false"));

            return false;
        }

        // Hook for ecg to enable late configuration settings
        if (sensorDataType != null && sensorDataType.equals(Initialization)) {

            logger.log(Level.FINE,
                "The SensorShell is receiving the server configuration.");

            initializeCommunication();
            initializeLogging();

            logger.exiting(this.getClass().getName(), "doCommand");

            return true;

        }

        logger.log(Level.FINE, "The SensorShell is getting an event.");

        result = analyseEvent(timeStamp, sensorDataType, argList);

        logger.exiting(this.getClass().getName(), "doCommand", new Boolean(
            result));

        return result;

    }

    /**
     * If the Sensorshell is getting an event, this method is checking
     * if the event is wellformed. Only {@link WellFormedEventPacket}
     * events are send to the ECG Lab.
     * @param timeStamp
     *            The timestamp of the event
     * @param sensorDataType
     *            The <em>HackyStat SensordataType</em> of the event
     * @param argList
     *            The stringlist with the event data
     * @return <code>true</code> id the event is wellformed and
     *         <code>false</code> otherwise
     */
    private boolean analyseEvent(final Date timeStamp,
        final String sensorDataType, final List argList) {
        logger.entering(this.getClass().getName(), "analyseEvent",
            new Object[] {timeStamp, sensorDataType, argList});

        if (this.serverMode == ServerMode.NULL) {
            logger.log(Level.FINE,
                "Server type is NULLSERVER, event will be ignored.");
            logger.exiting(this.getClass().getName(), "analyseEvent",
                new Boolean(true));
            return true;            
        }

        WellFormedEventPacket packet;
        
        try {
            packet = new WellFormedEventPacket(timeStamp, sensorDataType, argList);
        } catch (IllegalEventParameterException e) {
            logger.log(Level.FINE,
                "An error occured while getting an event from the sensor.");

            logger.exiting(this.getClass().getName(), "analyseEvent",
                new Boolean(false));

            return false;
        }

        if (this.eventSender != null) {
            // pass EventPacket to SendingThread
            try {
                this.eventSender.addEventPacket(packet);
            } catch (EventPacketQueueOverflowException e) {
                logger.log(Level.SEVERE,
                    "A QueueOverflowException has occured.");

                this.acceptEvents = false;
            }

            logger.log(Level.FINE,
                "An event has been passed to the EventSender.");

        } else {
            logger
                .log(
                    Level.WARNING,
                    "The stream to the ECG Lab is not open. Maybe the EventSender is not started yet?");
        }
        logger.exiting(this.getClass().getName(), "analyseEvent");

        return true;
    }

    // Here starts the set of methods that are only declared for
    // compatibility reasons with HackyStat sensors

    /**
     * This method returns the <em>SensorProperties</em> that are
     * declared in the <em>sensor.properties</em> file.
     * @return The <em>SensorProperties</em>
     */
    public final SensorProperties getSensorProperties() {
        logger.entering(this.getClass().getName(), "getSensorProperties");

        logger.exiting(this.getClass().getName(), "getSensorProperties",
            this.sensorProperties);

        return this.sensorProperties;
    }

    /**
     * This method is <em>not implemented</em> and only declared for
     * compatibility reasons.
     * @return Is allways <code>true</code> to to indicate the
     *         <em>HackyStat</em> sensors that sending was
     *         successfull
     */
    public final boolean send() {
        logger.entering(this.getClass().getName(), "send");

        logger.exiting(this.getClass().getName(), "send", new Boolean(true));

        return true;
    }

    /**
     * This method is <em>not implemented</em> and only declared for
     * compatibility reasons.
     * @param str
     *            not used
     */
    public final void println(@SuppressWarnings("unused")
    final String str) {
        logger.entering(this.getClass().getName(), "println");

        logger.exiting(this.getClass().getName(), "println");

        return;
    }

    /**
     * This method is <em>not implemented</em> and only declared for
     * compatibility reasons.
     * @return Is always <code>true</code> to tell <em>HacyStat</em>
     *         sensors that the server is pingable
     */
    public final boolean isServerPingable() {
        logger.entering(this.getClass().getName(), "isServerPingable");

        logger.exiting(this.getClass().getName(), "isServerPingable",
            new Boolean(true));

        return true;
    }

    /**
     * This method is <em>not implemented</em> and only declared for
     * compatibility reasons.
     * @param milliSecondsToWait
     *            Is not used
     * @return Is always <code>true</code> to tell
     *         <em>HackyStat</em> sensors that the server is
     *         pingable
     */
    public final boolean isServerPingable(@SuppressWarnings("unused")
    final int milliSecondsToWait) {
        logger.entering(this.getClass().getName(), "isServerPingable");

        logger.exiting(this.getClass().getName(), "isServerPingable",
            new Boolean(true));

        return true;
    }

    /**
     * This method is <em>not implemented</em> and only declared for
     * compatibility reasons.
     * @return An empty <code>String</code>
     */
    public final String getResultMessage() {
        logger.entering(this.getClass().getName(), "getResultMessage");

        logger.exiting(this.getClass().getName(), "getResultMessage", "");

        return "";
    }

    // End of set

    /**
     * Prints out a line to the prompt.
     * @param line
     *            Is the line to print out
     */
    private void print(final String line) {

        logger
            .entering(this.getClass().getName(), "print", new Object[] {line});

        if (this.isInteractive) {
            System.out.print(line);
        }

        logger.exiting(this.getClass().getName(), "print");
    }

    /**
     * Reads in a line from <em>SDTIN</em>.
     * @return The line
     */
    private String readLine() {
        logger.entering(this.getClass().getName(), "readLine");

        try {
            String line = this.bufferedReader.readLine();

            logger.log(Level.INFO, line + this.cr);

            logger.exiting(this.getClass().getName(), "readLine", line);

            return line;
        } catch (IOException e) {
            logger.exiting(this.getClass().getName(), "readLine", "quit");

            return "quit";

        }

    }

    /**
     * Prints out the prompt.
     */
    private void printPrompt() {
        logger.entering(this.getClass().getName(), "printPrompt");

        this.print(this.prompt);

        logger.exiting(this.getClass().getName(), "printPrompt");
    }

    /**
     * Quits the <em>SensorShell</em>.
     */
    private void quit() {
        logger.entering(this.getClass().getName(), "quit");

        logger.log(Level.INFO, "Exiting SensorShell...");

        logger.exiting(this.getClass().getName(), "quit");

        System.exit(0);

    }

    /**
     * Returns the absolute path to the ECG EclipseSensor plugin.
     * @return The absolute path to the ECG EclipseSensor plugin
    final String getSensorPath() {
        return this.sensorPath;
    }
     */

    /**
     * This makes the <em>SensorShell</em> a process that
     * continuously reads from <em>SDTIN</em>. Every
     * <code>String</codE> that is written to its
     * <em>SDTIN</em> is handled as event data.
     * @param args
     *            The first parameter shall be the toolname
     *            and the second shall be the path to the
     *            <em>sensor.properties</em> file.
     */
    public static void main(final String[] args) {

        logger.entering(SensorShell.class.getName(), "main", args);

        Thread
            .setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());

        logger.log(Level.FINE, "Registered DefaultExceptionHandler");

        if ((args.length == 1) && (args[0].equalsIgnoreCase("-help"))) {
            logger.log(Level.INFO, "Help requested by user");

            System.out
                .println("java -jar sensorshell.jar [toolname] [sensor.properties]");

            logger.exiting(SensorShell.class.getName(), "main");

            return;
        }

        SensorProperties sensorProperties;

        if (args.length >= 2) {
            sensorProperties = new SensorProperties("Shell", new File(args[1]));

            logger.log(Level.FINE, "Loaded SensorProperties from file.");

            if (!sensorProperties.isFileAvailable()) {
                System.out.println("Could not find sensor.properties file. ");

                System.out.println("Expected in: "
                                   + sensorProperties.getAbsolutePath());

                logger.exiting(SensorShell.class.getName(), "main");

                return;
            }
        } else {
            sensorProperties = new SensorProperties("ECG SensorShell");

            logger.log(Level.FINE, "Loaded default SensorProperties.");
        }

        boolean interactive = true;

        SensorShell shell = new SensorShell(sensorProperties, interactive, "");

        logger.log(Level.INFO, "Created ECG SensorShell.");

        while (true) {

            shell.printPrompt();

            String inputString = shell.readLine();

            // Quit if necessary.
            if (inputString != null && inputString.equalsIgnoreCase("quit")) {
                shell.quit();

                return;
            }

            StringTokenizer tokenizer = new StringTokenizer(inputString,
                shell.delimiter);

            int numTokens = tokenizer.countTokens();

            if (numTokens == 0) {
                continue;
            }

            String commandName = tokenizer.nextToken();

            ArrayList<String> argList = new ArrayList<String>();

            while (tokenizer.hasMoreElements()) {
                argList.add(tokenizer.nextToken());
            }

            shell.doCommand(new Date(), commandName, argList);

            logger.exiting(SensorShell.class.getName(), "main");
        }

    }

    /**
     * This <code>Thread</code> is used to read the output of the
     * ECG Lab process. The process output must be read to avoid
     * blocking of the processs.
     */
    private static class StreamGobbler extends Thread {

        /**
         * This is the logger.
         */
        private Logger streamGobblerLogger = LogHelper
            .createLogger(StreamGobbler.class.getName());

        /**
         * The <em>Stream</em> to read from.
         */
        private InputStream inputStream;

        /**
         * Is either <code>ERROR</code>, when the gobbler is
         * reading the <em>STDERR</em>, or <em>OUTPUT</em>, when
         * the gobbler is reading the <em>STDOUT</em>.
         */
        private String gobblerType;

        /**
         * Creates the gobbler.
         * @param is
         *            The <em>Stream</em> to read from
         * @param type
         *            not used
         */
        StreamGobbler(final InputStream is, final String type) {
            this.streamGobblerLogger.entering(this.getClass().getName(),
                "StreamGobbler");

            this.inputStream = is;

            this.gobblerType = type;

            this.streamGobblerLogger.exiting(this.getClass().getName(),
                "StreamGobbler");
        }

        /**
         * Reads the output of the ECG Lab process continously.
         */
        @Override
        public void run() {
            this.streamGobblerLogger.entering(this.getClass().getName(), "run");

            try {
                InputStreamReader isr = new InputStreamReader(this.inputStream);

                BufferedReader br = new BufferedReader(isr);

                String line = null;

                while ((line = br.readLine()) != null) {
                    this.streamGobblerLogger.log(Level.FINEST, this.gobblerType
                                                               + ">" + line);
                }
            } catch (IOException e) {
                this.streamGobblerLogger.log(Level.SEVERE,
                    "An error occured while reading from the ECG Lab process.");

                this.streamGobblerLogger.log(Level.SEVERE, e.getMessage());
            }

            this.streamGobblerLogger.exiting(this.getClass().getName(), "run");
        }

    }

    /**
     * This is the implementation of an <code>Exceptionhandler</code>
     * to catch all uncaught <code>(Runtime)Exceptions</code>.
     */
    private static class DefaultExceptionHandler implements
        UncaughtExceptionHandler {

        /**
         * This is the logger.
         */
        private static Logger defaultExceptionHandlerLogger = LogHelper
            .createLogger(DefaultExceptionHandler.class.getName());

        /**
         * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread,
         *      java.lang.Throwable)
         */
        public void uncaughtException(final Thread t, final Throwable e) {
            defaultExceptionHandlerLogger.entering(SensorShell.class.getName(),
                "uncaughtException");

            java.lang.System.out.println("An uncaught Exception had occured:");

            java.lang.System.out.println("Thread:" + t.getName());

            java.lang.System.out.println("Class: " + t.getClass());

            java.lang.System.out.println("State: " + t.getState());

            java.lang.System.out.println("Message: " + e.getMessage());

            java.lang.System.out.println("StackTrace: ");

            e.printStackTrace();

            defaultExceptionHandlerLogger.exiting(SensorShell.class.getName(),
                "uncaughtException");
        }

    }

    /**
     * This <code>Thread</code> creates the ECG Lab as a
     * <code>Process</code> if the <em>SensorShell</em> is running
     * in <em>Inlineserver</em> mode.
     */
    private static class InlineServer extends Thread {

        /**
         * This is the logger.
         */
        private static Logger inlineServerLogger = LogHelper
            .createLogger(InlineServer.class.getName());

        /**
         * Before the ECG Lab can be started, the <em>SensorShell</em>
         * needs to know the path to it. The ECG EclipseSensor plugin
         * knows the path and passes it to the <em>SensorShell</em>.
         * This constant tells how long this <code>Thread</code>
         * shall wait before looking if the path to the ECG Lab is
         * already passed from the ECG EclipseSensor.
         */
        private static final int WAIT_FOR_PATH_DELAY = 1000;

        /**
         * A reference to the ECG Lab <code>Process</code>.
         */
        private Process ecgLab;

        /**
         * A reference to the <em>SensorShell</em>.
         */
        private SensorShell shell;

        /**
         * Creates the <code>Thread</code>.
         * @param properties
         *            Is a reference to the <em>SensorProperties</em>
         */
        public InlineServer(SensorShell shell) {
            inlineServerLogger.entering(this.getClass().getName(),
                "InlineServer", new Object[] {shell});

            this.shell = shell;

            inlineServerLogger.exiting(this.getClass().getName(),
                "InlineServer");
        }

        /**
         * This is called to stop the ECG Lab <code>Process</code>
         * when the <em>SensorShell</em> stops.
         */
        public void stopECGServer() {

            inlineServerLogger.entering(this.getClass().getName(),
                "stopECGServer");

            if (this.ecgLab == null) {
                inlineServerLogger.log(Level.WARNING,
                    "The ECG Lab process is already null.");

                inlineServerLogger.exiting(this.getClass().getName(),
                    "stopECGServer");

                return;
            }

            OutputStream os = this.ecgLab.getOutputStream();

            PrintWriter writer = new PrintWriter(new OutputStreamWriter(os));

            inlineServerLogger
                .log(Level.WARNING, "Stream to ECG Lab established.");

            writer.println("quit");

            writer.flush();

            inlineServerLogger.log(Level.WARNING,
                "The quit-command has been sent.");

            inlineServerLogger.exiting(this.getClass().getName(),
                "stopECGServer");
        }

        /**
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            inlineServerLogger.entering(this.getClass().getName(), "run");

            String labPath = this.shell.getSensorProperties().getECGServerPath();

            while (labPath == null || labPath.equals("") || !(new File(labPath).exists())) {

                try {
                    inlineServerLogger.log(Level.WARNING,
                        "Path to ECG Lab not resolved yet. Waiting for "
                                        + WAIT_FOR_PATH_DELAY / 1000
                                        + " seconds ...");

                    Thread.sleep(WAIT_FOR_PATH_DELAY);

                    // refetch path
                    labPath = this.shell.getSensorProperties().getECGServerPath(); 
                
                } catch (InterruptedException e) {
                    inlineServerLogger.log(Level.SEVERE,
                        "Path to ECG Lab not received yet.");

                    inlineServerLogger.log(Level.SEVERE,
                        "Unable to start ECG Lab as InlineServer.");

                    inlineServerLogger
                        .exiting(this.getClass().getName(), "run");

                    return;
                }
            }

            try {
                ProcessBuilder pb = null;
    
                File ecgDir = new File(labPath);
                String shellCmd = null;
                
                // ecgDir could be the shell command directly or the directory of it
                if (ecgDir.isFile()) {
                    shellCmd = ecgDir.getName();
                    ecgDir = ecgDir.getParentFile();
                }
    
                String osName = System.getProperty("os.name");
    
                if (osName == null || osName.equals("")) {
                    inlineServerLogger.log(Level.SEVERE,
                        "The operating system name could not be read from the system environment. Aborting Inlineserver startup...");
    
                    inlineServerLogger.exiting(this.getClass().getName(),
                        "run");
    
                    return;
                }
    
                inlineServerLogger.log(Level.FINE,
                    "The operating system name is: " + osName);
    
                if (osName.startsWith("Windows")) {
                    inlineServerLogger.log(Level.FINE,
                        "The operating system is a windows system.");
                    
                    if (shellCmd == null)
                        shellCmd = "ecg.bat";
    
                    pb = new ProcessBuilder("cmd.exe", "/C", shellCmd);
    
                    inlineServerLogger
                        .log(Level.FINE, "ECG Lab directory is: "
                                         + ecgDir.getAbsolutePath());
    
                } else if (osName.startsWith("Linux")) {
                    inlineServerLogger.log(Level.FINE,
                        "The operating system is a linux system.");
    
                    if (shellCmd == null)
                        shellCmd = "./ecg.sh";
    
                    pb = new ProcessBuilder("sh", shellCmd);
    
                    inlineServerLogger
                        .log(Level.FINE, "ECG Lab directory is: "
                                         + ecgDir.getAbsolutePath());
    
                } else {
                    inlineServerLogger
                        .log(Level.SEVERE,
                            "The operating system is an unknown system. Aborting Inlineserver startup...");
    
                    return;
                }
    
                pb.directory(ecgDir);
    
                this.ecgLab = pb.start();
    
                inlineServerLogger.log(Level.INFO,
                    "ECG Lab process started.");
    
                StreamGobbler errorGobbler = new StreamGobbler(this.ecgLab
                    .getErrorStream(), "ERROR");
    
                StreamGobbler outputGobbler = new StreamGobbler(this.ecgLab
                    .getInputStream(), "OUTPUT");
    
                errorGobbler.start();
    
                outputGobbler.start();
    
                inlineServerLogger.exiting(this.getClass().getName(), "run");

            } catch (IOException e) {

                inlineServerLogger
                    .log(Level.SEVERE, "Unable to start ECG Lab.");

                inlineServerLogger.log(Level.SEVERE, e.getMessage());
            }
            
        }
    }

}
