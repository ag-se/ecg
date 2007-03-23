/*
 * Class: LogHelper
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.logging;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * This static helper class is the centralized logging facility of the
 * <em>ElectroCodeoGram</em>. It uses the Java Logging-API to and
 * introduces different Loglevel names and an additional Loglevel to
 * log the content of events.<br>
 * Logging can be done into a logfile and to <em>SDTOUT</em>.
 */
public final class LogHelper {

    /**
     * This is the standard log-directory under the home-directory of
     * the user.
     */
    private static final String LOG_DIR = "ecg_log";

    /**
     * The default loglevel if non or a unknown loglevel is defined.
     */
    private static final Level DEFAULT_LEVEL = Level.WARNING;

    /**
     * How many files to keep for log rotation.
     */
    private static final int MAX_FILES = 50;

    /**
     * The logfiles are rotated after 10MB.
     */
    private static final int FILE_SIZE = 10 * 1024 * 1024;

    /**
     * This is how the Logelevel <em>DEBUG</em> is defined.
     */
    public static final String LEVEL_DEBUG = "debug";

    /**
     * This is how the Logelevel <em>PACKET</em> is defined.
     */
    public static final String LEVEL_PACKET = "packet";

    /**
     * This is how the Logelevel <em>VERBOSE</em> is defined.
     */
    public static final String LEVEL_VERBOSE = "verbose";

    /**
     * This is how the Logelevel <em>INFO</em> is defined.
     */
    public static final String LEVEL_INFO = "info";

    /**
     * This is how the Logelevel <em>WARNING</em> is defined.
     */
    public static final String LEVEL_WARNING = "warning";

    /**
     * This is how the Logelevel <em>ERROR</em> is defined.
     */
    public static final String LEVEL_ERROR = "error";

    /**
     * This is how the Logelevel <em>OFF</em> is defined.
     */
    public static final String LEVEL_OFF = "off";

    static {
        Logger rootLogger = Logger.getLogger("");

        Handler[] handlers = rootLogger.getHandlers();

        for (Handler handler : handlers) {
            handler.setFormatter(new ECGFormatter());
        }

    }

    /**
     * The constructor is hiidden for this utility class.
     */
    private LogHelper() {
    // empty
    }

    /**
     * This creates a new <em>Logger</em> instance with the given
     * name.
     * @param name
     *            Is the name for the <em>logger</em>, usually the
     *            name of the class in ECG
     * @return A new <em>Logger</em> instance
     */
    public static Logger createLogger(final String name) {
        Logger logger = Logger.getLogger(name);

        logger.setUseParentHandlers(true);

        return logger;
    }

    /**
     * This sets the loglevel for all <em>Logger</em>.
     * @param logLevel
     *            Is the new Loglevel
     */
    public static void setLogLevel(Level logLevel) {
        if (logLevel == null) {
            logLevel = DEFAULT_LEVEL;
        }

        Logger logger = Logger.getLogger("");

        logger.setLevel(logLevel);

        Handler[] handlers = Logger.getLogger("").getHandlers();

        for (Handler handler : handlers) {
            handler.setLevel(logLevel);

        }

    }

    /**
     * This method is called to disable all logger that are not
     * from the ElectroCodeoGram itself, but from the Java API and
     * other components.
     *
     */
    public static void disableForeignLogger() {
        Enumeration < String > loggerNames = LogManager.getLogManager()
            .getLoggerNames();

        while (loggerNames.hasMoreElements()) {
            String loggerName = loggerNames.nextElement();

            if ((!loggerName.startsWith("org.electrocodeogram"))
                && (!loggerName.equals(""))) {
                Logger alienLogger = Logger.getLogger(loggerName);

                alienLogger.setLevel(Level.OFF);
            }
        }
    }

