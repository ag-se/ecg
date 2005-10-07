package org.electrocodeogram.module.source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.nio.CharBuffer;
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
import org.electrocodeogram.module.ModulePropertyException;
import org.electrocodeogram.moduleapi.system.IModuleSystemRoot;
import org.electrocodeogram.system.SystemRoot;

/**
 * This class is an ECG nodule that reads in ECG events form standard input.
 * It is primarilly used by the ECG EclipseSenor if it runs in InlineServer mode.
 * In that case the ECG Lab is started with this module and
 * the ECG EclipseSensor writes recorded events to the standard input of this module.
 */
public class IOSourceModule extends SourceModule
{

	static Logger _logger = LogHelper.createLogger(IOSourceModule.class.getName());
	
	private Console _console;
	
	/**
	 * The constructor creates the module instance. It is not to be called by
	 * developers, instead it is called from the ECG ModuleRegistry when the
	 * user requested a new instance of this module.
	 * 
	 * @param id
	 *            This is the unique String id of the module
	 * @param name
	 *            This is the name which is given to the module instance
	 */
	public IOSourceModule(String id, String name)
	{
		super(id, name);

	}

	/**
	 * @see org.electrocodeogram.module.Module#setProperty(java.lang.String,
	 *      java.lang.String)
	 * The method is not implemented in this module.      
	 */
	@SuppressWarnings("unused")
	@Override
	public void setProperty(String propertyName, @SuppressWarnings("unused") String propertyValue) throws ModulePropertyException
	{
		// not implemented
	}
	
	/**
	 * @see org.electrocodeogram.module.Module#analyseCoreNotification() This
	 * The method is not implemented in this module.
	 */
	@Override
	public void analyseCoreNotification()
	{
		// not implemented
	}

	/**
	 * @see org.electrocodeogram.module.Module#initialize()
	 * The method is not implemented in this module. 
	 */
	@Override
	public void initialize()
	{
		// not implemented
	}

	/**
	 * @see org.electrocodeogram.module.source.SourceModule#startReader(org.electrocodeogram.module.source.SourceModule)
	 */
	@Override
	public void startReader(SourceModule sourceModule) throws SourceModuleException
	{
		try
		{
			this._console = new Console(sourceModule);
		}
		catch (IOException e)
		{
			throw new SourceModuleException(e.getMessage());
		}

		this._console.start();

	}

	
	/**
	 * @see org.electrocodeogram.module.source.SourceModule#stopReader()
	 */
	@Override
	public void stopReader()
	{
		this._console.shutDown();
		
		this._console = null;
	}
	

	private class Console extends Thread
	{

		//private BufferedReader bufferedReader = null;
		
		private ObjectInputStream bufferedReader = null;

		private SourceModule _sourceModule;

		private StringTokenizer stringTokenizer;
		
		private boolean _run = true;

		/**
		 * This creates the Console Thread to
		 * continously read in events from standard input.
		 * @param sourceModule Is the SourceModule to which events are beeing passed
		 * @throws IOException 
		 */
		public Console(SourceModule sourceModule) throws IOException
		{
			this._sourceModule = sourceModule;

			this.bufferedReader = new ObjectInputStream(System.in);
		}

		/**
		 * This stops the Console Thread.
		 *
		 */
		public void shutDown()
		{
			this._run = false;
		}
		
		/**
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run()
		{
			
			Date timeStamp = null;
			
			String eventString;
			
			String[] eventStrings;
			
			int i;
			
			String token;
			
			String sensorDataTypeString;
			
			String argListString;
			
			StringTokenizer argListTokenizer;
			
			String[] argListStringArray;
			
			WellFormedEventPacket eventPacket;
			
			List argList;
			
			while (this._run)
			{

				System.out.println(this.getName() + " >>");

				Object inputObject = null;
				
				try
				{
					inputObject = this.readLine();
				}
				catch (ClassNotFoundException e)
				{
					_logger.log(Level.SEVERE,"An error occurred while receiving data.");
					
					return;
				}
				
				if (inputObject instanceof WellFormedEventPacket)
				{
					
					_logger.log(Level.INFO,"Event received");
					
					WellFormedEventPacket packet = (WellFormedEventPacket) inputObject;
					
					_logger.log(Level.INFO,packet.toString());
					
					this._sourceModule.append(packet);
	                
				}
				else if(inputObject instanceof String)
				{
					
					String string = (String) inputObject;
					
					_logger.log(Level.INFO,"Input is String");
					
					if(string.equals("quit"))
					{
						SystemRoot.getModuleInstance().quit();
					}
				}
			}
		}

		private Object readLine() throws ClassNotFoundException
		{
			
			try
			{
				Object object = this.bufferedReader.readObject();
				
				return object;
				
			}
			catch (IOException e)
			{
				return null;
			}
		}

	}
}
