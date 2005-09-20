package org.hackystat.kernel.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.client.IllegalHostOrPortException;
import org.electrocodeogram.client.SendingThread;
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
	private SensorProperties _properties = null;

	private Logger _logger = null;

	private BufferedReader _bufferedReader = null;

	private String _cr = System.getProperty("line.separator");

	private String _prompt = ">> ";

	private String _delimiter = "#";

	private boolean _interactive;

	private SendingThread _sendingThread = null;

	private boolean _standalone;

	private PrintWriter _logWriter;

	private PrintWriter _toInlineServer;

	private String ecgEclipseSensorPath;

	private static String LOG_SUFFIX = ".log";

	private static String LOG_PREFIX = "EcgEclipseSensor";

	/**
	 * This creates a ECG SensorShell instance with the given properties.
	 * @param properties The properties to configure the ECG SensorShell
	 * @param interactive Is "true" if the SensorShell is run as a process and "false" if it is
	 * instantiated to a SensorShell object.
	 * @param toolName Is the tool name
	 */
	public SensorShell(SensorProperties properties, boolean interactive, @SuppressWarnings("unused")
	String toolName)
	{
		this._logger = Logger.getLogger("ECG_SensorShell");

		String filename = LOG_PREFIX + new Date().toString().replace(' ', '_').replace(':', '_') + LOG_SUFFIX;

		try
		{
			this._logWriter = new PrintWriter(
					new FileWriter(new File(filename)));

			this._logWriter.println(new Date().toString());
		}
		catch (IOException e)
		{
			log(e.getMessage());
		}

		this._properties = properties;

		this._interactive = interactive;

		this._bufferedReader = new BufferedReader(new InputStreamReader(
				System.in));

		if (this._properties.getECGServerType().equals("INLINE"))
		{
			this.startInlineServer();
		}
		else if (this._properties.getECGServerType().equals("STANDALONE"))
		{
			this.startClientThread();
		}
		else
		{
			this.startClientThread();
		}

		log("SensorShell created");
	}

	private void startClientThread()
	{
		log("Starting ClientThread...");

		try
		{

			// Try to create the SendingThread ith the given ECG server address information 
			this._sendingThread = new SendingThread(
					this._properties.getECGServerAddress(),
					this._properties.getECGServerPort());
		}
		catch (IllegalHostOrPortException e)
		{

			this._logger.log(Level.SEVERE, "The ECG Server's address is invalid.\nPlease check the ECG_SERVER_ADDRESS and ECG_SERVER_PORT values in the file \".hackystat/sensor.properties\" in your home directory.");

		}
		catch (UnknownHostException e)
		{

			this._logger.log(Level.SEVERE, "The ECG Server's address is invalid.\nPlease check the ECG_SERVER_ADDRESS and ECG_SERVER_PORT values in the file \".hackystat/sensor.properties\" in your home directory.");
		}

		this._standalone = true;

		log("ClientThread started");
	}

	private void startInlineServer()
	{

		log("Starting InlineServer...");

		try
		{

			if (this.ecgEclipseSensorPath != null && !this.ecgEclipseSensorPath.equals(""))
			{
				ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C",
						"ecg.bat");

				pb.directory(new File(this.ecgEclipseSensorPath));

				log("PATH" + pb.directory().getAbsolutePath());

				Process p = pb.start();

				log("InlineServer started");

				this._toInlineServer = new PrintWriter(new OutputStreamWriter(
						p.getOutputStream()));

				StreamGobbler errorGobbler = new StreamGobbler(
						p.getErrorStream(), "ERROR");

				StreamGobbler outputGobbler = new StreamGobbler(
						p.getInputStream(), "OUTPUT");

				log("Streams to InlineServer established");

				errorGobbler.start();

				outputGobbler.start();

				this._standalone = false;
			}
		}
		catch (IOException e)
		{
			log(e.getMessage());
		}
	}

	/**
	 * This creates a ECG SensorShell instance with the given properties.
	 * @param properties The properties to configure the ECG SensorShell
	 * @param interactive Is "true" if the SensorShell is run as a process and "false" if it is
	 * instantiated to a SensorShell object.
	 */
	public SensorShell(SensorProperties properties, boolean interactive)
	{
		this(properties, interactive, "ElectroCodeoGram");
	}

	/**
	 * This creates a ECG SensorShell instance with the given properties.
	 * @param properties The properties to configure the ECG SensorShell
	 * @param interactive Is "true" if the SensorShell is run as a process and "false" if it is
	 * @param toolName Is the tool name
	 * instantiated to a SensorShell object.
	 * @param enableOfflineData Is not used
	 * @param commandFile Is not used
	 */
	public SensorShell(SensorProperties properties, boolean interactive, String toolName, @SuppressWarnings("unused")
	boolean enableOfflineData, @SuppressWarnings("unused")
	File commandFile)
	{
		this(properties, interactive, toolName);
	}

	/**
	 * This creates a ECG SensorShell instance with the given properties.
	 * @param properties The properties to configure the ECG SensorShell
	 * @param interactive Is "true" if the SensorShell is run as a process and "false" if it is
	 * @param toolName Is the tool name
	 * instantiated to a SensorShell object.
	 * @param enableOfflineData Is not used
	 */
	public SensorShell(SensorProperties properties, boolean interactive, String toolName, @SuppressWarnings("unused")
	boolean enableOfflineData)
	{
		this(properties, interactive, toolName);
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
		if (sensorDataType.equals("ECGEclipseSensorPath"))
		{
			log("Retreiving path of ECGEclipseSensor...");

			if (this.ecgEclipseSensorPath == null)
			{
				log("Path to ECGEclipseSensor is " + this.ecgEclipseSensorPath);

				this.ecgEclipseSensorPath = (String) argList.get(0);
			}
			else
			{
				log("Path has allready been set.");
			}
		}
		else
		{
			log("Retreiving event packet...");

			ValidEventPacket packet;
			try
			{
				packet = new ValidEventPacket(0, timeStamp, sensorDataType,
						argList);

				log("Packet is valid");

				log(packet.toString());
			}
			catch (IllegalEventParameterException e)
			{
				log(e.getMessage());

				return false;
			}

			if (this._standalone)
			{

				if (this._sendingThread != null)
				{
					// pass EventPacket to SendingThread
					this._sendingThread.addEventPacket(packet);

					log("Event packet passed to sending Thread");
				}
			}
			else
			{

				if (this._toInlineServer != null)
				{
					this._toInlineServer.flush();

					this._toInlineServer.print("MicroActivity#");

					this._toInlineServer.println(packet.toString());

					this._toInlineServer.flush();

					log("Event packet passed to InlineServer");
				}

			}
		}
		return true;

	}

	// Here starts the section of methods that are only implemented for compatibility issues

	/**
	 * This method returns the SensorProperties that are declared in the "sensor.properties" file.
	 * @return The SensorProperties
	 */
	public SensorProperties getSensorProperties()
	{
		return this._properties;
	}

	/**
	 * This method is not implemented and only declared for compatibility reasons.
	 * @return Is allways true to to tell the hackyStat sensors sending was successfull
	 */
	public boolean send()
	{
		return true;
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

	/**
	 * This method is not implemented and only declared for compatibility reasons.
	 * @return Is always true to tell HacyStat sensors that the server is pingable
	 */
	public boolean isServerPingable()
	{
		return true;
	}

	/**
	 * This method is not implemented and only declared for compatibility reasons.
	 * @param milliSecondsToWait Is not used
	 * @return s always true to tell HacyStat sensors that the server is pingable
	 */
	public boolean isServerPingable(@SuppressWarnings("unused")
	int milliSecondsToWait)
	{
		return true;
	}

	/**
	 * This method is not implemented and only declared for compatibility reasons.
	 * @return An empty String
	 */
	public String getResultMessage()
	{
		return "";
	}

	// End

	private void print(String line)
	{

		this._logger.info(line);

		if (this._interactive)
		{
			System.out.print(line);
		}
	}

	private String readLine()
	{
		try
		{
			String line = this._bufferedReader.readLine();

			this._logger.log(Level.INFO, line + this._cr);

			return line;
		}
		catch (IOException e)
		{

			return "quit";

		}
	}

	private void printPrompt()
	{
		this.print(this._prompt);
	}

	private void quit()
	{
		this._logger.log(Level.INFO, "Quitting SensorShell");

		System.exit(0);

	}

	/**
	 * The main method makes this class a process that continuously reads from standard-input.
	 * Every string that is passed to its standard-input is handled as recorded event data.
	 * @param args The first parameter shall be the tool name string and the second shall be the path to the "sensor.properties" file.
	 */
	public static void main(String args[])
	{

		Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());

		if ((args.length == 1) && (args[0].equalsIgnoreCase("-help")))
		{
			System.out.println("java -jar sensorshell.jar [toolname] [sensor.properties]");
			return;
		}

		// Set Parameter 2 (sensor properties file) to supplied or default value. Exit if can't find it.

		SensorProperties sensorProperties;

		if (args.length >= 2)
		{
			sensorProperties = new SensorProperties("Shell", new File(args[1]));

			if (!sensorProperties.isFileAvailable())
			{
				System.out.println("Could not find sensor.properties file. ");
				System.out.println("Expected in: " + sensorProperties.getAbsolutePath());
				System.out.println("Exiting...");
				return;
			}
		}
		else
		{
			sensorProperties = new SensorProperties("Shell");
		}

		boolean interactive = true;

		SensorShell shell = new SensorShell(sensorProperties, interactive, "");

		while (true)
		{

			shell.printPrompt();

			String inputString = shell.readLine();

			// Quit if necessary.
			if (inputString != null && inputString.equalsIgnoreCase("quit"))
			{
				shell.quit();

				return;
			}

			StringTokenizer tokenizer = new StringTokenizer(inputString,
					shell._delimiter);

			int numTokens = tokenizer.countTokens();

			if (numTokens == 0)
			{
				continue;
			}

			String commandName = tokenizer.nextToken();

			ArrayList<String> argList = new ArrayList<String>();

			while (tokenizer.hasMoreElements())
			{
				argList.add(tokenizer.nextToken());
			}

			shell.doCommand(new Date(), commandName, argList);

		}
	}

	/**
	 * @param string
	 */
	private void log(String string)
	{
		if (this._logWriter != null)
		{
			this._logWriter.println(string);

			this._logWriter.flush();
		}

	}

	/**
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();

		if (this._logWriter != null)
		{
			this._logWriter.close();
		}
	}

	private static class StreamGobbler extends Thread
	{
		InputStream _is;

		String _type;

		StreamGobbler(InputStream is, String type)
		{
			this._is = is;
			this._type = type;
		}

		public void run()
		{
			try
			{
				InputStreamReader isr = new InputStreamReader(this._is);
				
				BufferedReader br = new BufferedReader(isr);
				
				String line = null;
				
				while ((line = br.readLine()) != null)
					System.out.println(this._type + ">" + line);
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
		}
	}

	private static class DefaultExceptionHandler implements UncaughtExceptionHandler
	{
		public void uncaughtException(Thread t, Throwable e)
		{
			System.out.println("An uncaught Exception had occured:");

			System.out.println("Thread:" + t.getName());

			System.out.println("Class: " + t.getClass());

			System.out.println("State: " + t.getState());

			System.out.println("Message: " + e.getMessage());

			System.out.println("StackTrace: ");

			e.printStackTrace();

		}

	}

}