    /**
     * This sets the logfile to use.
     * @param filename
     *            Is the name of the logfile
     * @throws IOException
     *             If such an <em>Exception</em> occurs while
     *             creating the logfile
     */
    public static void setLogFile(String filename) throws IOException {
        if (filename == null) {
            return;
        }

        String homeDir = System.getProperty("user.home");

        if (homeDir == null || homeDir.equals("")) {
            homeDir = ".";
        }

        File logDir = new File(homeDir + File.separator + LOG_DIR);

        if (!logDir.exists() || logDir.isFile()) {
            logDir.mkdir();
        }

        // If the filename isn't a complete file path but any other, put
        //   it in the {home}/ecg_log directory
        File lf = new File(filename);
        if (!lf.isAbsolute() || !lf.isFile())
            filename = homeDir + File.separator + LOG_DIR + File.separator
                       + filename;

        // Create a file handler that write log record to a file
        // called my.log
        FileHandler handler = new FileHandler(filename, FILE_SIZE, MAX_FILES,
            true);

        handler.setFormatter(new ECGFormatter());

        // Add to the desired logger
        Logger logger = Logger.getLogger("");

        if (logger != null) {
            logger.addHandler(handler);
        }

    }

    /**
     * This returns the Loglevel object to a given Loglevel string.
     * @param logLevel
     *            Is the Loglevel string, i.e. "verbose"
     * @return A Loglevel object, i.e. <em>Level.FINE</em>
     */
    public static Level getLogLevel(final String logLevel) {

        if (logLevel == null) {
            return DEFAULT_LEVEL;
        } else if (logLevel.equalsIgnoreCase("INFO")) {
            return Level.INFO;
        } else if (logLevel.equals("WARNING")) {
            return Level.WARNING;
        } else if (logLevel.equals("ERROR")) {
            return Level.SEVERE;
        } else if (logLevel.equals("DEBUG")) {
            return Level.FINEST;
        } else if (logLevel.equalsIgnoreCase("VERBOSE")) {
            return Level.FINE;
        } else if (logLevel.equalsIgnoreCase("PACKET")) {
            return ECGLevel.PACKET;
        } else {
            return DEFAULT_LEVEL;
        }
    }

    /**
     * This class is used to formatt the log entries for all
     * <em>Logger</em>.
     */
    private static class ECGFormatter extends Formatter {

        /**
         * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
         */
        @Override
        public String format(final LogRecord record) {

            Object[] parameters = record.getParameters();

            StringBuffer logEntry = new StringBuffer(); 
            logEntry.append("[" + record.getLevel() + "]  "
                              + new Date(record.getMillis()).toString() + " : "
                              + record.getSourceClassName() + "#"
                              + record.getSourceMethodName());

            if (parameters == null) {
                logEntry.append(" : " + record.getMessage() + "\r\n");
            } else {
                logEntry.append("(");

                for (int i = 0; i < parameters.length; i++) {
                    if (parameters[i] == null) {
                        logEntry.append("null");
                    } else {
                        Class clazz = parameters[i].getClass();

                        Object o = clazz.cast(parameters[i]);

                        logEntry.append(clazz.getSimpleName() + " " + o.toString());
                    }

                    if (i < parameters.length - 1) {
                        logEntry.append(", ");
                    }
                }

                logEntry.append(")");

                logEntry.append(" : " + record.getMessage() + "\r\n");
            }

            return logEntry.toString();
        }

    }

    /**
     * This class defines an additional Loglevel for the ECG.
     */
    public static class ECGLevel extends Level {

        /**
         * Is the <em>Serialization</em> id.
         */
        private static final long serialVersionUID = 8792104761302051549L;

        /**
         * This additional Loglevel is used in the ECG to log event
         * contents in addition to the <em>VERBOSE</em> Loglevel.
         */
        public static final Level PACKET = new ECGLevel("PACKET", 450);

        /**
         * Creates an additional Loglevel.
         * @param name
         *            IS the name of the Loglevel
         * @param value
         *            Is the value for the Loglevel
         */
        protected ECGLevel(final String name, final int value) {
            super(name, value);

        }

    }
}
