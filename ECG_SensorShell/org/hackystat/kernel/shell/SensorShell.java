/*
 * Created on 02.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.hackystat.kernel.shell;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.logging.Level;

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
 * It's purpose is to be used by every ECG sensor that is written in Java to take
 * the sensors recorded data and send it to the server. So a sensor developer must not
 * implement the functionality of sending data to the ECG server.
 * 
 * Because the ECG framework directly supports the usage of original HackyStat sensors
 * this class acts like the original HackyStat SensorShell class including the naming.
 * 
 * Instead of processing the sensor data into the HackyStat environment, it is passed
 * over to the ECG server.
 */
public class SensorShell
{
    private SensorProperties properties = null;
    
    private Logger logger = null;
    
    private BufferedReader bufferedReader = null;
    
    private String cr = System.getProperty("line.separator");
    
    private String prompt = ">> ";
    
    private boolean isInteractive = false;
    
    private String delimiter = "#";


    
    /**
     * This creates a ECG SensorShell instance with the given properties.
     * @param propertiesPar The properties to configure the ECG SensorShell
     * @param b not used
     * @param s not used
     */
    public SensorShell(SensorProperties propertiesPar, boolean b, String s)
    {
        // assert parameters
        assert(propertiesPar != null);
        
        this.properties = propertiesPar;
        
        this.logger = Logger.getLogger("ECG_SensorShell");
        
        this.isInteractive = b;
        
        this.bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    }

    public SensorShell(SensorProperties sensorProperties, boolean interactive, String toolName, boolean offlineEnabled, File commandFile) {
		
    	this(sensorProperties,interactive,toolName);
    	
	}

	/**
     * This method is called by the ECG sensors whenever they record an event. The data
     * of the event is then passed over to the singleton SendingThread and therefore
     * processed asynchroneously.
     * @param timeStamp The timeStamp of the event
     * @param commandName The HackyStat commandName of the event
     * @param argList The argList of the event
     * @return "true" if the event's data is syntactically valid and "false" otherwises
     */
    public boolean doCommand(Date timeStamp, String commandName, List argList)
    {
        // check parameters
        if(!EventPacket.isSyntacticallyCorrect(timeStamp,commandName,argList))
        {
            return false;
        }
        
        // assert parameters
        assert(EventPacket.isSyntacticallyCorrect(timeStamp,commandName,argList));
        
        // get SendingThread
        SendingThread sendingThread = null;
        
        try {
        	
            sendingThread = SendingThread.getInstance(this.properties.getECGServerAddress(),this.properties.getECGServerPort());
        }
        catch (IllegalHostOrPortException e) {
            
            this.logger.log(Level.SEVERE,"Die Adresse des ECG Servers ist ungültig.\nPrüfen Sie, ob in der Datei \".hackystat/sensor.properties\" in Ihrem Heimatverzeichnis gültige Werte für die Parameter ECG_SERVER_ADDRESS und ECG_SERVER_PORT angegeben sind");

            
        } catch (UnknownHostException e) {
			
            this.logger.log(Level.SEVERE,"Die Adresse des ECG Servers ist ungültig.\nPrüfen Sie, ob in der Datei \".hackystat/sensor.properties\" in Ihrem Heimatverzeichnis gültige Werte für die Parameter ECG_SERVER_ADDRESS und ECG_SERVER_PORT angegeben sind");
	}
      
        // must not be "null"
        assert(sendingThread != null);
        
        // pass EventPacket to SendingThread
        try {
            sendingThread.addEventPacket(new ValidEventPacket(0,timeStamp,commandName,argList));
        }
        catch (IllegalEventParameterException e) {
            
            // As parameters are proofed above, this should never occur.
            
            e.printStackTrace();
        }
        return true;
        
    }
    
    /**
     * 
     * @return
     */
    public SensorProperties getSensorProperties()
    {
        return this.properties;
    }
    
