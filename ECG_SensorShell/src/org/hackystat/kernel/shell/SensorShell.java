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
import org.electrocodeogram.logging.LogHelper;
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

	protected boolean _standalone;

	private static PrintWriter _logWriter;
	
	private static boolean _enableLogging;

	protected PrintWriter _toInlineServer;

	protected String ecgEclipseSensorPath;

	private static String LOG_SUFFIX = ".log";

	private static String LOG_PREFIX = "EcgEclipseSensor";
	
	private Level _logLevel;
	
	public static String ECG_LAB_PATH = "ECG_Lab_Path";
	
	public static String ECG_LOG_ENTRY = "ECG_Log_Entry";

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
		this._properties = properties;
		
		this._interactive = interactive;
		
		initializeLogging();

		initializeECGLabCommunication();
	}

	/**
	 * 
	 */
	private void initializeECGLabCommunication()
	{
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
	}

	/**
	 * 
	 */
	private void initializeLogging()
	{
		this._logger = LogHelper.createLogger(this);
		
		String logFile = this._properties.getProperty("ECG_LOG_FILE");
		
		if(logFile != null)
		{
			try
			{
				LogHelper.setLogFile(logFile);
			}
			catch (SecurityException e)
			{
				this._logger.log(Level.SEVERE,"Error while creating the logfile" + logFile);
				
				this._logger.log(Level.FINEST,e.getMessage());
			}
			catch (IOException e)
			{
				this._logger.log(Level.SEVERE,"Error while creating the logfile" + logFile);
				
				this._logger.log(Level.FINEST,e.getMessage());
			}
		}
		
		this._logLevel = LogHelper.getLogLevel(this._properties.getProperty("ECG_LOG_LEVEL"));
		
		LogHelper.setLogLevel(this._logLevel);
		
		this._logger.log(Level.ALL,"foobar");
		
		
		
	}

	private void startClientThread()
	{
		this._logger.log(Level.INFO,"Starting ClientThread...");

		try
		{

			// Try to create the SendingThread ith the given ECG server address information 
			this._sendingThread = new SendingThread(
					this._properties.getECGServerAddress(),
					this._properties.getECGServerPort());
		}
		catch (IllegalHostOrPortException e)
		{

			this._logger.log(Level.SEVERE,"The ECG Server's address is invalid.\nPlease check the ECG_SERVER_ADDRESS and ECG_SERVER_PORT values in the file \".hackystat/sensor.properties\" in your home directory.");
			

		}
		catch (UnknownHostException e)
		{

			this._logger.log(Level.SEVERE,"The ECG Server's address is invalid.\nPlease check the ECG_SERVER_ADDRESS and ECG_SERVER_PORT values in the file \".hackystat/sensor.properties\" in your home directory.");
		}

		this._standalone = true;

		this._logger.log(Level.INFO,"ClientThread started");
	}

	private void startInlineServer()
	{
		this._logger.log(Level.INFO,"Starting InlineServer...");
		
		Thread inlineServer = new InlineServer(this);
		
		inlineServer.start();
		
		this._logger.log(Level.INFO,"InlineServer started");
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
	 * @param commandType The HackyStat SensorDataType or of the event
	 * @param argList The argList of the event that contains additional data or an ECG MicroSensorDataType
	 * @return "true" if the event's data is syntactically correct and "false" otherwise
	 */
	public boolean doCommand(Date timeStamp, String commandType, List argList)
	{
		if (commandType.equals(ECG_LAB_PATH))
		{
			return analysePath(argList);
			
		}
		else
		{
			return analyseEvent(timeStamp, commandType, argList);
		}
		
	}


	private boolean analyseEvent(Date timeStamp, String commandType, List argList)
	{
		this._logger.log(Level.INFO,"Getting event packet...");

		ValidEventPacket packet;
		try
		{
			packet = new ValidEventPacket(0, timeStamp, commandType,
					argList);

			this._logger.log(Level.INFO,"Packet is valid.");
			
			this._logger.log(Level.FINEST,packet.toString());
		
		}
		catch (IllegalEventParameterException e)
		{
			
			this._logger.log(Level.WARNING,e.getMessage());

			return false;
		}

		if (this._standalone)
		{

			if (this._sendingThread != null)
			{
				// pass EventPacket to SendingThread
				this._sendingThread.addEventPacket(packet);

				this._logger.log(Level.INFO,"Event packet passed to sending Thread");
				
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

				this._logger.log(Level.INFO,"Event packet passed to InlineServer");
			}
		}
		
		return true;
	}

	
	
	private boolean analysePath(List argList)
	{
		if (this.ecgEclipseSensorPath == null)
		{
			if(argList == null)
			{
				return false;
			}
			
			this.ecgEclipseSensorPath = (String) argList.get(0);
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
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();

		if (_logWriter != null)
		{
			_logWriter.close();
		}
	}

	private static class StreamGobbler extends Thread
	{
		private InputStream _is;

		private String _type;
		
		private SensorShell _shell;

		StreamGobbler(InputStream is, String type, SensorShell shell)
		{
			this._is = is;
			
			this._type = type;
			
			this._shell = shell;
		}

		public void run()
		{
			try
			{
				InputStreamReader isr = new InputStreamReader(this._is);
				
				BufferedReader br = new BufferedReader(isr);
				
				String line = null;
				
				while ((line = br.readLine()) != null)
				{
					this._shell._logger.log(Level.INFO,this._type + ">" + line);
				}
			}
			catch (IOException e)
			{
				this._shell._logger.log(Level.SEVERE,"An error occured while reading from ECGLab process.");
				
				this._shell._logger.log(Level.FINEST,e.getMessage());
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

	private static class InlineServer extends Thread
	{
		private SensorShell _shell;
		
		public InlineServer(SensorShell shell)
		{
			this._shell = shell;
		}
		
		public void run()
		{
			while(this._shell.ecgEclipseSensorPath == null)
			{
				try
				{
					this._shell._logger.log(Level.WARNING,"Path to ECGLab not received yet. Waiting...");
					
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					this._shell._logger.log(Level.SEVERE,"Path to ECGLab not received yet.");
					
					this._shell._logger.log(Level.SEVERE,"Unable to start ECGLab as InlineServer.");
					
					return;
				}
			}

			try
			{

				if (this._shell.ecgEclipseSensorPath != null && !this._shell.ecgEclipseSensorPath.equals(""))
				{
					
					ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C","ecg.bat");

					pb.directory(new File(this._shell.ecgEclipseSensorPath));

					Process p = pb.start();

					this._shell._toInlineServer = new PrintWriter(new OutputStreamWriter(
							p.getOutputStream()));

					StreamGobbler errorGobbler = new StreamGobbler(
							p.getErrorStream(), "ERROR",this._shell);

					StreamGobbler outputGobbler = new StreamGobbler(
							p.getInputStream(), "OUTPUT",this._shell);

					errorGobbler.start();

					outputGobbler.start();

					this._shell._standalone = false;
				}
				else
				{
					this._shell._logger.log(Level.SEVERE,"ECGLab path is invalid.");
				}
			}
			catch (IOException e)
			{
				this._shell._logger.log(Level.SEVERE,"Unable to start ECGLab.");
				
				this._shell._logger.log(Level.FINEST,e.getMessage());
			}
		}
	}
}
