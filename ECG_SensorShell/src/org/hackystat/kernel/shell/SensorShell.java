package org.hackystat.kernel.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.client.IllegalHostOrPortException;
import org.electrocodeogram.client.SendingThread;
import org.electrocodeogram.event.EventPacket;
import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.hackystat.kernel.admin.SensorProperties;

/**
 * This class is the ECG SensorShell. It is named org.hackystat.kernel.shell.SensorShell
 * after the class provided by the HackyStat project (please visit www.hackystat.org for more information).
 * 
 * It's purpose is to be used by every ECG sensor for recorded data to be sended to the ECG Server & Lab.
 * So a sensor developer must not implement the functionality of sending data to the ECG server himself.
 * 
 * Because the ECG framework directly supports the usage of original HackyStat sensors
 * this class acts like the original HackyStat SensorShell class including the naming.
 * That means that every original HackyStat sensor is able to collect data for the ECG
 * framework by simply replacing the HackyStat's sensorshell.jar library with the ECG sensorshell.jar.
 * 
 * Sensors are able to use this class in to different ways depending on the sensor programming
 * language. If the sensor is written in Java, this class can be instanciated to a SensorShell
 * object. On the SensorShell object the method doCommand is called to send recorded data to the
 * ECG framework. The recorded data must at least conform to a HackyStat SensorDataType.
 * 
 * To support even sensors that are not written in Java, this class provides a main method that makes
 * it a process that communicates via standard-input and -output. The process is created via a
 * "java -jar sensorshell.jar" command at the operating system level. Every string that is passed
 * to the standard-input of the SensorShell process, is taken for collected data.   
 * 
 */
public class SensorShell
{
    /**
     * This is a reference to the SensorProperties object, that contains the information of
     * the "sensor.properties" file. The file is used to configure all sensors in the system.
     */
    private SensorProperties $properties = null;

    private Logger logger = null;

    private BufferedReader bufferedReader = null;

    private String cr = System.getProperty("line.separator");

    private String prompt = ">> ";

    private String delimiter = "#";

    private boolean $interactive;

     
    private SendingThread sendingThread = null;

    /**
     * This creates a ECG SensorShell instance with the given properties.
     * @param properties The properties to configure the ECG SensorShell
     * @param interactive Is "true" if the SensorShell is run as a process and "false" if it is
     * instantiated to a SensorShell object.
     */
    public SensorShell(SensorProperties properties, boolean interactive, String toolName)
    {

        this.$properties = properties;

        this.$interactive = interactive;
        
        this.logger = Logger.getLogger("ECG_SensorShell");

        this.bufferedReader = new BufferedReader(new InputStreamReader(
                System.in));
        
        try {
            
            // Try to create the SendingThread ith the given ECG server address information 
            this.sendingThread = new SendingThread(this.$properties.getECGServerAddress(), this.$properties.getECGServerPort());
        }
        catch (IllegalHostOrPortException e) {

            this.logger.log(Level.SEVERE, "The ECG Server's address is invalid.\nPlease check the ECG_SERVER_ADDRESS and ECG_SERVER_PORT values in the file \".hackystat/sensor.properties\" in your home directory.");

        }
        catch (UnknownHostException e) {

            this.logger.log(Level.SEVERE, "The ECG Server's address is invalid.\nPlease check the ECG_SERVER_ADDRESS and ECG_SERVER_PORT values in the file \".hackystat/sensor.properties\" in your home directory.");
        }
    }

    /**
     * This method is called by the ECG sensors that are able to have an object reference to the
     * SensorShell. Whenever they record an event they call the doCommand method and pass
     * the parameters according to the HackyStat SensorDataType and ECG MicroSensorDataType
     * definitions.
     * @param timeStamp The timeStamp of the event
     * @param sensorDataType The HackyStat SensorDataType or of the event
     * @param argList The argList of the event that contains additional data or an ECG MicroSensorDataType
     * @return "true" if the event's data is syntactically correct and "false" otherwise
     */
    public boolean doCommand(Date timeStamp, String sensorDataType, List argList)
    {
        // check parameters
        if (!EventPacket.isSyntacticallyCorrect(timeStamp, sensorDataType, argList)) {
            return false;
        }
      
        // pass EventPacket to SendingThread
        try {
            this.sendingThread.addEventPacket(new ValidEventPacket(0, timeStamp,
                    sensorDataType, argList));
        }
        catch (IllegalEventParameterException e) {

            // As parameters are proofed above, this should never occur.

            // TODO : write out message here

            e.printStackTrace();
        }
        return true;

    }

    /**
     * This method returns the SensorProperties that are declared in the "sensor.properties" file.
     * @return The SensorProperties
     */
    public SensorProperties getSensorProperties()
    {
        return this.$properties;
    }

    /**
     * This method is not implemented and only declared for compatibility reasons.
     */
    public void send()
    {
        return;
    }

    /**
     * This method is not implemented and only declared for compatibility reasons.
     * @param str not used
     */
    public void println(@SuppressWarnings("unused")
    String str)
    {
        return;
    }

    private void print(String line)
    {

        this.logger.info(line);

        if (this.$interactive) {
            System.out.print(line);
        }
    }

    private String readLine()
    {
        try {
            String line = this.bufferedReader.readLine();

            this.logger.log(Level.INFO, line + this.cr);

            return line;
        }
        catch (IOException e) {

            return "quit";

        }
    }

    private void printPrompt()
    {
        this.print(this.prompt);
    }

    private void quit()
    {
        this.logger.log(Level.INFO, "Quitting SensorShell");

        System.exit(0);

    }

    /**
     * The main method makes this class a process that continuously reads from standard-input.
     * Every string that is passed to its standard-input is handled as recorded event data.
     * @param args The first parameter shall be the tool name string and the second shall be the path to the "sensor.properties" file.
     */
    public static void main(String args[])
    {

        if ((args.length == 1) && (args[0].equalsIgnoreCase("-help"))) {
            System.out.println("java -jar sensorshell.jar [toolname] [sensor.properties]");
            return;
        }

      // Set Parameter 2 (sensor properties file) to supplied or default value. Exit if can't find it.

        SensorProperties sensorProperties;

        if (args.length >= 2) {
            sensorProperties = new SensorProperties("Shell", new File(args[1]));

            if (!sensorProperties.isFileAvailable()) {
                System.out.println("Could not find sensor.properties file. ");
                System.out.println("Expected in: " + sensorProperties.getAbsolutePath());
                System.out.println("Exiting...");
                return;
            }
        }
        else {
            sensorProperties = new SensorProperties("Shell");
        }

        boolean interactive = true;

        SensorShell shell = new SensorShell(sensorProperties, interactive, "");

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

        }
    }

}
