package org.electrocodeogram.module.source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.module.ModulePropertyException;

/**
 * This class is an ECG nodule that reads in ECG events form standard input.
 * It is primarilly used by the ECG EclipseSenor if it runs in InlineServer mode.
 * In that case the ECG Lab is started with this module and
 * the ECG EclipseSensor writes recorded events to the standard input of this module.
 */
public class IOSourceModule extends SourceModule
{

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
	public void startReader(SourceModule sourceModule)
	{
		this._console = new Console(sourceModule);

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

		private BufferedReader bufferedReader = null;

		private SourceModule _sourceModule;

		private StringTokenizer stringTokenizer;
		
		private boolean _run = true;

		/**
		 * This creates the Console Thread to
		 * continously read in events from standard input.
		 * @param sourceModule Is the SourceModule to which events are beeing passed
		 */
		public Console(SourceModule sourceModule)
		{
			this._sourceModule = sourceModule;

			this.bufferedReader = new BufferedReader(new InputStreamReader(
					System.in));
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
			
			ValidEventPacket eventPacket;
			
			List argList;
			
			while (this._run)
			{

				System.out.println(this.getName() + " >>");

				String inputString = "" + this.readLine();

				System.out.println("Echo: " + inputString);

				if (inputString.startsWith("MicroActivity#"))
				{
					eventString = inputString.substring(new String(
							"MicroActivty#").length());

					this.stringTokenizer = new StringTokenizer(eventString,
							ValidEventPacket.EVENT_SEPARATOR);

					eventStrings = new String[this.stringTokenizer.countTokens()];

					i = 0;

					while (this.stringTokenizer.hasMoreTokens())
					{
						token = this.stringTokenizer.nextToken();

						if (token == null || token.equals(""))
						{
							break;
						}

						eventStrings[i++] = token;
					}

					try
					{
						timeStamp = new SimpleDateFormat(
								ValidEventPacket.DATE_FORMAT_PATTERN).parse(eventStrings[0]);
					}
					catch (ParseException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					sensorDataTypeString = eventStrings[1];

					argListString = eventStrings[2];

					argListTokenizer = new StringTokenizer(
							argListString, ValidEventPacket.ARGLIST_SEPARATOR);

					argListStringArray = new String[argListTokenizer.countTokens()];

					int j = 0;

					while (argListTokenizer.hasMoreTokens())
					{
						argListStringArray[j++] = argListTokenizer.nextToken();
					}

					argList = Arrays.asList(argListStringArray);
					
					eventPacket = null;
	                
	                
                    try
					{
						eventPacket = new ValidEventPacket(0,timeStamp,sensorDataTypeString,argList);
					}
					catch (IllegalEventParameterException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					this._sourceModule.append(eventPacket);
	                
				}
			}
		}

		private String readLine()
		{
			try
			{
				return this.bufferedReader.readLine();
			}
			catch (IOException e)
			{
				return null;
			}
		}

	}
}