    /**
     * 
     */
    public void send()
    {
        
    }
    
    /**
     * @param str
     */
    public void println(String str)
    {
        
    }
    
    private void print(String line) {
        logger.info(line);
        if (isInteractive) {
          System.out.print(line);
        }
      }
    
    private String readLine() {
        try {
          String line = this.bufferedReader.readLine();
          logger.info(line + cr);
          return line;
        }
        catch (IOException e) {
          //logger.info(cr);
          return "quit";
        }
      }
    
    private void printPrompt() {
        this.print(this.prompt);
      }
    
    public static void main(String args[]) {
        // Print help line and exit if arg is -help.
        if ((args.length == 1) && (args[0].equalsIgnoreCase("-help"))) {
          System.out.println("java -jar sensorshell.jar [toolname] [sensor.properties] [no offline] "
               + "[command filename]");
          return;
        }
        
//        // Perform verification procedures and exit if arg is -verify.
//        if ((args.length == 1) && (args[0].equalsIgnoreCase("-verify"))) {
//          SensorShell.verifyClientSide();
//          return;
//        }

        // Set Parameter 1 (toolname) to supplied or default value.
        String toolName = (args.length > 0) ? args[0] : "interactive";

        // Set Parameter 2 (sensor properties file) to supplied or default value. Exit if can't find it.
        SensorProperties sensorProperties = (args.length >= 2) ?
            new SensorProperties("Shell", new File(args[1])) :
            new SensorProperties("Shell");
        if (!sensorProperties.isFileAvailable()) {
          System.out.println("Could not find sensor.properties file. ");
          System.out.println("Expected in: " + sensorProperties.getAbsolutePath());
          System.out.println("Exiting...");
          return;
        }

        // Set Parameter 3 (offline). True if we don't supply a value, false if any value supplied.
        boolean offlineEnabled = ((args.length < 3));

        // Set Parameter 4 (command file). Null if not supplied. Exit if supplied and bogus.
        File commandFile = null;
        if (args.length == 4) {
          commandFile = new File(args[3]);
          if (!(commandFile.exists() && commandFile.isFile())) {
            System.out.println("Could not find the command file. Exiting...");
            return;
          }
        }

        // Set interactive parameter. From command line, always interactive unless using command file.
        boolean interactive = ((commandFile == null));

        // Now create the shell instance, supplying it with all the appropriate arguments.
        SensorShell shell = new SensorShell(sensorProperties, interactive, toolName, offlineEnabled,
          commandFile);

        // Start processing commands either interactively or from the command file.
        int count = 0;
        while (true) {
          // Get the next command
          shell.printPrompt();
          String inputString = shell.readLine();

//          // Quit if necessary.
//          if (inputString.equalsIgnoreCase("quit")) {
//            shell.quit();
//            return;
//          }

//          // Print help strings.
//          if (inputString.equalsIgnoreCase("help")) {
//            shell.printHelp();
//            continue;
//          }

//          // Send all the data.
//          if (inputString.equalsIgnoreCase("send")) {
//            shell.send();
//            count = 0;
//            continue;
//          }

          // Otherwise it's an extended command.
          StringTokenizer tokenizer = new StringTokenizer(inputString, shell.delimiter);
          int numTokens = tokenizer.countTokens();
          // Go back to start of loop if the line is empty.
          if (numTokens == 0) {
            continue;
          }

          // Get the command name and any additional arguments.
          String commandName = tokenizer.nextToken();
          ArrayList argList = new ArrayList();
          while (tokenizer.hasMoreElements()) {
            argList.add(tokenizer.nextToken());
          }
          // Invoke the command with a timestamp of right now.
          shell.doCommand(new Date(), commandName, argList);

          // If the commandFile is large we can run into problems reading and sending the whole
          // thing at once. so, we break up the file.      
          if (count >= 500) {
            shell.send();
            count = 0;
            continue;
          }
          count++;
          
        }
      }
}
