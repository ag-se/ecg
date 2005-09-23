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
 * 
 */
public class IOSourceModule extends SourceModule
{

	private Console _console;
	
	/**
	 * @param arg0
	 * @param arg1
	 */
	public IOSourceModule(String arg0, String arg1)
	{
		super(arg0, arg1);

	}

	/**
	 * @param propertyName
	 * @param propertyValue
	 * @throws ModulePropertyException
	 * 
	 */
	@Override
	public void setProperty(String propertyName, String propertyValue) throws ModulePropertyException
	{

	}

	public void analyseCoreNotification()
	{

	}

	/**
	 * @see org.electrocodeogram.module.Module#initialize()
	 */
	@Override
	public void initialize()
	{
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

	
	public void stopReader()
	{
		this._console.shutDown();
		
		this._console = null;
	}
	/**
	 * @see org.electrocodeogram.module.Module#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String propertyName)
	{
		return null;
	}

	private class Console extends Thread
	{

		private BufferedReader bufferedReader = null;

		private SourceModule _sourceModule;

		private StringTokenizer stringTokenizer;
		
		private boolean _run = true;

		/**
		 * Creates the console to manage the ECG Server & Lab.
		 * 
		 */
		public Console(SourceModule sourceModule)
		{
			this._sourceModule = sourceModule;

			this.bufferedReader = new BufferedReader(new InputStreamReader(
					System.in));

			System.out.println("ElectroCodeoGram Server & Lab is starting...");

		}

		
		public void shutDown()
		{
			this._run = false;
		}
		/**
		 * Here the reading of the console-input is done.
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
